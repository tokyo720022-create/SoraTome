package io.github.tokyo720022create.soratome.data.plugins

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Small settings about plugins, kept on the device: which plugins are switched off,
 * where each installed plugin came from, and the stores the user added.
 */
class PluginRegistry(context: Context) {

    private val prefs = context.getSharedPreferences("plugins", Context.MODE_PRIVATE)

    fun isEnabled(id: String): Boolean = id !in disabledIds()

    fun setEnabled(id: String, enabled: Boolean) {
        val disabled = disabledIds().toMutableSet()
        if (enabled) disabled.remove(id) else disabled.add(id)
        prefs.edit().putStringSet(KEY_DISABLED, disabled).apply()
    }

    fun originOf(id: String): String? {
        val origins = JSONObject(prefs.getString(KEY_ORIGINS, "{}") ?: "{}")
        return if (origins.isNull(id)) null else origins.getString(id)
    }

    fun setOrigin(id: String, origin: String) {
        val origins = JSONObject(prefs.getString(KEY_ORIGINS, "{}") ?: "{}")
        origins.put(id, origin)
        prefs.edit().putString(KEY_ORIGINS, origins.toString()).apply()
    }

    fun clearOrigin(id: String) {
        val origins = JSONObject(prefs.getString(KEY_ORIGINS, "{}") ?: "{}")
        origins.remove(id)
        prefs.edit().putString(KEY_ORIGINS, origins.toString()).apply()
    }

    /** The stores the user added. Also reads the older format, a plain list of addresses. */
    fun stores(): List<StoreInfo> {
        val saved = prefs.getString(KEY_STORES, null) ?: prefs.getString(KEY_OLD_REPOSITORIES, null) ?: "[]"
        val array = JSONArray(saved)
        return List(array.length()) { index ->
            when (val item = array.get(index)) {
                is JSONObject -> StoreInfo(
                    url = item.getString("url"),
                    name = item.optString("name", "").ifBlank { null },
                    website = item.optString("website", "").ifBlank { null },
                    support = item.optString("support", "").ifBlank { null },
                    pluginCount = if (item.has("pluginCount")) item.getInt("pluginCount") else null,
                )
                else -> StoreInfo(url = item.toString())
            }
        }
    }

    fun saveStores(stores: List<StoreInfo>) {
        val array = JSONArray()
        stores.forEach { store ->
            val item = JSONObject().put("url", store.url)
            store.name?.let { item.put("name", it) }
            store.website?.let { item.put("website", it) }
            store.support?.let { item.put("support", it) }
            store.pluginCount?.let { item.put("pluginCount", it) }
            array.put(item)
        }
        prefs.edit().putString(KEY_STORES, array.toString()).apply()
    }

    private fun disabledIds(): Set<String> = prefs.getStringSet(KEY_DISABLED, emptySet()) ?: emptySet()

    private companion object {
        const val KEY_DISABLED = "disabled_ids"
        const val KEY_ORIGINS = "origins"
        const val KEY_STORES = "stores"
        const val KEY_OLD_REPOSITORIES = "repositories"
    }
}
