# Architecture

## Principles

- **Local-first:** the app works fully offline with local files.
- **Modular:** features live in separate packages and Gradle modules with clear boundaries.
- **Plugins are untrusted:** they run sandboxed with explicit permissions.
- **One class per file**, grouped into a package by what it does.

## Gradle modules

| Module | Responsibility |
|---|---|
| `app` | Android app: UI, navigation, settings |
| `plugin-api` | Plugin contract: manifest types and the source plugin interface. No Android code |
| `plugin-runtime` | Runs plugins: QuickJS sandbox, network policy, manifest parsing, loading |

## `app` packages

Root: `io.github.tokyo720022create.soratome`

| Package | Contents |
|---|---|
| (root) | `MainActivity`, `SoraTomeApplication` |
| `navigation` | `Routes`, `TopLevelDestination`, `SoraTomeNavHost`, `SoraTomeBottomBar` |
| `ui` | `SoraTomeApp` (root composable) |
| `ui.components` | Shared composables, e.g. `PlaceholderScreen` |
| `domain.tracking` | `TrackerService`, `TrackerAccountState` |
| `feature.library` | Library tab |
| `feature.browse` | Browse tab (sources and search) |
| `feature.settings` | Settings home: `SettingsScreen`, `SettingsCategory` |
| `feature.settings.components` | Shared settings pieces: `SettingsScaffold`, `SettingsSectionHeader` |
| `feature.settings.tracking` | Tracking & sync settings (AniList, MyAnimeList accounts) |
| `feature.settings.about` | About screen |

## `plugin-api` packages

| Package | Contents |
|---|---|
| `plugin.api.manifest` | `PluginManifest`, `PluginPermissions`, `PluginType`, `PLUGIN_API_VERSION` |
| `plugin.api.source` | `SourcePlugin` and its models: `SeriesSummary`, `SearchResult`, `SeriesDetails`, `ChapterInfo`, `PageInfo` |

## `plugin-runtime` packages

| Package | Contents |
|---|---|
| `plugin.runtime.error` | `PluginException`, `PluginSecurityException` |
| `plugin.runtime.manifest` | `ManifestParser` |
| `plugin.runtime.network` | `NetworkPolicy`, `HostHttp` |
| `plugin.runtime.script` | `ScriptSourcePlugin` (QuickJS), JSON helpers |
| `plugin.runtime.loader` | `AssetPluginLoader` |

## Settings

The Settings tab lists categories defined in `SettingsCategory`. A category with a route opens its own screen under `feature.settings.<category>`; one without a route is shown as "Coming soon".

| Category | Status |
|---|---|
| Tracking & sync | Screen with AniList and MyAnimeList account rows (sign-in not implemented yet) |
| About | Version and license |
| Appearance, Library, Reader, Sources, Data & storage | Reserved, no screen yet |

To add a settings screen:

1. Add a route constant to `Routes`.
2. Give the matching `SettingsCategory` entry that route.
3. Create `feature/settings/<category>/<Category>SettingsScreen.kt`, using `SettingsScaffold`.
4. Register it in `SoraTomeNavHost`.

Settings are only added when the feature behind them exists.

To add another tracking service, add an entry to `TrackerService`.

## Planned

- Local database (Room) for the library
- Reader module (paged and webtoon, then panel detection, OCR, image enhancement)
- Sync of library and reading progress
