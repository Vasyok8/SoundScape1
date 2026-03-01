package com.soundscape.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soundscape.core.audio.AudioCommand
import com.soundscape.core.audio.MixerEngine
import com.soundscape.core.audio.PlayerState
import com.soundscape.core.domain.model.MixConfig
import com.soundscape.core.domain.model.PresetRepository
import com.soundscape.core.domain.model.Preset
import com.soundscape.core.domain.model.SoundscapeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val mixerEngine: MixerEngine,
    private val soundscapeRepository: SoundscapeRepository,
    private val presetRepository: PresetRepository
) : ViewModel() {

    // UI наблюдает этот StateFlow напрямую
    val state: StateFlow<PlayerState> = mixerEngine.state

    fun loadSoundscape(soundscapeId: String) {
        viewModelScope.launch {
            val soundscape = soundscapeRepository.getById(soundscapeId) ?: return@launch

            // Ищем дефолтный пресет
            val defaultPreset = presetRepository
                .getPresetsForSoundscape(soundscapeId)
                .first()
                .find { it.isDefault }

            mixerEngine.loadSoundscape(soundscape, defaultPreset?.mixConfig)
        }
    }

    fun onCommand(command: AudioCommand) {
        when (command) {
            is AudioCommand.Play -> mixerEngine.play()
            is AudioCommand.Pause -> mixerEngine.pause()
            is AudioCommand.Stop -> mixerEngine.stop()
            is AudioCommand.SetChannelLevel -> mixerEngine.setChannelLevel(command.index, command.level)
            is AudioCommand.SetMasterVolume -> mixerEngine.setMasterVolume(command.volume)
            is AudioCommand.SetAnimating -> mixerEngine.setAnimating(command.enabled)
            is AudioCommand.LoadPreset -> mixerEngine.loadPreset(command.levels)
            is AudioCommand.SetSleepTimer -> mixerEngine.startSleepTimer(command.minutes)
            is AudioCommand.CancelSleepTimer -> mixerEngine.cancelSleepTimer()
        }
    }

    fun saveCurrentAsPreset(name: String, soundscapeId: String) {
        viewModelScope.launch {
            val preset = Preset(
                id = UUID.randomUUID().toString(),
                soundscapeId = soundscapeId,
                name = name,
                mixConfig = MixConfig(mixerEngine.state.value.channelLevels)
            )
            presetRepository.savePreset(preset)
        }
    }

    override fun onCleared() {
        // ViewModel уничтожается при уходе с экрана — не останавливаем звук,
        // Service продолжает работать
    }
}
