package com.heathealth.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heathealth.app.data.local.HeatHealthDatabase
import com.heathealth.app.data.local.entity.AlertLogEntity
import com.heathealth.app.data.local.entity.HeatRiskLogEntity
import com.heathealth.app.data.model.DashboardUiState
import com.heathealth.app.data.model.PersonalizedGuidance
import com.heathealth.app.data.model.PersonalizedGuidanceEngine
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.data.repository.WaterIntakeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel @JvmOverloads constructor(
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

    private val waterRepository: WaterIntakeRepository? = try {
        val db = HeatHealthDatabase.getDatabase(application)
        WaterIntakeRepository(db.waterIntakeDao())
    } catch (e: Throwable) {
        null
    }

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = autoLoad))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val recentRiskLogs: StateFlow<List<HeatRiskLogEntity>> = repository.getRecentRiskLogs(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAlertLogs: StateFlow<List<AlertLogEntity>> = repository.getRecentAlertLogs(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWaterIntakeMl: StateFlow<Int> = (waterRepository?.getTodayTotalMl() ?: flowOf(0))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weeklyWaterTotalMl: StateFlow<Int> = (waterRepository?.getAllWaterIntakeHistory() ?: flowOf(emptyList()))
        .map { logs -> logs.sumOf { it.amountMl } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val personalizedGuidance: StateFlow<PersonalizedGuidance> = _uiState
        .map { state ->
            PersonalizedGuidanceEngine.generateGuidance(
                riskLevel = state.riskPrediction.level,
                temperatureC = state.weatherData.temperatureCelsius ?: 30,
                humidityPercent = state.weatherData.humidityPercent ?: 50
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PersonalizedGuidanceEngine.generateGuidance(_uiState.value.riskPrediction.level)
        )

    init {
        if (autoLoad) {
            loadData()
        }
    }

    fun loadData(
        latitude: Double = _uiState.value.latitude,
        longitude: Double = _uiState.value.longitude,
        label: String = _uiState.value.locationLabel,
        isRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }

            val result = repository.fetchHeatRiskData(latitude, longitude, label)
            result.fold(
                onSuccess = { state ->
                    _uiState.value = state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        customPrediction = _uiState.value.customPrediction
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.localizedMessage ?: "Failed to connect to backend server."
                    )
                }
            )
        }
    }

    fun refreshData() {
        loadData(isRefresh = true)
    }

    fun updateCoordinates(latitude: Double, longitude: Double, label: String) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            locationLabel = label
        )
        loadData(latitude, longitude, label)
    }

    fun loadSampleData() {
        _uiState.value = repository.getDashboardSampleData()
    }

    fun updateCustomInputs(temp: Double, humidity: Double, windSpeed: Double) {
        _uiState.value = _uiState.value.copy(
            customPrediction = _uiState.value.customPrediction.copy(
                temperatureC = temp,
                humidityPercent = humidity,
                windSpeedKmh = windSpeed
            )
        )
    }

    fun runCustomMlPrediction() {
        val current = _uiState.value.customPrediction
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                customPrediction = current.copy(isPredicting = true, errorMessage = null)
            )

            val result = repository.predictCustomHeatRisk(
                temperature = current.temperatureC,
                humidity = current.humidityPercent,
                windSpeed = current.windSpeedKmh
            )

            result.fold(
                onSuccess = { res ->
                    _uiState.value = _uiState.value.copy(
                        customPrediction = _uiState.value.customPrediction.copy(
                            isPredicting = false,
                            predictionResult = res,
                            errorMessage = null
                        )
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        customPrediction = _uiState.value.customPrediction.copy(
                            isPredicting = false,
                            errorMessage = err.localizedMessage ?: "Failed to run custom ML prediction."
                        )
                    )
                }
            )
        }
    }
}
