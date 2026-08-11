package com.crattendance.viewmodel

import com.crattendance.data.model.AppSettingsEntity
import com.crattendance.testutil.FakeAttendanceRepository
import com.crattendance.testutil.FakeClassRepository
import com.crattendance.testutil.FakeSettingsRepository
import com.crattendance.testutil.FakeStudentRepository
import com.crattendance.testutil.classEntity
import com.crattendance.testutil.studentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.attendanceViewModel(
        classes: FakeClassRepository = FakeClassRepository(),
        students: FakeStudentRepository = FakeStudentRepository(),
        attendance: FakeAttendanceRepository = FakeAttendanceRepository(),
        settings: FakeSettingsRepository = FakeSettingsRepository()
    ): AttendanceViewModel {
        val vm = AttendanceViewModel(classes, students, attendance, settings)
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `today is editable without any unlock`() = runTest(dispatcher) {
        val classes = FakeClassRepository(
            listOf(classEntity(id = "c1", dayOfWeek = LocalDate.now().dayOfWeek.value))
        )
        val students = FakeStudentRepository(listOf(studentEntity(id = "s1", section = 'A')))

        val vm = attendanceViewModel(classes = classes, students = students)

        assertFalse(vm.uiState.value.isFuture)
        assertTrue(vm.uiState.value.canEdit)
        assertEquals("c1", vm.uiState.value.selectedClassId)
    }

    @Test
    fun `future dates are never editable even after unlock taps`() = runTest(dispatcher) {
        val future = LocalDate.now().plusDays(1)
        val classes = FakeClassRepository(
            listOf(classEntity(id = "c1", dayOfWeek = future.dayOfWeek.value))
        )
        val vm = attendanceViewModel(classes = classes)

        vm.selectDate(future)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isFuture)
        assertFalse(vm.uiState.value.canEdit)

        vm.onUnlockToggle()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canEdit)
    }

    @Test
    fun `past date stays locked until seven toggle taps then unlocks with a toast`() = runTest(dispatcher) {
        val past = LocalDate.now().minusDays(1)
        val classes = FakeClassRepository(
            listOf(classEntity(id = "c1", dayOfWeek = past.dayOfWeek.value))
        )
        val vm = attendanceViewModel(classes = classes)

        vm.selectDate(past)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canEdit)

        val toasts = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.toastEvents.collect { toasts.add(it) }
        }

        repeat(6) { vm.onUnlockToggle() }
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canEdit)
        assertFalse(vm.uiState.value.unlocked)

        vm.onUnlockToggle()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.unlocked)
        assertTrue(vm.uiState.value.canEdit)
        assertEquals(listOf("Attendance Edit Unlocked"), toasts)
    }

    @Test
    fun `changing the date re-locks past editing`() = runTest(dispatcher) {
        val past = LocalDate.now().minusDays(1)
        val classes = FakeClassRepository(
            listOf(
                classEntity(id = "c1", dayOfWeek = past.dayOfWeek.value, startTime = java.time.LocalTime.of(9, 0)),
                classEntity(id = "c2", dayOfWeek = past.minusDays(1).dayOfWeek.value, startTime = java.time.LocalTime.of(10, 0))
            )
        )
        val vm = attendanceViewModel(classes = classes)

        vm.selectDate(past)
        advanceUntilIdle()
        repeat(7) { vm.onUnlockToggle() }
        advanceUntilIdle()
        assertTrue(vm.uiState.value.unlocked)

        vm.selectDate(past.minusDays(1))
        advanceUntilIdle()

        assertFalse(vm.uiState.value.unlocked)
        assertFalse(vm.uiState.value.canEdit)
    }

    @Test
    fun `resetUnlock re-locks past editing`() = runTest(dispatcher) {
        val past = LocalDate.now().minusDays(1)
        val classes = FakeClassRepository(
            listOf(classEntity(id = "c1", dayOfWeek = past.dayOfWeek.value))
        )
        val vm = attendanceViewModel(classes = classes)

        vm.selectDate(past)
        advanceUntilIdle()
        repeat(7) { vm.onUnlockToggle() }
        advanceUntilIdle()
        assertTrue(vm.uiState.value.unlocked)

        vm.resetUnlock()

        assertFalse(vm.uiState.value.unlocked)
        assertFalse(vm.uiState.value.canEdit)
    }

    @Test
    fun `toggleAttendance persists the switch state when editable`() = runTest(dispatcher) {
        val today = LocalDate.now()
        val classes = FakeClassRepository(
            listOf(classEntity(id = "c1", dayOfWeek = today.dayOfWeek.value))
        )
        val students = FakeStudentRepository(listOf(studentEntity(id = "s1", section = 'A')))
        val attendance = FakeAttendanceRepository()
        val vm = attendanceViewModel(classes = classes, students = students, attendance = attendance)

        assertTrue(vm.uiState.value.canEdit)
        vm.toggleAttendance("s1")
        advanceUntilIdle()

        val rows = attendance.getForClassAndDate("c1", today)
        assertEquals(1, rows.size)
        assertTrue(rows.first().isPresent)
        assertTrue(vm.uiState.value.attendance["s1"] == true)

        // Toggling again flips it back.
        vm.toggleAttendance("s1")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.attendance["s1"] == false)
    }

    @Test
    fun `toggleAttendance is ignored when editing is locked`() = runTest(dispatcher) {
        val past = LocalDate.now().minusDays(1)
        val classes = FakeClassRepository(
            listOf(classEntity(id = "c1", dayOfWeek = past.dayOfWeek.value))
        )
        val students = FakeStudentRepository(listOf(studentEntity(id = "s1", section = 'A')))
        val attendance = FakeAttendanceRepository()
        val vm = attendanceViewModel(classes = classes, students = students, attendance = attendance)

        vm.selectDate(past)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canEdit)

        vm.toggleAttendance("s1")
        advanceUntilIdle()

        assertEquals(0, attendance.getForClassAndDate("c1", past).size)
    }

    @Test
    fun `students are filtered by the selected section`() = runTest(dispatcher) {
        val today = LocalDate.now()
        val classes = FakeClassRepository(
            listOf(classEntity(id = "c1", dayOfWeek = today.dayOfWeek.value))
        )
        val students = FakeStudentRepository(
            listOf(
                studentEntity(id = "a1", section = 'A'),
                studentEntity(id = "b1", section = 'B')
            )
        )
        val settings = FakeSettingsRepository(AppSettingsEntity(selectedSection = "A"))

        val vm = attendanceViewModel(classes = classes, students = students, settings = settings)

        assertEquals(listOf("a1"), vm.uiState.value.students.map { it.id })
    }

    @Test
    fun `empty day has no selected class`() = runTest(dispatcher) {
        val vm = attendanceViewModel(classes = FakeClassRepository())
        assertTrue(vm.uiState.value.classesForDay.isEmpty())
        assertEquals(null, vm.uiState.value.selectedClassId)
    }
}
