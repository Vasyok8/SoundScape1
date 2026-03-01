package com.soundscape

import android.app.Application
import com.soundscape.core.data.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class — точка входа Hilt DI.
 * Здесь инициализируем базу данных при первом запуске.
 */
@HiltAndroidApp
class SoundScapeApp : Application() {

    @Inject lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate() {
        super.onCreate()
        databaseInitializer.initializeIfNeeded()
    }
}
