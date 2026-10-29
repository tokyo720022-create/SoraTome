package io.github.tokyo720022create.soratome.plugin.runtime

import io.github.tokyo720022create.soratome.plugin.api.PLUGIN_API_VERSION
import io.github.tokyo720022create.soratome.plugin.api.PluginManifest
import io.github.tokyo720022create.soratome.plugin.api.PluginPermissions
import io.github.tokyo720022create.soratome.plugin.api.PluginType
import org.json.JSONException
import org.json.JSONObject

object ManifestParser {

    fun parse(json: String): PluginManifest {
        try {
            val root = JSONObject(json)
            val permissions = root.optJSONObject("permissions")
            val network = permissions?.optJSONArray("network")?.let { array ->
                List(array.length()) { index -> array.getString(index) }
            } ?: emptyList()

            val manifest = PluginManifest(
                id = root.getString("id"),
                name = root.getString("name"),
                version = root.getString("version"),
                apiVersion = root.getInt("apiVersion"),
                type = PluginType.valueOf(root.getString("type").uppercase()),
                entry = root.optString("entry", "main.js"),
                language = if (root.isNull("language")) null else root.getString("language"),
                permissions = PluginPermissions(
                    network = network,
                    storage = permissions?.optBoolean("storage", false) ?: false,
                ),
            )
            validate(manifest)
            return manifest
        } catch (e: JSONException) {
            throw PluginException("Invalid plugin manifest: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw PluginException("Invalid plugin manifest: ${e.message}", e)
        }
    }

    private fun validate(manifest: PluginManifest) {
        if (manifest.apiVersion != PLUGIN_API_VERSION) {
            throw PluginException(
                "Plugin '${manifest.id}' needs API v${manifest.apiVersion}, " +
                    "this app supports v$PLUGIN_API_VERSION",
            )
        }
        val entry = manifest.entry
        if (entry.isBlank() || entry.startsWith("/") || entry.contains("..")) {
            throw PluginException("Plugin '${manifest.id}' has an invalid entry path: $entry")
        }
    }
}
