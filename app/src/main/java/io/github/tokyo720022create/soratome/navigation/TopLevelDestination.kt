package io.github.tokyo720022create.soratome.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** The tabs shown in the bottom navigation bar. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Library(Routes.LIBRARY, "Library", Icons.Filled.Home),
    Browse(Routes.BROWSE, "Browse", Icons.Filled.Search),
    Settings(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
}
