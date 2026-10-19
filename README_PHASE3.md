# HeatHealth — Phase 3: ML Integration & Heat Risk Prediction

> **Phase 3 Technical Documentation**

HeatHealth Phase 3 implements an end-to-end Machine Learning pipeline and custom prediction engine integration, allowing users to simulate custom environmental weather conditions and receive real-time heat-risk predictions, contributing factors, and safety recommendations from a trained scikit-learn `RandomForestClassifier` model.

---

## 📌 Phase 3 Scope & Objectives

Phase 3 delivers a complete ML inference pipeline:
- **scikit-learn ML Pipeline**: Trainable `RandomForestClassifier` model trained on NWS Heat Index and WHO thermal strain reference parameters across full environmental ranges (15°C to 50°C temp, 10% to 100% humidity, 0 to 50 km/h wind).
- **FastAPI `POST /predict` Endpoint**: Pydantic validated request (`PredictRequest`) and response (`PredictResponse`) schemas enabling on-demand heat-risk inference for custom input parameters.
- **Android Custom ML Risk Simulator Card**: Interactive Compose UI card on the Home dashboard allowing users to adjust temperature, relative humidity, and wind speed via sliders, and trigger `POST /predict` with non-blocking coroutines.
- **Model Disclaimers & Transparency**: Explicit prototype disclaimers (`is_demo_model: true`) clarifying that synthetic training metrics demonstrate formula approximation capabilities and do not constitute a clinical medical diagnosis.

---

## 🛠️ Phase 3 Technology Stack

### Android Frontend
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Networking**: Retrofit 2 + Gson Converter + OkHttp 3 (`POST /predict`)
- **Architecture**: MVVM (`DashboardViewModel`, `HeatHealthRepository`, `DashboardUiState`)
- **State Management**: Kotlin Coroutines + `StateFlow`

### Python Backend
- **Language**: Python 3.13+
- **Framework**: FastAPI + Pydantic v2
- **Machine Learning**: pandas, NumPy, scikit-learn (`RandomForestClassifier`), joblib
- **Model Artifacts**: `heat_risk_model.joblib` and `model_metadata.json`

---

## 📂 Phase 3 Project Structure

```text
HeatHealth/
├── app/                                    # Android Application Module
│   └── src/main/java/com/heathealth/app/
│       ├── data/
│       │   ├── model/                      # DashboardUiState, CustomPredictionState, RiskPrediction
│       │   └── repository/                 # HeatHealthRepository (predictCustomHeatRisk)
│       ├── network/
│       │   ├── HeatHealthApiService.kt     # Retrofit @POST("predict") Interface
│       │   └── dto/                        # PredictRequestDto, PredictResponseDto
│       ├── ui/
│       │   ├── components/                 # CustomMlPredictorCard, WeatherSummaryCard, RiskIndicatorCard
│       │   └── screens/                    # DashboardScreen (Home)
│       └── viewmodel/                      # DashboardViewModel (updateCustomInputs, runCustomMlPrediction)
├── backend/                                # Python FastAPI Backend Module
│   ├── app/
│   │   ├── schemas.py                      # PredictRequest and PredictResponse Pydantic schemas
│   │   ├── ml_pipeline.py                  # Scikit-learn RandomForest training pipeline & predict_custom()
│   │   └── models/                         # Serialized ML model & metadata
│   │       ├── heat_risk_model.joblib
│   │       └── model_metadata.json
│   ├── main.py                             # POST /predict route definition
│   ├── run_audit_tests.py                  # Audit test suite including test_audit_H_post_predict_endpoint
│   └── test_backend.py                     # Integration test suite
├── README_PHASE1.md                        # Phase 1 documentation
├── README_PHASE2.md                        # Phase 2 documentation
├── README_PHASE3.md                        # Phase 3 documentation
└── README.md                               # Main cumulative documentation
```

---

## ⚙️ Phase 3 API Schema (`POST /predict`)

### Example Request
```json
POST /predict
Content-Type: application/json

{
  "temperature_c": 38.0,
  "humidity_percent": 65.0,
  "wind_speed_kmh": 8.0
}
```

### Example Response
```json
{
  "risk_level": "Extreme",
  "risk_score": 48.5,
  "prediction_message": "RandomForest ML model predicted Extreme heat risk for 38.0°C and 65.0% humidity (Apparent: 48.5°C). Estimated model confidence: 98.5%.",
  "factors": [
    "Elevated air temperature (38.0°C)",
    "High relative humidity (65%) restricts body sweat evaporation",
    "Low wind speed (8.0 km/h) limits natural convective body cooling"
  ],
  "safety_recommendations": [
    {
      "id": "1",
      "title": "Maintain regular hydration",
      "description": "Drink clean water frequently throughout the day, even before feeling thirsty.",
      "category": "Hydration"
    },
    {
      "id": "3",
      "title": "Limit peak outdoor activity",
      "description": "Avoid heavy physical exertion during peak afternoon heat (11:00 AM to 4:00 PM).",
      "category": "Activity Reduction"
    }
  ],
  "model_version": "1.0.0-rf-prototype",
  "is_demo_model": true
}
```

---

## 🧪 Phase 3 Test Results

- **Backend `POST /predict` Unit & Audit Tests**: **16/16 PASSED (100% Success)**
- **Android Unit Tests**: **6/6 PASSED (100% Success)**
- **Android Gradle Build (`assembleDebug`)**: **BUILD SUCCESSFUL**
- **Android Static Analysis (`lintDebug`)**: **0 Fatal Errors**
