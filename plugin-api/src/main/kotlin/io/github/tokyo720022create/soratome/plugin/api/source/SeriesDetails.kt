package io.github.tokyo720022create.soratome.plugin.api.source

data class SeriesDetails(
    val id: String,
    val title: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val tags: List<String> = emptyList(),
)
