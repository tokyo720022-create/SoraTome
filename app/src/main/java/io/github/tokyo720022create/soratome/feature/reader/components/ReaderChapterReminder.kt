package io.github.tokyo720022create.soratome.feature.reader.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Brief label with the chapter name, shown when a chapter opens. */
@Composable
fun ReaderChapterReminder(chapterName: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.8f),
        modifier = modifier,
    ) {
        Text(
            text = chapterName,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
