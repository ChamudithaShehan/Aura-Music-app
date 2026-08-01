package com.example.ui.screens.equalizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AudioEqualizerState
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    equalizerState: AudioEqualizerState,
    allPresets: List<String>,
    visualizerBands: FloatArray,
    waveform: ByteArray,
    rms: Float,
    onToggleMaster: (Boolean) -> Unit,
    onSelectPreset: (String) -> Unit,
    onBandLevelChange: (Int, Int) -> Unit,
    onToggleBassBoost: (Boolean) -> Unit,
    onBassBoostStrengthChange: (Int) -> Unit,
    onToggleVirtualizer: (Boolean) -> Unit,
    onVirtualizerStrengthChange: (Int) -> Unit,
    onToggleLoudness: (Boolean) -> Unit,
    onLoudnessGainChange: (Int) -> Unit,
    onReset: () -> Unit,
    onSavePreset: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
    bottomPadding: Dp = 100.dp
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    
    val bandLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Title & Enable Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Aura DSP",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        letterSpacing = (-1).sp
                    )
                )
                Text(
                    text = "Master Audio Engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onReset()
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = equalizerState.isEnabled,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleMaster(it)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Professional Audio Analyzer Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REAL-TIME ANALYZER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Peak: ${(rms * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SpectrumAnalyzer(
                    bands = visualizerBands,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                WaveformDisplay(
                    waveform = waveform,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preset Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = equalizerState.presetName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Audio Profile") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                ) {
                    allPresets.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(preset, fontWeight = if (preset == equalizerState.presetName) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectPreset(preset)
                                dropdownExpanded = false
                            },
                            trailingIcon = {
                                if (preset !in listOf("Normal", "Rock", "Pop", "Jazz", "Dance", "Hip Hop", "Classical", "Acoustic", "Electronic", "Metal", "Podcast", "Movie", "Bass Boost", "Treble Boost", "Gaming", "Night Mode")) {
                                    IconButton(onClick = { onDeletePreset(preset) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
            FilledTonalButton(
                onClick = { showSaveDialog = true },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 10-Band Equalizer Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "PRECISION FREQUENCY SHAPING (dB)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val itemWidth = maxWidth / 10
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        bandLabels.forEachIndexed { index, freqLabel ->
                            val level = equalizerState.bandLevels.getOrElse(index) { 0 }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(itemWidth)
                            ) {
                                Text(
                                    text = "${if (level > 0) "+" else ""}$level",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (equalizerState.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                VerticalBandSlider(
                                    value = level.toFloat(),
                                    onValueChange = { 
                                        if (it.toInt() != level) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onBandLevelChange(index, it.toInt())
                                        }
                                    },
                                    valueRange = -15f..15f,
                                    enabled = equalizerState.isEnabled,
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(220.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = freqLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = if (equalizerState.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DSP Effects Section
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                EffectCard(
                    title = "BASS ENGINE",
                    enabled = equalizerState.bassBoostEnabled,
                    onToggle = onToggleBassBoost,
                    value = equalizerState.bassBoostStrength,
                    onValueChange = onBassBoostStrengthChange,
                    valueRange = 0f..1000f,
                    masterEnabled = equalizerState.isEnabled,
                    modifier = Modifier.weight(1f)
                )
                EffectCard(
                    title = "3D SPATIALIZER",
                    enabled = equalizerState.virtualizerEnabled,
                    onToggle = onToggleVirtualizer,
                    value = equalizerState.virtualizerStrength,
                    onValueChange = onVirtualizerStrengthChange,
                    valueRange = 0f..1000f,
                    masterEnabled = equalizerState.isEnabled,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            EffectCard(
                title = "BASS ENGINE",
                enabled = equalizerState.bassBoostEnabled,
                onToggle = onToggleBassBoost,
                value = equalizerState.bassBoostStrength,
                onValueChange = onBassBoostStrengthChange,
                valueRange = 0f..1000f,
                masterEnabled = equalizerState.isEnabled
            )
            Spacer(modifier = Modifier.height(16.dp))
            EffectCard(
                title = "3D SPATIALIZER",
                enabled = equalizerState.virtualizerEnabled,
                onToggle = onToggleVirtualizer,
                value = equalizerState.virtualizerStrength,
                onValueChange = onVirtualizerStrengthChange,
                valueRange = 0f..1000f,
                masterEnabled = equalizerState.isEnabled
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        EffectCard(
            title = "DYNAMICS LIMITER",
            enabled = equalizerState.loudnessEnabled,
            onToggle = onToggleLoudness,
            value = equalizerState.loudnessGain,
            onValueChange = onLoudnessGainChange,
            valueRange = 0f..1000f,
            masterEnabled = equalizerState.isEnabled
        )

        Spacer(modifier = Modifier.height(bottomPadding))
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Audio Profile") },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            onSavePreset(newPresetName)
                            newPresetName = ""
                            showSaveDialog = false
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SpectrumAnalyzer(
    bands: FloatArray,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Canvas(modifier = modifier) {
        val barWidth = size.width / (bands.size * 1.5f)
        val spacing = barWidth * 0.5f
        
        bands.forEachIndexed { index, magnitude ->
            val barHeight = magnitude * size.height
            val x = index * (barWidth + spacing)
            
            // Draw Glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.5f), Color.Transparent),
                    startY = size.height - barHeight,
                    endY = size.height
                ),
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
            
            // Draw Main Bar
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight.coerceAtLeast(2.dp.toPx())),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            
            // Draw Peak Dot
            drawRect(
                color = secondaryColor,
                topLeft = Offset(x, (size.height - barHeight - 4.dp.toPx()).coerceAtLeast(0f)),
                size = Size(barWidth, 2.dp.toPx())
            )
        }
    }
}

@Composable
fun WaveformDisplay(
    waveform: ByteArray,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Canvas(modifier = modifier) {
        val path = Path()
        val midY = size.height / 2f
        val stepX = size.width / waveform.size.toFloat()
        
        path.moveTo(0f, midY)
        for (i in waveform.indices) {
            val x = i * stepX
            val sample = (waveform[i].toInt() and 0xFF) - 128
            val y = midY + (sample / 128f) * midY
            path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.6f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EffectCard(
    title: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    masterEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle(it)
                    },
                    enabled = masterEnabled
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Intensiveness",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (masterEnabled && enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    text = "${value / 10}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (masterEnabled && enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = value.toFloat(),
                onValueChange = { 
                    if (it.toInt() != value) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onValueChange(it.toInt())
                    }
                },
                valueRange = valueRange,
                enabled = masterEnabled && enabled,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun VerticalBandSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = if (enabled) 1f else 0.3f
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "slider"
    )

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f * alpha))
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    val totalHeight = size.height.toFloat()
                    if (totalHeight > 0f) {
                        val deltaRatio = -dragAmount / totalHeight
                        val span = valueRange.endInclusive - valueRange.start
                        val newValue = (value + deltaRatio * span).coerceIn(valueRange.start, valueRange.endInclusive)
                        onValueChange(newValue)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val span = valueRange.endInclusive - valueRange.start
        val normalizedValue = if (span > 0) ((animatedValue - valueRange.start) / span).coerceIn(0f, 1f) else 0.5f

        Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 14.dp)) {
            val trackWidth = 6.dp.toPx()
            val thumbRadius = 10.dp.toPx()
            val xCenter = size.width / 2f
            val startY = thumbRadius
            val endY = size.height - thumbRadius
            val activeY = endY - (normalizedValue * (endY - startY))

            drawLine(
                color = primaryColor.copy(alpha = 0.05f * alpha),
                start = Offset(xCenter, startY),
                end = Offset(xCenter, endY),
                strokeWidth = trackWidth,
                cap = StrokeCap.Round
            )

            drawLine(
                color = primaryColor.copy(alpha = alpha),
                start = Offset(xCenter, endY),
                end = Offset(xCenter, activeY),
                strokeWidth = trackWidth,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = primaryColor.copy(alpha = alpha),
                radius = thumbRadius,
                center = Offset(xCenter, activeY)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = thumbRadius * 0.45f,
                center = Offset(xCenter, activeY)
            )
        }
    }
}

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.scale(0.85f),
        enabled = enabled
    )
}

fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(
            (placeable.width * scale).toInt(),
            (placeable.height * scale).toInt()
        ) {
            placeable.placeRelative(0, 0)
        }
    }
)
