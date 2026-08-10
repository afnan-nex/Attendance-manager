package com.crattendance.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.crattendance.data.model.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cls: ClassEntity): Long

    @Update
    suspend fun update(cls: ClassEntity)

    @Delete
    suspend fun delete(cls: ClassEntity)

    @Query("SELECT * FROM classes ORDER BY dayOfWeek, startTime")
    fun observeAll(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes ORDER BY dayOfWeek, startTime")
    suspend fun getAll(): List<ClassEntity>

    @Query("SELECT * FROM classes WHERE isHidden = 0 ORDER BY dayOfWeek, startTime")
    fun observeVisible(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ClassEntity?

    @Query("UPDATE classes SET isHidden = :hidden, updatedAt = :now WHERE id = :id")
    suspend fun setHidden(id: String, hidden: Boolean, now: java.time.LocalDateTime)
}
