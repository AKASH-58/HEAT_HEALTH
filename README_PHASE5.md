# Phase 5 — Student Safety Features & Personalized Heat-Health Guidance

Phase 5 implements **Student Safety Features & Personalized Heat-Health Guidance** for the HeatHealth Risk Prediction & Early Warning System.

---

## 📌 Phase 5 Key Features Implemented

### 1. Feature 1: Personalized Heat-Risk Guidance
- **Maintainable Guidance Engine (`PersonalizedGuidanceEngine.kt`)**:
  - Maps live thermal risk levels (`LOW`, `MODERATE`, `HIGH`, `VERY_HIGH`, `EXTREME`) to student-focused activity contexts:
    - *Walking to College / Campus Commute*
    - *Outdoor Classes & Fieldwork*
    - *Sports & Physical Activities*
    - *Indoor Study & Classroom Exposure*
  - Provides environmental risk explanations using ambient temperature and humidity metrics.
  - Displays activity recommendations dynamically on the **Dashboard** and **Safety** screens.
  - Features explicit heat emergency and first-aid instructions: shaded resting, body cooling, calling 108/112, and **CRITICAL safety rule**: *Do NOT give fluids to anyone with altered consciousness or who cannot swallow safely.*

### 2. Feature 2: Hydration & Heat-Safety Assistant
- **WorkManager Scheduling (`HydrationReminderWorker.kt`, `WorkManagerScheduler.kt`)**:
  - Schedules periodic background hydration reminders based on user-configured intervals (1h, 2h, 3h, 4h).
  - Uses `WorkManager` with `ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE` to avoid duplicate job scheduling.
  - Respects user toggles in `UserPreferencesRepository` (`hydration_reminders_enabled`).
- **Android Notifications & Permission Handling (`NotificationHelper.kt`)**:
  - Registers notification channels (`hydration_reminders_channel` and `heat_alerts_channel`).
  - Checks `POST_NOTIFICATIONS` runtime permission on Android 13+ (API 33+).
  - Includes a **"Mark as Hydrated (+250mL)"** notification action handled via `HydrationActionReceiver.kt`, logging water directly into Room database.

### 3. Feature 3: Student Outdoor Activity Planner
- **Interactive Outdoor Activity Planner Card (`OutdoorActivityPlannerCard.kt`)**:
  - Allows students to select an activity (*Walking to Class*, *Outdoor Sports*, *Campus Commute*, *Fieldwork*) and a planned time.
  - Looks up temperature, humidity, feels-like temperature, and predicted heat risk level from the live 24-hour forecast array (`hourlyForecast`).
  - Delivers actionable advice: proceed normally, take shade breaks, or avoid strenuous exertion during peak heat windows.
  - Suggests **Alternative Cooler Times** filtered from Low/Moderate risk forecast hours.
  - Handles missing/out-of-range dates gracefully: displays *"Future risk estimate is unavailable for the selected date/time beyond the 24-hour forecast window."*

### 4. Feature 4: Heat-Alert Notifications
- **Background Heat Risk Check (`HeatAlertCheckWorker.kt`)**:
  - Periodically inspects live heat risk data using `HeatHealthRepository`.
  - Compares risk levels against user-configurable minimum alert threshold (*Moderate*, *High*, *Extreme*).
  - Prevents duplicate alerts by tracking `last_alerted_risk_level` in DataStore.
  - Automatically records triggered alert events into Room database table `alert_logs`.

### 5. Feature 5: Personal Safety Dashboard
- **Personal Safety Dashboard Card (`PersonalSafetyDashboardCard.kt`)**:
  - Displays **Current Heat Risk State** and thermal score.
  - Shows **Today's Hydration Progress** bar against target (mL).
  - Displays genuine **Weekly Hydration Summary** (total Liters and daily average) calculated directly from recorded Room database entries (`WaterIntakeEntity`).
  - Lists **Recent Recorded Risk Assessments** from Room table `heat_risk_logs`.
  - Lists **Recent Triggered Heat Alerts** from Room table `alert_logs`.
  - Empty states inform the user when historical records are absent, without generating fake data.

---

## 🏗️ Architecture & Component Overview

```text
com.heathealth.app/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── AlertLogDao.kt                 # Room DAO for triggered alert logs
│   │   │   ├── HeatRiskLogDao.kt              # Room DAO for recorded heat risk predictions
│   │   │   ├── StudentProfileDao.kt           # Room DAO for student profile
│   │   │   └── WaterIntakeDao.kt              # Room DAO for water intake entries
│   │   ├── datastore/
│   │   │   └── UserPreferencesRepository.kt   # Preferences DataStore for alerts & reminders
│   │   ├── entity/
│   │   │   ├── AlertLogEntity.kt              # Room entity table for heat alerts
│   │   │   └── HeatRiskLogEntity.kt           # Room entity table for risk predictions
│   │   └── HeatHealthDatabase.kt              # Room database singleton (Version 2)
│   ├── model/
│   │   ├── PersonalizedGuidance.kt            # Guidance data structures & PersonalizedGuidanceEngine
│   │   ├── DashboardUiState.kt                # UI state model for Dashboard
│   │   └── RiskPrediction.kt                  # Risk level enums and prediction models
│   └── repository/
│       └── HeatHealthRepository.kt            # Repository fetching live risk & logging to Room
├── ui/
│   ├── components/
│   │   ├── OutdoorActivityPlannerCard.kt      # Interactive student activity planner component
│   │   └── PersonalSafetyDashboardCard.kt     # Personal safety dashboard card with real history
│   └── screens/
│       ├── DashboardScreen.kt                 # Integrated Home dashboard UI
│       ├── AlertsScreen.kt                    # Early warning alerts & preferences UI
│       ├── SafetyScreen.kt                    # Personalized safety guidance & emergency guide UI
│       └── ProfileScreen.kt                   # Profile preferences & reminder interval controls UI
├── util/
│   └── NotificationHelper.kt                  # Notification channel setup & notification triggers
├── worker/
│   ├── HeatAlertCheckWorker.kt                # WorkManager worker for periodic heat alert checks
│   ├── HydrationActionReceiver.kt             # BroadcastReceiver for "Mark as Hydrated" action
│   ├── HydrationReminderWorker.kt             # WorkManager worker for periodic hydration reminders
│   └── WorkManagerScheduler.kt                # Helper scheduling & canceling WorkManager jobs
└── viewmodel/
    ├── DashboardViewModel.kt                  # ViewModel managing dashboard state, logs, & guidance
    └── StudentProfileViewModel.kt             # ViewModel managing profile & reminder scheduling
```

---

## 🧪 Phase 5 Test Suite Results

### Android Unit Tests (`:app:testDebugUnitTest`)
All **31 unit tests** passed with a **100% success rate**:
- `PersonalizedGuidanceTest`: Low/High/Extreme risk guidance, student activity mapping, emergency protocol validation.
- `OutdoorActivityPlannerTest`: Forecast hour matching, alternative cooler times filtering, unavailable forecast handling.
- `UserPreferencesAndAlertsTest`: Default preferences, alert duplicate suppression, WorkManager job identifiers.
- `HeatHealthUnitTest`: Sample weather values, risk mapping, recommendations, offline sample load.
- `StudentProfileViewModelTest` & `WaterTrackerViewModelTest` & `WaterIntakeRepositoryTest`: Profile validation, water logging, undo deletion, date boundary filtering.

### Backend API Verification (`run_audit_tests.py`)
All **9 backend test suites** passed with a **100% success rate**:
- Root endpoint `/` and health check `/health`.
- Valid current weather and heat risk endpoints (`/api/v1/weather`, `/api/v1/heat-risk`).
- Custom ML prediction endpoint (`/predict`).
- Latitude and longitude coordinate boundary validation (`422 Unprocessable Entity` on invalid coords).
- OpenAPI spec `/openapi.json` and Swagger docs `/docs`.

---

## 📋 Manual Testing Checklist

| Feature | Scenario | Steps to Verify | Expected Result |
| :--- | :--- | :--- | :--- |
| **Personalized Guidance** | Viewing Guidance | Open **Safety** or **Home** tab | Displays risk-level overview and activity-tailored recommendations (walking, classes, sports). |
| **Emergency Instructions** | Heatstroke First Aid | Scroll to First Aid on **Safety** tab | Shows explicit heatstroke warning: cool person, call 108/112, DO NOT give fluids if impaired. |
| **Hydration Reminders** | WorkManager Schedule | Toggle Reminders ON in **Profile** | Schedules WorkManager task `hydration_reminder_work` with chosen frequency. |
| **Mark as Hydrated** | Notification Action | Tap "Mark as Hydrated (+250mL)" | Increases today's water intake by +250 mL in Room database and shows Toast feedback. |
| **Activity Planner** | Time Selection | Select "Outdoor Sports" at 14:00 on Home tab | Shows predicted thermal risk and suggests alternative cooler hours (e.g. 18:00). |
| **Activity Planner** | Out-of-Range Date | Select time beyond 24-hour forecast | Displays *"Future risk estimate is unavailable for selected time beyond forecast window."* |
| **Heat Alerts** | Alert Threshold | Set Alert Threshold to "High" in **Alerts** tab | WorkManager checks risk; fires notification if risk >= High and risk state has changed. |
| **Duplicate Alert Guard**| Unchanged State | Re-trigger alert check when risk is still High | Prevents duplicate notification; updates DataStore state. |
| **Safety Dashboard** | Genuine History | View Safety Dashboard on **Home** tab | Shows recent risk logs, recent alert logs, today's water intake, and weekly summary from Room. |
