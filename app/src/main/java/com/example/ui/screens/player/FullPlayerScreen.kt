package com.example.ui.screens.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.RepeatMode
import com.example.domain.model.Song
import com.example.ui.components.AudioVisualizerBars
import com.example.ui.components.VinylDisc

@Composable
fun FullPlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    shuffleModeEnabled: Boolean,
    repeatMode: RepeatMode,
    playbackSpeed: Float,
    visualizerBands: FloatArray,
    rms: Float,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeatMode: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onOpenLyricsClick: () -> Unit,
    onOpenSleepTimerClick: () -> Unit,
    onOpenEqualizerClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    var isVinylMode by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPositionMs by remember { mutableStateOf(0L) }

    val currentProgress = if (isSeeking) seekPositionMs else progressMs
    val progressRatio = if (durationMs > 0) (currentProgress.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val totalSec = durationMs / 1000
    val totalMin = totalSec / 60
    val totalRemSec = totalSec % 60
    val formattedDuration = String.format("%d:%02d", totalMin, totalRemSec)

    val curSec = currentProgress / 1000
    val curMin = curSec / 60
    val curRemSec = curSec % 60
    val formattedCurrent = String.format("%d:%02d", curMin, curRemSec)
    
    // Album Art Pulse Animation based on RMS
    val pulseScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f + (rms * 0.12f) else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        label = "pulse"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1528),
                        Color(0xFF0F0F12)
                    )
                )
            )
            .systemBarsPadding()
            .testTag("full_player_screen")
    ) {
        val calculatedArtSize = (maxHeight * 0.36f).coerceIn(160.dp, 290.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.testTag("close_player_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AURA AUDIO ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFFD0BCFF)
                    )
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { isVinylMode = !isVinylMode },
                    modifier = Modifier.testTag("toggle_vinyl_mode")
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Toggle Vinyl Mode",
                        tint = if (isVinylMode) Color(0xFFD0BCFF) else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Artwork or Vinyl Disc with Pulse Effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount < -50) {
                                onNextClick()
                            } else if (dragAmount > 50) {
                                onPreviousClick()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isVinylMode) {
                    VinylDisc(
                        albumArtUri = song.albumArtUri,
                        isPlaying = isPlaying,
                        modifier = Modifier
                            .size(calculatedArtSize)
                            .scale(pulseScale)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(calculatedArtSize)
                            .scale(pulseScale)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!song.albumArtUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = song.albumArtUri,
                                contentDescription = "Full Album Art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Professional Visualizer
            AudioVisualizerBars(
                bands = visualizerBands,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp)
            )

            // Song Info & Favorite Heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.artist} • ${song.genre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("player_favorite_button")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) Color(0xFFFF4081) else Color.White
                    )
                }
            }

            // Seekbar Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progressRatio,
                    onValueChange = { ratio ->
                        isSeeking = true
                        seekPositionMs = (ratio * durationMs).toLong()
                    },
                    onValueChangeFinished = {
                        onSeekTo(seekPositionMs)
                        isSeeking = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_seekbar")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formattedCurrent,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formattedDuration,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleModeEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp,
                    onClick = onPlayPauseClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = onCycleRepeatMode) {
                    Icon(
                        imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Secondary Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenEqualizerClick) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = onOpenLyricsClick) {
                    Icon(
                        imageVector = Icons.Default.Lyrics,
                        contentDescription = "Lyrics",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = onOpenSleepTimerClick) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Sleep Timer",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = {
                        val nextSpeed = when (playbackSpeed) {
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            1.5f -> 2.0f
                            2.0f -> 0.5f
                            else -> 1.0f
                        }
                        onSpeedChange(nextSpeed)
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Playback Speed",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${playbackSpeed}x",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
