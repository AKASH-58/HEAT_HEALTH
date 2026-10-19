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
                    _uiState.value = state.copy(isLoading = false, isRefreshing = false)
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
}
