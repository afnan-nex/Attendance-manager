package com.crattendance.utils

import com.crattendance.data.model.ClassEntity
import com.crattendance.data.model.LectureType
import com.crattendance.data.model.StudentEntity
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Serialises/deserialises [StudentEntity] and [ClassEntity] lists to/from a
 * compact human-readable JSON format.
 *
 * ### Student JSON keys
 * `Name`, `CNIC`, `Reg No`, `Section`, `WA Number`, `Ph Number`
 *
 * ### Class JSON keys
 * `Short Name`, `Full Name`, `Lecture Type`, `Day`, `Start Time`, `End Time`,
 * `Teacher Name`, `Location`, `Credit Hours`
 */
object JsonHelper {

    private val TIME_FMT = DateTimeFormatter.ofPattern("h:mm a")

    // ── Students ──────────────────────────────────────────────────────────────

    fun studentsToJson(students: List<StudentEntity>): String {
        val arr = JSONArray()
        students.forEach { s ->
            arr.put(
                JSONObject()
                    .put("Name",       s.name)
                    .put("CNIC",       s.cnic ?: "")
                    .put("Reg No",     s.registrationNumber)
                    .put("Section",    s.section.toString())
                    .put("WA Number",  s.whatsappNumber)
                    .put("Ph Number",  if (s.sameAsWhatsapp) "" else (s.phoneNumber ?: ""))
            )
        }
        return arr.toString(2)  // 2-space pretty print
    }

    /**
     * Parses a JSON array of student objects.
     * Returns a [ParseResult] containing successfully parsed students and any
     * row-level errors (so the caller can show a summary to the user).
     */
    fun studentsFromJson(json: String): ParseResult<StudentEntity> {
        val arr   = JSONArray(json.trim())
        val items = mutableListOf<StudentEntity>()
        val errors = mutableListOf<String>()

        for (i in 0 until arr.length()) {
            runCatching {
                val obj     = arr.getJSONObject(i)
                val section = obj.optString("Section").trim().firstOrNull()
                    ?: throw IllegalArgumentException("Missing Section")
                StudentEntity(
                    name               = obj.optString("Name").trim(),
                    registrationNumber = obj.optString("Reg No").trim(),
                    section            = section,
                    cnic               = obj.optString("CNIC").trim().ifBlank { null },
                    whatsappNumber     = obj.optString("WA Number").trim(),
                    phoneNumber        = obj.optString("Ph Number").trim().ifBlank { null },
                    sameAsWhatsapp     = obj.optString("Ph Number").isBlank()
                )
            }.onSuccess { items.add(it) }
             .onFailure { errors.add("Row ${i + 1}: ${it.message}") }
        }

        return ParseResult(items, errors)
    }

    // ── Classes ───────────────────────────────────────────────────────────────

    fun classesToJson(classes: List<ClassEntity>): String {
        val arr = JSONArray()
        classes.forEach { c ->
            arr.put(
                JSONObject()
                    .put("Short Name",    c.shortName)
                    .put("Full Name",     c.fullNameWithCode)
                    .put("Lecture Type",  c.lectureType.name)
                    .put("Day",           dayOfWeekLabel(c.dayOfWeek))
                    .put("Start Time",    c.startTime.format(TIME_FMT))
                    .put("End Time",      c.endTime.format(TIME_FMT))
                    .put("Teacher Name",  c.teacherName)
                    .put("Location",      c.location)
                    .put("Credit Hours",  c.creditHours)
            )
        }
        return arr.toString(2)
    }

    /**
     * Parses a JSON array of class objects.
     * Returns a [ParseResult] with successfully parsed classes and row errors.
     */
    fun classesFromJson(json: String): ParseResult<ClassEntity> {
        val arr    = JSONArray(json.trim())
        val items  = mutableListOf<ClassEntity>()
        val errors = mutableListOf<String>()

        for (i in 0 until arr.length()) {
            runCatching {
                val obj       = arr.getJSONObject(i)
                val dayLabel  = obj.optString("Day").trim()
                val dayNum    = dayLabelToInt(dayLabel)
                    ?: throw IllegalArgumentException("Unknown day: $dayLabel")
                val lectType  = runCatching {
                    LectureType.valueOf(obj.optString("Lecture Type").trim().uppercase())
                }.getOrElse { LectureType.LECTURE }

                ClassEntity(
                    shortName       = obj.optString("Short Name").trim(),
                    fullNameWithCode= obj.optString("Full Name").trim(),
                    lectureType     = lectType,
                    dayOfWeek       = dayNum,
                    startTime       = parseTime(obj.optString("Start Time").trim()),
                    endTime         = parseTime(obj.optString("End Time").trim()),
                    teacherName     = obj.optString("Teacher Name").trim(),
                    location        = obj.optString("Location").trim(),
                    creditHours     = obj.optInt("Credit Hours", 3)
                )
            }.onSuccess { items.add(it) }
             .onFailure { errors.add("Row ${i + 1}: ${it.message}") }
        }

        return ParseResult(items, errors)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    data class ParseResult<T>(val items: List<T>, val errors: List<String>)

    private fun dayOfWeekLabel(d: Int) = when (d) {
        1 -> "Monday"; 2 -> "Tuesday"; 3 -> "Wednesday"; 4 -> "Thursday"
        5 -> "Friday"; 6 -> "Saturday"; else -> "Sunday"
    }

    private fun dayLabelToInt(label: String): Int? = when (label.lowercase()) {
        "monday"    -> 1; "tuesday"  -> 2; "wednesday" -> 3; "thursday" -> 4
        "friday"    -> 5; "saturday" -> 6; "sunday"    -> 7; else       -> null
    }

    /** Parses "9:00 AM" / "09:00 AM" / "14:30" style strings into [LocalTime]. */
    private fun parseTime(s: String): LocalTime {
        // Try AM/PM first, then 24-hour
        return runCatching { LocalTime.parse(s.uppercase(), DateTimeFormatter.ofPattern("h:mm a")) }
            .getOrElse {
                runCatching { LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm")) }
                    .getOrElse { LocalTime.of(9, 0) }
            }
    }

    // ── Samples ───────────────────────────────────────────────────────────────

    /** Returns a sample students JSON string (2 example rows) as an import template. */
    fun sampleStudentsJson(): String = """
[
  {
    "Name": "Ahmed Ali",
    "CNIC": "3520212345678",
    "Reg No": "Fa23-bcs-001",
    "Section": "A",
    "WA Number": "+923001234567",
    "Ph Number": ""
  },
  {
    "Name": "Sara Khan",
    "CNIC": "",
    "Reg No": "Fa23-bcs-002",
    "Section": "B",
    "WA Number": "+923009876543",
    "Ph Number": "+923119876543"
  }
]
""".trimIndent()

    /** Returns a sample classes JSON string (2 example rows) as an import template. */
    fun sampleClassesJson(): String = """
[
  {
    "Short Name": "CP",
    "Full Name": "CS-101 Computer Programming",
    "Lecture Type": "LECTURE",
    "Day": "Monday",
    "Start Time": "9:00 AM",
    "End Time": "10:00 AM",
    "Teacher Name": "Dr. Ahmed",
    "Location": "Room 101",
    "Credit Hours": 3
  },
  {
    "Short Name": "OOP",
    "Full Name": "CS-201 Object Oriented Programming",
    "Lecture Type": "LECTURE",
    "Day": "Wednesday",
    "Start Time": "11:00 AM",
    "End Time": "12:00 PM",
    "Teacher Name": "Dr. Khan",
    "Location": "Lab 2",
    "Credit Hours": 4
  }
]
""".trimIndent()
}
