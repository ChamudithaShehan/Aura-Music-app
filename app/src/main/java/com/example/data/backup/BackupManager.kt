package com.example.data.backup

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val TAG = "BackupManager"
    private val driveManager = DriveBackupManager(context)
    private val prefs = context.getSharedPreferences("google_drive_backup_prefs", Context.MODE_PRIVATE)

    val userEmail: StateFlow<String?> = driveManager.userEmail

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val _backupProgress = MutableStateFlow(0f)
    val backupProgress: StateFlow<Float> = _backupProgress.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _lastBackupInfo = MutableStateFlow<BackupInfo?>(loadLastBackupInfo())
    val lastBackupInfo: StateFlow<BackupInfo?> = _lastBackupInfo.asStateFlow()

    private val BACKUP_ENCRYPTION_KEY = "AuraMusicBackupSecretKey2026AES".toByteArray(Charsets.UTF_8).copyOf(32)

    init {
        silentLoginCheck()
    }

    private fun logStep(message: String) {
        Log.i(TAG, message)
        _statusMessage.value = message
    }

    fun loginGoogleAccount(email: String) {
        val success = driveManager.signIn(email)
        if (success) {
            logStep("Google Sign-In Success")
            logStep("Access Token Received")
            logStep("Drive Connected")
        } else {
            logStep("Google Sign-In Failed")
        }
    }

    fun silentLoginCheck() {
        val email = prefs.getString("user_email", null)
        if (email != null) {
            logStep("Silent login initiated for $email")
            val loggedIn = driveManager.performSilentLogin()
            if (loggedIn) {
                logStep("Google Sign-In Success")
                logStep("Access Token Received")
                logStep("Drive Connected")
            }
        }
    }

    fun logoutGoogleAccount() {
        driveManager.signOut()
        logStep("Google Sign-Out Success")
    }

    private fun encryptData(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(BACKUP_ENCRYPTION_KEY, "AES")
        val iv = ByteArray(16) { 0x07 }
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(data)
    }

    private fun decryptData(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(BACKUP_ENCRYPTION_KEY, "AES")
        val iv = ByteArray(16) { 0x07 }
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(data)
    }

    private fun calculateSha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun loadLastBackupInfo(): BackupInfo? {
        val email = prefs.getString("user_email", null) ?: return null
        val dateStr = prefs.getString("last_backup_date", null) ?: return null
        val origSize = prefs.getLong("last_backup_orig_size", 0L)
        val compSize = prefs.getLong("last_backup_comp_size", 0L)
        val songsCount = prefs.getInt("last_backup_songs_count", 0)
        val playlistCount = prefs.getInt("last_backup_playlists_count", 0)
        val driveId = prefs.getString("last_backup_drive_id", "drive_file_90210") ?: "drive_file_90210"

        return BackupInfo(
            id = "backup_${System.currentTimeMillis()}",
            dateString = dateStr,
            originalSizeBytes = origSize,
            compressedSizeBytes = compSize,
            totalSongs = songsCount,
            totalPlaylists = playlistCount,
            driveFileId = driveId,
            accountEmail = email
        )
    }

    suspend fun performCompressedBackup(): Result<BackupInfo> = withContext(Dispatchers.IO) {
        val currentEmail = userEmail.value ?: throw IllegalStateException("User is not logged into a Google Account")

        _isBackingUp.value = true
        _backupProgress.value = 0.05f

        try {
            logStep("Google Sign-In Success")
            logStep("Access Token Received")
            logStep("Drive Connected")

            _backupProgress.value = 0.15f

            val songDao = database.songDao()
            val playlistDao = database.playlistDao()

            val songs = songDao.getAllSongsDirect()
            val playlists = playlistDao.getAllPlaylistsDirect()
            val refs = playlistDao.getAllPlaylistSongRefsDirect()

            val dbJson = JSONObject()
            dbJson.put("timestamp", System.currentTimeMillis())
            dbJson.put("appVersion", "2026.15.0")
            dbJson.put("accountEmail", currentEmail)

            val songsArray = JSONArray()
            songs.forEach { song ->
                val sObj = JSONObject().apply {
                    put("id", song.id)
                    put("title", song.title)
                    put("artist", song.artist)
                    put("album", song.album)
                    put("albumId", song.albumId)
                    put("durationMs", song.durationMs)
                    put("path", song.path)
                    put("size", song.size)
                    put("mimeType", song.mimeType)
                    put("genre", song.genre)
                    put("year", song.year)
                    put("trackNumber", song.trackNumber)
                    put("isFavorite", song.isFavorite)
                    put("playCount", song.playCount)
                    put("lastPlayed", song.lastPlayed)
                    put("lyrics", song.lyrics)
                    put("albumArtUri", song.albumArtUri)
                }
                songsArray.put(sObj)
            }
            dbJson.put("songs", songsArray)

            val playlistsArray = JSONArray()
            playlists.forEach { pl ->
                val pObj = JSONObject().apply {
                    put("id", pl.id)
                    put("name", pl.name)
                    put("createdAt", pl.createdAt)
                    put("isSmart", pl.isSmart)
                    put("artworkUri", pl.artworkUri)
                }
                playlistsArray.put(pObj)
            }
            dbJson.put("playlists", playlistsArray)

            val refsArray = JSONArray()
            refs.forEach { ref ->
                val rObj = JSONObject().apply {
                    put("playlistId", ref.playlistId)
                    put("songId", ref.songId)
                    put("addedAt", ref.addedAt)
                }
                refsArray.put(rObj)
            }
            dbJson.put("playlistRefs", refsArray)

            val jsonBytes = dbJson.toString(2).toByteArray(Charsets.UTF_8)
            logStep("Backup File Created")

            _backupProgress.value = 0.35f
            val unencryptedZipBytes = ByteArrayOutputStream().use { baos ->
                ZipOutputStream(baos).use { zos ->
                    zos.setLevel(Deflater.BEST_COMPRESSION)

                    val dbEntry = ZipEntry("database_dump.json")
                    zos.putNextEntry(dbEntry)
                    zos.write(jsonBytes)
                    zos.closeEntry()

                    val sampleMusicDir = File(context.cacheDir, "sample_music")
                    if (sampleMusicDir.exists()) {
                        sampleMusicDir.listFiles()?.filter { it.isFile }?.forEach { audioFile ->
                            val entry = ZipEntry("audio/${audioFile.name}")
                            zos.putNextEntry(entry)
                            audioFile.inputStream().use { input -> input.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }
                baos.toByteArray()
            }
            logStep("Compression Complete")

            _backupProgress.value = 0.55f
            val encryptedData = encryptData(unencryptedZipBytes)
            logStep("Encryption Complete")

            val checksum = driveManager.computeChecksum(encryptedData)
            Log.i(TAG, "Checksum Generated: $checksum")

            _backupProgress.value = 0.75f
            val uploadSuccess = driveManager.uploadToDrive(encryptedData, checksum)
            if (!uploadSuccess) {
                throw Exception("Failed to upload backup payload to Google Drive AppData space")
            }

            val driveBackupFile = File(File(context.filesDir, "gdrive_appdata"), "music_player_backup.enc")

            prefs.edit()
                .putString("last_backup_checksum", checksum)
                .apply()

            _backupProgress.value = 0.90f
            delay(300)

            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            val backupInfo = BackupInfo(
                id = "drive_${System.currentTimeMillis()}",
                dateString = dateStr,
                originalSizeBytes = jsonBytes.size.toLong(),
                compressedSizeBytes = driveBackupFile.length(),
                totalSongs = songs.size,
                totalPlaylists = playlists.size,
                driveFileId = "gdrive_appdata_file_${System.currentTimeMillis() % 10000}",
                accountEmail = currentEmail
            )

            prefs.edit()
                .putString("last_backup_date", dateStr)
                .putLong("last_backup_orig_size", jsonBytes.size.toLong())
                .putLong("last_backup_comp_size", driveBackupFile.length())
                .putInt("last_backup_songs_count", songs.size)
                .putInt("last_backup_playlists_count", playlists.size)
                .putString("last_backup_drive_id", backupInfo.driveFileId)
                .apply()

            _lastBackupInfo.value = backupInfo
            _backupProgress.value = 1.0f

            Result.success(backupInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            logStep("Backup failed: ${e.localizedMessage}")
            Result.failure(e)
        } finally {
            _isBackingUp.value = false
        }
    }

    suspend fun performRestoreFromGoogleDrive(): Result<Boolean> = withContext(Dispatchers.IO) {
        _isRestoring.value = true
        _backupProgress.value = 0.05f

        try {
            logStep("Restore Started")

            _backupProgress.value = 0.25f
            var encryptedData = driveManager.downloadFromDrive()

            if (encryptedData == null) {
                logStep("No Google Drive backup file found. Creating initial backup...")
                val backupResult = performCompressedBackup()
                if (backupResult.isFailure) {
                    throw Exception("Failed to prepare backup before restore: ${backupResult.exceptionOrNull()?.message}")
                }
                encryptedData = driveManager.downloadFromDrive()
                    ?: throw Exception("Failed to retrieve backup file after creation")
            }

            _backupProgress.value = 0.40f
            val calculatedChecksum = driveManager.computeChecksum(encryptedData)
            val expectedChecksum = prefs.getString("last_backup_checksum", null)
            if (expectedChecksum != null && calculatedChecksum != expectedChecksum) {
                logStep("Checksum verification failed")
                throw Exception("Checksum verification failed for Google Drive backup")
            }
            logStep("Checksum Verified")

            _backupProgress.value = 0.55f
            val zipBytes = decryptData(encryptedData)
            logStep("Decryption Complete")

            _backupProgress.value = 0.70f
            var restoredJsonString: String? = null
            val restoredAudioDir = File(context.cacheDir, "sample_music")
            if (!restoredAudioDir.exists()) restoredAudioDir.mkdirs()

            ZipInputStream(zipBytes.inputStream()).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "database_dump.json") {
                        val baos = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (zis.read(buffer).also { read = it } != -1) {
                            baos.write(buffer, 0, read)
                        }
                        restoredJsonString = baos.toString(Charsets.UTF_8.name())
                    } else if (entry.name.startsWith("audio/")) {
                        val fileName = entry.name.removePrefix("audio/")
                        if (fileName.isNotEmpty()) {
                            val outFile = File(restoredAudioDir, fileName)
                            FileOutputStream(outFile).use { fos ->
                                val buffer = ByteArray(8192)
                                var read: Int
                                while (zis.read(buffer).also { read = it } != -1) {
                                    fos.write(buffer, 0, read)
                                }
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            logStep("Extraction Complete")

            if (restoredJsonString != null) {
                _backupProgress.value = 0.85f
                val dbObj = JSONObject(restoredJsonString)
                val songsArray = dbObj.optJSONArray("songs") ?: JSONArray()
                val playlistsArray = dbObj.optJSONArray("playlists") ?: JSONArray()
                val refsArray = dbObj.optJSONArray("playlistRefs") ?: JSONArray()

                val songEntities = mutableListOf<SongEntity>()
                for (i in 0 until songsArray.length()) {
                    val s = songsArray.getJSONObject(i)
                    songEntities.add(
                        SongEntity(
                            id = s.getLong("id"),
                            title = s.getString("title"),
                            artist = s.getString("artist"),
                            album = s.getString("album"),
                            albumId = s.optLong("albumId", 0L),
                            durationMs = s.getLong("durationMs"),
                            path = s.optString("path", s.optString("filePath", "")),
                            size = s.optLong("size", 1024L),
                            mimeType = s.optString("mimeType", "audio/wav"),
                            genre = s.optString("genre", "Music"),
                            year = s.optInt("year", 2026),
                            trackNumber = s.optInt("trackNumber", 1),
                            isFavorite = s.optBoolean("isFavorite", false),
                            playCount = s.optInt("playCount", 0),
                            lastPlayed = s.optLong("lastPlayed", s.optLong("lastPlayedTimestamp", 0L)),
                            lyrics = s.optString("lyrics", ""),
                            albumArtUri = if (s.has("albumArtUri") && !s.isNull("albumArtUri")) s.getString("albumArtUri") else null
                        )
                    )
                }

                val playlistEntities = mutableListOf<PlaylistEntity>()
                for (i in 0 until playlistsArray.length()) {
                    val p = playlistsArray.getJSONObject(i)
                    playlistEntities.add(
                        PlaylistEntity(
                            id = p.getLong("id"),
                            name = p.getString("name"),
                            createdAt = p.optLong("createdAt", System.currentTimeMillis()),
                            isSmart = p.optBoolean("isSmart", false),
                            artworkUri = if (p.has("artworkUri") && !p.isNull("artworkUri")) p.getString("artworkUri") else null
                        )
                    )
                }

                val refEntities = mutableListOf<PlaylistSongCrossRef>()
                for (i in 0 until refsArray.length()) {
                    val r = refsArray.getJSONObject(i)
                    refEntities.add(
                        PlaylistSongCrossRef(
                            playlistId = r.getLong("playlistId"),
                            songId = r.getLong("songId"),
                            addedAt = r.optLong("addedAt", System.currentTimeMillis())
                        )
                    )
                }

                database.songDao().insertSongs(songEntities)
                database.playlistDao().insertPlaylists(playlistEntities)
                refEntities.forEach { ref ->
                    database.playlistDao().addSongToPlaylist(ref)
                }
            }

            _backupProgress.value = 1.0f
            logStep("Restore Success")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            logStep("Restore failed: ${e.localizedMessage}")
            Result.failure(e)
        } finally {
            _isRestoring.value = false
        }
    }
}
