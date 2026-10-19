import time
import threading
import urllib.request
import urllib.error
import json
import unittest
import uvicorn

from main import app
from app.weather_service import WeatherService
from app.heat_risk_engine import BaselineHeatRiskEngine
from app.ml_pipeline import MLPipeline, MLRiskPredictor

class FastAPIIntegrationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.port = 8008
        cls.base_url = f"http://127.0.0.1:{cls.port}"
        cls.config = uvicorn.Config(app, host="127.0.0.1", port=cls.port, log_level="error")
        cls.server = uvicorn.Server(cls.config)
        cls.server_thread = threading.Thread(target=cls.server.run, daemon=True)
        cls.server_thread.start()
        time.sleep(1.5)

    @classmethod
    def tearDownClass(cls):
        cls.server.should_exit = True
        cls.server_thread.join(timeout=2)

    def test_api_001_root_endpoint(self):
        req = urllib.request.Request(f"{self.base_url}/")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertEqual(data, {"app": "HeatHealth", "message": "Backend is running"})

    def test_api_002_health_endpoint(self):
        req = urllib.request.Request(f"{self.base_url}/health")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertEqual(data["status"], "healthy")
            self.assertEqual(data["service"], "heathealth-api")
            self.assertEqual(data["default_prediction_method"], "Baseline Rule Engine")

    def test_api_003_invalid_coordinates(self):
        req = urllib.request.Request(f"{self.base_url}/api/v1/weather?latitude=95.0&longitude=77.0")
        with self.assertRaises(urllib.error.HTTPError) as ctx:
            urllib.request.urlopen(req)
        self.assertIn(ctx.exception.code, [400, 422])

        req = urllib.request.Request(f"{self.base_url}/api/v1/heat-risk?latitude=28.0&longitude=-190.0")
        with self.assertRaises(urllib.error.HTTPError) as ctx:
            urllib.request.urlopen(req)
        self.assertIn(ctx.exception.code, [400, 422])

    def test_api_004_weather_endpoint_valid(self):
        url = f"{self.base_url}/api/v1/weather?latitude=28.6139&longitude=77.2090"
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertIn("location", data)
            self.assertIn("weather", data)
            self.assertIn("units", data)
            self.assertEqual(data["location"]["latitude"], 28.6139)
            self.assertEqual(data["location"]["longitude"], 77.2090)
            self.assertIsInstance(data["weather"]["temperature"], (int, float))

    def test_api_005_heat_risk_endpoint_default_baseline(self):
        url = f"{self.base_url}/api/v1/heat-risk?latitude=28.6139&longitude=77.2090&label=New%20Delhi"
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertEqual(data["risk"]["method"], "Baseline Rule Engine")

    def test_rule_engine_boundary_values(self):
        low_res = BaselineHeatRiskEngine.evaluate_risk(
            temperature=22.0, relative_humidity=40.0, wind_speed=15.0, apparent_temperature=21.5
        )
        self.assertEqual(low_res["level"], "Low")

        mod_res = BaselineHeatRiskEngine.evaluate_risk(
            temperature=29.0, relative_humidity=50.0, wind_speed=10.0, apparent_temperature=29.5
        )
        self.assertEqual(mod_res["level"], "Moderate")

        high_res = BaselineHeatRiskEngine.evaluate_risk(
            temperature=35.0, relative_humidity=60.0, wind_speed=8.0, apparent_temperature=38.0
        )
        self.assertEqual(high_res["level"], "High")

        vh_res = BaselineHeatRiskEngine.evaluate_risk(
            temperature=42.0, relative_humidity=65.0, wind_speed=5.0, apparent_temperature=45.0
        )
        self.assertEqual(vh_res["level"], "Extreme")

    def test_ml_pipeline_train_and_predict(self):
        metadata = MLPipeline.train_and_save_model()
        self.assertIn("metrics", metadata)
        self.assertFalse(metadata["clinical_validation"])

        predictor = MLRiskPredictor()
        self.assertTrue(predictor.loaded)

        res = predictor.predict(
            temperature=36.0, relative_humidity=70.0, wind_speed=5.0, apparent_temperature=42.0, use_ml=True
        )
        self.assertIn("RandomForest ML Model", res["method"])

if __name__ == "__main__":
    unittest.main()
