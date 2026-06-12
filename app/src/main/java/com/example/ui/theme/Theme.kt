package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

@Composable
fun MyApplicationTheme(
  isAmoled: Boolean = true,
  accentColor: String = "#38BDF8",
  content: @Composable () -> Unit,
) {
  val primaryColor = try { 
    Color(android.graphics.Color.parseColor(accentColor)) 
  } catch(e: Exception) { 
    Color(0xFF38BDF8) 
  }
  
  val backgroundColor = if (isAmoled) Color(0xFF000000) else Color(0xFF0F172A)
  val surfaceColor = if (isAmoled) Color(0xFF0A0F1D) else Color(0xFF1E293B)
  
  val colorScheme = darkColorScheme(
    primary = primaryColor,
    onPrimary = Color.Black,
    secondary = primaryColor.copy(alpha = 0.8f),
    onSecondary = Color.Black,
    background = backgroundColor,
    onBackground = Color.White,
    surface = surfaceColor,
    onSurface = Color.White,
    outline = Color(0xFF334155),
    surfaceVariant = surfaceColor,
    onSurfaceVariant = Color(0xFF94A3B8)
  )

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
