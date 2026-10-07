package com.dialogs.familycountdown.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Generates US holidays (New Year's, Easter, Independence Day, Thanksgiving,
 * Christmas) for the current and next year, matching the web page. These are
 * transient — regenerated at runtime, never persisted or editable.
 */
object HolidayProvider {

    /** Holidays for `[thisYear, thisYear + 1]`, at local midnight in [zone]. */
    fun holidays(now: Instant, zone: ZoneId): List<CountdownEvent> {
        val thisYear = now.atZone(zone).year
        val result = ArrayList<CountdownEvent>()
        for (year in listOf(thisYear, thisYear + 1)) {
            result += event("New Year's Day", LocalDate.of(year, 1, 1), zone)
            result += event("Easter", easterDate(year), zone)
            result += event("Independence Day", LocalDate.of(year, 7, 4), zone)
            result += event("Thanksgiving", thanksgiving(year), zone)
            result += event("Christmas", LocalDate.of(year, 12, 25), zone)
        }
        return result
    }

    private fun event(label: String, day: LocalDate, zone: ZoneId) = CountdownEvent(
        label = label,
        targetDate = day.atStartOfDay(zone).toInstant(),
        pinned = false, repeats = false, isGenerated = true,
    )

    /** Fourth Thursday of November. */
    fun thanksgiving(year: Int): LocalDate =
        LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY))

    /** Western (Gregorian) Easter Sunday — Anonymous Gregorian "Computus" algorithm. */
    fun easterDate(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }
}
