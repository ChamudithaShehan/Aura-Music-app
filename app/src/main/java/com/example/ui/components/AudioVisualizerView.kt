package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    barHeight: Dp = 60.dp,
    barColorStart: Color = Color(0xFFBB86FC),
    barColorEnd: Color = Color(0xFF00E5FF)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
    ) {
        val width = size.width
        val height = size.height
        val barCount = bands.size.coerceAtLeast(1)
        val gap = 6f
        val barWidth = (width - (gap * (barCount - 1))) / barCount

        val gradient = Brush.verticalGradient(
            colors = listOf(barColorStart, barColorEnd)
        )

        for (i in 0 until barCount) {
            val magnitude = bands[i].coerceIn(0.05f, 1.0f)
            val h = height * magnitude
            val x = i * (barWidth + gap)
            val y = height - h

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }
    }
}
