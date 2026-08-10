package com.crattendance.viewmodel

import com.crattendance.testutil.FakeClassRepository
import com.crattendance.testutil.classEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ManageClassesViewModelTest {

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
    fun `validate accepts a well-formed class`() {
        val vm = ManageClassesViewModel(FakeClassRepository())
        assertNull(vm.validate(classEntity()))
    }

    @Test
    fun `validate enforces required fields and value ranges`() {
        val vm = ManageClassesViewModel(FakeClassRepository())
        assertEquals("Short name is required", vm.validate(classEntity(shortName = "")))
        assertEquals("Short name must be 10 characters or fewer", vm.validate(classEntity(shortName = "ABCDEFGHIJK")))
        assertEquals("Full name with code is required", vm.validate(classEntity(fullNameWithCode = "  ")))
        assertEquals("Teacher name is required", vm.validate(classEntity(teacherName = "")))
        assertEquals("Location is required", vm.validate(classEntity(location = "  ")))
        assertEquals("Select a day", vm.validate(classEntity(dayOfWeek = 0)))
        assertEquals("Credit hours must be between 1 and 5", vm.validate(classEntity(creditHours = 6)))
        assertEquals("Start time must be before end time", vm.validate(classEntity(startTime = LocalTime.of(11, 0), endTime = LocalTime.of(9, 0))))
    }

    @Test
    fun `saveClass persists the class and emits success`() = runTest(dispatcher) {
        val repo = FakeClassRepository()
        val vm = ManageClassesViewModel(repo)
        val results = mutableListOf<ClassSaveResult>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.saveResult.collect { results.add(it) }
        }

        vm.saveClass(classEntity())
        advanceUntilIdle()

        assertEquals(1, repo.getAll().size)
        assertTrue(results.any { it is ClassSaveResult.Success })
    }

    @Test
    fun `saveClass rejects invalid input with an error result`() = runTest(dispatcher) {
        val repo = FakeClassRepository()
        val vm = ManageClassesViewModel(repo)
        val results = mutableListOf<ClassSaveResult>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.saveResult.collect { results.add(it) }
        }

        vm.saveClass(classEntity(shortName = ""))
        advanceUntilIdle()

        assertTrue(repo.getAll().isEmpty())
        val error = results.filterIsInstance<ClassSaveResult.Error>().firstOrNull()
        assertEquals("Short name is required", error?.message)
    }

    @Test
    fun `toggleHidden hides the class from the home list only`() = runTest(dispatcher) {
        val repo = FakeClassRepository(listOf(classEntity(id = "c1")))
        val vm = ManageClassesViewModel(repo)
        advanceUntilIdle()

        vm.toggleHidden("c1", hidden = true)
        advanceUntilIdle()

        assertTrue(repo.getById("c1")!!.isHidden)
        assertTrue(repo.visibleClasses.first().none { it.id == "c1" })
        assertTrue(repo.getAll().any { it.id == "c1" })
    }

    @Test
    fun `deleteClass removes the class from the repository`() = runTest(dispatcher) {
        val repo = FakeClassRepository(listOf(classEntity(id = "c1")))
        val vm = ManageClassesViewModel(repo)
        advanceUntilIdle()

        vm.deleteClass(classEntity(id = "c1"))
        advanceUntilIdle()

        assertTrue(repo.wasDeleted("c1"))
        assertTrue(repo.getAll().isEmpty())
    }
}
