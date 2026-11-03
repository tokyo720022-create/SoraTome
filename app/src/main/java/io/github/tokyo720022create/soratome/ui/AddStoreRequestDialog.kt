package io.github.tokyo720022create.soratome.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * A link outside the app (soratome://add-store?url=...) asked to add a plugin store.
 * Nothing is added until the user says yes.
 */
@Composable
fun AddStoreRequestDialog(url: String, onFinished: () -> Unit) {
    val context = LocalContext.current
    val manager = (context.applicationContext as SoraTomeApplication).pluginManager
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!busy) onFinished() },
        title = { Text("Add plugin store?") },
        text = {
            Column {
                Text("A link asked to add this plugin store to SoraTome:")
                Spacer(Modifier.height(8.dp))
                Text(url, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Only add stores from sources you trust.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy,
                onClick = {
                    scope.launch {
                        busy = true
                        try {
                            manager.addRepository(url)
                            Toast.makeText(context, "Store added", Toast.LENGTH_LONG).show()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message ?: "Could not add this store", Toast.LENGTH_LONG).show()
                        } finally {
                            busy = false
                            onFinished()
                        }
                    }
                },
            ) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = onFinished) { Text("Cancel") } },
    )
}
