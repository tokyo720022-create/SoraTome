package io.github.tokyo720022create.soratome.data.plugins

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Small settings about plugins, kept on the device: which plugins are switched off,
 * where each installed plugin came from, and the repositories the user added.
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

    fun repositories(): List<String> {
        val array = JSONArray(prefs.getString(KEY_REPOSITORIES, "[]") ?: "[]")
        return List(array.length()) { index -> array.getString(index) }
    }

    fun saveRepositories(urls: List<String>) {
        prefs.edit().putString(KEY_REPOSITORIES, JSONArray(urls).toString()).apply()
    }

    private fun disabledIds(): Set<String> = prefs.getStringSet(KEY_DISABLED, emptySet()) ?: emptySet()

    private companion object {
        const val KEY_DISABLED = "disabled_ids"
        const val KEY_ORIGINS = "origins"
        const val KEY_REPOSITORIES = "repositories"
    }
}
