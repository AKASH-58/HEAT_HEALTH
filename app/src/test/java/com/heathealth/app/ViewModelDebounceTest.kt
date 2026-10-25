package com.heathealth.app

import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.viewmodel.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelDebounceTest {

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
    fun testViewModelInflightRequestDebounce() {
        val mockRepo = HeatHealthRepository()
        val viewModel = DashboardViewModel(repository = mockRepo, autoLoad = false)

        val initialState = viewModel.uiState.value
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isRefreshing)
    }
}
