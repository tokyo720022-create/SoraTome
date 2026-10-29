package io.github.tokyo720022create.soratome.plugin.runtime.script

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.asyncFunction
import com.dokar.quickjs.binding.define
import com.dokar.quickjs.binding.function
import com.dokar.quickjs.evaluate
import io.github.tokyo720022create.soratome.plugin.api.manifest.PluginManifest
import io.github.tokyo720022create.soratome.plugin.api.manifest.PluginType
import io.github.tokyo720022create.soratome.plugin.api.source.ChapterInfo
import io.github.tokyo720022create.soratome.plugin.api.source.PageInfo
import io.github.tokyo720022create.soratome.plugin.api.source.SearchResult
import io.github.tokyo720022create.soratome.plugin.api.source.SeriesDetails
import io.github.tokyo720022create.soratome.plugin.api.source.SeriesSummary
import io.github.tokyo720022create.soratome.plugin.api.source.SourcePlugin
import io.github.tokyo720022create.soratome.plugin.runtime.error.PluginException
import io.github.tokyo720022create.soratome.plugin.runtime.manifest.ManifestParser
import io.github.tokyo720022create.soratome.plugin.runtime.network.HostHttp
import io.github.tokyo720022create.soratome.plugin.runtime.network.NetworkPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

/**
 * A [SourcePlugin] implemented by a JavaScript file running inside a QuickJS sandbox.
 *
 * The script must define `globalThis.plugin` with the async functions `search`,
 * `getSeries`, `getChapters` and `getPages`. It can only reach the outside world
 * through the `host` object (see docs/PLUGIN_API.md).
 */
class ScriptSourcePlugin private constructor(
    override val manifest: PluginManifest,
    private val script: String,
    private val logger: (String) -> Unit,
) : SourcePlugin {

    private val mutex = Mutex()
    private var instance: QuickJs? = null

    override suspend fun search(query: String, page: Int): SearchResult {
        val root = JSONObject(call("search", query, page))
        val items = root.getJSONArray("items").mapObjects { obj ->
            SeriesSummary(
                id = obj.getString("id"),
                title = obj.getString("title"),
                coverUrl = obj.stringOrNull("cover"),
            )
        }
        return SearchResult(items = items, hasNext = root.optBoolean("hasNext", false))
    }

    override suspend fun getSeries(id: String): SeriesDetails {
        val obj = JSONObject(call("getSeries", id))
        val tags = obj.optJSONArray("tags")?.let { array ->
            List(array.length()) { index -> array.getString(index) }
        } ?: emptyList()
        return SeriesDetails(
            id = obj.getString("id"),
            title = obj.getString("title"),
            description = obj.stringOrNull("description"),
            coverUrl = obj.stringOrNull("cover"),
            tags = tags,
        )
    }

    override suspend fun getChapters(seriesId: String): List<ChapterInfo> {
        return JSONArray(call("getChapters", seriesId)).mapObjects { obj ->
            ChapterInfo(
                id = obj.getString("id"),
                name = obj.getString("name"),
                number = if (obj.isNull("number")) null else obj.getDouble("number").toFloat(),
                dateMillis = if (obj.isNull("date")) null else obj.getLong("date"),
            )
        }
    }

    override suspend fun getPages(chapterId: String): List<PageInfo> {
        return JSONArray(call("getPages", chapterId)).mapObjects { obj ->
            PageInfo(index = obj.getInt("index"), url = obj.getString("url"))
        }
    }

    /** Releases the JavaScript engine. The plugin can still be used afterwards; it restarts lazily. */
    suspend fun close() {
        mutex.withLock {
            instance?.close()
            instance = null
        }
    }

    private suspend fun call(method: String, vararg args: Any?): String {
        val js = ensureEngine()
        val argList = args.joinToString(", ") { jsLiteral(it) }
        val code = "JSON.stringify(await plugin.$method($argList))"
        return js.evaluate<String?>(code, filename = "$method.call")
            ?: throw PluginException("Plugin '${manifest.id}' returned nothing from $method()")
    }

    private suspend fun ensureEngine(): QuickJs = mutex.withLock {
        instance ?: createEngine().also { instance = it }
    }

    private suspend fun createEngine(): QuickJs {
        val js = QuickJs.create(Dispatchers.Default)
        try {
            js.evaluationTimeoutMillis = 60_000
            val http = HostHttp(NetworkPolicy(manifest.permissions.network))

            js.define("host") {
                function<String, Unit>("log") { message -> logger(message) }
                asyncFunction<String, String>("httpGet") { url -> http.get(url) }
            }
            js.evaluate<Any?>(PRELUDE, filename = "prelude.js")
            js.evaluate<Any?>(script, filename = manifest.entry)

            val ready = js.evaluate<Boolean>("typeof plugin === 'object' && plugin !== null")
            if (!ready) {
                throw PluginException("Plugin '${manifest.id}' did not define globalThis.plugin")
            }
            return js
        } catch (t: Throwable) {
            js.close()
            throw t
        }
    }

    companion object {
        private const val PRELUDE = """
            host.http = {
                get: function (url) { return host.httpGet(String(url)); }
            };
        """

        fun create(
            manifestJson: String,
            script: String,
            logger: (String) -> Unit = {},
        ): ScriptSourcePlugin {
            val manifest = ManifestParser.parse(manifestJson)
            if (manifest.type != PluginType.SOURCE) {
                throw PluginException("Plugin '${manifest.id}' is not a source plugin")
            }
            return ScriptSourcePlugin(manifest, script, logger)
        }

        private fun jsLiteral(value: Any?): String = when (value) {
            null -> "null"
            is String -> JSONObject.quote(value)
            is Number, is Boolean -> value.toString()
            else -> throw IllegalArgumentException("Unsupported argument type: ${value::class}")
        }
    }
}
