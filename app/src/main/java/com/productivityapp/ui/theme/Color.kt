package com.productivityapp.ui.theme

import androidx.compose.ui.graphics.Color

// Primary colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Project color palette
val ProjectColors = listOf(
    Color(0xFF6200EE),
    Color(0xFF3700B3),
    Color(0xFF03DAC6),
    Color(0xFF018786),
    Color(0xFFB00020),
    Color(0xFFFF6B6B),
    Color(0xFFFFE66D),
    Color(0xFF4ECDC4),
    Color(0xFF45B7D1),
    Color(0xFFFF8A5B),
    Color(0xFFA06CD5),
    Color(0xFF6BCB77)
)

fun getProjectColorHex(index: Int): String {
    return when (index % ProjectColors.size) {
        0 -> "#6200EE"
        1 -> "#3700B3"
        2 -> "#03DAC6"
        3 -> "#018786"
        4 -> "#B00020"
        5 -> "#FF6B6B"
        6 -> "#FFE66D"
        7 -> "#4ECDC4"
        8 -> "#45B7D1"
        9 -> "#FF8A5B"
        10 -> "#A06CD5"
        11 -> "#6BCB77"
        else -> "#6200EE"
    }
}
