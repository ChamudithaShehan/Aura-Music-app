package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.MediaStoreScanner
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.local.entity.SongEntity
import com.example.data.sample.SampleAudioProvider
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Folder
import com.example.domain.model.Genre
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class MusicRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase
) : MusicRepository {

    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    private val searchHistoryDao = database.searchHistoryDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            rescanLibrary()
        }
    }

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getFavoriteSongs(): Flow<List<Song>> {
        return songDao.getFavoriteSongs().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getRecentlyPlayed(): Flow<List<Song>> {
        return songDao.getRecentlyPlayedSongs().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getMostPlayed(): Flow<List<Song>> {
        return songDao.getMostPlayedSongs().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getNewlyAdded(): Flow<List<Song>> {
        return songDao.getNewlyAddedSongs().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAlbums(): Flow<List<Album>> {
        return getAllSongs().map { songs ->
            songs.groupBy { it.album }.map { (albumName, albumSongs) ->
                val first = albumSongs.first()
                Album(
                    id = first.albumId,
                    name = albumName,
                    artist = first.artist,
                    songCount = albumSongs.size,
                    albumArtUri = first.albumArtUri
                )
            }.sortedBy { it.name }
        }
    }

    override fun getArtists(): Flow<List<Artist>> {
        return getAllSongs().map { songs ->
            songs.groupBy { it.artist }.entries.mapIndexed { index, entry ->
                Artist(
                    id = index.toLong() + 100L,
                    name = entry.key,
                    songCount = entry.value.size,
                    albumCount = entry.value.map { it.album }.distinct().size
                )
            }.sortedBy { it.name }
        }
    }

    override fun getFolders(): Flow<List<Folder>> {
        return getAllSongs().map { songs ->
            songs.mapNotNull { song ->
                if (song.path.isNotEmpty()) {
                    val file = File(song.path)
                    file.parentFile?.let { parent ->
                        parent.absolutePath to parent.name
                    }
                } else null
            }.groupBy { it.first }.map { (folderPath, pairs) ->
                Folder(
                    path = folderPath,
                    name = pairs.first().second,
                    songCount = pairs.size,
                    parentPath = File(folderPath).parent ?: ""
                )
            }.sortedBy { it.name }
        }
    }

    override fun getGenres(): Flow<List<Genre>> {
        return getAllSongs().map { songs ->
            songs.groupBy { it.genre }.entries.mapIndexed { index, entry ->
                Genre(
                    id = index.toLong() + 500L,
                    name = entry.key,
                    songCount = entry.value.size
                )
            }.sortedBy { it.name }
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    isSmart = entity.isSmart,
                    artworkUri = entity.artworkUri
                )
            }
        }
    }

    override fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylist(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getRecentSearches(): Flow<List<String>> {
        return searchHistoryDao.getRecentSearches().map { list -> list.map { it.query } }
    }

    override suspend fun rescanLibrary() {
        // Delete sample songs & WhatsApp audio from DB
        songDao.deleteSampleSongs()
        songDao.deleteWhatsAppAudio()

        val deviceSongs = MediaStoreScanner.scanDeviceAudio(context)

        val combined = deviceSongs
            .filter { !MediaStoreScanner.isWhatsAppAudio(it.path, it.title) }
            .distinctBy { it.id }
        songDao.insertSongs(combined.map { SongEntity.fromDomain(it) })

        // Initialize default playlists if empty
        if (playlistDao.getPlaylistCount() == 0) {
            val defaultPlaylists = listOf("Favorites", "Local Playlist")
            for (pl in defaultPlaylists) {
                val entity = PlaylistEntity(name = pl)
                playlistDao.insertPlaylist(entity)
            }
        }
    }

    override suspend fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        songDao.updateFavorite(songId, isFavorite)
    }

    override suspend fun recordSongPlayed(songId: Long) {
        songDao.recordSongPlayed(songId, System.currentTimeMillis())
    }

    override suspend fun updateLyrics(songId: Long, lyrics: String) {
        songDao.updateLyrics(songId, lyrics)
    }

    override suspend fun updateMetadata(
        songId: Long,
        title: String,
        artist: String,
        album: String,
        genre: String
    ) {
        songDao.updateMetadata(songId, title, artist, album, genre)
    }

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) {
        playlistDao.renamePlaylist(playlistId, newName)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    override suspend fun addSearchQuery(query: String) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = query.trim()))
        }
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearHistory()
    }

    override suspend fun deleteSong(songId: Long) {
        songDao.deleteSong(songId)
    }
}
