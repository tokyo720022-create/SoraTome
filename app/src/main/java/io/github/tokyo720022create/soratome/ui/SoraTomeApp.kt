package io.github.tokyo720022create.soratome.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.navigation.Routes
import io.github.tokyo720022create.soratome.navigation.SoraTomeBottomBar
import io.github.tokyo720022create.soratome.navigation.SoraTomeNavHost

@Composable
fun SoraTomeApp() {
    val application = LocalContext.current.applicationContext as SoraTomeApplication
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // The reader is full screen, so it hides the tab bar.
            if (currentRoute != Routes.READER) {
                SoraTomeBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        SoraTomeNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }

    val requestedStore = application.requestedStoreUrl
    if (requestedStore != null) {
        AddStoreRequestDialog(
            url = requestedStore,
            onFinished = { application.requestedStoreUrl = null },
        )
    }
}
