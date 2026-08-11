package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MusicRepository
import com.example.domain.model.LyricsLine
import com.example.domain.model.RepeatMode
import com.example.domain.model.Song
import com.example.player.PlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    val playerManager: PlayerManager,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val currentSong: StateFlow<Song?> = playerManager.currentSong
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val queue: StateFlow<List<Song>> = playerManager.queue
    val shuffleModeEnabled: StateFlow<Boolean> = playerManager.shuffleModeEnabled
    val repeatMode: StateFlow<RepeatMode> = playerManager.repeatMode
    val playbackSpeed: StateFlow<Float> = playerManager.playbackSpeed
    val sleepTimerRemainingSec: StateFlow<Int?> = playerManager.sleepTimerRemainingSec
    val visualizerBands: StateFlow<FloatArray> = playerManager.visualizerBands

    private val _parsedLyrics = MutableStateFlow<List<LyricsLine>>(emptyList())
    val parsedLyrics: StateFlow<List<LyricsLine>> = _parsedLyrics.asStateFlow()

    private val _lyricOffsetMs = MutableStateFlow(0L)
    val lyricOffsetMs: StateFlow<Long> = _lyricOffsetMs.asStateFlow()

    init {
        currentSong.value?.let { song ->
            parseLyrics(song.lyrics)
        }
        viewModelScope.launch {
            currentSong.collect { song ->
                _lyricOffsetMs.value = 0L
                if (song != null) {
                    musicRepository.recordSongPlayed(song.id)
                    parseLyrics(song.lyrics)
                } else {
                    _parsedLyrics.value = emptyList()
                }
            }
        }
    }

    fun setLyricOffsetMs(offsetMs: Long) {
        _lyricOffsetMs.value = offsetMs
    }

    fun adjustLyricOffsetMs(deltaMs: Long) {
        _lyricOffsetMs.value = (_lyricOffsetMs.value + deltaMs).coerceIn(-15000L, 15000L)
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        playerManager.playSong(song, queue)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun nextSong() {
        playerManager.nextSong()
    }

    fun previousSong() {
        playerManager.previousSong()
    }

    fun stopPlayback() {
        playerManager.stopPlayback()
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerManager.cycleRepeatMode()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }

    fun startSleepTimer(minutes: Int) {
        playerManager.startSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
    }

    fun toggleFavoriteCurrentSong() {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id, !song.isFavorite)
        }
    }

    private var searchingLyricsSongId: Long? = null

    fun setSearchingLyricsSongId(songId: Long?) {
        searchingLyricsSongId = songId
    }

    fun checkAndAutoSaveClipboardLyrics(context: android.content.Context, song: Song): Boolean {
        // STRICT CHECK: Only auto-save from clipboard if the user explicitly clicked "Search lyrics" for this exact song ID!
        val activeSearchId = searchingLyricsSongId
        if (activeSearchId == null || activeSearchId != song.id) {
            return false
        }

        try {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager ?: return false
            val clipData = clipboard.primaryClip ?: return false
            if (clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0)?.text?.toString()?.trim() ?: ""
                if (clipText.isNotBlank() && clipText != song.lyrics) {
                    searchingLyricsSongId = null // Immediately clear search token to prevent saving to any other song
                    saveLyricsForCurrentSong(clipText)
                    android.widget.Toast.makeText(
                        context,
                        "Lyrics automatically saved for ${song.title}!",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun saveLyricsForCurrentSong(newLyrics: String) {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            musicRepository.updateLyrics(song.id, newLyrics)
            playerManager.updateCurrentSongLyrics(newLyrics)
            parseLyrics(newLyrics)
        }
    }

    private fun parseLyrics(rawLyrics: String) {
        if (rawLyrics.isBlank()) {
            _parsedLyrics.value = emptyList()
            return
        }

        val lines = mutableListOf<LyricsLine>()
        val timestampRegex = Regex("""\[(\d{1,3}):(\d{2})(?:[\.:](\d{1,3}))?\]""")
        val offsetRegex = Regex("""\[offset:\s*([+-]?\d+)\]""", RegexOption.IGNORE_CASE)

        // Parse global LRC header offset if present
        var headerOffsetMs = 0L
        offsetRegex.find(rawLyrics)?.let { match ->
            headerOffsetMs = match.groupValues[1].toLongOrNull() ?: 0L
        }

        val hasLrcTime = timestampRegex.containsMatchIn(rawLyrics)

        if (hasLrcTime) {
            rawLyrics.lines().forEach { rawLine ->
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith("[ar:", ignoreCase = true) ||
                    line.startsWith("[ti:", ignoreCase = true) || line.startsWith("[al:", ignoreCase = true) ||
                    line.startsWith("[by:", ignoreCase = true) || line.startsWith("[re:", ignoreCase = true) ||
                    line.startsWith("[ve:", ignoreCase = true) || line.startsWith("[offset:", ignoreCase = true)
                ) {
                    return@forEach
                }

                val matches = timestampRegex.findAll(line).toList()
                if (matches.isNotEmpty()) {
                    val text = line.replace(timestampRegex, "").trim()
                    if (text.isNotEmpty()) {
                        for (match in matches) {
                            val min = match.groupValues[1].toLongOrNull() ?: 0L
                            val sec = match.groupValues[2].toLongOrNull() ?: 0L
                            val msStr = match.groupValues[3]
                            val msPart = when (msStr.length) {
                                1 -> (msStr.toLongOrNull() ?: 0L) * 100
                                2 -> (msStr.toLongOrNull() ?: 0L) * 10
                                3 -> msStr.toLongOrNull() ?: 0L
                                else -> 0L
                            }
                            val timeMs = ((min * 60 * 1000) + (sec * 1000) + msPart + headerOffsetMs).coerceAtLeast(0L)
                            lines.add(LyricsLine(timeMs, text))
                        }
                    }
                }
            }
            _parsedLyrics.value = lines.sortedBy { it.timeMs }
        } else {
            // Format plain text lyrics into synchronized structured lyric lines
            val song = currentSong.value
            val liveDuration = durationMs.value
            val totalDurationMs = when {
                liveDuration > 0 -> liveDuration
                song != null && song.durationMs > 0 -> song.durationMs
                else -> 180000L
            }
            val cleanLines = rawLyrics.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (cleanLines.isEmpty()) {
                _parsedLyrics.value = emptyList()
                return
            }

            // Distribute lines with realistic song intro offset and evenly spaced intervals
            val startIntroMs = (totalDurationMs * 0.04).toLong().coerceIn(3000L, 10000L)
            val endOutroMs = (totalDurationMs * 0.05).toLong().coerceIn(4000L, 15000L)
            val availableMs = (totalDurationMs - startIntroMs - endOutroMs).coerceAtLeast(10000L)
            val intervalMs = (availableMs / cleanLines.size).coerceAtLeast(2000L)

            cleanLines.forEachIndexed { index, text ->
                val timeMs = startIntroMs + (index * intervalMs)
                lines.add(LyricsLine(timeMs, text))
            }
            _parsedLyrics.value = lines.sortedBy { it.timeMs }
        }
    }
}
