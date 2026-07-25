"""Allowlisted sources and fetch limits.

Adding a source here is a deliberate, reviewed decision — this tool must
never fetch an arbitrary URL passed on the command line (CLAUDE.md: no
automatic scraping and publishing of religious content; PRD 6.3).
"""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class SourceSpec:
    source_id: str
    url: str
    display_name: str
    snapshot_prefix: str


# One entry per approved editorial reference (PRD 6.1). Istighosah is
# deliberately absent: PRD 6.2 only lists a *proposed*, not-yet-approved
# reference with no specific URL — see docs/operations/CONTENT_GOVERNANCE.md.
SOURCES: dict[str, SourceSpec] = {
    "tahlil-nu-online": SourceSpec(
        source_id="tahlil-nu-online",
        url=(
            "https://nu.or.id/nasional/"
            "bacaan-tahlil-singkat-lengkap-dengan-doa-dan-terjemahannya-UJz9F"
        ),
        display_name=(
            "NU Online — Bacaan Tahlil Singkat, Lengkap dengan Doa dan Terjemahannya"
        ),
        snapshot_prefix="tahlil-nu-online",
    ),
}

DEFAULT_SOURCE_ID = "tahlil-nu-online"

# A plain desktop-browser User-Agent; the source blocks the default Python
# urllib User-Agent with HTTP 403.
REQUEST_USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/120.0 Safari/537.36"
)

REQUEST_TIMEOUT_SECONDS = 15
MAX_RESPONSE_BYTES = 5 * 1024 * 1024  # 5 MiB — a news article page is far smaller than this.

SNAPSHOT_DIR_NAME = "snapshots"
OUTPUT_DIR_NAME = "output"
