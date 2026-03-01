package com.soundscape.core.audio

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt модуль для Audio-зависимостей.
 * MixerEngine и AudioFocusManager — синглтоны на всё приложение.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideAudioFocusManager(@ApplicationContext context: Context): AudioFocusManager {
        return AudioFocusManager(context)
    }

    // MixerEngine инжектируется через @Inject constructor — Hilt создаёт его сам
    // AudioFocusManager предоставляется через provideAudioFocusManager выше
}
