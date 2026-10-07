package com.dialogs.familycountdown

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dialogs.familycountdown.model.EventStore
import com.dialogs.familycountdown.model.SettingsStore
import com.dialogs.familycountdown.ui.board.BoardScreen
import com.dialogs.familycountdown.ui.editor.EditorOverlay
import com.dialogs.familycountdown.ui.theme.FamilyCountdownTheme

/** Station-board kiosk: landscape, immersive, never sleeps. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()

        val app = application as FamilyCountdownApp
        setContent {
            FamilyCountdownTheme {
                Root(app.eventStore, app.settingsStore)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@Composable
private fun Root(store: EventStore, settings: SettingsStore) {
    var showEditor by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        BoardScreen(store = store, settings = settings, onOpenEditor = { showEditor = true })
        if (showEditor) {
            EditorOverlay(store = store, settings = settings, onDismiss = { showEditor = false })
        }
    }
}
