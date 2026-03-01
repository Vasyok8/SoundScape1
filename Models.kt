package com.soundscape.core.domain.model

/**
 * Саундскейп — основная единица контента в приложении.
 */
data class Soundscape(
    val id: String,
    val title: String,
    val category: SoundscapeCategory,
    val description: String,
    val imageRes: Int,           // drawable resource id
    val audioType: AudioType,
    val isDownloaded: Boolean = false,
    val isPurchased: Boolean = false,
    val isFeatured: Boolean = false,
    val tags: List<String> = emptyList()
)

/**
 * Категории саундскейпов (как в myNoise)
 */
enum class SoundscapeCategory(val displayName: String) {
    NATURAL_NOISES("Natural Noises"),
    SPACES("Spaces"),
    EXPERIMENTAL("Experimental"),
    URBAN("Urban"),
    WATER("Water"),
    MEDITATION("Meditation")
}

/**
 * Тип аудио источника
 */
sealed class AudioType {
    /** Генерируется через AudioTrack API (не требует файла) */
    data class Generated(val generatorType: GeneratorType) : AudioType()
    /** Воспроизводится из файла (assets или internal storage) */
    data class File(val fileName: String) : AudioType()
    /** Комбинация: файл + наложенная генерация */
    data class Mixed(val fileName: String, val generatorType: GeneratorType) : AudioType()
}

/**
 * Типы звуков, генерируемых через AudioTrack
 */
enum class GeneratorType {
    WHITE_NOISE,
    PINK_NOISE,
    BROWN_NOISE,
    BINAURAL_ALPHA,    // 8-12 Hz — расслабление
    BINAURAL_THETA,    // 4-8 Hz — медитация
    BINAURAL_DELTA,    // 0.5-4 Hz — глубокий сон
    BINAURAL_BETA,     // 12-30 Hz — концентрация
    ISOCHRONIC_ALPHA,
    SOLFEGGIO_528      // 528 Hz — "частота трансформации"
}

/**
 * Конфигурация миксера — положения 10 слайдеров (0f..1f)
 */
data class MixConfig(
    val channelLevels: List<Float> = List(10) { 0.5f }
) {
    init {
        require(channelLevels.size == 10) { "MixConfig must have exactly 10 channels" }
        require(channelLevels.all { it in 0f..1f }) { "Channel levels must be in [0, 1]" }
    }
}

/**
 * Пресет — сохранённая конфигурация слайдеров
 */
data class Preset(
    val id: String,
    val soundscapeId: String,
    val name: String,
    val mixConfig: MixConfig,
    val isDefault: Boolean = false
)

/**
 * Конфигурация таймера сна
 */
data class SleepTimer(
    val durationMinutes: Int,
    val fadeOutSeconds: Int = 30,
    val isActive: Boolean = false
)

/**
 * Конфигурация будильника
 */
data class Alarm(
    val id: String,
    val hour: Int,
    val minute: Int,
    val soundscapeId: String,
    val presetId: String?,
    val fadeInSeconds: Int = 60,
    val isEnabled: Boolean = true,
    val repeatDays: Set<Int> = emptySet() // 1=Пн, 7=Вс (Calendar.DAY_OF_WEEK)
)
