package com.crattendance.data.repository

import com.crattendance.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow

interface IStudentRepository {
    val allStudents: Flow<List<StudentEntity>>
    fun observeBySection(section: Char): Flow<List<StudentEntity>>
    suspend fun getAll(): List<StudentEntity>
    suspend fun getBySection(section: Char): List<StudentEntity>
    suspend fun getById(id: String): StudentEntity?
    suspend fun save(student: StudentEntity): Boolean
    suspend fun delete(student: StudentEntity)
    suspend fun nextOrderIndex(): Int
    suspend fun reorder(orderIds: List<String>)
}
