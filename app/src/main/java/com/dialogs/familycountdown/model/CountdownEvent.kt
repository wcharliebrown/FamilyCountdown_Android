package com.dialogs.familycountdown.model

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

/**
 * A single countdown entry. Mirrors the web page's / iOS app's JSON shape
 * (`label`, `targetDate`, `pinned`, `repeats`) so files round-trip 1:1.
 * [id] is local-only and never written to disk.
 */
data class CountdownEvent(
    val id: UUID = UUID.randomUUID(),
    val label: String,
    val targetDate: Instant,
    val pinned: Boolean = false,
    val repeats: Boolean = false,
    /** Marks holidays generated at runtime (not user-owned, not persisted/editable). */
    val isGenerated: Boolean = false,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("label", label)
        .put("targetDate", Iso8601.format(targetDate))
        .put("pinned", pinned)
        .put("repeats", repeats)

    companion object {
        /** Throws if `label` or `targetDate` is missing/unparseable. */
        fun fromJson(o: JSONObject): CountdownEvent = CountdownEvent(
            label = o.getString("label"),
            targetDate = Iso8601.parse(o.getString("targetDate"))
                ?: throw IllegalArgumentException("Unrecognized ISO-8601 date: ${o.getString("targetDate")}"),
            pinned = o.optBoolean("pinned", false),
            repeats = o.optBoolean("repeats", false),
        )

        fun listFromJson(text: String): List<CountdownEvent> {
            val arr = JSONArray(text)
            return (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        }

        fun listToJson(events: List<CountdownEvent>): String {
            val arr = JSONArray()
            events.forEach { arr.put(it.toJson()) }
            return arr.toString(4)
        }
    }
}
