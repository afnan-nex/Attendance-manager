package com.crattendance.utils

import com.crattendance.data.model.AttendanceEntity
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.model.StudentEntity
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Builds a styled `.xlsx` workbook for one class and returns the raw bytes.
 *
 * Uses [fastexcel](https://github.com/dhatim/fastexcel) which streams directly
 * to an [OutputStream] — no in-memory DOM, low heap usage.
 *
 * ### Sheet layout
 * ```
 * | Sr# | Name | Reg No | Section | 15-Jan | 16-Jan | … | Total P | Total A | % |
 * ```
 * - Header row : bold, blue fill (#1B5E9E), white text, centered.
 * - Present "P" : light-green fill (#E8F5E9).
 * - Absent cells: empty, light-red fill (#FFEBEE).
 * - Summary cols: Total P / Total A / % appended at the right.
 * - Zebra rows  : alternating #F5F8FC on even data rows (static + summary cols only).
 */
object XlsxExporter {

    data class XlsxResult(val fileName: String, val bytes: ByteArray)

    fun build(
        classEntity: ClassEntity,
        students: List<StudentEntity>,
        attendance: List<AttendanceEntity>,
        exportedAt: LocalDateTime = LocalDateTime.now()
    ): XlsxResult {
        val dates: List<LocalDate> = attendance.map { it.date }.distinct().sorted()
        val presentByStudentDate: Map<String, Set<LocalDate>> = attendance
            .filter { it.isPresent }
            .groupBy { it.studentId }
            .mapValues { (_, rows) -> rows.map { it.date }.toSet() }

        val lastCol = 4 + dates.size + 2   // index of final % column (0-based)

        val bytes = ByteArrayOutputStream().use { bos ->
            val wb = Workbook(bos, "CR Attendance", "1.0")
            val ws = wb.newWorksheet(classEntity.shortName.take(31))

            writeHeader(ws, dates, lastCol)
            writeData(ws, students, dates, presentByStudentDate, lastCol)
            setColumnWidths(ws, dates.size)

            wb.finish()
            bos.toByteArray()
        }

        return XlsxResult(
            fileName = buildFileName(classEntity, exportedAt),
            bytes    = bytes
        )
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private fun writeHeader(ws: Worksheet, dates: List<LocalDate>, lastCol: Int) {
        val headers = buildList {
            add("Sr#"); add("Name"); add("Reg No"); add("Section")
            dates.forEach { add(DateUtils.formatShort(it)) }
            add("Total P"); add("Total A"); add("%")
        }
        headers.forEachIndexed { col, title -> ws.value(0, col, title) }

        // Style entire header row via range — the correct fastexcel API.
        ws.range(0, 0, 0, lastCol).style()
            .bold()
            .fillColor("1B5E9E")
            .fontColor("FFFFFF")
            .horizontalAlignment("center")
            .set()
    }

    // ── Data rows ─────────────────────────────────────────────────────────────

    private fun writeData(
        ws: Worksheet,
        students: List<StudentEntity>,
        dates: List<LocalDate>,
        presentByStudentDate: Map<String, Set<LocalDate>>,
        lastCol: Int
    ) {
        val sumCol = 4 + dates.size   // index of "Total P" column

        students.forEachIndexed { idx, student ->
            val row          = idx + 1
            val presentDates = presentByStudentDate[student.id] ?: emptySet()
            val totalPresent = dates.count { it in presentDates }
            val totalAbsent  = dates.size - totalPresent
            val pct          = if (dates.isEmpty()) 0.0 else totalPresent * 100.0 / dates.size

            // Static cells
            ws.value(row, 0, (idx + 1).toDouble())
            ws.value(row, 1, student.name)
            ws.value(row, 2, student.registrationNumber)
            ws.value(row, 3, student.section.toString())

            // Date cells — styled one-by-one via single-cell ranges
            dates.forEachIndexed { dIdx, date ->
                val col       = 4 + dIdx
                val isPresent = date in presentDates
                ws.value(row, col, if (isPresent) "P" else "")
                ws.range(row, col, row, col).style()
                    .fillColor(if (isPresent) "E8F5E9" else "FFEBEE")
                    .horizontalAlignment("center")
                    .set()
            }

            // Summary cells
            ws.value(row, sumCol,     totalPresent.toDouble())
            ws.value(row, sumCol + 1, totalAbsent.toDouble())
            ws.value(row, sumCol + 2, "%.1f%%".format(pct))

            // Zebra stripe (even rows, static + summary only — date cols keep P/A colours)
            if (row % 2 == 0) {
                ws.range(row, 0,      row, 3).style().fillColor("F5F8FC").set()
                ws.range(row, sumCol, row, lastCol).style().fillColor("F5F8FC").set()
            }
        }
    }

    // ── Column widths ─────────────────────────────────────────────────────────

    private fun setColumnWidths(ws: Worksheet, dateCols: Int) {
        ws.width(0, 5.0)
        ws.width(1, 28.0)
        ws.width(2, 14.0)
        ws.width(3, 9.0)
        for (i in 0 until dateCols) ws.width(4 + i, 9.0)
        ws.width(4 + dateCols,     9.0)
        ws.width(4 + dateCols + 1, 9.0)
        ws.width(4 + dateCols + 2, 7.0)
    }

    // ── File name ─────────────────────────────────────────────────────────────

    private fun buildFileName(cls: ClassEntity, at: LocalDateTime): String {
        val code = cls.code ?: cls.shortName
        val name = (cls.plainName.ifBlank { cls.shortName })
            .replace(Regex("[^A-Za-z0-9 ._-]"), "").trim().replace(' ', '-')
        val time = at.format(java.time.format.DateTimeFormatter.ofPattern("HHmm"))
        val date = DateUtils.formatFileDate(at.toLocalDate())
        return "${code}_${name}_${time}_$date.xlsx"
    }
}
