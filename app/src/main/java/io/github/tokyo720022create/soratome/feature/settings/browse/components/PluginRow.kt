package io.github.tokyo720022create.soratome.feature.settings.browse.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.tokyo720022create.soratome.data.plugins.PluginEntry

/** One plugin in the installed list: name, version, a switch, and (if installed) a remove button. */
@Composable
fun PluginRow(
    entry: PluginEntry,
    onEnabledChange: (Boolean) -> Unit,
    onUninstall: () -> Unit,
) {
    val manifest = entry.manifest
    val kind = if (entry.bundled) "bundled with this debug build" else "installed"

    ListItem(
        headlineContent = { Text(manifest.name) },
        supportingContent = { Text("Version ${manifest.version} · $kind") },
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
