package io.github.tokyo720022create.soratome.feature.reader.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.data.image.PageImageLoader
import io.github.tokyo720022create.soratome.domain.reader.ImageQuality
import io.github.tokyo720022create.soratome.plugin.api.source.PageInfo
import io.github.tokyo720022create.soratome.plugin.runtime.network.NetworkPolicy
import kotlinx.coroutines.CancellationException

private sealed interface PageLoadState {
    data object Loading : PageLoadState
    data class Loaded(val image: ImageBitmap) : PageLoadState
    data class Failed(val message: String) : PageLoadState
}

/**
 * One page of a chapter. Loads the image, shows progress, and shows the reason plus a
 * Retry button when loading fails.
 *
 * @param fillWidth Vertical mode: the page is as wide as the screen and as tall as it needs to be.
 * Otherwise the whole page is fitted inside the available space.
 * @param onRetry Called when the user taps Retry, so the caller can refresh expired addresses.
 */
@Composable
fun PageImage(
    page: PageInfo,
    quality: ImageQuality,
    policy: NetworkPolicy,
    loader: PageImageLoader,
    maxWidthPx: Int,
    fillWidth: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val url = if (quality == ImageQuality.DataSaver) page.lowQualityUrl ?: page.url else page.url
    var attempt by remember(url) { mutableIntStateOf(0) }
    var state by remember(url) { mutableStateOf<PageLoadState>(PageLoadState.Loading) }

    LaunchedEffect(url, attempt) {
        state = PageLoadState.Loading
        state = try {
            PageLoadState.Loaded(loader.load(url, policy, maxWidthPx, page.reportUrl).asImageBitmap())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            PageLoadState.Failed(e.message ?: "Could not load this page")
        }
    }

    when (val current = state) {
        PageLoadState.Loading -> Box(
            modifier = modifier.fillMaxWidth().heightIn(min = if (fillWidth) 400.dp else 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Color.White)
        }

        is PageLoadState.Failed -> Column(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = if (fillWidth) 400.dp else 0.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = current.message,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    attempt++
                    onRetry()
                },
            ) {
                Text("Retry")
            }
        }

        is PageLoadState.Loaded -> Image(
            bitmap = current.image,
            contentDescription = "Page ${page.index + 1}",
            modifier = if (fillWidth) modifier.fillMaxWidth() else modifier.fillMaxSize(),
            contentScale = if (fillWidth) ContentScale.FillWidth else ContentScale.Fit,
        )
    }
}
