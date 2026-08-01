package com.example.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.domain.model.Song
import java.io.File

object MediaStoreScanner {

    fun isWhatsAppAudio(path: String, title: String): Boolean {
        val lowerPath = path.lowercase()
        val lowerTitle = title.lowercase()
        return lowerPath.contains("whatsapp") ||
                lowerPath.contains("voice notes") ||
                lowerPath.contains("audio notes") ||
                lowerPath.contains("whatsapp audio") ||
                lowerPath.contains("whatsapp voice") ||
                lowerPath.contains("opus") ||
                lowerTitle.startsWith("ptt-") ||
                lowerTitle.startsWith("aud-") ||
                lowerTitle.contains("whatsapp")
    }

    fun scanDeviceAudio(context: Context): List<Song> {
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

                val artBaseUri = Uri.parse("content://media/external/audio/albumart")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val path = cursor.getString(dataColumn) ?: ""
                    val size = cursor.getLong(sizeColumn)
                    val mimeType = cursor.getString(mimeColumn) ?: "audio/mpeg"
                    val year = cursor.getInt(yearColumn)
                    val track = cursor.getInt(trackColumn)

                    val albumArtUri = try {
                        ContentUris.withAppendedId(artBaseUri, albumId).toString()
                    } catch (e: Exception) {
                        null
                    }

                    if (path.isNotEmpty() && File(path).exists() && !isWhatsAppAudio(path, title)) {
                        songs.add(
                            Song(
                                id = id,
                                title = title,
                                artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                                album = if (album == "<unknown>") "Unknown Album" else album,
                                albumId = albumId,
                                durationMs = duration,
                                path = path,
                                size = size,
                                mimeType = mimeType,
                                genre = detectGenreFromPath(path),
                                year = year,
                                trackNumber = track,
                                isFavorite = false,
                                albumArtUri = albumArtUri
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return songs
    }

    private fun detectGenreFromPath(path: String): String {
        val lower = path.lowercase()
        return when {
            lower.contains("pop") -> "Pop"
            lower.contains("rock") -> "Rock"
            lower.contains("hiphop") || lower.contains("rap") -> "Hip-Hop"
            lower.contains("jazz") -> "Jazz"
            lower.contains("classical") -> "Classical"
            lower.contains("edm") || lower.contains("electronic") -> "Electronic"
            lower.contains("ambient") || lower.contains("chill") -> "Ambient"
            else -> "General"
        }
    }
}
