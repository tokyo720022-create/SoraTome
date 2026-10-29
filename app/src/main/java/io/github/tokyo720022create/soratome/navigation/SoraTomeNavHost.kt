package io.github.tokyo720022create.soratome.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.tokyo720022create.soratome.feature.browse.BrowseScreen
import io.github.tokyo720022create.soratome.feature.browse.details.SeriesDetailsScreen
import io.github.tokyo720022create.soratome.feature.history.HistoryScreen
import io.github.tokyo720022create.soratome.feature.library.LibraryScreen
import io.github.tokyo720022create.soratome.feature.profile.ProfileScreen
import io.github.tokyo720022create.soratome.feature.reader.ReaderScreen
import io.github.tokyo720022create.soratome.feature.settings.SettingsScreen
import io.github.tokyo720022create.soratome.feature.settings.about.AboutSettingsScreen
import io.github.tokyo720022create.soratome.feature.settings.appearance.AppearanceSettingsScreen
import io.github.tokyo720022create.soratome.feature.settings.browse.BrowseSettingsScreen
import io.github.tokyo720022create.soratome.feature.settings.data.DataSettingsScreen
import io.github.tokyo720022create.soratome.feature.settings.downloads.DownloadsSettingsScreen
import io.github.tokyo720022create.soratome.feature.settings.library.LibrarySettingsScreen
import io.github.tokyo720022create.soratome.feature.settings.reader.ReaderSettingsScreen
import io.github.tokyo720022create.soratome.feature.settings.tracking.TrackingSettingsScreen

@Composable
fun SoraTomeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val goBack: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = Routes.LIBRARY,
        modifier = modifier,
    ) {
        // Tabs
        composable(Routes.LIBRARY) { LibraryScreen() }
        composable(Routes.HISTORY) { HistoryScreen() }
        composable(Routes.BROWSE) {
            BrowseScreen(onOpenSeries = { navController.navigate(Routes.SERIES_DETAILS) })
        }
        composable(Routes.SERIES_DETAILS) {
            SeriesDetailsScreen(
                onBack = goBack,
                onOpenReader = { navController.navigate(Routes.READER) },
            )
        }
        composable(Routes.READER) { ReaderScreen(onBack = goBack) }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenTrackingSettings = { navController.navigate(Routes.SETTINGS_TRACKING) },
            )
        }

        // Settings (opened from Profile)
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = goBack,
                onOpenCategory = { route -> navController.navigate(route) },
            )
        }
        composable(Routes.SETTINGS_APPEARANCE) { AppearanceSettingsScreen(onBack = goBack) }
        composable(Routes.SETTINGS_LIBRARY) { LibrarySettingsScreen(onBack = goBack) }
        composable(Routes.SETTINGS_READER) { ReaderSettingsScreen(onBack = goBack) }
        composable(Routes.SETTINGS_DOWNLOADS) { DownloadsSettingsScreen(onBack = goBack) }
        composable(Routes.SETTINGS_TRACKING) { TrackingSettingsScreen(onBack = goBack) }
        composable(Routes.SETTINGS_BROWSE) { BrowseSettingsScreen(onBack = goBack) }
        composable(Routes.SETTINGS_DATA) { DataSettingsScreen(onBack = goBack) }
        composable(Routes.SETTINGS_ABOUT) { AboutSettingsScreen(onBack = goBack) }
    }
}
