package com.heathealth.app.data.repository

import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.heathealth.app.data.local.dao.AlertLogDao
import com.heathealth.app.data.local.dao.HeatRiskLogDao
import com.heathealth.app.data.local.entity.AlertLogEntity
import com.heathealth.app.data.local.entity.HeatRiskLogEntity
import com.heathealth.app.data.model.DashboardUiState
import com.heathealth.app.data.model.DataSource
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import retrofit2.HttpException
import java.io.IOException

class HeatHealthRepository(
    private var apiService: HeatHealthApiService = BackendApiConfig.createApiService(),
    private val heatRiskLogDao: HeatRiskLogDao? = null,
    private val alertLogDao: AlertLogDao? = null,
    private val enableCandidateFailover: Boolean = true
) {

    suspend fun fetchHeatRiskData(
        latitude: Double,
        longitude: Double,
        label: String? = "New Delhi, India"
    ): Result<DashboardUiState> {
        try {
            val dto = apiService.getHeatRisk(latitude, longitude, label)
            val state = mapDtoToUiState(dto, latitude, longitude, label)
            persistRiskLog(dto, label)
            return Result.success(state)
        } catch (e: HttpException) {
            val code = e.code()
            if (code == 400 || code == 422) {
                return Result.failure(IllegalArgumentException("Invalid input coordinates (Latitude -90..90, Longitude -180..180). HTTP $code", e))
            } else {
                return Result.failure(IOException("Server error ($code): ${e.message()}", e))
            }
        } catch (e: JsonSyntaxException) {
            return Result.failure(IllegalArgumentException("Received malformed JSON data from server.", e))
        } catch (e: JsonParseException) {
            return Result.failure(IllegalArgumentException("Received malformed JSON payload structure from server.", e))
        } catch (initialErr: Exception) {
            if (!enableCandidateFailover) {
                val details = initialErr.localizedMessage ?: "Unable to connect to backend server"
                return Result.failure(IOException("Unable to connect to FastAPI backend. ($details)", initialErr))
            }

            // Fallback across candidate base URLs for network connection issues
            var lastException: Exception = initialErr
            for (candidateUrl in BackendApiConfig.getCandidateBaseUrls()) {
                try {
                    val service = BackendApiConfig.createApiService(candidateUrl)
                    val dto = service.getHeatRisk(latitude, longitude, label)
                    BackendApiConfig.baseUrl = candidateUrl
                    this.apiService = service
                    val state = mapDtoToUiState(dto, latitude, longitude, label)
                    persistRiskLog(dto, label)
                    return Result.success(state)
                } catch (e: Exception) {
                    lastException = e
                }
            }

            val details = lastException.localizedMessage ?: "Unable to connect to backend server"
            return Result.failure(IOException("Unable to connect to FastAPI backend at ${BackendApiConfig.baseUrl}. Ensure FastAPI is running on host 0.0.0.0:8000. ($details)", lastException))
        }
    }

    private suspend fun persistRiskLog(dto: HeatRiskResponseDto, label: String?) {
        heatRiskLogDao?.let { dao ->
            try {
                val nowMs = System.currentTimeMillis()
                val targetLabel = label ?: dto.location.locationLabel ?: "Current Location"
                val latest = dao.getLatestLog()

                // Skip duplicate insert if identical location & risk level within 60 seconds
                if (latest != null &&
                    latest.locationLabel.equals(targetLabel, ignoreCase = true) &&
                    latest.riskLevel.equals(dto.risk.level, ignoreCase = true) &&
                    (nowMs - latest.timestamp) < 60000L
                ) {
                    return
                }

                val log = HeatRiskLogEntity(
                    timestamp = nowMs,
                    isoTimestamp = dto.metadata.predictionTimestamp,
                    riskLevel = dto.risk.level,
                    riskScore = dto.risk.score,
                    temperatureCelsius = dto.weather.temperature.toInt(),
                    humidityPercent = dto.weather.relativeHumidity.toInt(),
                    feelsLikeCelsius = (dto.weather.apparentTemperature ?: dto.weather.temperature).toInt(),
                    locationLabel = targetLabel
                )
                dao.insertLog(log)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getRecentRiskLogs(limit: Int = 5): Flow<List<HeatRiskLogEntity>> {
        return heatRiskLogDao?.getRecentLogs(limit) ?: flowOf(emptyList())
    }

    fun getAllRiskLogs(): Flow<List<HeatRiskLogEntity>> {
        return heatRiskLogDao?.getAllLogs() ?: flowOf(emptyList())
    }

    fun getRiskLogsSince(minTimestamp: Long): Flow<List<HeatRiskLogEntity>> {
        return heatRiskLogDao?.getLogsSince(minTimestamp) ?: flowOf(emptyList())
    }

    suspend fun deleteRiskLogById(id: Long): Result<Boolean> {
        return try {
            heatRiskLogDao?.deleteLogById(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllRiskLogs(): Result<Boolean> {
        return try {
            heatRiskLogDao?.clearAllLogs()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getRecentAlertLogs(limit: Int = 5): Flow<List<AlertLogEntity>> {
        return alertLogDao?.getRecentAlerts(limit) ?: flowOf(emptyList())
    }

    suspend fun predictCustomHeatRisk(
        temperature: Double,
        humidity: Double,
        windSpeed: Double? = null
    ): Result<PredictResponseDto> {
        val req = PredictRequestDto(
            temperatureC = temperature,
            humidityPercent = humidity,
            windSpeedKmh = windSpeed
        )

        try {
            val res = apiService.predictHeatRisk(req)
            return Result.success(res)
        } catch (e: HttpException) {
            val code = e.code()
            if (code == 400 || code == 422) {
                return Result.failure(IllegalArgumentException("Invalid prediction parameters (Temp -100..80°C, Humidity 0..100%). HTTP $code", e))
            } else {
                return Result.failure(IOException("Server error ($code) during prediction: ${e.message()}", e))
            }
        } catch (e: JsonSyntaxException) {
            return Result.failure(IllegalArgumentException("Received malformed JSON prediction response.", e))
        } catch (e: JsonParseException) {
            return Result.failure(IllegalArgumentException("Received invalid prediction response structure.", e))
        } catch (initialErr: Exception) {
            if (!enableCandidateFailover) {
                val details = initialErr.localizedMessage ?: "ML model inference error"
                return Result.failure(IOException("Failed custom ML prediction: $details", initialErr))
            }

            var lastException: Exception = initialErr
            val candidates = listOf(BackendApiConfig.baseUrl) + BackendApiConfig.getCandidateBaseUrls()

            for (candidateUrl in candidates.distinct()) {
                try {
                    val service = BackendApiConfig.createApiService(candidateUrl)
                    val res = service.predictHeatRisk(req)
                    BackendApiConfig.baseUrl = candidateUrl
                    this.apiService = service
                    return Result.success(res)
                } catch (e: Exception) {
                    lastException = e
                }
            }

            val details = lastException.localizedMessage ?: "ML model inference error"
            return Result.failure(IOException("Failed custom ML prediction: $details", lastException))
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
            lastUpdated = dto.metadata.predictionTimestamp,
            dataSource = DataSource.LIVE
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
            errorMessage = null,
            dataSource = DataSource.DEMO
        )
    }

    // Phase 8 Smart Heat Alerts Methods
    suspend fun fetchAlertHistory(limit: Int = 20, offset: Int = 0): Result<List<AlertLogEntity>> {
        try {
            val response = apiService.getAlertHistory(limit, offset)
            val entities = response.alerts.map { dto ->
                AlertLogEntity(
                    isoTimestamp = dto.isoTimestamp,
                    timestamp = dto.timestamp,
                    title = dto.title,
                    description = dto.description,
                    riskLevel = dto.riskLevel,
                    triggerSource = dto.triggerSource
                )
            }

            // Sync into Room DB if available
            alertLogDao?.let { dao ->
                entities.forEach { entity ->
                    try {
                        dao.insertAlert(entity)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            return Result.success(entities)
        } catch (initialErr: Exception) {
            // Fall back across candidate base URLs
            if (enableCandidateFailover) {
                for (candidateUrl in BackendApiConfig.getCandidateBaseUrls()) {
                    try {
                        val service = BackendApiConfig.createApiService(candidateUrl)
                        val response = service.getAlertHistory(limit, offset)
                        BackendApiConfig.baseUrl = candidateUrl
                        this.apiService = service
                        val entities = response.alerts.map { dto ->
                            AlertLogEntity(
                                isoTimestamp = dto.isoTimestamp,
                                timestamp = dto.timestamp,
                                title = dto.title,
                                description = dto.description,
                                riskLevel = dto.riskLevel,
                                triggerSource = dto.triggerSource
                            )
                        }
                        return Result.success(entities)
                    } catch (e: Exception) {
                        // ignore and try next candidate
                    }
                }
            }

            // Fallback to local Room DB if network fails
            return try {
                val localAlerts: List<AlertLogEntity> = alertLogDao?.getRecentAlerts(limit)?.firstOrNull() ?: emptyList()
                Result.success(localAlerts)
            } catch (e: Exception) {
                Result.failure(IOException("Failed to fetch alert history: ${initialErr.localizedMessage}", initialErr))
            }
        }
    }

    suspend fun logAlert(
        title: String,
        description: String,
        riskLevel: String,
        triggerSource: String = "Local Risk Monitor",
        recommendations: List<String> = emptyList()
    ): Result<AlertLogEntity> {
        val nowMs = System.currentTimeMillis()
        val nowIso = java.time.Instant.ofEpochMilli(nowMs).toString()

        val entity = AlertLogEntity(
            timestamp = nowMs,
            isoTimestamp = nowIso,
            title = title,
            description = description,
            riskLevel = riskLevel,
            triggerSource = triggerSource
        )

        // Save locally first
        alertLogDao?.insertAlert(entity)

        // Sync with backend API asynchronously
        try {
            val req = com.heathealth.app.network.dto.CreateAlertRequestDto(
                title = title,
                description = description,
                riskLevel = riskLevel,
                triggerSource = triggerSource,
                recommendations = recommendations
            )
            apiService.createAlertHistory(req)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Result.success(entity)
    }

    suspend fun clearAlertHistory(): Result<Boolean> {
        alertLogDao?.clearAllAlerts()
        try {
            apiService.clearAlertHistory()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Result.success(true)
    }

    suspend fun fetchUpcomingForecast(
        latitude: Double,
        longitude: Double,
        label: String? = null
    ): Result<com.heathealth.app.network.dto.UpcomingForecastResponseDto> {
        try {
            val res = apiService.getUpcomingForecast(latitude, longitude, label)
            return Result.success(res)
        } catch (initialErr: Exception) {
            if (enableCandidateFailover) {
                for (candidateUrl in BackendApiConfig.getCandidateBaseUrls()) {
                    try {
                        val service = BackendApiConfig.createApiService(candidateUrl)
                        val res = service.getUpcomingForecast(latitude, longitude, label)
                        BackendApiConfig.baseUrl = candidateUrl
                        this.apiService = service
                        return Result.success(res)
                    } catch (e: Exception) {
                        // try next
                    }
                }
            }
            return Result.failure(IOException("Failed to fetch upcoming forecast: ${initialErr.localizedMessage}", initialErr))
        }
    }

    suspend fun evaluateAlert(
        temperature: Double,
        humidity: Double,
        userThreshold: String,
        lastAlertedRiskLevel: String
    ): Result<com.heathealth.app.network.dto.AlertEvaluationResponseDto> {
        try {
            val req = com.heathealth.app.network.dto.AlertEvaluationRequestDto(
                temperatureC = temperature,
                humidityPercent = humidity,
                userThreshold = userThreshold,
                lastAlertedRiskLevel = lastAlertedRiskLevel
            )
            val res = apiService.evaluateAlert(req)
            return Result.success(res)
        } catch (e: Exception) {
            return Result.failure(IOException("Alert evaluation failed: ${e.localizedMessage}", e))
        }
    }

    suspend fun fetchRemoteAlertPreferences(): Result<com.heathealth.app.network.dto.AlertPreferencesDto> {
        try {
            val res = apiService.getAlertPreferences()
            return Result.success(res)
        } catch (e: Exception) {
            return Result.failure(IOException("Failed to fetch alert preferences: ${e.localizedMessage}", e))
        }
    }

    suspend fun updateRemoteAlertPreferences(
        prefs: com.heathealth.app.network.dto.AlertPreferencesDto
    ): Result<com.heathealth.app.network.dto.AlertPreferencesDto> {
        try {
            val res = apiService.updateAlertPreferences(prefs)
            return Result.success(res)
        } catch (e: Exception) {
            return Result.failure(IOException("Failed to update alert preferences: ${e.localizedMessage}", e))
        }
    }

    suspend fun registerDeviceToken(token: String): Result<Boolean> {
        try {
            val req = com.heathealth.app.network.dto.DeviceTokenRequestDto(deviceToken = token)
            apiService.registerDeviceToken(req)
            return Result.success(true)
        } catch (e: Exception) {
            return Result.failure(IOException("Failed to register device token: ${e.localizedMessage}", e))
        }
    }
}
