package io.github.tokyo720022create.soratome.plugin.runtime.install

import io.github.tokyo720022create.soratome.plugin.api.manifest.PluginManifest

/** A checked plugin package, ready to be installed. */
class PluginPackage(
    val manifest: PluginManifest,
    val manifestJson: String,
    val script: String,
    val sizeBytes: Int,
)
