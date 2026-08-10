package com.crattendance.testutil

import com.crattendance.data.model.AppSettingsEntity
import com.crattendance.data.model.AttendanceEntity
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.model.StudentEntity
import com.crattendance.data.repository.IAttendanceRepository
import com.crattendance.data.repository.IClassRepository
import com.crattendance.data.repository.ISettingsRepository
import com.crattendance.data.repository.IStudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** In-memory [IClassRepository] backed by a StateFlow. */
class FakeClassRepository(initial: List<ClassEntity> = emptyList()) : IClassRepository {

    private val _all = MutableStateFlow(initial.sortedBy { it.startTime })
    private val _deleted = mutableListOf<String>()

    override val allClasses: Flow<List<ClassEntity>> = _all
    override val visibleClasses: Flow<List<ClassEntity>> = _all.map { list -> list.filter { !it.isHidden } }

    override suspend fun getAll(): List<ClassEntity> = _all.value
    override suspend fun getById(id: String): ClassEntity? = _all.value.firstOrNull { it.id == id }

    override suspend fun save(cls: ClassEntity) {
        _all.value = (_all.value.filterNot { it.id == cls.id } + cls).sortedBy { it.startTime }
    }

    override suspend fun setHidden(id: String, hidden: Boolean) {
        _all.value = _all.value.map { if (it.id == id) it.copy(isHidden = hidden) else it }
    }

    override suspend fun delete(cls: ClassEntity) {
        _all.value = _all.value.filterNot { it.id == cls.id }
        _deleted += cls.id
    }

    fun wasDeleted(id: String): Boolean = id in _deleted
}

/** In-memory [IStudentRepository] backed by a StateFlow. */
class FakeStudentRepository(initial: List<StudentEntity> = emptyList()) : IStudentRepository {

    private val _all = MutableStateFlow(initial.sortedBy { it.orderIndex })

    override val allStudents: Flow<List<StudentEntity>> = _all

    override fun observeBySection(section: Char): Flow<List<StudentEntity>> =
        _all.map { list -> list.filter { it.section == section } }

    override suspend fun getAll(): List<StudentEntity> = _all.value
    override suspend fun getBySection(section: Char): List<StudentEntity> =
        _all.value.filter { it.section == section }

    override suspend fun getById(id: String): StudentEntity? = _all.value.firstOrNull { it.id == id }

    override suspend fun save(student: StudentEntity): Boolean {
        val duplicate = _all.value.any { it.registrationNumber == student.registrationNumber && it.id != student.id }
        if (duplicate) return false
        _all.value = (_all.value.filterNot { it.id == student.id } + student).sortedBy { it.orderIndex }
        return true
    }

    override suspend fun delete(student: StudentEntity) {
        _all.value = _all.value.filterNot { it.id == student.id }
    }

    override suspend fun nextOrderIndex(): Int = (_all.value.maxOfOrNull { it.orderIndex } ?: -1) + 1

    override suspend fun reorder(orderIds: List<String>) {
        val byId = _all.value.associateBy { it.id }
        val reordered = orderIds.mapIndexedNotNull { index, id -> byId[id]?.copy(orderIndex = index) }
        val remaining = _all.value.filter { it.id !in orderIds }
        _all.value = (reordered + remaining).sortedBy { it.orderIndex }
    }
}

/** In-memory [IAttendanceRepository] backed by a StateFlow of all rows. */
class FakeAttendanceRepository : IAttendanceRepository {

    private val _rows = MutableStateFlow<List<AttendanceEntity>>(emptyList())

    override fun observeForClassAndDate(classId: String, date: LocalDate): Flow<List<AttendanceEntity>> =
        _rows.map { rows -> rows.filter { it.classId == classId && it.date == date } }

    override suspend fun getForClassAndDate(classId: String, date: LocalDate): List<AttendanceEntity> =
        _rows.value.filter { it.classId == classId && it.date == date }

    override suspend fun getForClass(classId: String): List<AttendanceEntity> =
        _rows.value.filter { it.classId == classId }

    override suspend fun getDatesForClass(classId: String): List<LocalDate> =
        _rows.value.filter { it.classId == classId }.map { it.date }.distinct()

    override suspend fun setPresent(classId: String, studentId: String, date: LocalDate, isPresent: Boolean) {
        val row = AttendanceEntity(classId = classId, studentId = studentId, date = date, isPresent = isPresent)
        val rows = _rows.value.toMutableList()
        val index = rows.indexOfFirst { it.classId == classId && it.studentId == studentId && it.date == date }
        if (index >= 0) rows[index] = row else rows += row
        _rows.value = rows
    }

    override suspend fun deleteByClass(classId: String) {
        _rows.value = _rows.value.filterNot { it.classId == classId }
    }
}

/** In-memory [ISettingsRepository] backed by a StateFlow. */
class FakeSettingsRepository(initial: AppSettingsEntity = AppSettingsEntity()) : ISettingsRepository {

    private val _state = MutableStateFlow(initial)

    override val settings: Flow<AppSettingsEntity> = _state
    override suspend fun get(): AppSettingsEntity = _state.value

    override suspend fun setSelectedSection(section: String) {
        _state.value = _state.value.copy(selectedSection = section)
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(biometricEnabled = enabled)
    }
}
