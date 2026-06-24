package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = CyberCyan,
    tertiary = NeonGreen,
    background = SpaceDark,
    surface = MetalSurface,
    onPrimary = SpaceDark,
    onSecondary = SpaceDark,
    onBackground = SoftWhite,
    onSurface = SoftWhite,
    primaryContainer = Purple40,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF312E81)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = SoftWhite,
    surface = SoftWhite,
    onPrimary = SoftWhite,
    onSecondary = SoftWhite,
    onBackground = SpaceDark,
    onSurface = SpaceDark
)

private val ColorPrimaryContainerForDark = androidx.compose.ui.graphics.Color(0xFF312E81)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Theme for a premium cohesive Arcade console look!
    dynamicColor: Boolean = false, // Use our handcrafted arcade colors by default!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
