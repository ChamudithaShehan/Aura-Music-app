package com.example.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.domain.model.AudioEqualizerState
import com.example.domain.model.RepeatMode
import com.example.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.sin

class PlayerManager(private val context: Context, private val equalizerManager: EqualizerManager) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            shuffleModeEnabled = false
        }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled: StateFlow<Boolean> = _shuffleModeEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _sleepTimerRemainingSec = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSec: StateFlow<Int?> = _sleepTimerRemainingSec.asStateFlow()

    // 32 frequency band amplitudes for 120fps smooth audio visualizer
    private val _visualizerBands = MutableStateFlow(FloatArray(32) { 0.1f })
    val visualizerBands: StateFlow<FloatArray> = _visualizerBands.asStateFlow()

    val equalizerState: StateFlow<AudioEqualizerState> = equalizerManager.state

    private var sleepTimerJob: Job? = null
    private var progressUpdateJob: Job? = null
    private var visualizerJob: Job? = null

    init {
        setupPlayerListener()
        startProgressTracker()
        startVisualizerSimulator()
        setupAudioEffects()
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val mediaId = mediaItem?.mediaId?.toLongOrNull()
                _currentSong.value = _queue.value.find { it.id == mediaId }
                _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
            }
        })
    }

    private fun setupAudioEffects() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != 0 && audioSessionId != -1) {
                    equalizerManager.init(audioSessionId)
                }
            }
        })

        val currentSessionId = exoPlayer.audioSessionId
        if (currentSessionId != 0 && currentSessionId != -1) {
            equalizerManager.init(currentSessionId)
        }
    }

    fun playSong(song: Song, newQueue: List<Song> = listOf(song)) {
        _queue.value = newQueue
        val index = newQueue.indexOfFirst { it.id == song.id }
        
        val mediaItems = newQueue.map { s ->
            MediaItem.Builder()
                .setMediaId(s.id.toString())
                .setUri(if (s.path.startsWith("/")) Uri.fromFile(File(s.path)) else Uri.parse(s.path))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .setArtworkUri(s.albumArtUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, if (index >= 0) index else 0, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
        _currentSong.value = song
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun nextSong() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_repeatMode.value == RepeatMode.ALL && _queue.value.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
        }
    }

    fun previousSong() {
        if (exoPlayer.currentPosition > 3000L) {
            exoPlayer.seekTo(0L)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        }
    }

    fun stopPlayback() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _currentSong.value = null
        _isPlaying.value = false
        _currentPositionMs.value = 0L
    }

    fun toggleShuffle() {
        val newShuffle = !_shuffleModeEnabled.value
        _shuffleModeEnabled.value = newShuffle
        exoPlayer.shuffleModeEnabled = newShuffle
    }

    fun cycleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = nextMode
        exoPlayer.repeatMode = when (nextMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer.setPlaybackSpeed(speed)
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        var remainingSec = minutes * 60
        _sleepTimerRemainingSec.value = remainingSec

        sleepTimerJob = scope.launch {
            while (remainingSec > 0) {
                delay(1000)
                remainingSec--
                _sleepTimerRemainingSec.value = remainingSec

                if (remainingSec == 10) {
                    // Gentle volume fade out
                    exoPlayer.volume = 0.5f
                }
            }
            exoPlayer.pause()
            exoPlayer.volume = 1.0f
            _sleepTimerRemainingSec.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSec.value = null
        exoPlayer.volume = 1.0f
    }

    fun setEqualizerState(state: AudioEqualizerState) {
        equalizerManager.applyState(state)
    }

    private fun startProgressTracker() {
        progressUpdateJob = scope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    _currentPositionMs.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                    delay(250)
                } else {
                    delay(500)
                }
            }
        }
    }

    private fun startVisualizerSimulator() {
        visualizerJob = scope.launch {
            var phase = 0.0
            val bands = FloatArray(32) { 0.02f }
            var activeFade = false
            while (true) {
                if (exoPlayer.isPlaying) {
                    phase += 0.15
                    val eqState = equalizerManager.state.value
                    val bassBoostFactor = if (eqState.isEnabled && eqState.bassBoostEnabled) 
                        1.0f + (eqState.bassBoostStrength / 1000f) * 0.5f else 1.0f

                    for (i in 0 until 32) {
                        val base = abs(sin(phase + i * 0.25)).toFloat()
                        val eqBandIndex = (i * 10) / 32
                        val eqGain = if (eqState.isEnabled) {
                            val db = eqState.bandLevels.getOrElse(eqBandIndex) { 0 }
                            1.0f + (db / 15f) * 0.4f
                        } else 1.0f
                        val bassMul = if (i < 8) 1.2f * bassBoostFactor else 0.8f
                        bands[i] = (base * bassMul * eqGain * 0.85f + 0.1f).coerceIn(0.05f, 1.0f)
                    }
                    _visualizerBands.value = bands.clone()
                    activeFade = true
                    delay(40)
                } else if (activeFade) {
                    var remaining = false
                    for (i in 0 until 32) {
                        bands[i] = bands[i] * 0.75f
                        if (bands[i] > 0.03f) {
                            remaining = true
                        } else {
                            bands[i] = 0.02f
                        }
                    }
                    _visualizerBands.value = bands.clone()
                    activeFade = remaining
                    delay(50)
                } else {
                    delay(500)
                }
            }
        }
    }

    fun release() {
        progressUpdateJob?.cancel()
        visualizerJob?.cancel()
        sleepTimerJob?.cancel()
        equalizerManager.release()
        exoPlayer.release()
    }
}
