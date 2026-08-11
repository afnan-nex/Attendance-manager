package com.crattendance.utils

import com.crattendance.data.model.AttendanceEntity
import com.crattendance.testutil.classEntity
import com.crattendance.testutil.studentEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class CsvExporterTest {

    private val exportedAt = LocalDateTime.of(2025, 1, 15, 9, 30)

    @Test
    fun build_writesHeaderChronologicalDateColumnsAndPresentMarks() {
        val cls = classEntity(fullNameWithCode = "CS-101 Computer Programming")
        val students = listOf(
            studentEntity(id = "s1", name = "Ali Khan", registrationNumber = "22P-0001", section = 'A', orderIndex = 0),
            studentEntity(id = "s2", name = "Bilal Ahmed", registrationNumber = "22P-0002", section = 'A', orderIndex = 1)
        )
        val attendance = listOf(
            AttendanceEntity(classId = cls.id, studentId = "s1", date = LocalDate.of(2025, 1, 14), isPresent = false),
            AttendanceEntity(classId = cls.id, studentId = "s1", date = LocalDate.of(2025, 1, 13), isPresent = true),
            AttendanceEntity(classId = cls.id, studentId = "s2", date = LocalDate.of(2025, 1, 13), isPresent = false)
        )

        val result = CsvExporter.build(cls, students, attendance, exportedAt)

        val expected = buildString {
            appendLine("sr_no,name,reg_no,section,2025-01-13,2025-01-14")
            appendLine("1,Ali Khan,22P-0001,A,P,")
            appendLine("2,Bilal Ahmed,22P-0002,A,,")
        }
        assertEquals(expected, result.content)
    }

    @Test
    fun build_escapesCommasQuotesAndNewlines() {
        val cls = classEntity()
        val students = listOf(
            studentEntity(id = "s1", name = "Khan, Abdul", registrationNumber = "22P-0001", section = 'A'),
            studentEntity(id = "s2", name = "Ali \"The\" Boss", registrationNumber = "22P-0002", section = 'A'),
            studentEntity(id = "s3", name = "Multi\nLine", registrationNumber = "22P-0003", section = 'A')
        )

        val result = CsvExporter.build(cls, students, emptyList(), exportedAt)

        val expected = buildString {
            appendLine("sr_no,name,reg_no,section")
            appendLine("1,\"Khan, Abdul\",22P-0001,A")
            appendLine("2,\"Ali \"\"The\"\" Boss\",22P-0002,A")
            appendLine("3,\"Multi\nLine\",22P-0003,A")
        }
        assertEquals(expected, result.content)
    }

    @Test
    fun build_omitsDateColumnsWhenThereIsNoAttendance() {
        val cls = classEntity()
        val students = listOf(studentEntity())

        val result = CsvExporter.build(cls, students, emptyList(), exportedAt)

        assertEquals("sr_no,name,reg_no,section\n1,Ali,22P-0001,A\n", result.content)
    }

    @Test
    fun build_numberRowsPerSectionSequentiallyFromOne() {
        // Section-scoping already happened upstream; the export re-numbers 1..n.
        val cls = classEntity()
        val students = listOf(
            studentEntity(id = "s1", registrationNumber = "22P-0001", section = 'B'),
            studentEntity(id = "s2", registrationNumber = "22P-0002", section = 'B')
        )

        val result = CsvExporter.build(cls, students, emptyList(), exportedAt)

        assertTrue(result.content.startsWith("sr_no,name,reg_no,section\n"))
        assertTrue(result.content.contains("1,Ali,22P-0001,B\n"))
        assertTrue(result.content.contains("2,Ali,22P-0002,B\n"))
    }

    @Test
    fun build_fileNameUsesCodeDashNameTimestampAndDate() {
        val cls = classEntity(fullNameWithCode = "CS-101 Computer Programming")
        val result = CsvExporter.build(cls, emptyList(), emptyList(), exportedAt)
        assertEquals("CS-101_Computer-Programming_0930_15-01-2025.csv", result.fileName)
    }

    @Test
    fun build_fileNameFallsBackToShortNameWhenNoCodePrefix() {
        // A single-token name has no code prefix, so the short name is used.
        val cls = classEntity(shortName = "MATH", fullNameWithCode = "Algorithms")
        val result = CsvExporter.build(cls, emptyList(), emptyList(), exportedAt)
        assertEquals("MATH_Algorithms_0930_15-01-2025.csv", result.fileName)
    }
}
