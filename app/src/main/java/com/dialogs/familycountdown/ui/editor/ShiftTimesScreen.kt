package com.dialogs.familycountdown.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialogs.familycountdown.model.EventStore
import com.dialogs.familycountdown.model.SettingsStore
import com.dialogs.familycountdown.model.TimeShift
import java.time.ZoneId

/**
 * Bulk re-stamps every saved event's time by reinterpreting its wall-clock
 * reading from one zone to another. Only user events are changed; holidays regenerate.
 */
@Composable
fun ShiftTimesScreen(store: EventStore, settings: SettingsStore, state: EditorState) {
    val events by store.events.collectAsState()
    val fromZone = SettingsStore.zoneFor(state.shiftFrom)
    val toZone = SettingsStore.zoneFor(state.shiftTo)
    val isNoOp = state.shiftFrom == state.shiftTo
    val sample = events.minByOrNull { it.targetDate }

    Column(Modifier.fillMaxSize()) {
        SheetTopBar(title = "Shift Event Times", leading = { BarButton("Back") { state.pop() } })

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Section(footer = "Each event keeps its wall-clock reading but moves to that time in the “To” zone.") {
                SettingsRow(
                    title = "From (interpret current times as)",
                    value = SettingsStore.pretty(state.shiftFrom), chevron = true,
                    onClick = { state.push(Screen.ZonePicker(ZoneTarget.SHIFT_FROM)) },
                )
                RowDivider()
                SettingsRow(
                    title = "To (change them to)",
                    value = SettingsStore.pretty(state.shiftTo), chevron = true,
                    onClick = { state.push(Screen.ZonePicker(ZoneTarget.SHIFT_TO)) },
                )
            }

            if (sample != null) {
                Section(header = "Preview") {
                    SettingsRow(title = sample.label)
                    RowDivider()
                    SettingsRow(title = "Now", value = SettingsStore.mediumString(sample.targetDate, toZone))
                    RowDivider()
                    SettingsRow(
                        title = "After",
                        value = SettingsStore.mediumString(TimeShift.reinterpret(sample.targetDate, fromZone, toZone), toZone),
                    )
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Button(
                    onClick = {
                        store.transformAll { it.copy(targetDate = TimeShift.reinterpret(it.targetDate, fromZone, toZone)) }
                        state.shiftApplied = true
                        state.shiftFrom = state.shiftTo   // collapse to a no-op so it can't be applied twice by mistake
                    },
                    enabled = !isNoOp && events.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Shift ${events.size} Event${if (events.size == 1) "" else "s"}")
                }
                if (state.shiftApplied) {
                    Text(
                        "Done. Times updated.",
                        color = Color(0xFF2E7D32), fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
