package com.crattendance.data.repository

import com.crattendance.data.db.dao.StudentDao
import com.crattendance.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/** Repository for [StudentEntity]. Open for test fakes. */
open class StudentRepository(private val studentDao: StudentDao) : IStudentRepository {

    open override val allStudents: Flow<List<StudentEntity>> = studentDao.observeAll()

    open override fun observeBySection(section: Char): Flow<List<StudentEntity>> = studentDao.observeBySection(section)

    open override suspend fun getAll(): List<StudentEntity> = studentDao.getAll()

    open override suspend fun getBySection(section: Char): List<StudentEntity> = studentDao.getBySection(section)

    open override suspend fun getById(id: String): StudentEntity? = studentDao.getById(id)

    open override suspend fun save(student: StudentEntity): Boolean {
        if (studentDao.countByRegistrationNumber(student.registrationNumber, student.id) > 0) {
            return false // registration number already used by another student
        }
        val now = LocalDateTime.now()
        if (studentDao.getById(student.id) == null) {
            // New students are appended to the end of the global order.
            studentDao.insert(
                student.copy(
                    orderIndex = studentDao.getMaxOrderIndex() + 1,
                    createdAt = now,
                    updatedAt = now
                )
            )
        } else {
            studentDao.update(student.copy(updatedAt = now))
        }
        return true
    }

    open override suspend fun delete(student: StudentEntity) = studentDao.delete(student)

    open override suspend fun nextOrderIndex(): Int = studentDao.getMaxOrderIndex() + 1

    /** Rewrites global order indices for the given ids (in list order). */
    open override suspend fun reorder(orderIds: List<String>) {
        val now = LocalDateTime.now()
        orderIds.forEachIndexed { index, id ->
            studentDao.updateOrderIndex(id, index, now)
        }
    }
}
