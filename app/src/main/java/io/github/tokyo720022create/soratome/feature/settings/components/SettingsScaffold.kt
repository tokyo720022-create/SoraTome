package io.github.tokyo720022create.soratome.feature.settings.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import io.github.tokyo720022create.soratome.ui.components.ScreenScaffold

/** Frame shared by every settings sub-screen: a title bar with a back button. */
@Composable
fun SettingsScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    ScreenScaffold(title = title, onBack = onBack, content = content)
}
