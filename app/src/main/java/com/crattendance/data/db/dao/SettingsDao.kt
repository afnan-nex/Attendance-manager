package com.crattendance.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crattendance.data.model.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE id = :id LIMIT 1")
    suspend fun get(id: String = AppSettingsEntity.SINGLE_ROW_ID): AppSettingsEntity?

    @Query("SELECT * FROM app_settings WHERE id = :id LIMIT 1")
    fun observe(id: String = AppSettingsEntity.SINGLE_ROW_ID): Flow<AppSettingsEntity?>
}
