package com.soundscape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.soundscape.core.ui.theme.SoundScapeTheme
import com.soundscape.navigation.SoundScapeNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * Единственная Activity в приложении.
 * Всё UI строится через Jetpack Compose и Navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SoundScapeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SoundScapeTheme.colors.background
                ) {
                    SoundScapeNavHost()
                }
            }
        }
    }
}
