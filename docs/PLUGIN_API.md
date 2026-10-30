# Plugin API (draft v1)

Plugins are packaged scripts that run in a QuickJS sandbox. They have no direct access to the filesystem or network. Everything goes through the `host` object, within the permissions the plugin declares.

## Package layout

```
my-plugin/
  manifest.json
  main.js
```

Bundled plugins live in `assets/plugins/<folder>/`. A sample plugin is included in **debug builds only** (`app/src/debug/assets/plugins/sample-source`). Release builds ship with no plugins.

## manifest.json

```json
{
  "id": "org.example.sample-source",
  "name": "Sample Source",
  "version": "1.0.0",
  "apiVersion": 1,
  "type": "source",
  "entry": "main.js",
  "language": "en",
  "permissions": {
    "network": ["api.example.org"],
    "storage": false
  }
}
```

`apiVersion` must match the version the app supports. `entry` must be a relative path without `..`.

## Plugin types

| Type | Status | Purpose |
|---|---|---|
| `source` | Implemented | Provide series, chapters, and pages |
| `tracker` | Planned | Sync reading progress with an external service |
| `translator` | Planned | Translate OCR text |
| `metadata` | Planned | Supply covers, descriptions, tags |
| `filter` | Planned | Process page images |

## Source plugins (JavaScript)

A source plugin is a classic script (not an ES module) that defines `globalThis.plugin`. All four functions are async and must return plain JSON-compatible values.

```js
globalThis.plugin = {
  async search(query, page) {
    // returns { items: [{ id, title, cover }], hasNext: boolean }
  },
  async getSeries(id) {
    // returns { id, title, description, cover, tags: [] }
  },
  async getChapters(seriesId) {
    // returns [{ id, name, number, date, url? }]   (date = epoch millis or null; url = web address, optional)
  },
  async getPages(chapterId) {
    // returns [{ index, url, lowQualityUrl?, reportUrl? }]
    // lowQualityUrl: smaller "data saver" image, optional
    // reportUrl: where the app reports each image load (success, size, time), optional
  },
};
```

## Host functions

| Function | Notes |
|---|---|
| `host.log(message)` | Debug logging |
| `await host.http.get(url)` | Returns the response body as a string |

`host.http.get` rules:

- https only
- the host must match a domain in `permissions.network` (subdomains included)
- redirects are followed manually and each hop is checked again
- 5 MB response limit, 15 s connect and 20 s read timeouts

Not available yet: `host.http.post`, HTML parsing helpers, per-plugin settings storage.

## Security model

- Scripts run in a separate QuickJS instance per plugin, with a 60 s evaluation limit.
- No `eval` of remote code and no dynamic imports from the network.
- The app ships with no default repositories. Users add repositories themselves.
- Planned: signed repository indexes, install-time permission prompts, memory limits.

## Open questions

- Declarative (JSON and CSS selector) sources for simple sites
- Repository index format and signing scheme
