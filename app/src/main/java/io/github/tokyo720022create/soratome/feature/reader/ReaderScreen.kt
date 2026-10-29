package io.github.tokyo720022create.soratome.feature.reader

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.domain.reader.ImageQuality
import io.github.tokyo720022create.soratome.domain.reader.ReadingMode
import io.github.tokyo720022create.soratome.feature.reader.components.PagedReader
import io.github.tokyo720022create.soratome.feature.reader.components.ReaderBottomBar
import io.github.tokyo720022create.soratome.feature.reader.components.ReaderOptionGroups
import io.github.tokyo720022create.soratome.feature.reader.components.ReaderTopBar
import io.github.tokyo720022create.soratome.feature.reader.components.VerticalReader
import io.github.tokyo720022create.soratome.plugin.api.source.PageInfo
import io.github.tokyo720022create.soratome.plugin.runtime.network.NetworkPolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Full-screen chapter reader.
 * Modes: left to right, right to left (paged) and vertical scroll.
 * Tap the left or right edge to turn pages, tap the middle to show or hide the controls.
 */
@Composable
fun ReaderScreen(onBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as SoraTomeApplication
    val session = application.readerSession

    // Keep the screen awake while reading.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    if (session == null) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nothing to read.", color = Color.White)
                TextButton(onClick = onBack) { Text("Back") }
            }
        }
        return
    }

    val chapter = session.chapter
    val pluginId = session.plugin.manifest.id
    val store = application.progressStore
    val scope = rememberCoroutineScope()

    var pages by remember(chapter.id) { mutableStateOf<List<PageInfo>?>(null) }
    var error by remember(chapter.id) { mutableStateOf<String?>(null) }
    var attempt by remember(chapter.id) { mutableIntStateOf(0) }
    var lastRefresh by remember(chapter.id) { mutableLongStateOf(0L) }
    val startPage = remember(chapter.id) {
        if (store.isFinished(pluginId, chapter.id)) 0 else store.lastPage(pluginId, chapter.id) ?: 0
    }

    LaunchedEffect(chapter.id, attempt) {
        pages = null
        error = null
        try {
            pages = session.plugin.getPages(chapter.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Could not load this chapter"
        }
    }

    // Page addresses can expire. Retry on a page asks for fresh ones, at most every 5 seconds.
    val refreshPages: () -> Unit = {
        val now = SystemClock.elapsedRealtime()
        if (now - lastRefresh > 5_000) {
            lastRefresh = now
            scope.launch {
                try {
                    pages = session.plugin.getPages(chapter.id)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Keep the old addresses; each page shows its own error.
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val loadedPages = pages
        val problem = error ?: if (loadedPages != null && loadedPages.isEmpty()) "This chapter has no pages." else null

        when {
            problem != null -> {
                ReaderTopBar(
                    seriesTitle = session.seriesTitle,
                    chapterName = chapter.name,
                    onBack = onBack,
                    onOptions = {},
                    modifier = Modifier.align(Alignment.TopCenter),
                )
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(problem, color = Color.White, textAlign = TextAlign.Center)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { attempt++ }) { Text("Retry") }
                        if (session.hasNext) {
                            TextButton(onClick = { session.goToNext() }) { Text("Next chapter") }
                        }
                    }
                }
            }

            loadedPages == null -> {
                ReaderTopBar(
                    seriesTitle = session.seriesTitle,
                    chapterName = chapter.name,
                    onBack = onBack,
                    onOptions = {},
                    modifier = Modifier.align(Alignment.TopCenter),
                )
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                )
            }

            else -> ReaderContent(
                application = application,
                session = session,
                pages = loadedPages,
                startPage = startPage.coerceIn(0, loadedPages.lastIndex),
                onBack = onBack,
                onRefreshPages = refreshPages,
            )
        }
    }
}

@Composable
private fun BoxScope.ReaderContent(
    application: SoraTomeApplication,
    session: ReaderSession,
    pages: List<PageInfo>,
    startPage: Int,
    onBack: () -> Unit,
    onRefreshPages: () -> Unit,
) {
    val preferences = application.readerPreferences
    val loader = application.imageLoader
    val store = application.progressStore
    val pluginId = session.plugin.manifest.id
    val chapter = session.chapter
    val mode = preferences.readingMode
    val quality = preferences.imageQuality
    val policy = remember(pluginId) { NetworkPolicy(session.plugin.manifest.permissions.network) }
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val maxWidthPx = with(density) { (configuration.screenWidthDp.dp * 2).roundToPx() }

    var uiVisible by remember { mutableStateOf(true) }
    var showOptions by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(startPage) }
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { pages.size })
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startPage)

    // Keep the page number in sync with whichever reader is showing, and carry the
    // position over when the reading mode changes.
    LaunchedEffect(mode) {
        if (mode == ReadingMode.Vertical) {
            listState.scrollToItem(currentPage)
            launch {
                snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                    .collect { lastVisible ->
                        if (lastVisible != null && lastVisible >= pages.lastIndex) {
                            store.markFinished(pluginId, chapter.id)
                        }
                    }
            }
            snapshotFlow { listState.firstVisibleItemIndex }.collect { currentPage = it }
        } else {
            pagerState.scrollToPage(currentPage)
            snapshotFlow { pagerState.currentPage }.collect { currentPage = it }
        }
    }

    // Remember where you stopped, and mark the chapter finished on its last page.
    LaunchedEffect(currentPage) {
        store.savePage(pluginId, chapter.id, currentPage)
        if (currentPage >= pages.lastIndex) store.markFinished(pluginId, chapter.id)
    }

    // Load the next two pages ahead of time.
    LaunchedEffect(currentPage, quality) {
        for (offset in 1..2) {
            val next = pages.getOrNull(currentPage + offset) ?: break
            val url = if (quality == ImageQuality.DataSaver) next.lowQualityUrl ?: next.url else next.url
            launch {
                try {
                    loader.load(url, policy, maxWidthPx, next.reportUrl)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // The page shows its own error when it is opened.
                }
            }
        }
    }

    val forward: () -> Unit = {
        if (pagerState.currentPage < pages.lastIndex) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        } else if (session.hasNext) {
            session.goToNext()
        }
    }
    val backward: () -> Unit = {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        } else if (session.hasPrevious) {
            session.goToPrevious()
        }
    }

    // Tap zones: left and right edges turn pages (reversed in right-to-left mode),
    // the middle shows or hides the controls. In vertical mode any tap toggles them.
    fun handleTap(fraction: Float) {
        when {
            mode == ReadingMode.Vertical -> uiVisible = !uiVisible
            fraction < 0.3f -> if (mode == ReadingMode.RightToLeft) forward() else backward()
            fraction > 0.7f -> if (mode == ReadingMode.RightToLeft) backward() else forward()
            else -> uiVisible = !uiVisible
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(mode) {
                detectTapGestures(onTap = { offset -> handleTap(offset.x / size.width) })
            },
    ) {
        if (mode == ReadingMode.Vertical) {
            VerticalReader(
                pages = pages,
                listState = listState,
                quality = quality,
                policy = policy,
                loader = loader,
                maxWidthPx = maxWidthPx,
                onRetry = onRefreshPages,
            )
        } else {
            PagedReader(
                pages = pages,
                pagerState = pagerState,
                rightToLeft = mode == ReadingMode.RightToLeft,
                quality = quality,
                policy = policy,
                loader = loader,
                maxWidthPx = maxWidthPx,
                onRetry = onRefreshPages,
            )
        }
    }

    if (uiVisible) {
        ReaderTopBar(
            seriesTitle = session.seriesTitle,
            chapterName = chapter.name,
            onBack = onBack,
            onOptions = { showOptions = true },
            modifier = Modifier.align(Alignment.TopCenter),
        )
        ReaderBottomBar(
            currentPage = currentPage,
            pageCount = pages.size,
            rightToLeft = mode == ReadingMode.RightToLeft,
            hasPreviousChapter = session.hasPrevious,
            hasNextChapter = session.hasNext,
            onPreviousChapter = { session.goToPrevious() },
            onNextChapter = { session.goToNext() },
            onSeek = { page ->
                scope.launch {
                    if (mode == ReadingMode.Vertical) listState.scrollToItem(page) else pagerState.scrollToPage(page)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Reader options") },
            text = { ReaderOptionGroups(preferences = preferences) },
            confirmButton = { TextButton(onClick = { showOptions = false }) { Text("Close") } },
        )
    }
}
