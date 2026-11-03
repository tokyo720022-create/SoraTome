# Publishing your own plugin store

A plugin store is just a folder of plugin packages and an `index.json` on the web. You can host one in this repository, or in any other GitHub repository, with no server.

## How it works here

1. Put each plugin in its own folder under `plugins/`, named after its id:

   ```
   plugins/
     store.json                     (name, website and support link of your store)
     org.example.sample/
       manifest.json
       main.js
   ```

2. `store.json` (all fields optional):

   ```json
   { "name": "My plugin store", "website": "https://example.org", "support": "https://example.org/help" }
   ```

3. The **Publish plugin store** workflow runs `tools/build_store.py`. For every plugin it writes `plugin-repo/<id>-<version>.soraplugin` and lists it, with its checksum and content warning, in `plugin-repo/index.json`. It commits the result for you.

4. To release a new version, change `version` in the plugin's `manifest.json`. Users see it under **Check for updates**.

The script checks the same rules the app checks when installing (id, version, permissions, content warning, files), so a mistake shows up as a failed run, not as a broken install.

## One-time setup

Add `.github/workflows/publish-store.yml` (from this project). Workflow files must be added by hand: GitHub does not let the unzip workflow add them.

## Adding the store to SoraTome

Any of these work in Settings > Browse > Plugin stores > **Add**, or in Installed plugins > **Install from link**:

- the raw link to `index.json`, for example
  `https://raw.githubusercontent.com/<owner>/<repo>/main/plugin-repo/index.json`
- the GitHub page of the repository, a folder, or the `index.json` file. SoraTome finds `index.json` in the repository root or in `plugin-repo/` (branches `main` and `master`)

An **Add to SoraTome link** also works on web pages that allow custom links:

```
soratome://add-store?url=https%3A%2F%2Fraw.githubusercontent.com%2F<owner>%2F<repo>%2Fmain%2Fplugin-repo%2Findex.json
```

The app asks before adding anything. GitHub's own README pages do not allow custom link schemes, so there you should show the plain https link instead.

## What a store is not

- A store for another app (for example a Mihon extension store) lists Android apps written for that app. It is not a SoraTome store and cannot be added. SoraTome stores list SoraTome plugins.
- SoraTome does not come with any store, and does not list or link to third-party stores.
