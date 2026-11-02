package io.github.tokyo720022create.soratome.plugin.api.manifest

/**
 * How suitable a plugin's content is. A plugin or store that does not say is treated as [MIXED].
 * The label is a claim made by the plugin's author; the app cannot check it.
 */
enum class ContentWarning(val label: String, val description: String) {
    SAFE("Safe", "All-ages content"),
    MIXED("Mixed", "May include mature themes"),
    ADULT("18+", "Adult content"),
}
