package com.soundscape.core.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.soundscape.core.data.db.SoundScapeDatabase
import com.soundscape.core.data.db.SoundscapeEntity
import com.soundscape.core.domain.model.AudioType
import com.soundscape.core.domain.model.GeneratorType
import com.soundscape.core.domain.model.Soundscape
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Инициализирует базу данных встроенным каталогом саундскейпов
 * при первом запуске приложения.
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: SoundScapeDatabase
) {
    companion object {
        private const val TAG = "DatabaseInitializer"
        private const val PREFS_NAME = "soundscape_init"
        private const val KEY_INITIALIZED = "db_initialized_v1"
    }

    private val gson = Gson()

    fun initializeIfNeeded() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_INITIALIZED, false)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entities = SoundscapeDataSource.builtInSoundscapes.map { it.toEntity() }
                database.soundscapeDao().insertAll(entities)
                prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
                Log.d(TAG, "Database initialized with ${entities.size} soundscapes")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize database", e)
            }
        }
    }

    private fun Soundscape.toEntity(): SoundscapeEntity = SoundscapeEntity(
        id = id,
        title = title,
        category = category.name,
        description = description,
        imageRes = imageRes,
        audioTypeJson = audioTypeToJson(audioType),
        isDownloaded = isDownloaded,
        isPurchased = isPurchased,
        isFeatured = isFeatured,
        tagsJson = gson.toJson(tags)
    )

    private fun audioTypeToJson(audioType: AudioType): String {
        val map = when (audioType) {
            is AudioType.Generated -> mapOf("type" to "generated", "generator" to audioType.generatorType.name)
            is AudioType.File -> mapOf("type" to "file", "fileName" to audioType.fileName)
            is AudioType.Mixed -> mapOf("type" to "mixed", "fileName" to audioType.fileName, "generator" to audioType.generatorType.name)
        }
        return gson.toJson(map)
    }
}
