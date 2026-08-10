package com.crattendance.viewmodel

import com.crattendance.testutil.FakeStudentRepository
import com.crattendance.testutil.studentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
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

@OptIn(ExperimentalCoroutinesApi::class)
class ManageStudentsViewModelTest {

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
    fun `validate accepts a well-formed student`() {
        val vm = ManageStudentsViewModel(FakeStudentRepository())
        assertNull(vm.validate(studentEntity()))
    }

    @Test
    fun `validate rejects blank name and registration number`() {
        val vm = ManageStudentsViewModel(FakeStudentRepository())
        assertEquals("Name is required", vm.validate(studentEntity(name = "  ")))
        assertEquals("Registration number is required", vm.validate(studentEntity(registrationNumber = "")))
    }

    @Test
    fun `validate rejects malformed WhatsApp number`() {
        val vm = ManageStudentsViewModel(FakeStudentRepository())
        assertEquals(
            "WhatsApp number must be in Pakistani format (+92… or 03…) with 10-13 digits",
            vm.validate(studentEntity(whatsappNumber = "123"))
        )
    }

    @Test
    fun `validate requires a phone when same-as-whatsapp is unchecked`() {
        val vm = ManageStudentsViewModel(FakeStudentRepository())
        assertEquals(
            "Phone number is required when \"Same as WhatsApp\" is unchecked",
            vm.validate(studentEntity(sameAsWhatsapp = false, phoneNumber = null))
        )
    }

    @Test
    fun `validate requires cnic to be 13 digits when provided`() {
        val vm = ManageStudentsViewModel(FakeStudentRepository())
        assertEquals(
            "CNIC must be 13 digits",
            vm.validate(studentEntity(cnic = "12345"))
        )
        assertNull(vm.validate(studentEntity(cnic = "4210123456789")))
    }

    @Test
    fun `saveStudent persists a valid student and emits success`() = runTest(dispatcher) {
        val repo = FakeStudentRepository()
        val vm = ManageStudentsViewModel(repo)
        val results = mutableListOf<StudentSaveResult>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.saveResult.collect { results.add(it) }
        }

        assertTrue(vm.saveStudent(studentEntity()))
        advanceUntilIdle()

        assertEquals(1, repo.getAll().size)
        assertTrue(results.any { it is StudentSaveResult.Success })
    }

    @Test
    fun `saveStudent rejects duplicate registration numbers`() = runTest(dispatcher) {
        val repo = FakeStudentRepository(
            listOf(studentEntity(id = "s1", registrationNumber = "22P-0001"))
        )
        val vm = ManageStudentsViewModel(repo)
        val results = mutableListOf<StudentSaveResult>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.saveResult.collect { results.add(it) }
        }

        assertTrue(vm.saveStudent(studentEntity(id = "s2", registrationNumber = "22P-0001")))
        advanceUntilIdle()

        assertEquals(1, repo.getAll().size)
        val error = results.filterIsInstance<StudentSaveResult.Error>().firstOrNull()
        assertNotNull(error)
        assertEquals("Registration number already exists", error?.message)
    }

    @Test
    fun `saveStudent refuses invalid input synchronously`() = runTest(dispatcher) {
        val repo = FakeStudentRepository()
        val vm = ManageStudentsViewModel(repo)
        assertFalse(vm.saveStudent(studentEntity(name = "")))
        assertEquals(0, repo.getAll().size)
    }

    @Test
    fun `reorder rewrites the global order in the repository`() = runTest(dispatcher) {
        val repo = FakeStudentRepository(
            listOf(
                studentEntity(id = "s1", orderIndex = 0),
                studentEntity(id = "s2", orderIndex = 1),
                studentEntity(id = "s3", orderIndex = 2)
            )
        )
        val vm = ManageStudentsViewModel(repo)
        advanceUntilIdle()

        vm.reorder(fromIndex = 2, toIndex = 0)
        advanceUntilIdle()

        val order = repo.getAll().sortedBy { it.orderIndex }.map { it.id }
        assertEquals(listOf("s3", "s1", "s2"), order)
    }

    @Test
    fun `deleteStudent removes the student`() = runTest(dispatcher) {
        val repo = FakeStudentRepository(listOf(studentEntity(id = "s1")))
        val vm = ManageStudentsViewModel(repo)
        advanceUntilIdle()

        vm.deleteStudent(studentEntity(id = "s1"))
        advanceUntilIdle()

        assertTrue(repo.getAll().isEmpty())
    }
}
