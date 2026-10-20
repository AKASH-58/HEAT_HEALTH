# HeatHealth — Heat-Health Risk Prediction & Early Warning System

> **Comprehensive Project Documentation — Phase 1, Phase 2, Phase 3, & Phase 4**

HeatHealth is an open-source health-technology application designed to help students, outdoor workers, athletes, and vulnerable individuals monitor heat-health risks and receive preventive early warning recommendations during extreme thermal conditions.

---

## 📚 Dedicated Phase Documentation Links
- 📘 [Phase 1 Detailed Documentation](README_PHASE1.md) — Foundation Dashboard & Local FastAPI Backend Setup.
- 📙 [Phase 2 Detailed Documentation](README_PHASE2.md) — Live Open-Meteo Integration, Heat-Risk Engine, Bottom Navigation & Hourly Forecasts.
- 📕 [Phase 3 Detailed Documentation](README_PHASE3.md) — ML Integration, `POST /predict` Endpoint, & Custom Risk Simulator.
- 📗 [Phase 4 Detailed Documentation](README_PHASE4.md) — Student Profiles, Water Intake Tracking, Room Database, & DataStore Persistence.

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
  - **Alerts**: Early warning advisories and customizable notification preference toggles.
  - **Safety**: Vulnerability profile selector (Outdoor Worker, Student, Senior Citizen, Athlete) with custom hydration targets, heat illness symptom checklists, and emergency helpline shortcuts (`108 / 112`).

### **Phase 3: ML Integration & Heat Risk Prediction (Completed)**
- **Scikit-Learn RandomForest Pipeline**: Trainable model persisted via `joblib` (`heat_risk_model.joblib`), classifying heat strain risk into `Low`, `Moderate`, `High`, and `Extreme`.
- **FastAPI `POST /predict` Endpoint**: Accepts custom environmental inputs (`temperature_c`, `humidity_percent`, `wind_speed_kmh`) and returns ML risk classification, score, contributing factors, and precautions.
- **Android Custom ML Risk Simulator Card**: Interactive Compose UI component on the Home screen enabling users to adjust weather variables via sliders and trigger real-time ML inference requests.

### **Phase 4: Personalized Student Profiles, Water Intake Tracking, & Room Persistence (Completed)**
- **Feature A — Personalized Student Profile**: Customizable student nickname, activity pattern selector, language preference (*"English"*, *"Hindi"*), validated daily hydration target (500–5000 mL), and reminder settings.
- **Feature B — Daily Water Intake Tracker**: Interactive hydration tracker with live progress bar and percentage, quick-add buttons (+150 mL, +250 mL, +500 mL), custom volume entry with strict validation, today's history log with timestamp formatting, entry deletion with **Undo** snackbar, and local date boundary filtering (`ZoneId.systemDefault()`).
- **Feature C — Structured Local Room Persistence**: Room Database (`HeatHealthDatabase`) storing profile settings (`student_profile`) and individual water intake records (`water_intake`).
- **Feature D — Preferences DataStore**: Preferences DataStore (`user_preferences`) storing reminder toggles and frequency settings.
- **Resilient Multi-Endpoint Failover**: Intelligent failover networking supporting USB reverse tunnel (`http://127.0.0.1:8000/`), local Wi-Fi IP (`http://10.92.1.9:8000/`), and Android Emulator (`http://10.0.2.2:8000/`).

> [!IMPORTANT]
> **Environmental Estimate Notice**: HeatHealth evaluates environmental thermal stress risk using meteorological data. It is an early-stage research prototype and does not provide an individualized medical diagnosis or official public weather warning.

---

## 🛠️ Complete Technology Stack

### Android Frontend
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Navigation**: Jetpack Navigation Compose (`androidx.navigation:navigation-compose:2.8.8`)
- **Local Database**: Android Room 2.7 (`androidx.room:room-runtime`, `androidx.room:room-ktx`, `androidx.room:room-compiler` via KSP)
- **Preferences**: Android DataStore (`androidx.datastore:datastore-preferences:1.1.2`)
- **Networking**: Retrofit 2 + Gson Converter + OkHttp 3 (with multi-endpoint candidate failover)
- **Architecture**: MVVM (`DashboardViewModel`, `StudentProfileViewModel`, `WaterTrackerViewModel`, Repositories, StateFlow)
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

---

## 📂 Project Structure

```text
HeatHealth/
├── app/                                    # Android Application Module
│   └── src/
│       ├── debug/                          # Debug Manifest (Cleartext Traffic enabled)
│       ├── main/
│       │   ├── AndroidManifest.xml         # Internet Permission & Network Security Config
│       │   ├── java/com/heathealth/app/
│       │   │   ├── MainActivity.kt         # App Entry Activity (Launches MainScreen)
│       │   │   ├── data/
│       │   │   │   ├── local/              # Room Database, Entities (StudentProfile, WaterIntake), DAOs, DataStore
│       │   │   │   ├── model/              # ProfileUiState, WaterTrackerUiState, DashboardUiState
│       │   │   │   └── repository/         # StudentProfileRepository, WaterIntakeRepository, HeatHealthRepository
│       │   │   ├── network/                # BackendApiConfig (Multi-Endpoint Failover), HeatHealthApiService, DTOs
│       │   │   ├── ui/
│       │   │   │   ├── components/         # Header, WeatherSummary, HourlyForecast, CustomMlPredictor, RiskIndicator, Safety
│       │   │   │   └── screens/            # MainScreen, DashboardScreen, WaterTrackerScreen, ProfileScreen, RiskMapScreen, AlertsScreen, SafetyScreen
│       │   │   └── viewmodel/              # DashboardViewModel, StudentProfileViewModel, WaterTrackerViewModel
│       │   └── res/
│       └── test/                            # Comprehensive Unit Test Suite (21 Tests)
├── backend/                                # Python FastAPI Backend Module
├── build.gradle.kts                        # Root Gradle configuration
├── README.md                               # Combined project documentation
├── README_PHASE1.md                        # Dedicated Phase 1 documentation
├── README_PHASE2.md                        # Dedicated Phase 2 documentation
├── README_PHASE3.md                        # Dedicated Phase 3 documentation
├── README_PHASE4.md                        # Dedicated Phase 4 documentation
└── .gitignore                              # Git exclusion rules
```

---

## 🧪 Automated Test Summary

- **Phase 1 Unit Tests**: 100% Passed
- **Phase 2 Backend Audit & Integration Tests**: 100% Passed
- **Phase 3 ML Inference Tests**: 100% Passed
- **Phase 4 Android Unit Tests**: **21/21 PASSED (100% Success)**
  - Profile preferences saving, restoration, validation, and reset.
  - Water tracking quick-add (+150 mL, +250 mL, +500 mL), custom volume validation, deletion, and undo.
  - Date boundary filtering and historical record preservation.
- **Android Gradle Assembly (`assembleDebug`)**: **BUILD SUCCESSFUL**
