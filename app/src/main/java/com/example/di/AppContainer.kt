package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.AudioFxPreferences
import com.example.data.repository.MusicRepository
import com.example.data.repository.MusicRepositoryImpl
import com.example.player.PlayerManager

class AppContainer(private val context: Context) {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aura_music_db"
        ).fallbackToDestructiveMigration().build()
    }

    val musicRepository: MusicRepository by lazy {
        MusicRepositoryImpl(context, database)
    }

    val audioFxPreferences: AudioFxPreferences by lazy {
        AudioFxPreferences(context)
    }

    val equalizerManager: com.example.player.EqualizerManager by lazy {
        com.example.player.EqualizerManager(context)
    }

    val playerManager: PlayerManager by lazy {
        PlayerManager(context, equalizerManager)
    }

    val backupManager: com.example.data.backup.BackupManager by lazy {
        com.example.data.backup.BackupManager(context, database)
    }
}
