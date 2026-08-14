package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.GoogleDriveBackupHelper
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.SongEntity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * UI State for Backup & Restore screen / dialog
 */
data class BackupUiState(
    val isSignedIn: Boolean = false,
    val userEmail: String? = null,
    val lastBackupTime: String? = null,
    val isLoading: Boolean = false,
    val statusMessage: String = ""
)

/**
 * ViewModel managing Google Drive Backup & Restore operation states,
 * JSON database serialization, and lifecycle updates.
 */
class BackupViewModel(
    application: Application,
    private val database: AppDatabase
) : AndroidViewModel(application) {

    private val TAG = "BackupViewModel"
    val driveHelper = GoogleDriveBackupHelper(application)

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        checkCurrentAccount()
    }

    /**
     * Checks if user is already signed in with required Google Drive scope
     */
    fun checkCurrentAccount() {
        val account = driveHelper.getSignedInAccount()
        if (account != null) {
            _uiState.value = _uiState.value.copy(
                isSignedIn = true,
                userEmail = account.email,
                statusMessage = "Google Account connected"
            )
            loadLastBackupTime()
        } else {
            _uiState.value = _uiState.value.copy(
                isSignedIn = false,
                userEmail = null,
                lastBackupTime = null,
                statusMessage = "Not signed in"
            )
        }
    }

    /**
     * Called when Google Sign-In succeeds from ActivityResultLauncher
     */
    fun onGoogleSignInSuccess(account: GoogleSignInAccount) {
        _uiState.value = _uiState.value.copy(
            isSignedIn = true,
            userEmail = account.email,
            statusMessage = "Signed in as ${account.email}"
        )
        loadLastBackupTime()
    }

    /**
     * Fetches the last backup timestamp from Google Drive appDataFolder
     */
    fun loadLastBackupTime() {
        driveHelper.getLastBackupTime { timestamp, exception ->
            if (timestamp != null) {
                _uiState.value = _uiState.value.copy(
                    lastBackupTime = timestamp,
                    statusMessage = "Last backup retrieved"
                )
            } else if (exception != null) {
                Log.e(TAG, "Error loading backup time", exception)
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Error checking backup time"
                )
            }
        }
    }

    /**
     * Serializes local database (playlists, favorites, settings) to JSON and uploads to Drive
     */
    fun performBackup(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Preparing backup data...")

        viewModelScope.launch {
            try {
                // 1. Export Room DB tables to JSON string on IO thread
                val backupJson = withContext(Dispatchers.IO) {
                    exportDataToJson()
                }

                _uiState.value = _uiState.value.copy(statusMessage = "Uploading to Google Drive...")

                // 2. Upload JSON to Google Drive appDataFolder
                driveHelper.uploadBackup(backupJson) { success, exception ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if (success) {
                        _uiState.value = _uiState.value.copy(statusMessage = "Backup completed successfully!")
                        loadLastBackupTime()
                        onSuccess()
                    } else {
                        val errorMsg = exception?.localizedMessage ?: "Upload failed"
                        _uiState.value = _uiState.value.copy(statusMessage = "Backup failed: $errorMsg")
                        onError(errorMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Backup failed", e)
                _uiState.value = _uiState.value.copy(isLoading = false, statusMessage = "Backup error: ${e.localizedMessage}")
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Downloads backup JSON from Drive appDataFolder and restores into Room DB
     */
    fun performRestore(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Downloading backup from Drive...")

        driveHelper.downloadBackup { jsonString, exception ->
            if (jsonString != null) {
                viewModelScope.launch {
                    try {
                        _uiState.value = _uiState.value.copy(statusMessage = "Restoring local database...")
                        // Restore JSON content into Room database
                        withContext(Dispatchers.IO) {
                            importDataFromJson(jsonString)
                        }
                        _uiState.value = _uiState.value.copy(isLoading = false, statusMessage = "Restore completed successfully!")
                        onSuccess()
                    } catch (e: Exception) {
                        Log.e(TAG, "Restore DB import failed", e)
                        _uiState.value = _uiState.value.copy(isLoading = false, statusMessage = "Restore failed during import")
                        onError("Failed to parse backup data")
                    }
                }
            } else {
                val errorMsg = exception?.localizedMessage ?: "No backup found in Drive"
                _uiState.value = _uiState.value.copy(isLoading = false, statusMessage = errorMsg)
                onError(errorMsg)
            }
        }
    }

    /**
     * Converts local database tables to JSON format
     */
    private suspend fun exportDataToJson(): String {
        val rootJson = JSONObject()

        val songDao = database.songDao()
        val playlistDao = database.playlistDao()

        // 1. Export Favorite Songs
        val allSongs = songDao.getAllSongsDirect()
        val favoriteSongsArray = JSONArray()
        allSongs.filter { it.isFavorite }.forEach { song ->
            val obj = JSONObject().apply {
                put("id", song.id)
                put("title", song.title)
                put("artist", song.artist)
                put("album", song.album)
                put("path", song.path)
            }
            favoriteSongsArray.put(obj)
        }
        rootJson.put("favorites", favoriteSongsArray)

        // 2. Export Playlists & Song Associations
        val playlists = playlistDao.getAllPlaylistsDirect()
        val playlistsArray = JSONArray()
        playlists.forEach { playlist ->
            val playlistObj = JSONObject().apply {
                put("id", playlist.id)
                put("name", playlist.name)
                put("createdAt", playlist.createdAt)
            }
            playlistsArray.put(playlistObj)
        }
        rootJson.put("playlists", playlistsArray)

        val crossRefs = playlistDao.getAllPlaylistSongRefsDirect()
        val refsArray = JSONArray()
        crossRefs.forEach { ref ->
            val refObj = JSONObject().apply {
                put("playlistId", ref.playlistId)
                put("songId", ref.songId)
                put("addedAt", ref.addedAt)
            }
            refsArray.put(refObj)
        }
        rootJson.put("playlistSongRefs", refsArray)

        rootJson.put("timestamp", System.currentTimeMillis())
        rootJson.put("version", 1)

        return rootJson.toString()
    }

    /**
     * Restores playlists and favorites into Room database from JSON string
     */
    private suspend fun importDataFromJson(jsonString: String) {
        val rootJson = JSONObject(jsonString)
        val songDao = database.songDao()
        val playlistDao = database.playlistDao()

        // 1. Restore Favorites
        if (rootJson.has("favorites")) {
            val favoritesArray = rootJson.getJSONArray("favorites")
            for (i in 0 until favoritesArray.length()) {
                val obj = favoritesArray.getJSONObject(i)
                val songId = obj.optLong("id")
                if (songId > 0) {
                    songDao.updateFavorite(songId, true)
                }
            }
        }

        // 2. Restore Playlists & Song Associations
        val playlistIdMap = mutableMapOf<Long, Long>()
        if (rootJson.has("playlists")) {
            val playlistsArray = rootJson.getJSONArray("playlists")
            for (i in 0 until playlistsArray.length()) {
                val playlistObj = playlistsArray.getJSONObject(i)
                val oldId = playlistObj.optLong("id", 0L)
                val name = playlistObj.getString("name")
                val createdAt = playlistObj.optLong("createdAt", System.currentTimeMillis())

                val newId = playlistDao.insertPlaylist(
                    PlaylistEntity(name = name, createdAt = createdAt)
                )
                if (oldId > 0) {
                    playlistIdMap[oldId] = newId
                }
            }
        }

        if (rootJson.has("playlistSongRefs")) {
            val refsArray = rootJson.getJSONArray("playlistSongRefs")
            for (i in 0 until refsArray.length()) {
                val refObj = refsArray.getJSONObject(i)
                val oldPlaylistId = refObj.getLong("playlistId")
                val songId = refObj.getLong("songId")
                val addedAt = refObj.optLong("addedAt", System.currentTimeMillis())
                val targetPlaylistId = playlistIdMap[oldPlaylistId] ?: oldPlaylistId

                playlistDao.addSongToPlaylist(
                    PlaylistSongCrossRef(playlistId = targetPlaylistId, songId = songId, addedAt = addedAt)
                )
            }
        }
    }
}
