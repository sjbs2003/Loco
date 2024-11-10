package com.example.loco.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Theme Colors
private val LightColors = lightColorScheme(
    primary = Color(0xFF006D3B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF95F7B5),
    onPrimaryContainer = Color(0xFF002110),
    secondary = Color(0xFF4F6354),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E8D5),
    onSecondaryContainer = Color(0xFF0C1F13),
    tertiary = Color(0xFF006C51),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF89F8D1),
    onTertiaryContainer = Color(0xFF002117),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBFDF8),
    onBackground = Color(0xFF191C19),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF191C19),
    surfaceVariant = Color(0xFFDCE5DB),
    onSurfaceVariant = Color(0xFF414942),
    outline = Color(0xFF717971),
    inverseOnSurface = Color(0xFFF0F1EC),
    inverseSurface = Color(0xFF2E312E),
    inversePrimary = Color(0xFF78DA9A)
)

// Dark Theme Colors
private val DarkColors = darkColorScheme(
    primary = Color(0xFF78DA9A),
    onPrimary = Color(0xFF003919),
    primaryContainer = Color(0xFF005229),
    onPrimaryContainer = Color(0xFF95F7B5),
    secondary = Color(0xFFB5CCB9),
    onSecondary = Color(0xFF213527),
    secondaryContainer = Color(0xFF374B3D),
    onSecondaryContainer = Color(0xFFD1E8D5),
    tertiary = Color(0xFF6CDBB5),
    onTertiary = Color(0xFF003828),
    tertiaryContainer = Color(0xFF00513C),
    onTertiaryContainer = Color(0xFF89F8D1),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF191C19),
    onBackground = Color(0xFFE1E3DE),
    surface = Color(0xFF191C19),
    onSurface = Color(0xFFE1E3DE),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC0C9C0),
    outline = Color(0xFF8B938C),
    inverseOnSurface = Color(0xFF191C19),
    inverseSurface = Color(0xFFE1E3DE),
    inversePrimary = Color(0xFF006D3B)
)

@Composable
fun LocoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Extension variables for commonly used colors
val ColorScheme.noteCardBackground: Color
    get() = surfaceVariant

val ColorScheme.searchBarBackground: Color
    get() = surfaceVariant.copy(alpha = 0.5f)

val ColorScheme.fabBackground: Color
    get() = primaryContainer

val ColorScheme.drawerBackground: Color
    get() = surface