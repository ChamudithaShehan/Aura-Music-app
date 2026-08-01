package com.example.data.repository

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Folder
import com.example.domain.model.Genre
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getFavoriteSongs(): Flow<List<Song>>
    fun getRecentlyPlayed(): Flow<List<Song>>
    fun getMostPlayed(): Flow<List<Song>>
    fun getNewlyAdded(): Flow<List<Song>>
    fun getAlbums(): Flow<List<Album>>
    fun getArtists(): Flow<List<Artist>>
    fun getFolders(): Flow<List<Folder>>
    fun getGenres(): Flow<List<Genre>>
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    fun getRecentSearches(): Flow<List<String>>

    suspend fun rescanLibrary()
    suspend fun toggleFavorite(songId: Long, isFavorite: Boolean)
    suspend fun recordSongPlayed(songId: Long)
    suspend fun updateLyrics(songId: Long, lyrics: String)
    suspend fun updateMetadata(songId: Long, title: String, artist: String, album: String, genre: String)
    suspend fun createPlaylist(name: String): Long
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    suspend fun addSearchQuery(query: String)
    suspend fun clearSearchHistory()
    suspend fun deleteSong(songId: Long)
}
