package io.github.tokyo720022create.soratome.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/** A settings row with a title, an explanation and a drop-down list of choices. */
@Composable
fun <T> DropdownRow(
    title: String,
    summary: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        trailingContent = {
            Box {
                OutlinedButton(onClick = { expanded = true }) { Text(label(selected)) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(label(option)) },
                            onClick = {
                                onSelect(option)
                                expanded = false
                            },
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}
