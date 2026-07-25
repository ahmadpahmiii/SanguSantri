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
    # Canonical package identity (docs/content-schema.md) — declared here,
    # not derived from filenames/URLs, so adding a source is one reviewed
    # dict entry rather than a string-munging hack elsewhere in the tool.
    content_slug: str
    amaliyah_id: str
    amaliyah_slug: str
    amaliyah_title_id: str
    variant_id: str
    variant_slug: str
    variant_name_id: str
    version_id: str
    approval_id: str
    document_reference_prefix: str
    description_id: str


# One entry per approved editorial reference (PRD 6.1). Every generated
# package is always DRAFT/PENDING (builder.py) regardless of what is listed
# here — an entry only means "this tool may fetch and parse this URL", never
# "this content is approved" (docs/operations/CONTENT_GOVERNANCE.md).
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
        content_slug="tahlil-general-v1",
        amaliyah_id="tahlil",
        amaliyah_slug="tahlil",
        amaliyah_title_id="Tahlil",
        variant_id="tahlil-umum",
        variant_slug="umum",
        variant_name_id="Umum",
        version_id="tahlil-umum-v1",
        approval_id="tahlil-umum-v1-approval",
        document_reference_prefix="DRAFT-TAHLIL-NU-ONLINE",
        description_id=(
            "Draf transkripsi otomatis dari NU Online, belum ditinjau manusia. "
            "Bukan konten produksi."
        ),
    ),
    "istighosah-nu-online": SourceSpec(
        source_id="istighosah-nu-online",
        url="https://quran.nu.or.id/doa/istighotsah-mujahadah",
        display_name=(
            "Quran NU Online — Kumpulan Istighotsah & Mujahadah: "
            "Istighotsah (KH Romli Tamim)"
        ),
        snapshot_prefix="istighosah-nu-online",
        content_slug="istighosah-general-v1",
        amaliyah_id="istighosah",
        amaliyah_slug="istighosah",
        amaliyah_title_id="Istighosah",
        variant_id="istighosah-umum",
        variant_slug="umum",
        variant_name_id="Umum",
        version_id="istighosah-umum-v1",
        approval_id="istighosah-umum-v1-approval",
        document_reference_prefix="DRAFT-ISTIGHOSAH-NU-ONLINE",
        description_id=(
            "Draf transkripsi otomatis dari Quran NU Online (Istighotsah KH Romli Tamim), "
            "belum ditinjau manusia. Bukan konten produksi."
        ),
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
