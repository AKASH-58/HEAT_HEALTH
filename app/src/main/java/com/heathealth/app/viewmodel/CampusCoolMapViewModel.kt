package com.heathealth.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heathealth.app.data.local.HeatHealthDatabase
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.data.model.CampusCoolMapUiState
import com.heathealth.app.data.model.CampusFacility
import com.heathealth.app.data.model.College
import com.heathealth.app.data.model.DataSource
import com.heathealth.app.data.model.FacilityCategory
import com.heathealth.app.data.repository.CampusCoolMapRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CampusCoolMapViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: CampusCoolMapRepository = run {
        try {
            val db = HeatHealthDatabase.getDatabase(application)
            CampusCoolMapRepository(
                campusFacilityDao = db.campusFacilityDao(),
                collegeDao = db.collegeDao()
            )
        } catch (e: Throwable) {
            CampusCoolMapRepository()
        }
    },
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(application),
    autoLoad: Boolean = true
) : AndroidViewModel(application) {

    constructor(repository: CampusCoolMapRepository, autoLoad: Boolean = true) : this(
        application = Application(),
        repository = repository,
        preferencesRepository = UserPreferencesRepository(null),
        autoLoad = autoLoad
    )

    private val _uiState = MutableStateFlow(CampusCoolMapUiState(isLoading = autoLoad))
    val uiState: StateFlow<CampusCoolMapUiState> = _uiState.asStateFlow()

    val cachedFacilities: StateFlow<List<CampusFacility>> = repository.getCachedFacilitiesByCollege("col_iisc")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (autoLoad) {
            loadCollegesAndFacilities()
            startRealtimeSyncLoop()
        }
    }

    private fun loadCollegesAndFacilities() {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val colResult = repository.fetchColleges()
            val colleges = colResult.getOrDefault(repository.getDemoColleges())
            val savedColId = prefs.selectedCollegeId
            val activeCollege = colleges.find { it.id == savedColId } ?: colleges.firstOrNull() ?: repository.getDemoColleges().first()

            _uiState.value = _uiState.value.copy(
                selectedCollege = activeCollege,
                collegesList = colleges,
                isLoading = true
            )

            loadFacilities()
        }
    }

    fun loadFacilities(isRefresh: Boolean = false) {
        if (!isRefresh && _uiState.value.isLoading && _uiState.value.isRefreshing) return

        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }

            val collegeId = _uiState.value.selectedCollege.id
            val categoryParam = _uiState.value.selectedCategory?.displayName
            val result = repository.fetchCampusFacilities(collegeId, categoryParam)

            result.fold(
                onSuccess = { (facilities, lastUpdated) ->
                    _uiState.value = _uiState.value.copy(
                        facilities = facilities,
                        isLoading = false,
                        isRefreshing = false,
                        dataSource = DataSource.LIVE,
                        errorMessage = null,
                        lastUpdated = lastUpdated
                    )
                },
                onFailure = { error ->
                    val currentState = _uiState.value
                    val cached = cachedFacilities.value
                    val hasData = cached.isNotEmpty() || currentState.facilities.isNotEmpty()

                    if (hasData) {
                        _uiState.value = currentState.copy(
                            facilities = if (currentState.facilities.isNotEmpty()) currentState.facilities else cached,
                            isLoading = false,
                            isRefreshing = false,
                            dataSource = DataSource.CACHED,
                            errorMessage = error.localizedMessage ?: "Unable to connect to campus server."
                        )
                    } else {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            isRefreshing = false,
                            dataSource = DataSource.UNAVAILABLE,
                            errorMessage = error.localizedMessage ?: "Unable to connect to campus server."
                        )
                    }
                }
            )
        }
    }

    private fun startRealtimeSyncLoop() {
        viewModelScope.launch {
            while (true) {
                delay(6000)
                if (_uiState.value.dataSource == DataSource.LIVE) {
                    val collegeId = _uiState.value.selectedCollege.id
                    val categoryParam = _uiState.value.selectedCategory?.displayName
                    val result = repository.fetchCampusFacilities(collegeId, categoryParam)
                    result.getOrNull()?.let { (freshFacilities, lastUpdated) ->
                        _uiState.value = _uiState.value.copy(
                            facilities = freshFacilities,
                            lastUpdated = lastUpdated
                        )
                    }
                }
            }
        }
    }

    fun selectCollege(college: College) {
        viewModelScope.launch {
            preferencesRepository.updateSelectedCollegeId(college.id)
            _uiState.value = _uiState.value.copy(
                selectedCollege = college,
                isCollegeSearchOpen = false,
                selectedFacility = null,
                isLoading = true
            )
            loadFacilities()
        }
    }

    fun searchColleges(query: String) {
        _uiState.value = _uiState.value.copy(collegeSearchQuery = query)
        viewModelScope.launch {
            val result = repository.fetchColleges(query = query)
            val matches = result.getOrDefault(repository.getDemoColleges())
            _uiState.value = _uiState.value.copy(collegesList = matches)
        }
    }

    fun openCollegeSearchDialog() {
        _uiState.value = _uiState.value.copy(isCollegeSearchOpen = true)
        searchColleges(_uiState.value.collegeSearchQuery)
    }

    fun closeCollegeSearchDialog() {
        _uiState.value = _uiState.value.copy(isCollegeSearchOpen = false)
    }

    fun openAddFacilityDialog() {
        _uiState.value = _uiState.value.copy(isAddFacilityDialogOpen = true)
    }

    fun closeAddFacilityDialog() {
        _uiState.value = _uiState.value.copy(isAddFacilityDialogOpen = false)
    }

    fun openReportStatusDialog() {
        _uiState.value = _uiState.value.copy(isReportStatusDialogOpen = true)
    }

    fun closeReportStatusDialog() {
        _uiState.value = _uiState.value.copy(isReportStatusDialogOpen = false)
    }

    fun submitNewFacility(
        name: String,
        category: String,
        description: String,
        latitude: Double?,
        longitude: Double?,
        buildingLocation: String,
        openingHours: String?,
        accessibilityInfo: String?,
        contactInfo: String?
    ) {
        viewModelScope.launch {
            val collegeId = _uiState.value.selectedCollege.id
            val result = repository.addFacility(
                collegeId = collegeId,
                name = name,
                category = category,
                description = description,
                latitude = latitude ?: _uiState.value.selectedCollege.latitude,
                longitude = longitude ?: _uiState.value.selectedCollege.longitude,
                buildingLocation = buildingLocation,
                openingHours = openingHours,
                accessibilityInfo = accessibilityInfo,
                contactInfo = contactInfo
            )
            result.getOrNull()?.let { newFac ->
                val updatedList = listOf(newFac) + _uiState.value.facilities
                _uiState.value = _uiState.value.copy(
                    facilities = updatedList,
                    selectedFacility = newFac,
                    isAddFacilityDialogOpen = false
                )
            }
        }
    }

    fun reportFacilityStatus(facilityId: String, newStatus: String, statusNote: String?) {
        viewModelScope.launch {
            val result = repository.updateFacilityStatus(facilityId, newStatus, statusNote)
            result.getOrNull()?.let { updatedFac ->
                val updatedList = _uiState.value.facilities.map { if (it.id == updatedFac.id) updatedFac else it }
                _uiState.value = _uiState.value.copy(
                    facilities = updatedList,
                    selectedFacility = updatedFac,
                    isReportStatusDialogOpen = false
                )
            }
        }
    }

    fun refreshFacilities() {
        loadFacilities(isRefresh = true)
    }

    fun selectCategory(category: FacilityCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadFacilities(isRefresh = false)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectFacility(facility: CampusFacility?) {
        _uiState.value = _uiState.value.copy(selectedFacility = facility)
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isMapView = !_uiState.value.isMapView)
    }

    fun loadSampleFacilities() {
        val sample = repository.getDemoSampleFacilities()
        _uiState.value = _uiState.value.copy(
            facilities = sample,
            isLoading = false,
            isRefreshing = false,
            dataSource = DataSource.DEMO,
            errorMessage = null,
            lastUpdated = "Demo Dataset"
        )
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            searchQuery = ""
        )
        loadFacilities(isRefresh = false)
    }
}
