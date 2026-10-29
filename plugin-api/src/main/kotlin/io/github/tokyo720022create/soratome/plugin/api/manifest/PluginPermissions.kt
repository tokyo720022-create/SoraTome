package io.github.tokyo720022create.soratome.plugin.api.manifest

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
