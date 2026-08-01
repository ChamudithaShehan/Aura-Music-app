package com.example.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.AppDatabase
import com.example.data.local.AudioFxPreferences
import com.example.data.repository.MusicRepository
import com.example.data.repository.MusicRepositoryImpl
import com.example.player.PlayerManager

class AppContainer(private val context: Context) {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `equalizer_presets` (
                    `name` TEXT NOT NULL,
                    `bandLevels` TEXT NOT NULL,
                    `bassBoostStrength` INTEGER NOT NULL,
                    `virtualizerStrength` INTEGER NOT NULL,
                    `loudnessGain` INTEGER NOT NULL,
                    `isBuiltIn` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`name`)
                )
            """.trimIndent())
        }
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aura_music_db"
        ).addMigrations(MIGRATION_1_2).build()
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

    val visualizerManager: com.example.player.VisualizerManager by lazy {
        com.example.player.VisualizerManager(context)
    }

    val playerManager: PlayerManager by lazy {
        PlayerManager(context, equalizerManager, visualizerManager)
    }

    val backupManager: com.example.data.backup.BackupManager by lazy {
        com.example.data.backup.BackupManager(context, database)
    }
}
