package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.EqualizerDao
import com.example.data.local.dao.PinnedItemDao
import com.example.data.local.dao.PlaylistDao
import com.example.data.local.dao.SearchHistoryDao
import com.example.data.local.dao.SongDao
import com.example.data.local.entity.EqualizerPresetEntity
import com.example.data.local.entity.PinnedItemEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        SearchHistoryEntity::class,
        PinnedItemEntity::class,
        EqualizerPresetEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun pinnedItemDao(): PinnedItemDao
    abstract fun equalizerDao(): EqualizerDao
}
