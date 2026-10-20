# Phase 4 — Personalized Student Profiles, Water Intake Tracking, & Room Persistence

> **Dedicated Documentation for Phase 4 Implementation**

Phase 4 introduces personalized student profiles, daily water intake tracking, and structured local data persistence using Android Room Database and Preferences DataStore.

---

## 📌 Phase 4 Key Features

### 1. Feature A: Personalized Student Profile & Preferences
- **Editable Student Attributes**:
  - Optional student nickname.
  - Daily activity pattern selector (*"Mostly indoors"*, *"Regular commuting"*, *"Frequent outdoor activities"*).
  - Preferred language selector (*"English"*, *"Hindi"*).
  - Daily hydration reference target input (mL) with validation enforcing limits between 500 mL and 5000 mL.
  - Hydration reminder preferences (switch toggle and interval selection: 1h, 2h, 3h, 4h).
- **Local Persistence & Recovery**: Saved locally in Room database (`student_profile` table) and DataStore (`user_preferences`), restored automatically across app restarts.
- **Reset to Defaults**: One-tap action to restore initial baseline preferences.
- **Non-Medical Disclaimer**: Educational notice emphasizing that hydration targets represent general reference guidelines.

### 2. Feature B: Daily Water Intake Tracker
- **Progress Monitoring**:
  - Live progress display calculating current day's total water intake against the user's reference target.
  - Visual linear progress indicator bar and progress percentage metrics.
- **Quick-Add & Custom Controls**:
  - Quick-add buttons for **+150 mL** (small glass), **+250 mL** (standard cup), and **+500 mL** (water bottle).
  - Custom intake volume input field with validation (rejects zero, negative, non-numeric, or amounts > 3000 mL per entry).
- **Intake Log & History**:
  - Individual entries saved as structured records with unique primary keys, timestamps, mL amounts, and notes.
  - Ordered today's history log with localized time formatting (e.g., `10:30 AM`).
  - Individual entry deletion with **Undo** action via Snackbar.
  - Empty state UI when no water intake has been recorded for the day.
- **Date Boundary Filtering**:
  - Dynamic local date range calculation using `ZoneId.systemDefault()`.
  - Today's total is computed dynamically from saved entries occurring between `00:00:00.000` and `23:59:59.999` of the local date.
  - Historical records from previous days remain preserved in local storage.

### 3. Feature C: Room Database Architecture
- **`HeatHealthDatabase`**: Thread-safe Room database singleton (`heat_health_database`).
- **`StudentProfileEntity`**: Room entity table storing profile preferences.
- **`WaterIntakeEntity`**: Room entity table storing individual water intake logs.
- **`StudentProfileDao` & `WaterIntakeDao`**: Reactive Kotlin `Flow` queries and `suspend` write operations.

### 4. Feature D: Preferences DataStore
- **`UserPreferencesRepository`**: Preferences DataStore (`user_preferences`) storing `hydration_reminders_enabled`, `reminder_frequency_hours`, and `notifications_enabled`.

---

## 🏗️ Architecture & Component Overview

```text
com.heathealth.app/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── StudentProfileDao.kt         # Room DAO for student profile table
│   │   │   └── WaterIntakeDao.kt            # Room DAO for water intake logs and daily totals
│   │   ├── datastore/
│   │   │   └── UserPreferencesRepository.kt # Preferences DataStore for reminder settings
│   │   ├── entity/
│   │   │   ├── StudentProfileEntity.kt      # Room table entity for profile settings
│   │   │   └── WaterIntakeEntity.kt         # Room table entity for water intake logs
│   │   └── HeatHealthDatabase.kt            # Room database singleton class
│   ├── model/
│   │   ├── ProfileUiState.kt                # UI state model for Profile screen
│   │   └── WaterTrackerUiState.kt           # UI state model for Water Tracker screen
│   └── repository/
│       ├── StudentProfileRepository.kt      # Repository for profile & DataStore integration
│       └── WaterIntakeRepository.kt         # Repository for water log queries and date calculations
├── ui/
│   └── screens/
│       ├── ProfileScreen.kt                 # Jetpack Compose Student Profile UI
│       ├── WaterTrackerScreen.kt            # Jetpack Compose Water Tracker UI
│       └── MainScreen.kt                    # Navigation bar with 6 top-level routes
└── viewmodel/
    ├── StudentProfileViewModel.kt           # ViewModel managing profile state & validation
    └── WaterTrackerViewModel.kt             # ViewModel managing water tracking, quick add, & undo
```

---

## 🧪 Phase 4 Test Suite Results

All unit tests and database verification suites passed with **100% success rate**:

```text
:app:testDebugUnitTest
- testInitialDefaultProfileState PASSED
- testValidTargetValidation PASSED
- testInvalidTargetValidationUnder500 PASSED
- testInvalidTargetValidationOver5000 PASSED
- testSaveProfilePersistsChanges PASSED
- testResetToDefaults PASSED
- testQuickAddWater PASSED
- testAddCustomWaterValid PASSED
- testAddCustomWaterInvalidZeroOrNegative PASSED
- testAddCustomWaterExceedsSingleLimit PASSED
- testDeleteEntryAndUndo PASSED
- testProgressCalculation PASSED
- testDateBoundaryFiltering PASSED
- testRejectZeroOrNegativeAmount PASSED
- testDeleteWaterIntake PASSED
- testDashboardSampleWeatherValues PASSED
- testDashboardSampleRiskPrediction PASSED
- testRiskLevelMapping PASSED
- testSafetyRecommendations PASSED
- testPrototypeDisclaimer PASSED
- testDashboardViewModelOfflineSampleLoad PASSED

Total: 21 PASSED, 0 FAILED, 0 SKIPPED (100% Success)
```
