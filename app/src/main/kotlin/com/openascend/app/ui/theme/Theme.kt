package com.openascend.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val AscendDark = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFB39DFF),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF1B1028),
    secondary = androidx.compose.ui.graphics.Color(0xFF7CFFB2),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF002114),
    tertiary = androidx.compose.ui.graphics.Color(0xFFFFB86C),
    background = androidx.compose.ui.graphics.Color(0xFF0E0B12),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE8E0F5),
    surface = androidx.compose.ui.graphics.Color(0xFF16111F),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE8E0F5),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2A2233),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFCFC3DC),
)

private val AscendLight = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF5E3FA7),
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    secondary = androidx.compose.ui.graphics.Color(0xFF006C45),
    onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    tertiary = androidx.compose.ui.graphics.Color(0xFFB85C00),
    background = androidx.compose.ui.graphics.Color(0xFFF7F4FF),
    onBackground = androidx.compose.ui.graphics.Color(0xFF1B1028),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1B1028),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE8E0F5),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF4A3F5C),
)

@Composable
fun OpenAscendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> AscendDark
        else -> AscendLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
