package com.crattendance.data.db

import androidx.room.TypeConverter
import com.crattendance.data.model.LectureType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Type converters for Room. Time values are stored losslessly as primitives:
 * [LocalDate] as epoch-day, [LocalTime] as minute-of-day and
 * [LocalDateTime] as epoch millis.
 */
class Converters {

    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromMinuteOfDay(value: Int?): LocalTime? =
        value?.let { LocalTime.ofSecondOfDay(it * 60L) }

    @TypeConverter
    fun localTimeToMinuteOfDay(time: LocalTime?): Int? = time?.toSecondOfDay()?.div(60)

    @TypeConverter
    fun fromEpochMillis(value: Long?): LocalDateTime? =
        value?.let(java.time.Instant::ofEpochMilli)?.let { java.time.LocalDateTime.ofInstant(it, java.time.ZoneId.systemDefault()) }

    @TypeConverter
    fun localDateTimeToEpochMillis(dt: LocalDateTime?): Long? =
        dt?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    @TypeConverter
    fun fromLectureType(value: String?): LectureType? =
        value?.let { runCatching { LectureType.valueOf(it) }.getOrNull() } ?: LectureType.OTHER

    @TypeConverter
    fun lectureTypeToString(type: LectureType?): String? = type?.name

    @TypeConverter
    fun fromChar(value: String?): Char? = value?.firstOrNull()

    @TypeConverter
    fun charToString(value: Char?): String? = value?.toString()
}
