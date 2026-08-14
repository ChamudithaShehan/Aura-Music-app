package com.example.data.backup

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Collections

/**
 * Senior Developer Production-Ready Google Drive Backup Helper
 *
 * Handles Google Sign-In with appDataFolder scope and Google Drive REST API operations
 * for app-specific hidden storage (`https://www.googleapis.com/auth/drive.appdata`).
 */
class GoogleDriveBackupHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleDriveBackupHelper"
        private const val BACKUP_FILE_NAME = "music_app_backup.json"
        private const val APPDATA_FOLDER = "appDataFolder"
        private const val MIME_TYPE_JSON = "application/json"
        const val GOOGLE_CLIENT_ID = "10604639567-grr7l2td1h3r6fvvjbe271vivm30pbhf.apps.googleusercontent.com"
        const val GOOGLE_PROJECT_ID = "music-app-505208"
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    /**
     * Builds GoogleSignInClient configured with DRIVE_APPDATA scope.
     */
    fun getGoogleSignInClient(): GoogleSignInClient {
        val driveScope = Scope(DriveScopes.DRIVE_APPDATA)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GOOGLE_CLIENT_ID)
            .requestEmail()
            .requestScopes(driveScope)
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Obtains the current signed-in account if authorized with DRIVE_APPDATA scope.
     */
    fun getSignedInAccount(): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val driveScope = Scope(DriveScopes.DRIVE_APPDATA)
        return if (account != null && GoogleSignIn.hasPermissions(account, driveScope)) {
            account
        } else {
            null
        }
    }

    /**
     * Constructs a Drive API service instance using the authenticated GoogleSignInAccount.
     */
    fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Aura Music Player")
            .build()
    }

    /**
     * Uploads or updates the backup JSON string inside the user's hidden appDataFolder.
     *
     * @param backupJsonString The serialized JSON string containing playlists, favorites, and settings.
     * @param completion Callback returned on Main thread with success status and any exception.
     */
    fun uploadBackup(
        backupJsonString: String,
        completion: (Boolean, Exception?) -> Unit
    ) {
        val account = getSignedInAccount()
        if (account == null) {
            completion(false, IllegalStateException("User is not signed in or scope missing"))
            return
        }

        coroutineScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    val driveService = getDriveService(account)

                    // Check if music_app_backup.json already exists in appDataFolder
                    val existingFileId = findBackupFileId(driveService)

                    val contentStream = ByteArrayContent.fromString(MIME_TYPE_JSON, backupJsonString)

                    if (existingFileId != null) {
                        // Update existing file content
                        Log.d(TAG, "Updating existing backup file: $existingFileId")
                        val fileMetadata = File()
                        driveService.files().update(existingFileId, fileMetadata, contentStream).execute()
                    } else {
                        // Create a new backup file in appDataFolder
                        Log.d(TAG, "Creating new backup file in appDataFolder")
                        val fileMetadata = File().apply {
                            name = BACKUP_FILE_NAME
                            parents = listOf(APPDATA_FOLDER)
                            mimeType = MIME_TYPE_JSON
                        }
                        driveService.files().create(fileMetadata, contentStream).execute()
                    }
                    true
                }
                completion(success, null)
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading backup to Drive", e)
                completion(false, e)
            }
        }
    }

    /**
     * Downloads the backup JSON string from appDataFolder for restoration into local DB.
     *
     * @param completion Callback returned on Main thread with the backup JSON string or exception.
     */
    fun downloadBackup(completion: (String?, Exception?) -> Unit) {
        val account = getSignedInAccount()
        if (account == null) {
            completion(null, IllegalStateException("User is not signed in or scope missing"))
            return
        }

        coroutineScope.launch {
            try {
                val jsonResult = withContext(Dispatchers.IO) {
                    val driveService = getDriveService(account)
                    val fileId = findBackupFileId(driveService)
                        ?: return@withContext null

                    val outputStream = ByteArrayOutputStream()
                    driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
                    outputStream.toString(Charsets.UTF_8.name())
                }
                completion(jsonResult, null)
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading backup from Drive", e)
                completion(null, e)
            }
        }
    }

    /**
     * Fetches the last modified timestamp of the backup file in appDataFolder.
     *
     * @param completion Callback returned on Main thread with the formatted date string or exception.
     */
    fun getLastBackupTime(completion: (String?, Exception?) -> Unit) {
        val account = getSignedInAccount()
        if (account == null) {
            completion(null, IllegalStateException("User is not signed in or scope missing"))
            return
        }

        coroutineScope.launch {
            try {
                val formattedTime = withContext(Dispatchers.IO) {
                    val driveService = getDriveService(account)
                    val fileList = driveService.files().list()
                        .setSpaces(APPDATA_FOLDER)
                        .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
                        .setFields("files(id, name, modifiedTime)")
                        .execute()

                    val file = fileList.files.firstOrNull()
                    file?.modifiedTime?.toString()
                }
                completion(formattedTime, null)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching last backup time", e)
                completion(null, e)
            }
        }
    }

    /**
     * Helper to search for music_app_backup.json inside appDataFolder.
     */
    private fun findBackupFileId(driveService: Drive): String? {
        val result = driveService.files().list()
            .setSpaces(APPDATA_FOLDER)
            .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
            .setFields("files(id, name)")
            .execute()

        val files = result.files
        return if (!files.isNullOrEmpty()) {
            files[0].id
        } else {
            null
        }
    }
}
