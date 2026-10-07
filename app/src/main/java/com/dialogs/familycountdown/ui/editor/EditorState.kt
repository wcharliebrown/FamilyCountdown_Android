package com.dialogs.familycountdown.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dialogs.familycountdown.model.CountdownEvent
import java.time.ZoneId

/** Which zone a TimeZonePicker writes back to. */
enum class ZoneTarget { DISPLAY, SHIFT_FROM, SHIFT_TO }

/** The editor's screens; navigation is a simple in-memory back stack. */
sealed interface Screen {
    data object List : Screen
    data object Editor : Screen          // edits EditorState.draft
    data object PickDate : Screen        // sub-screen of Editor
    data object PickTime : Screen        // sub-screen of Editor
    data class ZonePicker(val target: ZoneTarget) : Screen
    data object Shift : Screen
}

/** All editor UI state, hoisted so sub-screens (date/time/zone pickers) can share it. */
class EditorState(initialShiftTo: String) {
    val stack = mutableStateListOf<Screen>(Screen.List)
    val current: Screen get() = stack.last()

    fun push(screen: Screen) { stack.add(screen) }
    /** @return false when already at the root (caller should dismiss the editor). */
    fun pop(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    // Event being added/edited.
    var draft: CountdownEvent? by mutableStateOf(null)
    var draftIsNew: Boolean by mutableStateOf(false)

    // Shift Event Times.
    var shiftFrom: String by mutableStateOf(ZoneId.systemDefault().id)
    var shiftTo: String by mutableStateOf(initialShiftTo)
    var shiftApplied: Boolean by mutableStateOf(false)
}
