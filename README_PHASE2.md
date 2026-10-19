# HeatHealth — Phase 2: Live Weather Integration, Heat-Risk Engine, Bottom Navigation & Early Warning System

> **Phase 2 Technical Documentation**

HeatHealth Phase 2 delivers a complete end-to-end integration between the Android Jetpack Compose application and the Python FastAPI backend, incorporating live meteorological data from Open-Meteo, a dual prediction engine, 24-hour hourly forecasts, bottom navigation with 4 distinct screens, and resilient network security handling.

---

## 📌 Phase 2 Scope & Objectives

Phase 2 builds a complete end-to-end system:
- **Live Weather Integration**: Real-time current weather and 24-hour hourly forecasts via the free, open-source **Open-Meteo API** (Temperature, Relative Humidity, Apparent "Feels-like" Temperature, and Wind Speed).
- **Dual Heat-Risk Prediction Engine**:
  - **Baseline Rule Engine**: Documented environmental thermal stress calculations based on National Weather Service (NWS) Heat Index equations and World Health Organization (WHO) safety principles.
  - **Experimental Machine Learning Pipeline**: Trainable scikit-learn `RandomForestClassifier` pipeline with baseline fallback logic and explicit dataset provenance labeling.
- **Enhanced Android UI & Bottom Navigation**:
  - **Home**: Live weather summary (`LIVE DATA` badge), hourly heat forecast carousel, risk level indicator, contributing factors list, safety recommendations, and disclaimer.
  - **Risk Map**: Regional heat stress monitoring zone selector (e.g., New Delhi, Bengaluru, Mumbai, Chennai, Kolkata) with location search.
  - **Alerts**: Early warning advisories (Peak Afternoon Extreme Heat Advisory, High Humidity Strain Warning) and customizable notification preference toggles.
  - **Safety**: Vulnerability profile selector (Outdoor Worker, Student, Senior Citizen, Athlete) with custom hydration targets, heat illness symptom checklists, and emergency helpline shortcuts (`108 / 112`).
- **Resilient Network Layer**: Retrofit 2 + Gson HTTP client with OkHttp timeouts, cleartext network security rules for local development (`10.0.2.2:8000` / `10.92.1.9:8000`), and explicit error recovery states with offline demo fallback.

---

## 🛠️ Phase 2 Technology Stack

### Android Frontend
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Navigation**: Jetpack Navigation Compose (`androidx.navigation:navigation-compose:2.8.8`)
- **Networking**: Retrofit 2 + Gson Converter + OkHttp 3
- **Architecture**: MVVM (`DashboardViewModel`, `HeatHealthRepository`, `DashboardUiState`)
- **Minimum SDK**: API 26 (Android 8.0+)
- **Target SDK**: API 37 (Android 15+)

### Python Backend
- **Language**: Python 3.13+
- **Framework**: FastAPI + Pydantic v2
- **ASGI Server**: Uvicorn
- **HTTP Client**: HTTPX (Asynchronous)
- **Machine Learning**: pandas, NumPy, scikit-learn (`RandomForestClassifier`), joblib
- **Weather API**: Open-Meteo API (`https://api.open-meteo.com/v1/forecast`)
- **API Spec**: OpenAPI 3.0 / Swagger UI (`/docs`)

---

## 📂 Phase 2 Project Structure

```text
HeatHealth/
├── app/                                    # Android Application Module
│   └── src/
│       ├── debug/                          # Debug Manifest (Cleartext Traffic enabled)
│       └── main/
│           ├── AndroidManifest.xml         # Internet Permission & Network Security Config
│           ├── java/com/heathealth/app/
│           │   ├── MainActivity.kt         # App Entry Activity (Launches MainScreen)
│           │   ├── data/
│           │   │   ├── model/              # WeatherData, HourlyForecast, RiskPrediction, DashboardUiState
│           │   │   └── repository/         # HeatHealthRepository (Retrofit API + Offline Sample Fallback)
│           │   ├── network/
│           │   │   ├── BackendApiConfig.kt # Base URL Configuration (http://10.0.2.2:8000 / http://10.92.1.9:8000)
│           │   │   ├── HeatHealthApiService.kt # Retrofit Interface
│           │   │   └── dto/                # HeatRiskResponseDto, WeatherResponseDto, HourlyWeatherDto
│           │   ├── ui/
│           │   │   ├── components/         # Header, WeatherSummary, HourlyForecast, RiskIndicator, Safety, Disclaimer
│           │   │   ├── navigation/         # Bottom Navigation items & routes
│           │   │   ├── screens/            # MainScreen, DashboardScreen, RiskMapScreen, AlertsScreen, SafetyScreen
│           │   │   └── theme/              # Teal (#00897B) & Heat Amber (#F59E0B) Light Theme
│           │   └── viewmodel/              # DashboardViewModel (StateFlow state management)
│           └── res/
│               └── xml/
│                   └── network_security_config.xml # Development Cleartext Domain Security Rules
├── backend/                                # Python FastAPI Backend Module
│   ├── app/
│   │   ├── schemas.py                      # Pydantic JSON schemas
│   │   ├── weather_service.py              # Open-Meteo API Client, 300s TTL cache, strict range validation
│   │   ├── heat_risk_engine.py             # Baseline NWS/WHO Rule Engine
│   │   ├── ml_pipeline.py                  # Scikit-learn RandomForest pipeline & predictor
│   │   └── models/                         # Serialized ML model & metadata
│   ├── main.py                             # FastAPI entry point & REST endpoints
│   ├── requirements.txt                    # Python dependencies
│   ├── run_audit_tests.py                  # Comprehensive audit test suite
│   └── test_backend.py                     # Integration test suite
├── build.gradle.kts                        # Root Gradle configuration
├── README.md                               # Main project documentation
├── README_PHASE1.md                        # Phase 1 documentation
├── README_PHASE2.md                        # Phase 2 documentation
└── .gitignore                              # Git exclusion rules
```

---

## ⚙️ Phase 2 Backend API Endpoints

The FastAPI backend runs locally on `http://127.0.0.1:8000` or `0.0.0.0:8000`:

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Root verification endpoint |
| `GET` | `/health` | Service status, default prediction method (`Baseline Rule Engine`), and ML model status |
| `GET` | `/api/v1/weather?latitude={lat}&longitude={lon}` | Validated current weather & hourly forecast from Open-Meteo |
| `GET` | `/api/v1/heat-risk?latitude={lat}&longitude={lon}&use_ml={bool}` | Full heat-risk assessment, hourly forecast, contributing factors, and recommendations |
| `GET` | `/docs` | Interactive Swagger UI API documentation |
| `GET` | `/openapi.json` | Standard OpenAPI 3.0 JSON schema |

---

## 💻 How to Build & Run Phase 2

### 1. Running the FastAPI Backend

```powershell
# Navigate to backend directory
cd backend

# Create virtual environment
python -m venv .venv

# Activate virtual environment (Windows PowerShell)
.\.venv\Scripts\Activate.ps1

# Install required dependencies
python -m pip install -r requirements.txt

# Start Uvicorn development server
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

To run test suites:
```powershell
python -m unittest run_audit_tests.py
python -m unittest test_backend.py
```

### 2. Building & Running the Android Application

1. Open **Android Studio**.
2. Select **File -> Open** and select the project root directory.
3. Allow Gradle to synchronize dependencies.
4. Run Android unit tests:
   ```powershell
   .\gradlew.bat testDebugUnitTest
   ```
5. Select an **Android Emulator** (API 26+) or attached **Physical Android Device**.
6. For a physical phone on local Wi-Fi, ensure `BackendApiConfig.PHONE_BASE_URL` in [BackendApiConfig.kt](file:///D:/AndroidStudioProjects/MINIproject/app/src/main/java/com/heathealth/app/network/BackendApiConfig.kt) matches your computer's local IP address (e.g. `http://10.92.1.9:8000/`).
7. Click **Run 'app'** (`Shift + F10`).

---

## 🧪 Phase 2 Automated Test Results

- **Backend Audit & Integration Tests**: **15/15 PASSED (100% Success)**
- **Android Unit Tests**: **6/6 PASSED (100% Success)**
- **Android Gradle Assembly (`assembleDebug`)**: **BUILD SUCCESSFUL**
- **Android Static Analysis (`lintDebug`)**: **0 Fatal Errors**
