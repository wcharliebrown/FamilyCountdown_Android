package com.dialogs.familycountdown.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dialogs.familycountdown.model.SettingsStore
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs

/** Region/City zone ids, like iOS `knownTimeZoneIdentifiers` (drops the Etc/ and SystemV/ aliases). */
val knownZoneIds: List<String> by lazy {
    (ZoneId.getAvailableZoneIds()
        .filter { '/' in it && !it.startsWith("Etc/") && !it.startsWith("SystemV/") } + "UTC")
        .sorted()
}

fun offsetLabel(id: String): String {
    val zone = runCatching { ZoneId.of(id) }.getOrNull() ?: return ""
    val secs = zone.rules.getOffset(Instant.now()).totalSeconds
    val sign = if (secs < 0) "-" else "+"
    return String.format(Locale.US, "GMT%s%02d:%02d", sign, abs(secs) / 3600, (abs(secs) % 3600) / 60)
}

/**
 * Searchable time-zone chooser. Writes to the display setting or to the
 * Shift screen's From/To fields depending on [target]; only the display
 * setting offers "Automatic".
 */
@Composable
fun TimeZonePickerScreen(settings: SettingsStore, state: EditorState, target: ZoneTarget) {
    var search by remember { mutableStateOf("") }
    val selected = when (target) {
        ZoneTarget.DISPLAY -> settings.timeZoneIdentifier.value
        ZoneTarget.SHIFT_FROM -> state.shiftFrom
        ZoneTarget.SHIFT_TO -> state.shiftTo
    }
    val title = when (target) {
        ZoneTarget.DISPLAY -> "Time Zone"
        ZoneTarget.SHIFT_FROM -> "From (interpret current times as)"
        ZoneTarget.SHIFT_TO -> "To (change them to)"
    }
    val ids = remember(search) {
        if (search.isBlank()) knownZoneIds
        else knownZoneIds.filter { it.replace('_', ' ').contains(search.trim(), ignoreCase = true) }
    }

    fun select(id: String) {
        when (target) {
            ZoneTarget.DISPLAY -> settings.setTimeZoneIdentifier(id)
            ZoneTarget.SHIFT_FROM -> state.shiftFrom = id
            ZoneTarget.SHIFT_TO -> state.shiftTo = id
        }
        state.pop()
    }

    Column(Modifier.fillMaxSize()) {
        SheetTopBar(title = title, leading = { BarButton("Back") { state.pop() } })
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search time zones") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        )
        LazyColumn(Modifier.fillMaxSize()) {
            if (target == ZoneTarget.DISPLAY && search.isBlank()) {
                item {
                    Section {
                        ZoneRow(
                            title = "Automatic",
                            subtitle = "Follows this device (${SettingsStore.pretty(ZoneId.systemDefault().id)})",
                            checked = selected.isEmpty(),
                            onClick = { select("") },
                        )
                    }
                }
            }
            item {
                Section {
                    Column {
                        ids.forEachIndexed { idx, id ->
                            ZoneRow(
                                title = SettingsStore.pretty(id),
                                subtitle = offsetLabel(id),
                                checked = selected == id,
                                onClick = { select(id) },
                            )
                            if (idx < ids.lastIndex) HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneRow(title: String, subtitle: String, checked: Boolean, onClick: () -> Unit) {
    SettingsRow(title = title, subtitle = subtitle, onClick = onClick, trailing = {
        if (checked) Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
    })
}
