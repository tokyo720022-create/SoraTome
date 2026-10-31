package io.github.tokyo720022create.soratome.feature.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.plugin.api.source.SourcePlugin
import io.github.tokyo720022create.soratome.ui.components.RemoteImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun BrowseScreen(
    onOpenSeries: () -> Unit,
    onOpenPluginSettings: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as SoraTomeApplication
    val state = application.browseState
    val plugins = application.plugins
    val plugin = plugins.firstOrNull { it.manifest.id == state.selectedPluginId } ?: plugins.firstOrNull()
    val scope = rememberCoroutineScope()

    suspend fun search(source: SourcePlugin, page: Int) {
        state.status = "Searching..."
        try {
            val result = source.search(state.query, page)
            state.results = if (page == 1) result.items else state.results + result.items
            state.page = page
            state.hasNext = result.hasNext
            state.searched = true
            state.resultsPluginId = source.manifest.id
            state.status = if (state.results.isEmpty()) "No results" else "${state.results.size} result(s)"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            state.status = "Error: ${e.message}"
        }
    }

    // Show the plugin's default listing the first time, and again when the plugin changes
    // (for example after the one in use was switched off or removed).
    LaunchedEffect(plugin?.manifest?.id) {
        if (plugin != null) {
            state.selectedPluginId = plugin.manifest.id
            if (state.resultsPluginId != plugin.manifest.id) {
                // A different plugin than before: forget the old search text and results.
                if (state.resultsPluginId != null) state.query = ""
                state.clearResults()
                search(plugin, 1)
            }
        } else {
            state.clearResults()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(text = "Browse", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (plugin == null) {
            Text(text = "No plugins installed.", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "SoraTome comes with no sources. Add a plugin to start browsing.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Button(onClick = onOpenPluginSettings) { Text("Add plugins") }
            return@Column
        }

        if (plugins.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                plugins.forEach { candidate ->
                    val selected = candidate.manifest.id == plugin.manifest.id
                    val onClick = {
                        if (!selected) {
                            state.selectedPluginId = candidate.manifest.id
                            state.clearResults()
                        }
                    }
                    if (selected) {
                        FilledTonalButton(onClick = onClick) { Text(candidate.manifest.name) }
                    } else {
                        OutlinedButton(onClick = onClick) { Text(candidate.manifest.name) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { state.query = it },
                label = { Text("Search") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { scope.launch { search(plugin, 1) } }) { Text("Search") }
        }

        if (state.status.isNotEmpty()) {
            Text(
                text = state.status,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.results, key = { it.id }) { series ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            state.selectedSeries = series
                            onOpenSeries()
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RemoteImage(
                        url = series.coverUrl,
                        allowedDomains = plugin.manifest.permissions.network,
                        modifier = Modifier.size(width = 56.dp, height = 80.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = series.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 3,
                    )
                }
                HorizontalDivider()
            }
            if (state.hasNext) {
                item {
                    TextButton(
                        onClick = { scope.launch { search(plugin, state.page + 1) } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Load more")
                    }
                }
            }
        }
    }
}
