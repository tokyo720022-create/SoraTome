package io.github.tokyo720022create.soratome.feature.settings.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Body of a settings section that has no options yet. */
@Composable
fun SettingsPlaceholderScreen(title: String, onBack: () -> Unit) {
    SettingsScaffold(title = title, onBack = onBack) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Settings for this section will appear here.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
