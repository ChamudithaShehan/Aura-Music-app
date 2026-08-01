package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val path: String,
    val size: Long,
    val mimeType: String,
    val genre: String,
    val year: Int,
    val trackNumber: Int,
    val isFavorite: Boolean,
    val playCount: Int,
    val lastPlayed: Long,
    val lyrics: String,
    val albumArtUri: String?
) {
    fun toDomain(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            albumId = albumId,
            durationMs = durationMs,
            path = path,
            size = size,
            mimeType = mimeType,
            genre = genre,
            year = year,
            trackNumber = trackNumber,
            isFavorite = isFavorite,
            playCount = playCount,
            lastPlayed = lastPlayed,
            lyrics = lyrics,
            albumArtUri = albumArtUri
        )
    }

    companion object {
        fun fromDomain(song: Song): SongEntity {
            return SongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                albumId = song.albumId,
                durationMs = song.durationMs,
                path = song.path,
                size = song.size,
                mimeType = song.mimeType,
                genre = song.genre,
                year = song.year,
                trackNumber = song.trackNumber,
                isFavorite = song.isFavorite,
                playCount = song.playCount,
                lastPlayed = song.lastPlayed,
                lyrics = song.lyrics,
                albumArtUri = song.albumArtUri
            )
        }
    }
}
