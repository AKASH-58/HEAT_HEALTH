package com.heathealth.app.data.repository

import com.heathealth.app.data.model.DashboardUiState
import com.heathealth.app.data.model.HourlyForecast
import com.heathealth.app.data.model.Recommendation
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.model.RiskPrediction
import com.heathealth.app.data.model.WeatherData
import com.heathealth.app.network.BackendApiConfig
import com.heathealth.app.network.HeatHealthApiService
import com.heathealth.app.network.dto.HeatRiskResponseDto
import com.heathealth.app.network.dto.PredictRequestDto
import com.heathealth.app.network.dto.PredictResponseDto
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException

class HeatHealthRepository(
    private val apiService: HeatHealthApiService = BackendApiConfig.createApiService()
) {

    suspend fun fetchHeatRiskData(
        latitude: Double,
        longitude: Double,
        label: String? = "New Delhi, India"
    ): Result<DashboardUiState> {
        return try {
            val dto = apiService.getHeatRisk(latitude, longitude, label)
            val state = mapDtoToUiState(dto, latitude, longitude, label)
            Result.success(state)
        } catch (e: ConnectException) {
            Result.failure(IOException("Unable to connect to backend server at ${BackendApiConfig.baseUrl}. Ensure FastAPI is running on host 0.0.0.0:8000.", e))
        } catch (e: IOException) {
            val details = e.localizedMessage ?: "Connection refused or cleartext traffic blocked"
            Result.failure(IOException("Network error connecting to ${BackendApiConfig.baseUrl}: $details", e))
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                400, 422 -> "Invalid location coordinates provided."
                502 -> "Backend error fetching weather provider data."
                504 -> "Weather provider gateway timeout. Please try again."
                else -> "Backend server returned HTTP error ${e.code()}."
            }
            Result.failure(Exception(errorMsg, e))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to process heat-risk assessment: ${e.localizedMessage}", e))
        }
    }

    suspend fun predictCustomHeatRisk(
        temperature: Double,
        humidity: Double,
        windSpeed: Double? = null
    ): Result<PredictResponseDto> {
        return try {
            val req = PredictRequestDto(
                temperatureC = temperature,
                humidityPercent = humidity,
                windSpeedKmh = windSpeed
            )
            val res = apiService.predictHeatRisk(req)
            Result.success(res)
        } catch (e: ConnectException) {
            Result.failure(IOException("Unable to connect to backend server at ${BackendApiConfig.baseUrl}. Ensure FastAPI is running on host 0.0.0.0:8000.", e))
        } catch (e: IOException) {
            val details = e.localizedMessage ?: "Connection refused or cleartext traffic blocked"
            Result.failure(IOException("Network error connecting to ${BackendApiConfig.baseUrl}: $details", e))
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                400, 422 -> "Invalid input conditions specified for prediction."
                502 -> "Backend ML model inference error."
                else -> "Backend server returned HTTP error ${e.code()}."
            }
            Result.failure(Exception(errorMsg, e))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to run custom ML prediction: ${e.localizedMessage}", e))
        }
    }

    private fun mapDtoToUiState(
        dto: HeatRiskResponseDto,
        latitude: Double,
        longitude: Double,
        label: String?
    ): DashboardUiState {
        val w = dto.weather
        val r = dto.risk

        val weatherData = WeatherData(
            temperatureCelsius = w.temperature.toInt(),
            humidityPercent = w.relativeHumidity.toInt(),
            feelsLikeCelsius = (w.apparentTemperature ?: w.temperature).toInt(),
            windSpeedKmh = w.windSpeed?.toInt(),
            timestamp = w.timestamp,
            source = w.source,
            isDemoData = false
        )

        val hourlyList = dto.hourlyForecast.map { h ->
            HourlyForecast(
                time = h.time,
                temperatureCelsius = h.temperature.toInt(),
                humidityPercent = h.relativeHumidity.toInt(),
                feelsLikeCelsius = (h.apparentTemperature ?: h.temperature).toInt(),
                windSpeedKmh = h.windSpeed?.toInt(),
                riskLevel = RiskLevel.fromDisplayName(h.heatRiskLevel),
                heatIndexScore = h.heatIndexScore
            )
        }

        val riskLevel = RiskLevel.fromDisplayName(r.level)
        val riskPrediction = RiskPrediction(
            level = riskLevel,
            score = r.score,
            scoreInterpretation = r.scoreInterpretation,
            method = r.method,
            title = "${riskLevel.displayName} Heat Risk",
            subtitle = r.method,
            description = r.scoreInterpretation,
            isDemoData = false
        )

        val recommendations = dto.recommendations.map {
            Recommendation(
                id = it.id,
                title = it.title,
                description = it.description,
                category = it.category
            )
        }

        return DashboardUiState(
            weatherData = weatherData,
            hourlyForecast = hourlyList,
            riskPrediction = riskPrediction,
            factors = dto.factors,
            recommendations = recommendations,
            disclaimer = "Prototype Notice: HeatHealth environmental heat risk assessment is calculated via ${r.method}. It is not a medical diagnosis.",
            isLoading = false,
            isRefreshing = false,
            errorMessage = null,
            locationLabel = label ?: dto.location.locationLabel ?: "Coordinates ($latitude, $longitude)",
            latitude = latitude,
            longitude = longitude,
            lastUpdated = dto.metadata.predictionTimestamp
        )
    }

    fun getDashboardSampleData(): DashboardUiState {
        val sampleHourly = listOf(
            HourlyForecast("12:00", 33, 60, 37, 10, RiskLevel.MODERATE, 37.5),
            HourlyForecast("13:00", 35, 58, 39, 12, RiskLevel.HIGH, 39.2),
            HourlyForecast("14:00", 36, 55, 41, 14, RiskLevel.EXTREME, 41.5),
            HourlyForecast("15:00", 36, 52, 40, 15, RiskLevel.HIGH, 40.8),
            HourlyForecast("16:00", 34, 56, 38, 11, RiskLevel.HIGH, 38.6),
            HourlyForecast("17:00", 32, 62, 36, 9, RiskLevel.MODERATE, 36.1),
            HourlyForecast("18:00", 30, 68, 34, 8, RiskLevel.MODERATE, 34.2)
        )

        return DashboardUiState(
            weatherData = WeatherData(
                temperatureCelsius = 34,
                humidityPercent = 65,
                feelsLikeCelsius = 38,
                windSpeedKmh = 12,
                source = "Demo Provider",
                isDemoData = true
            ),
            hourlyForecast = sampleHourly,
            riskPrediction = RiskPrediction(
                level = RiskLevel.MODERATE,
                title = "Sample Risk Assessment",
                subtitle = "Demo Prediction",
                description = "Heat strain risk is elevated under current sample conditions.",
                isDemoData = true
            ),
            factors = listOf(
                "Elevated ambient temperature (34°C)",
                "High relative humidity (65%) restricts sweat evaporation"
            ),
            recommendations = listOf(
                Recommendation(
                    id = "1",
                    title = "Drink water regularly",
                    description = "Stay hydrated throughout the day even if you do not feel thirsty."
                ),
                Recommendation(
                    id = "2",
                    title = "Take breaks in a shaded or cool area",
                    description = "Rest periodically in shaded, ventilated, or air-conditioned environments."
                ),
                Recommendation(
                    id = "3",
                    title = "Avoid intense outdoor activity",
                    description = "Minimize strenuous outdoor exercises during peak heat afternoon hours."
                ),
                Recommendation(
                    id = "4",
                    title = "Watch for warning signs",
                    description = "Pay attention to dizziness, confusion, headaches, or unusual bodily weakness."
                )
            ),
            disclaimer = "Prototype Notice: HeatHealth is an early-phase research prototype and does not provide a medical diagnosis or official heat warning. Always follow instructions from public health authorities.",
            isLoading = false,
            isRefreshing = false,
            errorMessage = null
        )
    }
}
