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
| `ui` | `SoraTomeApp` (root composable), `AppVersion` |
| `ui.components` | Shared composables: `PlaceholderScreen`, `ScreenScaffold`, `RemoteImage` |
| `data.image` | `PageImageLoader`, `ImageLoadReporter`, `PageLoadException` |
| `data.reader` | `ReaderPreferences`, `ReadingProgressStore` |
| `domain.reader` | `ReadingMode`, `ImageQuality` |
| `domain.tracking` | `TrackerService`, `TrackerAccountState` |
| `feature.library` | Library tab |
| `feature.history` | History tab |
| `feature.browse` | Browse tab: plugin picker, search, results with covers; `BrowseState` |
| `feature.browse.details` | Series details and chapter list |
| `feature.reader` | Chapter reader: `ReaderScreen`, `ReaderSession` |
| `feature.reader.components` | `PagedReader`, `VerticalReader`, `PageImage`, `ReaderTopBar`, `ReaderBottomBar`, `ReaderOptionGroups` |
| `feature.profile` | Profile tab: account header, AniList and MyAnimeList sync cards, in-app stats, link to Settings |
| `feature.profile.components` | `ProfileHeaderCard`, `TrackerSyncCard`, `AppStatsCard` |
| `feature.settings` | Settings list: `SettingsScreen`, `SettingsCategory` |
| `feature.settings.components` | `SettingsScaffold`, `SettingsPlaceholderScreen`, `SettingsSectionHeader` |
| `feature.settings.<category>` | One screen per category: `appearance`, `library`, `reader`, `downloads`, `tracking`, `browse`, `data`, `about` |

## Navigation

Bottom bar tabs: **Library, History, Browse, Profile**. Browse opens a series (`browse/details`), which opens the full-screen reader (`reader`, no tab bar). Settings is not a tab: it opens from the Profile tab (`profile` -> `settings` -> `settings/<category>`), and Profile stays highlighted while Settings is open.

## `plugin-api` packages

| Package | Contents |
|---|---|
| `plugin.api.manifest` | `PluginManifest`, `PluginPermissions`, `PluginType`, `PLUGIN_API_VERSION` |
| `plugin.api.source` | `SourcePlugin` and its models: `SeriesSummary`, `SearchResult`, `SeriesDetails`, `ChapterInfo`, `PageInfo` (with optional data-saver URL and load-report URL) |

## `plugin-runtime` packages

| Package | Contents |
|---|---|
| `plugin.runtime.error` | `PluginException`, `PluginSecurityException` |
| `plugin.runtime.manifest` | `ManifestParser` |
| `plugin.runtime.network` | `NetworkPolicy`, `HostHttp` |
| `plugin.runtime.script` | `ScriptSourcePlugin` (QuickJS), JSON helpers |
| `plugin.runtime.loader` | `AssetPluginLoader` |

## Settings

`SettingsCategory` defines the list shown on the Settings screen. Every category opens its own screen.

| Category | Status |
|---|---|
| Tracking | AniList and MyAnimeList account rows (sign-in not implemented yet) |
| About | Version and license |
| Reader | Reading mode and image quality |
| Appearance, Library, Downloads, Browse, Data and storage | Placeholder, no options yet |

Not included on purpose: Security and privacy, Advanced.

To add a settings category:

1. Add a route constant to `Routes`.
2. Add an entry to `SettingsCategory` with that route.
3. Create `feature/settings/<category>/<Category>SettingsScreen.kt`, using `SettingsScaffold`.
4. Register it in `SoraTomeNavHost`.

To add another tracking service, add an entry to `TrackerService`.

## Planned

- Local database (Room) for the library
- Reader module (paged and webtoon, then panel detection, OCR, image enhancement)
- Sync of library and reading progress
