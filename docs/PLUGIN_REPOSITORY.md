# Plugin packages and stores

SoraTome ships with no plugins (release builds) and no stores. Users install plugins themselves, from a file, from a link, or from a plugin store they add. A **store** (also called a repository) is a list of plugins at an https address.

## Plugin package

A package is a **zip file** (use the extension `.soraplugin`; `.zip` also works) containing:

```
manifest.json
main.js          (the file named by "entry" in the manifest)
```

The files may also sit inside one folder in the zip.

Rules, checked before anything is installed:

| Rule | Limit |
|---|---|
| Package size | 2 MB |
| Files in the zip | 16, each up to 1 MB |
| File names | No `..`, no leading `/`, no backslashes |
| Plugin id | 3 to 80 characters: lowercase letters, digits, `.`, `_`, `-` |
| Version | Up to 32 characters, such as `1.2.0` |
| Plugin type | `source` (the only type supported for now) |
| Content warning | Optional `contentWarning`: `safe`, `mixed` (the default) or `adult` |
| API version | Must match the app (`apiVersion` 1) |
| Network permissions | Website names only, such as `api.example.org`. No addresses, no paths, no wildcards, no IP numbers |

Make one with any zip tool, for example:

```
zip -j my-plugin-1.0.0.soraplugin manifest.json main.js
sha256sum my-plugin-1.0.0.soraplugin
```

## Installing

Settings > Browse:

- **Plugin stores:** a card for each store you added (name, address, number of plugins) with **Website**, **Support**, **Copy** and **Remove**, a refresh button, and **Add**. Tap a store to see its plugins and install or update them.
- **Installed plugins:** switch plugins on or off, remove them, **Install from file** (a package on the device) or **Install from link** (an https link to a package), and **Check for updates**.
- **Allowed content warnings** and **Also apply to installed plugins**: see below.

Before installing or updating, SoraTome shows the plugin's name, version, content warning, where it came from, and the websites it is allowed to contact. Nothing is installed until you confirm.

An installed plugin with the same id as a bundled one (debug builds) replaces it; uninstalling brings the bundled one back. Reading progress is kept when a plugin is removed.

## Content warnings

Every plugin can label itself `safe`, `mixed` or `adult` (shown as 18+) in its manifest, and a store can repeat the label per plugin. A plugin with no label counts as `mixed`.

- **Allowed content warnings** (Settings > Browse): by default Safe and Mixed are allowed and 18+ is off. Turning on 18+ asks for confirmation.
- Plugins with a label that is off are hidden in stores and cannot be installed.
- **Also apply to installed plugins** (on by default): installed plugins with a label that is off stay listed but are not loaded. When off, plugins you already installed keep loading and updating whatever their label.
- The label is the author's claim. The app cannot check it, so a mislabelled plugin can still show content that does not match.

## Repository index

A repository is one JSON file at an https address:

```json
{
  "name": "My plugins",
  "website": "https://example.org",
  "support": "https://example.org/help",
  "plugins": [
    {
      "id": "org.example.sample",
      "name": "Sample",
      "version": "1.2.0",
      "apiVersion": 1,
      "language": "en",
      "description": "What this plugin does.",
      "url": "org.example.sample-1.2.0.soraplugin",
      "sha256": "64 lowercase hex characters",
      "contentWarning": "safe"
    }
  ]
}
```

- `name`, `website` and `support` describe the store. `website` and `support` (for example a community page) are optional and must be https; they appear as the **Website** and **Support** buttons on the store card.
- `contentWarning` is optional per plugin (`safe`, `mixed` or `adult`; default `mixed`).
- `url` may be relative to the index address. It must be https.
- `sha256` is optional but recommended. It is the SHA-256 of the package file. SoraTome refuses a download that does not match.
- Plugins with a different `apiVersion` are not listed.
- The index can be up to 1 MB.

## Updates

**Check for updates** (Installed plugins) reads every added store and offers newer versions of installed plugins. Updates also ask for confirmation.

## Safety

- A plugin runs in a sandbox. It can only reach the websites in its permissions, over https.
- Websites that resolve to a private or local network address are blocked.
- Page images are loaded only from the plugin's allowed websites.
- A checksum shows a download is intact; it does not prove who made it. Signed repositories are planned.
- SoraTome does not list or link to third-party stores. Only install plugins from people you trust.

## Test store

This project's own test store is in the `plugin-repo` folder (see its README). It lists one plugin that reads MangaDex through its public API.

Suggested test:

1. Install `org.soratome.mangadex-0.1.0.soraplugin` with **Install from file**.
2. Add the test store (Plugin stores > Add) and tap **Check for updates** in Installed plugins. Version 0.2.0 is offered.
3. Update, then open Browse and read a chapter.
4. Uninstall it (the bundled debug copy, if any, comes back).
