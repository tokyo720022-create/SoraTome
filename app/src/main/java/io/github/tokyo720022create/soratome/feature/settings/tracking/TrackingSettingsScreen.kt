package io.github.tokyo720022create.soratome.feature.settings.tracking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.domain.tracking.TrackerAccountState
import io.github.tokyo720022create.soratome.domain.tracking.TrackerService
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsScaffold
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsSectionHeader

@Composable
fun TrackingSettingsScreen(onBack: () -> Unit) {
    SettingsScaffold(title = "Tracking & sync", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionHeader("Accounts")
            TrackerService.entries.forEach { service ->
                // Sign-in is not implemented yet, so every account is shown as not connected.
                TrackerAccountRow(service = service, state = TrackerAccountState.NotConnected)
            }
            Text(
                text = "Signing in and syncing reading progress will be available in a future version.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun TrackerAccountRow(service: TrackerService, state: TrackerAccountState) {
    ListItem(
        headlineContent = { Text(service.displayName) },
        supportingContent = {
            Text(
                when (state) {
                    TrackerAccountState.NotConnected -> "Not connected"
                    is TrackerAccountState.Connected -> "Signed in as ${state.username}"
                },
            )
        },
        trailingContent = {
            OutlinedButton(onClick = {}, enabled = false) {
                Text(if (state is TrackerAccountState.Connected) "Log out" else "Log in")
            }
        },
    )
}
