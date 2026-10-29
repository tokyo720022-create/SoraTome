package io.github.tokyo720022create.soratome.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.plugin.runtime.network.NetworkPolicy
import kotlinx.coroutines.CancellationException

/**
 * Cover-style image loaded through the app's image loader, limited to [allowedDomains].
 * Shows a plain placeholder while loading or if the image cannot be loaded.
 */
@Composable
fun RemoteImage(
    url: String?,
    allowedDomains: List<String>,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val application = LocalContext.current.applicationContext as SoraTomeApplication
    var image by remember(url) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        image = null
        if (url != null) {
            image = try {
                application.imageLoader
                    .load(url, NetworkPolicy(allowedDomains), maxWidthPx = COVER_MAX_WIDTH_PX)
                    .asImageBitmap()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        image?.let { loaded ->
            Image(
                bitmap = loaded,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        }
    }
}

private const val COVER_MAX_WIDTH_PX = 600
