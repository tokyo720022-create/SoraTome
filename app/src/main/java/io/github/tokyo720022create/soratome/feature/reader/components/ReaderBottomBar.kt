package io.github.tokyo720022create.soratome.feature.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Page slider, page counter and previous/next chapter buttons. */
@Composable
fun ReaderBottomBar(
    currentPage: Int,
    pageCount: Int,
    rightToLeft: Boolean,
    hasPreviousChapter: Boolean,
    hasNextChapter: Boolean,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onSeek: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = Color.Black.copy(alpha = 0.8f), modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (pageCount > 1) {
                // In right-to-left mode the slider also runs right to left.
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (rightToLeft) LayoutDirection.Rtl else LayoutDirection.Ltr,
                ) {
                    Slider(
                        value = currentPage.toFloat(),
                        onValueChange = { onSeek(it.roundToInt()) },
                        valueRange = 0f..(pageCount - 1).toFloat(),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onPreviousChapter, enabled = hasPreviousChapter) {
                    Text("Previous")
                }
                Text(text = "${currentPage + 1} / $pageCount", color = Color.White)
                TextButton(onClick = onNextChapter, enabled = hasNextChapter) {
                    Text("Next")
                }
            }
        }
    }
}
