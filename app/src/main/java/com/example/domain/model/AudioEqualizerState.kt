package com.example.domain.model

data class AudioEqualizerState(
    val isEnabled: Boolean = false,
    val presetName: String = "Normal",
    val bandLevels: List<Int> = List(10) { 0 }, // 10 bands in dB (-15 to +15)
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 0, // 0 to 1000
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 0, // 0 to 1000
    val loudnessEnabled: Boolean = false,
    val loudnessGain: Int = 0, // 0 to 1000 (representing gain in mB)
    val isCustom: Boolean = false
)

data class EqualizerBand(
    val index: Int,
    val frequencyLabel: String,
    val gain: Int
)
