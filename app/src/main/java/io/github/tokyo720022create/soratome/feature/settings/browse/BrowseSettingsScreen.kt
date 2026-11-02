package io.github.tokyo720022create.soratome.feature.settings.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.feature.settings.browse.components.ContentWarningDialog
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsScaffold
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsSectionHeader
import io.github.tokyo720022create.soratome.plugin.api.manifest.ContentWarning
import io.github.tokyo720022create.soratome.ui.components.SwitchRow
import java.net.URL

/** Settings > Browse: plugin stores, installed plugins and the content warning filter. */
@Composable
fun BrowseSettingsScreen(
    onBack: () -> Unit,
    onOpenStores: () -> Unit,
    onOpenPlugins: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as SoraTomeApplication
    val manager = application.pluginManager
    val preferences = application.pluginPreferences

    var showWarnings by remember { mutableStateOf(false) }
    var confirmAdult by remember { mutableStateOf(false) }

    val storeCount = manager.stores.size
    val storesSummary = when (storeCount) {
        0 -> "No stores yet"
        1 -> "1 store"
        else -> "$storeCount stores"
    }
    val pluginCount = manager.entries.size
    val pluginsSummary = if (pluginCount == 1) "1 plugin" else "$pluginCount plugins"

    SettingsScaffold(title = "Browse", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionHeader("Plugins")
            ListItem(
                headlineContent = { Text("Plugin stores") },
                supportingContent = { Text(storesSummary) },
                modifier = Modifier.clickable(onClick = onOpenStores),
            )
            ListItem(
                headlineContent = { Text("Installed plugins") },
                supportingContent = { Text("$pluginsSummary · install from a file or link, update, remove") },
                modifier = Modifier.clickable(onClick = onOpenPlugins),
            )

            SettingsSectionHeader("Content")
            ListItem(
                headlineContent = { Text("Allowed content warnings") },
                supportingContent = { Text(preferences.summary()) },
                modifier = Modifier.clickable { showWarnings = true },
            )
            SwitchRow(
                title = "Also apply to installed plugins",
                summary = "When off, plugins you already installed keep loading and updating " +
                    "regardless of their warning",
                checked = preferences.applyToInstalled,
                onCheckedChange = { preferences.updateApplyToInstalled(it) },
            )

            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Installed plugins that aren't loaded stay listed without their sources; " +
                        "plugins you haven't installed are hidden in stores. This does not prevent " +
                        "plugins that label themselves wrongly from showing 18+ content in the app.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    if (showWarnings) {
        ContentWarningDialog(
            preferences = preferences,
            onAskAdult = { confirmAdult = true },
            onDismiss = { showWarnings = false },
        )
    }

    if (confirmAdult) {
        AlertDialog(
            onDismissRequest = { confirmAdult = false },
            title = { Text("Allow 18+ plugins?") },
            text = { Text("18+ plugins can show adult content. Turn this on only if you are an adult.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        preferences.setAllowed(ContentWarning.ADULT, true)
                        confirmAdult = false
                    },
                ) {
                    Text("Allow")
                }
            },
            dismissButton = { TextButton(onClick = { confirmAdult = false }) { Text("Cancel") } },
        )
    }
}

internal fun hostOf(url: String): String =
    try {
        URL(url).host
    } catch (e: Exception) {
        url
    }
