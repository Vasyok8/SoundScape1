package com.soundscape.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Управление Audio Focus — корректное поведение с другими приложениями.
 *
 * Сценарии:
 * - Входящий звонок → ставим на паузу
 * - Навигация (Google Maps) → снижаем громкость (duck)
 * - Музыкальное приложение запускается → останавливаемся
 * - Звонок завершился → возобновляем воспроизведение
 */
@Singleton
class AudioFocusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AudioFocusManager"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null

    // Коллбек для уведомления MixerEngine об изменении фокуса
    var onFocusLoss: (() -> Unit)? = null
    var onFocusGain: (() -> Unit)? = null
    var onFocusLossDucking: (() -> Unit)? = null  // Временное снижение громкости

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d(TAG, "Focus GAIN — resuming")
                onFocusGain?.invoke()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d(TAG, "Focus LOSS — stopping")
                onFocusLoss?.invoke()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d(TAG, "Focus LOSS_TRANSIENT — pausing")
                onFocusLoss?.invoke()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(TAG, "Focus LOSS_TRANSIENT_CAN_DUCK — ducking")
                onFocusLossDucking?.invoke()
            }
        }
    }

    /**
     * Запросить Audio Focus перед воспроизведением.
     * @return true если фокус получен
     */
    fun requestFocus(): Boolean {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()

        val result = audioManager.requestAudioFocus(focusRequest!!)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            || result == AudioManager.AUDIOFOCUS_REQUEST_DELAYED
    }

    /** Освободить Audio Focus после остановки */
    fun abandonFocus() {
        focusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
            focusRequest = null
        }
    }
}
