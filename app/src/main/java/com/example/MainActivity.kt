package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ImmersiveBackground

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.TagEditorDialog
import com.example.ui.screens.equalizer.EqualizerScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.lyrics.LyricsScreen
import com.example.ui.screens.player.FullPlayerScreen
import com.example.ui.screens.privacy.PrivacyPolicyScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.AuraMusicTheme
import com.example.ui.viewmodel.EqualizerViewModel
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.PlayerViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.ViewModelFactory
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {

    private val auraApp: AuraApplication
        get() = applicationContext as AuraApplication

    private val musicViewModel: MusicViewModel by viewModels { ViewModelFactory(auraApp.appContainer) }
    private val playerViewModel: PlayerViewModel by viewModels { ViewModelFactory(auraApp.appContainer) }
    private val equalizerViewModel: EqualizerViewModel by viewModels { ViewModelFactory(auraApp.appContainer) }
    private val settingsViewModel: SettingsViewModel by viewModels { ViewModelFactory(auraApp.appContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()

            AuraMusicTheme(themeMode = themeMode) {
                MainAppContent(
                    musicViewModel = musicViewModel,
                    playerViewModel = playerViewModel,
                    equalizerViewModel = equalizerViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    musicViewModel: MusicViewModel,
    playerViewModel: PlayerViewModel,
    equalizerViewModel: EqualizerViewModel,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // State collections
    val allSongs by musicViewModel.allSongs.collectAsState()
    val favoriteSongs by musicViewModel.favoriteSongs.collectAsState()
    val recentlyPlayed by musicViewModel.recentlyPlayed.collectAsState()
    val mostPlayed by musicViewModel.mostPlayed.collectAsState()
    val newlyAdded by musicViewModel.newlyAdded.collectAsState()
    val albums by musicViewModel.albums.collectAsState()
    val artists by musicViewModel.artists.collectAsState()
    val folders by musicViewModel.folders.collectAsState()
    val genres by musicViewModel.genres.collectAsState()
    val playlists by musicViewModel.playlists.collectAsState()
    val recentSearches by musicViewModel.recentSearches.collectAsState()
    val searchQuery by musicViewModel.searchQuery.collectAsState()
    val searchResults by musicViewModel.searchResults.collectAsState()

    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentPositionMs by playerViewModel.currentPositionMs.collectAsState()
    val durationMs by playerViewModel.durationMs.collectAsState()
    val shuffleModeEnabled by playerViewModel.shuffleModeEnabled.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()
    val playbackSpeed by playerViewModel.playbackSpeed.collectAsState()
    val sleepTimerRemainingSec by playerViewModel.sleepTimerRemainingSec.collectAsState()
    val parsedLyrics by playerViewModel.parsedLyrics.collectAsState()

    val equalizerState by equalizerViewModel.equalizerState.collectAsState()
    val allPresetNames by equalizerViewModel.allPresetNames.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState()

    val userEmail by settingsViewModel.userEmail.collectAsState()
    val isBackingUp by settingsViewModel.isBackingUp.collectAsState()
    val isRestoring by settingsViewModel.isRestoring.collectAsState()
    val backupProgress by settingsViewModel.backupProgress.collectAsState()
    val statusMessage by settingsViewModel.statusMessage.collectAsState()
    val lastBackupInfo by settingsViewModel.lastBackupInfo.collectAsState()

    val language by settingsViewModel.language.collectAsState()
    val gaplessPlayback by settingsViewModel.gaplessPlayback.collectAsState()
    val crossfadeSeconds by settingsViewModel.crossfadeSeconds.collectAsState()
    val pauseOnDisconnect by settingsViewModel.pauseOnDisconnect.collectAsState()
    val lockscreenArt by settingsViewModel.lockscreenArt.collectAsState()

    // Dialog & Screen Expansion states
    var isFullPlayerExpanded by remember { mutableStateOf(false) }
    var selectedLibraryTab by remember { mutableStateOf("SONGS") }

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var songToEditTags by remember { mutableStateOf<Song?>(null) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    // Accompanist Permission State Flow
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(
        permission = audioPermission,
        onPermissionResult = { isGranted ->
            if (isGranted) {
                musicViewModel.rescanLibrary()
            }
        }
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    ImmersiveBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (!isFullPlayerExpanded && currentRoute in listOf("home", "library", "equalizer", "search", "settings")) {
                    Box(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(28.dp))
                    ) {
                        NavigationBar(
                            containerColor = Color(0xFF1A1A1E),
                            contentColor = Color.White
                        ) {
                            val navItems = listOf(
                                Triple("home", "Home", Icons.Default.Home),
                                Triple("library", "Library", Icons.Default.LibraryMusic),
                                Triple("equalizer", "Equalizer", Icons.Default.Equalizer),
                                Triple("search", "Search", Icons.Default.Search),
                                Triple("settings", "Settings", Icons.Default.Settings)
                            )

                            navItems.forEach { (route, title, icon) ->
                                NavigationBarItem(
                                    selected = currentRoute == route,
                                    onClick = {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(imageVector = icon, contentDescription = title) },
                                    label = {
                                        Text(
                                            text = title,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = Color(0xFFD0BCFF),
                                        indicatorColor = Color(0xFFD0BCFF),
                                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Screen Navigation Host
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        allSongs = allSongs,
                        recentlyPlayed = recentlyPlayed,
                        mostPlayed = mostPlayed,
                        newlyAdded = newlyAdded,
                        favoriteSongs = favoriteSongs,
                        currentSong = currentSong,
                        onSongClick = { song, queue -> playerViewModel.playSong(song, queue) },
                        onToggleFavorite = { song -> musicViewModel.toggleFavorite(song) },
                        onAddToPlaylistClick = { song -> songToAddToPlaylist = song },
                        onEditTagsClick = { song -> songToEditTags = song },
                        onDeleteClick = { song -> musicViewModel.deleteSong(song.id) },
                        onRescanClick = {
                            if (permissionState.status.isGranted) {
                                musicViewModel.rescanLibrary()
                            } else {
                                permissionState.launchPermissionRequest()
                            }
                        },
                        onNavigateToLibraryTab = { tab ->
                            selectedLibraryTab = tab
                            navController.navigate("library")
                        },
                        onNavigateToEqualizer = { navController.navigate("equalizer") },
                        onNavigateToSearch = { navController.navigate("search") },
                        permissionState = permissionState
                    )
                }

                composable("library") {
                    LibraryScreen(
                        allSongs = allSongs,
                        albums = albums,
                        artists = artists,
                        folders = folders,
                        genres = genres,
                        playlists = playlists,
                        favoriteSongs = favoriteSongs,
                        currentSong = currentSong,
                        initialTab = selectedLibraryTab,
                        getPlaylistSongs = { playlistId -> musicViewModel.getPlaylistSongs(playlistId) },
                        onSongClick = { song, queue -> playerViewModel.playSong(song, queue) },
                        onToggleFavorite = { song -> musicViewModel.toggleFavorite(song) },
                        onAddToPlaylistClick = { song -> songToAddToPlaylist = song },
                        onEditTagsClick = { song -> songToEditTags = song },
                        onDeleteClick = { song -> musicViewModel.deleteSong(song.id) },
                        onCreatePlaylistClick = { showCreatePlaylistDialog = true },
                        onRenamePlaylist = { playlistId, newName -> musicViewModel.renamePlaylist(playlistId, newName) },
                        onDeletePlaylist = { playlistId -> musicViewModel.deletePlaylist(playlistId) },
                        onRemoveSongFromPlaylist = { playlistId, songId -> musicViewModel.removeSongFromPlaylist(playlistId, songId) },
                        permissionState = permissionState
                    )
                }

                composable("equalizer") {
                    EqualizerScreen(
                        equalizerState = equalizerState,
                        allPresets = allPresetNames,
                        onToggleMaster = { equalizerViewModel.toggleMaster(it) },
                        onSelectPreset = { equalizerViewModel.selectPreset(it) },
                        onBandLevelChange = { index, level -> equalizerViewModel.setBandLevel(index, level) },
                        onToggleBassBoost = { equalizerViewModel.toggleBassBoost(it) },
                        onBassBoostStrengthChange = { equalizerViewModel.setBassBoostStrength(it) },
                        onToggleVirtualizer = { equalizerViewModel.toggleVirtualizer(it) },
                        onVirtualizerStrengthChange = { equalizerViewModel.setVirtualizerStrength(it) },
                        onToggleLoudness = { equalizerViewModel.toggleLoudness(it) },
                        onLoudnessGainChange = { equalizerViewModel.setLoudnessGain(it) },
                        onReset = { equalizerViewModel.reset() },
                        onSavePreset = { equalizerViewModel.saveCustomPreset(it) },
                        onDeletePreset = { equalizerViewModel.deletePreset(it) }
                    )
                }

                composable("search") {
                    SearchScreen(
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        recentSearches = recentSearches,
                        currentSong = currentSong,
                        onQueryChange = { musicViewModel.setSearchQuery(it) },
                        onSubmitSearch = { musicViewModel.submitSearch(it) },
                        onClearHistory = { musicViewModel.clearSearchHistory() },
                        onSongClick = { song, queue -> playerViewModel.playSong(song, queue) },
                        onToggleFavorite = { song -> musicViewModel.toggleFavorite(song) },
                        onAddToPlaylistClick = { song -> songToAddToPlaylist = song },
                        onEditTagsClick = { song -> songToEditTags = song },
                        onDeleteClick = { song -> musicViewModel.deleteSong(song.id) }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        themeMode = themeMode,
                        sleepTimerRemainingSec = sleepTimerRemainingSec,
                        userEmail = userEmail,
                        isBackingUp = isBackingUp,
                        isRestoring = isRestoring,
                        backupProgress = backupProgress,
                        statusMessage = statusMessage,
                        lastBackupInfo = lastBackupInfo,
                        language = language,
                        gaplessPlayback = gaplessPlayback,
                        crossfadeSeconds = crossfadeSeconds,
                        pauseOnDisconnect = pauseOnDisconnect,
                        lockscreenArt = lockscreenArt,
                        onSetThemeMode = { settingsViewModel.setThemeMode(it) },
                        onRescanClick = { musicViewModel.rescanLibrary() },
                        onNavigateToPrivacyPolicy = { navController.navigate("privacy") },
                        onStartSleepTimer = { playerViewModel.startSleepTimer(it) },
                        onCancelSleepTimer = { playerViewModel.cancelSleepTimer() },
                        onLoginGoogleAccount = { email -> settingsViewModel.loginGoogleAccount(email) },
                        onLogoutGoogleAccount = { settingsViewModel.logoutGoogleAccount() },
                        onPerformBackup = { settingsViewModel.performBackup() },
                        onPerformRestore = { settingsViewModel.performRestore() },
                        onSetLanguage = { settingsViewModel.setLanguage(it) },
                        onSetGaplessPlayback = { settingsViewModel.setGaplessPlayback(it) },
                        onSetCrossfadeSeconds = { settingsViewModel.setCrossfadeSeconds(it) },
                        onSetPauseOnDisconnect = { settingsViewModel.setPauseOnDisconnect(it) },
                        onSetLockscreenArt = { settingsViewModel.setLockscreenArt(it) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("lyrics") {
                    LyricsScreen(
                        song = currentSong,
                        parsedLyrics = parsedLyrics,
                        currentPositionMs = currentPositionMs,
                        onSaveLyrics = { playerViewModel.saveLyricsForCurrentSong(it) },
                        onSeekTo = { playerViewModel.seekTo(it) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("privacy") {
                    PrivacyPolicyScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            // Bottom Mini Player Overlay
            if (currentSong != null && !isFullPlayerExpanded && currentRoute != "lyrics") {
                MiniPlayerBar(
                    song = currentSong,
                    isPlaying = isPlaying,
                    progressMs = currentPositionMs,
                    durationMs = durationMs,
                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                    onNextClick = { playerViewModel.nextSong() },
                    onExpandClick = { isFullPlayerExpanded = true },
                    onDismiss = { playerViewModel.stopPlayback() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Expanded Full Player Modal Sheet
            AnimatedVisibility(
                visible = isFullPlayerExpanded,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                val visualizerBands by playerViewModel.visualizerBands.collectAsState()
                FullPlayerScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    progressMs = currentPositionMs,
                    durationMs = durationMs,
                    shuffleModeEnabled = shuffleModeEnabled,
                    repeatMode = repeatMode,
                    playbackSpeed = playbackSpeed,
                    visualizerBands = visualizerBands,
                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                    onNextClick = { playerViewModel.nextSong() },
                    onPreviousClick = { playerViewModel.previousSong() },
                    onSeekTo = { playerViewModel.seekTo(it) },
                    onToggleShuffle = { playerViewModel.toggleShuffle() },
                    onCycleRepeatMode = { playerViewModel.cycleRepeatMode() },
                    onToggleFavorite = { playerViewModel.toggleFavoriteCurrentSong() },
                    onSpeedChange = { playerViewModel.setPlaybackSpeed(it) },
                    onOpenLyricsClick = {
                        isFullPlayerExpanded = false
                        navController.navigate("lyrics")
                    },
                    onOpenSleepTimerClick = { showSleepTimerDialog = true },
                    onOpenEqualizerClick = {
                        isFullPlayerExpanded = false
                        navController.navigate("equalizer")
                    },
                    onCloseClick = { isFullPlayerExpanded = false }
                )
            }
        }
    }
    }

    // Dialog Overlays
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name ->
                musicViewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }

    songToAddToPlaylist?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { songToAddToPlaylist = null },
            onSelectPlaylist = { playlist ->
                musicViewModel.addSongToPlaylist(playlist.id, song.id)
                songToAddToPlaylist = null
            },
            onCreateNewPlaylistClick = {
                songToAddToPlaylist = null
                showCreatePlaylistDialog = true
            }
        )
    }

    songToEditTags?.let { song ->
        TagEditorDialog(
            song = song,
            onDismiss = { songToEditTags = null },
            onSave = { title, artist, album, genre ->
                musicViewModel.updateMetadata(song.id, title, artist, album, genre)
                songToEditTags = null
            }
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            activeRemainingSec = sleepTimerRemainingSec,
            onDismiss = { showSleepTimerDialog = false },
            onSetTimer = { mins ->
                playerViewModel.startSleepTimer(mins)
                showSleepTimerDialog = false
            },
            onCancelTimer = {
                playerViewModel.cancelSleepTimer()
                showSleepTimerDialog = false
            }
        )
    }
}

