package com.soundscape.core.audio

/**
 * Состояние плеера — транслируется в UI через StateFlow.
 * Используется PlayerViewModel и PlayerScreen.
 */
data class PlayerUiState(
    val isPlaying: Boolean = false,
    val soundscapeId: String = "",
    val soundscapeTitle: String = "",
    val channelLevels: List<Float> = List(10) { 0.5f },
    val isAnimating: Boolean = false,
    val currentPresetName: String = "Default",
    val sleepTimerMinutesLeft: Int? = null
)
