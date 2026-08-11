package com.crattendance.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * A student enrolled in the class. [orderIndex] provides a global drag-reorder
 * ordering; the Attendance screen filters by section but preserves this order.
 */
@Entity(
    tableName = "students",
    indices = [Index(value = ["registrationNumber"], unique = true)]
)
data class StudentEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val registrationNumber: String,
    val section: Char,
    val cnic: String? = null,
    val whatsappNumber: String = "",
    val phoneNumber: String? = null,
    val sameAsWhatsapp: Boolean = true,
    val orderIndex: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /** Effective phone number used by dialer/open intents. */
    val effectivePhone: String
        get() = if (sameAsWhatsapp) whatsappNumber else (phoneNumber ?: whatsappNumber)
}
