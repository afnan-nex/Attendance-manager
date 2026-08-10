package com.crattendance.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Date/time formatting and 7-day window helpers. */
object DateUtils {

    private val TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    private val ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    private val FILE_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    private val TIME_FILE = DateTimeFormatter.ofPattern("HHmm", Locale.ENGLISH)
    private val LONG_DATE = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
    private val MONTH_YEAR = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)

    /** Monday of the week containing [date]. */
    fun weekStart(date: LocalDate): LocalDate =
        date.minusDays((date.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

    /** The 7 days (Mon-Sun) of the week containing [date]. */
    fun weekDays(anchor: LocalDate): List<LocalDate> {
        val monday = weekStart(anchor)
        return (0 until 7).map { monday.plusDays(it.toLong()) }
    }

    /**
     * Slides the 7-day window so [date] is visible. When [date] already sits
     * inside the current window it is returned unchanged; otherwise the window
     * becomes the week containing [date].
     */
    fun windowFor(windowStart: LocalDate, date: LocalDate): List<LocalDate> {
        val days = weekDays(windowStart)
        return if (date in days) days else weekDays(date)
    }

    /** e.g. "9:00 AM". */
    fun formatTime(time: LocalTime): String = time.format(TIME_FORMAT)

    /** e.g. "9:00 AM - 11:30 AM". */
    fun formatTimeRange(start: LocalTime, end: LocalTime): String =
        "${formatTime(start)} - ${formatTime(end)}"

    /** e.g. "2025-01-15". */
    fun formatIso(date: LocalDate): String = date.format(ISO_DATE)

    /** e.g. "15-01-2025" (used in export file names). */
    fun formatFileDate(date: LocalDate): String = date.format(FILE_DATE)

    /** e.g. "0930". */
    fun formatTimeFile(time: LocalTime): String = time.format(TIME_FILE)

    /** e.g. "Monday, 15 January 2025". */
    fun formatLong(date: LocalDate): String = date.format(LONG_DATE)

    /** e.g. "Jan 2025". */
    fun formatMonthYear(date: LocalDate): String = date.format(MONTH_YEAR)
}
