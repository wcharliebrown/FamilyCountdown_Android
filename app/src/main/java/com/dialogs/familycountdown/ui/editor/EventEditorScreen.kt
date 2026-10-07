package com.dialogs.familycountdown.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialogs.familycountdown.model.CountdownEvent
import com.dialogs.familycountdown.model.EventStore
import com.dialogs.familycountdown.model.SettingsStore
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

/** Add/edit form for a single event ([EditorState.draft]). */
@Composable
fun EventEditorScreen(store: EventStore, settings: SettingsStore, state: EditorState) {
    val draft = state.draft ?: run { state.pop(); return }
    val zone = settings.zone
    val local = draft.targetDate.atZone(zone)
    val canSave = draft.label.isNotBlank()
    var confirmDelete by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        SheetTopBar(
            title = if (state.draftIsNew) "New Event" else "Edit Event",
            leading = { BarButton("Cancel") { state.pop() } },
            trailing = {
                BarButton("Save", enabled = canSave, bold = true) {
                    val saved = draft.copy(label = draft.label.trim())
                    if (state.draftIsNew) store.add(saved) else store.update(saved)
                    state.pop()
                }
            },
        )

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Section(header = "Event") {
                TextField(
                    value = draft.label,
                    onValueChange = { state.draft = draft.copy(label = it) },
                    placeholder = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                )
                RowDivider()
                SettingsRow(title = "Date", value = dateFmt.format(local), chevron = true, onClick = { state.push(Screen.PickDate) })
                RowDivider()
                SettingsRow(title = "Time", value = timeFmt.format(local), chevron = true, onClick = { state.push(Screen.PickTime) })
            }

            Section(
                footer = "Repeating events (birthdays) roll to next year the day after they arrive. " +
                    "Pinned events are always kept on the board, anchored at the top.",
            ) {
                SettingsRow(title = "Repeats every year", trailing = {
                    Switch(checked = draft.repeats, onCheckedChange = { state.draft = draft.copy(repeats = it) })
                })
                RowDivider()
                SettingsRow(title = "Pinned (shown at top)", trailing = {
                    Switch(checked = draft.pinned, onCheckedChange = { state.draft = draft.copy(pinned = it) })
                })
            }

            if (!state.draftIsNew) {
                if (confirmDelete) {
                    InlineConfirm(
                        message = "Delete this event?",
                        confirmLabel = "Delete",
                        onConfirm = { store.delete(draft); state.pop() },
                        onCancel = { confirmDelete = false },
                    )
                } else {
                    Section {
                        SettingsRow(title = "Delete Event", onClick = { confirmDelete = true }, trailing = {})
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Replace the calendar date of [instant], keeping its wall-clock time in [zone]. */
private fun withDate(instant: Instant, date: LocalDate, zone: java.time.ZoneId): Instant =
    LocalDateTime.of(date, instant.atZone(zone).toLocalTime()).atZone(zone).toInstant()

private fun withTime(instant: Instant, time: LocalTime, zone: java.time.ZoneId): Instant =
    LocalDateTime.of(instant.atZone(zone).toLocalDate(), time).atZone(zone).toInstant()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickDateScreen(settings: SettingsStore, state: EditorState) {
    val draft = state.draft ?: run { state.pop(); return }
    val zone = settings.zone
    val current = draft.targetDate.atZone(zone).toLocalDate()
    // Material's DatePicker works in UTC-midnight epoch millis.
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = current.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )

    Column(Modifier.fillMaxSize()) {
        SheetTopBar(
            title = "Date",
            leading = { BarButton("Cancel") { state.pop() } },
            trailing = {
                BarButton("OK", bold = true, enabled = pickerState.selectedDateMillis != null) {
                    val ms = pickerState.selectedDateMillis ?: return@BarButton
                    val date = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                    state.draft = draft.copy(targetDate = withDate(draft.targetDate, date, zone))
                    state.pop()
                }
            },
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
            DatePicker(state = pickerState, showModeToggle = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickTimeScreen(settings: SettingsStore, state: EditorState) {
    val draft = state.draft ?: run { state.pop(); return }
    val zone = settings.zone
    val current = draft.targetDate.atZone(zone).toLocalTime()
    val pickerState = rememberTimePickerState(initialHour = current.hour, initialMinute = current.minute, is24Hour = false)

    Column(Modifier.fillMaxSize()) {
        SheetTopBar(
            title = "Time",
            leading = { BarButton("Cancel") { state.pop() } },
            trailing = {
                BarButton("OK", bold = true) {
                    val time = LocalTime.of(pickerState.hour, pickerState.minute)
                    state.draft = draft.copy(targetDate = withTime(draft.targetDate, time, zone))
                    state.pop()
                }
            },
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TimePicker(state = pickerState)
            Spacer(Modifier.height(8.dp))
            Text(
                "Times are in ${SettingsStore.pretty(zone.id)}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
