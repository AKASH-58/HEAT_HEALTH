# HeatHealth — Phase 1: Foundation Dashboard & Local FastAPI Backend

> **Phase 1 Technical Documentation**

HeatHealth Phase 1 establishes the core technical foundation for the HeatHealth ecosystem: a responsive Android Jetpack Compose dashboard with Material 3 design and a lightweight Python FastAPI backend service providing system health checks and RESTful endpoints.

---

## 📌 Phase 1 Objectives & Scope

Phase 1 established the foundation for the HeatHealth ecosystem:
- **Android App**: Modern, responsive Jetpack Compose dashboard with Material 3 design and clearly labeled demonstration data.
- **Backend Service**: Lightweight Python FastAPI backend providing system health checks and RESTful endpoints.
- **Zero-Cost Development**: 100% free, open-source stack designed to run locally without paid APIs, subscriptions, or cloud billing accounts.

> [!IMPORTANT]
> **Phase 1 Prototype Disclaimer**: All weather metrics (34°C Temp, 65% Humidity, 38°C Feels-like) and risk levels (Moderate) in Phase 1 were illustrative demonstration values. HeatHealth is an early-stage research prototype and does not provide a medical diagnosis or official weather warning.

---

## 🛠️ Phase 1 Technology Stack

### Android Frontend
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Minimum SDK**: API 26 (Android 8.0+)
- **Architecture**: MVVM (`DashboardViewModel`, `HeatHealthRepository`, `DashboardUiState`)
- **Theme**: Light health-tech theme (Teal `#00897B` primary accent, Amber `#F59E0B` heat indicators)

### Python Backend
- **Language**: Python 3.13+
- **Framework**: FastAPI
- **ASGI Server**: Uvicorn
- **API Spec**: OpenAPI 3.0 / Swagger UI (`/docs`)

---

## 📂 Phase 1 Project Structure

```text
HeatHealth/
├── app/                                    # Android Application Module
│   └── src/main/java/com/heathealth/app/
│       ├── MainActivity.kt                 # Entry Activity
│       ├── data/                           # Data Models & Repositories
│       │   ├── model/                      # Weather, Risk, & Recommendation models
│       │   └── repository/                 # HeatHealthRepository (Sample data)
│       ├── network/                        # Backend API Configuration (10.0.2.2:8000)
│       ├── ui/                             # Jetpack Compose UI Layer
│       │   ├── components/                 # Header, Weather, Risk, & Disclaimer cards
│       │   ├── screens/                    # DashboardScreen layout
│       │   └── theme/                      # Color, Theme, & Typography
│       └── viewmodel/                      # DashboardViewModel (StateFlow state)
├── backend/                                # FastAPI Backend
│   ├── main.py                             # FastAPI application & endpoints
│   ├── requirements.txt                    # Minimal backend dependencies
│   └── test_backend.py                     # Integration test suite for backend
├── build.gradle.kts                        # Root Gradle configuration
├── README.md                               # Project documentation
└── .gitignore                              # Git exclusion rules
```

---

## 🚀 Phase 1 Dashboard Features

1. **Application Header**: Title branding (`HeatHealth`), heat thermostat icon, and notification bell (visual indicator).
2. **Weather Summary Card**:
   - Temperature: **34°C**
   - Humidity: **65%**
   - Feels-Like: **38°C**
   - Clearly badged with **`DEMONSTRATION DATA`**.
3. **Sample Risk Indicator**:
   - Category: **Moderate**
   - Badged with **`Sample Risk` / `Demo Prediction`**.
   - Accompanied by descriptive heat-strain guidance.
4. **Safety Recommendations**:
   - Actionable guidelines: Hydration, shaded breaks, avoiding peak heat, and monitoring warning signs (dizziness, nausea, weakness).
5. **Prototype Disclaimer**:
   - Explicit disclaimer clarifying non-medical status.

---

## ⚙️ Phase 1 Backend API Endpoints

The backend ran locally on `http://127.0.0.1:8000`:

| Method | Endpoint | Description | Sample JSON Response |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Root verification | `{"app": "HeatHealth", "message": "Backend is running"}` |
| `GET` | `/health` | Service status | `{"status": "healthy", "service": "heathealth-api", "ml_model_loaded": false}` |
| `GET` | `/docs` | Interactive Swagger UI | Interactive HTML API documentation |
| `GET` | `/openapi.json` | OpenAPI 3.0 Schema | Standard OpenAPI JSON definition |

---

## 💻 How to Build & Run Phase 1

### 1. Running the FastAPI Backend (Windows PowerShell)

```powershell
# Navigate to backend directory
cd backend

# Create virtual environment
python -m venv venv

# Activate virtual environment
.\venv\Scripts\Activate.ps1

# Install requirements
pip install -r requirements.txt

# Run Uvicorn development server
uvicorn main:app --reload --port 8000
```

To test backend endpoints in PowerShell:
```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/"
Invoke-RestMethod -Uri "http://127.0.0.1:8000/health"
```

### 2. Building the Android Application

1. Open **Android Studio**.
2. Select **File -> Open** and select the project root directory.
3. Allow Gradle to synchronize dependencies.
4. Run unit tests to verify app logic:
   ```powershell
   .\gradlew.bat testDebugUnitTest
   ```
5. Select an Android Emulator (API 26+) or connected physical device and click **Run 'app'** (`Shift + F10`).

---

## 🧪 Phase 1 Testing Summary

All Phase 1 tests passed with 100% success rate:
- **Android Unit Tests**: 5/5 Passed (`testDebugUnitTest`).
- **Backend Tests**: 6/6 Passed (`test_backend.py`).
- **Zero-Cost Audit**: 100% Free / Open-source ($0 budget required).
