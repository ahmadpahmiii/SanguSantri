# Content Model

Applies to any task touching Room entities, the domain content model, the
seed importer, or the future backend content tables. The bundled JSON
seed format itself is documented separately in
[`docs/content-schema.md`](../content-schema.md) — read both together for a
seed-import task.

## Core hierarchy

```text
Amaliyah
└── Variant
    └── Immutable Version
        └── Ordered Steps
            └── Optional Assets
```

Example: `Tahlil → Umum → Version 1 → Step 1, Step 2, ...`. Future example:
`Tahlil → Umum | Pondok A → Version 1` (private pesantren variants,
`0.2.0`+).

One canonical model is defined in `domain/model/` and reused by both reader
modes (PRD §3.5) — it is never duplicated per reader mode. See ADR
[0006](../decisions/0006-content-schema-and-seed-import.md) for the
model-duplication reasoning (DTO/entity/domain still get separate classes
per boundary; the shared vocabulary enums do not).

## Step types

`HEADING`, `INSTRUCTION`, `ARABIC_TEXT`, `QURAN_AYAH`, `PRAYER`,
`REPEATED_READING`, `DIVIDER`, `CLOSING`.

A step may contain: Indonesian title, Arabic title, Arabic body, Indonesian
translation, Quran reference, repetition target, reader instruction, future
audio reference.

## Translation segmentation

For Quran content, translation maps to its corresponding ayah. For
non-Quran content, translation maps to a logical Arabic segment; long
prayers may be split into manageable segments, but meaning must not be
rearranged merely for visual convenience.

## Content immutability

Published versions are immutable. Any correction creates a new version. The
system must never mutate an already-approved version in place — see ADR
[0008](../decisions/0008-immutable-content-versions.md).

## Server tables (backend — not yet implemented)

### `amaliyah`

`id`, `slug`, `title_id`, `title_ar`, `description_id`, `description_ar`,
`category`, `status`, `created_at`, `updated_at`.

### `amaliyah_variants`

`id`, `amaliyah_id`, `slug`, `name_id`, `name_ar`, `owner_type`,
`pondok_id`, `visibility`, `is_default`, `created_at`. Initial values:
`owner_type = PUBLIC`, `pondok_id = null`, `visibility = PUBLIC`.

### `amaliyah_versions`

`id`, `variant_id`, `version_number`, `schema_version`, `status`,
`source_name`, `source_reference`, `approval_id`, `checksum_sha256`,
`minimum_app_version_code`, `published_at`, `revoked_at`, `created_at`.
Statuses: `DRAFT`, `IN_REVIEW`, `APPROVED`, `PUBLISHED`, `REVOKED`.

### `amaliyah_steps`

`id`, `version_id`, `position`, `step_type`, `title_id`, `title_ar`,
`arabic_text`, `translation_id`, `instruction_id`, `instruction_ar`,
`repeat_target`, `quran_surah_number`, `quran_ayah_start`,
`quran_ayah_end`, `audio_group_id`, `created_at`.

### `approvals`

`id`, `approver_name`, `approver_role`, `institution_name`,
`approval_date`, `approval_scope`, `document_storage_key`,
`public_document_storage_key`, `document_reference_number`, `status`,
`created_at`.

### `content_assets`

`id`, `asset_type`, `storage_key`, `checksum_sha256`, `size_bytes`,
`mime_type`, `language`, `created_at`.

### `feedback`

`id`, `installation_id`, `app_version`, `amaliyah_id`, `variant_id`,
`version_id`, `step_id`, `category`, `description`, `locale`, `status`,
`created_at`.

## Android Room tables

Android mirrors the content hierarchy above (implemented:
`AmaliyahEntity`, `AmaliyahVariantEntity`, `AmaliyahVersionEntity`,
`AmaliyahStepEntity`, `ApprovalEntity` — see `data/local/entity/`) and adds
reader-only state:

### `reading_sessions` (not yet implemented)

`id`, `version_id`, `reader_mode`, `advance_mode`, `current_step_id`,
`scroll_index`, `scroll_offset`, `started_at`, `last_opened_at`,
`completed_at`.

### `step_progress` (not yet implemented)

`session_id`, `step_id`, `current_count`, `is_complete`, `updated_at`.

### `feedback_outbox` (not yet implemented)

Local feedback payload, submission status, retry count, last error, created
time, last attempt time.

### `sync_metadata` (not yet implemented)

Last successful sync, manifest ETag, manifest checksum, content schema
version, last sync error.

User preferences remain in DataStore, not Room.

## Current implementation status

Implemented: canonical domain model, Room entities/DAOs for the content
hierarchy, versioned JSON seed schema, checksum-verified transactional
import (`data/local/seed/`). Not yet implemented: `reading_sessions`,
`step_progress`, `feedback_outbox`, `sync_metadata`, and the entire backend.
See `docs/PROGRESS.md` for the authoritative current state.
