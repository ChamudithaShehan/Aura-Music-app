package com.example.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long = 0L,
    val durationMs: Long = 0L,
    val path: String = "",
    val size: Long = 0L,
    val mimeType: String = "audio/mpeg",
    val genre: String = "Unknown",
    val year: Int = 0,
    val trackNumber: Int = 0,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0L,
    val lyrics: String = "",
    val albumArtUri: String? = null
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: String? = null
)

data class Artist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val albumCount: Int
)

data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0,
    val isSmart: Boolean = false,
    val artworkUri: String? = null
)

data class Folder(
    val path: String,
    val name: String,
    val songCount: Int,
    val parentPath: String = ""
)

data class Genre(
    val id: Long,
    val name: String,
    val songCount: Int
)

enum class RepeatMode {
    OFF, ONE, ALL
}

data class LyricsLine(
    val timeMs: Long,
    val text: String
)
