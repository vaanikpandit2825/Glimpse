package com.example.glimpse.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.glimpse.R

object GlimpseFonts {
    val PlayfairDisplay = FontFamily(
        Font(R.font.playfair_display_regular,  FontWeight.Normal),
        Font(R.font.playfair_display_medium,   FontWeight.Medium),
        Font(R.font.playfair_display_semibold, FontWeight.SemiBold),
        Font(R.font.playfair_display_bold,     FontWeight.Bold)
    )
}