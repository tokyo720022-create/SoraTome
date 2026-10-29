package io.github.tokyo720022create.soratome.plugin.api.manifest

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
