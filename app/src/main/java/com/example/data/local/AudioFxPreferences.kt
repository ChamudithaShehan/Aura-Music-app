package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AudioEqualizerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "aura_settings")

class AudioFxPreferences(context: Context) {

    private val appContext = context.applicationContext

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val EQ_PRESET = stringPreferencesKey("eq_preset")
        val BASS_BOOST_ENABLED = booleanPreferencesKey("bass_boost_enabled")
        val BASS_BOOST_STRENGTH = intPreferencesKey("bass_boost_strength")
        val VIRTUALIZER_ENABLED = booleanPreferencesKey("virtualizer_enabled")
        val VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")
        val LOUDNESS_ENABLED = booleanPreferencesKey("loudness_enabled")
        val LOUDNESS_GAIN = intPreferencesKey("loudness_gain")
        val BAND_LEVELS = stringPreferencesKey("band_levels")
        val IS_CUSTOM = booleanPreferencesKey("is_custom")
        
        val LANGUAGE = stringPreferencesKey("language")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val PAUSE_ON_DISCONNECT = booleanPreferencesKey("pause_on_disconnect")
        val LOCKSCREEN_ART = booleanPreferencesKey("lockscreen_art")
    }

    val themeMode: Flow<String> = appContext.dataStore.data.map {
        it[Keys.THEME_MODE] ?: "SYSTEM"
    }

    val language: Flow<String> = appContext.dataStore.data.map {
        it[Keys.LANGUAGE] ?: "English"
    }

    val gaplessPlayback: Flow<Boolean> = appContext.dataStore.data.map {
        it[Keys.GAPLESS_PLAYBACK] ?: true
    }

    val crossfadeSeconds: Flow<Int> = appContext.dataStore.data.map {
        it[Keys.CROSSFADE_SECONDS] ?: 2
    }

    val pauseOnDisconnect: Flow<Boolean> = appContext.dataStore.data.map {
        it[Keys.PAUSE_ON_DISCONNECT] ?: true
    }

    val lockscreenArt: Flow<Boolean> = appContext.dataStore.data.map {
        it[Keys.LOCKSCREEN_ART] ?: true
    }

    val equalizerState: Flow<AudioEqualizerState> = appContext.dataStore.data.map { prefs ->
        val bandsStr = prefs[Keys.BAND_LEVELS] ?: "0,0,0,0,0,0,0,0,0,0"
        val bands = try {
            bandsStr.split(",").map { it.toInt() }
        } catch (e: Exception) {
            List(10) { 0 }
        }

        AudioEqualizerState(
            isEnabled = prefs[Keys.EQ_ENABLED] ?: false,
            presetName = prefs[Keys.EQ_PRESET] ?: "Normal",
            bandLevels = if (bands.size == 10) bands else List(10) { 0 },
            bassBoostEnabled = prefs[Keys.BASS_BOOST_ENABLED] ?: false,
            bassBoostStrength = prefs[Keys.BASS_BOOST_STRENGTH] ?: 0,
            virtualizerEnabled = prefs[Keys.VIRTUALIZER_ENABLED] ?: false,
            virtualizerStrength = prefs[Keys.VIRTUALIZER_STRENGTH] ?: 0,
            loudnessEnabled = prefs[Keys.LOUDNESS_ENABLED] ?: false,
            loudnessGain = prefs[Keys.LOUDNESS_GAIN] ?: 0,
            isCustom = prefs[Keys.IS_CUSTOM] ?: false
        )
    }

    suspend fun setThemeMode(mode: String) {
        appContext.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setLanguage(lang: String) {
        appContext.dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        appContext.dataStore.edit { it[Keys.GAPLESS_PLAYBACK] = enabled }
    }

    suspend fun setCrossfadeSeconds(sec: Int) {
        appContext.dataStore.edit { it[Keys.CROSSFADE_SECONDS] = sec }
    }

    suspend fun setPauseOnDisconnect(enabled: Boolean) {
        appContext.dataStore.edit { it[Keys.PAUSE_ON_DISCONNECT] = enabled }
    }

    suspend fun setLockscreenArt(enabled: Boolean) {
        appContext.dataStore.edit { it[Keys.LOCKSCREEN_ART] = enabled }
    }

    suspend fun updateEqualizer(state: AudioEqualizerState) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.EQ_ENABLED] = state.isEnabled
            prefs[Keys.EQ_PRESET] = state.presetName
            prefs[Keys.BAND_LEVELS] = state.bandLevels.joinToString(",")
            prefs[Keys.BASS_BOOST_ENABLED] = state.bassBoostEnabled
            prefs[Keys.BASS_BOOST_STRENGTH] = state.bassBoostStrength
            prefs[Keys.VIRTUALIZER_ENABLED] = state.virtualizerEnabled
            prefs[Keys.VIRTUALIZER_STRENGTH] = state.virtualizerStrength
            prefs[Keys.LOUDNESS_ENABLED] = state.loudnessEnabled
            prefs[Keys.LOUDNESS_GAIN] = state.loudnessGain
            prefs[Keys.IS_CUSTOM] = state.isCustom
        }
    }
}
