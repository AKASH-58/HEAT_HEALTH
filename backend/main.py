import os
import time
import logging
from typing import Optional
from fastapi import FastAPI, Query, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.schemas import (
    WeatherResponse,
    HeatRiskResponse,
    LocationModel,
    WeatherModel,
    HourlyWeatherModel,
    RiskModel,
    RecommendationModel,
    MetadataModel
)
from app.weather_service import WeatherService
from app.heat_risk_engine import BaselineHeatRiskEngine
from app.ml_pipeline import MLPipeline, MLRiskPredictor

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("heathealth-backend")

app = FastAPI(
    title="HeatHealth API",
    description="Heat-Health Risk Prediction & Early Warning System API",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

weather_service = WeatherService(timeout_seconds=5.0)

# Load experimental ML predictor if trained
if not os.path.exists(os.path.join(os.path.dirname(__file__), "app", "models", "heat_risk_model.joblib")):
    try:
        logger.info("Training experimental ML risk model on initial startup...")
        MLPipeline.train_and_save_model()
    except Exception as e:
        logger.warning(f"Initial ML model training skipped or failed: {e}")

ml_predictor = MLRiskPredictor()

@app.get("/")
def read_root():
    return {
        "app": "HeatHealth",
        "message": "Backend is running"
    }

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "heathealth-api",
        "default_prediction_method": "Baseline Rule Engine",
        "experimental_ml_model_loaded": ml_predictor.loaded,
        "ml_dataset_status": "Synthetic rule-derived labels (Not clinically validated)"
    }

@app.get("/api/v1/weather", response_model=WeatherResponse)
async def get_weather(
    latitude: float = Query(..., ge=-90.0, le=90.0, description="Latitude from -90 to 90"),
    longitude: float = Query(..., ge=-180.0, le=180.0, description="Longitude from -180 to 180"),
    label: Optional[str] = Query(None, description="Optional location name")
):
    """
    Retrieves validated current weather metrics from Open-Meteo for specified coordinates.
    """
    raw_weather = await weather_service.fetch_weather(latitude, longitude)

    location = LocationModel(
        latitude=latitude,
        longitude=longitude,
        location_label=label
    )

    weather = WeatherModel(
        temperature=raw_weather["temperature"],
        relative_humidity=raw_weather["relative_humidity"],
        wind_speed=raw_weather["wind_speed"],
        apparent_temperature=raw_weather["apparent_temperature"],
        timestamp=raw_weather["timestamp"],
        source=raw_weather["source"],
        is_forecast=raw_weather["is_forecast"]
    )

    hourly = [HourlyWeatherModel(**h) for h in raw_weather.get("hourly_forecast", [])]

    units = {
        "temperature": "°C",
        "relative_humidity": "%",
        "wind_speed": "km/h",
        "apparent_temperature": "°C"
    }

    return WeatherResponse(
        location=location,
        weather=weather,
        hourly_forecast=hourly,
        units=units
    )

@app.get("/api/v1/heat-risk", response_model=HeatRiskResponse)
async def get_heat_risk(
    latitude: float = Query(..., ge=-90.0, le=90.0, description="Latitude from -90 to 90"),
    longitude: float = Query(..., ge=-180.0, le=180.0, description="Longitude from -180 to 180"),
    label: Optional[str] = Query(None, description="Optional location name"),
    use_ml: bool = Query(False, description="Whether to use experimental ML model (default: False, uses Baseline Rule Engine)")
):
    """
    Retrieves live weather data and evaluates environmental heat-health risk.
    Defaults to the documented Baseline Rule Engine.
    """
    raw_weather = await weather_service.fetch_weather(latitude, longitude)

    assessment = ml_predictor.predict(
        temperature=raw_weather["temperature"],
        relative_humidity=raw_weather["relative_humidity"],
        wind_speed=raw_weather["wind_speed"],
        apparent_temperature=raw_weather["apparent_temperature"],
        use_ml=use_ml
    )

    location = LocationModel(
        latitude=latitude,
        longitude=longitude,
        location_label=label or "Current Location"
    )

    weather = WeatherModel(
        temperature=raw_weather["temperature"],
        relative_humidity=raw_weather["relative_humidity"],
        wind_speed=raw_weather["wind_speed"],
        apparent_temperature=raw_weather["apparent_temperature"],
        timestamp=raw_weather["timestamp"],
        source=raw_weather["source"],
        is_forecast=raw_weather["is_forecast"]
    )

    hourly = [HourlyWeatherModel(**h) for h in raw_weather.get("hourly_forecast", [])]

    risk = RiskModel(
        level=assessment["level"],
        score=assessment["score"],
        score_interpretation=assessment["score_interpretation"],
        method=assessment["method"]
    )

    recommendations = [
        RecommendationModel(**rec) for rec in assessment["recommendations"]
    ]

    now_iso = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    metadata = MetadataModel(
        rules_version=assessment["rules_version"],
        model_version=assessment.get("model_version"),
        prediction_timestamp=now_iso,
        data_quality="Live observation from " + raw_weather["source"]
    )

    return HeatRiskResponse(
        location=location,
        weather=weather,
        hourly_forecast=hourly,
        risk=risk,
        factors=assessment["factors"],
        recommendations=recommendations,
        metadata=metadata
    )
