package com.crattendance.viewmodel

import com.crattendance.testutil.FakeClassRepository
import com.crattendance.testutil.FakeSettingsRepository
import com.crattendance.testutil.classEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState filters classes by the selected day and sorts by time`() = runTest(dispatcher) {
        val mon = LocalDate.of(2025, 1, 6)
        val tue = mon.plusDays(1)
        val repo = FakeClassRepository(
            listOf(
                classEntity(id = "late", dayOfWeek = tue.dayOfWeek.value, startTime = LocalTime.of(11, 0), shortName = "Late"),
                classEntity(id = "early", dayOfWeek = tue.dayOfWeek.value, startTime = LocalTime.of(9, 0), shortName = "Early"),
                classEntity(id = "monOnly", dayOfWeek = mon.dayOfWeek.value, startTime = LocalTime.of(8, 0), shortName = "Mon")
            )
        )
        val vm = HomeViewModel(repo, FakeSettingsRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        advanceUntilIdle()

        vm.selectDate(tue)
        advanceUntilIdle()

        assertEquals(tue, vm.uiState.value.selectedDate)
        assertEquals(listOf("early", "late"), vm.uiState.value.todayClasses.map { it.id })
        assertEquals(7, vm.uiState.value.weekDays.size)
    }

    @Test
    fun `hidden classes are excluded from the home list`() = runTest(dispatcher) {
        val day = LocalDate.now().dayOfWeek.value
        val repo = FakeClassRepository(
            listOf(
                classEntity(id = "visible", dayOfWeek = day, shortName = "Visible"),
                classEntity(id = "hidden", dayOfWeek = day, shortName = "Hidden", isHidden = true)
            )
        )
        val vm = HomeViewModel(repo, FakeSettingsRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        advanceUntilIdle()

        assertEquals(listOf("visible"), vm.uiState.value.todayClasses.map { it.id })
    }

    @Test
    fun `selecting a date far outside the window slides the week`() = runTest(dispatcher) {
        val far = LocalDate.now().plusDays(60)
        val vm = HomeViewModel(FakeClassRepository(), FakeSettingsRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        advanceUntilIdle()

        vm.selectDate(far)
        advanceUntilIdle()

        assertTrue(far in vm.uiState.value.weekDays)
        assertEquals(far, vm.uiState.value.selectedDate)
    }
}
