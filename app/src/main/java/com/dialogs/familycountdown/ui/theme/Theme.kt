package com.dialogs.familycountdown.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dialogs.familycountdown.R

/** JetBrains Mono ExtraBold (SIL OFL) — the split-flap glyph face. */
val JetBrainsMono: FontFamily = FontFamily(Font(R.font.jetbrains_mono_extrabold, FontWeight.ExtraBold))

private val EditorColors = lightColorScheme(
    primary = Color(0xFF1F6FEB),
    surface = Color(0xFFF2F2F7),        // iOS grouped-list background
    surfaceContainer = Color.White,
    background = Color(0xFFF2F2F7),
)

/** The board itself is always black; this theme styles the editor card. */
@Composable
fun FamilyCountdownTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EditorColors, content = content)
}
