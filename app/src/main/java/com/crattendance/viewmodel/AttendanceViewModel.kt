package com.crattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.model.StudentEntity
import com.crattendance.data.repository.IAttendanceRepository
import com.crattendance.data.repository.IClassRepository
import com.crattendance.data.repository.ISettingsRepository
import com.crattendance.data.repository.IStudentRepository
import com.crattendance.utils.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AttendanceUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val weekDays: List<LocalDate> = emptyList(),
    val classesForDay: List<ClassEntity> = emptyList(),
    val selectedClassId: String? = null,
    val section: String = "All",
    val students: List<StudentEntity> = emptyList(),
    val attendance: Map<String, Boolean> = emptyMap(),
    /** Today = free editing; past = only after 7-tap unlock; future = never. */
    val canEdit: Boolean = false,
    val isFuture: Boolean = false,
    val unlockTaps: Int = 0,
    val unlocked: Boolean = false,
    val selectedClass: ClassEntity? = null
)

/**
 * Attendance screen state. The unlock mechanism is intentionally quirky:
 * past dates are locked, and the user must flip a toggle 7 times to unlock.
 * Re-locking happens when the date changes or [resetUnlock] is called.
 */
class AttendanceViewModel(
    private val classRepository: IClassRepository,
    private val studentRepository: IStudentRepository,
    private val attendanceRepository: IAttendanceRepository,
    settingsRepository: ISettingsRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _selectedClassId = MutableStateFlow<String?>(null)
    private val _windowStart = MutableStateFlow(DateUtils.weekStart(LocalDate.now()))

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastEvents = _toastEvents.asSharedFlow()

    private val _unlocked = MutableStateFlow(false)
    private val _unlockTaps = MutableStateFlow(0)

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                _windowStart,
                _selectedDate,
                settingsRepository.settings
            ) { start, date, settings -> Triple(start, date, settings.selectedSection) }
                .collect { (start, date, section) ->
                    _unlocked.value = false
                    _unlockTaps.value = 0
                    val current = _uiState.value
                    val newState = current.copy(
                        selectedDate = date,
                        weekDays = DateUtils.weekDays(start),
                        section = section
                    )
                    _uiState.value = newState
                    updateEditability()
                    refreshStudents()
                    refreshClassesForDay()
                    loadAttendance()
                }
        }

        viewModelScope.launch {
            classRepository.allClasses.collect {
                _latestClasses = it
                refreshClassesForDay()
                loadAttendance()
            }
        }

        viewModelScope.launch {
            studentRepository.allStudents.collect {
                _latestStudents = it
                refreshStudents()
            }
        }
    }

    private var _latestClasses: List<ClassEntity> = emptyList()
    private var _latestStudents: List<StudentEntity> = emptyList()

    // ---- user actions ----

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        if (date !in DateUtils.weekDays(_windowStart.value)) {
            _windowStart.value = DateUtils.weekStart(date)
        }
    }

    fun selectClass(classId: String) {
        if (_selectedClassId.value == classId) return
        _selectedClassId.value = classId
        loadAttendance()
    }

    fun onUnlockToggle() {
        val date = _selectedDate.value
        if (date >= LocalDate.now()) return
        val taps = _unlockTaps.value + 1
        if (taps >= UNLOCK_TAPS_REQUIRED) {
            _unlockTaps.value = 0
            _unlocked.value = true
            updateEditability()
            _toastEvents.tryEmit("Attendance Edit Unlocked")
        } else {
            _unlockTaps.value = taps
        }
    }

    /** Called when leaving the screen so past editing locks again. */
    fun resetUnlock() {
        _unlocked.value = false
        _unlockTaps.value = 0
        updateEditability()
    }

    fun toggleAttendance(studentId: String) {
        val state = _uiState.value
        val classId = state.selectedClassId ?: return
        if (!state.canEdit) return
        val newValue = !(state.attendance[studentId] ?: false)
        viewModelScope.launch {
            attendanceRepository.setPresent(classId, studentId, state.selectedDate, newValue)
            _uiState.value = _uiState.value.copy(
                attendance = _uiState.value.attendance + (studentId to newValue)
            )
        }
    }

    // ---- internal refresh ----

    private fun refreshClassesForDay() {
        val date = _selectedDate.value
        val forDay = _latestClasses
            .filter { it.dayOfWeek == date.dayOfWeek.value }
            .sortedBy { it.startTime }
        val currentId = _selectedClassId.value
        val validId = if (currentId != null && forDay.any { it.id == currentId }) currentId
        else forDay.firstOrNull()?.id
        if (_selectedClassId.value != validId) _selectedClassId.value = validId
        _uiState.value = _uiState.value.copy(
            classesForDay = forDay,
            selectedClassId = validId,
            selectedClass = forDay.firstOrNull { it.id == validId }
        )
    }

    private fun refreshStudents() {
        val section = _uiState.value.section
        val students = if (section == "All") _latestStudents
        else _latestStudents.filter { it.section.toString() == section }
        _uiState.value = _uiState.value.copy(students = students)
    }

    private fun loadAttendance() {
        val classId = _selectedClassId.value ?: return
        val date = _selectedDate.value
        viewModelScope.launch {
            val rows = attendanceRepository.getForClassAndDate(classId, date)
            _uiState.value = _uiState.value.copy(
                attendance = rows.associate { it.studentId to it.isPresent }
            )
        }
    }

    private fun updateEditability() {
        val date = _selectedDate.value
        val today = LocalDate.now()
        val isFuture = date.isAfter(today)
        val canEdit = when {
            isFuture -> false
            date == today -> true
            else -> _unlocked.value
        }
        _uiState.value = _uiState.value.copy(
            canEdit = canEdit,
            isFuture = isFuture,
            unlocked = _unlocked.value,
            unlockTaps = _unlockTaps.value
        )
    }

    companion object {
        private const val UNLOCK_TAPS_REQUIRED = 7
    }
}
