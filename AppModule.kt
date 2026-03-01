package com.soundscape.core.data

import android.content.Context
import androidx.room.Room
import com.soundscape.core.data.db.AlarmDao
import com.soundscape.core.data.db.PresetDao
import com.soundscape.core.data.db.SoundScapeDatabase
import com.soundscape.core.data.db.SoundscapeDao
import com.soundscape.core.data.repository.AlarmRepositoryImpl
import com.soundscape.core.data.repository.PresetRepositoryImpl
import com.soundscape.core.data.repository.SoundscapeRepositoryImpl
import com.soundscape.core.domain.model.AlarmRepository
import com.soundscape.core.domain.model.PresetRepository
import com.soundscape.core.domain.model.SoundscapeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SoundScapeDatabase {
        return Room.databaseBuilder(
            context,
            SoundScapeDatabase::class.java,
            SoundScapeDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // В продакшне заменить на миграции
            .build()
    }

    @Provides
    fun provideSoundscapeDao(db: SoundScapeDatabase): SoundscapeDao = db.soundscapeDao()

    @Provides
    fun providePresetDao(db: SoundScapeDatabase): PresetDao = db.presetDao()

    @Provides
    fun provideAlarmDao(db: SoundScapeDatabase): AlarmDao = db.alarmDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSoundscapeRepository(impl: SoundscapeRepositoryImpl): SoundscapeRepository

    @Binds
    @Singleton
    abstract fun bindPresetRepository(impl: PresetRepositoryImpl): PresetRepository

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(impl: AlarmRepositoryImpl): AlarmRepository
}
