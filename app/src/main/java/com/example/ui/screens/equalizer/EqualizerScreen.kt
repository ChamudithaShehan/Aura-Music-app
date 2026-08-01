package com.example.ui.screens.equalizer

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
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
    
    val bandLabels = listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
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
                    text = "Equalizer",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                )
                Text(
                    text = "Professional Audio Processing",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = equalizerState.isEnabled,
                    onCheckedChange = onToggleMaster
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
                    label = { Text("Preset") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    allPresets.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(preset) },
                            onClick = {
                                onSelectPreset(preset)
                                dropdownExpanded = false
                            },
                            trailingIcon = {
                                if (preset !in listOf("Normal", "Rock", "Pop", "Jazz", "Dance", "Hip Hop", "Classical", "Acoustic", "Electronic", "Vocal", "Bass Boost", "Treble Boost")) {
                                    IconButton(onClick = { onDeletePreset(preset) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
            Button(
                onClick = { showSaveDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 10-Band Equalizer Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "10-BAND FREQUENCY CONTROL",
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
                                    color = if (equalizerState.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                VerticalBandSlider(
                                    value = level.toFloat(),
                                    onValueChange = { onBandLevelChange(index, it.toInt()) },
                                    valueRange = -15f..15f,
                                    enabled = equalizerState.isEnabled,
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(200.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = freqLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = if (equalizerState.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DSP Effects Section
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                EffectCard(
                    title = "BASS BOOST",
                    enabled = equalizerState.bassBoostEnabled,
                    onToggle = onToggleBassBoost,
                    value = equalizerState.bassBoostStrength,
                    onValueChange = onBassBoostStrengthChange,
                    valueRange = 0f..1000f,
                    masterEnabled = equalizerState.isEnabled,
                    modifier = Modifier.weight(1f)
                )
                EffectCard(
                    title = "3D VIRTUALIZER",
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
                title = "BASS BOOST",
                enabled = equalizerState.bassBoostEnabled,
                onToggle = onToggleBassBoost,
                value = equalizerState.bassBoostStrength,
                onValueChange = onBassBoostStrengthChange,
                valueRange = 0f..1000f,
                masterEnabled = equalizerState.isEnabled
            )
            Spacer(modifier = Modifier.height(16.dp))
            EffectCard(
                title = "3D VIRTUALIZER",
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
            title = "LOUDNESS ENHANCER",
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
            title = { Text("Save Custom Preset") },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Preset Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
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
                    shape = RoundedCornerShape(12.dp)
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
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    enabled = masterEnabled
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Strength",
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
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = valueRange,
                enabled = masterEnabled && enabled,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
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
    val alpha = if (enabled) 1f else 0.4f
    val primaryColor = MaterialTheme.colorScheme.primary
    
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f * alpha))
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
        val normalizedValue = if (span > 0) ((value - valueRange.start) / span).coerceIn(0f, 1f) else 0.5f

        Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)) {
            val trackWidth = 4.dp.toPx()
            val thumbRadius = 8.dp.toPx()
            val xCenter = size.width / 2f
            val startY = thumbRadius
            val endY = size.height - thumbRadius
            val activeY = endY - (normalizedValue * (endY - startY))

            drawLine(
                color = primaryColor.copy(alpha = 0.1f * alpha),
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
                radius = thumbRadius * 0.4f,
                center = Offset(xCenter, activeY)
            )
        }
    }
}
