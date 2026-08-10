package com.crattendance.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * One attendance record for a student on a date for a class.
 *
 * A composite unique index on (classId, studentId, date) guarantees a single
 * record per student per class per day; upserts replace the previous row and
 * refresh [recordedAt] (used by the past-edit lock).
 */
@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["classId", "studentId", "date"], unique = true),
        Index(value = ["classId", "date"]),
        Index(value = ["studentId"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: String,
    val studentId: String,
    val date: LocalDate,
    val isPresent: Boolean = false,
    val recordedAt: LocalDateTime = LocalDateTime.now()
)
