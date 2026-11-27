package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Immutable
data class SynapseExtendedColors(
    val voidBackground: Color,
    val surfaceLow: Color,
    val cardSurface: Color,
    val cardElevated: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val accent: Color,
    val onAccent: Color,
    val secondaryAccent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val highContrast: Boolean,
    val reduceMotion: Boolean
)

val LocalSynapseColors = staticCompositionLocalOf {
    SynapseExtendedColors(
        voidBackground = ObsidianVoid,
        surfaceLow = ObsidianSurface,
        cardSurface = ObsidianCard,
        cardElevated = ObsidianElevated,
        borderSubtle = ObsidianBorder,
        borderStrong = Color(0xFF384B6E),
        accent = SynapseCyan,
        onAccent = Color(0xFF04060C),
        secondaryAccent = SynapseIndigo,
        success = SynapseMint,
        warning = SynapseAmber,
        danger = SynapseCoral,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textMuted = TextMuted,
        highContrast = false,
        reduceMotion = false
    )
}

val SynapseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    isAmoled: Boolean = true,
    accentColor: String = "#38BDF8",
    highContrast: Boolean = false,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val parsedAccent = try {
        Color(android.graphics.Color.parseColor(accentColor))
    } catch (_: Exception) {
        SynapseCyan
    }

    val onAccentColor = if (parsedAccent.luminance() > 0.45f) Color(0xFF04060C) else Color.White

    val voidBg = when {
        highContrast -> Color.Black
        isAmoled -> ObsidianVoid
        else -> MidnightVoid
    }
    val surfaceLow = when {
        highContrast -> Color(0xFF05070B)
        isAmoled -> ObsidianSurface
        else -> MidnightSurface
    }
    val cardBg = when {
        highContrast -> Color(0xFF0D111A)
        isAmoled -> ObsidianCard
        else -> MidnightCard
    }
    val cardElevated = when {
        highContrast -> Color(0xFF161D2B)
        isAmoled -> ObsidianElevated
        else -> MidnightElevated
    }
    val borderSubtle = when {
        highContrast -> Color(0xFF64748B)
        isAmoled -> ObsidianBorder
        else -> MidnightBorder
    }
    val borderStrong = when {
        highContrast -> Color(0xFFCBD5E1)
        isAmoled -> Color(0xFF384B6E)
        else -> Color(0xFF435B8C)
    }

    val extended = SynapseExtendedColors(
        voidBackground = voidBg,
        surfaceLow = surfaceLow,
        cardSurface = cardBg,
        cardElevated = cardElevated,
        borderSubtle = borderSubtle,
        borderStrong = borderStrong,
        accent = parsedAccent,
        onAccent = onAccentColor,
        secondaryAccent = SynapseIndigo,
        success = SynapseMint,
        warning = SynapseAmber,
        danger = SynapseCoral,
        textPrimary = if (highContrast) Color.White else TextPrimary,
        textSecondary = if (highContrast) Color(0xFFF1F5F9) else TextSecondary,
        textMuted = if (highContrast) Color(0xFFCBD5E1) else TextMuted,
        highContrast = highContrast,
        reduceMotion = reduceMotion
    )

    val colorScheme = darkColorScheme(
        primary = parsedAccent,
        onPrimary = onAccentColor,
        primaryContainer = parsedAccent.copy(alpha = 0.16f),
        onPrimaryContainer = parsedAccent,
        secondary = SynapseIndigo,
        onSecondary = Color.White,
        background = voidBg,
        onBackground = extended.textPrimary,
        surface = surfaceLow,
        onSurface = extended.textPrimary,
        surfaceVariant = cardBg,
        onSurfaceVariant = extended.textMuted,
        outline = borderSubtle,
        outlineVariant = borderStrong,
        error = SynapseCoral,
        onError = Color.Black
    )

    CompositionLocalProvider(LocalSynapseColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = SynapseShapes,
            content = content
        )
    }
}
