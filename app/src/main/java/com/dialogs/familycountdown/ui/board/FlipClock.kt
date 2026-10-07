package com.dialogs.familycountdown.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.dialogs.familycountdown.model.TimeRemaining

/**
 * A zero-padded group of flip digits (e.g. 4 for days, 2 for h/m/s) with a
 * right-to-left 50ms stagger so the rightmost digit leads the cascade.
 */
@Composable
fun FlipGroup(value: Int, digits: Int, metrics: FlipMetrics) {
    val chars = value.coerceAtLeast(0).toString().padStart(digits, '0').takeLast(digits)
    val gap = with(LocalDensity.current) { metrics.intraSpacing.toDp() }
    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
        chars.forEachIndexed { idx, ch ->
            FlipDigit(value = ch, metrics = metrics, delayMs = (chars.length - 1 - idx) * FlipMetrics.STAGGER_MS)
        }
    }
}

/**
 * Renders a string as a row of resting flap tiles — used for event names and
 * the ARRIVED word so they match the split-flap digits.
 */
@Composable
fun TileText(
    text: String,
    metrics: FlipMetrics,
    tileTop: Color = FlipMetrics.cardTop,
    tileBottom: Color = FlipMetrics.cardBottom,
    glyph: Color = FlipMetrics.glyphColor,
) {
    val gap = with(LocalDensity.current) { metrics.intraSpacing.toDp() }
    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
        text.forEach { ch ->
            TileFace(char = ch, metrics = metrics, tileTop = tileTop, tileBottom = tileBottom, glyph = glyph)
        }
    }
}

/**
 * Column labels (DAYS / HOURS / MINUTES / SECONDS) sized and spaced to sit
 * directly over each digit group of a FlipClock.
 */
@Composable
fun ClockHeader(metrics: FlipMetrics) {
    val density = LocalDensity.current
    val gap = with(density) { metrics.groupSpacing.toDp() }
    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
        HeaderLabel("DAYS", 4, metrics)
        HeaderLabel("HOURS", 2, metrics)
        HeaderLabel("MINUTES", 2, metrics)
        HeaderLabel("SECONDS", 2, metrics)
    }
}

@Composable
private fun HeaderLabel(text: String, digits: Int, metrics: FlipMetrics) {
    val density = LocalDensity.current
    Box(Modifier.width(with(density) { metrics.groupWidth(digits).toDp() }), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = FlipMetrics.headerText,
            fontSize = with(density) { (metrics.fontSize * 0.24f).toSp() },
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            textAlign = TextAlign.Center,
        )
    }
}

/** The full DDDD HH MM SS split-flap readout — spacing between groups, exactly like the web board. */
@Composable
fun FlipClock(remaining: TimeRemaining, metrics: FlipMetrics) {
    val gap = with(LocalDensity.current) { metrics.groupSpacing.toDp() }
    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
        FlipGroup(remaining.days, 4, metrics)
        FlipGroup(remaining.hours, 2, metrics)
        FlipGroup(remaining.minutes, 2, metrics)
        FlipGroup(remaining.seconds, 2, metrics)
    }
}
