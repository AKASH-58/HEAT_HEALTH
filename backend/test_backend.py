import time
import threading
import urllib.request
import urllib.error
import json
import unittest
import uvicorn

import main
import unittest.mock

from main import app
from app.weather_service import WeatherService
from app.heat_risk_engine import BaselineHeatRiskEngine
from app.ml_pipeline import MLPipeline, MLRiskPredictor

class FastAPIIntegrationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.port = 8008
        cls.base_url = f"http://127.0.0.1:{cls.port}"

        # Mock external weather provider requests for deterministic offline test execution
        mock_weather = {
            "latitude": 28.6139,
            "longitude": 77.2090,
            "temperature": 35.0,
            "relative_humidity": 60.0,
            "wind_speed": 10.0,
            "apparent_temperature": 38.0,
            "timestamp": "2025-05-15T12:00:00Z",
            "source": "Mocked Test Provider",
            "is_forecast": False,
            "hourly_forecast": [
                {
                    "time": "12:00",
                    "temperature": 35.0,
                    "relative_humidity": 60.0,
                    "apparent_temperature": 38.0,
                    "wind_speed": 10.0,
                    "heat_risk_level": "High",
                    "heat_index_score": 38.0
                },
                {
                    "time": "13:00",
                    "temperature": 36.0,
                    "relative_humidity": 58.0,
                    "apparent_temperature": 40.0,
                    "wind_speed": 11.0,
                    "heat_risk_level": "High",
                    "heat_index_score": 40.0
                }
            ]
        }
        cls.weather_patcher = unittest.mock.patch.object(
            main.weather_service, "fetch_weather",
            new_callable=unittest.mock.AsyncMock,
            return_value=mock_weather
        )
        cls.weather_patcher.start()

        cls.config = uvicorn.Config(app, host="127.0.0.1", port=cls.port, log_level="error")
        cls.server = uvicorn.Server(cls.config)
        cls.server_thread = threading.Thread(target=cls.server.run, daemon=True)
        cls.server_thread.start()
        time.sleep(1.5)

    @classmethod
    def tearDownClass(cls):
        cls.weather_patcher.stop()
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

    def test_api_006_alert_history(self):
        url = f"{self.base_url}/api/v1/alerts/history"
        # Ensure at least one alert exists for the history test
        payload = json.dumps({
            "title": "History Test Advisory",
            "description": "Ensuring non-empty history for test verification",
            "risk_level": "Moderate",
            "trigger_source": "Test Suite Setup"
        }).encode('utf-8')
        req_post = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"}, method="POST")
        with urllib.request.urlopen(req_post) as resp_post:
            self.assertEqual(resp_post.status, 200)

        req = urllib.request.Request(url)
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertIn("alerts", data)
            self.assertIn("total_count", data)
            self.assertGreaterEqual(data["total_count"], 1)

    def test_api_007_alert_preferences(self):
        url = f"{self.base_url}/api/v1/alerts/preferences"
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertIn("heat_alerts_enabled", data)
            self.assertIn("alert_threshold", data)

    def test_api_008_alert_evaluate(self):
        url = f"{self.base_url}/api/v1/alerts/evaluate"
        payload = json.dumps({
            "temperature_c": 32.0,
            "humidity_percent": 40.0,
            "user_threshold": "High",
            "last_alerted_risk_level": ""
        }).encode('utf-8')
        req = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"}, method="POST")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertTrue(data["should_alert"])
            self.assertEqual(data["risk_level"], "High")

    def test_api_009_upcoming_forecast(self):
        url = f"{self.base_url}/api/v1/alerts/forecast?latitude=28.6139&longitude=77.2090"
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertIn("forecast_available", data)
            self.assertIn("items", data)

    def test_api_010_device_token(self):
        url = f"{self.base_url}/api/v1/alerts/device-token"
        payload = json.dumps({
            "device_token": "fcm_test_token_12345",
            "device_type": "android"
        }).encode('utf-8')
        req = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"}, method="POST")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertEqual(data["status"], "success")

    def test_api_011_disk_persistence_verification(self):
        # 1. Create alert log via API
        url = f"{self.base_url}/api/v1/alerts/history"
        payload = json.dumps({
            "title": "Persistence Test Alert",
            "description": "Verifying JSON file storage on disk",
            "risk_level": "High",
            "trigger_source": "Automated Persistence Test"
        }).encode('utf-8')
        req = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"}, method="POST")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)

        # 2. Update preferences via API
        url_pref = f"{self.base_url}/api/v1/alerts/preferences"
        payload_pref = json.dumps({
            "heat_alerts_enabled": True,
            "notifications_enabled": True,
            "alert_threshold": "Extreme",
            "reminder_frequency_hours": 4,
            "cooldown_minutes": 120,
            "last_alerted_risk_level": "High"
        }).encode('utf-8')
        req_pref = urllib.request.Request(url_pref, data=payload_pref, headers={"Content-Type": "application/json"}, method="PUT")
        with urllib.request.urlopen(req_pref) as response:
            self.assertEqual(response.status, 200)

        # 3. Read persisted disk files to verify durable persistence
        import os
        data_dir = os.path.join(os.path.dirname(__file__), "app", "data")
        history_path = os.path.join(data_dir, "alert_history.json")
        prefs_path = os.path.join(data_dir, "alert_preferences.json")

        self.assertTrue(os.path.exists(history_path))
        self.assertTrue(os.path.exists(prefs_path))

        with open(history_path, "r", encoding="utf-8") as f:
            saved_alerts = json.load(f)
            self.assertTrue(any(a["title"] == "Persistence Test Alert" for a in saved_alerts))

        with open(prefs_path, "r", encoding="utf-8") as f:
            saved_prefs = json.load(f)
            self.assertEqual(saved_prefs["alert_threshold"], "Extreme")
            self.assertEqual(saved_prefs["cooldown_minutes"], 120)

    def test_api_012_evaluate_cooldown_window(self):
        url = f"{self.base_url}/api/v1/alerts/evaluate"
        now_ms = int(time.time() * 1000)

        # Active cooldown: last alerted 30 mins ago, cooldown is 60 mins -> should NOT alert
        payload_active = json.dumps({
            "temperature_c": 32.0,
            "humidity_percent": 40.0,
            "user_threshold": "High",
            "last_alerted_risk_level": "High",
            "last_alerted_timestamp": now_ms - (30 * 60 * 1000),
            "cooldown_minutes": 60
        }).encode('utf-8')
        req = urllib.request.Request(url, data=payload_active, headers={"Content-Type": "application/json"}, method="POST")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertFalse(data["should_alert"])
            self.assertIn("cooldown", data["reason"].lower())

        # Expired cooldown: last alerted 90 mins ago, cooldown is 60 mins -> SHOULD alert
        payload_expired = json.dumps({
            "temperature_c": 32.0,
            "humidity_percent": 40.0,
            "user_threshold": "High",
            "last_alerted_risk_level": "High",
            "last_alerted_timestamp": now_ms - (90 * 60 * 1000),
            "cooldown_minutes": 60
        }).encode('utf-8')
        req_exp = urllib.request.Request(url, data=payload_expired, headers={"Content-Type": "application/json"}, method="POST")
        with urllib.request.urlopen(req_exp) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertTrue(data["should_alert"])

    def test_api_013_server_restart_persistence_and_clear(self):
        # 1. Create alert log via API
        url_create = f"{self.base_url}/api/v1/alerts/history"
        payload_create = json.dumps({
            "title": "Server Restart Verification Alert",
            "description": "Verifying survival across full process restart",
            "risk_level": "Extreme",
            "trigger_source": "Process Restart Test"
        }).encode('utf-8')
        req_create = urllib.request.Request(url_create, data=payload_create, headers={"Content-Type": "application/json"}, method="POST")
        with urllib.request.urlopen(req_create) as response:
            self.assertEqual(response.status, 200)

        # 2. Update preferences via API
        url_pref = f"{self.base_url}/api/v1/alerts/preferences"
        payload_pref = json.dumps({
            "heat_alerts_enabled": True,
            "notifications_enabled": True,
            "alert_threshold": "Moderate",
            "reminder_frequency_hours": 1,
            "cooldown_minutes": 45,
            "last_alerted_risk_level": "Moderate"
        }).encode('utf-8')
        req_pref = urllib.request.Request(url_pref, data=payload_pref, headers={"Content-Type": "application/json"}, method="PUT")
        with urllib.request.urlopen(req_pref) as response:
            self.assertEqual(response.status, 200)

        # 3. Simulate process restart by instantiating a new uvicorn server on port 8009
        port2 = 8009
        base_url2 = f"http://127.0.0.1:{port2}"
        config2 = uvicorn.Config(app, host="127.0.0.1", port=port2, log_level="error")
        server2 = uvicorn.Server(config2)
        thread2 = threading.Thread(target=server2.run, daemon=True)
        thread2.start()
        time.sleep(1.0)

        try:
            # Query history on new server instance
            req_hist = urllib.request.Request(f"{base_url2}/api/v1/alerts/history")
            with urllib.request.urlopen(req_hist) as resp_hist:
                self.assertEqual(resp_hist.status, 200)
                data_hist = json.loads(resp_hist.read().decode())
                self.assertTrue(any(a["title"] == "Server Restart Verification Alert" for a in data_hist["alerts"]))

            # Query preferences on new server instance
            req_get_pref = urllib.request.Request(f"{base_url2}/api/v1/alerts/preferences")
            with urllib.request.urlopen(req_get_pref) as resp_pref:
                self.assertEqual(resp_pref.status, 200)
                data_pref = json.loads(resp_pref.read().decode())
                self.assertEqual(data_pref["alert_threshold"], "Moderate")
                self.assertEqual(data_pref["cooldown_minutes"], 45)

            # 4. Test clearing history through API
            req_clear = urllib.request.Request(f"{base_url2}/api/v1/alerts/history", method="DELETE")
            with urllib.request.urlopen(req_clear) as resp_clear:
                self.assertEqual(resp_clear.status, 200)

            # Verify history is cleared on query
            req_hist_cleared = urllib.request.Request(f"{base_url2}/api/v1/alerts/history")
            with urllib.request.urlopen(req_hist_cleared) as resp_cleared:
                self.assertEqual(resp_cleared.status, 200)
                data_cleared = json.loads(resp_cleared.read().decode())
                self.assertEqual(data_cleared["total_count"], 0)
        finally:
            server2.should_exit = True
            thread2.join(timeout=2)

    def test_api_014_disabled_preferences_enforcement_in_evaluate(self):
        url = f"{self.base_url}/api/v1/alerts/evaluate"
        payload_disabled = json.dumps({
            "temperature_c": 42.0,  # Extreme heat risk
            "humidity_percent": 65.0,
            "user_threshold": "Moderate",
            "heat_alerts_enabled": False,
            "notifications_enabled": True
        }).encode('utf-8')
        req = urllib.request.Request(url, data=payload_disabled, headers={"Content-Type": "application/json"}, method="POST")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertFalse(data["should_alert"])
            self.assertIn("disabled", data["reason"].lower())

    def test_api_015_corrupted_json_persistence_recovery(self):
        import os
        from main import load_persisted_alerts, ALERTS_PERSIST_FILE

        corrupt_path = ALERTS_PERSIST_FILE + ".test_corrupt"
        with open(corrupt_path, "w", encoding="utf-8") as f:
            f.write("TRUNCATED_MALFORMED_JSON_DATABYTES_CORRUPT{{{")

        # Test recovery logic when file is unparseable
        original_path = main.ALERTS_PERSIST_FILE
        main.ALERTS_PERSIST_FILE = corrupt_path

        try:
            recovered_alerts = load_persisted_alerts()
            self.assertIsInstance(recovered_alerts, list)
            self.assertGreaterEqual(len(recovered_alerts), 1)
            self.assertTrue(os.path.exists(corrupt_path + ".bak"))
        finally:
            main.ALERTS_PERSIST_FILE = original_path
            if os.path.exists(corrupt_path):
                try: os.remove(corrupt_path)
                except Exception: pass
            if os.path.exists(corrupt_path + ".bak"):
                try: os.remove(corrupt_path + ".bak")
                except Exception: pass

if __name__ == "__main__":
    unittest.main()
