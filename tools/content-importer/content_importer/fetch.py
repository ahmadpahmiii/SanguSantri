"""Download the allowlisted source page and store a local, gitignored snapshot."""

from __future__ import annotations

import hashlib
import json
import urllib.error
import urllib.request
from dataclasses import asdict, dataclass
from datetime import datetime, timezone
from pathlib import Path

from .config import (
    MAX_RESPONSE_BYTES,
    REQUEST_TIMEOUT_SECONDS,
    REQUEST_USER_AGENT,
    SOURCES,
    SourceSpec,
)


class FetchError(RuntimeError):
    pass


@dataclass(frozen=True)
class SnapshotMetadata:
    sourceId: str
    sourceUrl: str
    retrievedAtUtc: str
    httpStatus: int
    byteLength: int
    sha256: str
    snapshotFile: str


def _read_capped(response, max_bytes: int) -> bytes:
    """Read at most max_bytes + 1 from response, raising if the cap is exceeded.

    Reads in chunks rather than response.read() directly so an oversized or
    malicious response cannot be buffered fully into memory first.
    """
    chunks: list[bytes] = []
    total = 0
    chunk_size = 64 * 1024
    while True:
        chunk = response.read(chunk_size)
        if not chunk:
            break
        total += len(chunk)
        if total > max_bytes:
            raise FetchError(f"response exceeded the {max_bytes}-byte size limit")
        chunks.append(chunk)
    return b"".join(chunks)


def fetch_source(source_id: str, out_dir: Path) -> SnapshotMetadata:
    """Download `source_id`'s allowlisted URL and write a snapshot + metadata sidecar.

    Raises FetchError for anything outside the allowlist, a timeout, an
    oversized response, or a non-2xx HTTP status. Never follows a
    caller-supplied URL — only URLs already present in config.SOURCES.
    """
    spec: SourceSpec | None = SOURCES.get(source_id)
    if spec is None:
        raise FetchError(
            f"unknown source id {source_id!r}; allowlisted sources: {sorted(SOURCES)}"
        )

    request = urllib.request.Request(spec.url, headers={"User-Agent": REQUEST_USER_AGENT})
    try:
        with urllib.request.urlopen(request, timeout=REQUEST_TIMEOUT_SECONDS) as response:
            status = response.status
            body = _read_capped(response, MAX_RESPONSE_BYTES)
    except urllib.error.URLError as exc:
        raise FetchError(f"failed to fetch {spec.url}: {exc}") from exc

    if status < 200 or status >= 300:
        raise FetchError(f"unexpected HTTP status {status} fetching {spec.url}")

    retrieved_at = datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    checksum = hashlib.sha256(body).hexdigest()

    out_dir.mkdir(parents=True, exist_ok=True)
    date_stamp = retrieved_at[:10]
    snapshot_filename = f"{spec.snapshot_prefix}-{date_stamp}.html"
    snapshot_path = out_dir / snapshot_filename
    snapshot_path.write_bytes(body)

    metadata = SnapshotMetadata(
        sourceId=spec.source_id,
        sourceUrl=spec.url,
        retrievedAtUtc=retrieved_at,
        httpStatus=status,
        byteLength=len(body),
        sha256=checksum,
        snapshotFile=snapshot_filename,
    )
    meta_path = out_dir / f"{snapshot_filename}.meta.json"
    meta_path.write_text(json.dumps(asdict(metadata), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    return metadata


def latest_snapshot(source_id: str, out_dir: Path) -> Path:
    """Return the most recently fetched snapshot for `source_id`, without re-downloading."""
    spec = SOURCES.get(source_id)
    if spec is None:
        raise FetchError(f"unknown source id {source_id!r}")

    candidates = sorted(out_dir.glob(f"{spec.snapshot_prefix}-*.html"))
    if not candidates:
        raise FetchError(
            f"no snapshot found for {source_id!r} in {out_dir} — run `fetch` first"
        )
    return candidates[-1]
