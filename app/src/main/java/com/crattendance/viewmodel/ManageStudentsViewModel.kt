package com.crattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crattendance.data.model.StudentEntity
import com.crattendance.data.repository.IStudentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ManageStudentsUiState(
    val students: List<StudentEntity> = emptyList()
)

/** Result of saving a student; used to surface validation errors to the sheet. */
sealed interface StudentSaveResult {
    data object Success : StudentSaveResult
    data class Error(val message: String) : StudentSaveResult
}

class ManageStudentsViewModel(
    private val studentRepository: IStudentRepository
) : ViewModel() {

    private val _saveResult = MutableSharedFlow<StudentSaveResult>(extraBufferCapacity = 2)
    val saveResult = _saveResult.asSharedFlow()

    private val _uiState = MutableStateFlow(ManageStudentsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            studentRepository.allStudents.collect { students ->
                _uiState.value = _uiState.value.copy(students = students)
            }
        }
    }

    /** Validates and saves. Returns success or a user-readable error. */
    fun saveStudent(student: StudentEntity): Boolean {
        val error = validate(student)
        if (error != null) {
            viewModelScope.launch { _saveResult.emit(StudentSaveResult.Error(error)) }
            return false
        }
        viewModelScope.launch {
            val ok = studentRepository.save(student)
            if (ok) _saveResult.emit(StudentSaveResult.Success)
            else _saveResult.emit(StudentSaveResult.Error("Registration number already exists"))
        }
        return true
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch { studentRepository.delete(student) }
    }

    /** Reorders students in the list and persists the global order indices. */
    fun reorder(fromIndex: Int, toIndex: Int) {
        val current = uiState.value.students
        if (fromIndex !in current.indices || toIndex !in current.indices || fromIndex == toIndex) return
        val reordered = current.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        viewModelScope.launch {
            studentRepository.reorder(reordered.map { it.id })
        }
    }

    fun validate(student: StudentEntity): String? = when {
        student.name.isBlank() -> "Name is required"
        student.registrationNumber.isBlank() -> "Registration number is required"
        student.section !in 'A'..'F' -> "Section must be between A and F"
        student.whatsappNumber.isBlank() -> "WhatsApp number is required"
        !student.whatsappNumber.replace(Regex("[^0-9+]"), "").matches(Regex("\\+?\\d{10,13}")) ->
            "WhatsApp number must be in Pakistani format (+92… or 03…) with 10-13 digits"
        !student.sameAsWhatsapp && student.phoneNumber.isNullOrBlank() ->
            "Phone number is required when \"Same as WhatsApp\" is unchecked"
        !student.cnic.isNullOrBlank() && student.cnic.replace(Regex("\\D"), "").length != 13 ->
            "CNIC must be 13 digits"
        else -> null
    }
}
