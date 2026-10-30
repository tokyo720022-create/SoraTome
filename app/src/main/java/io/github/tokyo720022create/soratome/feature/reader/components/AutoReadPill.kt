package io.github.tokyo720022create.soratome.feature.reader.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Shown while auto read is on. Touching the page pauses it; Resume continues it. */
@Composable
fun AutoReadPill(
    speedLabel: String,
    paused: Boolean,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.85f), modifier = modifier) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Auto read · $speedLabel",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
            TextButton(onClick = onTogglePause) { Text(if (paused) "Resume" else "Pause") }
            TextButton(onClick = onStop) { Text("Stop") }
        }
    }
}
