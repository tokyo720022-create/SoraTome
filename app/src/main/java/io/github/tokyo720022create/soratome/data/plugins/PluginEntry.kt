package io.github.tokyo720022create.soratome.data.plugins

import io.github.tokyo720022create.soratome.plugin.api.manifest.PluginManifest
import io.github.tokyo720022create.soratome.plugin.api.source.SourcePlugin

/**
 * A plugin the app knows about.
 *
 * @property bundled True for plugins packaged inside the app (debug builds only).
 * @property origin Where an installed plugin came from, shown to the user.
 */
data class PluginEntry(
    val plugin: SourcePlugin,
    val bundled: Boolean,
    val enabled: Boolean,
    val origin: String?,
) {
    val manifest: PluginManifest get() = plugin.manifest
}
