package io.github.tokyo720022create.soratome.feature.settings.browse.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.feature.settings.browse.PendingInstall

/**
 * Asks before a plugin is installed or updated, and shows what it is allowed to do:
 * which websites it can contact and whether it can keep its own settings.
 */
@Composable
fun InstallPluginDialog(
    pending: PendingInstall,
    installedVersion: String?,
    busy: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val manifest = pending.pkg.manifest
    val isUpdate = installedVersion != null

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(if (isUpdate) "Update plugin?" else "Install plugin?") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(manifest.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Version ${manifest.version}" + (installedVersion?.let { " (now $it)" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(manifest.id, style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(12.dp))
                Text("From", style = MaterialTheme.typography.labelLarge)
                Text(pending.origin, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(12.dp))
                Text("This plugin can contact", style = MaterialTheme.typography.labelLarge)
                if (manifest.permissions.network.isEmpty()) {
                    Text("No websites", style = MaterialTheme.typography.bodyMedium)
                } else {
                    manifest.permissions.network.forEach { domain ->
                        Text("• $domain", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (manifest.permissions.storage) {
                    Spacer(Modifier.height(8.dp))
                    Text("It can also keep its own settings.", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Only install plugins from sources you trust.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !busy) { Text(if (isUpdate) "Update" else "Install") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        },
    )
}
