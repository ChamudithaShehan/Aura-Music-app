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
        if (sessionId <= 0) return
        if (currentSessionId == sessionId && equalizer != null) {
            applyState(_state.value)
            return
        }
        
        release()
        currentSessionId = sessionId
        
        try {
            // Priority 0, audioSession = sessionId
            equalizer = Equalizer(0, sessionId)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error initializing Equalizer for session $sessionId", e)
            equalizer = null
        }

        try {
            bassBoost = BassBoost(0, sessionId)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error initializing BassBoost for session $sessionId", e)
            bassBoost = null
        }

        try {
            virtualizer = Virtualizer(0, sessionId)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error initializing Virtualizer for session $sessionId", e)
            virtualizer = null
        }

        try {
            loudnessEnhancer = LoudnessEnhancer(sessionId)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error initializing LoudnessEnhancer for session $sessionId", e)
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
                    val range = eq.bandLevelRange // [minMilliBel, maxMilliBel]
                    val minLevelMb = range[0].toInt()
                    val maxLevelMb = range[1].toInt()
                    val uiLevels = newState.bandLevels // 10 bands (-15dB to +15dB)

                    for (i in 0 until numBands) {
                        // Map hardware band index `i` across the 10 UI bands
                        val uiIndex = if (numBands == 10) i else ((i.toFloat() / (numBands - 1)) * 9).toInt().coerceIn(0, 9)
                        val requestedDb = uiLevels.getOrElse(uiIndex) { 0 }
                        val milliBel = (requestedDb * 100).coerceIn(minLevelMb, maxLevelMb).toShort()
                        eq.setBandLevel(i.toShort(), milliBel)
                    }
                }
            } catch (e: Exception) {
                Log.e("EqualizerManager", "Error applying Equalizer state", e)
            }
        }

        bassBoost?.let { bb ->
            try {
                val bbEnabled = enabled && newState.bassBoostEnabled
                bb.enabled = bbEnabled
                if (bbEnabled) {
                    if (bb.strengthSupported) {
                        val strength = newState.bassBoostStrength.coerceIn(0, 1000).toShort()
                        bb.setStrength(strength)
                    }
                }
            } catch (e: Exception) {
                Log.e("EqualizerManager", "Error applying BassBoost state", e)
            }
        }

        virtualizer?.let { virt ->
            try {
                val virtEnabled = enabled && newState.virtualizerEnabled
                virt.enabled = virtEnabled
                if (virtEnabled) {
                    if (virt.strengthSupported) {
                        val strength = newState.virtualizerStrength.coerceIn(0, 1000).toShort()
                        virt.setStrength(strength)
                    }
                }
            } catch (e: Exception) {
                Log.e("EqualizerManager", "Error applying Virtualizer state", e)
            }
        }

        loudnessEnhancer?.let { le ->
            try {
                val leEnabled = enabled && newState.loudnessEnabled
                le.enabled = leEnabled
                if (leEnabled) {
                    val targetGainMb = newState.loudnessGain.coerceIn(0, 1000)
                    le.setTargetGain(targetGainMb)
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


