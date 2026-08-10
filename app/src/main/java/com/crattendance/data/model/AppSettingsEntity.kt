package com.crattendance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Single-row app settings table. [selectedSection] is "All" or a section
 * letter "A".."F". [biometricEnabled] gates the biometric app-lock.
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: String = SINGLE_ROW_ID,
    val selectedSection: String = "All",
    val biometricEnabled: Boolean = false,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val SINGLE_ROW_ID = "single"
        const val ALL_SECTIONS = "All"
        val SECTIONS = listOf('A', 'B', 'C', 'D', 'E', 'F')
    }
}
