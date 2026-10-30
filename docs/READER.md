# Reader

The reader is adopted from the Tokyo Manga Lab reader and its Reading settings, rebuilt natively in Kotlin and Jetpack Compose. It follows the screenshots and changelog of that app, not its source code.

## Reading

| Feature | Notes |
|---|---|
| Reading modes | Left to right, right to left, vertical scroll |
| Page fit | Fit screen, or fit width (a tall page then scrolls up and down). Page-by-page modes only |
| **Zoom** | Pinch to zoom up to 4x, double-tap to zoom in and out. While zoomed, drag to move around. In vertical mode, dragging past the edge keeps scrolling the strip |
| Tap zones | Left and right edges turn pages (swapped in right-to-left), middle shows or hides controls. While zoomed, any tap shows or hides controls |
| Page slider and counter | Slider runs right to left in right-to-left mode |
| Previous / next chapter | Buttons in the controls; tapping forward on the last page opens the next chapter |
| Resume | Opens at the page you stopped on; finished chapters start at page one |
| Image quality | Original or data saver, when the source offers a smaller version |
| Preloading | The next two pages load ahead of time |
| Failed pages | Shows the reason and server, with a Retry button that asks the plugin for fresh page addresses |
| Load reports | Sends the image load report some image networks ask for: address, success, size, time |

## Reader options sheet (options button in the reader)

Reading mode, image quality, **auto read** (start or stop, five speeds; touching the page pauses it), **screen rotation** (auto, portrait, landscape), **show status bar**, **time and battery**, **volume keys turn pages**, **chapter reminder**, **skip extras**, **share chapter link**, **see the operation tips again**, **more settings**.

A short tips dialog appears the first time you open the reader.

## Settings > Reader

Same options as the sheet, in three sections like Tokyo Manga Lab:

- **Reader:** reading mode, page fit, image quality, screen rotation, auto read speed, resume (automatic).
- **Reader display and controls:** status bar, time and battery, volume keys, chapter reminder, skip extras, operation tips (show again).
- **Chapter list:** sort chapters (by source or by number), order (newest or oldest first), chapter names (source title or number only), only unread chapters.

## Left out on purpose

- **Only bookmarked chapters:** there are no bookmarks yet (they come with Sora Memory).
- **Keep screen on / Reading goals** ("Coming later" in Tokyo Manga Lab): the screen already stays on while reading; reading goals were not requested.

## How to test

1. Install a debug build (it includes the MangaDex test plugin and the demo plugin; release builds include none).
2. Browse > MangaDex (test) > open a title > Start.
3. Try: pinch and double-tap zoom in each reading mode, panning while zoomed, tap zones, slider, previous/next chapter, leaving and re-entering a chapter.
4. Options button: switch modes, auto read (both a paged mode and vertical), rotation, hide status bar, time and battery, volume keys, chapter reminder, share link, tips, more settings.
5. Settings > Profile > Settings > Reader: change options and check the chapter list on a series page (sort, order, names, only unread).

## Where the code is

| Area | Package |
|---|---|
| Reader screen and session | `feature.reader` |
| Zoom state and gestures | `feature.reader.zoom` |
| Page views, bars, options sheet, tips, badges | `feature.reader.components` |
| Series details and chapter list | `feature.browse.details` |
| Settings > Reader | `feature.settings.reader` |
| Image download, decode, cache, reports | `data.image` |
| Reader and chapter-list preferences, progress | `data.reader` |
| Modes, fit, rotation, speeds, chapter list options | `domain.reader` |
