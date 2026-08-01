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
                // Top-left deep purple atmospheric glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6750A4).copy(alpha = 0.45f),
                            Color(0xFF6750A4).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.15f, height * 0.10f),
                        radius = width * 0.85f
                    ),
                    center = Offset(width * 0.15f, height * 0.10f),
                    radius = width * 0.85f
                )

                // Right-middle lavender glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFD0BCFF).copy(alpha = 0.35f),
                            Color(0xFF8B5CF6).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.90f, height * 0.65f),
                        radius = width * 0.75f
                    ),
                    center = Offset(width * 0.90f, height * 0.65f),
                    radius = width * 0.75f
                )

                // Subtle bottom-left glow accent
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3B0764).copy(alpha = 0.30f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.05f, height * 0.90f),
                        radius = width * 0.60f
                    ),
                    center = Offset(width * 0.05f, height * 0.90f),
                    radius = width * 0.60f
                )
            } else {
                // Soft elegant glows for Light theme
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFD0BCFF).copy(alpha = 0.35f),
                            Color(0xFFE8DEF8).copy(alpha = 0.15f),
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
                            Color(0xFF6750A4).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.90f, height * 0.65f),
                        radius = width * 0.75f
                    ),
                    center = Offset(width * 0.90f, height * 0.65f),
                    radius = width * 0.75f
                )
            }
        }

        content()
    }
}
