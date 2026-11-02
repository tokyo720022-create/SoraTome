package io.github.tokyo720022create.soratome.data.plugins

/**
 * A plugin store (repository) the user added, with what was last read from its index.
 * Only [url] is always known; the rest is filled in when the store is added or refreshed.
 */
data class StoreInfo(
    val url: String,
    val name: String? = null,
    val website: String? = null,
    val support: String? = null,
    val pluginCount: Int? = null,
)
