from typing import List, Optional, Dict
from pydantic import BaseModel, Field

class LocationModel(BaseModel):
    latitude: float = Field(..., ge=-90.0, le=90.0, description="Latitude between -90 and 90")
    longitude: float = Field(..., ge=-180.0, le=180.0, description="Longitude between -180 and 180")
    location_label: Optional[str] = Field(None, description="Optional name or description of location")

class WeatherModel(BaseModel):
    temperature: float = Field(..., description="Air temperature in degrees Celsius")
    relative_humidity: float = Field(..., description="Relative humidity in percentage (0-100%)")
    wind_speed: Optional[float] = Field(None, description="Wind speed in km/h if available")
    apparent_temperature: Optional[float] = Field(None, description="Apparent temperature / Feels-like in degrees Celsius")
    timestamp: str = Field(..., description="ISO timestamp of weather observation")
    source: str = Field(..., description="Weather data source provider")
    is_forecast: bool = Field(False, description="Whether data represents future forecast or current observation")

class HourlyWeatherModel(BaseModel):
    time: str = Field(..., description="Time string e.g. 14:00 or ISO timestamp")
    temperature: float = Field(..., description="Temperature in °C")
    relative_humidity: float = Field(..., description="Relative humidity in %")
    apparent_temperature: Optional[float] = Field(None, description="Apparent temperature / Feels-like in °C")
    wind_speed: Optional[float] = Field(None, description="Wind speed in km/h")
    heat_risk_level: str = Field("Low", description="Risk level: Low, Moderate, High, Extreme")
    heat_index_score: Optional[float] = Field(None, description="Calculated heat index / thermal score in °C")

class RiskModel(BaseModel):
    level: str = Field(..., description="Risk category: Low, Moderate, High, Extreme")
    score: Optional[float] = Field(None, description="Calculated thermal stress score / Heat Index in °C")
    score_interpretation: str = Field(..., description="Textual explanation of the risk score")
    method: str = Field(..., description="Calculation method used")

class RecommendationModel(BaseModel):
    id: str = Field(..., description="Unique ID for recommendation")
    title: str = Field(..., description="Short action summary")
    description: str = Field(..., description="Detailed safety instruction")
    category: str = Field("Safety Action", description="Category classification")

class MetadataModel(BaseModel):
    rules_version: str = Field(..., description="Version of the heat health rules applied")
    model_version: Optional[str] = Field(None, description="Version of the ML model if applied")
    prediction_timestamp: str = Field(..., description="ISO timestamp of prediction generation")
    data_quality: str = Field(..., description="Indicator of data completeness and source status")

class HeatRiskResponse(BaseModel):
    location: LocationModel
    weather: WeatherModel
    hourly_forecast: List[HourlyWeatherModel] = Field(default_factory=list)
    risk: RiskModel
    factors: List[str]
    recommendations: List[RecommendationModel]
    metadata: MetadataModel

class WeatherResponse(BaseModel):
    location: LocationModel
    weather: WeatherModel
    hourly_forecast: List[HourlyWeatherModel] = Field(default_factory=list)
    units: Dict[str, str]

# Phase 3 ML Prediction Models
class PredictRequest(BaseModel):
    temperature_c: float = Field(..., ge=-100.0, le=80.0, description="Air temperature in degrees Celsius")
    humidity_percent: float = Field(..., ge=0.0, le=100.0, description="Relative humidity in percentage (0-100%)")
    wind_speed_kmh: Optional[float] = Field(None, ge=0.0, le=250.0, description="Wind speed in km/h if available")
    apparent_temperature_c: Optional[float] = Field(None, ge=-100.0, le=80.0, description="Apparent temperature in °C if available")

class PredictResponse(BaseModel):
    risk_level: str = Field(..., description="Predicted risk level: Low, Moderate, High, Extreme")
    risk_score: Optional[float] = Field(None, description="Calculated thermal stress score / Heat Index in °C")
    prediction_message: str = Field(..., description="Understandable explanation of the predicted heat risk")
    factors: List[str] = Field(default_factory=list, description="Contributing environmental factors")
    safety_recommendations: List[RecommendationModel] = Field(default_factory=list, description="Actionable heat precautions")
    model_version: str = Field("1.0.0-rf-prototype", description="ML model version tag")
    is_demo_model: bool = Field(True, description="Identifies illustrative prototype ML model status")

# Phase 7 Campus Cool Map Models
class CollegeModel(BaseModel):
    id: str = Field(..., description="Unique college identifier")
    name: str = Field(..., description="College or university name")
    city: str = Field(..., description="City location")
    address: str = Field(..., description="Campus street address or sector")
    latitude: float = Field(..., description="Campus central latitude")
    longitude: float = Field(..., description="Campus central longitude")
    is_verified: bool = Field(True, description="Verified campus status")
    data_source: str = Field("Verified Campus Directory", description="Data provenance")
    created_at: str = Field(..., description="Record creation timestamp")
    updated_at: str = Field(..., description="Record update timestamp")

class CollegesResponse(BaseModel):
    colleges: List[CollegeModel] = Field(default_factory=list)
    total_count: int = Field(..., description="Total matching colleges")

class CampusFacilityModel(BaseModel):
    id: str = Field(..., description="Unique facility identifier")
    college_id: str = Field("col_iisc", description="Associated college ID")
    name: str = Field(..., description="Facility display name")
    category: str = Field(..., description="Facility category: Drinking Water, Shaded Area, Cool Indoor Space, First Aid")
    description: str = Field(..., description="Details and usage instructions")
    latitude: Optional[float] = Field(None, ge=-90.0, le=90.0, description="Latitude coordinate if available")
    longitude: Optional[float] = Field(None, ge=-180.0, le=180.0, description="Longitude coordinate if available")
    building_location: str = Field(..., description="Building, floor, or campus sector location")
    opening_hours: Optional[str] = Field(None, description="Verified operating hours")
    accessibility_info: Optional[str] = Field(None, description="Accessibility details e.g. Wheelchair accessible")
    status: str = Field("Operational", description="Facility status: Operational, Maintenance / Broken, Closed")
    is_verified: bool = Field(True, description="Verification status by campus authorities")
    last_verified_timestamp: str = Field(..., description="Date/time of last verification")
    created_by: str = Field("Campus Member", description="Contributor name or role")
    updated_by: str = Field("Campus Member", description="Last editor name or role")
    updated_timestamp: str = Field(..., description="ISO timestamp of last status update")
    contact_info: Optional[str] = Field(None, description="Contact phone or emergency helpline")

class CampusFacilitiesResponse(BaseModel):
    facilities: List[CampusFacilityModel] = Field(default_factory=list)
    total_count: int = Field(..., description="Total facilities returned")
    last_updated: str = Field(..., description="ISO timestamp of data generation")

class AddFacilityRequest(BaseModel):
    college_id: str = Field(..., description="Target college ID")
    name: str = Field(..., description="Facility name")
    category: str = Field(..., description="Facility category")
    description: str = Field(..., description="Facility details")
    latitude: Optional[float] = Field(None, ge=-90.0, le=90.0)
    longitude: Optional[float] = Field(None, ge=-180.0, le=180.0)
    building_location: str = Field(..., description="Building or sector location")
    opening_hours: Optional[str] = Field(None)
    accessibility_info: Optional[str] = Field(None)
    contact_info: Optional[str] = Field(None)

class ReportFacilityStatusRequest(BaseModel):
    facility_id: str = Field(..., description="Facility ID to update")
    status: str = Field(..., description="New status: Operational, Maintenance / Broken, Closed")
    status_note: Optional[str] = Field(None, description="Problem description or note")
    reported_by: Optional[str] = Field("Campus Member", description="User name or role")

# Phase 8 Smart Heat Alerts & Notification Models
class AlertLogModel(BaseModel):
    id: str = Field(..., description="Unique alert log ID")
    timestamp: int = Field(..., description="Timestamp in milliseconds since epoch")
    iso_timestamp: str = Field(..., description="ISO 8601 formatted timestamp")
    title: str = Field(..., description="Alert title e.g. Extreme Heat Risk Advisory")
    description: str = Field(..., description="Detailed alert summary and safety advice")
    risk_level: str = Field(..., description="Risk category: Low, Moderate, High, Extreme")
    trigger_source: str = Field("Automated Risk Monitor", description="Triggering system or source")
    recommendations: List[str] = Field(default_factory=list, description="Associated safety precautions")

class AlertHistoryResponse(BaseModel):
    alerts: List[AlertLogModel] = Field(default_factory=list)
    total_count: int = Field(..., description="Total matching alert records")

class CreateAlertRequest(BaseModel):
    title: str = Field(..., description="Alert title")
    description: str = Field(..., description="Detailed description")
    risk_level: str = Field(..., description="Risk level: Low, Moderate, High, Extreme")
    trigger_source: str = Field("Local Risk Monitor", description="Trigger source")
    recommendations: List[str] = Field(default_factory=list)

class AlertPreferencesModel(BaseModel):
    heat_alerts_enabled: bool = Field(True, description="Whether heat risk alerts are active")
    notifications_enabled: bool = Field(True, description="Master notification toggle")
    alert_threshold: str = Field("High", description="Minimum risk level triggering notifications: Moderate, High, Extreme")
    reminder_frequency_hours: int = Field(2, description="Hydration/safety reminder interval in hours")
    cooldown_minutes: int = Field(60, description="Deduplication cooldown window in minutes")
    last_alerted_risk_level: str = Field("", description="Last alerted risk category")

class AlertEvaluationRequest(BaseModel):
    temperature_c: float = Field(..., description="Current ambient temperature °C")
    humidity_percent: float = Field(..., description="Relative humidity %")
    user_threshold: str = Field("High", description="Selected minimum alert threshold")
    last_alerted_risk_level: str = Field("", description="Last notified risk level")
    last_alerted_timestamp: Optional[int] = Field(None, description="Timestamp in milliseconds when last alerted")
    cooldown_minutes: int = Field(60, description="Deduplication cooldown window in minutes")
    heat_alerts_enabled: Optional[bool] = Field(None, description="Optional override for heat_alerts_enabled")
    notifications_enabled: Optional[bool] = Field(None, description="Optional override for notifications_enabled")

class AlertEvaluationResponse(BaseModel):
    should_alert: bool = Field(..., description="True if alert criteria and deduplication rules pass")
    risk_level: str = Field(..., description="Evaluated risk level")
    title: str = Field(..., description="Alert title")
    message: str = Field(..., description="Brief notification message")
    recommendations: List[RecommendationModel] = Field(default_factory=list)
    reason: str = Field(..., description="Explanation of evaluation decision")

class UpcomingForecastItemModel(BaseModel):
    time_period: str = Field(..., description="Human readable timeframe e.g. 12:00 PM - 03:00 PM")
    temperature_c: float = Field(..., description="Expected temperature in °C")
    humidity_percent: float = Field(..., description="Expected relative humidity %")
    apparent_temperature_c: Optional[float] = Field(None, description="Expected feels-like temperature in °C")
    risk_level: str = Field(..., description="Predicted risk level")
    recommendations: List[str] = Field(default_factory=list, description="Specific precautions for timeframe")

class UpcomingForecastResponse(BaseModel):
    forecast_available: bool = Field(..., description="True if genuine forecast data is accessible")
    location_label: str = Field(..., description="Target location name")
    items: List[UpcomingForecastItemModel] = Field(default_factory=list)
    generated_at: str = Field(..., description="ISO timestamp when forecast was evaluated")

class DeviceTokenRequest(BaseModel):
    device_token: str = Field(..., description="FCM device push token")
    device_type: str = Field("android", description="Platform identifier")

class DeviceTokenResponse(BaseModel):
    status: str = Field("success", description="Registration status")
    message: str = Field(..., description="Confirmation details")

