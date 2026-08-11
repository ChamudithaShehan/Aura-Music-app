package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun ImmersiveBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val isLight = bgColor.luminance() > 0.5f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (!isLight) {
                // Top-left Apple Red signature ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFA2D48).copy(alpha = 0.30f),
                            Color(0xFFB01229).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.20f, height * 0.15f),
                        radius = width * 0.90f
                    ),
                    center = Offset(width * 0.20f, height * 0.15f),
                    radius = width * 0.90f
                )

                // Bottom-right deep magenta glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8B125C).copy(alpha = 0.25f),
                            Color(0xFF3F0B30).copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.85f, height * 0.70f),
                        radius = width * 0.80f
                    ),
                    center = Offset(width * 0.85f, height * 0.70f),
                    radius = width * 0.80f
                )

                // Subtle middle blue-purple accent light
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF251F4F).copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.50f, height * 0.45f),
                        radius = width * 0.70f
                    ),
                    center = Offset(width * 0.50f, height * 0.45f),
                    radius = width * 0.70f
                )
            } else {
                // Light mode Apple Music subtle pinkish warm atmosphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD6DD).copy(alpha = 0.45f),
                            Color(0xFFFFF0F2).copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.15f, height * 0.10f),
                        radius = width * 0.85f
                    ),
                    center = Offset(width * 0.15f, height * 0.10f),
                    radius = width * 0.85f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFC0CB).copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.85f, height * 0.65f),
                        radius = width * 0.75f
                    ),
                    center = Offset(width * 0.85f, height * 0.65f),
                    radius = width * 0.75f
                )
            }
        }

        content()
    }
}

