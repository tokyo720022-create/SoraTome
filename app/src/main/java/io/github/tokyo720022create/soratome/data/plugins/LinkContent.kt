package io.github.tokyo720022create.soratome.data.plugins

import io.github.tokyo720022create.soratome.plugin.runtime.install.PluginPackage
import io.github.tokyo720022create.soratome.plugin.runtime.repository.RepositoryIndex

/** What a pasted link turned out to be. */
sealed interface LinkContent {
    /** A plugin package, ready to install. */
    class Package(val pkg: PluginPackage) : LinkContent

    /** A plugin store index, not a plugin. */
    class Store(val url: String, val index: RepositoryIndex) : LinkContent
}
