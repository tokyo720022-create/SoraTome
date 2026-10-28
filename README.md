# Tsuzuki (続き)

A modern, extensible manga reader built from scratch, with a sandboxed plugin system and an advanced panel-aware reader.

> *Tsuzuki* means "to be continued" in Japanese.

**Status:** early development. Nothing is usable yet. See the [roadmap](docs/ROADMAP.md).

## Goals

- **Sandboxed plugins.** Sources, trackers, translators and more run as scripts with declared permissions, not as full APKs. See [Plugin API](docs/PLUGIN_API.md).
- **Advanced reader.** Panel-aware navigation, webtoon and paged auto-detection, on-device OCR and translation overlay, image enhancement, per-series settings.
- **Local-first.** Read your own CBZ/ZIP/PDF/EPUB files out of the box.
- **Clean onboarding.** Easy plugin discovery and install, with no manual APK sideloading.
- **Sync.** Library and reading progress across devices.

## Tech stack

- Kotlin, Jetpack Compose (Android-first)
- Coroutines and Flow, Room, OkHttp, Coil
- QuickJS (planned) for the plugin runtime

See [Architecture](docs/ARCHITECTURE.md).

## Content policy

Tsuzuki does not host, distribute, or endorse any content. It ships with **no built-in sources and no default plugin repositories**. Users are responsible for the content they read and the plugins they install, and for complying with the laws of their country.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE).
