package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equalizer_presets")
data class EqualizerPresetEntity(
    @PrimaryKey val name: String,
    val bandLevels: String, // Comma separated values or JSON
    val bassBoostStrength: Int,
    val virtualizerStrength: Int,
    val loudnessGain: Int,
    val isBuiltIn: Boolean = false
)
