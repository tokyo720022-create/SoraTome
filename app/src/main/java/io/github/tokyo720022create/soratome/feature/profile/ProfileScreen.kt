package io.github.tokyo720022create.soratome.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.domain.tracking.TrackerAccountState
import io.github.tokyo720022create.soratome.domain.tracking.TrackerService
import io.github.tokyo720022create.soratome.feature.profile.components.AppStatsCard
import io.github.tokyo720022create.soratome.feature.profile.components.ProfileHeaderCard
import io.github.tokyo720022create.soratome.feature.profile.components.TrackerSyncCard
import io.github.tokyo720022create.soratome.ui.rememberAppVersionName

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenTrackingSettings: () -> Unit,
) {
    val version = rememberAppVersionName()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileHeaderCard()

        TrackerService.entries.forEach { service ->
            // Sign-in is not implemented yet, so every account is shown as not connected.
            TrackerSyncCard(
                service = service,
                state = TrackerAccountState.NotConnected,
                onOpenSettings = onOpenTrackingSettings,
            )
        }

        Text(
            text = "IN THIS APP",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        )
        AppStatsCard()

        Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Settings") },
                supportingContent = { Text("Appearance, reading, accounts, backup and more") },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable(onClick = onOpenSettings),
            )
        }

        Text(
            text = "SoraTome v$version",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
    }
}
