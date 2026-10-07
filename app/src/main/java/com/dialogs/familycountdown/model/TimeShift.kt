package com.dialogs.familycountdown.model

import java.time.Instant
import java.time.ZoneId

/**
 * Reinterprets an instant's wall-clock reading from one time zone to another —
 * the calendar fields (Y/M/D/H/M/S) are read in [from] and re-stamped in [to],
 * so the displayed time stays but the absolute instant moves.
 */
object TimeShift {
    fun reinterpret(instant: Instant, from: ZoneId, to: ZoneId): Instant =
        instant.atZone(from).toLocalDateTime().atZone(to).toInstant()
}
