package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = StitchPrimaryDark,
    secondary = StitchSecondaryDark,
    tertiary = StitchTertiary,
    background = StitchBackgroundDark,
    surface = StitchSurfaceDark,
    onPrimary = StitchOnPrimaryDark,
    onSecondary = StitchOnSecondaryDark,
    onTertiary = StitchOnTertiary,
    primaryContainer = StitchPrimaryContainerDark,
    onPrimaryContainer = StitchOnPrimaryContainerDark,
    secondaryContainer = StitchSecondaryContainerDark,
    onSecondaryContainer = StitchOnSecondaryContainerDark,
    tertiaryContainer = StitchTertiaryContainer,
    onTertiaryContainer = StitchOnTertiaryContainer,
    outline = StitchOutlineDark,
    outlineVariant = StitchOutlineVariantDark,
    onBackground = StitchOnBackgroundDark,
    onSurface = StitchOnSurfaceDark,
    onSurfaceVariant = StitchOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = StitchPrimary,
    secondary = StitchSecondary,
    tertiary = StitchTertiary,
    background = StitchBackground,
    surface = StitchSurface,
    onPrimary = StitchOnPrimary,
    onSecondary = StitchOnSecondary,
    onTertiary = StitchOnTertiary,
    primaryContainer = StitchPrimaryContainer,
    onPrimaryContainer = StitchOnPrimaryContainer,
    secondaryContainer = StitchSecondaryContainer,
    onSecondaryContainer = StitchOnSecondaryContainer,
    tertiaryContainer = StitchTertiaryContainer,
    onTertiaryContainer = StitchOnTertiaryContainer,
    outline = StitchOutline,
    outlineVariant = StitchOutlineVariant,
    onBackground = StitchOnBackground,
    onSurface = StitchOnSurface,
    onSurfaceVariant = StitchOnSurfaceVariant
)

/**
 * Main application theme wrapper that applies CareFlow design tokens.
 *
 * @param darkTheme Determines whether to apply the dark color scheme.
 * @param dynamicColor If true, attempts to apply Android 12+ dynamic system coloring (disabled by default).
 * @param content Composable lambda structure containing screens to style.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disengage dynamicColor by default to guarantee our medical-branded clinical color palette is visible to patients!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
