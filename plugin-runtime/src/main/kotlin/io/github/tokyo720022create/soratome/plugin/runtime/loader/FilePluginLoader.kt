package io.github.tokyo720022create.soratome.plugin.runtime.loader

import io.github.tokyo720022create.soratome.plugin.api.source.SourcePlugin
import io.github.tokyo720022create.soratome.plugin.runtime.manifest.ManifestParser
import io.github.tokyo720022create.soratome.plugin.runtime.script.ScriptSourcePlugin
import java.io.File

/**
 * Loads installed plugins from a folder, one subfolder per plugin id:
 * `<root>/<id>/manifest.json` plus the script named in the manifest.
 */
class FilePluginLoader(
    private val root: File,
    private val logger: (String) -> Unit = {},
) {

    fun loadAll(): List<SourcePlugin> {
        val folders = root.listFiles()
            ?.filter { it.isDirectory && !it.name.startsWith(".") }
            ?.sortedBy { it.name }
            .orEmpty()

        return folders.mapNotNull { folder ->
            try {
                val manifestJson = File(folder, "manifest.json").readText(Charsets.UTF_8)
                val manifest = ManifestParser.parse(manifestJson)
                if (manifest.id != folder.name) {
                    logger("Skipped plugin folder '${folder.name}': the id in its manifest is different")
                    null
                } else {
                    val script = File(folder, manifest.entry).readText(Charsets.UTF_8)
                    ScriptSourcePlugin.create(manifestJson, script, logger).also {
                        logger("Loaded installed plugin ${manifest.id} v${manifest.version}")
                    }
                }
            } catch (e: Exception) {
                logger("Could not load plugin in '${folder.name}': ${e.message}")
                null
            }
        }
    }
}
