# Plugin packages and repositories

SoraTome ships with no plugins (release builds) and no repositories. Users install plugins themselves, from a file, from a link, or from a repository they add.

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
| API version | Must match the app (`apiVersion` 1) |
| Network permissions | Website names only, such as `api.example.org`. No addresses, no paths, no wildcards, no IP numbers |

Make one with any zip tool, for example:

```
zip -j my-plugin-1.0.0.soraplugin manifest.json main.js
sha256sum my-plugin-1.0.0.soraplugin
```

## Installing

Settings > Browse:

- **Install from file:** pick a package on the device.
- **Install from link:** paste an https link to a package.
- **Add repository:** paste an https link to a repository index, then install from its list.

Before installing or updating, SoraTome shows the plugin's name, version, where it came from, and the websites it is allowed to contact. Nothing is installed until you confirm.

An installed plugin with the same id as a bundled one (debug builds) replaces it; uninstalling brings the bundled one back. Reading progress is kept when a plugin is removed.

## Repository index

A repository is one JSON file at an https address:

```json
{
  "name": "My plugins",
  "plugins": [
    {
      "id": "org.example.sample",
      "name": "Sample",
      "version": "1.2.0",
      "apiVersion": 1,
      "language": "en",
      "description": "What this plugin does.",
      "url": "org.example.sample-1.2.0.soraplugin",
      "sha256": "64 lowercase hex characters"
    }
  ]
}
```

- `url` may be relative to the index address. It must be https.
- `sha256` is optional but recommended. It is the SHA-256 of the package file. SoraTome refuses a download that does not match.
- Plugins with a different `apiVersion` are not listed.
- The index can be up to 1 MB.

## Updates

**Check for updates** in Settings > Browse reads every added repository and offers newer versions of installed plugins. Updates also ask for confirmation.

## Safety

- A plugin runs in a sandbox. It can only reach the websites in its permissions, over https.
- Websites that resolve to a private or local network address are blocked.
- Page images are loaded only from the plugin's allowed websites.
- A checksum shows a download is intact; it does not prove who made it. Signed repositories are planned.
- SoraTome does not list or link to third-party repositories. Only install plugins from people you trust.

## Test repository

This project's own test repository is in the `plugin-repo` folder (see its README). It lists one plugin that reads MangaDex through its public API.

Suggested test:

1. Install `org.soratome.mangadex-0.1.0.soraplugin` with **Install from file**.
2. Add the test repository and tap **Check for updates**. Version 0.2.0 is offered.
3. Update, then open Browse and read a chapter.
4. Uninstall it (the bundled debug copy, if any, comes back).
