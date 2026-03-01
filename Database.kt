package com.soundscape.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

// ============================================================
// ENTITIES
// ============================================================

@Entity(tableName = "soundscapes")
data class SoundscapeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val description: String,
    @ColumnInfo(name = "image_res") val imageRes: Int,
    @ColumnInfo(name = "audio_type_json") val audioTypeJson: String,
    @ColumnInfo(name = "is_downloaded") val isDownloaded: Boolean = false,
    @ColumnInfo(name = "is_purchased") val isPurchased: Boolean = false,
    @ColumnInfo(name = "is_featured") val isFeatured: Boolean = false,
    @ColumnInfo(name = "tags_json") val tagsJson: String = "[]"
)

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "soundscape_id") val soundscapeId: String,
    val name: String,
    @ColumnInfo(name = "channel_levels_json") val channelLevelsJson: String,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false
)

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val id: String,
    val hour: Int,
    val minute: Int,
    @ColumnInfo(name = "soundscape_id") val soundscapeId: String,
    @ColumnInfo(name = "preset_id") val presetId: String?,
    @ColumnInfo(name = "fade_in_seconds") val fadeInSeconds: Int = 60,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "repeat_days_json") val repeatDaysJson: String = "[]"
)

// ============================================================
// TYPE CONVERTERS
// ============================================================

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromFloatList(value: List<Float>): String = gson.toJson(value)

    @TypeConverter
    fun toFloatList(value: String): List<Float> =
        gson.fromJson(value, Array<Float>::class.java).toList()

    @TypeConverter
    fun fromIntSet(value: Set<Int>): String = gson.toJson(value.toList())

    @TypeConverter
    fun toIntSet(value: String): Set<Int> =
        gson.fromJson(value, Array<Int>::class.java).toSet()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        gson.fromJson(value, Array<String>::class.java).toList()
}

// ============================================================
// DAOs
// ============================================================

@Dao
interface SoundscapeDao {

    @Query("SELECT * FROM soundscapes")
    fun getAll(): Flow<List<SoundscapeEntity>>

    @Query("SELECT * FROM soundscapes WHERE category = :category")
    fun getByCategory(category: String): Flow<List<SoundscapeEntity>>

    @Query("SELECT * FROM soundscapes WHERE is_featured = 1")
    fun getFeatured(): Flow<List<SoundscapeEntity>>

    @Query("SELECT * FROM soundscapes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SoundscapeEntity?

    @Query("SELECT * FROM soundscapes WHERE title LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<SoundscapeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(soundscape: SoundscapeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(soundscapes: List<SoundscapeEntity>)

    @Update
    suspend fun update(soundscape: SoundscapeEntity)

    @Delete
    suspend fun delete(soundscape: SoundscapeEntity)
}

@Dao
interface PresetDao {

    @Query("SELECT * FROM presets WHERE soundscape_id = :soundscapeId ORDER BY is_default DESC, name ASC")
    fun getPresetsForSoundscape(soundscapeId: String): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preset: PresetEntity)

    @Query("DELETE FROM presets WHERE id = :presetId")
    suspend fun deleteById(presetId: String)
}

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAll(): Flow<List<AlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :alarmId")
    suspend fun deleteById(alarmId: String)

    @Query("UPDATE alarms SET is_enabled = :enabled WHERE id = :alarmId")
    suspend fun setEnabled(alarmId: String, enabled: Boolean)
}

// ============================================================
// DATABASE
// ============================================================

@Database(
    entities = [SoundscapeEntity::class, PresetEntity::class, AlarmEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SoundScapeDatabase : RoomDatabase() {
    abstract fun soundscapeDao(): SoundscapeDao
    abstract fun presetDao(): PresetDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        const val DATABASE_NAME = "soundscape.db"
    }
}
