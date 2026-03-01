package com.soundscape.core.audio

import kotlinx.coroutines.flow.StateFlow

/**
 * Контракт для любого аудио-источника.
 * Все генераторы и плееры реализуют этот интерфейс —
 * это позволяет легко добавлять новые типы звуков в будущем.
 */
interface AudioSource {
    /** Уникальный ID канала (0-9) */
    val channelIndex: Int
    /** Текущий уровень громкости (0f..1f) */
    val volume: StateFlow<Float>
    /** true = источник активен и воспроизводит звук */
    val isPlaying: StateFlow<Boolean>

    /** Начать воспроизведение */
    fun play()
    /** Приостановить */
    fun pause()
    /** Остановить и освободить ресурсы */
    fun release()
    /** Установить громкость (0f..1f) */
    fun setVolume(volume: Float)
}

/**
 * Состояние всего микшера
 */
data class MixerState(
    val isPlaying: Boolean = false,
    val channelLevels: List<Float> = List(10) { 0.5f },
    val isAnimating: Boolean = false
)
