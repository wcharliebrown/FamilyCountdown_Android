package com.dialogs.familycountdown.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dialogs.familycountdown.model.EventStore
import com.dialogs.familycountdown.model.SettingsStore

/**
 * The editor "sheet": a centred light card over the dimmed board, drawn inside
 * the activity window (not a Dialog) so the kiosk stays immersive.
 */
@Composable
fun EditorOverlay(store: EventStore, settings: SettingsStore, onDismiss: () -> Unit) {
    val state = remember { EditorState(initialShiftTo = settings.zone.id) }

    BackHandler { if (!state.pop()) onDismiss() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            // Tapping the scrim closes the editor (like dragging down the iOS sheet).
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .widthIn(max = 900.dp)
                .fillMaxHeight(0.9f)
                .imePadding()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 16.dp,
        ) {
            when (val screen = state.current) {
                Screen.List -> EventListScreen(store, settings, state, onDone = onDismiss)
                Screen.Editor -> EventEditorScreen(store, settings, state)
                Screen.PickDate -> PickDateScreen(settings, state)
                Screen.PickTime -> PickTimeScreen(settings, state)
                is Screen.ZonePicker -> TimeZonePickerScreen(settings, state, screen.target)
                Screen.Shift -> ShiftTimesScreen(store, settings, state)
            }
        }
    }
}
