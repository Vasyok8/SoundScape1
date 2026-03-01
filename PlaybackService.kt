package com.soundscape.core.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.soundscape.MainActivity
import com.soundscape.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ForegroundService для воспроизведения звука в фоне.
 *
 * Отображает постоянное уведомление с:
 * - Названием текущего саундскейпа
 * - Кнопками: Play/Pause, Stop
 *
 * Слушает изменения PlayerState из MixerEngine и обновляет уведомление.
 */
@AndroidEntryPoint
class PlaybackService : LifecycleService() {

    companion object {
        private const val TAG = "PlaybackService"
        const val CHANNEL_ID = "soundscape_playback"
        const val NOTIFICATION_ID = 1001

        // Actions для управления через уведомление
        const val ACTION_PLAY_PAUSE = "com.soundscape.PLAY_PAUSE"
        const val ACTION_STOP = "com.soundscape.STOP"

        fun startService(context: Context) {
            val intent = Intent(context, PlaybackService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, PlaybackService::class.java))
        }
    }

    @Inject lateinit var mixerEngine: MixerEngine

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    // BroadcastReceiver для кнопок уведомления
    private val mediaReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY_PAUSE -> {
                    val state = mixerEngine.state.value
                    if (state.isPlaying) mixerEngine.pause() else mixerEngine.play()
                }
                ACTION_STOP -> {
                    mixerEngine.stop()
                    stopSelf()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerReceiver(mediaReceiver, IntentFilter().apply {
            addAction(ACTION_PLAY_PAUSE)
            addAction(ACTION_STOP)
        }, RECEIVER_NOT_EXPORTED)

        // Подписка на изменения состояния — обновляем уведомление
        lifecycleScope.launch {
            mixerEngine.state.collectLatest { state ->
                val notification = buildNotification(state)
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val notification = buildNotification(mixerEngine.state.value)
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "PlaybackService started")
        return START_STICKY
    }

    override fun onDestroy() {
        mixerEngine.stop()
        unregisterReceiver(mediaReceiver)
        super.onDestroy()
        Log.d(TAG, "PlaybackService destroyed")
    }

    // ─── Notification ────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "SoundScape Playback",
            NotificationManager.IMPORTANCE_LOW  // LOW = без звука уведомления
        ).apply {
            description = "Controls for SoundScape audio playback"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(state: PlayerState): Notification {
        // Intent для открытия приложения по нажатию на уведомление
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Кнопка Play/Pause
        val playPauseIntent = PendingIntent.getBroadcast(
            this, 1,
            Intent(ACTION_PLAY_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseIcon = if (state.isPlaying)
            android.R.drawable.ic_media_pause
        else
            android.R.drawable.ic_media_play
        val playPauseLabel = if (state.isPlaying) "Pause" else "Play"

        // Кнопка Stop
        val stopIntent = PendingIntent.getBroadcast(
            this, 2,
            Intent(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timerText = state.sleepTimerMinutesLeft?.let { " · ${it}m left" } ?: ""

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(state.soundscapeTitle.ifEmpty { "SoundScape" })
            .setContentText("${if (state.isPlaying) "Playing" else "Paused"}$timerText")
            .setContentIntent(openIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setOngoing(state.isPlaying)
            .addAction(playPauseIcon, playPauseLabel, playPauseIntent)
            .addAction(android.R.drawable.ic_delete, "Stop", stopIntent)
            .build()
    }
}

/**
 * Восстановление будильников после перезагрузки.
 * Полная реализация — Этап 6.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed — alarms will be restored in Stage 6")
        }
    }
}
