package com.example.lostandfound.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = LaravelRed,
    onPrimary = White,
    primaryContainer = White,
    onPrimaryContainer = DarkGray,
    secondary = LaravelRed,
    onSecondary = White,
    secondaryContainer = White,
    onSecondaryContainer = DarkGray,
    tertiary = LaravelRed,
    onTertiary = White,
    tertiaryContainer = White,
    onTertiaryContainer = DarkGray,
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRed,
    onErrorContainer = White,
    background = White,
    onBackground = DarkGray,
    surface = White,
    onSurface = DarkGray,
    surfaceVariant = LightGray,
    onSurfaceVariant = TextGrey,
    outline = BorderGrey,
)

private val DarkColorScheme = darkColorScheme(
    primary = LaravelRed,
    onPrimary = White,
    primaryContainer = DarkGray,
    onPrimaryContainer = White,
    secondary = LaravelRed,
    onSecondary = White,
    secondaryContainer = DarkGray,
    onSecondaryContainer = White,
    tertiary = LaravelRed,
    onTertiary = White,
    tertiaryContainer = DarkGray,
    onTertiaryContainer = White,
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRed,
    onErrorContainer = White,
    background = DarkGray,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray,
    outline = BorderGrey
)

// Square shapes for ALL components
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

@Composable
fun LostAndFoundTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.navigationBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}