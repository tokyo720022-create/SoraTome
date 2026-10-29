package io.github.tokyo720022create.soratome.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.tokyo720022create.soratome.feature.browse.BrowseScreen
import io.github.tokyo720022create.soratome.feature.library.LibraryScreen
import io.github.tokyo720022create.soratome.feature.settings.SettingsScreen
import io.github.tokyo720022create.soratome.feature.settings.about.AboutSettingsScreen
import io.github.tokyo720022create.soratome.feature.settings.tracking.TrackingSettingsScreen

@Composable
fun SoraTomeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LIBRARY,
        modifier = modifier,
    ) {
        composable(Routes.LIBRARY) { LibraryScreen() }
        composable(Routes.BROWSE) { BrowseScreen() }
        composable(Routes.SETTINGS) {
            SettingsScreen(onOpenCategory = { route -> navController.navigate(route) })
        }
        composable(Routes.SETTINGS_TRACKING) {
            TrackingSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_ABOUT) {
            AboutSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
