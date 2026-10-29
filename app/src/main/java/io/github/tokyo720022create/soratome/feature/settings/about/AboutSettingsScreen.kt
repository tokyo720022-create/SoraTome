package io.github.tokyo720022create.soratome.feature.settings.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsScaffold
import io.github.tokyo720022create.soratome.ui.rememberAppVersionName

@Composable
fun AboutSettingsScreen(onBack: () -> Unit) {
    val versionName = rememberAppVersionName()

    SettingsScaffold(title = "About", onBack = onBack) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text(versionName) },
            )
            ListItem(
                headlineContent = { Text("License") },
                supportingContent = { Text("Apache License 2.0") },
            )
        }
    }
}
