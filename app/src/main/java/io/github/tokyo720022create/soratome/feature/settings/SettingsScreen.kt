package io.github.tokyo720022create.soratome.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onOpenCategory: (route: String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
        )
        SettingsCategory.entries.forEach { category ->
            val route = category.route
            ListItem(
                headlineContent = { Text(category.title) },
                supportingContent = {
                    Text(if (category.isAvailable) category.summary else "Coming soon")
                },
                modifier = Modifier
                    .alpha(if (category.isAvailable) 1f else 0.5f)
                    .clickable(enabled = route != null) { route?.let(onOpenCategory) },
            )
        }
    }
}
