import time
import threading
import urllib.request
import urllib.error
import json
import unittest
import uvicorn
from main import app

class FastAPIIntegrationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        # Start Uvicorn in a background thread for real HTTP testing
        cls.port = 8008
        cls.base_url = f"http://127.0.0.1:{cls.port}"
        cls.config = uvicorn.Config(app, host="127.0.0.1", port=cls.port, log_level="error")
        cls.server = uvicorn.Server(cls.config)
        cls.server_thread = threading.Thread(target=cls.server.run, daemon=True)
        cls.server_thread.start()
        # Wait briefly for server to bind
        time.sleep(1.5)

    @classmethod
    def tearDownClass(cls):
        cls.server.should_exit = True
        cls.server_thread.join(timeout=2)

    def test_api_001_root_endpoint(self):
        req = urllib.request.Request(f"{self.base_url}/")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            self.assertIn("application/json", response.headers.get("Content-Type", ""))
            data = json.loads(response.read().decode())
            self.assertEqual(data, {"app": "HeatHealth", "message": "Backend is running"})

    def test_api_002_health_endpoint(self):
        req = urllib.request.Request(f"{self.base_url}/health")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            self.assertIn("application/json", response.headers.get("Content-Type", ""))
            data = json.loads(response.read().decode())
            self.assertEqual(data, {
                "status": "healthy",
                "service": "heathealth-api",
                "ml_model_loaded": False
            })

    def test_api_003_docs_endpoint(self):
        req = urllib.request.Request(f"{self.base_url}/docs")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            content = response.read().decode()
            self.assertIn("swagger", content.lower())

    def test_api_004_openapi_json_endpoint(self):
        req = urllib.request.Request(f"{self.base_url}/openapi.json")
        with urllib.request.urlopen(req) as response:
            self.assertEqual(response.status, 200)
            data = json.loads(response.read().decode())
            self.assertIn("paths", data)
            self.assertIn("/", data["paths"])
            self.assertIn("/health", data["paths"])

    def test_api_005_unsupported_endpoint(self):
        req = urllib.request.Request(f"{self.base_url}/invalid-route")
        with self.assertRaises(urllib.error.HTTPError) as ctx:
            urllib.request.urlopen(req)
        self.assertEqual(ctx.exception.code, 404)

    def test_api_006_repeated_requests(self):
        for _ in range(10):
            with urllib.request.urlopen(f"{self.base_url}/health") as response:
                self.assertEqual(response.status, 200)

if __name__ == "__main__":
    unittest.main()
