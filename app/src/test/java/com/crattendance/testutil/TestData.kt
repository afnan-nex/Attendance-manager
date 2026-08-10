package com.crattendance.testutil

import com.crattendance.data.model.ClassEntity
import com.crattendance.data.model.LectureType
import com.crattendance.data.model.StudentEntity
import java.time.LocalTime

/** Test factories for entities. */
fun classEntity(
    id: String = "c1",
    shortName: String = "CS",
    fullNameWithCode: String = "CS-101 Computer Programming",
    lectureType: LectureType = LectureType.LECTURE,
    dayOfWeek: Int = 1,
    startTime: LocalTime = LocalTime.of(9, 0),
    endTime: LocalTime = LocalTime.of(10, 30),
    teacherName: String = "Dr. Khan",
    location: String = "Room 101",
    creditHours: Int = 3,
    isHidden: Boolean = false
) = ClassEntity(
    id = id,
    shortName = shortName,
    fullNameWithCode = fullNameWithCode,
    lectureType = lectureType,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    teacherName = teacherName,
    location = location,
    creditHours = creditHours,
    isHidden = isHidden
)

fun studentEntity(
    id: String = "s1",
    name: String = "Ali",
    registrationNumber: String = "22P-0001",
    section: Char = 'A',
    cnic: String? = "4210123456789",
    whatsappNumber: String = "03001234567",
    phoneNumber: String? = null,
    sameAsWhatsapp: Boolean = true,
    orderIndex: Int = 0
) = StudentEntity(
    id = id,
    name = name,
    registrationNumber = registrationNumber,
    section = section,
    cnic = cnic,
    whatsappNumber = whatsappNumber,
    phoneNumber = phoneNumber,
    sameAsWhatsapp = sameAsWhatsapp,
    orderIndex = orderIndex
)
