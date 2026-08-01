package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AudioVisualizerBars(
    bands: FloatArray,
    modifier: Modifier = Modifier,
    barHeight: Dp = 64.dp,
    barColorStart: Color = Color(0xFFD0BCFF),
    barColorEnd: Color = Color(0xFF7C4DFF)
) {
    val peakHold = remember { FloatArray(bands.size) { 0f } }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
    ) {
        val width = size.width
        val height = size.height
        val barCount = bands.size
        val gap = 4.dp.toPx()
        val barWidth = (width - (gap * (barCount - 1))) / barCount

        val gradient = Brush.verticalGradient(
            colors = listOf(barColorStart, barColorEnd)
        )

        for (i in 0 until barCount) {
            val magnitude = bands[i].coerceIn(0.02f, 1.0f)
            val h = height * magnitude
            val x = i * (barWidth + gap)
            
            // Peak hold logic for visualizer
            if (magnitude > peakHold[i]) {
                peakHold[i] = magnitude
            } else {
                peakHold[i] *= 0.95f
            }
            
            val peakY = height - (peakHold[i] * height)

            // Glow Effect
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(barColorStart.copy(alpha = 0.3f), Color.Transparent),
                    startY = height - h,
                    endY = height
                ),
                topLeft = Offset(x, height - h),
                size = Size(barWidth, h)
            )

            // Main Bar
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, height - h),
                size = Size(barWidth, h.coerceAtLeast(2.dp.toPx())),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            
            // Peak Line
            drawRect(
                color = barColorStart.copy(alpha = 0.8f),
                topLeft = Offset(x, peakY.coerceAtMost(height - 2.dp.toPx())),
                size = Size(barWidth, 2.dp.toPx())
            )
        }
    }
}
