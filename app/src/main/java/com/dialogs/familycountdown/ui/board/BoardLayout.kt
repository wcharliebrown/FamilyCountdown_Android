package com.dialogs.familycountdown.ui.board

import kotlin.math.max
import kotlin.math.min

/**
 * Derives one uniform font size, row height, and how many rows fit for the
 * current screen (all in pixels). The font size is the largest that still lets
 * the longest event name and the full DDDD·HH·MM·SS clock share a single line —
 * so every row (names and digits alike) renders at exactly the same size.
 */
class BoardLayout(widthPx: Float, heightPx: Float, longestLabel: Int) {
    val hPadding = 40f
    val topInset = 44f            // reserved strip so the gear never overlaps the header
    val metrics: FlipMetrics
    val headerHeight: Float
    val rowHeight: Float
    val maxRows: Int

    init {
        // Fixed horizontal chrome: left/right padding + name↔clock gaps.
        val chrome = hPadding * 2 + 60f
        val nameUnits = max(1, longestLabel) * FlipMetrics.ADVANCE_RATIO
        val widthFit = (widthPx - chrome) / (nameUnits + FlipMetrics.clockWidthUnits)
        val heightCap = min(58f, heightPx * 0.05f)
        val fontSize = max(16f, min(heightCap, widthFit))

        metrics = FlipMetrics(fontSize)
        headerHeight = fontSize * 0.24f + 10f       // label + gap under the header
        val vPadding = fontSize * 0.34f
        rowHeight = metrics.height + vPadding * 2
        maxRows = max(1, ((heightPx - topInset - headerHeight) / (rowHeight + 1f)).toInt())
    }
}
