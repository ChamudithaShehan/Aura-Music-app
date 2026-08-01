package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.EqualizerPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EqualizerDao {
    @Query("SELECT * FROM equalizer_presets")
    fun getAllPresets(): Flow<List<EqualizerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqualizerPresetEntity)

    @Delete
    suspend fun deletePreset(preset: EqualizerPresetEntity)

    @Query("SELECT * FROM equalizer_presets WHERE name = :name")
    suspend fun getPresetByName(name: String): EqualizerPresetEntity?
}
