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

    fun saveLyricsForCurrentSong(newLyrics: String) {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            musicRepository.updateLyrics(song.id, newLyrics)
            parseLyrics(newLyrics)
        }
    }

    private fun parseLyrics(rawLyrics: String) {
        val song = currentSong.value
        val effectiveLyrics = if (rawLyrics.isBlank() && song != null) {
            getOrGenerateLyrics(song)
        } else {
            rawLyrics
        }

        if (effectiveLyrics.isBlank()) {
            _parsedLyrics.value = emptyList()
            return
        }

        val lines = mutableListOf<LyricsLine>()
        val regex = Regex("""\[(\d{1,2}):(\d{2})(?:[\.:](\d{2,3}))?]""")

        effectiveLyrics.lines().forEach { line ->
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
    }

    private fun getOrGenerateLyrics(song: Song): String {
        val durationMs = if (song.durationMs > 0) song.durationMs else 15000L
        val lines = listOf(
            "♪ Playing ${song.title} ♪",
            "Artist: ${song.artist}",
            "Album: ${song.album}",
            "Rhythm flowing through the beat",
            "Harmonies filling up the space",
            "Feel the dynamic soundscape resonance",
            "Crystal clear audio performance",
            "Waves rising with the baseline",
            "Aura Music audio synchronization",
            "Outro - Fade into acoustic silence"
        )
        val intervalMs = (durationMs / lines.size).coerceAtLeast(2000L)
        return buildString {
            lines.forEachIndexed { index, lineText ->
                val timeMs = index * intervalMs
                val min = (timeMs / 1000) / 60
                val sec = (timeMs / 1000) % 60
                val ms = (timeMs % 1000) / 10
                appendLine(String.format("[%02d:%02d.%02d] %s", min, sec, ms, lineText))
            }
        }
    }
}
