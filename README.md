# HeatHealth — Heat-Health Risk Prediction & Early Warning System

> **Comprehensive Project Documentation — Phase 1, Phase 2, & Phase 3**

HeatHealth is an open-source health-technology application designed to help students, outdoor workers, athletes, and vulnerable individuals monitor heat-health risks and receive preventive early warning recommendations during extreme thermal conditions.

---

## 📚 Dedicated Phase Documentation Links
- 📘 [Phase 1 Detailed Documentation](README_PHASE1.md) — Foundation Dashboard & Local FastAPI Backend Setup.
- 📙 [Phase 2 Detailed Documentation](README_PHASE2.md) — Live Open-Meteo Integration, Heat-Risk Engine, Bottom Navigation & Hourly Forecasts.
- 📕 [Phase 3 Detailed Documentation](README_PHASE3.md) — ML Integration, `POST /predict` Endpoint, & Custom Risk Simulator.

---

## 📌 Project Overview & Roadmap

### **Phase 1: Foundation Dashboard & Backend Setup (Completed)**
- **Android App**: Modern, responsive Jetpack Compose dashboard with Material 3 design and clearly labeled demonstration data.
- **Backend Service**: Lightweight Python FastAPI backend providing system health checks and RESTful endpoints.
- **Zero-Cost Development**: 100% free, open-source stack designed to run locally without paid APIs, subscriptions, or cloud billing accounts.

### **Phase 2: Live Weather Integration, Heat-Risk Engine & Navigation (Completed)**
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

### **Phase 3: ML Integration & Heat Risk Prediction (Completed)**
- **Scikit-Learn RandomForest Pipeline**: Trainable model persisted via `joblib` (`heat_risk_model.joblib`), classifying heat strain risk into `Low`, `Moderate`, `High`, and `Extreme`.
- **FastAPI `POST /predict` Endpoint**: Accepts custom environmental inputs (`temperature_c`, `humidity_percent`, `wind_speed_kmh`) and returns ML risk classification, calculated score, prediction message, contributing factors, safety precautions, and prototype disclaimers.
- **Android Custom ML Risk Simulator Card**: Interactive Compose UI component on the Home screen enabling users to adjust weather variables via sliders and trigger real-time ML inference requests.
- **Model Transparency & Disclaimers**: Explicit prototype disclaimers (`is_demo_model: true`) clarifying that synthetic training metrics demonstrate formula approximation capabilities and do not constitute a clinical medical diagnosis.

> [!IMPORTANT]
> **Environmental Estimate Notice**: HeatHealth evaluates environmental thermal stress risk using meteorological data. It is an early-stage research prototype and does not provide an individualized medical diagnosis or official public weather warning.

---

## 🛠️ Complete Technology Stack

### Android Frontend
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Navigation**: Jetpack Navigation Compose (`androidx.navigation:navigation-compose:2.8.8`)
- **Networking**: Retrofit 2 + Gson Converter + OkHttp 3
- **Architecture**: MVVM (`DashboardViewModel`, `HeatHealthRepository`, `DashboardUiState`)
- **Minimum SDK**: API 26 (Android 8.0+)
- **Target SDK**: API 37 (Android 15+)
- **Theme**: Light health-tech theme (Teal `#00897B` primary accent, Amber `#F59E0B` heat indicators)

### Python Backend
- **Language**: Python 3.13+
- **Framework**: FastAPI + Pydantic v2
- **ASGI Server**: Uvicorn
- **HTTP Client**: HTTPX (Asynchronous)
- **Machine Learning**: pandas, NumPy, scikit-learn (`RandomForestClassifier`), joblib
- **Weather API**: Open-Meteo API (`https://api.open-meteo.com/v1/forecast`)
- **API Spec**: OpenAPI 3.0 / Swagger UI (`/docs`)

---

## 📂 Project Structure

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
│           │   │   ├── model/              # WeatherData, HourlyForecast, CustomPredictionState, RiskPrediction, DashboardUiState
│           │   │   └── repository/         # HeatHealthRepository (Retrofit API + predictCustomHeatRisk)
│           │   ├── network/
│           │   │   ├── BackendApiConfig.kt # Base URL Configuration (http://10.0.2.2:8000 / http://10.92.1.9:8000)
│           │   │   ├── HeatHealthApiService.kt # Retrofit Interface (@POST("predict"), @GET("api/v1/heat-risk"))
│           │   │   └── dto/                # HeatRiskResponseDto, PredictRequestDto, PredictResponseDto, HourlyWeatherDto
│           │   ├── ui/
│           │   │   ├── components/         # Header, WeatherSummary, HourlyForecast, CustomMlPredictor, RiskIndicator, Safety
│           │   │   ├── navigation/         # Bottom Navigation items & routes
│           │   │   ├── screens/            # MainScreen, DashboardScreen, RiskMapScreen, AlertsScreen, SafetyScreen
│           │   │   └── theme/              # Teal (#00897B) & Heat Amber (#F59E0B) Light Theme
│           │   └── viewmodel/              # DashboardViewModel (StateFlow & custom ML prediction methods)
│           └── res/
│               └── xml/
│                   └── network_security_config.xml # Development Cleartext Domain Security Rules
├── backend/                                # Python FastAPI Backend Module
│   ├── app/
│   │   ├── schemas.py                      # Pydantic JSON schemas (PredictRequest & PredictResponse)
│   │   ├── weather_service.py              # Open-Meteo API Client, 300s TTL cache, strict range validation
│   │   ├── heat_risk_engine.py             # Baseline NWS/WHO Rule Engine
│   │   ├── ml_pipeline.py                  # Scikit-learn RandomForest pipeline & predict_custom()
│   │   └── models/                         # Serialized ML model & metadata
│   ├── main.py                             # FastAPI entry point & REST endpoints (POST /predict)
│   ├── requirements.txt                    # Python dependencies
│   ├── run_audit_tests.py                  # Comprehensive audit test suite
│   └── test_backend.py                     # Integration test suite
├── build.gradle.kts                        # Root Gradle configuration
├── README.md                               # Combined project documentation
├── README_PHASE1.md                        # Dedicated Phase 1 documentation
├── README_PHASE2.md                        # Dedicated Phase 2 documentation
├── README_PHASE3.md                        # Dedicated Phase 3 documentation
└── .gitignore                              # Git exclusion rules
```

---

## ⚙️ Backend API Endpoints

The FastAPI backend runs locally on `http://127.0.0.1:8000` or `0.0.0.0:8000`:

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Root verification endpoint |
| `GET` | `/health` | Service status, default prediction method (`Baseline Rule Engine`), and ML model status |
| `POST` | `/predict` | **Phase 3**: Custom environmental ML heat-risk inference endpoint |
| `GET` | `/api/v1/weather?latitude={lat}&longitude={lon}` | Validated current weather & hourly forecast from Open-Meteo |
| `GET` | `/api/v1/heat-risk?latitude={lat}&longitude={lon}&use_ml={bool}` | Full heat-risk assessment, hourly forecast, contributing factors, and recommendations |
| `GET` | `/docs` | Interactive Swagger UI API documentation |
| `GET` | `/openapi.json` | Standard OpenAPI 3.0 JSON schema |

---

## 💻 How to Build & Run

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

## 🧪 Automated Test Summary

- **Phase 1 Unit Tests**: 100% Passed
- **Phase 2 Backend Audit & Integration Tests**: 100% Passed
- **Phase 3 `POST /predict` Backend Tests**: **16/16 PASSED (100% Success)**
- **Phase 3 Android Unit Tests**: **6/6 PASSED (100% Success)**
- **Android Gradle Assembly (`assembleDebug`)**: **BUILD SUCCESSFUL**
- **Android Static Analysis (`lintDebug`)**: **0 Fatal Errors**

---

## 🗓️ Project Roadmap

- [x] **Phase 1**: Foundation Dashboard & FastAPI Backend Local Setup.
- [x] **Phase 2**: Live Weather Data Integration (Open-Meteo), Heat-Risk Engine, Bottom Navigation, & Hourly Forecasts.
- [x] **Phase 3**: Machine Learning Integration & Custom Environmental Risk Prediction (`POST /predict`).
- [ ] **Phase 4**: Personalized User Profiles, Water Intake Tracking, & Local Room Database Persistence.

---

## 📜 License & Compliance

HeatHealth is developed under open-source licenses for academic and research purposes. All dependencies used are 100% free and open-source software (FOSS).
