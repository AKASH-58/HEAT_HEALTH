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
    heat_risk_level: str = Field("Low", description="Risk level: Low, Moderate, High, Very High")
    heat_index_score: Optional[float] = Field(None, description="Calculated heat index / thermal score in °C")

class RiskModel(BaseModel):
    level: str = Field(..., description="Risk category: Low, Moderate, High, Very High")
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
