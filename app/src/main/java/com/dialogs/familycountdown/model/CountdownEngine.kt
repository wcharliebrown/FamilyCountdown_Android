package com.dialogs.familycountdown.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

/** Days/hours/minutes/seconds remaining until a target instant. */
data class TimeRemaining(val days: Int, val hours: Int, val minutes: Int, val seconds: Int) {
    val isZero: Boolean get() = days == 0 && hours == 0 && minutes == 0 && seconds == 0

    companion object {
        /**
         * Non-negative breakdown of `target - now`, clamped to zero once passed.
         * Days are capped at 9999 (4 flip digits).
         */
        fun between(now: Instant, target: Instant): TimeRemaining {
            val total = maxOf(0L, target.epochSecond - now.epochSecond)
            val days = minOf(9999L, total / 86_400).toInt()
            val rem = (total % 86_400).toInt()
            return TimeRemaining(days, rem / 3_600, (rem % 3_600) / 60, rem % 60)
        }
    }
}

/**
 * A row ready for display: the resolved event, its (possibly rolled-forward)
 * occurrence, and whether it has "arrived".
 */
data class DisplayEvent(
    val id: UUID,
    val label: String,
    val occurrence: Instant,
    val arrived: Boolean,
    val pinned: Boolean,
    val remaining: TimeRemaining,
)

/**
 * Pure logic for turning the raw event list (+ generated holidays) into the
 * sorted, rolled-forward, filtered board shown on screen.
 */
object CountdownEngine {

    /**
     * Build the display list for [now]:
     * - repeating events roll their year forward to the next occurrence that is today or later;
     * - an event shows ARRIVED for the whole calendar day it lands on (once passed);
     * - the day after, non-repeating events drop off (repeating ones have already rolled);
     * - sort: pinned first (soonest on top), then the rest (soonest on top).
     */
    fun board(events: List<CountdownEvent>, now: Instant, zone: ZoneId): List<DisplayEvent> {
        val today = now.atZone(zone).toLocalDate()

        val resolved = events.mapNotNull { ev ->
            val occ = occurrence(ev, today, zone)
            val occDay = occ.atZone(zone).toLocalDate()
            if (occDay < today) return@mapNotNull null   // fully past its day
            val arrived = !now.isBefore(occ) && occDay == today
            DisplayEvent(
                id = ev.id, label = ev.label, occurrence = occ,
                arrived = arrived, pinned = ev.pinned,
                remaining = TimeRemaining.between(now, occ),
            )
        }

        return resolved.sortedWith(compareByDescending<DisplayEvent> { it.pinned }.thenBy { it.occurrence })
    }

    /**
     * The occurrence to display. For repeating events, advance the year until the
     * date is today or later (keeps ARRIVED visible on the day itself). Non-repeating
     * events return their stored date unchanged.
     */
    fun occurrence(event: CountdownEvent, today: LocalDate, zone: ZoneId): Instant {
        if (!event.repeats) return event.targetDate
        var date: ZonedDateTime = event.targetDate.atZone(zone)
        var guard = 0
        while (date.toLocalDate() < today && guard < 200) {
            date = date.plusYears(1)
            guard++
        }
        return date.toInstant()
    }
}
