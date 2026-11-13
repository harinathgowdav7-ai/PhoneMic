package com.phonemic.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Exact 1:1 PhoneMic Reference Palette from kk.jpeg
val PhoneMicGreenPrimary = Color(0xFF15803D)      // Button primary fill
val PhoneMicGreenVibrant = Color(0xFF16A34A)      // Icon & active accent green
val PhoneMicGreenBright = Color(0xFF22C55E)       // Ring glow & waveform top
val PhoneMicGreenSoftBg = Color(0xFFE8F8EE)       // Feature icon circle & active tab tint
val PhoneMicGreenWaveLight = Color(0xFFD1FAE5)    // Bottom wave highlight
val PhoneMicRedDisconnect = Color(0xFFEF4444)     // Disconnect red button
val PhoneMicBgLight = Color(0xFFF8FAFC)           // Screen background
val PhoneMicCardBg = Color(0xFFFFFFFF)            // Card surface
val PhoneMicSecondarySurface = Color(0xFFF1F5F9)  // Gray button & tab bar container
val PhoneMicBorder = Color(0xFFE2E8F0)            // Input & card border
val PhoneMicTextPrimary = Color(0xFF0F172A)       // Deep slate/black text
val PhoneMicTextSecondary = Color(0xFF64748B)     // Subtitle & secondary text

private val LightColorPalette = lightColorScheme(
    primary = PhoneMicGreenPrimary,
    secondary = PhoneMicGreenVibrant,
    background = PhoneMicBgLight,
    surface = PhoneMicCardBg,
    onPrimary = Color.White,
    onBackground = PhoneMicTextPrimary,
    onSurface = PhoneMicTextPrimary
)

@Composable
fun PhoneMicTheme(
    darkMode: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorPalette,
        content = content
    )
}
