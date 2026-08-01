package com.example.ui.viewmodel

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.di.AppContainer

class ViewModelFactory(
    private val appContainer: AppContainer,
    owner: SavedStateRegistryOwner
) : AbstractSavedStateViewModelFactory(owner, null) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return when {
            modelClass.isAssignableFrom(MusicViewModel::class.java) -> {
                MusicViewModel(appContainer.musicRepository, handle) as T
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
