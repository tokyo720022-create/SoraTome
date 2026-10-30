package io.github.tokyo720022create.soratome.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** A settings row with a title, a one-line explanation and an on/off switch. */
@Composable
fun SwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        modifier = modifier.clickable { onCheckedChange(!checked) },
    )
}
