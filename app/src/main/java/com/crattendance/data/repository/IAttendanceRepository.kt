package com.crattendance.data.repository

import com.crattendance.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface IAttendanceRepository {
    fun observeForClassAndDate(classId: String, date: LocalDate): Flow<List<AttendanceEntity>>
    suspend fun getForClassAndDate(classId: String, date: LocalDate): List<AttendanceEntity>
    suspend fun getForClass(classId: String): List<AttendanceEntity>
    suspend fun getDatesForClass(classId: String): List<LocalDate>
    suspend fun setPresent(classId: String, studentId: String, date: LocalDate, isPresent: Boolean)
    suspend fun deleteByClass(classId: String)
}
