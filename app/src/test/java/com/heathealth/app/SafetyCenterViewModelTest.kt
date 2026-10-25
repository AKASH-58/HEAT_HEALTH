package com.heathealth.app

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.heathealth.app.data.model.PersonalizedGuidanceEngine
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.repository.EmergencyContactsData
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.data.repository.SafetyCenterRepository
import com.heathealth.app.viewmodel.SafetyCenterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SafetyCenterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testOfflineSafetyGuidanceAvailableWithoutNetwork() {
        val guidanceLow = PersonalizedGuidanceEngine.generateGuidance(RiskLevel.LOW, temperatureC = 24)
        val guidanceExtreme = PersonalizedGuidanceEngine.generateGuidance(RiskLevel.EXTREME, temperatureC = 42)
        val guidanceOffline = PersonalizedGuidanceEngine.generateOfflineGuidance("Campus Commuter")

        assertNotNull(guidanceLow.overviewMessage)
        assertNotNull(guidanceExtreme.overviewMessage)
        assertNotNull(guidanceOffline.overviewMessage)

        assertEquals(RiskLevel.UNKNOWN, guidanceOffline.riskLevel)
        assertTrue("Offline overview must state that live data is unavailable", guidanceOffline.overviewMessage.contains("unavailable", ignoreCase = true) || guidanceOffline.overviewMessage.contains("offline", ignoreCase = true))
    }

    @Test
    fun testOfflineProfileSwitchingDoesNotFabricateLiveRisk() {
        val offlineGuidanceCommuter = PersonalizedGuidanceEngine.generateOfflineGuidance("Campus Commuter")
        val offlineGuidanceAthlete = PersonalizedGuidanceEngine.generateOfflineGuidance("Student Athlete")

        assertEquals(RiskLevel.UNKNOWN, offlineGuidanceCommuter.riskLevel)
        assertEquals(RiskLevel.UNKNOWN, offlineGuidanceAthlete.riskLevel)

        assertTrue(offlineGuidanceAthlete.overviewMessage.contains("Student Athlete"))
        assertFalse("Offline profile switching must NOT claim EXTREME live risk", offlineGuidanceAthlete.overviewMessage.contains("Extreme thermal strain danger"))
    }

    @Test
    fun testPhoneNumberValidationForEmergencyContacts() {
        assertTrue("Standard Indian mobile format should be valid", SafetyCenterRepository.isValidPhoneNumber("+91 98765 43210"))
        assertTrue("Short helpline 112 should be valid", SafetyCenterRepository.isValidPhoneNumber("112"))
        assertTrue("Helpline 108 should be valid", SafetyCenterRepository.isValidPhoneNumber("108"))
        assertTrue("Standard international format should be valid", SafetyCenterRepository.isValidPhoneNumber("+1 (555) 234-5678"))

        assertFalse("Empty string must be invalid", SafetyCenterRepository.isValidPhoneNumber(""))
        assertFalse("Too short phone number (<3 digits) must be invalid", SafetyCenterRepository.isValidPhoneNumber("12"))
        assertFalse("Alphabetic strings must be invalid", SafetyCenterRepository.isValidPhoneNumber("CALL_DOCTOR"))
    }

    @Test
    fun testBlankDefaultEmergencyContacts() {
        val defaultContacts = EmergencyContactsData()

        assertEquals("", defaultContacts.campusHealthName)
        assertEquals("", defaultContacts.campusHealthPhone)
        assertEquals("", defaultContacts.personalContactName)
        assertEquals("", defaultContacts.personalContactPhone)
        assertEquals("112", defaultContacts.publicHelplinePhone)
    }

    @Test
    fun testRealDataStoreRepositoryDailyChecklistAtomicResetAndPersistence() = runTest {
        val tempFile = File.createTempFile("test_safety_datastore", ".preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            produceFile = { tempFile }
        )

        var currentDateInTest = "2025-05-14"

        // Instantiate real DataStore-backed repository with controllable date provider
        val realRepo = SafetyCenterRepository(
            context = null,
            customDataStore = testDataStore,
            todayDateProvider = { currentDateInTest }
        )

        // 1. Complete multiple items on yesterday's date
        val yesterdayDate = "2025-05-14"
        realRepo.updateChecklistItem("item_water", true, yesterdayDate)
        realRepo.updateChecklistItem("item_shade", true, yesterdayDate)

        val yesterdayState = realRepo.checklistStateFlow.first()
        assertEquals("2025-05-14", yesterdayState.lastResetDate)
        assertEquals(true, yesterdayState.itemsCompletedMap["item_water"])
        assertEquals(true, yesterdayState.itemsCompletedMap["item_shade"])

        // 2. Simulate date changing to today: toggle item_rest on 2025-05-15
        val todayDate = "2025-05-15"
        currentDateInTest = todayDate
        realRepo.updateChecklistItem("item_rest", true, todayDate)

        // 3. Collect checklistStateFlow and verify reset behavior in real DataStore
        val todayState = realRepo.checklistStateFlow.first()
        assertEquals("2025-05-15", todayState.lastResetDate)
        assertEquals(true, todayState.itemsCompletedMap["item_rest"])
        assertEquals(false, todayState.itemsCompletedMap["item_water"]) // ATOMICALLY RESET
        assertEquals(false, todayState.itemsCompletedMap["item_shade"]) // ATOMICALLY RESET

        // 4. Recreate repository using the SAME test DataStore to verify state remains persisted
        val recreatedRepo = SafetyCenterRepository(
            context = null,
            customDataStore = testDataStore,
            todayDateProvider = { "2025-05-15" }
        )
        val recreatedState = recreatedRepo.checklistStateFlow.first()
        assertEquals("2025-05-15", recreatedState.lastResetDate)
        assertEquals(true, recreatedState.itemsCompletedMap["item_rest"])
        assertEquals(false, recreatedState.itemsCompletedMap["item_water"])

        tempFile.delete()
    }

    @Test
    fun testInitialChecklistStateWhenNoPreviousResetDateExistsInRealDataStore() = runTest {
        val freshTempFile = File.createTempFile("test_safety_fresh", ".preferences_pb")
        val freshDataStore = PreferenceDataStoreFactory.create(
            produceFile = { freshTempFile }
        )

        val freshRepo = SafetyCenterRepository(
            context = null,
            customDataStore = freshDataStore,
            todayDateProvider = { "2025-05-15" }
        )
        val todayDate = "2025-05-15"

        // First update with empty saved date
        freshRepo.updateChecklistItem("item_clothing", true, todayDate)
        val freshState = freshRepo.checklistStateFlow.first()

        assertEquals("2025-05-15", freshState.lastResetDate)
        assertEquals(true, freshState.itemsCompletedMap["item_clothing"])

        freshTempFile.delete()
    }

    @Test
    fun testViewModelOfflineAndMissingLocationState() {
        val viewModel = SafetyCenterViewModel(
            repository = HeatHealthRepository(enableCandidateFailover = false),
            safetyRepo = SafetyCenterRepository(null),
            autoLoad = false
        )

        // Load data with no coordinates configured
        viewModel.loadData(latitude = null, longitude = null, label = null)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Live risk must NOT be available when campus coordinates are unconfigured", state.isLiveRiskAvailable)
        assertEquals(RiskLevel.UNKNOWN, state.guidance.riskLevel)
        assertTrue("ErrorMessage must indicate location not configured", state.errorMessage?.contains("not configured", ignoreCase = true) == true)
        assertNotNull("Offline guidance overview must be provided", state.guidance.overviewMessage)
    }

    @Test
    fun testSanitizedDialerUriFormatting() {
        val rawPhone = "+91 (80) 2293-2234"
        val sanitized = rawPhone.replace("[^0-9+]".toRegex(), "")

        assertEquals("+918022932234", sanitized)
    }

    @Test
    fun testHeatstrokeFirstAidCriticalWarnings() {
        val guidance = PersonalizedGuidanceEngine.generateGuidance(RiskLevel.EXTREME)
        val advice = guidance.heatstrokeEmergencyAdvice

        assertTrue("First aid advice must warn against giving fluids to unconscious person", advice.contains("Do NOT give fluids", ignoreCase = true))
        assertTrue("First aid advice must include emergency helpline call step", advice.contains("108") || advice.contains("112"))
    }
}
