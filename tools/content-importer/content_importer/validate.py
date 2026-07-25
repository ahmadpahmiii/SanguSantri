"""Pure structural validation of a generated content package.

Mirrors `SeedContentValidator` (Kotlin,
`app/src/main/java/com/sangusantri/app/data/local/seed/SeedContentValidator.kt`)
rule-for-rule so a draft can be checked before ever touching the Android app
or the Gradle build (docs/content-schema.md "Structural validation"). Keep
the two in sync if either changes.
"""

from __future__ import annotations

SUPPORTED_SCHEMA_VERSION = 1

_ARABIC_TEXT_TYPES = {"ARABIC_TEXT", "PRAYER"}


def validate_package(pkg: dict) -> list[str]:
    """Return a list of validation failure reasons; empty means valid."""
    errors: list[str] = []

    if pkg.get("schemaVersion") != SUPPORTED_SCHEMA_VERSION:
        errors.append(f"unsupported schemaVersion {pkg.get('schemaVersion')!r}")

    amaliyah = pkg.get("amaliyah", {})
    if not amaliyah.get("id") or not amaliyah.get("slug"):
        errors.append("amaliyah.id/slug must not be blank")

    variant = pkg.get("variant", {})
    if not variant.get("id") or not variant.get("slug"):
        errors.append("variant.id/slug must not be blank")

    version = pkg.get("version", {})
    if not version.get("id"):
        errors.append("version.id must not be blank")
    if not isinstance(version.get("versionNumber"), int) or version.get("versionNumber", 0) <= 0:
        errors.append("version.versionNumber must be positive")

    approval = pkg.get("approval", {})
    if not approval.get("id"):
        errors.append("approval.id must not be blank")

    # Identifier errors are fatal to further structural checks in the Kotlin validator
    # (validateIdentifiers short-circuits validateSteps) — mirror that here.
    if errors:
        return errors

    steps = pkg.get("steps", [])
    if not steps:
        return ["steps must not be empty"]

    step_ids = [s.get("id") for s in steps]
    if any(not sid for sid in step_ids):
        return ["step.id must not be blank"]
    if len(set(step_ids)) != len(step_ids):
        return ["step.id values must be unique"]

    positions = [s.get("position") for s in steps]
    if any(not isinstance(p, int) or p <= 0 for p in positions):
        return ["step.position must be positive"]
    if len(set(positions)) != len(positions):
        return ["step.position values must be unique"]

    for step in steps:
        reason = _validate_step(step)
        if reason:
            return [f"step {step.get('id')}: {reason}"]

    return []


def _validate_step(step: dict) -> str | None:
    step_type = step.get("stepType")
    if step_type == "HEADING":
        if not step.get("titleId") and not step.get("titleAr"):
            return "HEADING requires titleId or titleAr"
        return None
    if step_type == "INSTRUCTION":
        if not step.get("instructionId"):
            return "INSTRUCTION requires instructionId"
        return None
    if step_type in _ARABIC_TEXT_TYPES:
        if not step.get("arabicText"):
            return f"{step_type} requires arabicText"
        return None
    if step_type == "REPEATED_READING":
        if not step.get("arabicText"):
            return "REPEATED_READING requires arabicText"
        repeat_target = step.get("repeatTarget")
        if not isinstance(repeat_target, int) or repeat_target <= 0:
            return "REPEATED_READING requires a positive repeatTarget"
        return None
    if step_type == "QURAN_AYAH":
        if not step.get("arabicText"):
            return "QURAN_AYAH requires arabicText"
        surah = step.get("quranSurahNumber")
        if not isinstance(surah, int) or surah <= 0:
            return "QURAN_AYAH requires a positive quranSurahNumber"
        ayah_start = step.get("quranAyahStart")
        if not isinstance(ayah_start, int) or ayah_start <= 0:
            return "QURAN_AYAH requires a positive quranAyahStart"
        ayah_end = step.get("quranAyahEnd")
        if ayah_end is not None and ayah_end < ayah_start:
            return "QURAN_AYAH quranAyahEnd must not precede quranAyahStart"
        return None
    if step_type == "DIVIDER":
        return None
    if step_type == "CLOSING":
        if not step.get("titleId") and not step.get("instructionId"):
            return "CLOSING requires titleId or instructionId"
        return None
    return f"unrecognised stepType {step_type!r}"
