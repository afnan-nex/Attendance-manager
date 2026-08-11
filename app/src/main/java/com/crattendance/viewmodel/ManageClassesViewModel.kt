package com.crattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.repository.IClassRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ManageClassesUiState(
    val classes: List<ClassEntity> = emptyList()
)

sealed interface ClassSaveResult {
    data object Success : ClassSaveResult
    data class Error(val message: String) : ClassSaveResult
}

class ManageClassesViewModel(
    private val classRepository: IClassRepository
) : ViewModel() {

    private val _saveResult = MutableSharedFlow<ClassSaveResult>(extraBufferCapacity = 2)
    val saveResult = _saveResult.asSharedFlow()

    private val _uiState = MutableStateFlow(ManageClassesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            classRepository.allClasses.collect { classes ->
                _uiState.value = _uiState.value.copy(classes = classes)
            }
        }
    }

    fun saveClass(cls: ClassEntity) {
        val error = validate(cls)
        if (error != null) {
            viewModelScope.launch { _saveResult.emit(ClassSaveResult.Error(error)) }
            return
        }
        viewModelScope.launch {
            classRepository.save(cls)
            _saveResult.emit(ClassSaveResult.Success)
        }
    }

    /** Deletes the class and its attendance records. */
    fun deleteClass(cls: ClassEntity) {
        viewModelScope.launch { classRepository.delete(cls) }
    }

    fun toggleHidden(id: String, hidden: Boolean) {
        viewModelScope.launch { classRepository.setHidden(id, hidden) }
    }

    fun validate(cls: ClassEntity): String? = when {
        cls.shortName.isBlank() -> "Short name is required"
        cls.shortName.length > 10 -> "Short name must be 10 characters or fewer"
        cls.fullNameWithCode.isBlank() -> "Full name with code is required"
        cls.fullNameWithCode.length > 100 -> "Full name must be 100 characters or fewer"
        cls.teacherName.isBlank() -> "Teacher name is required"
        cls.location.isBlank() -> "Location is required"
        cls.dayOfWeek !in 1..7 -> "Select a day"
        cls.creditHours !in 1..5 -> "Credit hours must be between 1 and 5"
        cls.startTime >= cls.endTime -> "Start time must be before end time"
        else -> null
    }
}
