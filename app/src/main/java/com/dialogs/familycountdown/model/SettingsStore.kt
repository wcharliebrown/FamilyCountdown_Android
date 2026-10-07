package com.dialogs.familycountdown.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * User preferences. Currently the display time zone used for day boundaries
 * (ARRIVED / roll-forward), holiday midnights, and the editor's date fields.
 * Empty identifier == Automatic (follows the device).
 */
class SettingsStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _timeZoneIdentifier = MutableStateFlow(prefs.getString(KEY_TZ, "") ?: "")
    val timeZoneIdentifier: StateFlow<String> = _timeZoneIdentifier

    fun setTimeZoneIdentifier(id: String) {
        _timeZoneIdentifier.value = id
        prefs.edit().putString(KEY_TZ, id).apply()
    }

    val isAutomatic: Boolean get() = _timeZoneIdentifier.value.isEmpty()

    /** Resolved zone; read freshly each time so a system zone change is picked up. */
    val zone: ZoneId get() = zoneFor(_timeZoneIdentifier.value)

    /** Human-readable label for the current selection. */
    val timeZoneLabel: String get() = if (isAutomatic) "Automatic" else pretty(zone.id)

    companion object {
        private const val KEY_TZ = "timeZoneIdentifier"

        fun zoneFor(identifier: String): ZoneId {
            if (identifier.isEmpty()) return ZoneId.systemDefault()
            return runCatching { ZoneId.of(identifier) }.getOrDefault(ZoneId.systemDefault())
        }

        fun pretty(identifier: String): String = identifier.replace('_', ' ')

        private val medium: DateTimeFormatter =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

        /** Medium date + short time, rendered in [zone]. */
        fun mediumString(instant: Instant, zone: ZoneId): String = medium.format(instant.atZone(zone))
    }
}
