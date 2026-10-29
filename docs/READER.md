# Reader

The reader is adopted from the Tokyo Manga Lab reader (versions 0.7.0 to 0.8.0 in its changelog), rebuilt natively in Kotlin and Jetpack Compose. The Tokyo Manga Lab source files were not available to read, so this follows what its changelog describes, not its code.

## What works now

| Feature | Notes |
|---|---|
| Reading modes | Left to right, right to left, vertical scroll |
| Page slider and counter | Slider runs right to left in right-to-left mode |
| Tap zones | Left and right edges turn pages (swapped in right-to-left), middle shows or hides controls |
| Previous / next chapter | Buttons in the controls; tapping forward on the last page opens the next chapter |
| Resume | Opens at the page you stopped on; finished chapters are marked |
| Image quality | Original or data saver, when the source offers a smaller version |
| Preloading | The next two pages load ahead of time |
| Failed pages | Shows the reason (refused 403, not found 404, rate limited 429, could not connect, not an image) with the server name and port, and a Retry button |
| Expired addresses | Retry asks the plugin for fresh page addresses (at most every 5 seconds) |
| Load reports | Sends the image load report some image networks ask for: address, success, size, time. No account or device data |
| Screen | Stays awake while reading |
| Settings | Reading mode and image quality in the reader's options and in Settings > Reader |

Pages are only held in memory. Nothing is saved to disk and nothing is re-hosted.

## How to test

1. Install a debug build. Debug builds include two test plugins: **MangaDex (test)** and **Sample Source (demo)**. Release builds include none.
2. Open **Browse**. Pick **MangaDex (test)**. It lists popular titles; search for any title.
3. Tap a title, then **Start** or any chapter.
4. Check: page turning in all three modes, the slider, previous/next chapter, leaving and re-entering a chapter (it should resume), and a failed page's Retry.

## Source of pages

The MangaDex test plugin talks to MangaDex's public API directly from the phone, so SoraTome does not need the Cloudflare Worker that Tokyo Manga Lab uses.

- Search and details: `api.mangadex.org`. Covers: `uploads.mangadex.org`.
- Chapter list: English only, only chapters with readable pages, chapters hosted on other sites skipped, duplicate uploads of the same chapter number reduced to one.
- Pages: MangaDex's at-home servers (`*.mangadex.network`).
- Only "safe" and "suggestive" content ratings are requested.
- Chapters MangaDex has no readable pages for show a message instead of a reader.

## Not built yet (from the Tokyo Manga Lab reader options)

Auto read with speeds, screen rotation option, show/hide status bar, time and battery, volume keys turn pages, chapter reminder, skip extras, share chapter link, first-run operation tips, zoom and double-tap zoom, page-fit setting, automatic address refresh on failure (currently on Retry), and an end card after the last page.

## Where the code is

| Area | Package |
|---|---|
| Reader screen and session | `feature.reader` |
| Page views, bars, option groups | `feature.reader.components` |
| Series details and chapter list | `feature.browse.details` |
| Image download, decode, cache, reports | `data.image` |
| Reader options and progress storage | `data.reader` |
| Modes and quality | `domain.reader` |
