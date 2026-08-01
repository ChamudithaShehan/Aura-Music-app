package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MusicRepository
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Folder
import com.example.domain.model.Genre
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val musicRepository: MusicRepository
) : ViewModel() {

    val allSongs: StateFlow<List<Song>> = musicRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = musicRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = musicRepository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = musicRepository.getMostPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newlyAdded: StateFlow<List<Song>> = musicRepository.getNewlyAdded()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = musicRepository.getAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = musicRepository.getArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<Folder>> = musicRepository.getFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val genres: StateFlow<List<Genre>> = musicRepository.getGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = musicRepository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<String>> = musicRepository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            viewModelScope.launch {
                musicRepository.searchSongs(query).collect { results ->
                    _searchResults.value = results
                }
            }
        }
    }

    fun submitSearch(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                musicRepository.addSearchQuery(query)
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            musicRepository.clearSearchHistory()
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id, !song.isFavorite)
        }
    }

    fun createPlaylist(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                musicRepository.createPlaylist(name)
            }
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        if (newName.isNotBlank()) {
            viewModelScope.launch {
                musicRepository.renamePlaylist(playlistId, newName)
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }

    fun getPlaylistSongs(playlistId: Long): kotlinx.coroutines.flow.Flow<List<Song>> {
        return musicRepository.getPlaylistSongs(playlistId)
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            musicRepository.rescanLibrary()
        }
    }

    fun updateMetadata(songId: Long, title: String, artist: String, album: String, genre: String) {
        viewModelScope.launch {
            musicRepository.updateMetadata(songId, title, artist, album, genre)
        }
    }

    fun deleteSong(songId: Long) {
        viewModelScope.launch {
            musicRepository.deleteSong(songId)
        }
    }
}
