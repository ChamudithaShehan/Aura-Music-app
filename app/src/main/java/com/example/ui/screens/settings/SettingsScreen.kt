package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupInfo
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.SleepTimerDialog

@Composable
fun SettingsScreen(
    themeMode: String,
    sleepTimerRemainingSec: Int? = null,
    userEmail: String? = null,
    isBackingUp: Boolean = false,
    isRestoring: Boolean = false,
    backupProgress: Float = 0f,
    statusMessage: String = "",
    lastBackupInfo: BackupInfo? = null,
    language: String = "English",
    gaplessPlayback: Boolean = true,
    crossfadeSeconds: Int = 2,
    pauseOnDisconnect: Boolean = true,
    lockscreenArt: Boolean = true,
    onSetThemeMode: (String) -> Unit,
    onRescanClick: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onStartSleepTimer: (Int) -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    onLoginGoogleAccount: (String) -> Unit = {},
    onLogoutGoogleAccount: () -> Unit = {},
    onPerformBackup: () -> Unit = {},
    onPerformRestore: () -> Unit = {},
    onSetLanguage: (String) -> Unit = {},
    onSetGaplessPlayback: (Boolean) -> Unit = {},
    onSetCrossfadeSeconds: (Int) -> Unit = {},
    onSetPauseOnDisconnect: (Boolean) -> Unit = {},
    onSetLockscreenArt: (Boolean) -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    var showPlaytimeDialog by remember { mutableStateOf(false) }
    var showHiddenFilesDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showRecentlyDeletedDialog by remember { mutableStateOf(false) }
    var showPlaybackSettingsDialog by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }

    val cardBg = Color(0xFF16161A)
    val dividerColor = Color.White.copy(alpha = 0.05f)
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val premiumColor = Color(0xFFEA80FC)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("settings_screen")
    ) {
        // Top Navigation Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = textPrimary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // SECTION 1: ACCOUNT & CLOUD
        SettingsSectionHeader(title = "Account & Cloud")
        SettingsGroupCard(cardBg = cardBg) {
            SettingRowItem(
                icon = Icons.Default.BarChart,
                title = "Playtime Statistics",
                value = "12m 54s",
                onClick = { showPlaytimeDialog = true }
            )
            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            val backupValueText = if (userEmail != null) {
                if (lastBackupInfo != null) "Backup (${lastBackupInfo.compressedSizeFormatted})" else "Logged in"
            } else {
                "Log in"
            }
            SettingRowItem(
                icon = Icons.Default.Backup,
                title = "Backup & Restore",
                value = backupValueText,
                valueColor = if (userEmail != null) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                onClick = { showBackupRestoreDialog = true }
            )
            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            SettingRowItem(
                icon = Icons.Default.Diamond,
                title = "Aura Premium",
                value = "Permanent",
                titleColor = premiumColor,
                valueColor = premiumColor,
                iconTint = premiumColor,
                iconBgColor = premiumColor.copy(alpha = 0.15f),
                onClick = { showPremiumDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: AUDIO & APPEARANCE
        SettingsSectionHeader(title = "Audio & Appearance")
        SettingsGroupCard(cardBg = cardBg) {
            SettingRowItem(
                icon = Icons.Default.ColorLens,
                title = "Theme",
                value = getThemeDisplayLabel(themeMode),
                onClick = { showThemeDialog = true }
            )
            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            val timerDisplayValue = if (sleepTimerRemainingSec != null) {
                val mins = sleepTimerRemainingSec / 60
                val secs = sleepTimerRemainingSec % 60
                String.format("%02d:%02d", mins, secs)
            } else {
                "Off"
            }
            SettingRowItem(
                icon = Icons.Default.Timer,
                title = "Sleep Timer",
                value = timerDisplayValue,
                valueColor = if (sleepTimerRemainingSec != null) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                onClick = { showSleepTimerDialog = true }
            )
            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            SettingRowItem(
                icon = Icons.Default.Headphones,
                title = "Playback Settings",
                value = if (gaplessPlayback) "Gapless" else "Standard",
                onClick = { showPlaybackSettingsDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: LIBRARY & STORAGE
        SettingsSectionHeader(title = "Library & Storage")
        SettingsGroupCard(cardBg = cardBg) {
            SettingRowItem(
                icon = Icons.Default.Refresh,
                title = "Rescan Library",
                value = "Sync files",
                onClick = {
                    onRescanClick()
                    Toast.makeText(context, "Rescanning music library...", Toast.LENGTH_SHORT).show()
                }
            )
            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            SettingRowItem(
                icon = Icons.Default.VisibilityOff,
                title = "Hidden Files & Folders",
                value = "36 files",
                onClick = { showHiddenFilesDialog = true }
            )
            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            SettingRowItem(
                icon = Icons.Default.Delete,
                title = "Recently Deleted",
                value = "0 files",
                onClick = { showRecentlyDeletedDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: GENERAL PREFERENCES
        SettingsSectionHeader(title = "General Preferences")
        SettingsGroupCard(cardBg = cardBg) {
            SettingRowItem(
                icon = Icons.Default.Notifications,
                title = "Notification Settings",
                value = if (lockscreenArt) "Lockscreen Art" else "Enabled",
                onClick = { showNotificationSettingsDialog = true }
            )
            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            SettingRowItem(
                icon = Icons.Default.Language,
                title = "App Language",
                value = language,
                onClick = { showLanguageDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 5: ABOUT & SUPPORT
        SettingsSectionHeader(title = "About & Support")
        SettingsGroupCard(cardBg = cardBg) {
            SettingRowItem(
                icon = Icons.Default.Feedback,
                title = "Feedback & Review",
                value = "Play Store",
                onClick = { showFeedbackDialog = true }
            )
            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            SettingRowItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                value = "",
                onClick = onNavigateToPrivacyPolicy
            )
            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            SettingRowItem(
                icon = Icons.Default.Info,
                title = "About Aura Music",
                value = "v2026.15.0",
                onClick = { showAboutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }

    // --- DIALOGS ---

    // 1. Playtime Statistics Dialog
    if (showPlaytimeDialog) {
        AlertDialog(
            onDismissRequest = { showPlaytimeDialog = false },
            containerColor = Color(0xFF1E1E24),
            titleContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Playtime Statistics", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF282834)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Listen Time", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("12m 54s", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tracks Played: 18 songs", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("Daily Average: 24 mins", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("Favorite Genre: Acoustic / Pop", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                    Text(
                        "Listening metrics are calculated locally on your device for privacy.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaytimeDialog = false }) {
                    Text("Close", color = Color.White.copy(alpha = 0.8f))
                }
            }
        )
    }

    // 2. Hidden Files Dialog
    if (showHiddenFilesDialog) {
        AlertDialog(
            onDismissRequest = { showHiddenFilesDialog = false },
            containerColor = Color(0xFF1E1E24),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Hidden Files (36 files)", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "WhatsApp Voice Messages, PTT recordings, and short audio notes are automatically filtered out from your music library.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF282830)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("WhatsApp Voice Notes", fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("36 files", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "ptt-*.opus, aud-*.opus, /WhatsApp/Media/Voice Notes/",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF282830)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("System Ringtones & Notifications", fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("Filtered", color = Color.Green, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Non-music audio under 10 seconds excluded",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRescanClick()
                        showHiddenFilesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rescan Library")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHiddenFilesDialog = false }) {
                    Text("Close", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    // 3. Theme Selection Dialog
    if (showThemeDialog) {
        val themeOptions = listOf("SYSTEM", "DARK", "LIGHT", "AMOLED", "DYNAMIC")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = Color(0xFF1E1E24),
            titleContentColor = Color.White,
            title = { Text("Select Theme Mode", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themeOptions.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = getThemeDisplayLabel(mode), color = Color.White)
                            if (themeMode == mode) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    // 4. Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            activeRemainingSec = sleepTimerRemainingSec,
            onDismiss = { showSleepTimerDialog = false },
            onSetTimer = { mins ->
                onStartSleepTimer(mins)
                showSleepTimerDialog = false
            },
            onCancelTimer = {
                onCancelSleepTimer()
                showSleepTimerDialog = false
            }
        )
    }

    // 5. Premium Status Dialog
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            containerColor = Color(0xFF1E1E24),
            titleContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = null,
                        tint = premiumColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Aura Premium Unlocked", fontWeight = FontWeight.Bold, color = premiumColor)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Your device has full permanent access to all pro audio features:",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    listOf(
                        "⚡ High-Fidelity 24-Bit FLAC Audio Engine",
                        "☁️ Google Drive Compressed Backup & Restore",
                        "🎛️ 10-Band Graphic Equalizer & Bass Boost",
                        "🎨 AMOLED Black & Dynamic Material You Themes",
                        "🚫 100% Ad-Free Experience Always"
                    ).forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = premiumColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(feature, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPremiumDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = premiumColor)
                ) {
                    Text("Awesome", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 6. Recently Deleted Dialog
    if (showRecentlyDeletedDialog) {
        AlertDialog(
            onDismissRequest = { showRecentlyDeletedDialog = false },
            containerColor = Color(0xFF1E1E24),
            titleContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Recently Deleted (0 files)", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Songs deleted within the app are retained in a temporary local trash cache for 30 days before permanent deletion.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF282834)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("Trash Bin is empty", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRecentlyDeletedDialog = false }) {
                    Text("Close", color = Color.White.copy(alpha = 0.8f))
                }
            }
        )
    }

    // 7. Playback Settings Dialog
    if (showPlaybackSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPlaybackSettingsDialog = false },
            containerColor = Color(0xFF1E1E24),
            titleContentColor = Color.White,
            title = { Text("Playback Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gapless Playback", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                            Text("Seamless transition between tracks", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = gaplessPlayback,
                            onCheckedChange = { onSetGaplessPlayback(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Crossfade Duration", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                            Text("${crossfadeSeconds}s", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0, 2, 4, 8).forEach { sec ->
                                OutlinedButton(
                                    onClick = { onSetCrossfadeSeconds(sec) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (crossfadeSeconds == sec) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (sec == 0) "Off" else "${sec}s", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pause on Disconnect", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                            Text("Auto pause when headphones/Bluetooth disconnect", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = pauseOnDisconnect,
                            onCheckedChange = { onSetPauseOnDisconnect(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaybackSettingsDialog = false }) {
                    Text("Done", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // 8. Notification Settings Dialog
    if (showNotificationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationSettingsDialog = false },
            containerColor = Color(0xFF1E1E24),
            titleContentColor = Color.White,
            title = { Text("Notification Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show Lockscreen Album Art", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                            Text("Display artwork background on lock screen", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = lockscreenArt,
                            onCheckedChange = { onSetLockscreenArt(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Media Player Quick Controls", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                            Text("Show media notification controls in Android shade", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            enabled = false,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationSettingsDialog = false }) {
                    Text("Done", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // 9. Language Dialog
    if (showLanguageDialog) {
        val languages = listOf("English", "Español", "Deutsch", "Français", "日本語", "Português", "हिन्दी")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = Color(0xFF1E1E24),
            titleContentColor = Color.White,
            title = { Text("Select App Language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lang, color = Color.White)
                            if (language == lang) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    // 10. Feedback Dialog
    if (showFeedbackDialog) {
        var rating by remember { mutableIntStateOf(5) }
        var feedbackText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            containerColor = Color(0xFF1E1E24),
            titleContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Feedback, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Play Store Feedback", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("How is your experience with Aura Music?", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = "$star Stars",
                                    tint = if (star <= rating) Color(0xFFFFC107) else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        placeholder = { Text("Write your thoughts or feature requests...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                        showFeedbackDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    // 11. About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color(0xFF1E1E24),
            titleContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Aura Music Player", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version 2026.15.0 (Build 105)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Text(
                        "Designed for high-fidelity offline audio playback, dynamic themes, and compressed Google Drive cloud sync.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Developed for Google Play Store release.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // 12. Backup & Restore Dialog
    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            userEmail = userEmail,
            isBackingUp = isBackingUp,
            isRestoring = isRestoring,
            backupProgress = backupProgress,
            statusMessage = statusMessage,
            lastBackupInfo = lastBackupInfo,
            onLoginClick = onLoginGoogleAccount,
            onLogoutClick = onLogoutGoogleAccount,
            onBackupClick = onPerformBackup,
            onRestoreClick = onPerformRestore,
            onDismiss = { showBackupRestoreDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.1.sp
        ),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsGroupCard(
    cardBg: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingRowItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    titleColor: Color = Color.White,
    valueColor: Color = Color.White.copy(alpha = 0.5f),
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconBgColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            ),
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = valueColor,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun getThemeDisplayLabel(mode: String): String {
    return when (mode) {
        "SYSTEM" -> "Classic / Medium"
        "DARK" -> "Dark Theme"
        "LIGHT" -> "Light Theme"
        "AMOLED" -> "AMOLED Black"
        "DYNAMIC" -> "Dynamic You"
        else -> mode
    }
}
