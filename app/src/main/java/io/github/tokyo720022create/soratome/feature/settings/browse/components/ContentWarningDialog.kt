package io.github.tokyo720022create.soratome.feature.settings.browse.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.data.plugins.PluginPreferences
import io.github.tokyo720022create.soratome.plugin.api.manifest.ContentWarning

/** Choose which content warnings are allowed. Turning on 18+ asks for confirmation first. */
@Composable
fun ContentWarningDialog(
    preferences: PluginPreferences,
    onAskAdult: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allowed content warnings") },
        text = {
            Column {
                Text(
                    "Plugins and stores label themselves. Plugins with a label that is off are " +
                        "hidden in stores and cannot be installed.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                ContentWarning.entries.forEach { warning ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val turnOn = !preferences.isAllowed(warning)
                                if (warning == ContentWarning.ADULT && turnOn) {
                                    onAskAdult()
                                } else {
                                    preferences.setAllowed(warning, turnOn)
                                }
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = preferences.isAllowed(warning), onCheckedChange = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(warning.label, style = MaterialTheme.typography.bodyLarge)
                            Text(warning.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}
