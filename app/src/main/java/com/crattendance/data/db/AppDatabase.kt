package com.crattendance.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crattendance.data.db.dao.AttendanceDao
import com.crattendance.data.db.dao.ClassDao
import com.crattendance.data.db.dao.SettingsDao
import com.crattendance.data.db.dao.StudentDao
import com.crattendance.data.model.AppSettingsEntity
import com.crattendance.data.model.AttendanceEntity
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.model.StudentEntity

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        AttendanceEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        private const val DB_NAME = "cr_attendance.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build().also { INSTANCE = it }
            }
    }
}
