package io.github.tokyo720022create.soratome.feature.settings.browse.stores

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import io.github.tokyo720022create.soratome.data.plugins.StoreInfo
import io.github.tokyo720022create.soratome.feature.settings.browse.components.StoreCard
import io.github.tokyo720022create.soratome.feature.settings.browse.hostOf
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsScaffold
import io.github.tokyo720022create.soratome.ui.components.TextInputDialog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Plugin stores: a card for each store with its name, address and plugin count, a refresh
 * button, and Add. SoraTome comes with no stores; the user adds the ones they trust.
 */
@Composable
fun StoresScreen(
    onBack: () -> Unit,
    onOpenStore: (index: Int) -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as SoraTomeApplication
    val manager = application.pluginManager
    val scope = rememberCoroutineScope()

    var busy by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var removeTarget by remember { mutableStateOf<StoreInfo?>(null) }

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

    fun openLink(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            tell("No app can open this link")
        }
    }

    fun copyLink(url: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Plugin store", url))
        tell("Link copied")
    }

    SettingsScaffold(
        title = "Plugin stores",
        onBack = onBack,
        actions = {
            IconButton(
                enabled = !busy && manager.stores.isNotEmpty(),
                onClick = {
                    guarded {
                        val failed = manager.refreshStores()
                        tell(
                            if (failed.isEmpty()) "Stores refreshed"
                            else "Could not read: " + failed.joinToString { hostOf(it) },
                        )
                    }
                },
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh stores")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (manager.stores.isEmpty()) {
                    Text(
                        text = "No stores yet. A store is a list of plugins you can install and update. " +
                            "SoraTome comes with none, so add one from someone you trust.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(count = manager.stores.size, key = { manager.stores[it].url }) { position ->
                        val store = manager.stores[position]
                        StoreCard(
                            store = store,
                            onOpen = { onOpenStore(position) },
                            onOpenWebsite = { store.website?.let { openLink(it) } },
                            onOpenSupport = { store.support?.let { openLink(it) } },
                            onCopyLink = { copyLink(store.url) },
                            onRemove = { removeTarget = store },
                        )
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    if (showAddDialog) {
        TextInputDialog(
            title = "Add plugin store",
            label = "Store index URL",
            hint = "The https link to the store's index file.",
            confirmLabel = "Add",
            onConfirm = { link ->
                showAddDialog = false
                guarded {
                    manager.addRepository(link)
                    tell("Store added")
                }
            },
            onDismiss = { showAddDialog = false },
        )
    }

    removeTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("Remove ${target.name ?: hostOf(target.url)}?") },
            text = {
                Text("Plugins you installed from it stay installed. You will no longer see its plugins or updates.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        manager.removeRepository(target.url)
                        removeTarget = null
                    },
                ) {
                    Text("Remove")
                }
            },
            dismissButton = { TextButton(onClick = { removeTarget = null }) { Text("Cancel") } },
        )
    }
}
