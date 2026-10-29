# Signature features (planning)

Status: **planning only, no code yet.** These six features are what should make SoraTome different from other readers. They come from the brainstorm notes and are written down here so the architecture can make room for them early.

> A manga reader that remembers your reading life, survives broken sources, and keeps your library connected.

## At a glance

| # | Feature | In one line | Builds on |
|---|---|---|---|
| 1 | **Sora Continuity Engine** (top pick) | Cross-source chapter matching and recovery | Library database, plugin runtime, Source Radar |
| 2 | **Sora Memory** | Your personal manga journal | Library database, reader |
| 3 | **Sora Atmosphere** | A reader that transforms with your mood | Appearance settings, reader |
| 4 | **Sora Source Radar** | Know when your reading sources break | Plugin runtime |
| 5 | **Sora Passport** | Your library belongs to you | Library database, Memory, Reader profiles |
| 6 | **Sora Reader Intelligence** | A reader that adapts to each manga | Reader |

## 1. Sora Continuity Engine

**Cross-source chapter matching and recovery.**

If you have read 180 chapters of a series from one source and that source stops working, SoraTome finds the matching chapters on another source you have configured and helps you continue from the correct place instead of starting over.

How it could work:

- Match series by aliases and metadata, then chapters by number and identifier.
- Where appropriate, compare chapter contents as a later, optional step.
- Detect ambiguous matches and **ask before switching**. Never switch silently.

Why it matters: the app protects your reading continuity, not just your list of manga.

Where it lives: series details (a "Sources" section listing the sources a series is mapped to), plus a prompt when a source fails. Uses source health from Source Radar.

First version: aliases plus chapter-number matching across sources you already added, with a confirmation prompt.
Later: content-based matching (needs downloading and comparing pages, so it is slower and heavier).

## 2. Sora Memory

**Your personal manga journal.**

Save memorable panels, chapter-specific notes, favourite moments, character observations and predictions. Each item is attached to the exact page and chapter. Return to a series after six months and see your last reading position, important characters, your notes, and a **spoiler-safe reminder** of where you stopped.

Why it matters: SoraTome becomes a personal manga archive rather than simply another reader.

Where it lives: a button in the reader to add a note or save a panel, a "Journal" section on the series page, and the recap shown when reopening a series.

First version: notes and bookmarks anchored to series, chapter and page.
Later: saved panels, character list, and the spoiler-safe recap (only shows what comes before your last-read chapter).

## 3. Sora Atmosphere

**A reader that transforms with your mood.**

- Generate a theme from a colour palette extracted from a cover.
- Customise the background, reader controls and accent colours.
- Create and share theme packs, for example night-sky, rainy-day, cyberpunk or monochrome.
- The Your Name, Suzume and Detective Conan themes discussed earlier can inspire the designs.
- Users **import artwork they have permission to use**. SoraTome does not bundle copyrighted artwork.

Why it matters: personalisation becomes an ecosystem instead of a static dark/light toggle.

Where it lives: Settings > Appearance, plus a theme editor, and reader theming.

First version: palette from cover, accent and background colours, light/dark.
Later: theme packs (import, export, share) with user-supplied artwork.

## 4. Sora Source Radar

**Know when your reading sources break.**

Source management becomes transparent instead of leaving users guessing why chapters have disappeared or stopped loading.

*Note: the middle of the original description was not captured in the notes, so this section only covers the parts that were visible. Add the missing details here.*

Where it lives: a status per source in Browse and in Settings > Browse, with a clear reason when something fails.

First version: record success and failure of each plugin call and show the last result and reason per source.
Later: proactive checks and feeding failures into the Continuity Engine.

## 5. Sora Passport

**Your manga library belongs to you.**

Export a portable bundle containing series identities, aliases, tags, reading progress, bookmarks, notes and reading preferences. Restore it after reinstalling the app or moving to another device, then **remap sources** where necessary.

Why it matters: your personal library stays useful even when your provider or device changes.

Where it lives: Settings > Data and storage (export and import).

First version: export and import of library, progress and tags to a single file.
Later: notes, bookmarks, reader profiles, and a guided source-remapping step.

Design rule: the bundle holds **metadata only**, never page images or chapter files.

## 6. Sora Reader Intelligence

**A reader that adapts to each manga.**

Per-series reading profiles covering: right-to-left or left-to-right, webtoon or paged, image scaling, gestures, panel navigation, and optional OCR tools. Profiles can be exported and shared.

Later: adaptive panel navigation and OCR-assisted reading, with **manual controls whenever detection gets things wrong**.

Why it matters: the reader adapts to the work and the person instead of requiring endless manual adjustments.

Where it lives: defaults in Settings > Reader, per-series overrides from the reader menu.

First version: saved per-series profile (direction, mode, scaling).
Later: gestures, panel navigation, OCR, profile sharing.

## Suggested build order

Not every feature should be built at once. This order follows what each one depends on. It is a suggestion and can change.

| Step | Work | Why this order |
|---|---|---|
| 0 | **Foundations**: library database with stable series identity, reader basics | Almost every feature needs these |
| 1 | **Reader Intelligence** (profiles) and **Source Radar** (basic status) | Small first versions, visible value, few dependencies |
| 2 | **Continuity Engine** (metadata matching) | Needs the database, several sources, and Source Radar |
| 3 | **Memory** (notes and bookmarks) | Needs the reader and the database |
| 4 | **Passport** | Best once the data it exports has settled |
| 5 | **Atmosphere** | Mostly independent, can move earlier if wanted |

## Decisions to make now so these features fit later

- **Series identity:** every series gets an internal ID, separate from any source ID, with aliases and a list of source mappings. Continuity, Passport, Memory and Radar all rely on this.
- **Chapter numbering:** one normalised representation that handles decimals (12.5), volumes, and specials.
- **Anchors:** notes and bookmarks point to (series, chapter, page), not to a source URL, so they survive source changes.
- **Versioned data format:** the export format carries a schema version from day one.
- **Source health:** plugin calls report success or failure in one common way, which Radar reads.

## Rules for all features

- **Neutral content policy:** SoraTome ships with no sources and no default repositories. These features work with sources and files the user adds.
- **Copyright:** no bundled copyrighted artwork. Themes use user-supplied art. Exports and shared profiles contain metadata and settings, not page images.
- **On-device first:** matching, OCR and panel detection run on the device.
- **User stays in control:** automatic decisions (switching source, detected panels) can always be reviewed or overridden manually.

## Open questions

1. Source Radar: what else was in the original description?
2. Is the build order above right, or should Continuity or Atmosphere come earlier?
3. Should saved panels (Memory) stay on the device only, with Passport exporting just their page positions?
