package com.heathealth.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heathealth.app.data.local.HeatHealthDatabase
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.data.local.entity.WaterIntakeEntity
import com.heathealth.app.data.model.WaterTrackerUiState
import com.heathealth.app.data.repository.StudentProfileRepository
import com.heathealth.app.data.repository.WaterIntakeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WaterTrackerViewModel(
    application: Application,
    private val waterRepository: WaterIntakeRepository,
    private val profileRepository: StudentProfileRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        WaterIntakeRepository(HeatHealthDatabase.getDatabase(application).waterIntakeDao()),
        StudentProfileRepository(
            HeatHealthDatabase.getDatabase(application).studentProfileDao(),
            UserPreferencesRepository(application)
        )
    )

    private val _uiState = MutableStateFlow(WaterTrackerUiState(isLoading = true))
    val uiState: StateFlow<WaterTrackerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                waterRepository.getTodayWaterEntries(),
                waterRepository.getTodayTotalMl(),
                profileRepository.profileFlow
            ) { entries, total, profile ->
                _uiState.value.copy(
                    todayEntries = entries,
                    todayTotalMl = total,
                    targetMl = profile.hydrationTargetMl,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun addQuickWater(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            waterRepository.addWaterIntake(amountMl = amountMl, note = "Quick add ${amountMl}ml")
            _uiState.value = _uiState.value.copy(
                userFeedbackMessage = "Added ${amountMl} mL of water.",
                customInputError = null
            )
        }
    }

    fun onCustomAmountChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(
            customAmountInput = filtered,
            customInputError = null
        )
    }

    fun addCustomWater() {
        val input = _uiState.value.customAmountInput
        val amount = input.toIntOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = _uiState.value.copy(customInputError = "Please enter a valid amount > 0 mL.")
            return
        }
        if (amount > 3000) {
            _uiState.value = _uiState.value.copy(customInputError = "Single entry cannot exceed 3000 mL.")
            return
        }

        viewModelScope.launch {
            waterRepository.addWaterIntake(amountMl = amount, note = "Custom log")
            _uiState.value = _uiState.value.copy(
                customAmountInput = "",
                customInputError = null,
                userFeedbackMessage = "Added ${amount} mL of water."
            )
        }
    }

    fun deleteEntry(entry: WaterIntakeEntity) {
        viewModelScope.launch {
            waterRepository.deleteWaterIntake(entry)
            _uiState.value = _uiState.value.copy(
                lastDeletedEntry = entry,
                userFeedbackMessage = "Deleted entry: ${entry.amountMl} mL."
            )
        }
    }

    fun undoDelete() {
        val entry = _uiState.value.lastDeletedEntry ?: return
        viewModelScope.launch {
            waterRepository.addWaterIntake(
                amountMl = entry.amountMl,
                note = entry.note,
                timestamp = entry.timestamp
            )
            _uiState.value = _uiState.value.copy(
                lastDeletedEntry = null,
                userFeedbackMessage = "Restored entry: ${entry.amountMl} mL."
            )
        }
    }

    fun clearFeedbackMessage() {
        _uiState.value = _uiState.value.copy(userFeedbackMessage = null)
    }
}
