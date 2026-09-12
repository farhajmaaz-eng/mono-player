package com.monoplayer.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.monoplayer.app.domain.MonoTheme

private val Black = Color(0xFF000000); private val White = Color(0xFFFFFFFF); private val Gray = Color(0xFF8C8C8C)
private val dark = darkColorScheme(primary=White, onPrimary=Black, background=Black, onBackground=White, surface=Black, onSurface=White, surfaceVariant=Color(0xFF171717), onSurfaceVariant=Color(0xFFB5B5B5), outline=Color(0xFF666666))
private val light = lightColorScheme(primary=Black, onPrimary=White, background=White, onBackground=Black, surface=White, onSurface=Black, surfaceVariant=Color(0xFFF0F0F0), onSurfaceVariant=Color(0xFF4A4A4A), outline=Gray)
@Composable fun MonoTheme(mode: MonoTheme, content: @Composable () -> Unit) { MaterialTheme(colorScheme = when (mode) { MonoTheme.LIGHT -> light; MonoTheme.BLACK -> dark; MonoTheme.SYSTEM -> if (androidx.compose.foundation.isSystemInDarkTheme()) dark else light }, typography = Typography(), content = content) }
