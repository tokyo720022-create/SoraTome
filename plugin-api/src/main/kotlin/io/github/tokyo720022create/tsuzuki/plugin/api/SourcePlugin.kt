package io.github.tokyo720022create.tsuzuki.plugin.api

/** Contract every source plugin fulfils, regardless of the runtime that executes it. */
interface SourcePlugin {
    val manifest: PluginManifest

    suspend fun search(query: String, page: Int): SearchResult

    suspend fun getSeries(id: String): SeriesDetails

    suspend fun getChapters(seriesId: String): List<ChapterInfo>

    suspend fun getPages(chapterId: String): List<PageInfo>
}
