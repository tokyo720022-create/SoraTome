package io.github.tokyo720022create.soratome.data.plugins

import io.github.tokyo720022create.soratome.plugin.runtime.repository.RepositoryPlugin

/** A newer version of an installed plugin, found in one of the user's repositories. */
data class AvailableUpdate(
    val installed: PluginEntry,
    val repositoryUrl: String,
    val candidate: RepositoryPlugin,
)
