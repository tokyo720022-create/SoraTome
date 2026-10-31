package io.github.tokyo720022create.soratome.feature.settings.browse

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.data.plugins.PluginEntry
import io.github.tokyo720022create.soratome.data.plugins.UpdateCheck
import io.github.tokyo720022create.soratome.feature.settings.browse.components.InstallPluginDialog
import io.github.tokyo720022create.soratome.feature.settings.browse.components.PluginRow
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsScaffold
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsSectionHeader
import io.github.tokyo720022create.soratome.ui.components.TextInputDialog
import java.net.URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Settings > Browse: install, update, switch off and remove plugins, and manage repositories. */
@Composable
fun BrowseSettingsScreen(
    onBack: () -> Unit,
    onOpenRepository: (index: Int) -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as SoraTomeApplication
    val manager = application.pluginManager
    val scope = rememberCoroutineScope()

    var busy by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<PendingInstall?>(null) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showRepositoryDialog by remember { mutableStateOf(false) }
    var uninstallTarget by remember { mutableStateOf<PluginEntry?>(null) }
    var updateCheck by remember { mutableStateOf<UpdateCheck?>(null) }

    fun tell(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    // Runs [block] with the busy bar showing, and reports any error in a message.
    fun guarded(block: suspend () -> Unit) {
        scope.launch {
            busy = true
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                tell(e.message ?: "Something went wrong")
            } finally {
                busy = false
            }
        }
    }

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            guarded { pending = PendingInstall(manager.readPackage(uri), "A file on this device") }
        }
    }

    SettingsScaffold(title = "Browse", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            Text(
                text = "SoraTome comes with no plugins and no repositories. A plugin can contact " +
                    "the websites it lists, so only install plugins from sources you trust.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
            )

            SettingsSectionHeader("Plugins")
            if (manager.entries.isEmpty()) {
                ListItem(
                    headlineContent = { Text("No plugins installed") },
                    supportingContent = { Text("Add one below.") },
                )
            }
            manager.entries.forEach { entry ->
                PluginRow(
                    entry = entry,
                    onEnabledChange = { manager.setEnabled(entry.manifest.id, it) },
                    onUninstall = { uninstallTarget = entry },
                )
            }

            SettingsSectionHeader("Add a plugin")
            ListItem(
                headlineContent = { Text("Install from file") },
                supportingContent = { Text("Choose a plugin package on this device (.soraplugin or .zip)") },
                modifier = Modifier.clickable(enabled = !busy) { pickFile.launch(arrayOf("*/*")) },
            )
            ListItem(
                headlineContent = { Text("Install from link") },
                supportingContent = { Text("Paste the https link to a plugin package") },
                modifier = Modifier.clickable(enabled = !busy) { showLinkDialog = true },
            )

            SettingsSectionHeader("Repositories")
            manager.repositories.forEachIndexed { index, url ->
                ListItem(
                    headlineContent = { Text(hostOf(url)) },
                    supportingContent = { Text(url, maxLines = 2) },
                    trailingContent = {
                        IconButton(onClick = { manager.removeRepository(url) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove repository")
                        }
                    },
                    modifier = Modifier.clickable { onOpenRepository(index) },
                )
            }
            ListItem(
                headlineContent = { Text("Add repository") },
                supportingContent = { Text("A repository is a list of plugins you can install and update") },
                modifier = Modifier.clickable(enabled = !busy) { showRepositoryDialog = true },
            )

            SettingsSectionHeader("Updates")
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedButton(
                    onClick = { guarded { updateCheck = manager.checkForUpdates() } },
                    enabled = !busy && manager.repositories.isNotEmpty(),
                ) {
                    Text("Check for updates")
                }
                if (manager.repositories.isEmpty()) {
                    Text(
                        "Add a repository to check for updates.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            updateCheck?.let { result ->
                if (result.updates.isEmpty()) {
                    ListItem(headlineContent = { Text("All plugins are up to date") })
                }
                result.updates.forEach { update ->
                    ListItem(
                        headlineContent = { Text(update.installed.manifest.name) },
                        supportingContent = {
                            Text("${update.installed.manifest.version} → ${update.candidate.version}")
                        },
                        trailingContent = {
                            Button(
                                enabled = !busy,
                                onClick = {
                                    guarded {
                                        pending = PendingInstall(
                                            manager.downloadFromRepository(update.candidate),
                                            update.candidate.packageUrl,
                                        )
                                    }
                                },
                            ) {
                                Text("Update")
                            }
                        },
                    )
                }
                if (result.failedRepositories.isNotEmpty()) {
                    Text(
                        "Could not read: " + result.failedRepositories.joinToString { hostOf(it) },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }

    pending?.let { request ->
        val current = manager.entries.firstOrNull { it.manifest.id == request.pkg.manifest.id && !it.bundled }
        InstallPluginDialog(
            pending = request,
            installedVersion = current?.manifest?.version,
            busy = busy,
            onConfirm = {
                guarded {
                    manager.install(request.pkg, request.origin)
                    tell("Installed ${request.pkg.manifest.name}")
                    pending = null
                    updateCheck = null
                }
            },
            onDismiss = { pending = null },
        )
    }

    uninstallTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { uninstallTarget = null },
            title = { Text("Uninstall ${target.manifest.name}?") },
            text = { Text("The plugin is removed from this device. Your reading progress is kept.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        uninstallTarget = null
                        guarded {
                            manager.uninstall(target.manifest.id)
                            tell("Removed ${target.manifest.name}")
                        }
                    },
                ) {
                    Text("Uninstall")
                }
            },
            dismissButton = { TextButton(onClick = { uninstallTarget = null }) { Text("Cancel") } },
        )
    }

    if (showLinkDialog) {
        TextInputDialog(
            title = "Install from link",
            label = "https link to a plugin package",
            confirmLabel = "Continue",
            onConfirm = { link ->
                showLinkDialog = false
                guarded { pending = PendingInstall(manager.downloadFromLink(link), link) }
            },
            onDismiss = { showLinkDialog = false },
        )
    }

    if (showRepositoryDialog) {
        TextInputDialog(
            title = "Add repository",
            label = "https link to the repository index",
            confirmLabel = "Add",
            onConfirm = { link ->
                showRepositoryDialog = false
                guarded {
                    manager.addRepository(link)
                    tell("Repository added")
                }
            },
            onDismiss = { showRepositoryDialog = false },
        )
    }
}

internal fun hostOf(url: String): String =
    try {
        URL(url).host
    } catch (e: Exception) {
        url
    }
