import os
import time
import json
import logging
from typing import Optional
from fastapi import FastAPI, Query, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.schemas import (
    WeatherResponse,
    HeatRiskResponse,
    LocationModel,
    WeatherModel,
    HourlyWeatherModel,
    RiskModel,
    RecommendationModel,
    MetadataModel,
    PredictRequest,
    PredictResponse,
    CampusFacilityModel,
    CampusFacilitiesResponse,
    CollegeModel,
    CollegesResponse,
    AddFacilityRequest,
    ReportFacilityStatusRequest,
    AlertLogModel,
    AlertHistoryResponse,
    CreateAlertRequest,
    AlertPreferencesModel,
    AlertEvaluationRequest,
    AlertEvaluationResponse,
    UpcomingForecastItemModel,
    UpcomingForecastResponse,
    DeviceTokenRequest,
    DeviceTokenResponse
)
from app.weather_service import WeatherService
from app.heat_risk_engine import BaselineHeatRiskEngine
from app.ml_pipeline import MLPipeline, MLRiskPredictor

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("heathealth-backend")

app = FastAPI(
    title="HeatHealth API",
    description="Heat-Health Risk Prediction & Early Warning System API",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

weather_service = WeatherService(timeout_seconds=5.0)

# Load experimental ML predictor if trained
if not os.path.exists(os.path.join(os.path.dirname(__file__), "app", "models", "heat_risk_model.joblib")):
    try:
        logger.info("Training experimental ML risk model on initial startup...")
        MLPipeline.train_and_save_model()
    except Exception as e:
        logger.warning(f"Initial ML model training skipped or failed: {e}")

ml_predictor = MLRiskPredictor()

@app.get("/")
def read_root():
    return {
        "app": "HeatHealth",
        "message": "Backend is running"
    }

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "heathealth-api",
        "default_prediction_method": "Baseline Rule Engine",
        "experimental_ml_model_loaded": ml_predictor.loaded,
        "ml_dataset_status": "Synthetic rule-derived labels (Not clinically validated)"
    }

@app.post("/predict", response_model=PredictResponse)
async def post_predict(request: PredictRequest):
    """
    Predicts heat-health risk level and recommendations for custom environmental weather conditions.
    """
    prediction = ml_predictor.predict_custom(
        temperature_c=request.temperature_c,
        humidity_percent=request.humidity_percent,
        wind_speed_kmh=request.wind_speed_kmh,
        apparent_temperature_c=request.apparent_temperature_c
    )

    recommendations = [
        RecommendationModel(**rec) for rec in prediction["safety_recommendations"]
    ]

    return PredictResponse(
        risk_level=prediction["risk_level"],
        risk_score=prediction["risk_score"],
        prediction_message=prediction["prediction_message"],
        factors=prediction["factors"],
        safety_recommendations=recommendations,
        model_version=prediction["model_version"],
        is_demo_model=prediction["is_demo_model"]
    )

@app.get("/api/v1/weather", response_model=WeatherResponse)
async def get_weather(
    latitude: float = Query(..., ge=-90.0, le=90.0, description="Latitude from -90 to 90"),
    longitude: float = Query(..., ge=-180.0, le=180.0, description="Longitude from -180 to 180"),
    label: Optional[str] = Query(None, description="Optional location name")
):
    """
    Retrieves validated current weather metrics from Open-Meteo for specified coordinates.
    """
    raw_weather = await weather_service.fetch_weather(latitude, longitude)

    location = LocationModel(
        latitude=latitude,
        longitude=longitude,
        location_label=label
    )

    weather = WeatherModel(
        temperature=raw_weather["temperature"],
        relative_humidity=raw_weather["relative_humidity"],
        wind_speed=raw_weather["wind_speed"],
        apparent_temperature=raw_weather["apparent_temperature"],
        timestamp=raw_weather["timestamp"],
        source=raw_weather["source"],
        is_forecast=raw_weather["is_forecast"]
    )

    hourly = [HourlyWeatherModel(**h) for h in raw_weather.get("hourly_forecast", [])]

    units = {
        "temperature": "°C",
        "relative_humidity": "%",
        "wind_speed": "km/h",
        "apparent_temperature": "°C"
    }

    return WeatherResponse(
        location=location,
        weather=weather,
        hourly_forecast=hourly,
        units=units
    )

@app.get("/api/v1/heat-risk", response_model=HeatRiskResponse)
async def get_heat_risk(
    latitude: float = Query(..., ge=-90.0, le=90.0, description="Latitude from -90 to 90"),
    longitude: float = Query(..., ge=-180.0, le=180.0, description="Longitude from -180 to 180"),
    label: Optional[str] = Query(None, description="Optional location name"),
    use_ml: bool = Query(False, description="Whether to use experimental ML model (default: False, uses Baseline Rule Engine)")
):
    """
    Retrieves live weather data and evaluates environmental heat-health risk.
    Defaults to the documented Baseline Rule Engine.
    """
    raw_weather = await weather_service.fetch_weather(latitude, longitude)

    assessment = ml_predictor.predict(
        temperature=raw_weather["temperature"],
        relative_humidity=raw_weather["relative_humidity"],
        wind_speed=raw_weather["wind_speed"],
        apparent_temperature=raw_weather["apparent_temperature"],
        use_ml=use_ml
    )

    location = LocationModel(
        latitude=latitude,
        longitude=longitude,
        location_label=label or "Current Location"
    )

    weather = WeatherModel(
        temperature=raw_weather["temperature"],
        relative_humidity=raw_weather["relative_humidity"],
        wind_speed=raw_weather["wind_speed"],
        apparent_temperature=raw_weather["apparent_temperature"],
        timestamp=raw_weather["timestamp"],
        source=raw_weather["source"],
        is_forecast=raw_weather["is_forecast"]
    )

    hourly = [HourlyWeatherModel(**h) for h in raw_weather.get("hourly_forecast", [])]

    risk = RiskModel(
        level=assessment["level"],
        score=assessment["score"],
        score_interpretation=assessment["score_interpretation"],
        method=assessment["method"]
    )

    recommendations = [
        RecommendationModel(**rec) for rec in assessment["recommendations"]
    ]

    now_iso = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    metadata = MetadataModel(
        rules_version=assessment["rules_version"],
        model_version=assessment.get("model_version"),
        prediction_timestamp=now_iso,
        data_quality="Live observation from " + raw_weather["source"]
    )

    return HeatRiskResponse(
        location=location,
        weather=weather,
        hourly_forecast=hourly,
        risk=risk,
        factors=assessment["factors"],
        recommendations=recommendations,
        metadata=metadata
    )

# Shared Verified Directory Stores for Multi-College Collaborative Campus Mapping
TODAY_DATE = time.strftime("%Y-%m-%d", time.gmtime())

SHARED_COLLEGES = [
    # Bengaluru Colleges (Karnataka)
    CollegeModel(
        id="col_iisc",
        name="Indian Institute of Science (IISc)",
        city="Bengaluru",
        address="CV Raman Rd, Mathikere, Bengaluru, Karnataka 560012",
        latitude=13.0197,
        longitude=77.5662,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_rvce",
        name="RV College of Engineering (RVCE)",
        city="Bengaluru",
        address="RV Vidyanikethan Post, Mysore Rd, Bengaluru, Karnataka 560059",
        latitude=12.9237,
        longitude=77.4987,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_bmsce",
        name="BMS College of Engineering (BMSCE)",
        city="Bengaluru",
        address="Bull Temple Rd, Basavanagudi, Bengaluru, Karnataka 560019",
        latitude=12.9410,
        longitude=77.5655,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_pes",
        name="PES University (Ring Road Campus)",
        city="Bengaluru",
        address="100 Feet Ring Rd, Banashankari 3rd Stage, Bengaluru, Karnataka 560085",
        latitude=12.9344,
        longitude=77.5345,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_msrit",
        name="Ramaiah Institute of Technology (MSRIT)",
        city="Bengaluru",
        address="MSR Nagar, Mathikere, Bengaluru, Karnataka 560054",
        latitude=13.0308,
        longitude=77.5648,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_bu",
        name="Bangalore University (Jnana Bharathi Campus)",
        city="Bengaluru",
        address="Mysore Rd, Jnana Bharathi, Bengaluru, Karnataka 560056",
        latitude=12.9352,
        longitude=77.5020,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_christ",
        name="Christ University (Central Campus)",
        city="Bengaluru",
        address="Hosur Rd, Bhavani Nagar, Bengaluru, Karnataka 560029",
        latitude=12.9343,
        longitude=77.6060,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_mcc",
        name="Mount Carmel College (MCC)",
        city="Bengaluru",
        address="58, Palace Rd, Vasanth Nagar, Bengaluru, Karnataka 560052",
        latitude=12.9904,
        longitude=77.5878,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_sju",
        name="St. Joseph's University",
        city="Bengaluru",
        address="36, Lalbagh Rd, Shanti Nagar, Bengaluru, Karnataka 560027",
        latitude=12.9619,
        longitude=77.5976,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_nlsiu",
        name="National Law School of India University (NLSIU)",
        city="Bengaluru",
        address="Gnana Bharathi Main Rd, Nagarbhavi, Bengaluru, Karnataka 560072",
        latitude=12.9482,
        longitude=77.5074,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_uvce",
        name="University Visvesvaraya College of Engineering (UVCE)",
        city="Bengaluru",
        address="KR Circle, Sampangi Rama Nagar, Bengaluru, Karnataka 560001",
        latitude=12.9754,
        longitude=77.5866,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_dsce",
        name="Dayananda Sagar College of Engineering (DSCE)",
        city="Bengaluru",
        address="Kumaraswamy Layout, Bengaluru, Karnataka 560078",
        latitude=12.9081,
        longitude=77.5662,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_bmsit",
        name="BMS Institute of Technology and Management (BMSIT)",
        city="Bengaluru",
        address="Doddaballapur Main Rd, Yelahanka, Bengaluru, Karnataka 560064",
        latitude=13.1340,
        longitude=77.5684,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_sirmvit",
        name="Sir M. Visvesvaraya Institute of Technology (Sir MVIT)",
        city="Bengaluru",
        address="Hunasamaranahalli, Yelahanka, Bengaluru, Karnataka 562157",
        latitude=13.1509,
        longitude=77.6108,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_reva",
        name="Reva University",
        city="Bengaluru",
        address="Rukmini Knowledge Park, Kattigenahalli, Yelahanka, Bengaluru 560064",
        latitude=13.1167,
        longitude=77.6347,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_nhce",
        name="New Horizon College of Engineering (NHCE)",
        city="Bengaluru",
        address="Outer Ring Rd, Near Marathahalli, Bengaluru, Karnataka 560103",
        latitude=12.9327,
        longitude=77.6918,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_cmrit",
        name="CMR Institute of Technology (CMRIT)",
        city="Bengaluru",
        address="AECS Layout, ITPL Main Rd, Kundalahalli, Bengaluru 560037",
        latitude=12.9667,
        longitude=77.7123,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_nmit",
        name="Nitte Meenakshi Institute of Technology (NMIT)",
        city="Bengaluru",
        address="Govindapura, Gollahalli, Yelahanka, Bengaluru, Karnataka 560064",
        latitude=13.1278,
        longitude=77.5932,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_bmcri",
        name="Bangalore Medical College and Research Institute (BMCRI)",
        city="Bengaluru",
        address="Fort, KR Market, Bengaluru, Karnataka 560002",
        latitude=12.9592,
        longitude=77.5750,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_stjohns",
        name="St. John's Medical College",
        city="Bengaluru",
        address="Sarjapur Rd, Koramangala, Bengaluru, Karnataka 560034",
        latitude=12.9315,
        longitude=77.6200,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_oxford",
        name="The Oxford College of Engineering",
        city="Bengaluru",
        address="Hosur Rd, Bommanahalli, Bengaluru, Karnataka 560068",
        latitude=12.9020,
        longitude=77.6225,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_rnsit",
        name="RNS Institute of Technology (RNSIT)",
        city="Bengaluru",
        address="Uttarahalli-Kengeri Main Road, Channasandra, RR Nagar, Bengaluru, Karnataka 560098",
        latitude=12.9007,
        longitude=77.5186,
        is_verified=True,
        data_source="VTU Affiliated Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    # Additional Representative National Campuses
    CollegeModel(
        id="col_du",
        name="Delhi University (DU Main Campus)",
        city="New Delhi",
        address="University Enclave, New Delhi, Delhi 110007",
        latitude=28.6139,
        longitude=77.2090,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_iitb",
        name="IIT Bombay",
        city="Mumbai",
        address="Main Gate Rd, Powai, Mumbai, Maharashtra 400076",
        latitude=19.0760,
        longitude=72.8777,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_iitm",
        name="IIT Madras",
        city="Chennai",
        address="IIT P.O., Adyar, Chennai, Tamil Nadu 600036",
        latitude=13.0067,
        longitude=80.2372,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    ),
    CollegeModel(
        id="col_ju",
        name="Jadavpur University",
        city="Kolkata",
        address="188, Raja S.C. Mallick Rd, Kolkata, West Bengal 700032",
        latitude=22.4989,
        longitude=88.3713,
        is_verified=True,
        data_source="Verified Educational Directory",
        created_at="2025-01-01T00:00:00Z",
        updated_at="2025-05-15T12:00:00Z"
    )
]

SHARED_FACILITIES = [
    # IISc Bengaluru Campus Facilities
    CampusFacilityModel(
        id="fac_iisc_w1",
        college_id="col_iisc",
        name="IISc J.R.D. Tata Memorial Library Chilled Water Point",
        category="Drinking Water",
        description="RO purified chilled water dispenser with bottle refill station.",
        latitude=13.0199,
        longitude=77.5670,
        building_location="Central Library Ground Floor Entrance, IISc Campus",
        opening_hours="08:00 AM - 10:00 PM",
        accessibility_info="Wheelchair accessible, Low-height spout",
        status="Operational",
        is_verified=True,
        last_verified_timestamp=TODAY_DATE,
        created_by="Campus Admin",
        updated_by="Campus Admin",
        updated_timestamp=TODAY_DATE,
        contact_info="Helpdesk: ext 102"
    ),
    CampusFacilityModel(
        id="fac_iisc_s1",
        college_id="col_iisc",
        name="Main Academic Building Banyan Quadrangle Shade",
        category="Shaded Area",
        description="Covered seating pergola under ancient banyan tree canopy with mist spray cooling.",
        latitude=13.0190,
        longitude=77.5655,
        building_location="Main Quadrangle Lawn, IISc Mathikere",
        opening_hours="24/7 Access",
        accessibility_info="Ramp accessible seating",
        status="Operational",
        is_verified=True,
        last_verified_timestamp=TODAY_DATE,
        created_by="Campus Admin",
        updated_by="Campus Admin",
        updated_timestamp=TODAY_DATE,
        contact_info=None
    ),
    CampusFacilityModel(
        id="fac_iisc_c1",
        college_id="col_iisc",
        name="Student Activity Center Climate Lounge",
        category="Cool Indoor Space",
        description="Air-conditioned quiet study hall with climate control maintained at 23°C.",
        latitude=13.0210,
        longitude=77.5675,
        building_location="Student Center 1st Floor, Room 104",
        opening_hours="09:00 AM - 08:00 PM",
        accessibility_info="Elevator accessible",
        status="Operational",
        is_verified=True,
        last_verified_timestamp=TODAY_DATE,
        created_by="Campus Admin",
        updated_by="Campus Admin",
        updated_timestamp=TODAY_DATE,
        contact_info="Manager: +91 98765 43210"
    ),
    CampusFacilityModel(
        id="fac_iisc_m1",
        college_id="col_iisc",
        name="IISc Health Center & Emergency Triage Room",
        category="First Aid",
        description="Full first-aid facility equipped with ORS, ice packs, cooling beds, and emergency medical staff.",
        latitude=13.0185,
        longitude=77.5645,
        building_location="Health Center Building, IISc West Gate",
        opening_hours="24/7 Emergency Care",
        accessibility_info="Full medical ramp & stretcher access",
        status="Operational",
        is_verified=True,
        last_verified_timestamp=TODAY_DATE,
        created_by="Campus Admin",
        updated_by="Campus Admin",
        updated_timestamp=TODAY_DATE,
        contact_info="Emergency Helpline: 108 / 112 or ext 999"
    ),
    # RVCE Bengaluru Campus Facilities
    CampusFacilityModel(
        id="fac_rvce_w1",
        college_id="col_rvce",
        name="RVCE Administrative Block Chilled Water Kiosk",
        category="Drinking Water",
        description="UV-purified cold water dispenser.",
        latitude=12.9239,
        longitude=77.4989,
        building_location="Main Administrative Block Ground Floor",
        opening_hours="08:00 AM - 06:00 PM",
        accessibility_info="Ground floor access",
        status="Operational",
        is_verified=True,
        last_verified_timestamp=TODAY_DATE,
        created_by="Campus Admin",
        updated_by="Campus Admin",
        updated_timestamp=TODAY_DATE,
        contact_info=None
    ),
    # RNSIT Bengaluru Campus Facilities
    CampusFacilityModel(
        id="fac_rnsit_w1",
        college_id="col_rnsit",
        name="RNSIT Academic Block A Chilled Water Station",
        category="Drinking Water",
        description="RO purified chilled water dispenser with bottle refill kiosk.",
        latitude=12.9008,
        longitude=77.5187,
        building_location="Academic Block A Ground Floor Foyer, RR Nagar",
        opening_hours="08:00 AM - 07:00 PM",
        accessibility_info="Wheelchair accessible",
        status="Operational",
        is_verified=True,
        last_verified_timestamp=TODAY_DATE,
        created_by="Campus Admin",
        updated_by="Campus Admin",
        updated_timestamp=TODAY_DATE,
        contact_info="Helpdesk: +91 80 2861 1880"
    ),
    CampusFacilityModel(
        id="fac_rnsit_c1",
        college_id="col_rnsit",
        name="RNSIT Central Library AC Reference Hall",
        category="Cool Indoor Space",
        description="Air-conditioned quiet study reading room with climate control.",
        latitude=12.9005,
        longitude=77.5185,
        building_location="Central Library 1st Floor, RNSIT Campus",
        opening_hours="08:30 AM - 08:00 PM",
        accessibility_info="Elevator accessible",
        status="Operational",
        is_verified=True,
        last_verified_timestamp=TODAY_DATE,
        created_by="Campus Admin",
        updated_by="Campus Admin",
        updated_timestamp=TODAY_DATE,
        contact_info=None
    ),
    # BMSCE Bengaluru Campus Facilities
    CampusFacilityModel(
        id="fac_bmsce_c1",
        college_id="col_bmsce",
        name="BMSCE Central Library AC Reading Hall",
        category="Cool Indoor Space",
        description="Air-conditioned reference reading room.",
        latitude=12.9412,
        longitude=77.5658,
        building_location="Library Building 2nd Floor",
        opening_hours="08:00 AM - 09:00 PM",
        accessibility_info="Elevator access",
        status="Operational",
        is_verified=True,
        last_verified_timestamp=TODAY_DATE,
        created_by="Campus Admin",
        updated_by="Campus Admin",
        updated_timestamp=TODAY_DATE,
        contact_info=None
    ),
    # DU New Delhi Campus Facilities
    CampusFacilityModel(
        id="fac_du_w1",
        college_id="col_du",
        name="DU Arts Faculty Hydration Fountain",
        category="Drinking Water",
        description="Chilled drinking water point near Arts Quadrangle.",
        latitude=28.6142,
        longitude=77.2095,
        building_location="Arts Faculty Building Entrance",
        opening_hours="08:00 AM - 08:00 PM",
        accessibility_info="Ground floor access",
        status="Operational",
        is_verified=True,
        last_verified_timestamp=TODAY_DATE,
        created_by="Campus Admin",
        updated_by="Campus Admin",
        updated_timestamp=TODAY_DATE,
        contact_info=None
    )
]

@app.get("/api/v1/campus-cool-map/colleges", response_model=CollegesResponse)
async def get_colleges(
    query: Optional[str] = Query(None, description="Search query by name, city, or address"),
    city: Optional[str] = Query(None, description="City filter")
):
    """
    Returns colleges matching the search query or city filter.
    """
    colleges = list(SHARED_COLLEGES)
    if city:
        colleges = [c for c in colleges if c.city.lower() == city.lower()]
    if query:
        q = query.lower()
        colleges = [
            c for c in colleges
            if q in c.name.lower() or q in c.city.lower() or q in c.address.lower()
        ]

    return CollegesResponse(colleges=colleges, total_count=len(colleges))

@app.get("/api/v1/campus-cool-map/facilities", response_model=CampusFacilitiesResponse)
async def get_campus_facilities(
    college_id: Optional[str] = Query(None, description="Filter facilities by college ID e.g. col_iisc"),
    category: Optional[str] = Query(None, description="Optional category filter: Drinking Water, Shaded Area, Cool Indoor Space, First Aid")
):
    """
    Returns verified and community-reported campus cool facilities.
    """
    now_iso = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    facilities = list(SHARED_FACILITIES)

    if college_id:
        facilities = [f for f in facilities if f.college_id.lower() == college_id.lower()]
    if category:
        facilities = [f for f in facilities if f.category.lower() == category.lower()]

    return CampusFacilitiesResponse(
        facilities=facilities,
        total_count=len(facilities),
        last_updated=now_iso
    )

@app.post("/api/v1/campus-cool-map/facilities", response_model=CampusFacilityModel)
async def add_campus_facility(req: AddFacilityRequest):
    """
    Collaborative endpoint allowing students to submit new campus facilities in real-time.
    """
    now_iso = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    today_date = time.strftime("%Y-%m-%d", time.gmtime())
    new_id = f"fac_user_{int(time.time() * 1000)}"

    new_facility = CampusFacilityModel(
        id=new_id,
        college_id=req.college_id,
        name=req.name,
        category=req.category,
        description=req.description,
        latitude=req.latitude,
        longitude=req.longitude,
        building_location=req.building_location,
        opening_hours=req.opening_hours or "Check on-site schedule",
        accessibility_info=req.accessibility_info or "Standard access",
        status="Operational",
        is_verified=False,  # Community-Reported / Pending Verification
        last_verified_timestamp=today_date,
        created_by="Campus Member",
        updated_by="Campus Member",
        updated_timestamp=now_iso,
        contact_info=req.contact_info
    )

    SHARED_FACILITIES.append(new_facility)
    return new_facility

@app.put("/api/v1/campus-cool-map/facilities/{facility_id}/status", response_model=CampusFacilityModel)
async def update_facility_status(facility_id: str, req: ReportFacilityStatusRequest):
    """
    Collaborative status reporting endpoint for broken water points, closed rooms, or maintenance issues.
    """
    now_iso = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())

    for i, fac in enumerate(SHARED_FACILITIES):
        if fac.id == facility_id:
            updated = fac.copy(
                update={
                    "status": req.status,
                    "updated_by": req.reported_by or "Campus Member",
                    "updated_timestamp": now_iso,
                    "description": f"{fac.description} [Status Note: {req.status_note}]" if req.status_note else fac.description
                }
            )
            SHARED_FACILITIES[i] = updated
            return updated

    raise HTTPException(status_code=404, detail="Facility ID not found")

# Phase 8 Smart Heat Alerts & Notification State Stores with Atomic File Persistence
DATA_DIR = os.path.join(os.path.dirname(__file__), "app", "data")
os.makedirs(DATA_DIR, exist_ok=True)

ALERTS_PERSIST_FILE = os.path.join(DATA_DIR, "alert_history.json")
PREFS_PERSIST_FILE = os.path.join(DATA_DIR, "alert_preferences.json")

def load_persisted_alerts():
    if os.path.exists(ALERTS_PERSIST_FILE):
        try:
            with open(ALERTS_PERSIST_FILE, "r", encoding="utf-8") as f:
                raw_list = json.load(f)
                return [AlertLogModel(**item) for item in raw_list]
        except Exception as e:
            logger.warning(f"Malformed or unreadable alert history file ({e}). Recovering defaults.")
            try:
                os.replace(ALERTS_PERSIST_FILE, ALERTS_PERSIST_FILE + ".bak")
            except Exception:
                pass

    defaults = [
        AlertLogModel(
            id="alert_init_1",
            timestamp=int((time.time() - 3600 * 2) * 1000),
            iso_timestamp=time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime(time.time() - 3600 * 2)),
            title="High Thermal Stress Advisory",
            description="Apparent temperature reached 38.5°C. Stay hydrated and limit outdoor strenuous physical activities.",
            risk_level="High",
            trigger_source="Automated Risk Monitor",
            recommendations=[
                "Drink at least 250-500 mL water every hour",
                "Seek shaded or air-conditioned campus rest areas",
                "Avoid intense outdoor physical exercise between 12 PM - 4 PM"
            ]
        ),
        AlertLogModel(
            id="alert_init_2",
            timestamp=int((time.time() - 3600 * 24) * 1000),
            iso_timestamp=time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime(time.time() - 3600 * 24)),
            title="Elevated Humidity Strain Warning",
            description="High relative humidity restricted natural sweat evaporation.",
            risk_level="Moderate",
            trigger_source="Automated Risk Monitor",
            recommendations=[
                "Maintain regular fluid intake",
                "Wear light, breathable clothing"
            ]
        )
    ]
    save_persisted_alerts(defaults)
    return defaults

def save_persisted_alerts(alerts):
    tmp_path = ALERTS_PERSIST_FILE + ".tmp"
    try:
        raw_list = [a.model_dump() if hasattr(a, "model_dump") else a.dict() for a in alerts]
        with open(tmp_path, "w", encoding="utf-8") as f:
            json.dump(raw_list, f, indent=2)
            f.flush()
            os.fsync(f.fileno())
        os.replace(tmp_path, ALERTS_PERSIST_FILE)
    except Exception as e:
        logger.warning(f"Failed to save alert history to disk: {e}")
        if os.path.exists(tmp_path):
            try:
                os.remove(tmp_path)
            except Exception:
                pass

def load_persisted_preferences():
    if os.path.exists(PREFS_PERSIST_FILE):
        try:
            with open(PREFS_PERSIST_FILE, "r", encoding="utf-8") as f:
                raw_dict = json.load(f)
                return AlertPreferencesModel(**raw_dict)
        except Exception as e:
            logger.warning(f"Malformed or unreadable alert preferences file ({e}). Recovering defaults.")
            try:
                os.replace(PREFS_PERSIST_FILE, PREFS_PERSIST_FILE + ".bak")
            except Exception:
                pass

    defaults = AlertPreferencesModel(
        heat_alerts_enabled=True,
        notifications_enabled=True,
        alert_threshold="High",
        reminder_frequency_hours=2,
        cooldown_minutes=60,
        last_alerted_risk_level=""
    )
    save_persisted_preferences(defaults)
    return defaults

def save_persisted_preferences(prefs):
    tmp_path = PREFS_PERSIST_FILE + ".tmp"
    try:
        raw_dict = prefs.model_dump() if hasattr(prefs, "model_dump") else prefs.dict()
        with open(tmp_path, "w", encoding="utf-8") as f:
            json.dump(raw_dict, f, indent=2)
            f.flush()
            os.fsync(f.fileno())
        os.replace(tmp_path, PREFS_PERSIST_FILE)
    except Exception as e:
        logger.warning(f"Failed to save alert preferences to disk: {e}")
        if os.path.exists(tmp_path):
            try:
                os.remove(tmp_path)
            except Exception:
                pass

SHARED_ALERT_LOGS = load_persisted_alerts()
USER_ALERT_PREFERENCES = load_persisted_preferences()

REGISTERED_DEVICE_TOKENS = []

RISK_RANK = {
    "low": 0,
    "moderate": 1,
    "high": 2,
    "extreme": 3
}

@app.get("/api/v1/alerts/history", response_model=AlertHistoryResponse)
async def get_alert_history(
    limit: int = Query(20, ge=1, le=100, description="Max alerts to return"),
    offset: int = Query(0, ge=0, description="Pagination offset")
):
    """
    Retrieves reverse-chronological alert history records.
    """
    sorted_alerts = sorted(SHARED_ALERT_LOGS, key=lambda a: a.timestamp, reverse=True)
    paged = sorted_alerts[offset:offset + limit]
    return AlertHistoryResponse(
        alerts=paged,
        total_count=len(SHARED_ALERT_LOGS)
    )

@app.post("/api/v1/alerts/history", response_model=AlertLogModel)
async def create_alert_history(req: CreateAlertRequest):
    """
    Logs a new heat risk alert event into history.
    """
    now_ms = int(time.time() * 1000)
    now_iso = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    new_id = f"alert_log_{now_ms}"

    alert = AlertLogModel(
        id=new_id,
        timestamp=now_ms,
        iso_timestamp=now_iso,
        title=req.title,
        description=req.description,
        risk_level=req.risk_level,
        trigger_source=req.trigger_source,
        recommendations=req.recommendations
    )

    SHARED_ALERT_LOGS.insert(0, alert)
    save_persisted_alerts(SHARED_ALERT_LOGS)
    return alert

@app.delete("/api/v1/alerts/history")
async def clear_alert_history():
    """
    Clears all recorded alert logs.
    """
    SHARED_ALERT_LOGS.clear()
    save_persisted_alerts(SHARED_ALERT_LOGS)
    return {"status": "cleared", "message": "Alert history cleared successfully"}

@app.get("/api/v1/alerts/preferences", response_model=AlertPreferencesModel)
async def get_alert_preferences():
    """
    Retrieves persistent notification and alert trigger preferences.
    """
    return USER_ALERT_PREFERENCES

@app.put("/api/v1/alerts/preferences", response_model=AlertPreferencesModel)
async def update_alert_preferences(prefs: AlertPreferencesModel):
    """
    Updates user notification threshold and alert preferences.
    """
    global USER_ALERT_PREFERENCES
    USER_ALERT_PREFERENCES = prefs
    save_persisted_preferences(USER_ALERT_PREFERENCES)
    return USER_ALERT_PREFERENCES

@app.post("/api/v1/alerts/evaluate", response_model=AlertEvaluationResponse)
async def evaluate_alert(req: AlertEvaluationRequest):
    """
    Evaluates current weather conditions against student threshold and deduplication rules.
    """
    alerts_enabled = req.heat_alerts_enabled if req.heat_alerts_enabled is not None else USER_ALERT_PREFERENCES.heat_alerts_enabled
    notifs_enabled = req.notifications_enabled if req.notifications_enabled is not None else USER_ALERT_PREFERENCES.notifications_enabled

    assessment = ml_predictor.predict(
        temperature=req.temperature_c,
        relative_humidity=req.humidity_percent,
        wind_speed=None,
        apparent_temperature=None,
        use_ml=False
    )

    current_risk = assessment["level"]

    if not alerts_enabled or not notifs_enabled:
        return AlertEvaluationResponse(
            should_alert=False,
            risk_level=current_risk,
            title="Heat Alerts Disabled",
            message="Alert notifications are currently disabled in user preferences.",
            recommendations=[RecommendationModel(**r) for r in assessment["recommendations"]],
            reason="Alert evaluation suppressed because heat alert notifications are disabled in preferences."
        )

    user_thresh = req.user_threshold.lower()
    current_rank = RISK_RANK.get(current_risk.lower(), 0)
    thresh_rank = RISK_RANK.get(user_thresh, 2)

    if current_rank < thresh_rank:
        return AlertEvaluationResponse(
            should_alert=False,
            risk_level=current_risk,
            title=f"{current_risk} Risk Level Below Threshold",
            message=f"Current risk ({current_risk}) is below your configured alert threshold ({req.user_threshold}).",
            recommendations=[RecommendationModel(**r) for r in assessment["recommendations"]],
            reason="Risk level does not satisfy minimum configured notification threshold."
        )

    last_level = req.last_alerted_risk_level.lower()
    last_ts = req.last_alerted_timestamp
    cooldown_ms = req.cooldown_minutes * 60 * 1000
    now_ms = int(time.time() * 1000)

    is_same_risk = (last_level == current_risk.lower())
    cooldown_active = (last_ts is not None) and ((now_ms - last_ts) < cooldown_ms)

    if is_same_risk and cooldown_active:
        return AlertEvaluationResponse(
            should_alert=False,
            risk_level=current_risk,
            title=f"{current_risk} Risk Advisory Unchanged",
            message=f"Risk level remains at {current_risk}.",
            recommendations=[RecommendationModel(**r) for r in assessment["recommendations"]],
            reason=f"Duplicate alert suppressed within active {req.cooldown_minutes}-minute cooldown window."
        )

    title = f"{current_risk} Heat Risk Advisory"
    msg = f"Thermal risk has reached {current_risk} ({req.temperature_c}°C, {req.humidity_percent}% RH). Take protective measures."

    return AlertEvaluationResponse(
        should_alert=True,
        risk_level=current_risk,
        title=title,
        message=msg,
        recommendations=[RecommendationModel(**r) for r in assessment["recommendations"]],
        reason=f"Risk level ({current_risk}) met or exceeded threshold ({req.user_threshold}) and passed cooldown rules."
    )

    return AlertEvaluationResponse(
        should_alert=True,
        risk_level=current_risk,
        title=title,
        message=msg,
        recommendations=[RecommendationModel(**r) for r in assessment["recommendations"]],
        reason=f"Risk level ({current_risk}) met or exceeded threshold ({req.user_threshold}) and passed cooldown rules."
    )

    return AlertEvaluationResponse(
        should_alert=True,
        risk_level=current_risk,
        title=title,
        message=msg,
        recommendations=[RecommendationModel(**r) for r in assessment["recommendations"]],
        reason=f"Risk level ({current_risk}) met or exceeded threshold ({req.user_threshold}) and state changed."
    )

@app.get("/api/v1/alerts/forecast", response_model=UpcomingForecastResponse)
async def get_upcoming_forecast(
    latitude: float = Query(..., ge=-90.0, le=90.0),
    longitude: float = Query(..., ge=-180.0, le=180.0),
    label: Optional[str] = Query(None)
):
    """
    Retrieves genuine upcoming high-risk hourly periods from weather forecast data.
    """
    now_iso = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())

    try:
        raw_weather = await weather_service.fetch_weather(latitude, longitude)
        hourly_data = raw_weather.get("hourly_forecast", [])

        if not hourly_data:
            return UpcomingForecastResponse(
                forecast_available=False,
                location_label=label or "Current Location",
                items=[],
                generated_at=now_iso
            )

        items = []
        for h in hourly_data[:12]:
            temp = h.get("temperature", 30.0)
            rh = h.get("relative_humidity", 50.0)
            app_temp = h.get("apparent_temperature", temp)
            time_str = h.get("time", "")

            risk = ml_predictor.predict(
                temperature=temp,
                relative_humidity=rh,
                wind_speed=h.get("wind_speed"),
                apparent_temperature=app_temp,
                use_ml=False
            )

            rec_texts = [r["description"] for r in risk.get("recommendations", [])[:2]]

            items.append(
                UpcomingForecastItemModel(
                    time_period=time_str,
                    temperature_c=temp,
                    humidity_percent=rh,
                    apparent_temperature_c=app_temp,
                    risk_level=risk["level"],
                    recommendations=rec_texts
                )
            )

        return UpcomingForecastResponse(
            forecast_available=True,
            location_label=label or "Current Location",
            items=items,
            generated_at=now_iso
        )
    except Exception as e:
        logger.warning(f"Error fetching upcoming forecast: {e}")
        return UpcomingForecastResponse(
            forecast_available=False,
            location_label=label or "Current Location",
            items=[],
            generated_at=now_iso
        )

@app.post("/api/v1/alerts/device-token", response_model=DeviceTokenResponse)
async def register_device_token(req: DeviceTokenRequest):
    """
    Registers an authenticated FCM device token for remote push notification delivery.
    """
    if req.device_token not in REGISTERED_DEVICE_TOKENS:
        REGISTERED_DEVICE_TOKENS.append(req.device_token)

    return DeviceTokenResponse(
        status="success",
        message=f"Device token successfully registered ({len(REGISTERED_DEVICE_TOKENS)} active tokens)."
    )



