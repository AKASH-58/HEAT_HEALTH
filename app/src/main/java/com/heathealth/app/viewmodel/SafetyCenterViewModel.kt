package com.heathealth.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heathealth.app.data.local.HeatHealthDatabase
import com.heathealth.app.data.local.dao.CollegeDao
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.data.local.entity.CollegeEntity
import com.heathealth.app.data.model.DashboardUiState
import com.heathealth.app.data.model.PersonalizedGuidance
import com.heathealth.app.data.model.PersonalizedGuidanceEngine
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.repository.EmergencyContactsData
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.data.repository.SafetyCenterRepository
import com.heathealth.app.data.repository.SafetyChecklistState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SafetyChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)

data class SafetyCenterUiState(
    val liveUiState: DashboardUiState = DashboardUiState(),
    val isLiveRiskAvailable: Boolean = false,
    val selectedProfile: String = "Campus Commuter",
    val guidance: PersonalizedGuidance = PersonalizedGuidanceEngine.generateOfflineGuidance("Campus Commuter"),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SafetyCenterViewModel @JvmOverloads constructor(
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
    private val safetyRepo: SafetyCenterRepository = SafetyCenterRepository(application),
    private val userPrefsRepo: UserPreferencesRepository? = try {
        UserPreferencesRepository(application)
    } catch (e: Throwable) { null },
    private val collegeDao: CollegeDao? = try {
        HeatHealthDatabase.getDatabase(application).collegeDao()
    } catch (e: Throwable) { null },
    autoLoad: Boolean = true
) : AndroidViewModel(application) {

    constructor(
        repository: HeatHealthRepository,
        safetyRepo: SafetyCenterRepository,
        autoLoad: Boolean = true
    ) : this(
        application = Application(),
        repository = repository,
        safetyRepo = safetyRepo,
        userPrefsRepo = null,
        collegeDao = null,
        autoLoad = autoLoad
    )

    private val _uiState = MutableStateFlow(SafetyCenterUiState(isLoading = autoLoad))
    val uiState: StateFlow<SafetyCenterUiState> = _uiState.asStateFlow()

    val emergencyContacts: StateFlow<EmergencyContactsData> = safetyRepo.emergencyContactsFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            EmergencyContactsData()
        )

    val checklistState: StateFlow<SafetyChecklistState> = safetyRepo.checklistStateFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SafetyChecklistState()
        )

    val checklistItems: StateFlow<List<SafetyChecklistItem>> = checklistState.map { state ->
        val defaultList = listOf(
            SafetyChecklistItem("item_water", "Carry Clean Drinking Water", "Keep a refillable 1-2L bottle or electrolyte solution accessible across campus."),
            SafetyChecklistItem("item_rest", "Take Shaded Rest Breaks", "Pause in shaded, air-conditioned, or ventilated spaces between outdoor classes."),
            SafetyChecklistItem("item_shade", "Identify Cool Campus Places", "Locate campus library reading halls, chilled water kiosks, or canopy seating."),
            SafetyChecklistItem("item_activity", "Avoid Midday Outdoor Exertion", "Minimize strenuous outdoor sports, running, or fieldwork during 11 AM - 4 PM heat."),
            SafetyChecklistItem("item_risk", "Check Live Heat Predictions", "Review current environmental thermal risk levels before long campus travel."),
            SafetyChecklistItem("item_clothing", "Wear Light Breathable Attire", "Dress in light-colored, loose cotton garments with sun protection hats/umbrellas.")
        )

        defaultList.map { item ->
            item.copy(isCompleted = state.itemsCompletedMap[item.id] ?: false)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        if (autoLoad) {
            loadData()
        }
    }

    fun loadData(
        latitude: Double? = null,
        longitude: Double? = null,
        label: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Resolve target campus coordinates from location parameters or selected College in Room/DataStore
            var targetLat = latitude
            var targetLng = longitude
            var targetLabel = label

            if (targetLat == null || targetLng == null) {
                userPrefsRepo?.let { prefsRepo ->
                    val userPrefs = prefsRepo.userPreferencesFlow.first()
                    val collegeId = userPrefs.selectedCollegeId
                    collegeDao?.getCollegeById(collegeId)?.let { college ->
                        targetLat = college.latitude
                        targetLng = college.longitude
                        targetLabel = "${college.name}, ${college.city}"
                    }
                }
            }

            val resLat = targetLat
            val resLng = targetLng

            if (resLat == null || resLng == null) {
                _uiState.value = _uiState.value.copy(
                    isLiveRiskAvailable = false,
                    isLoading = false,
                    errorMessage = "Campus location not configured. Displaying general offline safety guidance.",
                    guidance = PersonalizedGuidanceEngine.generateOfflineGuidance(_uiState.value.selectedProfile)
                )
                return@launch
            }

            val result = repository.fetchHeatRiskData(resLat, resLng, targetLabel ?: "Selected Campus")
            result.fold(
                onSuccess = { liveState ->
                    val guidance = PersonalizedGuidanceEngine.generateGuidance(
                        riskLevel = liveState.riskPrediction.level,
                        temperatureC = liveState.weatherData.feelsLikeCelsius ?: liveState.weatherData.temperatureCelsius ?: 32,
                        humidityPercent = liveState.weatherData.humidityPercent ?: 50,
                        activityPattern = _uiState.value.selectedProfile
                    )
                    _uiState.value = _uiState.value.copy(
                        liveUiState = liveState,
                        isLiveRiskAvailable = true,
                        guidance = guidance,
                        isLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLiveRiskAvailable = false,
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Live prediction unavailable. Showing general safety protocols.",
                        guidance = PersonalizedGuidanceEngine.generateOfflineGuidance(_uiState.value.selectedProfile)
                    )
                }
            )
        }
    }

    fun selectProfile(profile: String) {
        val updatedGuidance = if (_uiState.value.isLiveRiskAvailable) {
            PersonalizedGuidanceEngine.generateGuidance(
                riskLevel = _uiState.value.liveUiState.riskPrediction.level,
                temperatureC = _uiState.value.liveUiState.weatherData.feelsLikeCelsius ?: 32,
                humidityPercent = _uiState.value.liveUiState.weatherData.humidityPercent ?: 50,
                activityPattern = profile
            )
        } else {
            PersonalizedGuidanceEngine.generateOfflineGuidance(profile)
        }

        _uiState.value = _uiState.value.copy(
            selectedProfile = profile,
            guidance = updatedGuidance
        )
    }

    fun toggleChecklistItem(itemId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val todayStr = try { LocalDate.now().toString() } catch (e: Exception) { "" }
            safetyRepo.updateChecklistItem(itemId, isCompleted, todayStr)
        }
    }

    fun saveEmergencyContacts(
        campusName: String,
        campusPhone: String,
        personalName: String,
        personalPhone: String
    ): Result<Boolean> {
        val trimmedCampus = campusPhone.trim()
        val trimmedPersonal = personalPhone.trim()

        if (trimmedCampus.isNotEmpty() && !SafetyCenterRepository.isValidPhoneNumber(trimmedCampus)) {
            return Result.failure(IllegalArgumentException("Invalid campus health center phone number format."))
        }
        if (trimmedPersonal.isNotEmpty() && !SafetyCenterRepository.isValidPhoneNumber(trimmedPersonal)) {
            return Result.failure(IllegalArgumentException("Invalid personal emergency contact phone number format."))
        }

        viewModelScope.launch {
            safetyRepo.saveEmergencyContacts(campusName, campusPhone, personalName, personalPhone)
        }
        return Result.success(true)
    }

    fun evaluateDateReset(todayDateStr: String) {
        viewModelScope.launch {
            val current = checklistState.value
            if (current.lastResetDate.isNotEmpty() && current.lastResetDate != todayDateStr) {
                safetyRepo.resetChecklistForNewDay(todayDateStr)
            }
        }
    }
}
