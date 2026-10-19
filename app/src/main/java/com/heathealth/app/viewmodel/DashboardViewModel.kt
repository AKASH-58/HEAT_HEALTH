package com.heathealth.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heathealth.app.data.model.DashboardUiState
import com.heathealth.app.data.repository.HeatHealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: HeatHealthRepository = HeatHealthRepository(),
    autoLoad: Boolean = true
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = autoLoad))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

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
