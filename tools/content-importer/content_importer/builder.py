"""Assemble a schemaVersion-1 content package draft (docs/content-schema.md)
from parsed steps, plus a provenance sidecar recording source URL, retrieval
date, and checksum. Always DRAFT/PENDING — this module has no way to mark
content approved, by design."""

from __future__ import annotations

from dataclasses import asdict
from datetime import date

from .fetch import SnapshotMetadata
from .parser_nu_tahlil import DraftStep, ParseResult

DRAFT_MARKER_AR = "[DRAFT — Arabic title pending manual review, not yet transcribed by a human reviewer]"

AMALIYAH_ID = "tahlil"
VARIANT_ID = "tahlil-umum"
VERSION_ID = "tahlil-umum-v1"
APPROVAL_ID = "tahlil-umum-v1-approval"


def _step_id(index: int, total: int) -> str:
    width = max(2, len(str(total)))
    return f"{VERSION_ID}-step-{index:0{width}d}"


def _step_to_dict(step: DraftStep, position: int, total: int) -> dict:
    return {
        "id": _step_id(position, total),
        "position": position,
        "stepType": step.step_type,
        "titleId": step.title_id,
        "titleAr": None,
        "arabicText": step.arabic_text,
        "translationId": step.translation_id,
        "instructionId": None,
        "instructionAr": None,
        "repeatTarget": step.repeat_target,
        "quranSurahNumber": None,
        "quranAyahStart": None,
        "quranAyahEnd": None,
        "audioGroupId": None,
    }


def build_draft_package(parse_result: ParseResult, snapshot: SnapshotMetadata) -> dict:
    steps = parse_result.steps
    total = len(steps)
    today = date.today().isoformat()

    return {
        "schemaVersion": 1,
        "amaliyah": {
            "id": AMALIYAH_ID,
            "slug": "tahlil",
            "titleId": "Tahlil",
            "titleAr": DRAFT_MARKER_AR,
            "descriptionId": (
                "Draf transkripsi otomatis dari NU Online, belum ditinjau manusia. "
                "Bukan konten produksi."
            ),
            "descriptionAr": DRAFT_MARKER_AR,
            "category": "AMALIYAH",
        },
        "variant": {
            "id": VARIANT_ID,
            "slug": "umum",
            "nameId": "Umum",
            "nameAr": DRAFT_MARKER_AR,
            "ownerType": "PUBLIC",
            "pondokId": None,
            "visibility": "PUBLIC",
            "isDefault": True,
        },
        "version": {
            "id": VERSION_ID,
            "versionNumber": 1,
            "status": "DRAFT",
            "sourceName": (
                "NU Online — Bacaan Tahlil Singkat, Lengkap dengan Doa dan Terjemahannya "
                f"(automated draft transcription, unreviewed, retrieved {snapshot.retrievedAtUtc})"
            ),
            "sourceReference": snapshot.sourceUrl,
            "minimumAppVersionCode": 1,
            "publishedAt": None,
            "revokedAt": None,
        },
        "approval": {
            "id": APPROVAL_ID,
            "approverName": "PENDING — draft awaiting kyai/sesepuh review",
            "approverRole": "N/A",
            "institutionName": None,
            "approvalDate": today,
            "approvalScope": "N/A — automated draft transcription, not reviewed or approved",
            "publicDocumentStorageKey": None,
            "documentReferenceNumber": f"DRAFT-TAHLIL-NU-ONLINE-{today}",
            "status": "PENDING",
        },
        "steps": [_step_to_dict(step, i + 1, total) for i, step in enumerate(steps)],
    }


def build_provenance(snapshot: SnapshotMetadata, package_checksum_sha256: str) -> dict:
    return {
        "sourceId": snapshot.sourceId,
        "sourceUrl": snapshot.sourceUrl,
        "retrievedAtUtc": snapshot.retrievedAtUtc,
        "snapshotFile": snapshot.snapshotFile,
        "snapshotSha256": snapshot.sha256,
        "packageChecksumSha256": package_checksum_sha256,
        "note": (
            "Development draft only (DRAFT/PENDING). Not approved religious content. "
            "See docs/operations/CONTENT_GOVERNANCE.md before promoting this into "
            "app/src/main/assets/content/."
        ),
    }


def report_to_dict(parse_result: ParseResult) -> dict:
    report = parse_result.report
    return {
        "stepsExtracted": len(parse_result.steps),
        "preambleParagraphsSkipped": report.preamble_paragraphs_skipped,
        "ambiguousSections": report.ambiguous_sections,
        "possibleQuranAyahCandidates": report.possible_quran_ayah_candidates,
    }
