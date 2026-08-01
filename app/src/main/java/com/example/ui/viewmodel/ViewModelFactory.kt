package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.di.AppContainer

class ViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MusicViewModel::class.java) -> {
                MusicViewModel(appContainer.musicRepository) as T
            }
            modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                PlayerViewModel(appContainer.playerManager, appContainer.musicRepository) as T
            }
            modelClass.isAssignableFrom(EqualizerViewModel::class.java) -> {
                EqualizerViewModel(
                    appContainer.playerManager,
                    appContainer.audioFxPreferences,
                    appContainer.database.equalizerDao()
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(appContainer.audioFxPreferences, appContainer.backupManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
