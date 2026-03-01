package com.soundscape.feature.player

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AlarmAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soundscape.core.audio.AudioCommand
import com.soundscape.core.audio.PlaybackService
import com.soundscape.core.audio.PlayerState
import com.soundscape.core.ui.theme.OrangeAccent
import com.soundscape.core.ui.theme.SliderColors
import com.soundscape.core.ui.theme.SoundScapeTheme

/**
 * Главный экран плеера — повторяет UX myNoise.
 *
 * Содержит:
 * - 10 вертикальных слайдеров с цветовым градиентом
 * - Кнопки: Play/Pause, Info, Share
 * - Нижняя панель: Animate, Presets, Alarm, Timer
 * - Информация о саундскейпе и пресете
 */
@Composable
fun PlayerScreen(
    soundscapeId: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Загружаем саундскейп при первом открытии экрана
    LaunchedEffect(soundscapeId) {
        viewModel.loadSoundscape(soundscapeId)
        // Запускаем Service для фонового воспроизведения
        PlaybackService.startService(context)
    }

    var showTimerDialog by remember { mutableStateOf(false) }
    var showPresetsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0A1A), Color(0xFF000000))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // ─── Верхняя стрелка (назад) ─────────────────────────────
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Back",
                    tint = SoundScapeTheme.colors.textSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ─── 10 вертикальных слайдеров ───────────────────────────
            SlidersSection(
                levels = state.channelLevels,
                onLevelChange = { index, level ->
                    viewModel.onCommand(AudioCommand.SetChannelLevel(index, level))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // ─── Название и пресет ───────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Text(
                    text = state.soundscapeTitle.ifEmpty {
                        soundscapeId.replace("_", " ").replaceFirstChar { it.uppercase() }
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = SoundScapeTheme.colors.textPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = state.currentPresetName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoundScapeTheme.colors.textSecondary
                    )
                    state.sleepTimerMinutesLeft?.let { minutes ->
                        Text(
                            text = "· ${minutes}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrangeAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Основные кнопки управления ──────────────────────────
            MainControls(
                isPlaying = state.isPlaying,
                onPlayPause = {
                    if (state.isPlaying)
                        viewModel.onCommand(AudioCommand.Pause)
                    else
                        viewModel.onCommand(AudioCommand.Play)
                },
                onInfo = { /* TODO: показать описание */ },
                onShare = { /* TODO: поделиться конфигом */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Нижняя панель: Animate, Presets, Alarm, Timer ───────
            BottomControlBar(
                isAnimating = state.isAnimating,
                onAnimate = { viewModel.onCommand(AudioCommand.SetAnimating(!state.isAnimating)) },
                onPresets = { showPresetsDialog = true },
                onAlarm = { /* TODO: Этап 6 */ },
                onTimer = { showTimerDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ─── Диалог выбора таймера ───────────────────────────────────────
    if (showTimerDialog) {
        SleepTimerDialog(
            currentMinutes = state.sleepTimerMinutesLeft,
            onSelect = { minutes ->
                viewModel.onCommand(AudioCommand.SetSleepTimer(minutes))
                showTimerDialog = false
            },
            onCancel = {
                viewModel.onCommand(AudioCommand.CancelSleepTimer)
                showTimerDialog = false
            },
            onDismiss = { showTimerDialog = false }
        )
    }
}

// ─── Секция слайдеров ────────────────────────────────────────────────

@Composable
private fun SlidersSection(
    levels: List<Float>,
    onLevelChange: (Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        levels.forEachIndexed { index, level ->
            VerticalSlider(
                value = level,
                onValueChange = { newValue -> onLevelChange(index, newValue) },
                color = SliderColors.getOrElse(index) { SliderColors.last() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

/**
 * Вертикальный слайдер — повторяет дизайн myNoise.
 *
 * Реализован через detectVerticalDragGestures, так как стандартный
 * Slider в Compose горизонтальный и плохо поддаётся вертикальной ориентации.
 */
@Composable
private fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var trackHeightPx by remember { mutableFloatStateOf(0f) }

    // Анимируем позицию шарика
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "slider_$value"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    if (trackHeightPx > 0f) {
                        // Тянем вниз — уменьшаем значение, тянем вверх — увеличиваем
                        val delta = -dragAmount / trackHeightPx
                        val newValue = (value + delta).coerceIn(0f, 1f)
                        onValueChange(newValue)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Трек (вертикальная полоса)
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.1f))
        )

        // Заполненная часть снизу до шарика
        val filledFraction = animatedValue
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(filledFraction)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(2.dp))
                .background(color.copy(alpha = 0.3f))
        )

        // Шарик слайдера
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // Позиционируем шарик пропорционально значению
                .graphicsLayer {
                    if (trackHeightPx == 0f) trackHeightPx = size.height
                    translationY = -(animatedValue * (trackHeightPx - 36.dp.toPx()))
                }
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

// ─── Основные кнопки ─────────────────────────────────────────────────

@Composable
private fun MainControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onInfo: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка Speaker/Speaker sets (будущая функция)
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Equalizer",
                tint = SoundScapeTheme.colors.textSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Info
        IconButton(onClick = onInfo) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = SoundScapeTheme.colors.textSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Play/Pause — главная кнопка
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(32.dp)
            )
        }

        // Share
        IconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = SoundScapeTheme.colors.textSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Placeholder (в myNoise здесь — Download)
        Spacer(modifier = Modifier.size(48.dp))
    }
}

// ─── Нижняя панель ───────────────────────────────────────────────────

@Composable
private fun BottomControlBar(
    isAnimating: Boolean,
    onAnimate: () -> Unit,
    onPresets: () -> Unit,
    onAlarm: () -> Unit,
    onTimer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomBarItem(
            label = "Animate",
            icon = { AnimateIcon(isAnimating) },
            onClick = onAnimate,
            isActive = isAnimating
        )
        BottomBarItem(
            label = "Presets",
            icon = { Icon(Icons.Default.Tune, null, modifier = Modifier.size(24.dp)) },
            onClick = onPresets
        )
        BottomBarItem(
            label = "Alarm",
            icon = { Icon(Icons.Outlined.AlarmAdd, null, modifier = Modifier.size(24.dp)) },
            onClick = onAlarm
        )
        BottomBarItem(
            label = "Timer",
            icon = { Icon(Icons.Default.Timer, null, modifier = Modifier.size(24.dp)) },
            onClick = onTimer
        )
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    val tint by animateColorAsState(
        targetValue = if (isActive) OrangeAccent else SoundScapeTheme.colors.textSecondary,
        label = "icon_tint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
    ) {
        IconButton(onClick = onClick) {
            CompositionLocalProvider(LocalContentColor provides tint) {
                icon()
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnimateIcon(isActive: Boolean) {
    // Простая визуализация "волны" как в myNoise
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val heights = listOf(0.4f, 0.7f, 1f, 0.7f, 0.4f)
        heights.forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((20 * fraction).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isActive) OrangeAccent
                        else SoundScapeTheme.colors.textSecondary
                    )
            )
        }
    }
}

// ─── Диалог таймера ──────────────────────────────────────────────────

@Composable
private fun SleepTimerDialog(
    currentMinutes: Int?,
    onSelect: (Int) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(15, 30, 45, 60, 90, 120)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text(
                "Sleep Timer",
                color = SoundScapeTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { minutes ->
                    TextButton(
                        onClick = { onSelect(minutes) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (minutes < 60) "$minutes minutes"
                            else "${minutes / 60} hour${if (minutes > 60) "s" else ""}",
                            color = SoundScapeTheme.colors.textPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (currentMinutes != null) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel timer", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
