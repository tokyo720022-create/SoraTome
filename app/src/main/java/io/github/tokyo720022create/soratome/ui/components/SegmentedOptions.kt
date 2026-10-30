package io.github.tokyo720022create.soratome.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** A row of equal-width buttons where exactly one is selected. */
@Composable
fun <T> SegmentedOptions(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val buttonModifier = Modifier.weight(1f)
            if (option == selected) {
                FilledTonalButton(onClick = { onSelect(option) }, modifier = buttonModifier) {
                    Text(label(option), textAlign = TextAlign.Center, maxLines = 2)
                }
            } else {
                OutlinedButton(onClick = { onSelect(option) }, modifier = buttonModifier) {
                    Text(label(option), textAlign = TextAlign.Center, maxLines = 2)
                }
            }
        }
    }
}
