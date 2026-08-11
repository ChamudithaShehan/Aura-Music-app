package com.example.domain.model

data class PlaytimeStats(
    val totalListenTimeSec: Long = 0L,
    val tracksPlayedCount: Int = 0,
    val dailyAverageMins: Long = 0L,
    val favoriteGenre: String = "Acoustic / Pop",
    val firstListenTimestampMs: Long = System.currentTimeMillis()
) {
    val formattedTotalTime: String
        get() {
            if (totalListenTimeSec <= 0L) return "0m 0s"
            val hours = totalListenTimeSec / 3600
            val minutes = (totalListenTimeSec % 3600) / 60
            val seconds = totalListenTimeSec % 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }

    val formattedDailyAverage: String
        get() {
            return if (dailyAverageMins > 0) "$dailyAverageMins mins" else "< 1 min"
        }
}
