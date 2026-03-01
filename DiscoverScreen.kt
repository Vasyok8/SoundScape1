package com.soundscape.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.soundscape.core.ui.theme.OrangeAccent
import com.soundscape.core.ui.theme.SoundScapeTheme

// Временные данные для UI-разработки (заменятся ViewModel + Room в Этапе 4)
private data class SoundscapePreview(
    val id: String,
    val title: String,
    val category: String,
    val subtitle: String,
    val bgColor: Color,
    val isDownloaded: Boolean = false
)

private val featuredSoundscape = SoundscapePreview(
    id = "dissociative_trance",
    title = "Dissociative Trance",
    subtitle = "Discover our newest soundscape",
    category = "Experimental",
    bgColor = Color(0xFF3D1A6E)
)

private val recommendedSoundscapes = listOf(
    SoundscapePreview("distant_thunder", "Distant Thunder", "Natural Noises", "Thunder & Rain", Color(0xFF1A2A3A)),
    SoundscapePreview("cafe_restaurant", "Cafe Restaurant", "Spaces", "Noise Without Caffeine", Color(0xFF3A2010), isDownloaded = true),
    SoundscapePreview("pure_white", "Pure White Noise", "Experimental", "Pure Sound", Color(0xFF1A3A2A)),
    SoundscapePreview("ocean_waves", "Ocean Waves", "Water", "Coastal Ambience", Color(0xFF0A2040)),
    SoundscapePreview("deep_forest", "Deep Forest", "Natural Noises", "Birds & Wind", Color(0xFF0A2010)),
)

@Composable
fun DiscoverScreen(
    onSoundscapeClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundScapeTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)
    ) {
        // Заголовок
        Text(
            text = "Discover",
            style = MaterialTheme.typography.displayLarge,
            color = SoundScapeTheme.colors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Featured Card
        var showFeatured by remember { mutableStateOf(true) }
        if (showFeatured) {
            FeaturedCard(
                soundscape = featuredSoundscape,
                onClose = { showFeatured = false },
                onClick = { onSoundscapeClick(featuredSoundscape.id) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Recommended section
        Text(
            text = "Based on your preferences",
            style = MaterialTheme.typography.titleLarge,
            color = SoundScapeTheme.colors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recommendedSoundscapes) { soundscape ->
                SoundscapeCard(
                    soundscape = soundscape,
                    onClick = { onSoundscapeClick(soundscape.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FeaturedCard(
    soundscape: SoundscapePreview,
    onClose: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(soundscape.bgColor.copy(alpha = 0.6f), soundscape.bgColor)
                )
            )
            .clickable { onClick() }
    ) {
        // Кнопка закрытия
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White.copy(alpha = 0.7f)
            )
        }

        // Текст внизу карточки
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = soundscape.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = soundscape.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Listen now!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SoundscapeCard(
    soundscape: SoundscapePreview,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        // Изображение / цветной блок
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(soundscape.bgColor)
        ) {
            if (soundscape.isDownloaded) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Downloaded",
                    tint = OrangeAccent,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = soundscape.category.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = OrangeAccent
        )

        Text(
            text = soundscape.title,
            style = MaterialTheme.typography.titleMedium,
            color = SoundScapeTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = soundscape.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = SoundScapeTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
