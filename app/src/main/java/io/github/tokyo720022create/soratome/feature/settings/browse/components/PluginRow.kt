package io.github.tokyo720022create.soratome.feature.settings.browse.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.tokyo720022create.soratome.data.plugins.PluginEntry

/**
 * One plugin in the installed list: name, version, content warning, a switch, and (if installed)
 * a remove button. [blocked] means the content warning settings keep it from loading.
 */
@Composable
fun PluginRow(
    entry: PluginEntry,
    blocked: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onUninstall: () -> Unit,
) {
    val manifest = entry.manifest
    val kind = if (entry.bundled) "bundled with this debug build" else "installed"

    ListItem(
        headlineContent = { Text(manifest.name) },
        supportingContent = {
            Column {
                Text("Version ${manifest.version} · ${manifest.contentWarning.label} · $kind")
                if (blocked) {
                    Text(
                        "Not loaded: hidden by your content warning settings",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!entry.bundled) {
                    IconButton(onClick = onUninstall) {
                        Icon(Icons.Filled.Delete, contentDescription = "Uninstall ${manifest.name}")
                    }
                }
                Switch(checked = entry.enabled, onCheckedChange = onEnabledChange)
            }
        },
    )
}
