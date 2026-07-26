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

`QURAN_AYAH` represents a verse that is already part of an amaliyah's own
reading text (for example, Al-Fatihah inside Tahlil). There is no standalone
Quran feature and no Quran API (Kemenag, Quran Foundation, or otherwise) —
`QURAN_AYAH` text is entered and versioned as part of that amaliyah's own
content package, the same as any other step, and is never fetched separately
at runtime. See `docs/product/ROADMAP.md` and `docs/product/PRD.md` §6.4.

## Category taxonomy (Figma product-alignment pass — Jelajahi Amaliyah, `0.0.1`)

`amaliyah.category` already exists as a plain string field (both bundled
packages currently use the single value `"AMALIYAH"`). Jelajahi Amaliyah
(`docs/product/PRD.md` FR-020) needs a real, extensible taxonomy — initial
values proposed: `Tahlil & Doa`, `Shalawat`, `Ratib & Wirid`, `Musiman`.
This is a content-metadata change (editing `amaliyah.category`'s value in
the existing published packages), not a step-content change — `amaliyah`
sits above the versioned `variant → version → steps` tree (see Core
hierarchy above), so updating it does not trigger ADR 0008's
new-version-on-correction rule. No schema change is required: the field
already exists as a free-form string. The screen structure must not branch
on specific category values (`docs/product/PRD.md` FR-020) — treat the
taxonomy as data, not a fixed enum baked into UI logic, so future
categories need no code change. The actual JSON edits are an
implementation-phase task (Phase B), not made by the documentation pass
that added this note.

## Table of Contents sections (Figma product-alignment pass — `0.0.1`)

The reader Table of Contents (`docs/product/PRD.md` FR-017) needs "logical
reading sections" and their step ranges. This is derived at read time from
already-existing data — a section boundary is a `HEADING`-typed step, and
a section's range runs from that heading's `position` to the position
immediately before the next `HEADING` (or the end of the step list). **No
new column, table, or schema change is needed** — this keeps Phase A
inside the "no Room migrations this pass" constraint. If a future amaliyah
package's heading structure turns out not to segment cleanly this way,
revisit this note before assuming the derivation always holds.

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

`status` (readability/publication) and the linked `approvals.status`
(user-facing `Approved by` display, PRD §6.5) are deliberately independent
fields, not coupled into one enum: `status` controls whether the app can
display a version at all (`ContentRepositoryImpl.resolveVersion`'s
`PUBLISHED`-first, debug-only-fallback logic — `docs/content-schema.md`),
while `approvals.status` controls only what the compact `Approved by`
status shows. A version can be, and as of Milestone 6 genuinely is,
`PUBLISHED` (readable in every build) while its `approvals.status` stays
`PENDING` (no religious-authority sign-off) — this is the mechanism behind
the risk-based publication model (`docs/product/PRD.md` §3.1,
`docs/operations/CONTENT_GOVERNANCE.md`): standard public amaliyah publish
on `status = PUBLISHED` alone, while `approvals.status = APPROVED` remains
reserved for genuine kyai/sesepuh sign-off, mandatory only for higher-risk
content. The UI never conflates the two, and never infers one from the
other.

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

### `reading_positions` (implemented, Milestone 3)

`versionId` (primary key), `itemIndex`, `itemOffset`, `lastOpenedAtEpochMillis`.
One row per immutable content version — a new version (a correction) starts
its own position rather than inheriting the previous version's. This is the
Full Reader's minimum reading-position persistence (`ReadingPositionEntity`,
`ReadingPositionDao`) and is deliberately still separate from
`guided_reading_sessions` below — Full Reader scroll position and Guided
Reader step/completion state are different shapes of progress, kept in
different tables rather than one overloaded table.

### `guided_reading_sessions` (implemented, Milestone 4)

`versionId` (primary key), `currentStepId`, `lastOpenedAtEpochMillis`,
`completedAtEpochMillis` (nullable — set only after the user presses the
final completion confirmation, FR-007). One row per immutable content
version, mirroring `reading_positions`'s per-version keying
(`GuidedReadingSessionEntity`, `GuidedReadingSessionDao`). Narrower than the
`reading_sessions` table originally sketched here: `reader_mode` needs no
column since the table itself is guided-only, and `advance_mode` (the
automatic/manual progression preference) is a user preference in DataStore
(`ReaderSettings.guidedProgressionMode`), not per-content-version Room state.

### `step_progress` (implemented, Milestone 4)

`versionId`, `stepId` (composite primary key), `currentCount`,
`updatedAtEpochMillis` (`StepProgressEntity`, `StepProgressDao`). Completion
against a step's `repeatTarget` is derived at read time from content, not
stored as a redundant `is_complete` column.

Both tables are combined behind one `GuidedReadingRepository`
(`domain/repository/GuidedReadingRepository.kt`) rather than one repository
per table, per `CODING_STANDARD.md`'s no-duplicate-repository guidance.

### `favorites` (planned, Phase B `0.0.1` — not yet implemented)

`amaliyahId` (primary key), `addedAtEpochMillis`. One row per amaliyah the
user has favourited — deliberately keyed by `amaliyahId`, not
`versionId`, since favouriting is a user-curation action on the amaliyah
itself, not on a specific immutable content version. Backs both Beranda's
favourites section (FR-019) and Jelajahi Amaliyah's Favourite filter
(FR-020).

### `recently_opened` (planned, Phase B `0.0.1` — not yet implemented)

`amaliyahId` (primary key), `lastOpenedAtEpochMillis`. One row per amaliyah
ever opened, updated (not inserted again) on each open — `@Upsert`, same
pattern as `reading_positions`/`guided_reading_sessions`. Deliberately
**separate** from completion history (`0.0.3` Aktivitas scope,
`docs/product/PRD.md` FR-021): opening an amaliyah is not the same event
as completing it, and this table must not be read as if it were a
completion log.

### `tasbih_sessions` (planned, Phase C `0.0.2` — not yet implemented)

Forward-documented here for content-model completeness only; do not
create this table before Phase C. Expected shape: an identifier, a
`target` (33 / 100 / unlimited / custom positive integer — never 99, per
`docs/product/PRD.md` §0.0.2 requirements), an optional session/dhikr
name, `currentCount`, `updatedAtEpochMillis`. Independent from
`step_progress` — Standalone Tasbih is not tied to any amaliyah content
step.

### Reminder model (planned, Phase E `0.0.4` — not yet implemented)

Forward-documented only. Expected shape: personal schedule entries (e.g.
Tahlil malam Jumat, Istighosah weekly presets), a Gregorian/Hijri
date/time representation, and enough state to reschedule after device
reboot (WorkManager, `docs/engineering/OFFLINE_FIRST.md`). Not designed in
detail until Phase E is explicitly requested.

### `feedback_outbox` (removed from `0.0.1` scope)

Public content-correction feedback was removed from release `0.0.1`
(Milestone 5, `docs/product/PRD.md` FR-012) — content correction is an
internal SanguSantri-team operation, not a user-facing feature
(`docs/operations/CONTENT_GOVERNANCE.md`). This table was never
implemented and is not planned for any currently scheduled release.

### `sync_metadata` (removed from `0.0.1` scope)

Remote content synchronisation was removed from release `0.0.1` (Milestone
5, `docs/product/PRD.md` FR-010) along with the Go backend it depends on.
This table was never implemented and remains an unscheduled future item,
not a committed roadmap version (`docs/product/ROADMAP.md`).

User preferences remain in DataStore, not Room.

## Current implementation status

Implemented: canonical domain model, Room entities/DAOs for the content
hierarchy, versioned JSON seed schema, checksum-verified transactional
import (`data/local/seed/`), `reading_positions` (Full Reader
reading-position persistence), and `guided_reading_sessions`/`step_progress`
(Milestone 4 Guided Reader step/counter/completion persistence, also used
by Milestone 5's cross-mode progress mapping when switching between Full
and Guided readers). `feedback_outbox`, `sync_metadata`, remote content
synchronisation (FR-010), and the entire backend are removed from `0.0.1`
scope (Milestone 5) — not merely deferred; see
`docs/product/PRD.md`/`docs/product/ROADMAP.md`. `favorites` and
`recently_opened` are planned for Phase B of the `0.0.1` Figma
product-alignment work (not yet implemented); `tasbih_sessions` (Phase C,
`0.0.2`) and the reminder model (Phase E, `0.0.4`) are forward-documented
only. See `docs/PROGRESS.md` for the authoritative current state.

## Schema-freeze policy (pre-public-release)

The application has not been publicly released, so the current Room schema
is a **future production baseline candidate**, not yet frozen. Milestone 4
reset the schema version to **1** as a clean pre-release baseline —
consolidating the Milestone 1-3 migration chain (previously
`MIGRATION_1_2`/`MIGRATION_2_3`, now deleted) into one coherent set of
tables, since the app has no public release and no production users to
migrate. Developers with an existing local install must clear app data or
reinstall once after pulling that change (Room cannot open an on-disk
database at a higher version number than the app declares, and this project
deliberately does not use `fallbackToDestructiveMigration` to paper over
that — see below). Until the initial public schema is frozen:

* Local development data may be reset by a schema change when necessary —
  do not build a production migration chain prematurely, and prefer another
  clean version-1 baseline reset (as Milestone 4 did) over accumulating
  migrations for a schema no production install has ever run.
* Every schema change must still keep the Room schema internally coherent
  (foreign keys, indices, non-null constraints matching the domain model).
* Destructive migration (`fallbackToDestructiveMigration`) remains
  prohibited in any build that could reach a real user (ADR
  [0003](../decisions/0003-room-as-local-source-of-truth.md)) — this
  project has never enabled it, even during this pre-release phase;
  "local development data may be reset" above is handled by developers
  clearing app data, not by a destructive-migration code path.

**This baseline-reset policy ends the moment the initial public schema
ships.** From that point on, every schema change MUST ship a real, tested
Room `Migration` (as Milestone 1-3 did before this reset) — no further
version-reset-and-clear-app-data cycles once real installs hold real user
data.
