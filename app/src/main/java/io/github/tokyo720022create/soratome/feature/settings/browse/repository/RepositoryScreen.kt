package io.github.tokyo720022create.soratome.feature.settings.browse.repository

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.feature.settings.browse.PendingInstall
import io.github.tokyo720022create.soratome.feature.settings.browse.components.InstallPluginDialog
import io.github.tokyo720022create.soratome.feature.settings.browse.hostOf
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsScaffold
import io.github.tokyo720022create.soratome.plugin.runtime.install.PluginVersion
import io.github.tokyo720022create.soratome.plugin.runtime.repository.RepositoryIndex
import io.github.tokyo720022create.soratome.plugin.runtime.repository.RepositoryPlugin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private sealed interface RepositoryState {
    data object Loading : RepositoryState
    data class Failed(val message: String) : RepositoryState
    data class Loaded(val index: RepositoryIndex) : RepositoryState
}

/**
 * The plugins listed in one store, with Install and Update buttons.
 * Plugins whose content warning is not allowed are left out.
 */
@Composable
fun RepositoryScreen(index: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as SoraTomeApplication
    val manager = application.pluginManager
    val preferences = application.pluginPreferences
    val scope = rememberCoroutineScope()
    val store = manager.stores.getOrNull(index)

    if (store == null) {
        SettingsScaffold(title = "Plugin store", onBack = onBack) { padding ->
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("This store was removed.")
            }
        }
        return
    }
    val url = store.url

    var state by remember(url) { mutableStateOf<RepositoryState>(RepositoryState.Loading) }
    var attempt by remember(url) { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<PendingInstall?>(null) }

    fun tell(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

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

    LaunchedEffect(url, attempt) {
        state = RepositoryState.Loading
        state = try {
            RepositoryState.Loaded(manager.fetchRepository(url))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RepositoryState.Failed(e.message ?: "Could not read this store")
        }
    }

    val title = (state as? RepositoryState.Loaded)?.index?.name ?: store.name ?: hostOf(url)

    SettingsScaffold(title = title, onBack = onBack) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val current = state) {
                RepositoryState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is RepositoryState.Failed -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(current.message, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { attempt++ }) { Text("Retry") }
                }
                is RepositoryState.Loaded -> {
                    val visible = current.index.plugins.filter { preferences.isAllowed(it.contentWarning) }
                    val hidden = current.index.plugins.size - visible.size

                    Column(Modifier.fillMaxSize()) {
                        if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        if (visible.isEmpty() && hidden == 0) {
                            Text(
                                "This store lists no plugins for this version of SoraTome.",
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                        if (hidden > 0) {
                            Text(
                                if (hidden == 1) {
                                    "1 plugin is hidden by your content warning settings."
                                } else {
                                    "$hidden plugins are hidden by your content warning settings."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(count = visible.size, key = { visible[it].id }) { position ->
                                val candidate = visible[position]
                                val installedVersion = manager.entries
                                    .firstOrNull { it.manifest.id == candidate.id && !it.bundled }
                                    ?.manifest?.version
                                PluginListing(
                                    candidate = candidate,
                                    installedVersion = installedVersion,
                                    enabled = !busy,
                                    onInstall = {
                                        guarded {
                                            pending = PendingInstall(
                                                manager.downloadFromRepository(candidate),
                                                candidate.packageUrl,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pending?.let { request ->
        val installedVersion = manager.entries
            .firstOrNull { it.manifest.id == request.pkg.manifest.id && !it.bundled }
            ?.manifest?.version
        InstallPluginDialog(
            pending = request,
            installedVersion = installedVersion,
            busy = busy,
            onConfirm = {
                guarded {
                    manager.install(request.pkg, request.origin)
                    tell("Installed ${request.pkg.manifest.name}")
                    pending = null
                }
            },
            onDismiss = { pending = null },
        )
    }
}

@Composable
private fun PluginListing(
    candidate: RepositoryPlugin,
    installedVersion: String?,
    enabled: Boolean,
    onInstall: () -> Unit,
) {
    val upToDate = installedVersion != null &&
        PluginVersion.compare(candidate.version, installedVersion) <= 0
    val needsUpdate = installedVersion != null && !upToDate

    ListItem(
        headlineContent = { Text(candidate.name) },
        supportingContent = {
            val details = listOfNotNull(
                "Version ${candidate.version}",
                candidate.contentWarning.label,
                candidate.language,
                candidate.description,
            ).joinToString(" · ")
            Text(details, maxLines = 3)
        },
        trailingContent = {
            when {
                upToDate -> OutlinedButton(onClick = {}, enabled = false) { Text("Installed") }
                else -> Button(onClick = onInstall, enabled = enabled) {
                    Text(if (needsUpdate) "Update" else "Install")
                }
            }
        },
    )
}
