// AGP 9 bundles Kotlin support; do NOT apply org.jetbrains.kotlin.android.
plugins {
    id("com.android.application") version "9.3.2" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
}
