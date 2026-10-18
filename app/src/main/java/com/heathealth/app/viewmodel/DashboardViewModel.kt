package com.heathealth.app.viewmodel

import androidx.lifecycle.ViewModel
import com.heathealth.app.data.model.DashboardUiState
import com.heathealth.app.data.repository.HeatHealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel(
    private val repository: HeatHealthRepository = HeatHealthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(repository.getDashboardSampleData())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun refreshData() {
        _uiState.value = repository.getDashboardSampleData()
    }
}
