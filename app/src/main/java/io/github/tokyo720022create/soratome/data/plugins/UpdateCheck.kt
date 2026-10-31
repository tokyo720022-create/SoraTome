package io.github.tokyo720022create.soratome.data.plugins

/** Result of looking for plugin updates. [failedRepositories] could not be read. */
data class UpdateCheck(
    val updates: List<AvailableUpdate>,
    val failedRepositories: List<String>,
)
