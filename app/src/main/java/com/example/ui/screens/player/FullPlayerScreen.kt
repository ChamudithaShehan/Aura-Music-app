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
import androidx.compose.ui.draw.shadow
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
import com.example.ui.theme.AppleMusicRed

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

    val albumArtScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.86f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "artScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C0B14),
                        Color(0xFF140509),
                        Color(0xFF070204)
                    )
                )
            )
            .systemBarsPadding()
            .testTag("full_player_screen")
    ) {
        val calculatedArtSize = (maxHeight * 0.38f).coerceIn(200.dp, 330.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Drag grab handle bar & Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top drag grab indicator
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 12.dp)
                        .width(38.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.3f))
                )

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
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PLAYING FROM ALBUM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = song.album,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.9f),
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
                            tint = if (isVinylMode) AppleMusicRed else Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Apple Music Album Artwork Presentation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                            .scale(albumArtScale)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(calculatedArtSize)
                            .scale(albumArtScale)
                            .shadow(elevation = 28.dp, shape = RoundedCornerShape(20.dp), spotColor = AppleMusicRed.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(20.dp)),
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
                                tint = AppleMusicRed,
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }
                }
            }

            // Audio Visualizer subtle waveform
            AudioVisualizerBars(
                bands = visualizerBands,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(horizontal = 12.dp)
            )

            // Song Info & Apple Music Lossless / Star Favorite Accent
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp
                            ),
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
                            imageVector = if (song.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (song.isFavorite) AppleMusicRed else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Apple Lossless Badge
                Surface(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "LOSSLESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Scrubbing Progress Bar
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
                        thumbColor = Color.White,
                        activeTrackColor = Color.White.copy(alpha = 0.85f),
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_seekbar")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formattedCurrent,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.55f)
                    )
                    Text(
                        text = formattedDuration,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }
            }

            // Apple Music Signature Transport Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleModeEnabled) AppleMusicRed else Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }

                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                IconButton(onClick = onCycleRepeatMode) {
                    Icon(
                        imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) AppleMusicRed else Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom Action Bar (Lyrics, Equalizer, Sleep Timer, Speed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenLyricsClick) {
                    Icon(
                        imageVector = Icons.Default.Lyrics,
                        contentDescription = "Lyrics",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = onOpenEqualizerClick) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Equalizer",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = onOpenSleepTimerClick) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Sleep Timer",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(22.dp)
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
                        Text(
                            text = "${playbackSpeed}x",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppleMusicRed
                        )
                    }
                }
            }
        }
    }
}

