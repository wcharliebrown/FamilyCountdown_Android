package com.dialogs.familycountdown

import android.app.Application
import com.dialogs.familycountdown.model.EventStore
import com.dialogs.familycountdown.model.SettingsStore
import java.io.File

/** Process-wide owner of the two stores (the app has a single screen; no DI needed). */
class FamilyCountdownApp : Application() {
    lateinit var eventStore: EventStore
        private set
    lateinit var settingsStore: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        settingsStore = SettingsStore(this)
        eventStore = EventStore(File(filesDir, EventStore.FILE_NAME)) {
            runCatching { assets.open(EventStore.SEED_ASSET).bufferedReader().use { it.readText() } }.getOrNull()
        }
    }
}
