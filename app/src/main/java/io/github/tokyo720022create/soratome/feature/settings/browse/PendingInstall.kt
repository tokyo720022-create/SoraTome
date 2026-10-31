package io.github.tokyo720022create.soratome.feature.settings.browse

import io.github.tokyo720022create.soratome.plugin.runtime.install.PluginPackage

/** A plugin package waiting for the user to confirm the install. */
data class PendingInstall(
    val pkg: PluginPackage,
    val origin: String,
)
