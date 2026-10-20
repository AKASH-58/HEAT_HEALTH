package com.heathealth.app

import android.app.Application
import com.heathealth.app.data.local.dao.StudentProfileDao
import com.heathealth.app.data.local.dao.WaterIntakeDao
import com.heathealth.app.data.local.datastore.UserPreferences
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.data.local.entity.StudentProfileEntity
import com.heathealth.app.data.local.entity.WaterIntakeEntity
import com.heathealth.app.data.repository.StudentProfileRepository
import com.heathealth.app.data.repository.WaterIntakeRepository
import com.heathealth.app.viewmodel.WaterTrackerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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

private class DummyApp : Application()

@OptIn(ExperimentalCoroutinesApi::class)
class WaterTrackerViewModelTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var fakeWaterDao: FakeTestWaterIntakeDao
    private lateinit var fakeProfileDao: FakeTestStudentProfileDao
    private lateinit var waterRepo: WaterIntakeRepository
    private lateinit var profileRepo: StudentProfileRepository
    private lateinit var viewModel: WaterTrackerViewModel

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        fakeWaterDao = FakeTestWaterIntakeDao()
        fakeProfileDao = FakeTestStudentProfileDao()
        waterRepo = WaterIntakeRepository(fakeWaterDao, testDispatcher)
        val fakePrefs = FakeTestUserPrefsRepository()
        profileRepo = StudentProfileRepository(fakeProfileDao, fakePrefs, testDispatcher)
        viewModel = WaterTrackerViewModel(DummyApp(), waterRepo, profileRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testQuickAddWater() = runBlocking {
        viewModel.addQuickWater(250)

        val state = viewModel.uiState.value
        assertEquals(1, state.todayEntries.size)
        assertEquals(250, state.todayEntries[0].amountMl)
        assertEquals(250, state.todayTotalMl)
        assertEquals("Added 250 mL of water.", state.userFeedbackMessage)
    }

    @Test
    fun testAddCustomWaterValid() = runBlocking {
        viewModel.onCustomAmountChanged("350")
        viewModel.addCustomWater()

        val state = viewModel.uiState.value
        assertEquals("", state.customAmountInput)
        assertNull(state.customInputError)
        assertEquals(350, state.todayTotalMl)
    }

    @Test
    fun testAddCustomWaterInvalidZeroOrNegative() = runBlocking {
        viewModel.onCustomAmountChanged("0")
        viewModel.addCustomWater()

        val state = viewModel.uiState.value
        assertNotNull(state.customInputError)
        assertEquals("Please enter a valid amount > 0 mL.", state.customInputError)
        assertEquals(0, state.todayTotalMl)
    }

    @Test
    fun testAddCustomWaterExceedsSingleLimit() = runBlocking {
        viewModel.onCustomAmountChanged("4000")
        viewModel.addCustomWater()

        val state = viewModel.uiState.value
        assertEquals("Single entry cannot exceed 3000 mL.", state.customInputError)
    }

    @Test
    fun testDeleteEntryAndUndo() = runBlocking {
        viewModel.addQuickWater(500)
        val entry = viewModel.uiState.value.todayEntries[0]

        viewModel.deleteEntry(entry)
        val stateAfterDelete = viewModel.uiState.value
        assertEquals(0, stateAfterDelete.todayEntries.size)
        assertEquals(0, stateAfterDelete.todayTotalMl)
        assertEquals(entry, stateAfterDelete.lastDeletedEntry)

        viewModel.undoDelete()
        val stateAfterUndo = viewModel.uiState.value
        assertEquals(1, stateAfterUndo.todayEntries.size)
        assertEquals(500, stateAfterUndo.todayTotalMl)
    }

    @Test
    fun testProgressCalculation() = runBlocking {
        viewModel.addQuickWater(1250)
        val state = viewModel.uiState.value

        assertEquals(2500, state.targetMl)
        assertEquals(1250, state.todayTotalMl)
        assertEquals(0.5f, state.progress, 0.01f)
        assertEquals(50, state.progressPercentage)
    }
}

class FakeTestWaterIntakeDao : WaterIntakeDao {
    private val entries = mutableListOf<WaterIntakeEntity>()
    private val _flow = MutableStateFlow<List<WaterIntakeEntity>>(emptyList())
    private var counter = 1L

    override fun getAllWaterIntake(): Flow<List<WaterIntakeEntity>> = _flow

    override fun getWaterIntakeForDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<WaterIntakeEntity>> {
        return _flow.map { list -> list.filter { it.timestamp in startTimestamp..endTimestamp } }
    }

    override suspend fun getWaterIntakeForDateRangeSync(startTimestamp: Long, endTimestamp: Long): List<WaterIntakeEntity> {
        return entries.filter { it.timestamp in startTimestamp..endTimestamp }
    }

    override fun getDailyTotalMl(startTimestamp: Long, endTimestamp: Long): Flow<Int?> {
        return _flow.map { list ->
            val sum = list.filter { it.timestamp in startTimestamp..endTimestamp }.sumOf { it.amountMl }
            if (sum > 0) sum else null
        }
    }

    override suspend fun insertWaterIntake(entry: WaterIntakeEntity): Long {
        val newEntry = if (entry.id == 0L) entry.copy(id = counter++) else entry
        entries.removeAll { it.id == newEntry.id }
        entries.add(0, newEntry)
        _flow.value = entries.toList()
        return newEntry.id
    }

    override suspend fun deleteWaterIntake(entry: WaterIntakeEntity): Int {
        val removed = entries.removeAll { it.id == entry.id }
        _flow.value = entries.toList()
        return if (removed) 1 else 0
    }

    override suspend fun deleteWaterIntakeById(id: Long): Int {
        val removed = entries.removeAll { it.id == id }
        _flow.value = entries.toList()
        return if (removed) 1 else 0
    }

    override suspend fun deleteAllWaterIntake(): Int {
        val size = entries.size
        entries.clear()
        _flow.value = emptyList()
        return size
    }
}

class FakeTestStudentProfileDao : StudentProfileDao {
    private val _flow = MutableStateFlow<StudentProfileEntity?>(StudentProfileEntity())

    override fun getProfile(): Flow<StudentProfileEntity?> = _flow

    override suspend fun getProfileSync(): StudentProfileEntity? = _flow.value

    override suspend fun insertOrUpdateProfile(profile: StudentProfileEntity): Long {
        _flow.value = profile
        return 1L
    }

    override suspend fun deleteProfile(): Int {
        _flow.value = null
        return 1
    }
}

class FakeTestUserPrefsRepository : UserPreferencesRepository(null) {
    private val _prefs = MutableStateFlow(UserPreferences())

    override val userPreferencesFlow: Flow<UserPreferences> = _prefs

    override suspend fun updateHydrationRemindersEnabled(enabled: Boolean) {
        _prefs.value = _prefs.value.copy(hydrationRemindersEnabled = enabled)
    }

    override suspend fun updateReminderFrequencyHours(hours: Int) {
        _prefs.value = _prefs.value.copy(reminderFrequencyHours = hours)
    }
}
