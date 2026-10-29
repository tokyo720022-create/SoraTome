package io.github.tokyo720022create.soratome.plugin.runtime

import android.content.Context
import io.github.tokyo720022create.soratome.plugin.api.SourcePlugin

/**
 * Loads plugins that are packaged as assets, one folder per plugin:
 * `assets/<root>/<plugin-folder>/manifest.json` plus the script named in the manifest.
 */
class AssetPluginLoader(
    private val context: Context,
    private val logger: (String) -> Unit = {},
) {

    fun loadAll(root: String = "plugins"): List<SourcePlugin> {
        val folders = context.assets.list(root).orEmpty()
        return folders.mapNotNull { folder ->
            try {
                val manifestJson = readAsset("$root/$folder/manifest.json")
                val manifest = ManifestParser.parse(manifestJson)
                val script = readAsset("$root/$folder/${manifest.entry}")
                ScriptSourcePlugin.create(manifestJson, script, logger).also {
                    logger("Loaded plugin ${manifest.id} v${manifest.version}")
                }
            } catch (e: Exception) {
                logger("Could not load plugin in '$folder': ${e.message}")
                null
            }
        }
    }

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }
}
