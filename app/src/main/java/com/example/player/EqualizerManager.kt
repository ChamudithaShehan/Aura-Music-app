package com.example.player

import android.content.Context
import android.media.audiofx.AudioEffect
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
            bassBoost = BassBoost(0, sessionId)
            virtualizer = Virtualizer(0, sessionId)
            loudnessEnhancer = LoudnessEnhancer(sessionId)
            
            applyState(_state.value)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error initializing audio effects", e)
        }
    }

    fun applyState(newState: AudioEqualizerState) {
        _state.value = newState
        
        val eq = equalizer ?: return
        val bb = bassBoost ?: return
        val virt = virtualizer ?: return
        val le = loudnessEnhancer ?: return

        try {
            val enabled = newState.isEnabled
            
            eq.enabled = enabled
            if (enabled) {
                val numBands = eq.numberOfBands.toInt()
                val bandRange = eq.bandLevelRange
                val minLevel = bandRange[0].toInt()
                val maxLevel = bandRange[1].toInt()
                
                newState.bandLevels.forEachIndexed { index, level ->
                    if (index < numBands) {
                        // Level is in dB, convert to milliBel
                        val milliBel = (level * 100).coerceIn(minLevel, maxLevel).toShort()
                        eq.setBandLevel(index.toShort(), milliBel)
                    }
                }
            }

            bb.enabled = enabled && newState.bassBoostEnabled
            if (bb.enabled) {
                bb.setStrength(newState.bassBoostStrength.toShort())
            }

            virt.enabled = enabled && newState.virtualizerEnabled
            if (virt.enabled) {
                virt.setStrength(newState.virtualizerStrength.toShort())
            }

            le.enabled = enabled && newState.loudnessEnabled
            if (le.enabled) {
                le.setTargetGain(newState.loudnessGain)
            }
            
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error applying equalizer state", e)
        }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        loudnessEnhancer?.release()
        
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        currentSessionId = -1
    }
}
