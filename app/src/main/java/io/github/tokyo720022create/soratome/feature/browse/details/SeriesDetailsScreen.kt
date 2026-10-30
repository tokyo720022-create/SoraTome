package io.github.tokyo720022create.soratome.feature.browse.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.data.reader.ReadingProgressStore
import io.github.tokyo720022create.soratome.domain.reader.ChapterNaming
import io.github.tokyo720022create.soratome.domain.reader.ChapterOrder
import io.github.tokyo720022create.soratome.domain.reader.ChapterSort
import io.github.tokyo720022create.soratome.feature.reader.ReaderSession
import io.github.tokyo720022create.soratome.plugin.api.source.ChapterInfo
import io.github.tokyo720022create.soratome.plugin.api.source.SeriesDetails
import io.github.tokyo720022create.soratome.ui.components.RemoteImage
import io.github.tokyo720022create.soratome.ui.components.ScreenScaffold
import kotlinx.coroutines.CancellationException

private sealed interface DetailsState {
    data object Loading : DetailsState
    data class Failed(val message: String) : DetailsState
    data class Loaded(val details: SeriesDetails, val chapters: List<ChapterInfo>) : DetailsState
}

@Composable
fun SeriesDetailsScreen(
    onBack: () -> Unit,
    onOpenReader: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as SoraTomeApplication
    val browse = application.browseState
    val series = browse.selectedSeries
    val plugin = application.plugins.firstOrNull { it.manifest.id == browse.selectedPluginId }

    if (series == null || plugin == null) {
        ScreenScaffold(title = "Series", onBack = onBack) { padding ->
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No series selected.")
            }
        }
        return
    }

    var state by remember(series.id) { mutableStateOf<DetailsState>(DetailsState.Loading) }
    var attempt by remember(series.id) { mutableIntStateOf(0) }

    LaunchedEffect(series.id, attempt) {
        state = DetailsState.Loading
        state = try {
            val details = plugin.getSeries(series.id)
            val chapters = plugin.getChapters(series.id)
            DetailsState.Loaded(details, chapters)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DetailsState.Failed(e.message ?: "Could not load this series")
        }
    }

    ScreenScaffold(title = series.title, onBack = onBack) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val current = state) {
                DetailsState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DetailsState.Failed -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(current.message, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { attempt++ }) { Text("Retry") }
                }
                is DetailsState.Loaded -> {
                    val store = application.progressStore
                    val listPrefs = application.chapterListPreferences
                    val pluginId = plugin.manifest.id

                    // Reading order, first chapter first. Also the order Next/Previous follow.
                    val ordered = remember(current.chapters, listPrefs.sort) {
                        if (listPrefs.sort == ChapterSort.ByNumber) {
                            current.chapters.sortedWith(
                                compareBy<ChapterInfo> { it.number == null }.thenBy { it.number },
                            )
                        } else {
                            current.chapters
                        }
                    }
                    // Positions in [ordered] to show, in display order.
                    val shown = (if (listPrefs.order == ChapterOrder.NewestFirst) ordered.indices.reversed() else ordered.indices)
                        .filter { !listPrefs.onlyUnread || !store.isFinished(pluginId, ordered[it].id) }

                    fun open(position: Int) {
                        application.readerSession = ReaderSession(
                            plugin = plugin,
                            seriesTitle = current.details.title,
                            chapters = ordered,
                            startIndex = position,
                        )
                        onOpenReader()
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Header(
                                details = current.details,
                                allowedDomains = plugin.manifest.permissions.network,
                            )
                        }
                        item {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                if (ordered.isEmpty()) {
                                    Text(
                                        "No readable chapters here. This title's chapters may be " +
                                            "hosted on another site.",
                                    )
                                } else {
                                    val resume = resumeIndex(ordered, store, pluginId)
                                    val started = store.lastPage(pluginId, ordered[resume].id) != null
                                    Button(
                                        onClick = { open(resume) },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(
                                            (if (started) "Continue: " else "Start: ") +
                                                chapterTitle(ordered[resume], listPrefs.naming),
                                            maxLines = 1,
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Chapters (${ordered.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                        items(count = shown.size, key = { ordered[shown[it]].id }) { row ->
                            val position = shown[row]
                            val chapter = ordered[position]
                            ListItem(
                                headlineContent = { Text(chapterTitle(chapter, listPrefs.naming), maxLines = 2) },
                                supportingContent = {
                                    val label = progressLabel(store, pluginId, chapter.id)
                                    if (label != null) Text(label)
                                },
                                modifier = Modifier.clickable { open(position) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(details: SeriesDetails, allowedDomains: List<String>) {
    Column(Modifier.padding(16.dp)) {
        Row {
            RemoteImage(
                url = details.coverUrl,
                allowedDomains = allowedDomains,
                modifier = Modifier.size(width = 110.dp, height = 160.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(details.title, style = MaterialTheme.typography.titleLarge)
                if (details.tags.isNotEmpty()) {
                    Text(details.tags.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        details.description?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 8)
        }
    }
}

/** The name shown for a chapter: the source's title, or just "Chapter N". */
private fun chapterTitle(chapter: ChapterInfo, naming: ChapterNaming): String {
    val number = chapter.number
    if (naming == ChapterNaming.NumberOnly && number != null) {
        val text = if (number % 1f == 0f) number.toInt().toString() else number.toString()
        return "Chapter $text"
    }
    return chapter.name
}

/** Where "Continue" should start: the unfinished chapter you left, or the one after the last finished. */
private fun resumeIndex(
    chapters: List<ChapterInfo>,
    store: ReadingProgressStore,
    pluginId: String,
): Int {
    val lastTouched = chapters.indexOfLast {
        store.lastPage(pluginId, it.id) != null || store.isFinished(pluginId, it.id)
    }
    if (lastTouched < 0) return 0
    return if (store.isFinished(pluginId, chapters[lastTouched].id)) {
        (lastTouched + 1).coerceAtMost(chapters.lastIndex)
    } else {
        lastTouched
    }
}

private fun progressLabel(store: ReadingProgressStore, pluginId: String, chapterId: String): String? =
    when {
        store.isFinished(pluginId, chapterId) -> "Finished"
        else -> store.lastPage(pluginId, chapterId)?.let { "Stopped at page ${it + 1}" }
    }
