package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AudioFxPreferences
import com.example.data.local.dao.EqualizerDao
import com.example.data.local.entity.EqualizerPresetEntity
import com.example.domain.model.AudioEqualizerState
import com.example.player.PlayerManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EqualizerViewModel(
    private val playerManager: PlayerManager,
    private val audioFxPreferences: AudioFxPreferences,
    private val equalizerDao: EqualizerDao
) : ViewModel() {

    val equalizerState: StateFlow<AudioEqualizerState> = playerManager.equalizerState
    
    // Audio Visualization data
    val visualizerBands: StateFlow<FloatArray> = playerManager.visualizerBands
    val waveform: StateFlow<ByteArray> = playerManager.waveform
    val rms: StateFlow<Float> = playerManager.rms

    private val builtInPresets = mapOf(
        "Normal" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "Rock" to listOf(4, 3, 2, 0, -1, -2, -1, 1, 3, 4),
        "Pop" to listOf(-1, 0, 1, 2, 3, 3, 2, 1, 0, -1),
        "Jazz" to listOf(3, 2, 1, 1, -1, -1, 0, 1, 2, 3),
        "Dance" to listOf(5, 4, 2, 0, 1, 2, 3, 4, 5, 1),
        "Hip Hop" to listOf(5, 3, 1, 2, 0, 0, 1, 2, 4, 5),
        "Classical" to listOf(4, 3, 2, 1, 0, 0, 0, 1, 2, 3),
        "Acoustic" to listOf(3, 2, 1, 0, 1, 2, 2, 3, 3, 2),
        "Electronic" to listOf(4, 3, 1, 2, 3, 2, 1, 2, 4, 5),
        "Metal" to listOf(5, 4, -2, -3, -1, 0, 2, 3, 4, 5),
        "Podcast" to listOf(-3, -2, -1, 1, 4, 4, 3, 2, 1, 0),
        "Movie" to listOf(3, 1, 0, 0, 1, 2, 2, 3, 2, 1),
        "Bass Boost" to listOf(6, 5, 4, 2, 1, 0, 0, 0, 0, 0),
        "Treble Boost" to listOf(0, 0, 0, 0, 0, 1, 2, 4, 6, 8),
        "Gaming" to listOf(4, 2, 0, 1, 2, 3, 3, 4, 3, 2),
        "Night Mode" to listOf(-2, -3, -2, -1, 1, 1, 0, -1, -2, -3)
    )

    val customPresets: StateFlow<List<EqualizerPresetEntity>> = equalizerDao.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPresetNames: StateFlow<List<String>> = customPresets.map { custom ->
        builtInPresets.keys.toList() + custom.map { it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), builtInPresets.keys.toList())

    init {
        viewModelScope.launch {
            audioFxPreferences.equalizerState.collect { savedState ->
                playerManager.setEqualizerState(savedState)
            }
        }
    }

    fun toggleMaster(enabled: Boolean) {
        updateState(equalizerState.value.copy(isEnabled = enabled))
    }

    fun selectPreset(name: String) {
        val builtIn = builtInPresets[name]
        if (builtIn != null) {
            updateState(equalizerState.value.copy(
                presetName = name,
                bandLevels = builtIn,
                isCustom = false
            ))
        } else {
            viewModelScope.launch {
                val custom = equalizerDao.getPresetByName(name)
                custom?.let {
                    updateState(equalizerState.value.copy(
                        presetName = it.name,
                        bandLevels = it.bandLevels.split(",").map { b -> b.toInt() },
                        bassBoostStrength = it.bassBoostStrength,
                        virtualizerStrength = it.virtualizerStrength,
                        loudnessGain = it.loudnessGain,
                        isCustom = true
                    ))
                }
            }
        }
    }

    fun setBandLevel(index: Int, level: Int) {
        val levels = equalizerState.value.bandLevels.toMutableList()
        if (index in levels.indices) {
            levels[index] = level
            updateState(equalizerState.value.copy(
                bandLevels = levels,
                presetName = "Custom",
                isCustom = true
            ))
        }
    }

    fun toggleBassBoost(enabled: Boolean) {
        updateState(equalizerState.value.copy(bassBoostEnabled = enabled))
    }

    fun setBassBoostStrength(strength: Int) {
        updateState(equalizerState.value.copy(bassBoostStrength = strength))
    }

    fun toggleVirtualizer(enabled: Boolean) {
        updateState(equalizerState.value.copy(virtualizerEnabled = enabled))
    }

    fun setVirtualizerStrength(strength: Int) {
        updateState(equalizerState.value.copy(virtualizerStrength = strength))
    }

    fun toggleLoudness(enabled: Boolean) {
        updateState(equalizerState.value.copy(loudnessEnabled = enabled))
    }

    fun setLoudnessGain(gain: Int) {
        updateState(equalizerState.value.copy(loudnessGain = gain))
    }

    fun reset() {
        updateState(AudioEqualizerState(isEnabled = equalizerState.value.isEnabled))
    }

    fun saveCustomPreset(name: String) {
        val state = equalizerState.value
        viewModelScope.launch {
            equalizerDao.insertPreset(
                EqualizerPresetEntity(
                    name = name,
                    bandLevels = state.bandLevels.joinToString(","),
                    bassBoostStrength = state.bassBoostStrength,
                    virtualizerStrength = state.virtualizerStrength,
                    loudnessGain = state.loudnessGain
                )
            )
            updateState(state.copy(presetName = name, isCustom = true))
        }
    }

    fun deletePreset(name: String) {
        viewModelScope.launch {
            val preset = equalizerDao.getPresetByName(name)
            preset?.let {
                equalizerDao.deletePreset(it)
                if (equalizerState.value.presetName == name) {
                    selectPreset("Normal")
                }
            }
        }
    }

    private fun updateState(state: AudioEqualizerState) {
        playerManager.setEqualizerState(state)
        viewModelScope.launch {
            audioFxPreferences.updateEqualizer(state)
        }
    }
}
