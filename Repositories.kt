package com.soundscape.core.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.soundscape.core.data.db.AlarmDao
import com.soundscape.core.data.db.AlarmEntity
import com.soundscape.core.data.db.PresetDao
import com.soundscape.core.data.db.PresetEntity
import com.soundscape.core.data.db.SoundscapeDao
import com.soundscape.core.data.db.SoundscapeEntity
import com.soundscape.core.domain.model.Alarm
import com.soundscape.core.domain.model.AlarmRepository
import com.soundscape.core.domain.model.AudioType
import com.soundscape.core.domain.model.GeneratorType
import com.soundscape.core.domain.model.MixConfig
import com.soundscape.core.domain.model.Preset
import com.soundscape.core.domain.model.PresetRepository
import com.soundscape.core.domain.model.Soundscape
import com.soundscape.core.domain.model.SoundscapeCategory
import com.soundscape.core.domain.model.SoundscapeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val gson = Gson()

// ============================================================
// SOUNDSCAPE REPOSITORY
// ============================================================

class SoundscapeRepositoryImpl @Inject constructor(
    private val dao: SoundscapeDao
) : SoundscapeRepository {

    override fun getAll(): Flow<List<Soundscape>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getByCategory(category: SoundscapeCategory): Flow<List<Soundscape>> =
        dao.getByCategory(category.name).map { list -> list.map { it.toDomain() } }

    override fun getFeatured(): Flow<List<Soundscape>> =
        dao.getFeatured().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Soundscape? =
        dao.getById(id)?.toDomain()

    override fun search(query: String): Flow<List<Soundscape>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }
}

private fun SoundscapeEntity.toDomain(): Soundscape = Soundscape(
    id = id,
    title = title,
    category = SoundscapeCategory.valueOf(category),
    description = description,
    imageRes = imageRes,
    audioType = parseAudioType(audioTypeJson),
    isDownloaded = isDownloaded,
    isPurchased = isPurchased,
    isFeatured = isFeatured,
    tags = gson.fromJson(tagsJson, Array<String>::class.java).toList()
)

private fun parseAudioType(json: String): AudioType {
    val map = gson.fromJson<Map<String, Any>>(json, object : TypeToken<Map<String, Any>>() {}.type)
    return when (map["type"]) {
        "generated" -> AudioType.Generated(GeneratorType.valueOf(map["generator"] as String))
        "file" -> AudioType.File(map["fileName"] as String)
        "mixed" -> AudioType.Mixed(map["fileName"] as String, GeneratorType.valueOf(map["generator"] as String))
        else -> AudioType.Generated(GeneratorType.WHITE_NOISE)
    }
}

// ============================================================
// PRESET REPOSITORY
// ============================================================

class PresetRepositoryImpl @Inject constructor(
    private val dao: PresetDao
) : PresetRepository {

    override fun getPresetsForSoundscape(soundscapeId: String): Flow<List<Preset>> =
        dao.getPresetsForSoundscape(soundscapeId).map { list -> list.map { it.toDomain() } }

    override suspend fun savePreset(preset: Preset) {
        dao.insert(preset.toEntity())
    }

    override suspend fun deletePreset(presetId: String) {
        dao.deleteById(presetId)
    }
}

private fun PresetEntity.toDomain(): Preset = Preset(
    id = id,
    soundscapeId = soundscapeId,
    name = name,
    mixConfig = MixConfig(
        channelLevels = gson.fromJson(channelLevelsJson, Array<Float>::class.java).toList()
    ),
    isDefault = isDefault
)

private fun Preset.toEntity(): PresetEntity = PresetEntity(
    id = id,
    soundscapeId = soundscapeId,
    name = name,
    channelLevelsJson = gson.toJson(mixConfig.channelLevels),
    isDefault = isDefault
)

// ============================================================
// ALARM REPOSITORY
// ============================================================

class AlarmRepositoryImpl @Inject constructor(
    private val dao: AlarmDao
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveAlarm(alarm: Alarm) {
        dao.insert(alarm.toEntity())
    }

    override suspend fun deleteAlarm(alarmId: String) {
        dao.deleteById(alarmId)
    }

    override suspend fun setAlarmEnabled(alarmId: String, enabled: Boolean) {
        dao.setEnabled(alarmId, enabled)
    }
}

private fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    hour = hour,
    minute = minute,
    soundscapeId = soundscapeId,
    presetId = presetId,
    fadeInSeconds = fadeInSeconds,
    isEnabled = isEnabled,
    repeatDays = gson.fromJson(repeatDaysJson, Array<Int>::class.java).toSet()
)

private fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    hour = hour,
    minute = minute,
    soundscapeId = soundscapeId,
    presetId = presetId,
    fadeInSeconds = fadeInSeconds,
    isEnabled = isEnabled,
    repeatDaysJson = gson.toJson(repeatDays.toList())
)
