package io.github.tokyo720022create.soratome.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsScaffold

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenCategory: (route: String) -> Unit,
) {
    SettingsScaffold(title = "Settings", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsCategory.entries.forEach { category ->
                ListItem(
                    leadingContent = { Icon(category.icon, contentDescription = null) },
                    headlineContent = { Text(category.title) },
                    supportingContent = { Text(category.summary) },
                    modifier = Modifier.clickable { onOpenCategory(category.route) },
                )
            }
        }
    }
}
