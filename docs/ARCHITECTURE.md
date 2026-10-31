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
| `ui` | `SoraTomeApp` (root composable), `AppVersion`, `findActivity` |
| `ui.components` | Shared composables: `PlaceholderScreen`, `ScreenScaffold`, `RemoteImage`, `SwitchRow`, `DropdownRow`, `SegmentedOptions` |
| `data.image` | `PageImageLoader`, `ImageLoadReporter`, `PageLoadException` |
| `data.plugins` | `PluginManager` (install, update, remove, switch, repositories), `PluginRegistry`, `PluginEntry`, `AvailableUpdate`, `UpdateCheck` |
| `data.reader` | `ReaderPreferences`, `ChapterListPreferences`, `ReadingProgressStore` |
| `domain.reader` | `ReadingMode`, `ImageQuality`, `PageFit`, `ScreenRotation`, `AutoReadSpeed`, `ChapterSort`, `ChapterOrder`, `ChapterNaming` |
| `domain.tracking` | `TrackerService`, `TrackerAccountState` |
| `feature.splash` | Start-up splash screen (name, tagline and caption are constants at the top of `SplashScreen.kt`) |
| `feature.library` | Library tab |
| `feature.history` | History tab |
| `feature.browse` | Browse tab: plugin picker, search, results with covers; `BrowseState` |
| `feature.browse.details` | Series details and chapter list |
| `feature.reader` | Chapter reader: `ReaderScreen`, `ReaderSession`, `ReaderKeyEvents` (volume keys) |
| `feature.reader.zoom` | `ZoomState`, pinch and drag gestures |
| `feature.reader.components` | `PagedReader`, `VerticalReader`, `PageImage`, `ReaderTopBar`, `ReaderBottomBar`, `ReaderOptionsSheet`, `OperationTipsDialog`, `ClockBatteryBadge`, `AutoReadPill`, `ReaderChapterReminder` |
| `feature.profile` | Profile tab: account header, AniList and MyAnimeList sync cards, in-app stats, link to Settings |
| `feature.profile.components` | `ProfileHeaderCard`, `TrackerSyncCard`, `AppStatsCard` |
| `feature.settings` | Settings list: `SettingsScreen`, `SettingsCategory` |
| `feature.settings.components` | `SettingsScaffold`, `SettingsPlaceholderScreen`, `SettingsSectionHeader` |
| `feature.settings.browse` | Settings > Browse: plugin list, install from file or link, repositories, updates (`BrowseSettingsScreen`, `PendingInstall`) |
| `feature.settings.browse.components` | `PluginRow`, `InstallPluginDialog` |
| `feature.settings.browse.repository` | `RepositoryScreen` (plugins listed in one repository) |
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
| `plugin.runtime.loader` | `AssetPluginLoader` (bundled), `FilePluginLoader` (installed) |
| `plugin.runtime.install` | `PluginPackageReader`, `PluginPackage`, `PluginStore`, `PluginDownloader`, `LimitedReader`, `Hashing`, `PluginVersion` |
| `plugin.runtime.repository` | `RepositoryIndexParser`, `RepositoryIndex`, `RepositoryPlugin` |

## Settings

`SettingsCategory` defines the list shown on the Settings screen. Every category opens its own screen.

| Category | Status |
|---|---|
| Tracking | AniList and MyAnimeList account rows (sign-in not implemented yet) |
| About | Version and license |
| Reader | Reader options, display and controls, chapter list (see `READER.md`) |
| Browse | Plugins: install, update, remove, switch on or off, repositories (see `PLUGIN_REPOSITORY.md`) |
| Appearance, Library, Downloads, Data and storage | Placeholder, no options yet |

Not included on purpose: Security and privacy, Advanced.

To add a settings category:

1. Add a route constant to `Routes`.
2. Add an entry to `SettingsCategory` with that route.
3. Create `feature/settings/<category>/<Category>SettingsScreen.kt`, using `SettingsScaffold`.
4. Register it in `SoraTomeNavHost`.

To add another tracking service, add an entry to `TrackerService`.

## App icon and splash

- Icon artwork: `docs/images/soratome-icon.png`. The launcher icon is an adaptive icon (`res/mipmap-anydpi-v26`) with the artwork in `drawable-nodpi/ic_launcher_foreground.png` on the navy `ic_launcher_background`.
- The in-app splash uses `drawable-nodpi/splash_logo.png`. The window background (`drawable/splash_background.xml`) is the same dark gradient, so there is no white flash on launch. On Android 12 and newer the system splash uses `splash_background` too.

## Planned

- Local database (Room) for the library
- Reader module (paged and webtoon, then panel detection, OCR, image enhancement)
- Sync of library and reading progress
