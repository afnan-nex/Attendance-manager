package com.crattendance

import android.app.Application
import com.crattendance.data.db.AppDatabase
import com.crattendance.data.repository.AttendanceRepository
import com.crattendance.data.repository.ClassRepository
import com.crattendance.data.repository.SettingsRepository
import com.crattendance.data.repository.StudentRepository

/** Application class owning the single Room instance and repositories. */
class CRAttendanceApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val classRepository: ClassRepository by lazy {
        ClassRepository(database.classDao(), database.attendanceDao())
    }

    val studentRepository: StudentRepository by lazy {
        StudentRepository(database.studentDao())
    }

    val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepository(database.attendanceDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(database.settingsDao())
    }
}
