package io.github.tokyo720022create.soratome.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The tabs shown in the bottom navigation bar.
 *
 * @property ownedRoots Route prefixes that keep this tab highlighted, so Settings
 * (opened from Profile) does not change the selected tab.
 */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val ownedRoots: List<String> = listOf(route),
) {
    Library(Routes.LIBRARY, "Library", Icons.AutoMirrored.Filled.List),
    History(Routes.HISTORY, "History", Icons.Filled.DateRange),
    Browse(Routes.BROWSE, "Browse", Icons.Filled.Search),
    Profile(Routes.PROFILE, "Profile", Icons.Filled.Person, listOf(Routes.PROFILE, Routes.SETTINGS)),
}
