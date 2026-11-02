package io.github.tokyo720022create.soratome.data.plugins

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.tokyo720022create.soratome.plugin.api.manifest.ContentWarning

/**
 * Which content warnings the user allows for plugins (the label a plugin or store gives itself),
 * and whether that also applies to plugins that are already installed.
 */
class PluginPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("plugin_preferences", Context.MODE_PRIVATE)

    var allowedWarnings by mutableStateOf(loadAllowed())
        private set

    /** When off, plugins you already installed keep loading and updating whatever their warning. */
    var applyToInstalled by mutableStateOf(prefs.getBoolean(KEY_APPLY_TO_INSTALLED, true))
        private set

    fun isAllowed(warning: ContentWarning): Boolean = warning in allowedWarnings

    fun setAllowed(warning: ContentWarning, allowed: Boolean) {
        val updated = if (allowed) allowedWarnings + warning else allowedWarnings - warning
        allowedWarnings = updated
        prefs.edit().putStringSet(KEY_ALLOWED, updated.map { it.name }.toSet()).apply()
    }

    fun updateApplyToInstalled(value: Boolean) {
        applyToInstalled = value
        prefs.edit().putBoolean(KEY_APPLY_TO_INSTALLED, value).apply()
    }

    /** Short text for settings rows, such as "Safe, Mixed". */
    fun summary(): String =
        ContentWarning.entries.filter { it in allowedWarnings }
            .joinToString(", ") { it.label }
            .ifEmpty { "None" }

    private fun loadAllowed(): Set<ContentWarning> {
        val saved = prefs.getStringSet(KEY_ALLOWED, null) ?: return setOf(ContentWarning.SAFE, ContentWarning.MIXED)
        return saved.mapNotNull { name -> ContentWarning.entries.firstOrNull { it.name == name } }.toSet()
    }

    private companion object {
        const val KEY_ALLOWED = "allowed_warnings"
        const val KEY_APPLY_TO_INSTALLED = "apply_to_installed"
    }
}
