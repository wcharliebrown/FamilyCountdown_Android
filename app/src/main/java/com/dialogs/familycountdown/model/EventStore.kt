package com.dialogs.familycountdown.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Loads, persists, and mutates the user's event list as a local JSON file
 * (`files/FamilyCountdownEvents.json`), seeded on first launch from the bundled
 * `assets/SeedEvents.json`. On-device only — no server/sync.
 *
 * [seedJson] is called only when the file is missing or unreadable.
 */
class EventStore(private val file: File, private val seedJson: () -> String?) {

    private val _events = MutableStateFlow<List<CountdownEvent>>(emptyList())
    val events: StateFlow<List<CountdownEvent>> = _events

    init { load() }

    // MARK: - Loading / seeding

    private fun load() {
        if (file.exists()) {
            val decoded = runCatching { CountdownEvent.listFromJson(file.readText()) }.getOrNull()
            if (decoded != null) {
                _events.value = decoded
                return
            }
        }
        // First launch (or unreadable): seed from the bundled JSON.
        _events.value = seedJson()
            ?.let { runCatching { CountdownEvent.listFromJson(it) }.getOrNull() }
            ?: emptyList()
        save()
    }

    // MARK: - Persistence

    @Synchronized
    fun save() {
        runCatching {
            file.parentFile?.mkdirs()
            val tmp = File(file.parentFile, file.name + ".tmp")
            tmp.writeText(CountdownEvent.listToJson(_events.value))
            if (!tmp.renameTo(file)) {           // rename can fail across some filesystems; fall back to copy
                file.writeText(tmp.readText())
                tmp.delete()
            }
        }
    }

    // MARK: - CRUD

    fun add(event: CountdownEvent) {
        _events.value = _events.value + event
        save()
    }

    fun update(event: CountdownEvent) {
        val idx = _events.value.indexOfFirst { it.id == event.id }
        if (idx < 0) return
        _events.value = _events.value.toMutableList().also { it[idx] = event }
        save()
    }

    fun delete(event: CountdownEvent) {
        _events.value = _events.value.filterNot { it.id == event.id }
        save()
    }

    /** Apply a transform to every event and persist once. */
    fun transformAll(transform: (CountdownEvent) -> CountdownEvent) {
        _events.value = _events.value.map(transform)
        save()
    }

    companion object {
        const val FILE_NAME = "FamilyCountdownEvents.json"
        const val SEED_ASSET = "SeedEvents.json"
    }
}
