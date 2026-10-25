# Phase 7 & 7.1 — Collaborative Campus Cool Map & Multi-College Discovery System

Phase 7 & 7.1 implements the **Collaborative Campus Cool Map**, **Multi-College Directory & Search**, **Real-Time Cross-Client Sync**, **Community Facility Contributions (+ Add Facility)**, **Operational Status Reporting**, **Room Schema Migration v4**, **Full-Screen Geographic Map Mode**, and **Critical Data Audit & Verification Safeguards** in the HeatHealth Risk Prediction & Early Warning System.

---

## 📌 Phase 7 & 7.1 Key Features Implemented

### 1. Data Accuracy, Audit & Verification Fixes (`Phase 7.1`)
- **Strict Data State Separation**: Separates `VERIFIED` (officially verified by campus administration with source URL) from `COMMUNITY_REPORTED` (submitted by students / campus members) and `DEMO` records.
- **Honest Empty States**: When an empty facility list is returned for a college, the app displays an honest message (*"Campus facility data is not available yet for [College Name]. You can help add verified locations."*), eliminating fake or randomly generated markers.
- **Production Fallback Fixes**: Removed repository catch-all blocks in `CampusCoolMapRepository.kt` that previously swallowed backend network errors and returned fake demo colleges. Server errors now properly return `Result.failure()` or expose offline cached data explicitly labeled `DataSource.CACHED`.
- **Repeatable Data Import Script (`import_colleges.py`)**: Idempotent Python script that ingests verified Bengaluru educational directory records, validates coordinates, detects duplicates, and records provenance.

### 2. Multi-College Search & Discovery
- **Prominent College Search Experience**: Dedicated college search dialog allowing students to search by college name, city (*Bengaluru*, *New Delhi*, *Mumbai*, *Chennai*, *Kolkata*), or address.
- **Data Persistence**: Followed college selection persists locally in DataStore (`selectedCollegeId`) and Room DB v4 (`colleges` table) across app restarts.
- **Campus Isolation**: Selecting a college centers the geographic map on that campus and filters facility directories specifically for that institution.

### 3. Campus-Specific Facility Directory
- **4 Essential Categories**: *Drinking Water*, *Shaded Areas*, *Cool Indoor Study Spaces*, *First Aid & Emergency Care*.
- **Geographic OpenStreetMap Canvas (`InteractiveCampusMapView.kt`)**: Native multi-touch pan/zoom using `osmdroid-android:6.1.18` with category-colored location pins.
- **Full-Screen Map Mode**: Expands map across entire screen area, hides bottom navigation bar, and handles system back press (`BackHandler`).

### 4. Community Contributions & Facility Submission
- **"+ Add Facility" Action**: Floating Action Button (FAB) and dialog allowing students to contribute new campus heat-relief points.
- **Field Validation**: Name, category, description, and building location fields validated before saving.
- **Status & Badges**: Community-contributed facilities are immediately labeled as `COMMUNITY REPORTED / PENDING VERIFICATION`.

### 5. Problem Reporting & Operational Status Updates
- **Status Reporting Action**: "Report Issue / Update Status" button on facility cards allowing students to report broken water dispensers, closed AC rooms, or maintenance issues.
- **Status States**: `OPERATIONAL` (Green), `MAINTENANCE / BROKEN` (Amber), `CLOSED` (Red).

### 6. Real-Time Synchronization Between Clients
- **FastAPI Endpoints**:
  - `GET /api/v1/campus-cool-map/colleges`
  - `GET /api/v1/campus-cool-map/facilities`
  - `POST /api/v1/campus-cool-map/facilities`
  - `PUT /api/v1/campus-cool-map/facilities/{id}/status`
- **Real-Time Sync Engine**: Periodic synchronization loop in `CampusCoolMapViewModel` ensures changes made on Device A (new facility or status update) automatically update Device B in real time without manual refreshes!

### 7. Room Database Schema Migration v4
- Registered `CollegeEntity` (`colleges` table) and upgraded `CampusFacilityEntity` (`campus_facilities` table) with `collegeId`, `status`, `createdBy`, `updatedBy`, and `updatedTimestamp`.
- Implemented `MIGRATION_3_4` preserving all existing profile, water tracking, and alert logs.

---

## 🧪 Phase 7 & 7.1 Automated Test Suite Results

### 1. Android Unit Tests (`:app:testDebugUnitTest`)
- **Total Tests Executed**: 48
- **Passed**: 48
- **Failed**: 0
- **Result**: **100% Success Rate**

```text
Test Suite Breakdown (48 Total Tests):
- com.heathealth.app.CampusCoolMapTest (7 tests) PASSED
  ├─ testCollegeSearchAndFiltering PASSED
  ├─ testFacilityIsolationByCollegeId PASSED
  ├─ testFacilityCategoryMapping PASSED
  ├─ testSearchByNameAndLocation PASSED
  ├─ testEntityConversionAndDuplicatePrevention PASSED
  └─ testOfflineFallbackToCachedFacilities PASSED
- com.heathealth.app.BackendOfflineVerificationTest (2 tests) PASSED
- com.heathealth.app.NetworkReliabilityAndErrorHandlingTest (4 tests) PASSED
- com.heathealth.app.AlertDeduplicationAndThresholdTest (4 tests) PASSED
- com.heathealth.app.ViewModelDebounceTest (1 test) PASSED
- com.heathealth.app.PersonalizedGuidanceTest (3 tests) PASSED
- com.heathealth.app.OutdoorActivityPlannerTest (3 tests) PASSED
- com.heathealth.app.UserPreferencesAndAlertsTest (4 tests) PASSED
- com.heathealth.app.HeatHealthUnitTest (6 tests) PASSED
- com.heathealth.app.StudentProfileViewModelTest (8 tests) PASSED
- com.heathealth.app.WaterTrackerViewModelTest (5 tests) PASSED
- com.heathealth.app.WaterIntakeRepositoryTest (2 tests) PASSED
```

### 2. Python FastAPI Backend Tests (`test_backend.py` & `run_audit_tests.py`)
- **`test_backend.py`**: **7/7 PASSED**
- **`run_audit_tests.py`**: **9/9 PASSED**

### 3. Gradle Assembly
- `./gradlew :app:assembleDebug`: **BUILD SUCCESSFUL**
- `./gradlew :app:assembleRelease`: **BUILD SUCCESSFUL**

---

## 📲 Exact Testing Instructions for Two Emulators / Phones

### Step 1: Start FastAPI Backend
```powershell
cd backend
.venv\Scripts\activate
uvicorn main:app --host 0.0.0.0 --port 8000
```

### Step 2: Launch App on Device A and Device B
1. Launch app on **Device A** (e.g. Phone 1 or Emulator 1) and **Device B** (e.g. Phone 2 or Emulator 2).
2. Open **Cool Map** tab on both devices. Both devices connect to the FastAPI backend and display IISc Bengaluru as the active college.

### Step 3: Test Real-Time Addition of Facilities Across Devices
1. On **Device A**, tap **"+ Add Facility"** FAB button.
2. Enter Name: *"New RO Water Dispenser - Library Annex"*, Category: *"Drinking Water"*, Building: *"Library North Wing"*. Tap **Submit Facility**.
3. Observe **Device B**: Within ~5 seconds, the real-time sync loop receives the update, rendering the new water point marker on Device B's map and directory automatically!

### Step 4: Test Real-Time Status Reporting Across Devices
1. On **Device A**, select *"IISc Central Library Chilled Water Station"*, tap **"Report Issue"**, select **"Maintenance / Broken"**, and submit.
2. Observe **Device B**: Device B's map marker and details panel automatically update status to **MAINTENANCE / BROKEN** (Amber badge) in real time!

### Step 5: Test College Switching & Campus Isolation
1. On **Device B**, tap **"Switch College"**. Type *"Delhi"* and select **"Delhi University (DU)"**.
2. Confirm Device B's map camera moves to DU New Delhi campus coordinates (`28.6139, 77.2090`), displaying DU's facilities while Device A continues showing IISc Bengaluru's facilities.
