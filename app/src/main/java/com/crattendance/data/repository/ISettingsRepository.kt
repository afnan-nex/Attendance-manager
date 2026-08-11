package com.crattendance.data.repository

import com.crattendance.data.model.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    val settings: Flow<AppSettingsEntity>
    suspend fun get(): AppSettingsEntity
    suspend fun setSelectedSection(section: String)
    suspend fun setBiometricEnabled(enabled: Boolean)
}
