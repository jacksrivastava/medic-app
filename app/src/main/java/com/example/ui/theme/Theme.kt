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
    primary = BentoBlueAccentDark,
    secondary = BentoSalmonBgDark,
    tertiary = BentoGreenBgDark,
    background = BentoBgDark,
    surface = BentoBackboneDark,
    onPrimary = BentoTextLight,
    onSecondary = BentoDeepRedTextDark,
    onTertiary = BentoDeepGreenTextDark,
    primaryContainer = BentoLightBlueBgDark,
    onPrimaryContainer = BentoDeepBlueTextDark,
    secondaryContainer = BentoSalmonBgDark,
    onSecondaryContainer = BentoDeepRedTextDark,
    tertiaryContainer = BentoGreenBgDark,
    onTertiaryContainer = BentoDeepGreenTextDark,
    outline = BentoMutedTextDark
)

private val LightColorScheme = lightColorScheme(
    primary = BentoBlueAccent,
    secondary = BentoSalmonBg,
    tertiary = BentoGreenBg,
    background = BentoBgLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = BentoDeepRedText,
    onTertiary = BentoDeepGreenText,
    primaryContainer = BentoLightBlueBg,
    onPrimaryContainer = BentoDeepBlueText,
    secondaryContainer = BentoSalmonBg,
    onSecondaryContainer = BentoDeepRedText,
    tertiaryContainer = BentoGreenBg,
    onTertiaryContainer = BentoDeepGreenText,
    outline = BentoBorderColor
)

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
