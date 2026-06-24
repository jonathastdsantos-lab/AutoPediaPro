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

private val DarkColorScheme = darkColorScheme(
    primary = ElegantPrimary,
    secondary = ElegantSecondaryContainer,
    tertiary = ElegantTertiary,
    background = ElegantBackground,
    surface = ElegantSurface,
    onPrimary = ElegantOnPrimary,
    onSecondary = ElegantOnSecondary,
    onBackground = ElegantOnBackground,
    onSurface = ElegantOnSurface,
    surfaceVariant = ElegantSurface,
    onSurfaceVariant = ElegantOnSurfaceVariant,
    outline = ElegantOutline,
    error = ElegantError,
    onError = ElegantOnError
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4), // Standard M3 Purple
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFEF7FF),
    surface = Color(0xFFF7F2FA),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true or our customized DarkColorScheme
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Disable dynamic color to maintain consistent navy/red branding
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
