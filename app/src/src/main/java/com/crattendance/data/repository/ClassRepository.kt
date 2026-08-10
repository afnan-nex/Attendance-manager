package com.crattendance.data.repository

import com.crattendance.data.db.dao.AttendanceDao
import com.crattendance.data.db.dao.ClassDao
import com.crattendance.data.model.ClassEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/** Repository for [ClassEntity]. Open for test fakes. */
open class ClassRepository(
    private val classDao: ClassDao,
    private val attendanceDao: AttendanceDao
) : IClassRepository {

    open override val allClasses: Flow<List<ClassEntity>> = classDao.observeAll()
    open override val visibleClasses: Flow<List<ClassEntity>> = classDao.observeVisible()

    open override suspend fun getAll(): List<ClassEntity> = classDao.getAll()

    open override suspend fun getById(id: String): ClassEntity? = classDao.getById(id)

    open override suspend fun save(cls: ClassEntity) {
        if (classDao.getById(cls.id) == null) {
            classDao.insert(cls.copy(createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()))
        } else {
            classDao.update(cls.copy(updatedAt = LocalDateTime.now()))
        }
    }

    open override suspend fun setHidden(id: String, hidden: Boolean) =
        classDao.setHidden(id, hidden, LocalDateTime.now())

    /** Deletes the class and its attendance records (attendance cascades via FK). */
    open override suspend fun delete(cls: ClassEntity) {
        attendanceDao.deleteByClass(cls.id)
        classDao.delete(cls)
    }
}
