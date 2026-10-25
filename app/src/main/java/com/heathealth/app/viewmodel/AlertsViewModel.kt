package com.heathealth.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heathealth.app.data.local.HeatHealthDatabase
import com.heathealth.app.data.local.datastore.UserPreferences
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.data.local.entity.AlertLogEntity
import com.heathealth.app.data.model.DashboardUiState
import com.heathealth.app.data.model.DataSource
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.network.dto.UpcomingForecastItemDto
import com.heathealth.app.util.NotificationHelper
import com.heathealth.app.worker.WorkManagerScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UpcomingForecastUiState(
    val isLoading: Boolean = false,
    val isAvailable: Boolean = false,
    val locationLabel: String = "",
    val items: List<UpcomingForecastItemDto> = emptyList(),
    val errorMessage: String? = null
)

class AlertsViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: HeatHealthRepository = run {
        try {
            val db = HeatHealthDatabase.getDatabase(application)
            HeatHealthRepository(
                heatRiskLogDao = db.heatRiskLogDao(),
                alertLogDao = db.alertLogDao()
            )
        } catch (e: Throwable) {
            HeatHealthRepository()
        }
    },
    autoLoad: Boolean = true
) : AndroidViewModel(application) {

    constructor(repository: HeatHealthRepository, autoLoad: Boolean = true) : this(
        application = Application(),
        repository = repository,
        autoLoad = autoLoad
    )

    private val prefsRepo = UserPreferencesRepository(application)

    val userPreferences: StateFlow<UserPreferences> = prefsRepo.userPreferencesFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferences()
        )

    private val _alertsUiState = MutableStateFlow(DashboardUiState(isLoading = autoLoad))
    val alertsUiState: StateFlow<DashboardUiState> = _alertsUiState.asStateFlow()

    private val _upcomingForecastState = MutableStateFlow(UpcomingForecastUiState(isLoading = autoLoad))
    val upcomingForecastState: StateFlow<UpcomingForecastUiState> = _upcomingForecastState.asStateFlow()

    private val _alertHistory = MutableStateFlow<List<AlertLogEntity>>(emptyList())
    val alertHistory: StateFlow<List<AlertLogEntity>> = _alertHistory.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        if (autoLoad) {
            loadAllData()
        }
    }

    fun loadAllData(
        latitude: Double = _alertsUiState.value.latitude,
        longitude: Double = _alertsUiState.value.longitude,
        label: String = _alertsUiState.value.locationLabel,
        isRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _alertsUiState.value = _alertsUiState.value.copy(isLoading = true, errorMessage = null)
                _upcomingForecastState.value = _upcomingForecastState.value.copy(isLoading = true, errorMessage = null)
            }

            // 1. Fetch current live risk data
            val riskResult = repository.fetchHeatRiskData(latitude, longitude, label)
            riskResult.fold(
                onSuccess = { state ->
                    _alertsUiState.value = state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        dataSource = DataSource.LIVE,
                        errorMessage = null
                    )
                },
                onFailure = { err ->
                    _alertsUiState.value = _alertsUiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        dataSource = DataSource.UNAVAILABLE,
                        errorMessage = err.localizedMessage ?: "Unable to fetch live heat risk prediction."
                    )
                }
            )

            // 2. Fetch upcoming hourly forecast data
            val forecastResult = repository.fetchUpcomingForecast(latitude, longitude, label)
            forecastResult.fold(
                onSuccess = { res ->
                    _upcomingForecastState.value = UpcomingForecastUiState(
                        isLoading = false,
                        isAvailable = res.forecastAvailable,
                        locationLabel = res.locationLabel,
                        items = res.items,
                        errorMessage = if (!res.forecastAvailable) "Forecast data currently unavailable from provider." else null
                    )
                },
                onFailure = { err ->
                    _upcomingForecastState.value = UpcomingForecastUiState(
                        isLoading = false,
                        isAvailable = false,
                        locationLabel = label,
                        items = emptyList(),
                        errorMessage = err.localizedMessage ?: "Failed to retrieve upcoming forecast."
                    )
                }
            )

            // 3. Fetch alert history from Room DB + Backend API
            val historyResult = repository.fetchAlertHistory(limit = 20, offset = 0)
            historyResult.fold(
                onSuccess = { logs ->
                    _alertHistory.value = logs
                },
                onFailure = {
                    // Keep existing local list if available
                }
            )

            _isRefreshing.value = false
        }
    }

    fun refreshAlerts() {
        loadAllData(isRefresh = true)
    }

    fun updateHeatAlertsEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.updateHeatAlertsEnabled(enabled)
            if (enabled) {
                WorkManagerScheduler.scheduleHeatAlertChecks(context)
            } else {
                WorkManagerScheduler.cancelHeatAlertChecks(context)
            }

            repository.updateRemoteAlertPreferences(
                com.heathealth.app.network.dto.AlertPreferencesDto(
                    heatAlertsEnabled = enabled,
                    notificationsEnabled = userPreferences.value.notificationsEnabled,
                    alertThreshold = userPreferences.value.alertThreshold,
                    reminderFrequencyHours = userPreferences.value.reminderFrequencyHours
                )
            )
        }
    }

    fun updateAlertThreshold(threshold: String) {
        viewModelScope.launch {
            prefsRepo.updateAlertThreshold(threshold)
            repository.updateRemoteAlertPreferences(
                com.heathealth.app.network.dto.AlertPreferencesDto(
                    heatAlertsEnabled = userPreferences.value.heatAlertsEnabled,
                    notificationsEnabled = userPreferences.value.notificationsEnabled,
                    alertThreshold = threshold,
                    reminderFrequencyHours = userPreferences.value.reminderFrequencyHours
                )
            )
        }
    }

    fun updateReminderFrequencyHours(hours: Int) {
        viewModelScope.launch {
            prefsRepo.updateReminderFrequencyHours(hours)
        }
    }

    fun clearAlertHistory() {
        viewModelScope.launch {
            repository.clearAlertHistory()
            _alertHistory.value = emptyList()
        }
    }

    fun triggerLocalTestNotification(context: Context) {
        viewModelScope.launch {
            val prefs = prefsRepo.userPreferencesFlow.first()
            val currentRisk = _alertsUiState.value.riskPrediction.level
            val currentTemp = _alertsUiState.value.weatherData.feelsLikeCelsius ?: _alertsUiState.value.weatherData.temperatureCelsius ?: 32

            val title = "${currentRisk.displayName} Heat Risk Advisory"
            val message = "Current thermal risk is ${currentRisk.displayName} (${currentTemp}°C feels-like). Take shade and stay hydrated."

            if (NotificationHelper.hasNotificationPermission(context)) {
                NotificationHelper.sendHeatAlertNotification(
                    context = context,
                    title = title,
                    message = message
                )

                val logged = repository.logAlert(
                    title = title,
                    description = message,
                    riskLevel = currentRisk.displayName,
                    triggerSource = "User Triggered Test Alert",
                    recommendations = _alertsUiState.value.recommendations.map { it.title }
                )

                logged.getOrNull()?.let { newAlert ->
                    _alertHistory.value = listOf(newAlert) + _alertHistory.value
                }
            }
        }
    }
}
