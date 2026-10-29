package io.github.tokyo720022create.soratome.feature.settings.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.feature.reader.components.ReaderOptionGroups
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsScaffold

@Composable
fun ReaderSettingsScreen(onBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as SoraTomeApplication

    SettingsScaffold(title = "Reader", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            ReaderOptionGroups(preferences = application.readerPreferences)
        }
    }
}
