package com.crattendance.viewmodel

import com.crattendance.data.model.AppSettingsEntity
import com.crattendance.data.model.AttendanceEntity
import com.crattendance.testutil.FakeAttendanceRepository
import com.crattendance.testutil.FakeClassRepository
import com.crattendance.testutil.FakeSettingsRepository
import com.crattendance.testutil.FakeStudentRepository
import com.crattendance.testutil.classEntity
import com.crattendance.testutil.studentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.collectUiState(vm: SettingsViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        advanceUntilIdle()
    }

    @Test
    fun `uiState exposes settings and classes`() = runTest(dispatcher) {
        val cls = classEntity(id = "c1")
        val settings = FakeSettingsRepository(AppSettingsEntity(selectedSection = "C", biometricEnabled = true))
        val vm = SettingsViewModel(
            FakeClassRepository(listOf(cls)),
            FakeStudentRepository(),
            FakeAttendanceRepository(),
            settings
        )
        collectUiState(vm)

        assertEquals("C", vm.uiState.value.selectedSection)
        assertEquals(true, vm.uiState.value.biometricEnabled)
        assertEquals(listOf("c1"), vm.uiState.value.classes.map { it.id })
    }

    @Test
    fun `setSection persists to the settings repository`() = runTest(dispatcher) {
        val settings = FakeSettingsRepository()
        val vm = SettingsViewModel(
            FakeClassRepository(),
            FakeStudentRepository(),
            FakeAttendanceRepository(),
            settings
        )
        collectUiState(vm)

        vm.setSection("D")
        advanceUntilIdle()

        assertEquals("D", settings.get().selectedSection)
    }

    @Test
    fun `setBiometricEnabled persists to the settings repository`() = runTest(dispatcher) {
        val settings = FakeSettingsRepository()
        val vm = SettingsViewModel(
            FakeClassRepository(),
            FakeStudentRepository(),
            FakeAttendanceRepository(),
            settings
        )
        collectUiState(vm)

        vm.setBiometricEnabled(true)
        advanceUntilIdle()

        assertEquals(true, settings.get().biometricEnabled)
    }

    @Test
    fun `buildExportCsv scopes rows to the selected section`() = runTest(dispatcher) {
        val cls = classEntity(id = "c1")
        val students = listOf(
            studentEntity(id = "a1", registrationNumber = "22P-0001", section = 'A'),
            studentEntity(id = "b1", registrationNumber = "22P-0002", section = 'B')
        )
        val settings = FakeSettingsRepository(AppSettingsEntity(selectedSection = "B"))
        val vm = SettingsViewModel(
            FakeClassRepository(listOf(cls)),
            FakeStudentRepository(students),
            FakeAttendanceRepository(),
            settings
        )
        collectUiState(vm)

        val csv = vm.buildExportCsv("c1")
        assertNotNull(csv)
        assertTrue(csv!!.content.contains("22P-0002"))
        assertFalse(csv.content.contains("22P-0001"))
    }

    @Test
    fun `buildExportCsv includes all sections when set to All`() = runTest(dispatcher) {
        val cls = classEntity(id = "c1")
        val students = listOf(
            studentEntity(id = "a1", registrationNumber = "22P-0001", section = 'A'),
            studentEntity(id = "b1", registrationNumber = "22P-0002", section = 'B')
        )
        val vm = SettingsViewModel(
            FakeClassRepository(listOf(cls)),
            FakeStudentRepository(students),
            FakeAttendanceRepository(),
            FakeSettingsRepository(AppSettingsEntity(selectedSection = "All"))
        )
        collectUiState(vm)

        val csv = vm.buildExportCsv("c1")
        assertNotNull(csv)
        assertTrue(csv!!.content.contains("22P-0001"))
        assertTrue(csv.content.contains("22P-0002"))
    }

    @Test
    fun `buildExportCsv carries attendance date columns and marks`() = runTest(dispatcher) {
        val cls = classEntity(id = "c1")
        val students = listOf(studentEntity(id = "a1", registrationNumber = "22P-0001", section = 'A'))
        val attendance = FakeAttendanceRepository()
        attendance.setPresent("c1", "a1", LocalDate.of(2025, 1, 13), isPresent = true)

        val vm = SettingsViewModel(
            FakeClassRepository(listOf(cls)),
            FakeStudentRepository(students),
            attendance,
            FakeSettingsRepository(AppSettingsEntity(selectedSection = "All"))
        )
        collectUiState(vm)

        val csv = vm.buildExportCsv("c1")
        assertNotNull(csv)
        assertTrue(csv!!.content.contains("2025-01-13"))
        assertTrue(csv.content.contains("1,Ali,22P-0001,A,P"))
    }

    @Test
    fun `buildExportCsv returns null when the class no longer exists`() = runTest(dispatcher) {
        val vm = SettingsViewModel(
            FakeClassRepository(),
            FakeStudentRepository(),
            FakeAttendanceRepository(),
            FakeSettingsRepository()
        )
        collectUiState(vm)

        assertNull(vm.buildExportCsv("missing"))
    }
}
