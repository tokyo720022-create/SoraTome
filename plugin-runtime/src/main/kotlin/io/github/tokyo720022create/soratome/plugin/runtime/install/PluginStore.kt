package io.github.tokyo720022create.soratome.plugin.runtime.install

import io.github.tokyo720022create.soratome.plugin.runtime.error.PluginException
import java.io.File

/** Keeps installed plugins on disk, one folder per plugin id. */
class PluginStore(val root: File) {

    /** Saves [pkg], replacing any installed version of the same plugin. */
    fun write(pkg: PluginPackage) {
        val id = pkg.manifest.id
        root.mkdirs()

        val temp = File(root, ".installing-$id")
        temp.deleteRecursively()
        if (!temp.mkdirs()) throw PluginException("Could not create the plugin folder")

        File(temp, "manifest.json").writeText(pkg.manifestJson, Charsets.UTF_8)
        val scriptFile = File(temp, pkg.manifest.entry)
        scriptFile.parentFile?.mkdirs()
        scriptFile.writeText(pkg.script, Charsets.UTF_8)

        val target = File(root, id)
        target.deleteRecursively()
        if (!temp.renameTo(target)) {
            temp.deleteRecursively()
            throw PluginException("Could not save the plugin")
        }
    }

    fun delete(id: String) {
        File(root, id).deleteRecursively()
    }
}
