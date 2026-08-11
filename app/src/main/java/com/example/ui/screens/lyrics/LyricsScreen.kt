package com.example.ui.screens.lyrics

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.domain.model.LyricsLine
import com.example.domain.model.Song

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune

@Composable
fun LyricsScreen(
    song: Song?,
    parsedLyrics: List<LyricsLine>,
    currentPositionMs: Long,
    lyricOffsetMs: Long = 0L,
    onAdjustOffsetMs: (Long) -> Unit = {},
    onSaveLyrics: (String) -> Unit,
    onSearchLyrics: () -> Unit = {},
    onCheckClipboard: (android.content.Context) -> Unit = {},
    onSeekTo: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    if (song == null) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showOffsetControls by remember { mutableStateOf(false) }
    var editLyricsText by remember { mutableStateOf(song.lyrics) }
    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    // Pause auto-scroll when user manually drags the list
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(isDragged) {
        if (isDragged) {
            isAutoScrollEnabled = false
        }
    }

    // Helper to open browser search autofilled with song title and artist
    val openBrowserSearch: () -> Unit = {
        onSearchLyrics()
        val query = Uri.encode("${song.title} ${song.artist} lyrics".trim())
        val url = "https://www.google.com/search?q=$query"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // Listen to ON_RESUME lifecycle event to auto-detect copied lyrics from browser
    DisposableEffect(lifecycleOwner, song.id) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onCheckClipboard(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Determine current active lyric index with exact timing synchronization including offset adjustment
    val effectivePositionMs = (currentPositionMs + lyricOffsetMs).coerceAtLeast(0L)
    val activeIndex = remember(effectivePositionMs, parsedLyrics) {
        val index = parsedLyrics.indexOfLast { it.timeMs <= effectivePositionMs }
        if (index >= 0) index else 0
    }

    // Auto scroll list down synchronously with current position
    LaunchedEffect(activeIndex, isAutoScrollEnabled) {
        if (isAutoScrollEnabled && parsedLyrics.isNotEmpty() && activeIndex in parsedLyrics.indices) {
            listState.animateScrollToItem(activeIndex, scrollOffset = -220)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("lyrics_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Small Auto-Scroll / Sync toggle button enabled when lyrics exist
                    if (parsedLyrics.isNotEmpty()) {
                        IconButton(onClick = { showOffsetControls = !showOffsetControls }) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Sync Offset Tuner",
                                tint = if (showOffsetControls || lyricOffsetMs != 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilterChip(
                            selected = isAutoScrollEnabled,
                            onClick = { isAutoScrollEnabled = !isAutoScrollEnabled },
                            label = { Text("Auto-Scroll", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VerticalAlignCenter,
                                    contentDescription = "Sync Auto Scroll",
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    TextButton(onClick = openBrowserSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search lyrics",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Search", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Lyrics",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expandable Sync Offset Tuner Bar
            AnimatedVisibility(
                visible = showOffsetControls && parsedLyrics.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Timing Sync:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val formattedOffset = if (lyricOffsetMs >= 0) "+%.1fs".format(lyricOffsetMs / 1000.0) else "%.1fs".format(lyricOffsetMs / 1000.0)
                            Text(
                                text = formattedOffset,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (lyricOffsetMs != 0L) {
                                IconButton(
                                    onClick = { onAdjustOffsetMs(-lyricOffsetMs) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset Sync Offset",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { onAdjustOffsetMs(-1000L) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("-1.0s", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { onAdjustOffsetMs(-500L) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("-0.5s", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { onAdjustOffsetMs(500L) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("+0.5s", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { onAdjustOffsetMs(1000L) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("+1.0s", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Body
            if (parsedLyrics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "This song doesn't have lyrics",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Search online and copy lyrics to automatically save them for this song",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = openBrowserSearch,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Search lyrics",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: ""
                                        if (clipText.isNotBlank()) {
                                            onSaveLyrics(clipText)
                                            android.widget.Toast.makeText(context, "Lyrics pasted from clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Clipboard is empty", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paste Clipboard")
                            }
                            OutlinedButton(
                                onClick = { showEditDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(top = 80.dp, bottom = 140.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(parsedLyrics) { index, line ->
                        val isActive = index == activeIndex
                        val textColor by animateColorAsState(
                            targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            label = "lyric_color"
                        )

                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                fontSize = if (isActive) 24.sp else 18.sp,
                                textAlign = TextAlign.Center
                            ),
                            color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSeekTo(line.timeMs)
                                    isAutoScrollEnabled = true
                                }
                                .padding(vertical = 12.dp, horizontal = 24.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        OutlinedButton(
                            onClick = openBrowserSearch,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Search lyrics online", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }

        // Floating "Sync with Song" / Resume Auto-Scroll Button
        AnimatedVisibility(
            visible = parsedLyrics.isNotEmpty() && !isAutoScrollEnabled,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Surface(
                onClick = { isAutoScrollEnabled = true },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shadowElevation = 8.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerticalAlignCenter,
                        contentDescription = "Sync with Song",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sync with Song",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Song Lyrics") },
            text = {
                OutlinedTextField(
                    value = editLyricsText,
                    onValueChange = { editLyricsText = it },
                    placeholder = { Text("Paste or type song lyrics here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveLyrics(editLyricsText)
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
