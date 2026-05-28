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

private val AshesColorScheme = darkColorScheme(
  primary = PhoenixOrange,
  secondary = AshGrey,
  tertiary = GoldAccent,
  background = CarbonBlack,
  surface = Charcoal,
  onPrimary = CarbonBlack,
  onSecondary = BoneWhite,
  onTertiary = CarbonBlack,
  onBackground = BoneWhite,
  onSurface = BoneWhite
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for ASHES digital ritual
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve exact brand aesthetics
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = AshesColorScheme,
    typography = Typography,
    content = content
  )
}
