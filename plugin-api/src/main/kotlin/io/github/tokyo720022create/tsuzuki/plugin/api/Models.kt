package io.github.tokyo720022create.tsuzuki.plugin.api

data class SeriesSummary(
    val id: String,
    val title: String,
    val coverUrl: String? = null,
)

data class SearchResult(
    val items: List<SeriesSummary>,
    val hasNext: Boolean,
)

data class SeriesDetails(
    val id: String,
    val title: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val tags: List<String> = emptyList(),
)

data class ChapterInfo(
    val id: String,
    val name: String,
    val number: Float? = null,
    val dateMillis: Long? = null,
)

data class PageInfo(
    val index: Int,
    val url: String,
)
