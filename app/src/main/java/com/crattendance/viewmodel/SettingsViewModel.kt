package com.crattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.repository.IAttendanceRepository
import com.crattendance.data.repository.IClassRepository
import com.crattendance.data.repository.ISettingsRepository
import com.crattendance.data.repository.IStudentRepository
import com.crattendance.utils.CsvExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedSection: String = "All",
    val biometricEnabled: Boolean = false,
    val classes: List<ClassEntity> = emptyList(),
    val selectedExportClassId: String? = null
)

class SettingsViewModel(
    private val classRepository: IClassRepository,
    private val studentRepository: IStudentRepository,
    private val attendanceRepository: IAttendanceRepository,
    private val settingsRepository: ISettingsRepository
) : ViewModel() {

    private val _selectedExportClassId = MutableStateFlow<String?>(null)

    val uiState = combine(
        settingsRepository.settings,
        classRepository.allClasses,
        _selectedExportClassId
    ) { settings, classes, exportId ->
        SettingsUiState(
            selectedSection = settings.selectedSection,
            biometricEnabled = settings.biometricEnabled,
            classes = classes,
            selectedExportClassId = exportId
        )
    }.stateIn(
        scope = viewModelScope,
        // Eagerly: the ViewModel now lives for the whole activity, so collect
        // once at creation and keep the data warm across tab switches.
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState()
    )

    fun selectExportClass(classId: String?) {
        _selectedExportClassId.value = classId
    }

    fun setSection(section: String) {
        viewModelScope.launch { settingsRepository.setSelectedSection(section) }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBiometricEnabled(enabled) }
    }

    /**
     * Builds the CSV for the selected class, scoped to the selected section.
     * Returns null if the class no longer exists.
     */
    suspend fun buildExportCsv(classId: String): CsvExporter.CsvResult? {
        val cls = classRepository.getById(classId) ?: return null
        val section = settingsRepository.get().selectedSection
        val students = studentRepository.getAll()
            .filter { section == "All" || it.section.toString() == section }
        val attendance = attendanceRepository.getForClass(classId)
        return CsvExporter.build(cls, students, attendance)
    }
}
