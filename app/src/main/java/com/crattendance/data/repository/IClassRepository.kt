package com.crattendance.data.repository

import com.crattendance.data.model.ClassEntity
import kotlinx.coroutines.flow.Flow

interface IClassRepository {
    val allClasses: Flow<List<ClassEntity>>
    val visibleClasses: Flow<List<ClassEntity>>
    suspend fun getAll(): List<ClassEntity>
    suspend fun getById(id: String): ClassEntity?
    suspend fun save(cls: ClassEntity)
    suspend fun setHidden(id: String, hidden: Boolean)
    suspend fun delete(cls: ClassEntity)
}
