package com.soundscape.core.data

import com.soundscape.core.domain.model.AudioType
import com.soundscape.core.domain.model.GeneratorType
import com.soundscape.core.domain.model.Soundscape
import com.soundscape.core.domain.model.SoundscapeCategory

/**
 * Встроенный каталог саундскейпов.
 *
 * Все генерируемые звуки работают без интернета и без файлов.
 * Файловые звуки (из Freesound) будут добавлены в Этапе 4.
 */
object SoundscapeDataSource {

    val builtInSoundscapes: List<Soundscape> = listOf(

        // ─── Experimental ────────────────────────────────────────────
        Soundscape(
            id = "dissociative_trance",
            title = "Dissociative Trance",
            category = SoundscapeCategory.EXPERIMENTAL,
            description = "A deep, immersive soundscape combining pink noise with theta binaural beats. Perfect for deep meditation and altered states of consciousness.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.BINAURAL_THETA),
            isFeatured = true,
            tags = listOf("binaural", "theta", "meditation", "trance")
        ),
        Soundscape(
            id = "pure_white",
            title = "Pure White Noise",
            category = SoundscapeCategory.EXPERIMENTAL,
            description = "Classic white noise for masking distractions and improving focus. Every frequency equally represented.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.WHITE_NOISE),
            tags = listOf("white noise", "focus", "masking")
        ),
        Soundscape(
            id = "pink_serenity",
            title = "Pink Serenity",
            category = SoundscapeCategory.EXPERIMENTAL,
            description = "Pink noise follows natural patterns found in music and nature. Smoother and more natural than white noise.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.PINK_NOISE),
            tags = listOf("pink noise", "natural", "focus", "sleep")
        ),
        Soundscape(
            id = "brown_earth",
            title = "Brown Earth",
            category = SoundscapeCategory.EXPERIMENTAL,
            description = "Deep, rumbling brown noise. Like a powerful waterfall or distant thunder. Perfect for deep relaxation.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.BROWN_NOISE),
            tags = listOf("brown noise", "deep", "relaxation", "bass")
        ),

        // ─── Meditation ───────────────────────────────────────────────
        Soundscape(
            id = "alpha_focus",
            title = "Alpha Focus",
            category = SoundscapeCategory.MEDITATION,
            description = "Alpha binaural beats at 10 Hz layered with pink noise. Promotes relaxed alertness and enhanced creativity.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.BINAURAL_ALPHA),
            tags = listOf("binaural", "alpha", "focus", "creativity")
        ),
        Soundscape(
            id = "theta_dream",
            title = "Theta Dream",
            category = SoundscapeCategory.MEDITATION,
            description = "Theta waves at 6 Hz for deep meditation, REM sleep, and subconscious processing. Use with headphones.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.BINAURAL_THETA),
            tags = listOf("binaural", "theta", "meditation", "sleep")
        ),
        Soundscape(
            id = "delta_sleep",
            title = "Delta Sleep",
            category = SoundscapeCategory.MEDITATION,
            description = "Delta binaural beats at 2 Hz. The frequency of deep dreamless sleep and regeneration.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.BINAURAL_DELTA),
            tags = listOf("binaural", "delta", "sleep", "regeneration")
        ),
        Soundscape(
            id = "beta_sharp",
            title = "Beta Sharp Mind",
            category = SoundscapeCategory.MEDITATION,
            description = "Beta waves at 20 Hz for intense focus, concentration and active thinking.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.BINAURAL_BETA),
            tags = listOf("binaural", "beta", "focus", "concentration")
        ),
        Soundscape(
            id = "solfeggio_528",
            title = "Solfeggio 528 Hz",
            category = SoundscapeCategory.MEDITATION,
            description = "The legendary 528 Hz frequency, known as the 'Love Frequency' or 'Miracle Tone'. Pure sine wave meditation.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.SOLFEGGIO_528),
            tags = listOf("solfeggio", "528hz", "healing", "meditation")
        ),

        // ─── Natural Noises (заглушки — файлы добавим в Этапе 4) ─────
        Soundscape(
            id = "distant_thunder",
            title = "Distant Thunder",
            category = SoundscapeCategory.NATURAL_NOISES,
            description = "Thunder rumbling over distant mountains, with gentle rain. Generates brown noise as a base with thunder layer.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.BROWN_NOISE), // TODO: заменить на AudioType.File в Этапе 4
            tags = listOf("thunder", "rain", "storm", "nature")
        ),
        Soundscape(
            id = "ocean_waves",
            title = "Ocean Waves",
            category = SoundscapeCategory.WATER,
            description = "Rhythmic ocean waves on a sandy beach. Pink noise base with wave modulation.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.PINK_NOISE),
            tags = listOf("ocean", "waves", "beach", "water")
        ),

        // ─── Spaces ───────────────────────────────────────────────────
        Soundscape(
            id = "cafe_restaurant",
            title = "Cafe Restaurant",
            category = SoundscapeCategory.SPACES,
            description = "Ambient coffee shop noise. White noise base simulating crowd murmur and espresso machines.",
            imageRes = 0,
            audioType = AudioType.Generated(GeneratorType.WHITE_NOISE),
            tags = listOf("cafe", "coffee", "crowd", "work")
        )
    )
}
