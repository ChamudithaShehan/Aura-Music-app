package com.example.player

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.domain.model.AudioEqualizerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class EqualizerManager(private val context: Context) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    
    private var currentSessionId: Int = -1
    
    private val _state = MutableStateFlow(AudioEqualizerState())
    val state = _state.asStateFlow()

    fun init(sessionId: Int) {
        if (sessionId == -1 || sessionId == 0) return
        if (currentSessionId == sessionId) return
        
        release()
        currentSessionId = sessionId
        
        try {
            equalizer = Equalizer(0, sessionId)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error initializing Equalizer", e)
            equalizer = null
        }

        try {
            bassBoost = BassBoost(0, sessionId)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error initializing BassBoost", e)
            bassBoost = null
        }

        try {
            virtualizer = Virtualizer(0, sessionId)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error initializing Virtualizer", e)
            virtualizer = null
        }

        try {
            loudnessEnhancer = LoudnessEnhancer(sessionId)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error initializing LoudnessEnhancer", e)
            loudnessEnhancer = null
        }

        applyState(_state.value)
    }

    fun applyState(newState: AudioEqualizerState) {
        _state.value = newState
        val enabled = newState.isEnabled

        equalizer?.let { eq ->
            try {
                eq.enabled = enabled
                if (enabled) {
                    val numBands = eq.numberOfBands.toInt()
                    val bandRange = eq.bandLevelRange
                    val minLevel = bandRange[0].toInt()
                    val maxLevel = bandRange[1].toInt()

                    val maxBoost = newState.bandLevels.maxOrNull() ?: 0
                    val headroomOffset = if (maxBoost > 0) -maxBoost * 50 else 0 // in milliBels

                    newState.bandLevels.forEachIndexed { index, level ->
                        if (index < numBands) {
                            val milliBel = (level * 100 + headroomOffset).coerceIn(minLevel, maxLevel).toShort()
                            eq.setBandLevel(index.toShort(), milliBel)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("EqualizerManager", "Error applying Equalizer state", e)
            }
        }

        bassBoost?.let { bb ->
            try {
                bb.enabled = enabled && newState.bassBoostEnabled
                if (bb.enabled && bb.strengthSupported) {
                    bb.setStrength(newState.bassBoostStrength.toShort())
                }
            } catch (e: Exception) {
                Log.e("EqualizerManager", "Error applying BassBoost state", e)
            }
        }

        virtualizer?.let { virt ->
            try {
                virt.enabled = enabled && newState.virtualizerEnabled
                if (virt.enabled && virt.strengthSupported) {
                    virt.setStrength(newState.virtualizerStrength.toShort())
                }
            } catch (e: Exception) {
                Log.e("EqualizerManager", "Error applying Virtualizer state", e)
            }
        }

        loudnessEnhancer?.let { le ->
            try {
                le.enabled = enabled && newState.loudnessEnabled
                if (le.enabled) {
                    le.setTargetGain(newState.loudnessGain.coerceAtMost(800))
                }
            } catch (e: Exception) {
                Log.e("EqualizerManager", "Error applying LoudnessEnhancer state", e)
            }
        }
    }

    fun release() {
        try { equalizer?.apply { enabled = false; release() } } catch (e: Exception) { Log.e("EqualizerManager", "Error releasing Equalizer", e) }
        try { bassBoost?.apply { enabled = false; release() } } catch (e: Exception) { Log.e("EqualizerManager", "Error releasing BassBoost", e) }
        try { virtualizer?.apply { enabled = false; release() } } catch (e: Exception) { Log.e("EqualizerManager", "Error releasing Virtualizer", e) }
        try { loudnessEnhancer?.apply { enabled = false; release() } } catch (e: Exception) { Log.e("EqualizerManager", "Error releasing LoudnessEnhancer", e) }
        
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        currentSessionId = -1
    }
}
