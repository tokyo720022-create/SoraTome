#!/usr/bin/env python3
"""Builds a SoraTome plugin store from the plugins/ folder.

For every folder plugins/<id>/ that holds manifest.json and the script named in it, this
writes plugin-repo/<id>-<version>.soraplugin and lists it, with its checksum, in
plugin-repo/index.json. Store details (name, website, support) come from plugins/store.json.

The rules match the ones the app checks when installing, so a mistake shows up here first.
Run it with:  python3 tools/build_store.py
"""
import hashlib
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "plugins"
OUT = ROOT / "plugin-repo"

ID = re.compile(r"^[a-z0-9][a-z0-9._-]{2,79}$")
VERSION = re.compile(r"^[0-9A-Za-z][0-9A-Za-z.+-]{0,31}$")
DOMAIN = re.compile(r"^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$")
NUMBERS = re.compile(r"^[0-9.]+$")
WARNINGS = {"safe", "mixed", "adult", "18+"}
API_VERSION = 1


def fail(folder, message):
    sys.exit(f"{folder.name}: {message}")


def read_manifest(folder):
    path = folder / "manifest.json"
    if not path.is_file():
        fail(folder, "manifest.json is missing")
    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as error:
        fail(folder, f"manifest.json is not valid JSON ({error})")

    plugin_id = manifest.get("id", "")
    if not ID.match(plugin_id):
        fail(folder, f'the id "{plugin_id}" is not valid (lowercase letters, digits, dots, dashes)')
    if plugin_id != folder.name:
        fail(folder, f'the folder name must match the id "{plugin_id}"')
    if not VERSION.match(str(manifest.get("version", ""))):
        fail(folder, "the version is missing or not valid")
    if not manifest.get("name"):
        fail(folder, "the name is missing")
    if manifest.get("apiVersion") != API_VERSION:
        fail(folder, f"apiVersion must be {API_VERSION}")
    if str(manifest.get("type", "")).lower() != "source":
        fail(folder, 'type must be "source"')

    entry = manifest.get("entry", "main.js")
    if entry.startswith("/") or ".." in entry or not (folder / entry).is_file():
        fail(folder, f"the script {entry} is missing or its path is not allowed")

    for domain in (manifest.get("permissions") or {}).get("network", []):
        name = domain.lower()
        if not DOMAIN.match(name) or NUMBERS.match(name):
            fail(folder, f'"{domain}" is not a valid website name for permissions')

    warning = str(manifest.get("contentWarning", "mixed")).lower()
    if warning not in WARNINGS:
        fail(folder, 'contentWarning must be "safe", "mixed" or "adult"')
    return manifest, entry


def build_package(folder, manifest, entry):
    """A zip with fixed timestamps, so the same input always gives the same bytes."""
    target = OUT / f"{manifest['id']}-{manifest['version']}.soraplugin"
    names = ["manifest.json"] if entry == "manifest.json" else ["manifest.json", entry]
    with zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as package:
        for name in names:
            info = zipfile.ZipInfo(name, date_time=(2026, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            package.writestr(info, (folder / name).read_bytes())
    return target


def main():
    if not SOURCE.is_dir():
        sys.exit("There is no plugins/ folder.")
    OUT.mkdir(exist_ok=True)

    folders = sorted(path for path in SOURCE.iterdir() if path.is_dir())
    entries, keep = [], set()
    for folder in folders:
        manifest, entry = read_manifest(folder)
        package = build_package(folder, manifest, entry)
        keep.add(package.name)
        entries.append(
            {
                "id": manifest["id"],
                "name": manifest["name"],
                "version": str(manifest["version"]),
                "apiVersion": API_VERSION,
                "language": manifest.get("language"),
                "description": manifest.get("description"),
                "url": package.name,
                "sha256": hashlib.sha256(package.read_bytes()).hexdigest(),
                "contentWarning": str(manifest.get("contentWarning", "mixed")).lower(),
            }
        )

    # Drop packages of older versions.
    for old in OUT.glob("*.soraplugin"):
        if old.name not in keep:
            old.unlink()

    details = {}
    store_file = SOURCE / "store.json"
    if store_file.is_file():
        details = json.loads(store_file.read_text(encoding="utf-8"))

    index = {"name": details.get("name", "My plugin store")}
    for key in ("website", "support"):
        if details.get(key):
            index[key] = details[key]
    index["plugins"] = [{k: v for k, v in entry.items() if v} for entry in entries]

    (OUT / "index.json").write_text(json.dumps(index, indent=2) + "\n", encoding="utf-8")
    print(f"Built {len(entries)} plugin(s) into {OUT.name}/")


if __name__ == "__main__":
    main()
