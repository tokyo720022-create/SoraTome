package io.github.tokyo720022create.soratome.plugin.runtime.repository

/** One plugin listed in a repository. */
data class RepositoryPlugin(
    val id: String,
    val name: String,
    val version: String,
    val description: String?,
    val language: String?,
    val packageUrl: String,
    val sha256: String?,
)
