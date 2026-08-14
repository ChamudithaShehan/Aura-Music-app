package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDeepLink
import androidx.navigation.compose.*
import androidx.navigation.navDeepLink
import com.example.domain.model.Song
import com.example.ui.components.*
import com.example.ui.navigation.NavigationActions
import com.example.ui.navigation.Screen
import com.example.ui.screens.equalizer.EqualizerScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.lyrics.LyricsScreen
import com.example.ui.screens.player.FullPlayerScreen
import com.example.ui.screens.privacy.PrivacyPolicyScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.AuraMusicTheme
import com.example.ui.viewmodel.*
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {

    private val auraApp: AuraApplication
        get() = applicationContext as AuraApplication

    private val musicViewModel: MusicViewModel by viewModels { ViewModelFactory(auraApp.appContainer, this) }
    private val playerViewModel: PlayerViewModel by viewModels { ViewModelFactory(auraApp.appContainer, this) }
    private val equalizerViewModel: EqualizerViewModel by viewModels { ViewModelFactory(auraApp.appContainer, this) }
    private val settingsViewModel: SettingsViewModel by viewModels { ViewModelFactory(auraApp.appContainer, this) }

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
    val navController = rememberNavController()
    val navActions = remember(navController) { NavigationActions(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home

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
    val lyricOffsetMs by playerViewModel.lyricOffsetMs.collectAsState()

    val equalizerState by equalizerViewModel.equalizerState.collectAsState()
    val allPresetNames by equalizerViewModel.allPresetNames.collectAsState()
    val visualizerBands by equalizerViewModel.visualizerBands.collectAsState()
    val waveform by equalizerViewModel.waveform.collectAsState()
    val rms by equalizerViewModel.rms.collectAsState()
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
    val playtimeStats by settingsViewModel.playtimeStats.collectAsState()

    // Dialog & UI states (preserved during configuration changes)
    var selectedLibraryTab by rememberSaveable { mutableStateOf("SONGS") }
    var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var songToEditTags by remember { mutableStateOf<Song?>(null) }
    var showSleepTimerDialog by rememberSaveable { mutableStateOf(false) }

    // Permission Handling
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(
        permission = audioPermission,
        onPermissionResult = { isGranted ->
            if (isGranted) musicViewModel.rescanLibrary()
        }
    )

    // Notification Permission Handling (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(notificationPermissionState.status.isGranted) {
            if (!notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }

    val bottomBarRoutes = listOf(Screen.Home, Screen.Library, Screen.Equalizer, Screen.Settings)
    val showBottomBar = currentRoute in bottomBarRoutes

    ImmersiveBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)), RoundedCornerShape(28.dp))
                    ) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            val navItems = listOf(
                                Triple(Screen.Home, "Home", Icons.Default.Home),
                                Triple(Screen.Library, "Library", Icons.Default.LibraryMusic),
                                Triple(Screen.Equalizer, "Equalizer", Icons.Default.Equalizer),
                                Triple(Screen.Settings, "Settings", Icons.Default.Settings)
                            )

                            navItems.forEach { (route, title, icon) ->
                                NavigationBarItem(
                                    selected = currentRoute == route,
                                    onClick = { navActions.navigateToTopLevelDestination(route) },
                                    icon = { Icon(imageVector = icon, contentDescription = title) },
                                    label = {
                                        Text(
                                            text = title,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            NavHost(
                navController = navController,
                startDestination = Screen.Home,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                composable(
                    route = Screen.Home,
                    deepLinks = listOf(navDeepLink { uriPattern = "aura://home" })
                ) {
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
                            if (permissionState.status.isGranted) musicViewModel.rescanLibrary()
                            else permissionState.launchPermissionRequest()
                        },
                        onNavigateToLibraryTab = { tab ->
                            selectedLibraryTab = tab
                            navActions.navigateToTopLevelDestination(Screen.Library)
                        },
                        onNavigateToEqualizer = { navActions.navigateToTopLevelDestination(Screen.Equalizer) },
                        onNavigateToSearch = { navActions.navigateToTopLevelDestination(Screen.Search) },
                        permissionState = permissionState
                    )
                }

                composable(
                    route = Screen.Library,
                    deepLinks = listOf(navDeepLink { uriPattern = "aura://library" })
                ) {
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
                        getPlaylistSongs = { musicViewModel.getPlaylistSongs(it) },
                        onSongClick = { song, queue -> playerViewModel.playSong(song, queue) },
                        onToggleFavorite = { song -> musicViewModel.toggleFavorite(song) },
                        onAddToPlaylistClick = { song -> songToAddToPlaylist = song },
                        onEditTagsClick = { song -> songToEditTags = song },
                        onDeleteClick = { song -> musicViewModel.deleteSong(song.id) },
                        onCreatePlaylistClick = { showCreatePlaylistDialog = true },
                        onRenamePlaylist = { id, name -> musicViewModel.renamePlaylist(id, name) },
                        onDeletePlaylist = { musicViewModel.deletePlaylist(it) },
                        onRemoveSongFromPlaylist = { pid, sid -> musicViewModel.removeSongFromPlaylist(pid, sid) },
                        permissionState = permissionState
                    )
                }

                composable(
                    route = Screen.Equalizer,
                    deepLinks = listOf(navDeepLink { uriPattern = "aura://equalizer" })
                ) {
                    EqualizerScreen(
                        equalizerState = equalizerState,
                        allPresets = allPresetNames,
                        visualizerBands = visualizerBands,
                        waveform = waveform,
                        rms = rms,
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

                composable(
                    route = Screen.Search,
                    deepLinks = listOf(navDeepLink { uriPattern = "aura://search" })
                ) {
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

                composable(
                    route = Screen.Settings,
                    deepLinks = listOf(navDeepLink { uriPattern = "aura://settings" })
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val googleDriveBackupHelper = remember(context) { com.example.data.backup.GoogleDriveBackupHelper(context) }
                    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                            if (account != null) {
                                settingsViewModel.onGoogleSignInSuccess(account)
                                android.widget.Toast.makeText(context, "Signed in as ${account.email}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: com.google.android.gms.common.api.ApiException) {
                            android.widget.Toast.makeText(context, "Google Sign-In cancelled or failed (${e.statusCode})", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }

                    SettingsScreen(
                        themeMode = themeMode,
                        sleepTimerRemainingSec = sleepTimerRemainingSec,
                        userEmail = userEmail,
                        isBackingUp = isBackingUp,
                        isRestoring = isRestoring,
                        backupProgress = backupProgress,
                        statusMessage = statusMessage,
                        lastBackupInfo = lastBackupInfo,
                        playtimeStats = playtimeStats,
                        language = language,
                        gaplessPlayback = gaplessPlayback,
                        crossfadeSeconds = crossfadeSeconds,
                        pauseOnDisconnect = pauseOnDisconnect,
                        lockscreenArt = lockscreenArt,
                        onSetThemeMode = { settingsViewModel.setThemeMode(it) },
                        onRescanClick = { musicViewModel.rescanLibrary() },
                        onNavigateToPrivacyPolicy = { navActions.navigateTo(Screen.Privacy) },
                        onStartSleepTimer = { playerViewModel.startSleepTimer(it) },
                        onCancelSleepTimer = { playerViewModel.cancelSleepTimer() },
                        onLoginGoogleAccount = { googleSignInLauncher.launch(googleDriveBackupHelper.getGoogleSignInClient().signInIntent) },
                        onLogoutGoogleAccount = { settingsViewModel.logoutGoogleAccount() },
                        onPerformBackup = { settingsViewModel.performBackup() },
                        onPerformRestore = { settingsViewModel.performRestore() },
                        onSetLanguage = { settingsViewModel.setLanguage(it) },
                        onSetGaplessPlayback = { settingsViewModel.setGaplessPlayback(it) },
                        onSetCrossfadeSeconds = { settingsViewModel.setCrossfadeSeconds(it) },
                        onSetPauseOnDisconnect = { settingsViewModel.setPauseOnDisconnect(it) },
                        onSetLockscreenArt = { settingsViewModel.setLockscreenArt(it) },
                        onBackClick = { navActions.popBack() }
                    )
                }

                composable(
                    route = Screen.Player,
                    deepLinks = listOf(navDeepLink { uriPattern = "aura://player" }),
                    enterTransition = { slideInVertically(initialOffsetY = { it }) },
                    exitTransition = { slideOutVertically(targetOffsetY = { it }) }
                ) {
                    FullPlayerScreen(
                        song = currentSong,
                        isPlaying = isPlaying,
                        progressMs = currentPositionMs,
                        durationMs = durationMs,
                        shuffleModeEnabled = shuffleModeEnabled,
                        repeatMode = repeatMode,
                        playbackSpeed = playbackSpeed,
                        visualizerBands = visualizerBands,
                        rms = rms,
                        onPlayPauseClick = { playerViewModel.togglePlayPause() },
                        onNextClick = { playerViewModel.nextSong() },
                        onPreviousClick = { playerViewModel.previousSong() },
                        onSeekTo = { playerViewModel.seekTo(it) },
                        onToggleShuffle = { playerViewModel.toggleShuffle() },
                        onCycleRepeatMode = { playerViewModel.cycleRepeatMode() },
                        onToggleFavorite = { playerViewModel.toggleFavoriteCurrentSong() },
                        onSpeedChange = { playerViewModel.setPlaybackSpeed(it) },
                        onOpenLyricsClick = { navActions.navigateTo(Screen.Lyrics) },
                        onOpenSleepTimerClick = { showSleepTimerDialog = true },
                        onOpenEqualizerClick = { navActions.navigateToTopLevelDestination(Screen.Equalizer) },
                        onCloseClick = { navActions.popBack() }
                    )
                }

                composable(
                    route = Screen.Lyrics,
                    deepLinks = listOf(navDeepLink { uriPattern = "aura://lyrics" })
                ) {
                    LyricsScreen(
                        song = currentSong,
                        parsedLyrics = parsedLyrics,
                        currentPositionMs = currentPositionMs,
                        lyricOffsetMs = lyricOffsetMs,
                        onAdjustOffsetMs = { playerViewModel.adjustLyricOffsetMs(it) },
                        onSaveLyrics = { playerViewModel.saveLyricsForCurrentSong(it) },
                        onSearchLyrics = { currentSong?.let { song -> playerViewModel.setSearchingLyricsSongId(song.id) } },
                        onCheckClipboard = { ctx -> currentSong?.let { song -> playerViewModel.checkAndAutoSaveClipboardLyrics(ctx, song) } },
                        onSeekTo = { playerViewModel.seekTo(it) },
                        onBackClick = { navActions.popBack() }
                    )
                }

                composable(
                    route = Screen.Privacy,
                    deepLinks = listOf(navDeepLink { uriPattern = "aura://privacy" })
                ) {
                    PrivacyPolicyScreen(onBackClick = { navActions.popBack() })
                }
            }

            // Mini Player - Only shown when a song is loaded and NOT on the Player screen
            if (currentSong != null && currentRoute != Screen.Player && currentRoute != Screen.Lyrics) {
                MiniPlayerBar(
                    song = currentSong,
                    isPlaying = isPlaying,
                    progressMs = currentPositionMs,
                    durationMs = durationMs,
                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                    onNextClick = { playerViewModel.nextSong() },
                    onExpandClick = { navActions.navigateTo(Screen.Player) },
                    onDismiss = { playerViewModel.stopPlayback() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
    }

    // Dialogs
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { musicViewModel.createPlaylist(it); showCreatePlaylistDialog = false }
        )
    }

    songToAddToPlaylist?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { songToAddToPlaylist = null },
            onSelectPlaylist = { musicViewModel.addSongToPlaylist(it.id, song.id); songToAddToPlaylist = null },
            onCreateNewPlaylistClick = { songToAddToPlaylist = null; showCreatePlaylistDialog = true }
        )
    }

    songToEditTags?.let { song ->
        TagEditorDialog(
            song = song,
            onDismiss = { songToEditTags = null },
            onSave = { t, a, al, g -> musicViewModel.updateMetadata(song.id, t, a, al, g); songToEditTags = null }
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            activeRemainingSec = sleepTimerRemainingSec,
            onDismiss = { showSleepTimerDialog = false },
            onSetTimer = { playerViewModel.startSleepTimer(it); showSleepTimerDialog = false },
            onCancelTimer = { playerViewModel.cancelSleepTimer(); showSleepTimerDialog = false }
        )
    }
}
