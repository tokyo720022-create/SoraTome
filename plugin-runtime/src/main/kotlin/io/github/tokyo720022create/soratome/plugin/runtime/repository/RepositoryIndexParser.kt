package io.github.tokyo720022create.soratome.plugin.runtime.repository

import io.github.tokyo720022create.soratome.plugin.api.manifest.PLUGIN_API_VERSION
import io.github.tokyo720022create.soratome.plugin.runtime.error.PluginException
import java.net.MalformedURLException
import java.net.URL
import org.json.JSONException
import org.json.JSONObject

/**
 * Reads a repository index (see docs/PLUGIN_REPOSITORY.md).
 * Package links may be relative to the index address. Plugins that need a different plugin
 * API version than this app supports are left out.
 */
object RepositoryIndexParser {

    const val MAX_INDEX_BYTES = 1024 * 1024

    private val sha256Pattern = Regex("^[0-9a-f]{64}$")

    fun parse(json: String, indexUrl: String): RepositoryIndex {
        try {
            val base = URL(indexUrl)
            val root = JSONObject(json)
            val name = root.optString("name", "").ifBlank { base.host }
            val array = root.getJSONArray("plugins")

            val plugins = ArrayList<RepositoryPlugin>()
            for (position in 0 until array.length()) {
                val item = array.getJSONObject(position)
                if (item.optInt("apiVersion", PLUGIN_API_VERSION) != PLUGIN_API_VERSION) continue

                val packageUrl = URL(base, item.getString("url"))
                if (packageUrl.protocol != "https") {
                    throw PluginException("This repository lists a link that is not https")
                }
                val sha256 = item.optString("sha256", "").lowercase().ifBlank { null }
                if (sha256 != null && !sha256Pattern.matches(sha256)) {
                    throw PluginException("This repository lists an invalid checksum")
                }
                plugins.add(
                    RepositoryPlugin(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        version = item.getString("version"),
                        description = item.optString("description", "").ifBlank { null },
                        language = item.optString("language", "").ifBlank { null },
                        packageUrl = packageUrl.toString(),
                        sha256 = sha256,
                    ),
                )
            }
            return RepositoryIndex(name = name, plugins = plugins)
        } catch (e: JSONException) {
            throw PluginException("This is not a valid plugin repository", e)
        } catch (e: MalformedURLException) {
            throw PluginException("This is not a valid plugin repository", e)
        }
    }
}
