# HeatHealth — Heat-Health Risk Prediction & Early Warning System

> **Comprehensive Project Documentation — Phase 1, Phase 2, Phase 3, Phase 4, Phase 5, Phase 6, & Phase 7**

HeatHealth is an open-source health-technology application designed to help students, outdoor workers, athletes, and vulnerable individuals monitor heat-health risks and receive preventive early warning recommendations during extreme thermal conditions.

---

## 📚 Dedicated Phase Documentation Links
- 📘 [Phase 1 Detailed Documentation](README_PHASE1.md) — Foundation Dashboard & Local FastAPI Backend Setup.
- 📙 [Phase 2 Detailed Documentation](README_PHASE2.md) — Live Open-Meteo Integration, Heat-Risk Engine, Bottom Navigation & Hourly Forecasts.
- 📕 [Phase 3 Detailed Documentation](README_PHASE3.md) — ML Integration, `POST /predict` Endpoint, & Custom Risk Simulator.
- 📗 [Phase 4 Detailed Documentation](README_PHASE4.md) — Student Profiles, Water Intake Tracking, Room Database, & DataStore Persistence.
- 📓 [Phase 5 Detailed Documentation](README_PHASE5.md) — Student Safety Features, Personalized Guidance, WorkManager Reminders, Activity Planner, & Personal Safety Dashboard.
- 📒 [Phase 6 Detailed Documentation](README_PHASE6.md) — Smart Heat Alerts, Network Reliability, Comprehensive Unit Tests, & Final APK Builds.
- 📓 [Phase 7 Detailed Documentation](README_PHASE7.md) — Campus Cool Map Implementation, Facility Directory, Room Migration v3, & Offline Data Handling Fix.

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

### **Phase 5: Student Safety Features & Personalized Guidance (Completed)**
- **Feature 1 — Personalized Heat-Risk Guidance**: Centralized guidance engine (`PersonalizedGuidanceEngine`) mapping thermal risk levels to student activity contexts (*Walking to College*, *Outdoor Classes*, *Sports*, *Indoor Study*), with explicit heatstroke emergency rules (shaded resting, body cooling, call 108/112, **CRITICAL safety rule**: *Do NOT give fluids to anyone with altered consciousness or who cannot swallow safely*).
- **Feature 2 — Hydration & Heat-Safety Assistant**: Periodic background scheduling via WorkManager (`HydrationReminderWorker`), Android notification channel management, `POST_NOTIFICATIONS` runtime permission handling, and **"Mark as Hydrated (+250mL)"** notification action via `HydrationActionReceiver`.
- **Feature 3 — Student Outdoor Activity Planner**: Interactive planner card (`OutdoorActivityPlannerCard`) inspecting live 24-hour hourly forecasts, matching selected activity times, providing risk recommendations, and suggesting alternative cooler forecast hours.
- **Feature 4 — Heat-Alert Notifications**: Periodic background risk monitoring (`HeatAlertCheckWorker`) with user threshold selection (*Moderate*, *High*, *Extreme*), duplicate alert suppression for unchanged states via DataStore tracking, and Room database alert logging.
- **Feature 5 — Personal Safety Dashboard**: Comprehensive dashboard card (`PersonalSafetyDashboardCard`) showing current risk state, today's water progress, genuine weekly summary (liters and daily average), recent risk logs (`heat_risk_logs`), and recent alert logs (`alert_logs`).

### **Phase 6: Smart Heat Alerts, Reliability, Testing & Final Integration (Completed)**
- **API Integration & Network Reliability**: Differentiated exception handling for HTTP 400/422 validation errors, HTTP 500/502 gateway errors, malformed JSON structure errors, and network timeouts. Inflight request debouncing prevents duplicate parallel requests.
- **Smart Alert Deduplication**: Evaluates live thermal risk against user threshold (*Moderate*, *High*, *Extreme*), tracks last alerted risk state in DataStore, suppresses duplicate notifications for unchanged states, and logs alert events to Room.
- **Backend-Offline Data-Source Fix**: Introduced explicit `DataSource` enum (`LIVE`, `CACHED`, `DEMO`, `UNAVAILABLE`). Displays a prominent `StaleDataWarningCard` banner and `CACHED DATA (OFFLINE)` badge when offline without falsely claiming new live data or altering original timestamps.

### **Phase 7: Collaborative Campus Cool Map & Multi-College Discovery (Completed)**
- **Multi-College Directory & Search**: Allows students to search and follow colleges across Bengaluru, New Delhi, Mumbai, Chennai, Kolkata, and custom cities, with selection persisted in DataStore & Room DB v4.
- **Geographic OpenStreetMap & Full-Screen Mode**: Renders OpenStreetMap tiles (`osmdroid-android:6.1.18`) with pinch zoom, drag panning, full-screen expansion mode, and bottom navigation hiding.
- **Community Contributions & Status Reporting**: FAB button allowing students to submit new water points, shaded areas, cool rooms, and first aid stations, plus a "Report Issue" status reporter for broken dispensers or closed rooms.
- **Real-Time Cross-Client Sync**: Centralized FastAPI backend endpoints (`/api/v1/campus-cool-map/colleges`, `/facilities`) and periodic polling sync engine ensuring updates submitted on Device A appear automatically on Device B in real time.

> [!IMPORTANT]
> **Environmental Estimate Notice**: HeatHealth evaluates environmental thermal stress risk using meteorological data. It is an early-stage research prototype and does not provide an individualized medical diagnosis or official public weather warning.

---

## 🛠️ Complete Technology Stack

### Android Frontend
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Navigation**: Jetpack Navigation Compose (`androidx.navigation:navigation-compose:2.8.8`)
- **Background Tasks**: Android WorkManager (`androidx.work:work-runtime-ktx:2.9.1`)
- **Local Database**: Android Room 2.7 (`androidx.room:room-runtime`, `androidx.room:room-ktx`, `androidx.room:room-compiler` via KSP) - Version 3
- **Preferences**: Android DataStore (`androidx.datastore:datastore-preferences:1.1.2`)
- **Networking**: Retrofit 2 + Gson Converter + OkHttp 3 (with multi-endpoint candidate failover)
- **Architecture**: MVVM (`DashboardViewModel`, `StudentProfileViewModel`, `WaterTrackerViewModel`, `CampusCoolMapViewModel`, Repositories, StateFlow)
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
│       │   ├── AndroidManifest.xml         # Internet Permission, POST_NOTIFICATIONS & Network Security Config
│       │   ├── java/com/heathealth/app/
│       │   │   ├── MainActivity.kt         # App Entry Activity (Launches MainScreen)
│       │   │   ├── data/
│       │   │   │   ├── local/              # Room DB v3, Entities (StudentProfile, WaterIntake, HeatRiskLog, AlertLog, CampusFacility), DAOs, DataStore
│       │   │   │   ├── model/              # CampusFacility, PersonalizedGuidance, ProfileUiState, WaterTrackerUiState, DashboardUiState
│       │   │   │   └── repository/         # CampusCoolMapRepository, StudentProfileRepository, WaterIntakeRepository, HeatHealthRepository
│       │   │   ├── network/                # BackendApiConfig (Multi-Endpoint Failover), HeatHealthApiService, DTOs
│       │   │   ├── ui/
│       │   │   │   ├── components/         # InteractiveCampusMapView, CampusFacilityDetails, CampusCoolMapShortcut, OutdoorActivityPlanner, PersonalSafetyDashboard, WeatherSummary
│       │   │   │   └── screens/            # CampusCoolMapScreen, MainScreen, DashboardScreen, WaterTrackerScreen, ProfileScreen, RiskMapScreen, AlertsScreen, SafetyScreen
│       │   │   ├── util/                   # NotificationHelper
│       │   │   ├── worker/                 # HydrationReminderWorker, HeatAlertCheckWorker, HydrationActionReceiver, WorkManagerScheduler
│       │   │   └── viewmodel/              # CampusCoolMapViewModel, DashboardViewModel, StudentProfileViewModel, WaterTrackerViewModel
│       │   └── res/
│       └── test/                            # Comprehensive Unit Test Suite (49 Tests)
├── backend/                                # Python FastAPI Backend Module
├── build.gradle.kts                        # Root Gradle configuration
├── README.md                               # Combined project documentation
├── README_PHASE1.md                        # Dedicated Phase 1 documentation
├── README_PHASE2.md                        # Dedicated Phase 2 documentation
├── README_PHASE3.md                        # Dedicated Phase 3 documentation
├── README_PHASE4.md                        # Dedicated Phase 4 documentation
├── README_PHASE5.md                        # Dedicated Phase 5 documentation
├── README_PHASE6.md                        # Dedicated Phase 6 documentation
├── README_PHASE7.md                        # Dedicated Phase 7 documentation
└── .gitignore                              # Git exclusion rules
```

---

## 🧪 Automated Test Summary

- **Phase 1 Unit Tests**: 100% Passed
- **Phase 2 Backend Audit & Integration Tests**: 100% Passed
- **Phase 3 ML Inference Tests**: 100% Passed
- **Phase 4 Android Unit Tests**: 100% Passed
- **Phase 5 Android Unit Tests**: 100% Passed
- **Phase 6 Android Unit Tests**: 100% Passed
- **Phase 7 Android Unit Tests**: **49/49 PASSED (100% Success)**
  - `CampusCoolMapTest`: Category filtering, facility search, combined search+filter, missing coordinates handling, Room entity conversion, duplicate prevention, and offline fallback.
  - `BackendOfflineVerificationTest`: 9-step backend offline and recovery sequence.
  - `NetworkReliabilityAndErrorHandlingTest`, `AlertDeduplicationAndThresholdTest`, `ViewModelDebounceTest`, `PersonalizedGuidanceTest`, `OutdoorActivityPlannerTest`, `UserPreferencesAndAlertsTest`, `HeatHealthUnitTest`, `StudentProfileViewModelTest`, `WaterTrackerViewModelTest`, `WaterIntakeRepositoryTest`.
- **Backend Test Suite (`test_backend.py` & `run_audit_tests.py`)**: **16/16 PASSED (100% Success)**
- **Android Gradle Assembly (`assembleDebug` & `assembleRelease`)**: **BUILD SUCCESSFUL**
