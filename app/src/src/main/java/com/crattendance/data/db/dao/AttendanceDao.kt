package com.crattendance.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crattendance.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AttendanceDao {

    /** Inserts or replaces a record — the composite unique index prevents duplicates. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: AttendanceEntity): Long

    @Query("SELECT * FROM attendance WHERE classId = :classId AND date = :date")
    fun observeForClassAndDate(classId: String, date: LocalDate): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE classId = :classId AND date = :date")
    suspend fun getForClassAndDate(classId: String, date: LocalDate): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE classId = :classId")
    suspend fun getForClass(classId: String): List<AttendanceEntity>

    @Query("SELECT DISTINCT date FROM attendance WHERE classId = :classId ORDER BY date")
    suspend fun getDatesForClass(classId: String): List<LocalDate>

    @Query("DELETE FROM attendance WHERE classId = :classId")
    suspend fun deleteByClass(classId: String)
}
