#!/usr/bin/env python3
"""Pre-merge check for both content trees (ADR 0015, docs/operations/CONTENT_GOVERNANCE.md).

Mirrors the rules `data/content/ContentValidator.kt` and `data/content/ContentImporter.kt` apply on
device. The app fails closed on a bad catalog, so a mistake here does not put wrong text in front of
a reader — it makes content silently stop updating instead. Catching it on the pull request is the
difference between a red check and a quiet outage nobody notices for a week.

Checks both trees because they are meant to be the same contract served from two places:

  app/src/main/assets/content/      bundled baseline, ships inside the APK
  content-hosting/public/content/   Firebase Hosting, what remote sync fetches
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

SUPPORTED_SCHEMA_VERSION = 1
CONTENT_PATH_PREFIX = "/content/"
HTTPS_SCHEME = "https://"

TREES = (
    Path("app/src/main/assets/content"),
    Path("content-hosting/public/content"),
)

errors: list[str] = []


def fail(where: str, message: str) -> None:
    errors.append(f"{where}: {message}")


def is_origin_relative_content_path(url: str) -> bool:
    """Same rule as ContentValidator.isOriginRelativeContentPath.

    An absolute URL here would override Retrofit's base URL outright, and the bundled reader feeds
    the same string to AssetManager.open.
    """
    return (
        url.startswith(CONTENT_PATH_PREFIX)
        and "//" not in url
        and "\\" not in url
        and not any(c.isspace() for c in url)
        and ".." not in url.split("/")
    )


def is_allowed_image_url(url: str | None) -> bool:
    """Same rule as ContentValidator.isAllowedImageUrl."""
    if url is None:
        return True
    return url.startswith(HTTPS_SCHEME) and len(url) > len(HTTPS_SCHEME) and not any(c.isspace() for c in url)


def check_catalog_item(where: str, item: dict, seen_ids: set[str]) -> None:
    item_id = item.get("id", "")
    if not item_id.strip():
        fail(where, "catalog item id must not be blank")
        return
    if item_id in seen_ids:
        fail(where, f"duplicate catalog item id {item_id!r}")
    seen_ids.add(item_id)

    if item.get("version", 0) <= 0:
        fail(where, f"item {item_id}: version must be positive")
    for field in ("title", "description"):
        if not str(item.get(field, "")).strip():
            fail(where, f"item {item_id}: {field} must not be blank")

    content_url = item.get("contentUrl", "")
    if not content_url.strip():
        fail(where, f"item {item_id}: contentUrl must not be blank")
    elif not is_origin_relative_content_path(content_url):
        fail(where, f"item {item_id}: contentUrl {content_url!r} is not an origin-relative path under {CONTENT_PATH_PREFIX}")

    if not is_allowed_image_url(item.get("imageUrl")):
        fail(where, f"item {item_id}: imageUrl {item.get('imageUrl')!r} must be an https URL")


def check_content_file(where: str, item: dict, path: Path) -> None:
    item_id = item.get("id", "?")
    if not path.is_file():
        fail(where, f"item {item_id}: contentUrl points at {path}, which does not exist")
        return

    try:
        content = json.loads(path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, UnicodeDecodeError) as exc:
        fail(f"{path}", f"not valid UTF-8 JSON: {exc}")
        return

    if content.get("schemaVersion") != SUPPORTED_SCHEMA_VERSION:
        fail(f"{path}", f"unsupported schemaVersion {content.get('schemaVersion')}")

    # ContentImporter rejects a file whose id/version disagree with the catalog entry that named it,
    # so a mismatch here means the item would never import on any device.
    if content.get("id") != item_id:
        fail(f"{path}", f"id {content.get('id')!r} does not match catalog id {item_id!r}")
    if content.get("version") != item.get("version"):
        fail(f"{path}", f"version {content.get('version')} does not match catalog version {item.get('version')}")

    for field in ("sourceName", "sourceUrl"):
        if not str(content.get(field, "")).strip():
            fail(f"{path}", f"{field} must not be blank — attribution is mandatory (CONTENT_GOVERNANCE.md)")

    steps = content.get("steps") or []
    if not steps:
        fail(f"{path}", "steps must not be empty")
    step_ids: set[str] = set()
    for index, step in enumerate(steps):
        label = f"step[{index}] ({step.get('id', '?')})"
        step_id = str(step.get("id", ""))
        if not step_id.strip():
            fail(f"{path}", f"{label}: id must not be blank")
        elif step_id in step_ids:
            fail(f"{path}", f"{label}: duplicate step id")
        step_ids.add(step_id)

        if not str(step.get("arabicText", "")).strip():
            fail(f"{path}", f"{label}: arabicText must not be blank")
        if not str(step.get("translation", "")).strip():
            fail(f"{path}", f"{label}: translation must not be blank")
        if step.get("repeatTarget", 0) < 1:
            fail(f"{path}", f"{label}: repeatTarget must be at least 1")


def check_tree(tree: Path) -> dict | None:
    catalog_path = tree / "catalog.json"
    if not catalog_path.is_file():
        fail(str(tree), "catalog.json is missing")
        return None

    try:
        catalog = json.loads(catalog_path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, UnicodeDecodeError) as exc:
        fail(str(catalog_path), f"not valid UTF-8 JSON: {exc}")
        return None

    where = str(catalog_path)
    if catalog.get("schemaVersion") != SUPPORTED_SCHEMA_VERSION:
        fail(where, f"unsupported schemaVersion {catalog.get('schemaVersion')}")

    seen_ids: set[str] = set()
    for item in catalog.get("items", []):
        check_catalog_item(where, item, seen_ids)
        content_url = item.get("contentUrl", "")
        if is_origin_relative_content_path(content_url):
            check_content_file(where, item, tree / content_url[len(CONTENT_PATH_PREFIX):])

    return catalog


def main() -> int:
    catalogs = {tree: check_tree(tree) for tree in TREES}

    # The two trees serve the same contract, and remote sync never downgrades. A hosted entry older
    # than the bundled one for the same id would import on a fresh install and then be permanently
    # skipped by every sync — a state that is confusing to debug and easy to create by editing one
    # tree and forgetting the other.
    bundled, hosted = (catalogs[TREES[0]], catalogs[TREES[1]])
    if bundled and hosted:
        bundled_versions = {i["id"]: i.get("version") for i in bundled.get("items", []) if "id" in i}
        for item in hosted.get("items", []):
            bundled_version = bundled_versions.get(item.get("id"))
            if bundled_version is not None and item.get("version", 0) < bundled_version:
                fail(
                    "catalog consistency",
                    f"item {item['id']}: hosted version {item.get('version')} is older than the bundled "
                    f"version {bundled_version}, so remote sync would never apply it",
                )

    if errors:
        for error in errors:
            print(f"::error::{error}")
        print(f"\n{len(errors)} content problem(s) found.", file=sys.stderr)
        return 1

    print(f"Content OK — {len(TREES)} tree(s) validated.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
