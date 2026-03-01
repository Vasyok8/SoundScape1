package com.soundscape.core.audio

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * FileAudioPlayer — воспроизводит аудиофайлы через ExoPlayer.
 *
 * Поддерживает:
 * - OGG Vorbis из assets/ (prefix "assets://")
 * - Файлы из Internal Storage
 * - Бесконечный бесшовный loop (REPEAT_MODE_ONE)
 * - Плавное управление громкостью
 *
 * Все операции с ExoPlayer выполняются на Main-потоке (требование ExoPlayer).
 */
class FileAudioPlayer(
    override val channelIndex: Int,
    private val assetFileName: String,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) : AudioSource {

    companion object {
        private const val TAG = "FileAudioPlayer"
    }

    private val _volume = MutableStateFlow(0.5f)
    override val volume: StateFlow<Float> = _volume

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    // ExoPlayer инициализируется лениво при первом play()
    private var player: ExoPlayer? = null
    private var context: Context? = null

    /**
     * Устанавливает контекст — вызывается из PlaybackService перед использованием.
     */
    fun init(ctx: Context) {
        context = ctx
    }

    override fun play() {
        val ctx = context ?: run {
            Log.e(TAG, "[$channelIndex] Context not set! Call init() first.")
            return
        }

        if (player == null) {
            player = ExoPlayer.Builder(ctx).build().apply {
                val uri = android.net.Uri.parse("asset:///$assetFileName")
                val item = MediaItem.fromUri(uri)
                setMediaItem(item)
                repeatMode = Player.REPEAT_MODE_ONE  // Бесконечный loop
                volume = _volume.value
                prepare()
            }
        }

        player?.volume = _volume.value
        player?.play()
        _isPlaying.value = true
        Log.d(TAG, "[$channelIndex] Playing: $assetFileName")
    }

    override fun pause() {
        player?.pause()
        _isPlaying.value = false
        Log.d(TAG, "[$channelIndex] Paused: $assetFileName")
    }

    override fun release() {
        player?.stop()
        player?.release()
        player = null
        _isPlaying.value = false
        Log.d(TAG, "[$channelIndex] Released: $assetFileName")
    }

    override fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _volume.value = clamped
        player?.volume = clamped
    }
}
