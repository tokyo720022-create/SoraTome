package io.github.tokyo720022create.soratome.feature.settings

import io.github.tokyo720022create.soratome.navigation.Routes

/**
 * The sections of the Settings screen. A category with a [route] opens its own screen;
 * one without a route is listed as "Coming soon" until its feature exists.
 */
enum class SettingsCategory(
    val title: String,
    val summary: String,
    val route: String? = null,
) {
    Appearance("Appearance", "Theme and display"),
    Library("Library", "Library behavior"),
    Reader("Reader", "Reading mode and images"),
    Sources("Sources", "Plugins and repositories"),
    Tracking("Tracking & sync", "AniList and MyAnimeList", Routes.SETTINGS_TRACKING),
    Data("Data & storage", "Downloads, backups and cache"),
    About("About", "Version and license", Routes.SETTINGS_ABOUT),
    ;

    val isAvailable: Boolean get() = route != null
}
