package com.soundscape.core.domain.model

import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий саундскейпов.
 * Реализация будет в слое data через Room + встроенный каталог.
 */
interface SoundscapeRepository {
    fun getAll(): Flow<List<Soundscape>>
    fun getByCategory(category: SoundscapeCategory): Flow<List<Soundscape>>
    fun getFeatured(): Flow<List<Soundscape>>
    suspend fun getById(id: String): Soundscape?
    fun search(query: String): Flow<List<Soundscape>>
}

/**
 * Репозиторий пресетов.
 */
interface PresetRepository {
    fun getPresetsForSoundscape(soundscapeId: String): Flow<List<Preset>>
    suspend fun savePreset(preset: Preset)
    suspend fun deletePreset(presetId: String)
}

/**
 * Репозиторий будильников.
 */
interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun saveAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarmId: String)
    suspend fun setAlarmEnabled(alarmId: String, enabled: Boolean)
}
