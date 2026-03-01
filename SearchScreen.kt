package com.soundscape.feature.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.soundscape.core.ui.theme.SoundScapeTheme

// Временно помещён сюда — будет перенесён в feature/search на Этапе 5
@Composable
fun SearchScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundScapeTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.displayLarge,
            color = SoundScapeTheme.colors.textSecondary
        )
    }
}
