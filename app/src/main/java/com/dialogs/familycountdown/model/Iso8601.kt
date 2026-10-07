package com.dialogs.familycountdown.model

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * ISO-8601 with explicit timezone offset (e.g. `2026-11-20T00:00:00-05:00`),
 * matching the web page's `targetDate` format. Writes in the device's current
 * zone so exported files look like the originals.
 */
object Iso8601 {
    private val writer: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)

    /** Accepts `...Z`, `...±HH:MM`, with or without fractional seconds. */
    fun parse(text: String): Instant? = try {
        OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
    } catch (e: DateTimeParseException) {
        null
    }

    fun format(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        writer.format(instant.atZone(zone))
}
