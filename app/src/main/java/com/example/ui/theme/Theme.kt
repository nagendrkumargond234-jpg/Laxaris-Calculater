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

private val DarkColorScheme =
  darkColorScheme(
    primary = SoftCopper,
    secondary = DimChocolate,
    tertiary = SoftCream,
    background = EspressoBackground,
    surface = EspressoSurface,
    onPrimary = EspressoBackground,
    onSecondary = EspressoTextPrimary,
    onBackground = EspressoTextPrimary,
    onSurface = EspressoTextPrimary
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LuxuryTeracotta,
    secondary = WarmSand,
    tertiary = DeepEspresso,
    background = CreamBackground,
    surface = CreamSurface,
    onPrimary = CreamBackground,
    onSecondary = CreamTextPrimary,
    onBackground = CreamTextPrimary,
    onSurface = CreamTextPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to enforce our premium luxury aesthetic consistently
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) {
    DarkColorScheme
  } else {
    LightColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
