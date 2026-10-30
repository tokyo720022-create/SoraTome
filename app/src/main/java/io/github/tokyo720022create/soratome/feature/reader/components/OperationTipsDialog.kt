package io.github.tokyo720022create.soratome.feature.reader.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Short explanation of the reader gestures, shown the first time and on request. */
@Composable
fun OperationTipsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reader tips") },
        text = {
            Column {
                Tip("Tap the left or right edge to turn the page.")
                Tip("Tap the middle to show or hide the controls.")
                Tip("Pinch or double-tap to zoom in and out.")
                Tip("Drag the slider to jump to a page.")
                Tip("Open the options button for reading mode, auto read and more.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } },
    )
}

@Composable
private fun Tip(text: String) {
    Text(text = text, modifier = Modifier.padding(vertical = 4.dp))
}
