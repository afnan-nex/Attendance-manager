package com.crattendance.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crattendance.data.model.AttendanceEntity
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.model.StudentEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/** In-memory Room test covering DAO constraints used by the repositories. */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun seedClassAndStudent(): Pair<ClassEntity, StudentEntity> {
        val cls = ClassEntity(
            id = "c1",
            shortName = "CS",
            fullNameWithCode = "CS-101 Computer Programming",
            dayOfWeek = 1,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 30),
            teacherName = "Dr. Khan",
            location = "Room 101"
        )
        val student = StudentEntity(
            id = "s1",
            name = "Ali",
            registrationNumber = "22P-0001",
            section = 'A'
        )
        runBlocking {
            db.classDao().insert(cls)
            db.studentDao().insert(student)
        }
        return cls to student
    }

    @Test
    fun attendanceUpsert_replacesTheRowPerStudentPerClassPerDate() = runBlocking {
        val (cls, student) = seedClassAndStudent()
        val date = LocalDate.of(2025, 1, 13)

        db.attendanceDao().upsert(
            AttendanceEntity(classId = cls.id, studentId = student.id, date = date, isPresent = true)
        )
        db.attendanceDao().upsert(
            AttendanceEntity(classId = cls.id, studentId = student.id, date = date, isPresent = false)
        )

        val rows = db.attendanceDao().getForClassAndDate(cls.id, date)
        assertEquals(1, rows.size)
        assertEquals(false, rows[0].isPresent)
    }

    @Test
    fun studentDao_rejectsDuplicateRegistrationNumbers() = runBlocking {
        val (_, first) = seedClassAndStudent()
        val duplicate = StudentEntity(
            id = "s2",
            name = "Bilal",
            registrationNumber = first.registrationNumber,
            section = 'B'
        )

        var rejected = false
        try {
            db.studentDao().insert(duplicate)
        } catch (e: Exception) {
            rejected = true
        }
        assertTrue("duplicate registration number should be rejected", rejected)
    }

    @Test
    fun deletingAClass_cascadesToItsAttendance() = runBlocking {
        val (cls, student) = seedClassAndStudent()
        db.attendanceDao().upsert(
            AttendanceEntity(classId = cls.id, studentId = student.id, date = LocalDate.of(2025, 1, 13))
        )
        assertEquals(1, db.attendanceDao().getForClass(cls.id).size)

        db.classDao().delete(cls)

        assertEquals(0, db.attendanceDao().getForClass(cls.id).size)
    }

    @Test
    fun settingsDao_upsertMaintainsASingleRow() = runBlocking {
        db.settingsDao().upsert(com.crattendance.data.model.AppSettingsEntity())
        db.settingsDao().upsert(com.crattendance.data.model.AppSettingsEntity(selectedSection = "B"))

        val row = db.settingsDao().get()
        assertEquals("B", row?.selectedSection)
    }
}
