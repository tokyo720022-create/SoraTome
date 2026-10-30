package io.github.tokyo720022create.soratome.feature.reader

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.domain.reader.ImageQuality
import io.github.tokyo720022create.soratome.domain.reader.ReadingMode
import io.github.tokyo720022create.soratome.domain.reader.ScreenRotation
import io.github.tokyo720022create.soratome.feature.reader.components.AutoReadPill
import io.github.tokyo720022create.soratome.feature.reader.components.ClockBatteryBadge
import io.github.tokyo720022create.soratome.feature.reader.components.OperationTipsDialog
import io.github.tokyo720022create.soratome.feature.reader.components.PagedReader
import io.github.tokyo720022create.soratome.feature.reader.components.ReaderBottomBar
import io.github.tokyo720022create.soratome.feature.reader.components.ReaderChapterReminder
import io.github.tokyo720022create.soratome.feature.reader.components.ReaderOptionsSheet
import io.github.tokyo720022create.soratome.feature.reader.components.ReaderTopBar
import io.github.tokyo720022create.soratome.feature.reader.components.VerticalReader
import io.github.tokyo720022create.soratome.feature.reader.zoom.ZoomState
import io.github.tokyo720022create.soratome.feature.reader.zoom.onTouchDown
import io.github.tokyo720022create.soratome.feature.reader.zoom.zoomGestures
import io.github.tokyo720022create.soratome.plugin.api.source.PageInfo
import io.github.tokyo720022create.soratome.plugin.runtime.network.NetworkPolicy
import io.github.tokyo720022create.soratome.ui.findActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen chapter reader.
 *
 * Modes: left to right, right to left (paged) and vertical scroll.
 * Tap the left or right edge to turn pages, tap the middle to show or hide the controls,
 * pinch or double-tap to zoom.
 */
@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    onOpenReaderSettings: () -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as SoraTomeApplication
    val preferences = application.readerPreferences
    val session = application.readerSession
    val view = LocalView.current
    val activity = context.findActivity()

    // Keep the screen awake while reading.
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Screen rotation option.
    val rotation = preferences.screenRotation
    DisposableEffect(rotation, activity) {
        activity?.requestedOrientation = when (rotation) {
            ScreenRotation.Auto -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            ScreenRotation.Portrait -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ScreenRotation.Landscape -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    // Show status bar option.
    val showStatusBar = preferences.showStatusBar
    DisposableEffect(showStatusBar, activity, view) {
        val window = activity?.window
        val controller = if (window != null) WindowCompat.getInsetsController(window, view) else null
        if (!showStatusBar) {
            controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.statusBars())
        }
        onDispose { controller?.show(WindowInsetsCompat.Type.statusBars()) }
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
                        if (session.hasNext(preferences.skipExtras)) {
                            TextButton(onClick = { session.goToNext(preferences.skipExtras) }) {
                                Text("Next chapter")
                            }
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
                onOpenReaderSettings = onOpenReaderSettings,
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
    onOpenReaderSettings: () -> Unit,
) {
    val context = LocalContext.current
    val preferences = application.readerPreferences
    val loader = application.imageLoader
    val store = application.progressStore
    val pluginId = session.plugin.manifest.id
    val chapter = session.chapter
    val mode = preferences.readingMode
    val quality = preferences.imageQuality
    val skipExtras = preferences.skipExtras
    val autoSpeed = preferences.autoReadSpeed
    val policy = remember(pluginId) { NetworkPolicy(session.plugin.manifest.permissions.network) }
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val maxWidthPx = with(density) { (configuration.screenWidthDp.dp * 2).roundToPx() }

    var uiVisible by remember { mutableStateOf(true) }
    var showOptions by remember { mutableStateOf(false) }
    var showTips by remember { mutableStateOf(!preferences.tipsSeen) }
    var showReminder by remember { mutableStateOf(false) }
    var autoRead by remember { mutableStateOf(false) }
    var autoPaused by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(startPage) }
    val zoom = remember { ZoomState() }
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { pages.size })
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startPage)

    // Keep the page number in sync with whichever reader is showing, and carry the
    // position over when the reading mode changes.
    LaunchedEffect(mode) {
        zoom.reset()
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

    // A new page starts at normal size (the vertical mode keeps its zoom while scrolling).
    LaunchedEffect(currentPage) {
        if (mode != ReadingMode.Vertical) zoom.reset()
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

    // Chapter reminder: show the chapter name for a moment when it opens.
    LaunchedEffect(chapter.id) {
        if (preferences.chapterReminder) {
            showReminder = true
            delay(2_000)
            showReminder = false
        }
    }

    val forward: () -> Unit = {
        if (pagerState.currentPage < pages.lastIndex) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        } else if (session.hasNext(skipExtras)) {
            session.goToNext(skipExtras)
        }
    }
    val backward: () -> Unit = {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        } else if (session.hasPrevious(skipExtras)) {
            session.goToPrevious(skipExtras)
        }
    }

    // Volume keys (only while the option is on).
    val volumeStep: (Boolean) -> Unit = { goForward ->
        if (mode == ReadingMode.Vertical) {
            val distance = with(density) { configuration.screenHeightDp.dp.toPx() } * 0.85f
            scope.launch { listState.animateScrollBy(if (goForward) distance else -distance) }
        } else if (goForward) {
            forward()
        } else {
            backward()
        }
    }
    val latestVolumeStep by rememberUpdatedState(volumeStep)
    DisposableEffect(preferences.volumeKeysTurnPages) {
        if (preferences.volumeKeysTurnPages) {
            ReaderKeyEvents.onVolumeKey = { goForward -> latestVolumeStep(goForward) }
        }
        onDispose { ReaderKeyEvents.onVolumeKey = null }
    }

    // Auto read: scroll smoothly in vertical mode, turn pages on a timer otherwise.
    LaunchedEffect(autoRead, autoPaused, mode, autoSpeed) {
        if (autoRead && !autoPaused && mode == ReadingMode.Vertical) {
            val pixelsPerSecond = autoSpeed.scrollDpPerSecond * density.density
            var last = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                val seconds = (now - last) / 1_000_000_000f
                last = now
                listState.scrollBy(pixelsPerSecond * seconds)
                if (!listState.canScrollForward) {
                    autoRead = false
                    break
                }
            }
        }
    }
    LaunchedEffect(autoRead, autoPaused, mode, autoSpeed, currentPage) {
        if (autoRead && !autoPaused && mode != ReadingMode.Vertical) {
            delay(autoSpeed.pageSeconds * 1_000L)
            if (currentPage < pages.lastIndex) forward() else autoRead = false
        }
    }

    // Tap zones: left and right edges turn pages (reversed in right-to-left mode),
    // the middle shows or hides the controls. While zoomed, and in vertical mode,
    // any tap toggles the controls.
    fun handleTap(fraction: Float) {
        when {
            zoom.isZoomed || mode == ReadingMode.Vertical -> uiVisible = !uiVisible
            fraction < 0.3f -> if (mode == ReadingMode.RightToLeft) forward() else backward()
            fraction > 0.7f -> if (mode == ReadingMode.RightToLeft) backward() else forward()
            else -> uiVisible = !uiVisible
        }
    }

    val shareChapter: () -> Unit = {
        val link = chapter.url
        if (link != null) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${session.seriesTitle} - ${chapter.name}\n$link")
            }
            context.startActivity(Intent.createChooser(send, "Share chapter link"))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onTouchDown { if (autoRead) autoPaused = true }
            .pointerInput(mode) {
                detectTapGestures(
                    onDoubleTap = { offset -> zoom.toggle(offset, size) },
                    onTap = { offset -> handleTap(offset.x / size.width) },
                )
            }
            .zoomGestures(zoom, mode) { overflow ->
                // Zoomed in on a long strip: when the drag runs past the edge, keep scrolling.
                if (mode == ReadingMode.Vertical) listState.dispatchRawDelta(-overflow.y / zoom.scale)
            }
            .clipToBounds(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoom.scale
                    scaleY = zoom.scale
                    translationX = zoom.translation.x
                    translationY = zoom.translation.y
                },
        ) {
            if (mode == ReadingMode.Vertical) {
                VerticalReader(
                    pages = pages,
                    listState = listState,
                    userScrollEnabled = !zoom.isZoomed,
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
                    pageFit = preferences.pageFit,
                    userScrollEnabled = !zoom.isZoomed,
                    quality = quality,
                    policy = policy,
                    loader = loader,
                    maxWidthPx = maxWidthPx,
                    onRetry = onRefreshPages,
                )
            }
        }
    }

    if (showReminder) {
        ReaderChapterReminder(
            chapterName = chapter.name,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 88.dp),
        )
    }

    if (preferences.showTimeAndBattery && !uiVisible) {
        ClockBatteryBadge(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
    }

    if (autoRead) {
        AutoReadPill(
            speedLabel = autoSpeed.label,
            paused = autoPaused,
            onTogglePause = { autoPaused = !autoPaused },
            onStop = {
                autoRead = false
                autoPaused = false
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (uiVisible) 120.dp else 24.dp),
        )
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
            hasPreviousChapter = session.hasPrevious(skipExtras),
            hasNextChapter = session.hasNext(skipExtras),
            onPreviousChapter = { session.goToPrevious(skipExtras) },
            onNextChapter = { session.goToNext(skipExtras) },
            onSeek = { page ->
                scope.launch {
                    if (mode == ReadingMode.Vertical) listState.scrollToItem(page) else pagerState.scrollToPage(page)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showOptions) {
        ReaderOptionsSheet(
            preferences = preferences,
            autoReadActive = autoRead,
            onToggleAutoRead = {
                autoRead = !autoRead
                autoPaused = false
                showOptions = false
                if (autoRead) uiVisible = false
            },
            canShareChapter = chapter.url != null,
            onShareChapter = shareChapter,
            onShowTips = {
                showOptions = false
                showTips = true
            },
            onMoreSettings = {
                showOptions = false
                onOpenReaderSettings()
            },
            onDismiss = { showOptions = false },
        )
    }

    if (showTips) {
        OperationTipsDialog(
            onDismiss = {
                showTips = false
                preferences.updateTipsSeen(true)
            },
        )
    }
}
