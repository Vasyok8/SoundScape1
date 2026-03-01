package com.soundscape.core.ui.theme

import androidx.compose.ui.graphics.Color

// === Основные цвета ===
val Black = Color(0xFF000000)
val DarkSurface = Color(0xFF1A1A1A)
val DarkSurfaceVariant = Color(0xFF252525)

// === Акцентные цвета ===
val OrangeAccent = Color(0xFFFF8C00)      // Активные элементы, иконки, кнопки
val OrangeLight = Color(0xFFFFAA44)

// === Слайдеры — градиент от фиолетового к бирюзовому ===
val SliderPurple1 = Color(0xFF5B4BA0)     // Канал 1 (низкие частоты)
val SliderPurple2 = Color(0xFF4D3F8F)
val SliderPurple3 = Color(0xFF4A4A9F)
val SliderPurple4 = Color(0xFF4055A8)
val SliderBlue5 = Color(0xFF3A6AB0)
val SliderBlue6 = Color(0xFF3A7DB5)
val SliderTeal7 = Color(0xFF3EA89A)
val SliderTeal8 = Color(0xFF44B89E)
val SliderTeal9 = Color(0xFF48C4A4)
val SliderTeal10 = Color(0xFF4ECFAA)      // Канал 10 (высокие частоты)

// Список цветов слайдеров по порядку
val SliderColors = listOf(
    SliderPurple1, SliderPurple2, SliderPurple3, SliderPurple4,
    SliderBlue5, SliderBlue6,
    SliderTeal7, SliderTeal8, SliderTeal9, SliderTeal10
)

// === Текст ===
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFAAAAAA)
val TextDisabled = Color(0xFF555555)

// === Категории (цвет как в myNoise) ===
val CategoryOrange = Color(0xFFFF8C00)

// === Статусы ===
val Success = Color(0xFF4CAF50)
val Error = Color(0xFFE53935)
val Warning = Color(0xFFFFB300)
