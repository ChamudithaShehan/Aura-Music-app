package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.AuraApplication
import com.example.MainActivity

class PlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "PlaybackServiceTrace"
        const val CHANNEL_ID = "aura_playback_channel"
        const val NOTIFICATION_ID = 1001
    }

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Step 2: PlaybackService.onCreate() executed")
        val app = applicationContext as AuraApplication
        val playerManager = app.appContainer.playerManager

        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, playerManager.exoPlayer)
            .setSessionActivity(pendingIntent)
            .setCallback(object : MediaSession.Callback {})
            .build()
        Log.d(TAG, "Step 5: MediaSession created and bound to ExoPlayer")

        try {
            setMediaNotificationProvider(
                DefaultMediaNotificationProvider.Builder(this)
                    .setChannelId(CHANNEL_ID)
                    .setNotificationId(NOTIFICATION_ID)
                    .build()
            )
            Log.d(TAG, "Step 4: setMediaNotificationProvider() executed successfully with Channel ID=$CHANNEL_ID")
        } catch (e: Exception) {
            Log.e(TAG, "Step 4 Failure: Error setting MediaNotificationProvider", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aura Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls and song info"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Step 3: NotificationChannel '$CHANNEL_ID' created with VISIBILITY_PUBLIC")
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d(TAG, "Step 5b: onGetSession() requested by ${controllerInfo.packageName}")
        return mediaSession
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        val player = session.player
        Log.d(TAG, "Step 6: onUpdateNotification() -> playWhenReady=${player.playWhenReady}, state=${player.playbackState}, startInForegroundRequired=$startInForegroundRequired")
        super.onUpdateNotification(session, startInForegroundRequired)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        Log.d(TAG, "PlaybackService.onTaskRemoved() -> playWhenReady=${player?.playWhenReady}")
        if (player != null && (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED)) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "PlaybackService.onDestroy() executed")
        mediaSession?.run {
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
