package io.github.tokyo720022create.soratome.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.tokyo720022create.soratome.navigation.Routes

/** The sections listed on the Settings screen, in display order. Each opens its own screen. */
enum class SettingsCategory(
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val route: String,
) {
    Appearance("Appearance", "Theme and display", Icons.Filled.Create, Routes.SETTINGS_APPEARANCE),
    Library("Library", "Categories and updates", Icons.AutoMirrored.Filled.List, Routes.SETTINGS_LIBRARY),
    Reader("Reader", "Reading mode, display, navigation", Icons.AutoMirrored.Filled.ArrowForward, Routes.SETTINGS_READER),
    Downloads("Downloads", "Download behavior", Icons.Filled.KeyboardArrowDown, Routes.SETTINGS_DOWNLOADS),
    Tracking("Tracking", "AniList and MyAnimeList", Icons.Filled.Refresh, Routes.SETTINGS_TRACKING),
    Browse("Browse", "Sources and plugins", Icons.Filled.Place, Routes.SETTINGS_BROWSE),
    Data("Data and storage", "Backups and storage space", Icons.Filled.Menu, Routes.SETTINGS_DATA),
    About("About", "Version and license", Icons.Filled.Info, Routes.SETTINGS_ABOUT),
}
