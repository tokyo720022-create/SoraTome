package io.github.tokyo720022create.soratome.plugin.runtime.repository

data class RepositoryIndex(
    val name: String,
    val plugins: List<RepositoryPlugin>,
)
