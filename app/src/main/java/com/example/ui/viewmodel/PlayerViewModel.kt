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

    init {
        currentSong.value?.let { song ->
            parseLyrics(song.lyrics)
        }
        viewModelScope.launch {
            currentSong.collect { song ->
                if (song != null) {
                    musicRepository.recordSongPlayed(song.id)
                    parseLyrics(song.lyrics)
                } else {
                    _parsedLyrics.value = emptyList()
                }
            }
        }
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
        val regex = Regex("""\[(\d{1,2}):(\d{2})(?:[\.:](\d{2,3}))?]""")
        val hasLrcTime = regex.containsMatchIn(rawLyrics)

        if (hasLrcTime) {
            rawLyrics.lines().forEach { line ->
                val match = regex.find(line)
                if (match != null) {
                    val min = match.groupValues[1].toLongOrNull() ?: 0L
                    val sec = match.groupValues[2].toLongOrNull() ?: 0L
                    val msStr = match.groupValues[3]
                    val msPart = when {
                        msStr.length == 3 -> msStr.toLongOrNull() ?: 0L
                        msStr.length == 2 -> (msStr.toLongOrNull() ?: 0L) * 10
                        else -> 0L
                    }
                    val timeMs = (min * 60 * 1000) + (sec * 1000) + msPart
                    val text = line.replace(regex, "").trim()
                    if (text.isNotEmpty()) {
                        lines.add(LyricsLine(timeMs, text))
                    }
                }
            }
            _parsedLyrics.value = lines.sortedBy { it.timeMs }
        } else {
            // Format plain text lyrics into structured lyric lines
            val song = currentSong.value
            val totalDurationMs = if (song != null && song.durationMs > 0) song.durationMs else 180000L
            val cleanLines = rawLyrics.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (cleanLines.isEmpty()) {
                _parsedLyrics.value = emptyList()
                return
            }
            val lineIntervalMs = (totalDurationMs / cleanLines.size).coerceIn(2000L, 5000L)
            cleanLines.forEachIndexed { index, text ->
                lines.add(LyricsLine(index * lineIntervalMs, text))
            }
            _parsedLyrics.value = lines
        }
    }
}
