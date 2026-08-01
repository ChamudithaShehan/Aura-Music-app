package com.example.data.backup

data class BackupInfo(
    val id: String,
    val dateString: String,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val totalSongs: Int,
    val totalPlaylists: Int,
    val driveFileId: String,
    val accountEmail: String
) {
    val compressionSavingsPercent: Int
        get() = if (originalSizeBytes > 0) {
            (((originalSizeBytes - compressedSizeBytes).toDouble() / originalSizeBytes.toDouble()) * 100).toInt().coerceIn(0, 99)
        } else 0

    val originalSizeFormatted: String
        get() = formatFileSize(originalSizeBytes)

    val compressedSizeFormatted: String
        get() = formatFileSize(compressedSizeBytes)

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}

data class DatabaseBackupPayload(
    val exportTimestamp: Long,
    val appVersion: String = "2026.15.0",
    val songEntities: List<Map<String, String>>,
    val playlists: List<Map<String, String>>,
    val playlistSongRefs: List<Map<String, String>>
)
