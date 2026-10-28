package io.github.tokyo720022create.tsuzuki.plugin.api

/** Version of the plugin API implemented by this build of Tsuzuki. */
const val PLUGIN_API_VERSION = 1

enum class PluginType {
    SOURCE,
    TRACKER,
    TRANSLATOR,
    METADATA,
    FILTER,
}

/**
 * Permissions a plugin declares. The user approves these at install time.
 *
 * @property network Domains the plugin may contact. Everything else is blocked.
 * @property storage Whether the plugin may keep its own settings.
 */
data class PluginPermissions(
    val network: List<String> = emptyList(),
    val storage: Boolean = false,
)

/** Parsed `manifest.json` of a plugin package. */
data class PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val apiVersion: Int,
    val type: PluginType,
    val entry: String,
    val language: String? = null,
    val permissions: PluginPermissions = PluginPermissions(),
)
