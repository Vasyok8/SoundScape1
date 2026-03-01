package com.soundscape.core.audio

import android.content.Context
import android.util.Log
import com.soundscape.core.domain.model.AudioType
import com.soundscape.core.domain.model.GeneratorType
import com.soundscape.core.domain.model.MixConfig
import com.soundscape.core.domain.model.Soundscape
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.random.Random

/**
 * MixerEngine — центральное ядро аудио системы.
 *
 * Управляет:
 * - 10 каналами звука (каждый может быть NoiseGenerator или FileAudioPlayer)
 * - Громкостью каждого канала через слайдеры
 * - Общей громкостью
 * - Режимом Animate (плавное случайное движение слайдеров)
 * - Sleep Timer (постепенное затухание и остановка)
 *
 * Является Singleton — один экземпляр на всё приложение.
 */
@Singleton
class MixerEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioFocusManager: AudioFocusManager
) {
    companion object {
        private const val TAG = "MixerEngine"
        const val CHANNEL_COUNT = 10
        private const val ANIMATE_INTERVAL_MS = 3000L
        private const val ANIMATE_STEP_DURATION_MS = 2500L
        private const val FADE_STEP_MS = 100L
    }

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Текущие генераторы для каждого канала
    private val generators = arrayOfNulls<NoiseGenerator>(CHANNEL_COUNT)

    // Текущие уровни слайдеров (0f..1f)
    private val channelLevels = FloatArray(CHANNEL_COUNT) { 0.5f }

    private var masterVolume = 1f
    private var isPlaying = false

    // Animate job
    private var animateJob: Job? = null

    // Sleep timer job
    private var sleepTimerJob: Job? = null
    private var sleepTimerTargetMs = 0L

    // Текущий саундскейп
    private var currentSoundscape: Soundscape? = null

    // ─── Конфигурация каналов для генерируемых звуков ──────────────
    // 10 каналов = 10 генераторов разных типов, образующих богатый микс
    private val defaultChannelConfig = listOf(
        GeneratorType.BROWN_NOISE,      // Канал 1 — глубокий бас
        GeneratorType.PINK_NOISE,       // Канал 2 — мягкий шум
        GeneratorType.PINK_NOISE,       // Канал 3 — ещё один слой розового
        GeneratorType.WHITE_NOISE,      // Канал 4 — высокие частоты
        GeneratorType.WHITE_NOISE,      // Канал 5 — воздух
        GeneratorType.BINAURAL_ALPHA,   // Канал 6 — расслабление
        GeneratorType.BINAURAL_THETA,   // Канал 7 — медитация
        GeneratorType.ISOCHRONIC_ALPHA, // Канал 8 — тоны
        GeneratorType.SOLFEGGIO_528,    // Канал 9 — solfeggio
        GeneratorType.BINAURAL_DELTA    // Канал 10 — глубокий сон
    )

    // ─── Публичный API ──────────────────────────────────────────────

    fun loadSoundscape(soundscape: Soundscape, initialConfig: MixConfig? = null) {
        Log.d(TAG, "Loading soundscape: ${soundscape.title}")
        stopInternal()
        currentSoundscape = soundscape

        val levels = initialConfig?.channelLevels ?: getDefaultLevels(soundscape)
        levels.forEachIndexed { i, level -> channelLevels[i] = level }

        setupGenerators(soundscape)

        _state.update { it.copy(
            soundscapeId = soundscape.id,
            soundscapeTitle = soundscape.title,
            channelLevels = channelLevels.toList()
        )}
    }

    fun play() {
        if (isPlaying) return
        if (!audioFocusManager.requestFocus()) {
            Log.w(TAG, "Audio focus denied")
            return
        }

        generators.forEachIndexed { i, gen ->
            gen?.volume = channelLevels[i] * masterVolume
            gen?.start()
        }
        isPlaying = true
        _state.update { it.copy(isPlaying = true) }
        Log.d(TAG, "MixerEngine playing")
    }

    fun pause() {
        generators.forEach { it?.stop() }
        isPlaying = false
        _state.update { it.copy(isPlaying = false) }
        audioFocusManager.abandonFocus()
    }

    fun stop() {
        stopInternal()
        audioFocusManager.abandonFocus()
    }

    fun setChannelLevel(index: Int, level: Float) {
        if (index !in 0 until CHANNEL_COUNT) return
        val clamped = level.coerceIn(0f, 1f)
        channelLevels[index] = clamped
        generators[index]?.volume = clamped * masterVolume

        _state.update { it.copy(
            channelLevels = channelLevels.toList()
        )}
    }

    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        generators.forEachIndexed { i, gen ->
            gen?.volume = channelLevels[i] * masterVolume
        }
        _state.update { it.copy(masterVolume = masterVolume) }
    }

    fun loadPreset(levels: List<Float>, presetName: String = "Custom") {
        require(levels.size == CHANNEL_COUNT)
        levels.forEachIndexed { i, level ->
            channelLevels[i] = level.coerceIn(0f, 1f)
            generators[i]?.volume = channelLevels[i] * masterVolume
        }
        _state.update { it.copy(
            channelLevels = channelLevels.toList(),
            currentPresetName = presetName
        )}
    }

    // ─── Animate ────────────────────────────────────────────────────

    fun setAnimating(enabled: Boolean) {
        if (enabled) startAnimate() else stopAnimate()
        _state.update { it.copy(isAnimating = enabled) }
    }

    private fun startAnimate() {
        animateJob?.cancel()
        animateJob = scope.launch {
            // Целевые значения для плавной интерполяции
            val targets = channelLevels.copyOf()

            while (isActive) {
                // Каждые N секунд генерируем новые цели
                val steps = (ANIMATE_STEP_DURATION_MS / FADE_STEP_MS).toInt()

                for (step in 0 until steps) {
                    if (!isActive) break
                    val progress = step.toFloat() / steps

                    for (i in 0 until CHANNEL_COUNT) {
                        val current = channelLevels[i]
                        val target = targets[i]
                        val interpolated = current + (target - current) * (FADE_STEP_MS.toFloat() / ANIMATE_STEP_DURATION_MS)
                        channelLevels[i] = interpolated.coerceIn(0.1f, 0.95f)
                        generators[i]?.volume = channelLevels[i] * masterVolume
                    }

                    _state.update { it.copy(channelLevels = channelLevels.toList()) }
                    delay(FADE_STEP_MS)
                }

                // Генерируем новые цели
                for (i in 0 until CHANNEL_COUNT) {
                    targets[i] = Random.nextFloat() * 0.8f + 0.1f  // 0.1..0.9
                }

                delay(ANIMATE_INTERVAL_MS)
            }
        }
    }

    private fun stopAnimate() {
        animateJob?.cancel()
        animateJob = null
    }

    // ─── Sleep Timer ────────────────────────────────────────────────

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalMs = minutes * 60_000L
        val fadeStartMs = totalMs - 30_000L   // Начать затухание за 30 сек

        sleepTimerJob = scope.launch {
            sleepTimerTargetMs = System.currentTimeMillis() + totalMs

            // Ждём до начала затухания
            if (fadeStartMs > 0) delay(fadeStartMs)

            // Плавное затухание за 30 секунд
            val fadeSteps = 300  // 30 сек / 100 мс
            val initialVolume = masterVolume

            for (step in fadeSteps downTo 0) {
                if (!isActive) break
                val fadedVolume = initialVolume * (step.toFloat() / fadeSteps)
                setMasterVolume(fadedVolume)
                updateTimerDisplay()
                delay(FADE_STEP_MS)
            }

            // Стоп
            stop()
            _state.update { it.copy(sleepTimerMinutesLeft = null) }
        }

        scope.launch {
            while (sleepTimerJob?.isActive == true) {
                updateTimerDisplay()
                delay(60_000L) // Обновляем каждую минуту
            }
        }

        _state.update { it.copy(sleepTimerMinutesLeft = minutes) }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _state.update { it.copy(sleepTimerMinutesLeft = null) }
    }

    private fun updateTimerDisplay() {
        val msLeft = sleepTimerTargetMs - System.currentTimeMillis()
        val minutesLeft = (msLeft / 60_000L).toInt().coerceAtLeast(0)
        _state.update { it.copy(sleepTimerMinutesLeft = minutesLeft) }
    }

    // ─── Внутренние методы ──────────────────────────────────────────

    private fun setupGenerators(soundscape: Soundscape) {
        releaseGenerators()

        val channelTypes = when (val type = soundscape.audioType) {
            is AudioType.Generated -> getChannelTypesForGenerator(type.generatorType)
            is AudioType.File -> defaultChannelConfig  // Файловые — тоже 10 каналов шума поверх
            is AudioType.Mixed -> getChannelTypesForGenerator(type.generatorType)
        }

        channelTypes.forEachIndexed { i, genType ->
            generators[i] = NoiseGenerator(genType)
            generators[i]?.volume = channelLevels[i] * masterVolume
        }
    }

    /**
     * Возвращает набор из 10 генераторов для конкретного типа.
     * Основной генератор на первых каналах + сопутствующие на остальных.
     */
    private fun getChannelTypesForGenerator(primary: GeneratorType): List<GeneratorType> {
        return when (primary) {
            GeneratorType.WHITE_NOISE -> List(10) { if (it < 6) GeneratorType.WHITE_NOISE else defaultChannelConfig[it] }
            GeneratorType.PINK_NOISE -> List(10) { if (it < 6) GeneratorType.PINK_NOISE else defaultChannelConfig[it] }
            GeneratorType.BROWN_NOISE -> List(10) { if (it < 4) GeneratorType.BROWN_NOISE else defaultChannelConfig[it] }
            GeneratorType.BINAURAL_ALPHA -> listOf(
                GeneratorType.PINK_NOISE, GeneratorType.PINK_NOISE,
                GeneratorType.BROWN_NOISE, GeneratorType.BROWN_NOISE,
                GeneratorType.WHITE_NOISE, GeneratorType.WHITE_NOISE,
                GeneratorType.BINAURAL_ALPHA, GeneratorType.BINAURAL_ALPHA,
                GeneratorType.BINAURAL_THETA, GeneratorType.ISOCHRONIC_ALPHA
            )
            else -> defaultChannelConfig
        }
    }

    private fun getDefaultLevels(soundscape: Soundscape): List<Float> {
        // Разные дефолтные уровни для разных типов звука
        return when (soundscape.audioType) {
            is AudioType.Generated -> listOf(0.8f, 0.7f, 0.6f, 0.4f, 0.3f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            else -> listOf(0.7f, 0.6f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.0f, 0.0f, 0.0f)
        }
    }

    private fun stopInternal() {
        stopAnimate()
        cancelSleepTimer()
        generators.forEach { it?.stop() }
        isPlaying = false
        _state.update { it.copy(isPlaying = false) }
    }

    private fun releaseGenerators() {
        generators.forEach { it?.stop() }
        generators.fill(null)
    }

    fun release() {
        stopInternal()
        releaseGenerators()
        audioFocusManager.abandonFocus()
    }
}
