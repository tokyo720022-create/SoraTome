package io.github.tokyo720022create.soratome.plugin.runtime.repository

/**
 * @property website Web page of whoever runs the store (https only), if given.
 * @property support Where to get help, such as a community page (https only), if given.
 */
data class RepositoryIndex(
    val name: String,
    val website: String?,
    val support: String?,
    val plugins: List<RepositoryPlugin>,
)
