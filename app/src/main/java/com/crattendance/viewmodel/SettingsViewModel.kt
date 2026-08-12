package com.crattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.repository.IAttendanceRepository
import com.crattendance.data.repository.IClassRepository
import com.crattendance.data.repository.ISettingsRepository
import com.crattendance.data.repository.IStudentRepository
import com.crattendance.utils.JsonHelper
import com.crattendance.utils.XlsxExporter
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
     * Builds the Excel (.xlsx) export for the selected class, scoped to the
     * selected section. Returns null if the class no longer exists.
     */
    suspend fun buildExportXlsx(classId: String): XlsxExporter.XlsxResult? {
        val cls = classRepository.getById(classId) ?: return null
        val section = settingsRepository.get().selectedSection
        val students = studentRepository.getAll()
            .filter { section == "All" || it.section.toString() == section }
        val attendance = attendanceRepository.getForClass(classId)
        return XlsxExporter.build(cls, students, attendance)
    }

    /**
     * Serialises all students to a pretty-printed JSON string.
     */
    suspend fun exportStudentsJson(): String =
        JsonHelper.studentsToJson(studentRepository.getAll())

    /**
     * Parses [json], upserts each student into the database, and returns a
     * human-readable summary ("Imported 12 students, 1 error: …").
     */
    suspend fun importStudentsJson(json: String): String {
        val result = JsonHelper.studentsFromJson(json)
        result.items.forEach { studentRepository.save(it) }
        return buildSummary("student", result.items.size, result.errors)
    }

    /**
     * Serialises all classes to a pretty-printed JSON string.
     */
    suspend fun exportClassesJson(): String =
        JsonHelper.classesToJson(classRepository.getAll())

    /**
     * Parses [json], upserts each class into the database, and returns a
     * human-readable summary.
     */
    suspend fun importClassesJson(json: String): String {
        val result = JsonHelper.classesFromJson(json)
        result.items.forEach { classRepository.save(it) }
        return buildSummary("class", result.items.size, result.errors)
    }

    private fun buildSummary(entity: String, count: Int, errors: List<String>): String {
        val ok  = "Imported $count ${entity}${if (count == 1) "" else "es"}"
        val err = if (errors.isEmpty()) "" else ", ${errors.size} error(s):\n${errors.take(3).joinToString("\n")}"
        return ok + err
    }
}
