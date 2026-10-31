package io.github.tokyo720022create.soratome.feature.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.tokyo720022create.soratome.plugin.api.source.SeriesSummary

/** What the Browse tab is showing. Lives in the Application so it survives navigation. */
class BrowseState {
    var selectedPluginId by mutableStateOf<String?>(null)
    var query by mutableStateOf("")
    var results by mutableStateOf(emptyList<SeriesSummary>())
    var status by mutableStateOf("")
    var page by mutableIntStateOf(1)
    var hasNext by mutableStateOf(false)
    var searched by mutableStateOf(false)

    /** The plugin the current results came from. */
    var resultsPluginId by mutableStateOf<String?>(null)

    /** The series opened from the results list. */
    var selectedSeries by mutableStateOf<SeriesSummary?>(null)

    fun clearResults() {
        results = emptyList()
        status = ""
        page = 1
        hasNext = false
        searched = false
        resultsPluginId = null
    }
}
