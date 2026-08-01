package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY title ASC")
    suspend fun getAllSongsDirect(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT 30")
    fun getRecentlyPlayedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT 30")
    fun getMostPlayedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY id DESC LIMIT 30")
    fun getNewlyAddedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: Long, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :songId")
    suspend fun recordSongPlayed(songId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE songs SET lyrics = :lyrics WHERE id = :songId")
    suspend fun updateLyrics(songId: Long, lyrics: String)

    @Query("UPDATE songs SET title = :title, artist = :artist, album = :album, genre = :genre WHERE id = :songId")
    suspend fun updateMetadata(songId: Long, title: String, artist: String, album: String, genre: String)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSong(songId: Long)

    @Query("DELETE FROM songs WHERE path LIKE '%sample_music%' OR id >= 9000")
    suspend fun deleteSampleSongs()

    @Query("DELETE FROM songs WHERE LOWER(path) LIKE '%whatsapp%' OR LOWER(path) LIKE '%voice notes%' OR LOWER(path) LIKE '%audio notes%' OR LOWER(path) LIKE '%.opus' OR LOWER(title) LIKE 'ptt-%' OR LOWER(title) LIKE 'aud-%'")
    suspend fun deleteWhatsAppAudio()
}
