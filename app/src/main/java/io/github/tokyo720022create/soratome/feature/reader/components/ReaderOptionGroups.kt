package io.github.tokyo720022create.soratome.feature.reader.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.data.reader.ReaderPreferences
import io.github.tokyo720022create.soratome.domain.reader.ImageQuality
import io.github.tokyo720022create.soratome.domain.reader.ReadingMode

/** Reading mode and image quality choices. Used by the reader's options dialog and Settings > Reader. */
@Composable
fun ReaderOptionGroups(preferences: ReaderPreferences, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        GroupTitle("Reading mode")
        ReadingMode.entries.forEach { mode ->
            OptionRow(
                label = mode.label,
                selected = preferences.readingMode == mode,
                onSelect = { preferences.updateReadingMode(mode) },
            )
        }
        GroupTitle("Image quality")
        ImageQuality.entries.forEach { quality ->
            OptionRow(
                label = quality.label,
                selected = preferences.imageQuality == quality,
                onSelect = { preferences.updateImageQuality(quality) },
            )
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
