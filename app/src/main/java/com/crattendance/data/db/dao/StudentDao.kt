package com.crattendance.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.crattendance.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(student: StudentEntity): Long

    @Update
    suspend fun update(student: StudentEntity)

    @Delete
    suspend fun delete(student: StudentEntity)

    @Query("SELECT * FROM students ORDER BY orderIndex")
    fun observeAll(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY orderIndex")
    suspend fun getAll(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE section = :section ORDER BY orderIndex")
    fun observeBySection(section: Char): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE section = :section ORDER BY orderIndex")
    suspend fun getBySection(section: Char): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): StudentEntity?

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM students")
    suspend fun getMaxOrderIndex(): Int

    @Query("SELECT COUNT(*) FROM students WHERE registrationNumber = :regNo AND id <> :excludeId")
    suspend fun countByRegistrationNumber(regNo: String, excludeId: String): Int

    @Query("UPDATE students SET orderIndex = :orderIndex, updatedAt = :now WHERE id = :id")
    suspend fun updateOrderIndex(id: String, orderIndex: Int, now: java.time.LocalDateTime)
}
