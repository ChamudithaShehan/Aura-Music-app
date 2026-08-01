package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSmart: Boolean = false,
    val artworkUri: String? = null
)

@Entity(tableName = "playlist_song_cross_ref", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "pinned_items")
data class PinnedItemEntity(
    @PrimaryKey val id: String, // e.g. "ALBUM_123" or "ARTIST_456" or "PLAYLIST_789"
    val type: String, // "ALBUM", "ARTIST", "PLAYLIST"
    val title: String,
    val subtitle: String,
    val artworkUri: String? = null
)
