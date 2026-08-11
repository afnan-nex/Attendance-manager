package com.crattendance.data.repository

import com.crattendance.data.db.dao.SettingsDao
import com.crattendance.data.model.AppSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

/** Repository for [AppSettingsEntity]. Open for test fakes. */
open class SettingsRepository(private val settingsDao: SettingsDao) : ISettingsRepository {

    private val defaults = AppSettingsEntity()

    open override val settings: Flow<AppSettingsEntity> = settingsDao.observe().map { it ?: defaults }

    open override suspend fun get(): AppSettingsEntity = settingsDao.get() ?: defaults

    open override suspend fun setSelectedSection(section: String) {
        settingsDao.upsert(current().copy(selectedSection = section, lastUpdated = LocalDateTime.now()))
    }

    open override suspend fun setBiometricEnabled(enabled: Boolean) {
        settingsDao.upsert(current().copy(biometricEnabled = enabled, lastUpdated = LocalDateTime.now()))
    }

    private suspend fun current(): AppSettingsEntity = settingsDao.get() ?: defaults
}
