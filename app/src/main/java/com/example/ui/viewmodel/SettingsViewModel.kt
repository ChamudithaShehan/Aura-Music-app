package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupInfo
import com.example.data.backup.BackupManager
import com.example.data.local.AudioFxPreferences
import com.example.domain.model.PlaytimeStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val audioFxPreferences: AudioFxPreferences,
    val backupManager: BackupManager? = null
) : ViewModel() {

    val themeMode: StateFlow<String> = audioFxPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val language: StateFlow<String> = audioFxPreferences.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "English")

    val gaplessPlayback: StateFlow<Boolean> = audioFxPreferences.gaplessPlayback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val crossfadeSeconds: StateFlow<Int> = audioFxPreferences.crossfadeSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val pauseOnDisconnect: StateFlow<Boolean> = audioFxPreferences.pauseOnDisconnect
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val lockscreenArt: StateFlow<Boolean> = audioFxPreferences.lockscreenArt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val playtimeStats: StateFlow<PlaytimeStats> = audioFxPreferences.playtimeStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaytimeStats())

    val userEmail: StateFlow<String?> = backupManager?.userEmail
        ?: kotlinx.coroutines.flow.MutableStateFlow(null)

    val isBackingUp: StateFlow<Boolean> = backupManager?.isBackingUp
        ?: kotlinx.coroutines.flow.MutableStateFlow(false)

    val isRestoring: StateFlow<Boolean> = backupManager?.isRestoring
        ?: kotlinx.coroutines.flow.MutableStateFlow(false)

    val backupProgress: StateFlow<Float> = backupManager?.backupProgress
        ?: kotlinx.coroutines.flow.MutableStateFlow(0f)

    val statusMessage: StateFlow<String> = backupManager?.statusMessage
        ?: kotlinx.coroutines.flow.MutableStateFlow("")

    val lastBackupInfo: StateFlow<BackupInfo?> = backupManager?.lastBackupInfo
        ?: kotlinx.coroutines.flow.MutableStateFlow(null)

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            audioFxPreferences.setThemeMode(mode)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            audioFxPreferences.setLanguage(lang)
        }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch {
            audioFxPreferences.setGaplessPlayback(enabled)
        }
    }

    fun setCrossfadeSeconds(seconds: Int) {
        viewModelScope.launch {
            audioFxPreferences.setCrossfadeSeconds(seconds)
        }
    }

    fun setPauseOnDisconnect(enabled: Boolean) {
        viewModelScope.launch {
            audioFxPreferences.setPauseOnDisconnect(enabled)
        }
    }

    fun setLockscreenArt(enabled: Boolean) {
        viewModelScope.launch {
            audioFxPreferences.setLockscreenArt(enabled)
        }
    }

    fun loginGoogleAccount(email: String = "chamudithagame3@gmail.com") {
        backupManager?.loginGoogleAccount(email)
    }

    fun logoutGoogleAccount() {
        backupManager?.logoutGoogleAccount()
    }

    fun performBackup() {
        viewModelScope.launch {
            backupManager?.performCompressedBackup()
        }
    }

    fun performRestore() {
        viewModelScope.launch {
            backupManager?.performRestoreFromGoogleDrive()
        }
    }
}
