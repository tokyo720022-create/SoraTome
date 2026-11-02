package io.github.tokyo720022create.soratome.plugin.runtime.repository

import io.github.tokyo720022create.soratome.plugin.api.manifest.ContentWarning

/** One plugin listed in a repository. */
data class RepositoryPlugin(
    val id: String,
    val name: String,
    val version: String,
    val description: String?,
    val language: String?,
    val packageUrl: String,
    val sha256: String?,
    val contentWarning: ContentWarning,
)
