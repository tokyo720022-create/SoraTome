package io.github.tokyo720022create.soratome.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** The versionName of the installed app, or "Unknown" if it cannot be read. */
@Composable
fun rememberAppVersionName(): String {
    val context = LocalContext.current
    return remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "Unknown"
    }
}
