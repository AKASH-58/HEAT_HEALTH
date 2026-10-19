import math
from typing import Dict, Any, List, Optional, Tuple

class BaselineHeatRiskEngine:
    """
    Documented rule-based heat-risk prediction engine based on National Weather Service (NWS)
    Heat Index thresholds and World Health Organization (WHO) environmental thermal stress principles.

    Provides a preliminary environmental estimate (not a medical diagnosis or clinical assessment).
    """

    MODERATE_THRESHOLD = 27.0   # 27°C (approx 80°F): Caution threshold
    HIGH_THRESHOLD = 32.0       # 32°C (approx 90°F): Extreme Caution / High Risk threshold
    EXTREME_THRESHOLD = 41.0    # 41°C (approx 105°F): Danger / Extreme Risk threshold

    RULES_VERSION = "1.0.0-baseline"

    @classmethod
    def calculate_heat_index_celsius(cls, temperature_c: float, humidity_percent: float) -> float:
        """
        Calculates Heat Index in Celsius using standard NOAA / NWS Rothfusz regression equation.
        """
        if temperature_c < 20.0:
            return temperature_c

        T = (temperature_c * 9.0 / 5.0) + 32.0
        RH = humidity_percent

        hi = 0.5 * (T + 61.0 + ((T - 68.0) * 1.2) + (RH * 0.094))

        if hi >= 80.0:
            hi = (
                -42.379
                + 2.04901523 * T
                + 10.14333127 * RH
                - 0.22475541 * T * RH
                - 0.00683783 * T * T
                - 0.05481717 * RH * RH
                + 0.00122874 * T * T * RH
                + 0.00085282 * T * RH * RH
                - 0.00000199 * T * T * RH * RH
            )

            if RH < 13.0 and 80.0 <= T <= 112.0:
                adj = ((13.0 - RH) / 4.0) * math.sqrt((17.0 - abs(T - 95.0)) / 17.0)
                hi -= adj
            elif RH > 85.0 and 80.0 <= T <= 87.0:
                adj = ((RH - 85.0) / 10.0) * ((87.0 - T) / 5.0)
                hi += adj

        hi_celsius = (hi - 32.0) * 5.0 / 9.0
        return round(hi_celsius, 1)

    @classmethod
    def evaluate_risk(
        cls,
        temperature: float,
        relative_humidity: float,
        wind_speed: Optional[float] = None,
        apparent_temperature: Optional[float] = None
    ) -> Dict[str, Any]:
        """
        Evaluates heat-health risk based on environmental weather variables.
        """
        calculated_hi = cls.calculate_heat_index_celsius(temperature, relative_humidity)
        effective_score = apparent_temperature if apparent_temperature is not None else calculated_hi
        effective_score = max(effective_score, temperature)

        if effective_score < cls.MODERATE_THRESHOLD:
            level = "Low"
            interpretation = (
                "Thermal conditions are within normal comfort limits for most healthy individuals. "
                "Low environmental heat stress risk."
            )
        elif effective_score < cls.HIGH_THRESHOLD:
            level = "Moderate"
            interpretation = (
                "Warm thermal conditions present moderate environmental heat strain. "
                "Fatigue is possible with prolonged outdoor exposure or strenuous activity."
            )
        elif effective_score < cls.EXTREME_THRESHOLD:
            level = "High"
            interpretation = (
                "Significant heat strain risk. Outdoor exertion or prolonged exposure is likely "
                "to cause heat cramps, exhaustion, or dehydration."
            )
        else:
            level = "Extreme"
            interpretation = (
                "Dangerous extreme thermal strain. High probability of heat stroke, fainting, or severe "
                "heat illness with continued outdoor exposure."
            )

        factors: List[str] = []
        if temperature >= 30.0:
            factors.append(f"Elevated air temperature ({temperature:.1f}°C)")
        elif temperature >= 25.0:
            factors.append(f"Warm ambient temperature ({temperature:.1f}°C)")

        if relative_humidity >= 60.0:
            factors.append(f"High relative humidity ({relative_humidity:.0f}%) restricts body sweat evaporation")
        elif relative_humidity <= 25.0 and temperature >= 30.0:
            factors.append(f"Low relative humidity ({relative_humidity:.0f}%) accelerates dehydration rate")

        if wind_speed is not None:
            if wind_speed < 8.0:
                factors.append(f"Low wind speed ({wind_speed:.1f} km/h) limits natural convective body cooling")
            elif wind_speed > 25.0 and temperature >= 35.0:
                factors.append(f"Hot wind convection ({wind_speed:.1f} km/h) may increase heat gain")
        else:
            factors.append("Wind speed measurement unavailable")

        if apparent_temperature is not None and (apparent_temperature - temperature) >= 2.0:
            factors.append(f"Apparent 'feels-like' temperature ({apparent_temperature:.1f}°C) exceeds air temperature")

        if not factors:
            factors.append("Thermal conditions are within normal baseline ranges.")

        recommendations = cls._generate_recommendations(level, temperature, relative_humidity)

        return {
            "level": level,
            "score": round(float(effective_score), 1),
            "score_interpretation": interpretation,
            "method": "Baseline Rule Engine",
            "factors": factors,
            "recommendations": recommendations,
            "rules_version": cls.RULES_VERSION
        }

    @classmethod
    def _generate_recommendations(
        cls, level: str, temperature: float, relative_humidity: float
    ) -> List[Dict[str, str]]:
        recs = [
            {
                "id": "1",
                "title": "Maintain regular hydration",
                "description": "Drink clean water frequently throughout the day, even before feeling thirsty.",
                "category": "Hydration"
            },
            {
                "id": "2",
                "title": "Take shaded breaks",
                "description": "Rest periodically in cool, ventilated, or shaded areas during outdoor activity.",
                "category": "Rest & Environment"
            }
        ]

        if level in ["High", "Extreme", "Very High"]:
            recs.append({
                "id": "3",
                "title": "Limit peak outdoor activity",
                "description": "Avoid heavy physical exertion during peak afternoon heat (11:00 AM to 4:00 PM).",
                "category": "Activity Reduction"
            })
            recs.append({
                "id": "4",
                "title": "Watch for heat illness signs",
                "description": "Monitor for dizziness, headache, nausea, rapid pulse, or muscle cramps.",
                "category": "Health Vigilance"
            })
        else:
            recs.append({
                "id": "3",
                "title": "Wear lightweight clothing",
                "description": "Wear loose, light-colored, and breathable garments when venturing outdoors.",
                "category": "Personal Protection"
            })
            recs.append({
                "id": "4",
                "title": "Stay aware of indoor heat",
                "description": "Ensure indoor spaces are well ventilated or fan-cooled during warm hours.",
                "category": "Indoor Comfort"
            })

        return recs
