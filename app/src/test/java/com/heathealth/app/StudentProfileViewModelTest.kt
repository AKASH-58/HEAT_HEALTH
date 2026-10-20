package com.heathealth.app

import android.app.Application
import com.heathealth.app.data.local.dao.StudentProfileDao
import com.heathealth.app.data.local.datastore.UserPreferences
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.data.local.entity.StudentProfileEntity
import com.heathealth.app.data.repository.StudentProfileRepository
import com.heathealth.app.viewmodel.StudentProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

private class DummyApplication : Application()

@OptIn(ExperimentalCoroutinesApi::class)
class StudentProfileViewModelTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var fakeDao: FakeStudentProfileDao
    private lateinit var fakePrefsRepo: FakeUserPreferencesRepository
    private lateinit var repository: StudentProfileRepository
    private lateinit var viewModel: StudentProfileViewModel

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeStudentProfileDao()
        fakePrefsRepo = FakeUserPreferencesRepository()
        repository = StudentProfileRepository(fakeDao, fakePrefsRepo, testDispatcher)
        val mockApp = DummyApplication()
        viewModel = StudentProfileViewModel(mockApp, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialDefaultProfileState() = runBlocking {
        val state = viewModel.uiState.value
        assertEquals("", state.nickname)
        assertEquals("Regular commuting", state.activityType)
        assertEquals("English", state.preferredLanguage)
        assertEquals("2500", state.hydrationTargetMlText)
    }

    @Test
    fun testValidTargetValidation() = runBlocking {
        viewModel.onHydrationTargetChanged("2800")
        val state = viewModel.uiState.value

        assertEquals("2800", state.hydrationTargetMlText)
        assertNull(state.targetValidationError)
    }

    @Test
    fun testInvalidTargetValidationUnder500() = runBlocking {
        viewModel.onHydrationTargetChanged("300")
        val state = viewModel.uiState.value

        assertNotNull(state.targetValidationError)
        assertEquals("Target should be at least 500 mL.", state.targetValidationError)
    }

    @Test
    fun testInvalidTargetValidationOver5000() = runBlocking {
        viewModel.onHydrationTargetChanged("6000")
        val state = viewModel.uiState.value

        assertNotNull(state.targetValidationError)
        assertEquals("Target cannot exceed 5000 mL.", state.targetValidationError)
    }

    @Test
    fun testSaveProfilePersistsChanges() = runBlocking {
        viewModel.onNicknameChanged("Aarav")
        viewModel.onActivityTypeChanged("Frequent outdoor activities")
        viewModel.onPreferredLanguageChanged("Hindi")
        viewModel.onHydrationTargetChanged("3200")

        viewModel.saveProfile()

        val state = viewModel.uiState.value
        assertEquals("Profile preferences saved successfully!", state.successMessage)

        val persisted = fakeDao.getProfileSync()
        assertNotNull(persisted)
        assertEquals("Aarav", persisted?.nickname)
        assertEquals("Frequent outdoor activities", persisted?.activityType)
        assertEquals("Hindi", persisted?.preferredLanguage)
        assertEquals(3200, persisted?.hydrationTargetMl)
    }

    @Test
    fun testResetToDefaults() = runBlocking {
        viewModel.onNicknameChanged("Custom")
        viewModel.saveProfile()

        viewModel.resetToDefaults()

        val state = viewModel.uiState.value
        assertEquals("", state.nickname)
        assertEquals("2500", state.hydrationTargetMlText)
        assertEquals("Preferences reset to default values.", state.successMessage)
    }
}

class FakeStudentProfileDao : StudentProfileDao {
    private val _profileFlow = MutableStateFlow<StudentProfileEntity?>(StudentProfileEntity())

    override fun getProfile(): Flow<StudentProfileEntity?> = _profileFlow

    override suspend fun getProfileSync(): StudentProfileEntity? = _profileFlow.value

    override suspend fun insertOrUpdateProfile(profile: StudentProfileEntity): Long {
        _profileFlow.value = profile
        return 1L
    }

    override suspend fun deleteProfile(): Int {
        _profileFlow.value = null
        return 1
    }
}

class FakeUserPreferencesRepository : UserPreferencesRepository(null) {
    private val _prefs = MutableStateFlow(UserPreferences())

    override val userPreferencesFlow: Flow<UserPreferences> = _prefs

    override suspend fun updateHydrationRemindersEnabled(enabled: Boolean) {
        _prefs.value = _prefs.value.copy(hydrationRemindersEnabled = enabled)
    }

    override suspend fun updateReminderFrequencyHours(hours: Int) {
        _prefs.value = _prefs.value.copy(reminderFrequencyHours = hours)
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        _prefs.value = _prefs.value.copy(notificationsEnabled = enabled)
    }
}
