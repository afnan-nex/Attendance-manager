package com.crattendance.utils

import com.crattendance.data.model.AttendanceEntity
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.model.StudentEntity
import java.time.LocalDate
import java.time.LocalDateTime

/** Builds the CSV export string for one class. Pure function — easily unit tested. */
object CsvExporter {

    data class CsvResult(val fileName: String, val content: String)

    /**
     * @param classEntity  the class to export
     * @param students     students already filtered to the selected section (global order)
     * @param attendance   all attendance rows for this class (any section) —
     *                     used to derive the date columns.
     */
    fun build(
        classEntity: ClassEntity,
        students: List<StudentEntity>,
        attendance: List<AttendanceEntity>,
        exportedAt: LocalDateTime = LocalDateTime.now()
    ): CsvResult {
        val dates: List<LocalDate> = attendance.map { it.date }.distinct().sorted()
        val presentByStudentDate: Map<String, Set<LocalDate>> = attendance
            .filter { it.isPresent }
            .groupBy { it.studentId }
            .mapValues { (_, rows) -> rows.map { it.date }.toSet() }

        val header = listOf("sr_no", "name", "reg_no", "section") + dates.map(DateUtils::formatIso)

        val rows: List<List<String>> = students.mapIndexed { index, student ->
            val row = mutableListOf(
                (index + 1).toString(),
                student.name,
                student.registrationNumber,
                student.section.toString()
            )
            dates.forEach { date ->
                row += if (student.id in presentByStudentDate && date in presentByStudentDate.getValue(student.id)) "P" else ""
            }
            row
        }

        val csv = buildString {
            appendLine(renderRow(header))
            rows.forEach { appendLine(renderRow(it)) }
        }

        return CsvResult(fileName = buildFileName(classEntity, exportedAt), content = csv)
    }

    private fun renderRow(cells: List<String>): String = cells.joinToString(",") { escape(it) }

    private fun escape(value: String): String {
        val needsQuoting = value.contains(',') || value.contains('"') || value.contains('\n')
        return if (needsQuoting) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }

    /**
     * `CS-101_Computer-Programming_0930_15-01-2025.csv`
     * Uses the code prefix (first token) of the full name; the remainder is the
     * name with spaces turned into dashes. Falls back to the short name when the
     * full name has no code prefix.
     */
    private fun buildFileName(cls: ClassEntity, exportedAt: LocalDateTime): String {
        val code = cls.code ?: cls.shortName
        val name = (cls.plainName.ifBlank { cls.shortName })
            .replace(Regex("[^A-Za-z0-9 ._-]"), "")
            .trim()
            .replace(' ', '-')
        val time = exportedAt.format(java.time.format.DateTimeFormatter.ofPattern("HHmm"))
        val date = DateUtils.formatFileDate(exportedAt.toLocalDate())
        return "${code}_${name}_${time}_$date.csv"
    }
}
