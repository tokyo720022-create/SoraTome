package io.github.tokyo720022create.soratome.feature.reader.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.tokyo720022create.soratome.data.image.PageImageLoader
import io.github.tokyo720022create.soratome.domain.reader.ImageQuality
import io.github.tokyo720022create.soratome.plugin.api.source.PageInfo
import io.github.tokyo720022create.soratome.plugin.runtime.network.NetworkPolicy

/**
 * One page at a time, swiped sideways.
 * With [rightToLeft] the first page is on the right and you swipe left to go forward.
 */
@Composable
fun PagedReader(
    pages: List<PageInfo>,
    pagerState: PagerState,
    rightToLeft: Boolean,
    quality: ImageQuality,
    policy: NetworkPolicy,
    loader: PageImageLoader,
    maxWidthPx: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pagerState,
        reverseLayout = rightToLeft,
        modifier = modifier.fillMaxSize(),
    ) { index ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PageImage(
                page = pages[index],
                quality = quality,
                policy = policy,
                loader = loader,
                maxWidthPx = maxWidthPx,
                fillWidth = false,
                onRetry = onRetry,
            )
        }
    }
}
