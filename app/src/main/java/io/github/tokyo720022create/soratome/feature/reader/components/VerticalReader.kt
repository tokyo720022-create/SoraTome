package io.github.tokyo720022create.soratome.feature.reader.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.tokyo720022create.soratome.data.image.PageImageLoader
import io.github.tokyo720022create.soratome.domain.reader.ImageQuality
import io.github.tokyo720022create.soratome.plugin.api.source.PageInfo
import io.github.tokyo720022create.soratome.plugin.runtime.network.NetworkPolicy

/** All pages in one continuous vertical scroll, for webtoons and long strips. */
@Composable
fun VerticalReader(
    pages: List<PageInfo>,
    listState: LazyListState,
    userScrollEnabled: Boolean,
    quality: ImageQuality,
    policy: NetworkPolicy,
    loader: PageImageLoader,
    maxWidthPx: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        userScrollEnabled = userScrollEnabled,
        modifier = modifier.fillMaxSize(),
    ) {
        items(count = pages.size, key = { it }) { index ->
            PageImage(
                page = pages[index],
                quality = quality,
                policy = policy,
                loader = loader,
                maxWidthPx = maxWidthPx,
                fillWidth = true,
                onRetry = onRetry,
            )
        }
    }
}
