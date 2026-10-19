import json
import unittest
from unittest.mock import patch, AsyncMock
from fastapi.testclient import TestClient
from fastapi import HTTPException

from main import app
from app.heat_risk_engine import BaselineHeatRiskEngine
from app.weather_service import WeatherService
from app.ml_pipeline import MLPipeline, MLRiskPredictor

class ComprehensiveBackendAudit(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.client = TestClient(app)
        cls.weather_service = WeatherService()

    def test_audit_A_health_endpoint(self):
        response = self.client.get("/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "healthy")
        self.assertEqual(data["service"], "heathealth-api")
        self.assertEqual(data["default_prediction_method"], "Baseline Rule Engine")
        self.assertIn("experimental_ml_model_loaded", data)

    def test_audit_B_weather_endpoint_bengaluru(self):
        response = self.client.get("/api/v1/weather?latitude=12.9716&longitude=77.5946")
        self.assertEqual(response.status_code, 200)
        data = response.json()

        self.assertIn("location", data)
        self.assertIn("weather", data)
        self.assertIn("units", data)

        self.assertEqual(data["location"]["latitude"], 12.9716)
        self.assertEqual(data["location"]["longitude"], 77.5946)
        self.assertIsInstance(data["weather"]["temperature"], (int, float))
        self.assertIsInstance(data["weather"]["relative_humidity"], (int, float))
        self.assertEqual(data["units"]["temperature"], "°C")
        self.assertEqual(data["units"]["relative_humidity"], "%")
        self.assertEqual(data["units"]["wind_speed"], "km/h")
        self.assertEqual(data["weather"]["source"], "Open-Meteo API")

    def test_audit_C_heat_risk_endpoint_default_baseline(self):
        # Default request (use_ml=False) must use Baseline Rule Engine
        response = self.client.get("/api/v1/heat-risk?latitude=12.9716&longitude=77.5946&label=Bengaluru")
        self.assertEqual(response.status_code, 200)
        data = response.json()

        self.assertIn("location", data)
        self.assertIn("weather", data)
        self.assertIn("risk", data)

        self.assertEqual(data["risk"]["method"], "Baseline Rule Engine")
        self.assertIn(data["risk"]["level"], ["Low", "Moderate", "High", "Very High"])

    def test_audit_C2_heat_risk_endpoint_experimental_ml(self):
        # Explicit request with use_ml=True must return Experimental ML status
        response = self.client.get("/api/v1/heat-risk?latitude=12.9716&longitude=77.5946&use_ml=true")
        self.assertEqual(response.status_code, 200)
        data = response.json()

        self.assertIn("Experimental ML Model", data["risk"]["method"])

    def test_audit_D_input_validation(self):
        # Latitude = 91
        r1 = self.client.get("/api/v1/weather?latitude=91.0&longitude=77.5946")
        self.assertIn(r1.status_code, [400, 422])

        # Latitude = -91
        r2 = self.client.get("/api/v1/weather?latitude=-91.0&longitude=77.5946")
        self.assertIn(r2.status_code, [400, 422])

        # Longitude = 181
        r3 = self.client.get("/api/v1/heat-risk?latitude=12.9716&longitude=181.0")
        self.assertIn(r3.status_code, [400, 422])

        # Longitude = -181
        r4 = self.client.get("/api/v1/heat-risk?latitude=12.9716&longitude=-181.0")
        self.assertIn(r4.status_code, [400, 422])

        # Missing coordinates
        r5 = self.client.get("/api/v1/weather")
        self.assertEqual(r5.status_code, 422)

        # Non-numeric coordinates
        r6 = self.client.get("/api/v1/weather?latitude=abc&longitude=xyz")
        self.assertEqual(r6.status_code, 422)

    def test_audit_E_missing_weather_values_and_strict_validation(self):
        # Test 1: Missing wind speed is preserved as None (NOT converted to 0.0)
        payload_no_wind = {
            "current": {
                "temperature_2m": 30.0,
                "relative_humidity_2m": 50.0,
                "apparent_temperature": 32.0,
                "time": "2025-05-15T12:00:00Z"
            }
        }
        res1 = self.weather_service._parse_open_meteo_response(payload_no_wind, 12.9716, 77.5946)
        self.assertIsNone(res1["wind_speed"])

        # Test 2: Out-of-range humidity (> 100%) produces HTTP 502
        payload_bad_humidity = {
            "current": {
                "temperature_2m": 30.0,
                "relative_humidity_2m": 150.0,
                "time": "2025-05-15T12:00:00Z"
            }
        }
        with self.assertRaises(HTTPException) as ctx2:
            self.weather_service._parse_open_meteo_response(payload_bad_humidity, 12.9716, 77.5946)
        self.assertEqual(ctx2.exception.status_code, 502)

        # Test 3: Missing timestamp produces HTTP 502
        payload_no_time = {
            "current": {
                "temperature_2m": 30.0,
                "relative_humidity_2m": 50.0
            }
        }
        with self.assertRaises(HTTPException) as ctx3:
            self.weather_service._parse_open_meteo_response(payload_no_time, 12.9716, 77.5946)
        self.assertEqual(ctx3.exception.status_code, 502)

        # Test 4: Missing temperature produces HTTP 502
        payload_no_temp = {
            "current": {
                "relative_humidity_2m": 50.0,
                "time": "2025-05-15T12:00:00Z"
            }
        }
        with self.assertRaises(HTTPException) as ctx4:
            self.weather_service._parse_open_meteo_response(payload_no_temp, 12.9716, 77.5946)
        self.assertEqual(ctx4.exception.status_code, 502)

    def test_audit_F_prediction_logic_rules(self):
        # Test Low risk boundary (< 27°C)
        res_low = BaselineHeatRiskEngine.evaluate_risk(22.0, 40.0, 10.0, 21.0)
        self.assertEqual(res_low["level"], "Low")

        # Test Moderate risk boundary (27°C - 32°C)
        res_mod = BaselineHeatRiskEngine.evaluate_risk(28.0, 50.0, 10.0, 29.0)
        self.assertEqual(res_mod["level"], "Moderate")

        # Test High risk boundary (32°C - 41°C)
        res_high = BaselineHeatRiskEngine.evaluate_risk(35.0, 60.0, 5.0, 38.0)
        self.assertEqual(res_high["level"], "High")

        # Test Very High risk boundary (>= 41°C)
        res_vh = BaselineHeatRiskEngine.evaluate_risk(42.0, 70.0, 5.0, 45.0)
        self.assertEqual(res_vh["level"], "Very High")

        # Test missing wind speed handling in Baseline Engine
        res_no_wind = BaselineHeatRiskEngine.evaluate_risk(35.0, 60.0, None, 38.0)
        self.assertIn("Wind speed measurement unavailable", res_no_wind["factors"])

    def test_audit_G_docs_and_openapi(self):
        r_docs = self.client.get("/docs")
        self.assertEqual(r_docs.status_code, 200)

        r_openapi = self.client.get("/openapi.json")
        self.assertEqual(r_openapi.status_code, 200)

if __name__ == "__main__":
    unittest.main()
