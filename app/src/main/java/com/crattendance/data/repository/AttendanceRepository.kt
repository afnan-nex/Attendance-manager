package com.crattendance.data.repository

import com.crattendance.data.db.dao.AttendanceDao
import com.crattendance.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Repository for [AttendanceEntity]. Open for test fakes. */
open class AttendanceRepository(private val attendanceDao: AttendanceDao) : IAttendanceRepository {

    open override fun observeForClassAndDate(classId: String, date: LocalDate): Flow<List<AttendanceEntity>> =
        attendanceDao.observeForClassAndDate(classId, date)

    open override suspend fun getForClassAndDate(classId: String, date: LocalDate): List<AttendanceEntity> =
        attendanceDao.getForClassAndDate(classId, date)

    open override suspend fun getForClass(classId: String): List<AttendanceEntity> =
        attendanceDao.getForClass(classId)

    open override suspend fun getDatesForClass(classId: String): List<LocalDate> =
        attendanceDao.getDatesForClass(classId)

    open override suspend fun setPresent(classId: String, studentId: String, date: LocalDate, isPresent: Boolean) {
        attendanceDao.upsert(
            AttendanceEntity(
                classId = classId,
                studentId = studentId,
                date = date,
                isPresent = isPresent,
                recordedAt = java.time.LocalDateTime.now()
            )
        )
    }

    open override suspend fun deleteByClass(classId: String) = attendanceDao.deleteByClass(classId)
}
