# Roadmap

Rough milestones. Scope and order may change. The six signature features are described in [FEATURES.md](FEATURES.md).

## M0: Repo setup
- [x] Create repository
- [x] README and contribution guide
- [ ] LICENSE file (Apache 2.0)
- [x] Android project skeleton (Kotlin + Compose)
- [x] CI: build debug APK on GitHub Actions
- [x] App shell: Library, History, Browse and Profile tabs, settings sections as placeholders

## M1: Local library
- [ ] Library screen, series and chapter models, Room database (with stable series identity, see FEATURES.md)
- [ ] Import local CBZ/ZIP
- [ ] Settings content for Appearance and Library

## M2: Reader basics
See [READER.md](READER.md).
- [x] Paged reader (LTR/RTL) and continuous vertical reader
- [x] Page slider, tap zones, previous/next chapter, preloading
- [x] Reading progress (resume, finished chapters)
- [ ] Zoom and double-tap zoom
- [ ] Reader options from Tokyo Manga Lab: auto read, rotation, status bar, time and battery, volume keys
- [ ] History tab fed by reading progress

## M3: Plugin runtime
- [x] Plugin manifest and permission model
- [x] Sandboxed script runtime (QuickJS)
- [x] Sample plugin with demo data (debug builds only)
- [ ] Plugin install, update, and removal

## M4: Advanced reader
- [ ] Panel detection and smart zoom
- [ ] On-device OCR and translation overlay
- [ ] Image enhancement (denoise, upscale)
- [ ] Auto-crop

## M5: Ecosystem
- [ ] AniList and MyAnimeList sign-in and sync (account screens exist as placeholders)
- [ ] Tracker plugins
- [ ] More file formats (PDF, EPUB)

## Signature features
Planning only. Details and suggested order are in [FEATURES.md](FEATURES.md).

- [ ] Sora Reader Intelligence (per-series reading profiles)
- [ ] Sora Source Radar (source status)
- [ ] Sora Continuity Engine (cross-source chapter matching)
- [ ] Sora Memory (notes, bookmarks, journal)
- [ ] Sora Passport (portable library export and restore)
- [ ] Sora Atmosphere (themes from cover palettes, theme packs)
