import math
import time
import logging
from typing import Dict, Any, Optional, Tuple, List
import httpx
from fastapi import HTTPException

from app.heat_risk_engine import BaselineHeatRiskEngine

logger = logging.getLogger(__name__)

class WeatherService:
    """
    Communicates with the Open-Meteo API to fetch current weather and hourly forecasts.
    Enforces strict coordinate validation, TTL caching, timeouts, and missing value preservation.
    """
    BASE_URL = "https://api.open-meteo.com/v1/forecast"
    CACHE_TTL_SECONDS = 300  # 5 minutes cache

    def __init__(self, timeout_seconds: float = 5.0):
        self.timeout_seconds = timeout_seconds
        self._cache: Dict[Tuple[float, float], Tuple[float, Dict[str, Any]]] = {}

    def validate_coordinates(self, latitude: float, longitude: float) -> None:
        if latitude is None or longitude is None:
            raise ValueError("Latitude and longitude coordinates are required.")
        if not isinstance(latitude, (int, float)) or not isinstance(longitude, (int, float)):
            raise ValueError("Coordinates must be numeric values.")
        if math.isnan(latitude) or math.isinf(latitude):
            raise ValueError("Latitude must be a finite number.")
        if math.isnan(longitude) or math.isinf(longitude):
            raise ValueError("Longitude must be a finite number.")
        if not (-90.0 <= latitude <= 90.0):
            raise ValueError(f"Latitude {latitude} out of valid range [-90, 90].")
        if not (-180.0 <= longitude <= 180.0):
            raise ValueError(f"Longitude {longitude} out of valid range [-180, 180].")

    def _get_from_cache(self, latitude: float, longitude: float) -> Optional[Dict[str, Any]]:
        key = (round(latitude, 3), round(longitude, 3))
        if key in self._cache:
            cached_time, data = self._cache[key]
            if time.time() - cached_time < self.CACHE_TTL_SECONDS:
                return data
            else:
                del self._cache[key]
        return None

    def _set_cache(self, latitude: float, longitude: float, data: Dict[str, Any]) -> None:
        key = (round(latitude, 3), round(longitude, 3))
        self._cache[key] = (time.time(), data)

    async def fetch_weather(self, latitude: float, longitude: float) -> Dict[str, Any]:
        try:
            self.validate_coordinates(latitude, longitude)
        except ValueError as e:
            raise HTTPException(status_code=400, detail=str(e))

        cached = self._get_from_cache(latitude, longitude)
        if cached is not None:
            return cached

        params = {
            "latitude": latitude,
            "longitude": longitude,
            "current": "temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m",
            "hourly": "temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m",
            "forecast_days": 1,
            "wind_speed_unit": "kmh",
            "timezone": "auto"
        }

        try:
            async with httpx.AsyncClient(timeout=self.timeout_seconds) as client:
                response = await client.get(self.BASE_URL, params=params)
                if response.status_code != 200:
                    logger.error(f"Open-Meteo returned status {response.status_code}: {response.text}")
                    raise HTTPException(
                        status_code=502,
                        detail=f"Weather provider error: HTTP {response.status_code}"
                    )
                try:
                    data = response.json()
                except Exception as json_err:
                    logger.error(f"Malformed JSON from weather provider: {json_err}")
                    raise HTTPException(
                        status_code=502,
                        detail="Malformed JSON response from weather provider."
                    )
        except httpx.TimeoutException:
            logger.error("Timeout connecting to Open-Meteo API.")
            raise HTTPException(
                status_code=504,
                detail="Weather provider gateway timeout. Please try again."
            )
        except httpx.RequestError as exc:
            logger.error(f"Network error requesting weather data: {exc}")
            raise HTTPException(
                status_code=502,
                detail="Network failure communicating with weather provider."
            )
        except HTTPException:
            raise
        except Exception as e:
            logger.error(f"Unexpected error fetching weather: {e}")
            raise HTTPException(
                status_code=500,
                detail="Internal error processing weather data."
            )

        parsed_weather = self._parse_open_meteo_response(data, latitude, longitude)
        self._set_cache(latitude, longitude, parsed_weather)
        return parsed_weather

    def _parse_open_meteo_response(
        self, data: Dict[str, Any], latitude: float, longitude: float
    ) -> Dict[str, Any]:
        if not isinstance(data, dict):
            raise HTTPException(
                status_code=502,
                detail="Invalid weather provider payload: root element must be a JSON object."
            )

        current = data.get("current")
        if not isinstance(current, dict):
            raise HTTPException(
                status_code=502,
                detail="Invalid response format from weather provider: missing or malformed 'current' object."
            )

        temperature = current.get("temperature_2m")
        relative_humidity = current.get("relative_humidity_2m")
        wind_speed = current.get("wind_speed_10m")
        apparent_temperature = current.get("apparent_temperature")
        timestamp = current.get("time")

        if temperature is None or relative_humidity is None:
            raise HTTPException(
                status_code=502,
                detail="Incomplete weather observations: required temperature or humidity field missing."
            )

        try:
            temp_val = float(temperature)
            humidity_val = float(relative_humidity)
        except (ValueError, TypeError):
            raise HTTPException(
                status_code=502,
                detail="Non-numeric value encountered for required temperature or humidity observation."
            )

        if math.isnan(temp_val) or math.isinf(temp_val) or not (-100.0 <= temp_val <= 80.0):
            raise HTTPException(
                status_code=502,
                detail=f"Out-of-range temperature value: {temp_val}°C."
            )

        if math.isnan(humidity_val) or math.isinf(humidity_val) or not (0.0 <= humidity_val <= 100.0):
            raise HTTPException(
                status_code=502,
                detail=f"Out-of-range relative humidity value: {humidity_val}%."
            )

        wind_val: Optional[float] = None
        if wind_speed is not None:
            try:
                w_float = float(wind_speed)
                if not math.isnan(w_float) and not math.isinf(w_float) and 0.0 <= w_float <= 250.0:
                    wind_val = w_float
            except (ValueError, TypeError):
                wind_val = None

        app_temp_val: Optional[float] = None
        if apparent_temperature is not None:
            try:
                app_float = float(apparent_temperature)
                if not math.isnan(app_float) and not math.isinf(app_float) and -100.0 <= app_float <= 80.0:
                    app_temp_val = app_float
            except (ValueError, TypeError):
                app_temp_val = None

        if not timestamp or not isinstance(timestamp, str):
            raise HTTPException(
                status_code=502,
                detail="Missing or invalid timestamp in weather provider response."
            )

        # Parse Hourly Forecast
        hourly_list: List[Dict[str, Any]] = []
        hourly_data = data.get("hourly")
        if isinstance(hourly_data, dict):
            h_times = hourly_data.get("time", [])
            h_temps = hourly_data.get("temperature_2m", [])
            h_hums = hourly_data.get("relative_humidity_2m", [])
            h_apps = hourly_data.get("apparent_temperature", [])
            h_winds = hourly_data.get("wind_speed_10m", [])

            # Take up to 24 hourly data points
            for i in range(min(24, len(h_times))):
                try:
                    t_str = str(h_times[i])
                    # Format time as HH:MM if ISO timestamp like "2025-05-15T14:00"
                    time_label = t_str.split("T")[-1] if "T" in t_str else t_str

                    ht = float(h_temps[i])
                    hrh = float(h_hums[i])
                    happ = float(h_apps[i]) if i < len(h_apps) and h_apps[i] is not None else ht
                    hwind = float(h_winds[i]) if i < len(h_winds) and h_winds[i] is not None else None

                    # Evaluate risk for this hour
                    h_risk = BaselineHeatRiskEngine.evaluate_risk(ht, hrh, hwind, happ)

                    hourly_list.append({
                        "time": time_label,
                        "temperature": ht,
                        "relative_humidity": hrh,
                        "apparent_temperature": happ,
                        "wind_speed": hwind,
                        "heat_risk_level": h_risk["level"],
                        "heat_index_score": h_risk["score"]
                    })
                except Exception as e:
                    logger.warning(f"Error parsing hourly forecast point {i}: {e}")

        return {
            "latitude": latitude,
            "longitude": longitude,
            "temperature": temp_val,
            "relative_humidity": humidity_val,
            "wind_speed": wind_val,
            "apparent_temperature": app_temp_val,
            "timestamp": timestamp,
            "source": "Open-Meteo API",
            "is_forecast": False,
            "hourly_forecast": hourly_list
        }
