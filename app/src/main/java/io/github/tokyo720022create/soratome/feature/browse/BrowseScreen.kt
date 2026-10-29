package io.github.tokyo720022create.soratome.feature.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.plugin.api.source.SeriesSummary
import io.github.tokyo720022create.soratome.plugin.api.source.SourcePlugin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun BrowseScreen() {
    val application = LocalContext.current.applicationContext as SoraTomeApplication
    val plugin = remember { application.plugins.firstOrNull() }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(emptyList<SeriesSummary>()) }
    var details by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Browse", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (plugin == null) {
            Text(text = "No plugins installed.", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Plugins will be installable from here in a future version.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Text(
                text = "Source: ${plugin.manifest.name} v${plugin.manifest.version}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            status = "Searching..."
                            try {
                                val result = plugin.search(query, 1)
                                results = result.items
                                status = "${result.items.size} result(s)"
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                status = "Error: ${e.message}"
                            }
                        }
                    },
                ) {
                    Text("Search")
                }
            }

            if (status.isNotEmpty()) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(results, key = { it.id }) { series ->
                    Text(
                        text = series.title,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { scope.launch { details = describe(plugin, series) } }
                            .padding(vertical = 12.dp),
                    )
                    HorizontalDivider()
                }
            }

            if (details.isNotEmpty()) {
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
        }
    }
}

private suspend fun describe(plugin: SourcePlugin, series: SeriesSummary): String {
    return try {
        val info = plugin.getSeries(series.id)
        val chapters = plugin.getChapters(series.id)
        val firstChapter = chapters.firstOrNull()
        val pages = firstChapter?.let { plugin.getPages(it.id) }.orEmpty()
        buildString {
            appendLine(info.title)
            info.description?.let { appendLine(it) }
            if (info.tags.isNotEmpty()) appendLine("Tags: ${info.tags.joinToString()}")
            appendLine("${chapters.size} chapter(s)")
            if (firstChapter != null) appendLine("${firstChapter.name}: ${pages.size} page(s)")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
