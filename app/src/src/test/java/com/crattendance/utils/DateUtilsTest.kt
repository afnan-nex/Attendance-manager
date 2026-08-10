package com.crattendance.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class DateUtilsTest {

    @Test
    fun weekStart_returnsTheMondayOfTheContainingWeek() {
        // 2025-01-08 is a Wednesday; the week starts Monday 2025-01-06.
        assertEquals(LocalDate.of(2025, 1, 6), DateUtils.weekStart(LocalDate.of(2025, 1, 8)))
        // A Monday is its own week start.
        assertEquals(LocalDate.of(2025, 1, 6), DateUtils.weekStart(LocalDate.of(2025, 1, 6)))
        // Sunday 2025-01-12 belongs to the week that starts Monday 2025-01-06.
        assertEquals(LocalDate.of(2025, 1, 6), DateUtils.weekStart(LocalDate.of(2025, 1, 12)))
    }

    @Test
    fun weekDays_returnsSevenConsecutiveDaysFromMonday() {
        val days = DateUtils.weekDays(LocalDate.of(2025, 1, 8))
        assertEquals(7, days.size)
        assertEquals(DayOfWeek.MONDAY, days.first().dayOfWeek)
        assertEquals(LocalDate.of(2025, 1, 6), days.first())
        assertEquals(LocalDate.of(2025, 1, 12), days.last())
        assertEquals(days, (0L..6L).map { days.first().plusDays(it) })
    }

    @Test
    fun windowFor_keepsTheCurrentWindow_whenDateIsInside() {
        val window = DateUtils.weekDays(LocalDate.of(2025, 1, 8))
        assertEquals(window, DateUtils.windowFor(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 10)))
    }

    @Test
    fun windowFor_showsTheContainingWeek_whenDateIsOutside() {
        // Anchor week starts Monday 2025-01-06; 2025-02-01 (a Saturday) is far outside.
        val result = DateUtils.windowFor(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 2, 1))
        // The week containing 2025-02-01 starts Monday 2025-01-27.
        assertEquals(LocalDate.of(2025, 1, 27), result.first())
        assertTrue(LocalDate.of(2025, 2, 1) in result)
        assertEquals(7, result.size)
    }

    @Test
    fun windowFor_midWeekTargetDatesAlwaysLandInsideTheWindow() {
        // A Monday jump must show the week containing that Monday, not the prior week.
        val result = DateUtils.windowFor(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 2, 3))
        assertTrue(LocalDate.of(2025, 2, 3) in result)
    }

    @Test
    fun timeFormats_areHumanReadable12Hour() {
        assertEquals("9:05 AM", DateUtils.formatTime(LocalTime.of(9, 5)))
        assertEquals("5:30 PM", DateUtils.formatTime(LocalTime.of(17, 30)))
        assertEquals("12:00 PM", DateUtils.formatTime(LocalTime.of(12, 0)))
        assertEquals("9:00 AM - 11:30 AM", DateUtils.formatTimeRange(LocalTime.of(9, 0), LocalTime.of(11, 30)))
    }

    @Test
    fun dateFormats_matchTheSpec() {
        assertEquals("2025-01-15", DateUtils.formatIso(LocalDate.of(2025, 1, 15)))
        assertEquals("15-01-2025", DateUtils.formatFileDate(LocalDate.of(2025, 1, 15)))
        assertEquals("0930", DateUtils.formatTimeFile(LocalTime.of(9, 30)))
    }
}
