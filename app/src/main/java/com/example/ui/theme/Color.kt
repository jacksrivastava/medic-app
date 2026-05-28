package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Stitch CareFlow Design System Theme Palette - Light
val StitchPrimary = Color(0xFF005DA7)
val StitchPrimaryContainer = Color(0xFF2976C7)
val StitchOnPrimary = Color(0xFFFFFFFF)
val StitchOnPrimaryContainer = Color(0xFFFDFCFF)

val StitchSecondary = Color(0xFF006781)
val StitchSecondaryContainer = Color(0xFF5AD4FE)
val StitchOnSecondary = Color(0xFFFFFFFF)
val StitchOnSecondaryContainer = Color(0xFF005A71)

val StitchTertiary = Color(0xFF595C5E)
val StitchTertiaryContainer = Color(0xFF727577)
val StitchOnTertiary = Color(0xFFFFFFFF)
val StitchOnTertiaryContainer = Color(0xFFFBFDFF)

val StitchBackground = Color(0xFFF8F9FF)
val StitchOnBackground = Color(0xFF0B1C30)

val StitchSurface = Color(0xFFFFFFFF)
val StitchOnSurface = Color(0xFF0B1C30)
val StitchOnSurfaceVariant = Color(0xFF414751)

val StitchOutline = Color(0xFF717783)
val StitchOutlineVariant = Color(0xFFC1C7D3)

// Tonal Surface Containers
val StitchSurfaceContainerLowest = Color(0xFFFFFFFF)
val StitchSurfaceContainerLow = Color(0xFFEFF4FF)
val StitchSurfaceContainer = Color(0xFFE5EEFF)
val StitchSurfaceContainerHigh = Color(0xFFDCE9FF)
val StitchSurfaceContainerHighest = Color(0xFFD3E4FE)
val StitchSurfaceDim = Color(0xFFCBDBF5)
val StitchSurfaceBright = Color(0xFFF8F9FF)

// Stitch CareFlow Design System Theme Palette - Dark
val StitchPrimaryDark = Color(0xFFADC6FF)
val StitchPrimaryContainerDark = Color(0xFF1B2C56)
val StitchOnPrimaryDark = Color(0xFF002F66)
val StitchOnPrimaryContainerDark = Color(0xFFD4E3FF)

val StitchSecondaryDark = Color(0xFF5AD4FE)
val StitchSecondaryContainerDark = Color(0xFF004D62)
val StitchOnSecondaryDark = Color(0xFF003544)
val StitchOnSecondaryContainerDark = Color(0xFFB9EAFF)

val StitchBackgroundDark = Color(0xFF0B1C30)
val StitchOnBackgroundDark = Color(0xFFE5EEFF)

val StitchSurfaceDark = Color(0xFF122336)
val StitchOnSurfaceDark = Color(0xFFE5EEFF)
val StitchOnSurfaceVariantDark = Color(0xFFC1C7D3)

val StitchOutlineDark = Color(0xFF8B919E)
val StitchOutlineVariantDark = Color(0xFF414751)

// Fallback compatible values for original app colors to prevent compilation errors
val BentoBgLight = StitchBackground
val BentoBackboneLight = StitchSurfaceContainer
val BentoTextDark = StitchOnBackground
val BentoMutedText = StitchOnSurfaceVariant

val BentoBlueAccent = StitchPrimary
val BentoLightBlueBg = StitchSurfaceContainerLow
val BentoDeepBlueText = StitchPrimary

val BentoSalmonBg = Color(0xFFFFDAD6)
val BentoDeepRedText = Color(0xFF93000A)

val BentoGreenBg = Color(0xFFE2F1E3)
val BentoDeepGreenText = Color(0xFF00210B)

val BentoBorderColor = StitchOutlineVariant

// Dark Fallbacks
val BentoBgDark = StitchBackgroundDark
val BentoBackboneDark = StitchSurfaceDark
val BentoTextLight = StitchOnBackgroundDark
val BentoMutedTextDark = StitchOnSurfaceVariantDark

val BentoBlueAccentDark = StitchPrimaryDark
val BentoLightBlueBgDark = StitchPrimaryContainerDark
val BentoDeepBlueTextDark = StitchOnPrimaryContainerDark

val BentoSalmonBgDark = Color(0xFF5C1E1D)
val BentoDeepRedTextDark = Color(0xFFFFDAD6)

val BentoGreenBgDark = Color(0xFF1B301B)
val BentoDeepGreenTextDark = Color(0xFFE2F1E3)

val MedTealPrimary = StitchPrimary
val MedCyanSecondary = BentoSalmonBg
val MedBlueTertiary = StitchPrimary
val MedBackgroundLight = StitchBackground
val MedSurfaceLight = StitchSurface

val MedTealDarkPrimary = StitchPrimaryDark
val MedCyanDarkSecondary = BentoSalmonBgDark
val MedBlueDarkTertiary = StitchPrimaryDark
val MedBackgroundDark = StitchBackgroundDark
val MedSurfaceDark = StitchSurfaceDark
