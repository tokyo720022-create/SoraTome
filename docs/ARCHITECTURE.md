# Architecture (draft)

## Principles

- **Local-first:** the app works fully offline with local files.
- **Modular:** features live in separate Gradle modules with clear boundaries.
- **Plugins are untrusted:** they run sandboxed with explicit permissions.
- **Unidirectional data flow:** UI observes state from ViewModels, backed by Kotlin Flow.

## Layers

```
UI (Jetpack Compose) -> ViewModels -> Domain (use cases) -> Data (repositories)
                                                          |-> Local (Room, files)
                                                          |-> Plugin runtime
```

## Planned modules

| Module | Responsibility |
|---|---|
| `app` | Application entry point, navigation, DI wiring |
| `core:model` | Shared data models (Series, Chapter, Page) |
| `core:database` | Room database and DAOs |
| `core:ui` | Shared Compose components and theme |
| `data` | Repositories, local library, file importers |
| `domain` | Use cases |
| `reader` | Paged and webtoon readers, gestures, caching |
| `reader:ml` | Panel detection, OCR, translation, enhancement |
| `plugin:api` | Plugin interfaces, manifest, permission model |
| `plugin:runtime` | Script runtime (QuickJS) and host bindings |
| `plugin:manager` | Install, update, and verify plugins |

## Candidate libraries

Compose and Material 3, Room, OkHttp, Coil, kotlinx.serialization, Koin or Hilt for DI, QuickJS for plugins, ML Kit or Tesseract for OCR, ONNX Runtime or NCNN for image models.

Versions are pinned in `gradle/libs.versions.toml` once the Android project exists.
