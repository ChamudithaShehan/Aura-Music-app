package com.example.ui.screens.lyrics

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LyricsLine
import com.example.domain.model.Song

@Composable
fun LyricsScreen(
    song: Song?,
    parsedLyrics: List<LyricsLine>,
    currentPositionMs: Long,
    onSaveLyrics: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    if (song == null) return

    var showEditDialog by remember { mutableStateOf(false) }
    var editLyricsText by remember { mutableStateOf(song.lyrics) }
    val listState = rememberLazyListState()

    // Determine current active lyric index
    val activeIndex = remember(currentPositionMs, parsedLyrics) {
        val index = parsedLyrics.indexOfLast { it.timeMs <= currentPositionMs }
        if (index >= 0) index else 0
    }

    // Auto scroll list to active line
    LaunchedEffect(activeIndex) {
        if (parsedLyrics.isNotEmpty() && activeIndex in parsedLyrics.indices) {
            listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0817))
            .testTag("lyrics_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = { showEditDialog = true }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Lyrics", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Synced Lyrics List
        if (parsedLyrics.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Synced Lyrics Available",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showEditDialog = true }) {
                        Text("Add Lyrics (LRC Format)", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                itemsIndexed(parsedLyrics) { index, line ->
                    val isActive = index == activeIndex
                    val textColor by animateColorAsState(
                        targetValue = if (isActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.35f),
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
                            .clickable { onSeekTo(line.timeMs) }
                            .padding(vertical = 12.dp, horizontal = 24.dp)
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit LRC Synced Lyrics") },
            text = {
                OutlinedTextField(
                    value = editLyricsText,
                    onValueChange = { editLyricsText = it },
                    placeholder = { Text("[00:12.34] Lyrics line here...") },
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
