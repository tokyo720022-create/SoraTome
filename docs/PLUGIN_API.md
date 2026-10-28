# Plugin API (draft v0)

Plugins are packaged scripts that run in a sandbox. They cannot access the filesystem or network except through host functions, and only within the permissions they declare. The user sees and approves requested permissions at install time.

## Package layout

```
my-plugin/
  manifest.json
  main.js
```

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

## Plugin types

| Type | Purpose |
|---|---|
| `source` | Provide series, chapters, and pages |
| `tracker` | Sync reading progress with an external service |
| `translator` | Translate OCR text |
| `metadata` | Supply covers, descriptions, tags |
| `filter` | Process page images |

## Source interface (JavaScript)

```js
// main.js
export async function search(query, page) {
  // returns { items: [{ id, title, cover }], hasNext: boolean }
}

export async function getSeries(id) {
  // returns { id, title, description, cover, tags: [] }
}

export async function getChapters(seriesId) {
  // returns [{ id, name, number, date }]
}

export async function getPages(chapterId) {
  // returns [{ index, url }]
}
```

## Host functions

| Function | Notes |
|---|---|
| `host.http.get(url, options)` | Allowed only for domains in `permissions.network` |
| `host.http.post(url, body, options)` | Same restriction |
| `host.html.parse(html)` | Returns a queryable document (CSS selectors) |
| `host.log(message)` | Debug logging |
| `host.prefs.get/set(key, value)` | Per-plugin settings, only if `permissions.storage` is true |

## Security model

- No `eval` of remote code and no dynamic imports from the network.
- Plugins are versioned, and repositories can sign their index.
- Execution time and memory limits per call.
- The app ships with no default repositories. Users add repositories themselves.

## Open questions

- QuickJS vs WASM as the primary runtime
- Declarative (JSON and CSS selector) sources for simple sites
- Repository index format and signing scheme
