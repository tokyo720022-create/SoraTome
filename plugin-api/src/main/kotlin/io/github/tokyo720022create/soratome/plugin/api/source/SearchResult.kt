package io.github.tokyo720022create.soratome.plugin.api.source

data class SearchResult(
    val items: List<SeriesSummary>,
    val hasNext: Boolean,
)
