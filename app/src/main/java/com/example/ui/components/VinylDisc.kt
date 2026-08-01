package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun VinylDisc(
    albumArtUri: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val currentRotation = if (isPlaying) rotationAngle else 0f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .rotate(currentRotation),
        contentAlignment = Alignment.Center
    ) {
        // Vinyl Outer Disc Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerRadius = size.minDimension / 2f
            
            // Outer Black Vinyl Rim
            drawCircle(
                color = Color(0xFF111115),
                radius = centerRadius
            )

            // Vinyl Groove Concentric Rings
            val grooveCount = 8
            for (i in 1..grooveCount) {
                val r = centerRadius * (0.45f + i * 0.06f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = r,
                    style = Stroke(width = 1.5f)
                )
            }

            // Glossy Shine Highlight Brush
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    radius = centerRadius * 0.8f
                ),
                radius = centerRadius
            )
        }

        // Center Album Art Badge
        Box(
            modifier = Modifier
                .fillMaxSize(0.48f)
                .clip(CircleShape)
                .background(Color(0xFF22222B)),
            contentAlignment = Alignment.Center
        ) {
            if (!albumArtUri.isNullOrEmpty()) {
                AsyncImage(
                    model = albumArtUri,
                    contentDescription = "Vinyl Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF))
                            )
                        )
                )
            }

            // Spindle Hole
            Box(
                modifier = Modifier
                    .fillMaxSize(0.22f)
                    .clip(CircleShape)
                    .background(Color(0xFF0F0817))
            )
        }
    }
}
