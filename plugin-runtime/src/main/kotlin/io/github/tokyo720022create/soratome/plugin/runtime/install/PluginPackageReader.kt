package io.github.tokyo720022create.soratome.plugin.runtime.install

import io.github.tokyo720022create.soratome.plugin.api.manifest.PluginManifest
import io.github.tokyo720022create.soratome.plugin.api.manifest.PluginType
import io.github.tokyo720022create.soratome.plugin.runtime.error.PluginException
import io.github.tokyo720022create.soratome.plugin.runtime.manifest.ManifestParser
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.ZipInputStream

/**
 * Reads and checks a plugin package.
 *
 * A package is a zip file containing `manifest.json` and the script named in it, either at
 * the top level or inside one folder. Anything unsafe or unexpected is rejected.
 */
object PluginPackageReader {

    const val MAX_PACKAGE_BYTES = 2 * 1024 * 1024

    private const val MAX_ENTRY_BYTES = 1024 * 1024
    private const val MAX_TOTAL_BYTES = 4 * 1024 * 1024
    private const val MAX_ENTRIES = 16

    private val idPattern = Regex("^[a-z0-9][a-z0-9._-]{2,79}$")
    private val versionPattern = Regex("^[0-9A-Za-z][0-9A-Za-z.+-]{0,31}$")
    private val domainPattern = Regex("^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$")
    private val ipPattern = Regex("^[0-9.]+$")

    fun read(bytes: ByteArray): PluginPackage {
        if (bytes.size > MAX_PACKAGE_BYTES) throw PluginException("The plugin package is too large")

        val files = readEntries(bytes)
        val manifestPath = (
            if ("manifest.json" in files) {
                "manifest.json"
            } else {
                files.keys.firstOrNull { path -> path.count { it == '/' } == 1 && path.endsWith("/manifest.json") }
            }
            ) ?: throw PluginException("The package has no manifest.json")

        val prefix = manifestPath.removeSuffix("manifest.json")
        val manifestJson = String(files.getValue(manifestPath), Charsets.UTF_8)
        val manifest = ManifestParser.parse(manifestJson)
        validate(manifest)

        val scriptBytes = files[prefix + manifest.entry]
            ?: throw PluginException("The package is missing ${manifest.entry}")
        val script = String(scriptBytes, Charsets.UTF_8)
        if (script.isBlank()) throw PluginException("${manifest.entry} is empty")

        return PluginPackage(manifest, manifestJson, script, bytes.size)
    }

    private fun readEntries(bytes: ByteArray): Map<String, ByteArray> {
        val notZip = "This is not a valid plugin package (it must be a zip file)"
        val files = LinkedHashMap<String, ByteArray>()
        var total = 0
        try {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                var count = 0
                while (true) {
                    val entry = zip.nextEntry ?: break
                    count++
                    if (count > MAX_ENTRIES) throw PluginException("The package has too many files")
                    val name = entry.name
                    if (name.startsWith("/") || name.contains("..") || name.contains('\\')) {
                        throw PluginException("The package contains an unsafe file name")
                    }
                    if (entry.isDirectory) continue
                    val data = LimitedReader.read(zip, MAX_ENTRY_BYTES, "A file in the package is too large")
                    total += data.size
                    if (total > MAX_TOTAL_BYTES) throw PluginException("The package is too large when unpacked")
                    files[name] = data
                }
            }
        } catch (e: IOException) {
            throw PluginException(notZip, e)
        }
        if (files.isEmpty()) throw PluginException(notZip)
        return files
    }

    private fun validate(manifest: PluginManifest) {
        if (!idPattern.matches(manifest.id)) {
            throw PluginException(
                "The plugin id \"${manifest.id}\" is not valid. Use lowercase letters, digits, dots and dashes.",
            )
        }
        if (!versionPattern.matches(manifest.version)) {
            throw PluginException("The plugin version \"${manifest.version}\" is not valid")
        }
        if (manifest.type != PluginType.SOURCE) {
            throw PluginException("This version of SoraTome can only use source plugins")
        }
        manifest.permissions.network.forEach { domain ->
            val name = domain.lowercase()
            if (!domainPattern.matches(name) || ipPattern.matches(name)) {
                throw PluginException("\"$domain\" is not a valid website name for plugin permissions")
            }
        }
    }
}
