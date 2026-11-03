package io.github.tokyo720022create.soratome.data.plugins

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.tokyo720022create.soratome.plugin.api.manifest.ContentWarning
import io.github.tokyo720022create.soratome.plugin.api.manifest.PluginManifest
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
import io.github.tokyo720022create.soratome.plugin.runtime.repository.StoreAddress
import io.github.tokyo720022create.soratome.plugin.runtime.script.ScriptSourcePlugin
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Knows every plugin: the ones packaged in the app (debug builds) and the ones the user
 * installed. Installs, updates, removes and switches plugins, and manages plugin stores.
 *
 * SoraTome ships with no plugins in release builds and no stores at all.
 */
class PluginManager(
    private val context: Context,
    private val preferences: PluginPreferences,
) {

    private val registry = PluginRegistry(context)
    private val store = PluginStore(File(context.filesDir, "plugins"))
    private val logger: (String) -> Unit = { message -> Log.d("SoraTome", message) }

    private var bundled: List<SourcePlugin> = emptyList()
    private var installed: List<SourcePlugin> = emptyList()

    /** Every plugin, installed ones first. An installed plugin replaces a bundled one with the same id. */
    var entries by mutableStateOf<List<PluginEntry>>(emptyList())
        private set

    /** The plugin stores the user added. */
    var stores by mutableStateOf(registry.stores())
        private set

    /** Addresses of the stores, in the same order as [stores]. */
    val repositories: List<String>
        get() = stores.map { it.url }

    /** The plugins that are switched on and allowed by the content warning settings. */
    val enabledPlugins: List<SourcePlugin>
        get() = entries.filter { it.enabled && isLoadable(it.manifest) }.map { it.plugin }

    init {
        bundled = AssetPluginLoader(context, logger).loadAll()
        installed = FilePluginLoader(this.store.root, logger).loadAll()
        refreshEntries()
    }

    /**
     * False when the content warning settings keep this plugin from loading.
     * With "also apply to installed plugins" off, the settings only affect new installs.
     */
    fun isLoadable(manifest: PluginManifest): Boolean =
        !preferences.applyToInstalled || preferences.isAllowed(manifest.contentWarning)

    // ---- installing ----

    /** Reads a plugin package the user picked on their device. */
    suspend fun readPackage(uri: Uri): PluginPackage = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
            LimitedReader.read(stream, PluginPackageReader.MAX_PACKAGE_BYTES, "The plugin package is too large")
        } ?: throw PluginException("Could not open the file")
        PluginPackageReader.read(bytes)
    }

    /**
     * Reads what a link the user pasted points to: a plugin package, or a plugin store.
     * A GitHub page for a file is turned into its raw address first.
     */
    suspend fun inspectLink(input: String): LinkContent = withContext(Dispatchers.IO) {
        val address = StoreAddress.toRaw(input)
        val bytes = PluginDownloader.download(address, PluginPackageReader.MAX_PACKAGE_BYTES)
        if (!PluginPackageReader.looksLikeZip(bytes)) {
            val text = String(bytes, Charsets.UTF_8)
            if (text.trimStart().startsWith("{")) {
                val index = try {
                    RepositoryIndexParser.parse(text, address)
                } catch (e: PluginException) {
                    null
                }
                if (index != null) return@withContext LinkContent.Store(address, index)
            }
        }
        LinkContent.Package(PluginPackageReader.read(bytes))
    }

    /** Downloads a plugin listed in a store and checks it against the listing. */
    suspend fun downloadFromRepository(entry: RepositoryPlugin): PluginPackage = withContext(Dispatchers.IO) {
        val bytes = PluginDownloader.download(entry.packageUrl, PluginPackageReader.MAX_PACKAGE_BYTES)
        val expected = entry.sha256
        if (expected != null && Hashing.sha256Hex(bytes) != expected) {
            throw PluginException("The download does not match the checksum in the store")
        }
        val pkg = PluginPackageReader.read(bytes)
        if (pkg.manifest.id != entry.id) {
            throw PluginException("The package does not match what the store lists")
        }
        pkg
    }

    /**
     * Installs [pkg], or updates the installed version of the same plugin.
     * A plugin whose content warning is not allowed cannot be installed. An update of a plugin
     * you already have is allowed when "also apply to installed plugins" is off.
     */
    suspend fun install(pkg: PluginPackage, origin: String) {
        withContext(Dispatchers.IO) {
            val manifest = pkg.manifest
            val id = manifest.id
            val isUpdate = installed.any { it.manifest.id == id }
            val allowedByUpdateRule = isUpdate && !preferences.applyToInstalled
            if (!allowedByUpdateRule && !preferences.isAllowed(manifest.contentWarning)) {
                throw PluginException(
                    "This plugin is marked ${manifest.contentWarning.label}. " +
                        "Allow it in Settings > Browse > Allowed content warnings first.",
                )
            }

            this@PluginManager.store.write(pkg)
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
            this@PluginManager.store.delete(id)
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

    // ---- stores ----

    /** Reads a store's index. */
    suspend fun fetchRepository(url: String): RepositoryIndex = withContext(Dispatchers.IO) {
        val bytes = PluginDownloader.download(url, RepositoryIndexParser.MAX_INDEX_BYTES)
        if (PluginPackageReader.looksLikeZip(bytes)) {
            throw PluginException(
                "This link is a plugin package, not a store. Use Installed plugins > Install from link.",
            )
        }
        RepositoryIndexParser.parse(String(bytes, Charsets.UTF_8), url)
    }

    /**
     * Finds the store at [input] (a store index link, or a GitHub page that holds one), checks it,
     * and remembers it with its name and links.
     */
    suspend fun addRepository(input: String) {
        val candidates = StoreAddress.candidates(input)
        var lastError: PluginException? = null
        for (address in candidates) {
            val index = try {
                fetchRepository(address)
            } catch (e: PluginException) {
                lastError = e
                continue
            }
            saveStore(address, index)
            return
        }
        if (candidates.size == 1 && lastError != null) throw lastError
        throw PluginException("Could not find a plugin store at this address")
    }

    /** Remembers a store whose index was already read. */
    fun saveStore(url: String, index: RepositoryIndex) {
        if (url in repositories) throw PluginException("This store is already added")
        val updated = stores + infoFrom(url, index)
        registry.saveStores(updated)
        stores = updated
    }

    fun removeRepository(url: String) {
        val updated = stores.filter { it.url != url }
        registry.saveStores(updated)
        stores = updated
    }

    /** Reads every store again and updates the saved names and links. Returns the addresses that failed. */
    suspend fun refreshStores(): List<String> {
        val failed = ArrayList<String>()
        val updated = stores.map { known ->
            try {
                infoFrom(known.url, fetchRepository(known.url))
            } catch (e: Exception) {
                failed.add(known.url)
                known
            }
        }
        registry.saveStores(updated)
        stores = updated
        return failed
    }

    /** Looks in every added store for newer versions of the installed plugins. */
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

    private fun infoFrom(url: String, index: RepositoryIndex) = StoreInfo(
        url = url,
        name = index.name,
        website = index.website,
        support = index.support,
        pluginCount = index.plugins.size,
    )

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
