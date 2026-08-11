package com.example.ui.screens.library

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Folder
import com.example.domain.model.Genre
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import com.example.ui.components.AudioPermissionCard
import com.example.ui.components.SongListItem
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.collectAsState
import com.example.ui.components.ConfirmDeletePlaylistDialog
import com.example.ui.components.RenamePlaylistDialog
import kotlinx.coroutines.flow.Flow

@Composable
fun LibraryScreen(
    allSongs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    folders: List<Folder>,
    genres: List<Genre>,
    playlists: List<Playlist>,
    favoriteSongs: List<Song>,
    currentSong: Song?,
    initialTab: String = "SONGS",
    getPlaylistSongs: ((Long) -> Flow<List<Song>>)? = null,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onEditTagsClick: (Song) -> Unit,
    onDeleteClick: (Song) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onRenamePlaylist: ((Long, String) -> Unit)? = null,
    onDeletePlaylist: ((Long) -> Unit)? = null,
    onRemoveSongFromPlaylist: ((Long, Long) -> Unit)? = null,
    permissionState: PermissionState? = null
) {
    val tabs = listOf("SONGS", "ALBUMS", "ARTISTS", "FOLDERS", "GENRES", "PLAYLISTS", "FAVORITES")
    var selectedTabIndex by remember(initialTab) {
        mutableStateOf(tabs.indexOf(initialTab.uppercase()).coerceAtLeast(0))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        // Title
        Text(
            text = "Your Library",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        if (permissionState != null && !permissionState.status.isGranted) {
            AudioPermissionCard(
                permissionState = permissionState,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 20.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (tabs[selectedTabIndex]) {
                "SONGS" -> SongsTabContent(
                    songs = allSongs,
                    currentSong = currentSong,
                    onSongClick = onSongClick,
                    onToggleFavorite = onToggleFavorite,
                    onAddToPlaylistClick = onAddToPlaylistClick,
                    onEditTagsClick = onEditTagsClick,
                    onDeleteClick = onDeleteClick
                )
                "ALBUMS" -> AlbumsTabContent(albums = albums, allSongs = allSongs, onSongClick = onSongClick)
                "ARTISTS" -> ArtistsTabContent(artists = artists, allSongs = allSongs, onSongClick = onSongClick)
                "FOLDERS" -> FoldersTabContent(folders = folders, allSongs = allSongs, onSongClick = onSongClick)
                "GENRES" -> GenresTabContent(genres = genres, allSongs = allSongs, onSongClick = onSongClick)
                "PLAYLISTS" -> PlaylistsTabContent(
                    playlists = playlists,
                    currentSong = currentSong,
                    getPlaylistSongs = getPlaylistSongs,
                    onSongClick = onSongClick,
                    onToggleFavorite = onToggleFavorite,
                    onAddToPlaylistClick = onAddToPlaylistClick,
                    onEditTagsClick = onEditTagsClick,
                    onDeleteClick = onDeleteClick,
                    onCreatePlaylistClick = onCreatePlaylistClick,
                    onRenamePlaylist = onRenamePlaylist,
                    onDeletePlaylist = onDeletePlaylist,
                    onRemoveSongFromPlaylist = onRemoveSongFromPlaylist
                )
                "FAVORITES" -> SongsTabContent(
                    songs = favoriteSongs,
                    currentSong = currentSong,
                    onSongClick = onSongClick,
                    onToggleFavorite = onToggleFavorite,
                    onAddToPlaylistClick = onAddToPlaylistClick,
                    onEditTagsClick = onEditTagsClick,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun SongsTabContent(
    songs: List<Song>,
    currentSong: Song?,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onEditTagsClick: (Song) -> Unit,
    onDeleteClick: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No songs found", color = Color.White.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(songs) { song ->
                SongListItem(
                    song = song,
                    isCurrentPlaying = song.id == currentSong?.id,
                    onSongClick = { onSongClick(song, songs) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                    onEditTagsClick = { onEditTagsClick(song) },
                    onDeleteClick = { onDeleteClick(song) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun AlbumsTabContent(
    albums: List<Album>,
    allSongs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(albums) { album ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable {
                        val albumSongs = allSongs.filter { it.album == album.name }
                        if (albumSongs.isNotEmpty()) {
                            onSongClick(albumSongs.first(), albumSongs)
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!album.albumArtUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = album.albumArtUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Album, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = album.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text(text = "${album.artist} • ${album.songCount} Songs", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun ArtistsTabContent(
    artists: List<Artist>,
    allSongs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(artists) { artist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable {
                        val artistSongs = allSongs.filter { it.artist == artist.name }
                        if (artistSongs.isNotEmpty()) {
                            onSongClick(artistSongs.first(), artistSongs)
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = artist.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text(text = "${artist.songCount} Songs • ${artist.albumCount} Albums", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun FoldersTabContent(
    folders: List<Folder>,
    allSongs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(folders) { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable {
                        val folderSongs = allSongs.filter { it.path.startsWith(folder.path) }
                        if (folderSongs.isNotEmpty()) {
                            onSongClick(folderSongs.first(), folderSongs)
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = folder.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text(text = "${folder.songCount} Songs in Folder", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun GenresTabContent(
    genres: List<Genre>,
    allSongs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(genres) { genre ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable {
                        val genreSongs = allSongs.filter { it.genre == genre.name }
                        if (genreSongs.isNotEmpty()) {
                            onSongClick(genreSongs.first(), genreSongs)
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = genre.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text(text = "${genre.songCount} Songs", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<Playlist>,
    currentSong: Song?,
    getPlaylistSongs: ((Long) -> Flow<List<Song>>)?,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onEditTagsClick: (Song) -> Unit,
    onDeleteClick: (Song) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onRenamePlaylist: ((Long, String) -> Unit)?,
    onDeletePlaylist: ((Long) -> Unit)?,
    onRemoveSongFromPlaylist: ((Long, Long) -> Unit)?
) {
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }

    if (selectedPlaylist != null) {
        val playlist = selectedPlaylist!!
        val songsFlow = remember(playlist.id) { getPlaylistSongs?.invoke(playlist.id) }
        val playlistSongs by (songsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header with Back, Title & Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedPlaylist = null }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back to Playlists", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${playlistSongs.size} Tracks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { playlistToRename = playlist }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename Playlist", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { playlistToDelete = playlist }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Playlist", tint = MaterialTheme.colorScheme.error)
                }
            }

            // Quick Play All Action
            if (playlistSongs.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onSongClick(playlistSongs.first(), playlistSongs) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Play All", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Songs List or Empty State
            if (playlistSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs added to this playlist yet.\nUse 'Add to Playlist' from any song menu to add songs!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(playlistSongs) { song ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                SongListItem(
                                    song = song,
                                    isCurrentPlaying = song.id == currentSong?.id,
                                    onSongClick = { onSongClick(song, playlistSongs) },
                                    onToggleFavorite = { onToggleFavorite(song) },
                                    onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                                    onEditTagsClick = { onEditTagsClick(song) },
                                    onDeleteClick = { onDeleteClick(song) }
                                )
                            }
                            IconButton(
                                onClick = { onRemoveSongFromPlaylist?.invoke(playlist.id, song.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove from Playlist",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Playlists Overview List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .clickable { onCreatePlaylistClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Create New Playlist",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(playlists) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { selectedPlaylist = playlist }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Tap to view tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(onClick = { playlistToRename = playlist }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { playlistToDelete = playlist }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }

    playlistToRename?.let { pl ->
        RenamePlaylistDialog(
            currentName = pl.name,
            onDismiss = { playlistToRename = null },
            onRename = { newName ->
                onRenamePlaylist?.invoke(pl.id, newName)
                if (selectedPlaylist?.id == pl.id) {
                    selectedPlaylist = pl.copy(name = newName)
                }
                playlistToRename = null
            }
        )
    }

    playlistToDelete?.let { pl ->
        ConfirmDeletePlaylistDialog(
            playlistName = pl.name,
            onDismiss = { playlistToDelete = null },
            onConfirm = {
                onDeletePlaylist?.invoke(pl.id)
                if (selectedPlaylist?.id == pl.id) {
                    selectedPlaylist = null
                }
                playlistToDelete = null
            }
        )
    }
}
