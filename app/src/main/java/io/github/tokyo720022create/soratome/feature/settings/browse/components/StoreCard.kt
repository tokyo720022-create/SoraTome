package io.github.tokyo720022create.soratome.feature.settings.browse.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.data.plugins.StoreInfo
import io.github.tokyo720022create.soratome.feature.settings.browse.hostOf

/** A plugin store: its name, address and plugin count, with Website, Support, Copy and Remove. */
@Composable
fun StoreCard(
    store: StoreInfo,
    onOpen: () -> Unit,
    onOpenWebsite: () -> Unit,
    onOpenSupport: () -> Unit,
    onCopyLink: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 4.dp)) {
            Text(text = store.name ?: hostOf(store.url), style = MaterialTheme.typography.titleMedium)
            Text(
                text = store.url,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            store.pluginCount?.let { count ->
                Text(
                    text = if (count == 1) "1 plugin" else "$count plugins",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (store.website != null) TextButton(onClick = onOpenWebsite) { Text("Website") }
                if (store.support != null) TextButton(onClick = onOpenSupport) { Text("Support") }
                TextButton(onClick = onCopyLink) { Text("Copy") }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove store")
                }
            }
        }
    }
}
