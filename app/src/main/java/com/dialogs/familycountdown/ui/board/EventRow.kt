package com.dialogs.familycountdown.ui.board

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dialogs.familycountdown.model.DisplayEvent

/**
 * One board row: event name on the left, and on the right either the split-flap
 * countdown or a red ARRIVED once the event's day has come.
 */
@Composable
fun EventRow(event: DisplayEvent, metrics: FlipMetrics, rowHeight: Dp) {
    Row(
        Modifier.fillMaxWidth().height(rowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TileText(text = event.label, metrics = metrics)
        Spacer(Modifier.weight(1f).widthIn(min = 32.dp))
        if (event.arrived) {
            TileText(
                text = "ARRIVED", metrics = metrics,
                tileTop = FlipMetrics.arrivedTile, tileBottom = FlipMetrics.arrivedTile,
                glyph = Color.White,
            )
        } else {
            FlipClock(remaining = event.remaining, metrics = metrics)
        }
    }
}
