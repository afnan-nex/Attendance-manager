package com.crattendance.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.LocalTime

/** Type of a lecture/class session. */
enum class LectureType { LECTURE, TUTORIAL, PRACTICAL_LAB, WORKSHOP, SEMINAR, OTHER }

/**
 * A scheduled class/subject. One lecture per subject per week.
 *
 * [dayOfWeek] follows [java.time.DayOfWeek] numbering: Monday=1 … Sunday=7.
 */
@Entity(
    tableName = "classes",
    indices = [Index(value = ["shortName"])]
)
data class ClassEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val shortName: String,
    val fullNameWithCode: String,
    val lectureType: LectureType = LectureType.LECTURE,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val teacherName: String,
    val location: String,
    val creditHours: Int = 3,
    val isHidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /** e.g. "CS-101" from "CS-101 Computer Programming", or null when no code prefix. */
    val code: String?
        get() = fullNameWithCode.trim().substringBefore(' ').takeIf { it.isNotBlank() && it != fullNameWithCode.trim() }

    /** e.g. "Computer Programming" from "CS-101 Computer Programming". */
    val plainName: String
        get() {
            val trimmed = fullNameWithCode.trim()
            return code?.let { trimmed.substringAfter(it).trim() } ?: trimmed
        }
}
