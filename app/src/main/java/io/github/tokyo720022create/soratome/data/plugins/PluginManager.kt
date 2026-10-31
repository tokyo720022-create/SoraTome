package io.github.tokyo720022create.soratome.data.plugins

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.tokyo720022create.soratome.plugin.api.source.SourcePlugin
import io.github.tokyo720022create.soratome.plugin.runtime.error.PluginException
import io.github.tokyo720022create.soratome.plugin.runtime.install.Hashing
import io.github.tokyo720022create.soratome.plugin.runtime.install.LimitedReader
import io.github.tokyo720022create.soratome.plugin.runtime.install.PluginDownloader
import io.github.tokyo720022create.soratome.plugin.runtime.install.PluginPackage
import io.github.tokyo720022create.soratome.plugin.runtime.install.PluginPackageReader
import io.github.tokyo720022create.soratome.plugin.runtime.install.PluginStore
import io.github.tokyo720022create.soratome.plugin.runtime.install.PluginVersion
import io.github.tokyo720022create.soratome.plugin.runtime.loader.AssetPluginLoader
import io.github.tokyo720022create.soratome.plugin.runtime.loader.FilePluginLoader
import io.github.tokyo720022create.soratome.plugin.runtime.repository.RepositoryIndex
import io.github.tokyo720022create.soratome.plugin.runtime.repository.RepositoryIndexParser
import io.github.tokyo720022create.soratome.plugin.runtime.repository.RepositoryPlugin
import io.github.tokyo720022create.soratome.plugin.runtime.script.ScriptSourcePlugin
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Knows every plugin: the ones packaged in the app (debug builds) and the ones the user
 * installed. Installs, updates, removes and switches plugins, and manages repositories.
 *
 * SoraTome ships with no plugins in release builds and no repositories at all.
 */
class PluginManager(private val context: Context) {

    private val registry = PluginRegistry(context)
    private val store = PluginStore(File(context.filesDir, "plugins"))
    private val logger: (String) -> Unit = { message -> Log.d("SoraTome", message) }

    private var bundled: List<SourcePlugin> = emptyList()
    private var installed: List<SourcePlugin> = emptyList()

    /** Every plugin, installed ones first. An installed plugin replaces a bundled one with the same id. */
    var entries by mutableStateOf<List<PluginEntry>>(emptyList())
        private set

    /** Repository addresses the user added. */
    var repositories by mutableStateOf(registry.repositories())
        private set

    /** The plugins that are switched on. */
    val enabledPlugins: List<SourcePlugin>
        get() = entries.filter { it.enabled }.map { it.plugin }

    init {
        bundled = AssetPluginLoader(context, logger).loadAll()
        installed = FilePluginLoader(store.root, logger).loadAll()
        refreshEntries()
    }

    // ---- installing ----

    /** Reads a plugin package the user picked on their device. */
    suspend fun readPackage(uri: Uri): PluginPackage = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
            LimitedReader.read(stream, PluginPackageReader.MAX_PACKAGE_BYTES, "The plugin package is too large")
        } ?: throw PluginException("Could not open the file")
        PluginPackageReader.read(bytes)
    }

    /** Downloads a plugin package from a link the user typed. */
    suspend fun downloadFromLink(url: String): PluginPackage = withContext(Dispatchers.IO) {
        PluginPackageReader.read(PluginDownloader.download(url, PluginPackageReader.MAX_PACKAGE_BYTES))
    }

    /** Downloads a plugin listed in a repository and checks it against the listing. */
    suspend fun downloadFromRepository(entry: RepositoryPlugin): PluginPackage = withContext(Dispatchers.IO) {
        val bytes = PluginDownloader.download(entry.packageUrl, PluginPackageReader.MAX_PACKAGE_BYTES)
        val expected = entry.sha256
        if (expected != null && Hashing.sha256Hex(bytes) != expected) {
            throw PluginException("The download does not match the checksum in the repository")
        }
        val pkg = PluginPackageReader.read(bytes)
        if (pkg.manifest.id != entry.id) {
            throw PluginException("The package does not match what the repository lists")
        }
        pkg
    }

    /** Installs [pkg], or updates the installed version of the same plugin. */
    suspend fun install(pkg: PluginPackage, origin: String) {
        withContext(Dispatchers.IO) {
            val id = pkg.manifest.id
            store.write(pkg)
            val plugin = ScriptSourcePlugin.create(pkg.manifestJson, pkg.script, logger)
            val previous = installed.firstOrNull { it.manifest.id == id }
            installed = installed.filter { it.manifest.id != id } + plugin
            registry.setOrigin(id, origin)
            refreshEntries()
            (previous as? ScriptSourcePlugin)?.close()
        }
    }

    suspend fun uninstall(id: String) {
        withContext(Dispatchers.IO) {
            val previous = installed.firstOrNull { it.manifest.id == id }
            store.delete(id)
            installed = installed.filter { it.manifest.id != id }
            registry.clearOrigin(id)
            refreshEntries()
            (previous as? ScriptSourcePlugin)?.close()
        }
    }

    fun setEnabled(id: String, enabled: Boolean) {
        registry.setEnabled(id, enabled)
        refreshEntries()
    }

    // ---- repositories ----

    /** Reads a repository index. */
    suspend fun fetchRepository(url: String): RepositoryIndex = withContext(Dispatchers.IO) {
        val bytes = PluginDownloader.download(url, RepositoryIndexParser.MAX_INDEX_BYTES)
        RepositoryIndexParser.parse(String(bytes, Charsets.UTF_8), url)
    }

    /** Checks that [url] is a readable repository, then remembers it. */
    suspend fun addRepository(url: String) {
        val clean = url.trim()
        try {
            URL(clean)
        } catch (e: MalformedURLException) {
            throw PluginException("This is not a valid link")
        }
        if (clean in repositories) throw PluginException("This repository is already added")
        fetchRepository(clean)
        val updated = repositories + clean
        registry.saveRepositories(updated)
        repositories = updated
    }

    fun removeRepository(url: String) {
        val updated = repositories.filter { it != url }
        registry.saveRepositories(updated)
        repositories = updated
    }

    /** Looks in every added repository for newer versions of the installed plugins. */
    suspend fun checkForUpdates(): UpdateCheck = withContext(Dispatchers.IO) {
        val best = LinkedHashMap<String, AvailableUpdate>()
        val failed = ArrayList<String>()

        for (url in repositories) {
            val index = try {
                fetchRepository(url)
            } catch (e: Exception) {
                failed.add(url)
                continue
            }
            for (candidate in index.plugins) {
                val current = entries.firstOrNull { it.manifest.id == candidate.id && !it.bundled } ?: continue
                if (PluginVersion.compare(candidate.version, current.manifest.version) <= 0) continue
                val known = best[candidate.id]
                if (known == null || PluginVersion.compare(candidate.version, known.candidate.version) > 0) {
                    best[candidate.id] = AvailableUpdate(current, url, candidate)
                }
            }
        }
        UpdateCheck(updates = best.values.toList(), failedRepositories = failed)
    }

    // ---- internals ----

    private fun refreshEntries() {
        val installedIds = installed.map { it.manifest.id }.toSet()
        val installedEntries = installed.map { plugin ->
            PluginEntry(
                plugin = plugin,
                bundled = false,
                enabled = registry.isEnabled(plugin.manifest.id),
                origin = registry.originOf(plugin.manifest.id),
            )
        }
        val bundledEntries = bundled
            .filter { it.manifest.id !in installedIds }
            .map { plugin ->
                PluginEntry(
                    plugin = plugin,
                    bundled = true,
                    enabled = registry.isEnabled(plugin.manifest.id),
                    origin = null,
                )
            }
        entries = (installedEntries + bundledEntries).sortedBy { it.manifest.name.lowercase() }
    }
}
