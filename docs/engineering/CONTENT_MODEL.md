# Content Model

Applies to any task touching Room entities, the domain content model, or
the content importer (bundled or remote). The canonical catalog/content-file
JSON format itself is documented separately in
[`docs/content-schema.md`](../content-schema.md) — read both together for
a content-import task.

## Core hierarchy (flat, ADR 0015)

```text
Content
└── Ordered ContentSteps
```

Example: `tahlil → step 1, step 2, ...`. The former four-level hierarchy
(`Amaliyah → Variant → Version → Step`, plus a separate `Approval`) was
collapsed into this flat shape by ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md) — a
content item's `id` (e.g. `"tahlil"`) is both its stable identifier and its
Android navigation key, and `version` is a plain incrementing integer on
the `Content` row itself, not a separate immutable entity.

One canonical model is defined in `domain/model/` (`Content`,
`ContentStep`, `ContentDetail`) and reused by both reader modes (PRD §3.5)
— it is never duplicated per reader mode. See ADR
[0006](../decisions/0006-content-schema-and-seed-import.md) for the
original model-duplication reasoning (DTO/entity/domain still get separate
classes per boundary — `ContentCatalogItemDto`/`ContentFileDto`/
`ContentEntity`/`Content` are four distinct classes for four distinct
boundaries — but a shared vocabulary is not duplicated three times for no
reason).

## No step "type" (ADR 0015)

The former `HEADING`/`INSTRUCTION`/`ARABIC_TEXT`/`QURAN_AYAH`/`PRAYER`/
`REPEATED_READING`/`DIVIDER`/`CLOSING` enum, and the per-type optional
fields it justified (title, instruction text, Quran surah/ayah reference,
audio group), are gone. Every `ContentStep` has exactly `arabicText`,
`translation`, and `repeatTarget` (minimum `1`, never absent) — see
`docs/content-schema.md`. A Quran verse embedded inside an amaliyah's own
reading text (e.g. Al-Fatihah inside Tahlil) is still just an ordinary step
with its Arabic text and translation; there is no separate `QURAN_AYAH`
reference tag any more. The standalone Al-Qur'an Kemenag feature approved for
`0.0.6` is a separate bounded data model and does not restore the removed
amaliyah step type — see `docs/product/QURAN_PRD.md` and ADR 0016.

## Standalone Quran bounded model (`0.0.6`, planned)

The standalone feature owns dedicated Room tables rather than forcing
official Kemenag records into the versioned amaliyah catalog:

* `quran_surahs` — official surah identity/name/category/verse-count fields.
* `quran_verses` — stable local key `(surah, ayat)`, unique remote ayat `id`,
  Juz/page metadata, official Usmani/gundul text, translation, and footnote
  fields. The API's Latin `teks` field MUST NOT be persisted or displayed.
* `quran_tafsir` — remote ayat id plus concise and tahlili text and fetch time.
* `quran_bookmarks` — local ayat-only bookmarks.
* `quran_reading_state` — one global last-read position and reading mode.
* `quran_reading_sessions` — local reading activity emitted only after the
  position advances by at least one ayat.

Quran settings that are simple scalar preferences belong in DataStore. Sync
timestamps/status reuse namespaced keys in `app_metadata`; do not create a
table solely for a timestamp. Network DTOs, Room entities, and domain models
remain separate boundary types. Official Arabic, translation, footnote, and
tafsir fields are preserved exactly as received; only numeric ordering,
structural validation, and UI presentation may transform them. Full field and
sync rules: `docs/product/QURAN_PRD.md` and
`docs/engineering/QURAN_API_CONTRACT_DRAFT.md`.

Section-heading text that previously existed only as a `HEADING` step's
Indonesian title (no Arabic body) has no home in the new schema. Where real
bundled content had these (Tahlil's "Ayat Kursi", "Doa Tahlil", etc.), the
ADR 0015 migration dropped the heading steps rather than inventing Arabic
text for them — a product-owner decision, not an AI content judgement (see
ADR 0015 and `docs/content-schema.md` §Content safety). One direct
consequence: **the Reader Table of Contents (former FR-017) is removed, not
adapted** — it derived its sections entirely from `HEADING` step titles,
and there is no data left to build one from. `TocSection`,
`toTocSections()`, `ReaderTableOfContentsSheet`, and the "Buka Daftar Isi"
overflow-menu action were deleted along with the step-type enum.

## Category taxonomy (Figma product-alignment pass — Jelajahi Amaliyah, `0.0.1`)

`Content.category` is a plain, optional string field (both bundled items
currently use `"Tahlil dan Doa"`). Jelajahi Amaliyah (`docs/product/PRD.md`
FR-020) needs a real, extensible taxonomy — initial values proposed:
`Tahlil & Doa`, `Shalawat`, `Ratib & Wirid`, `Musiman`. This is a
content-metadata change (editing a catalog item's `category` value), not a
step-content change, and does not require a version bump under ADR 0008's
correction rule (correcting reading content requires a new version;
correcting display metadata does not). The screen structure must not
branch on specific category values (`docs/product/PRD.md` FR-020) — treat
the taxonomy as data, not a fixed enum baked into UI logic.

## Translation segmentation

Translation maps to its corresponding logical Arabic segment; long prayers
may be split into multiple steps, but meaning must not be rearranged
merely for visual convenience.

## Content immutability

Published versions are immutable. Any correction creates a new version
(bump `version` in both the catalog entry and the content file). The
system must never mutate an already-published version in place — see ADR
[0008](../decisions/0008-immutable-content-versions.md), unaffected by ADR
0015's simplification of *how* a version is represented (a plain integer
instead of a UUID-like identifier plus checksum).

## Historical content record (no server database, ADR 0014/0015)

ADR [0011](../decisions/0011-go-and-supabase-managed-postgresql-backend.md)
originally planned a Postgres schema to hold this data server-side. That
backend was never implemented and was superseded by ADR
[0014](../decisions/0014-firebase-hosting-static-content-delivery.md):
there is no database anywhere in this architecture. The equivalent fields
live directly in the static catalog/content-file JSON
(`docs/content-schema.md`) — the same files whether bundled or served by
Firebase Hosting; there is no separate DB-schema representation to keep in
sync with them, and (since ADR 0015) no separate `Approval` object either —
`sourceName`/`sourceUrl` on `Content` are the only provenance fields the
Android app carries.

### Content history vs. Android retention (ADR 0012/0014/0015)

Full immutable revision history lives in `content-hosting/`'s git history
(ADR 0014), not a database: every published version is committed as a
distinct, never-edited content file, and a correction adds a new version
without deleting the historical one. Android retains **only the current
version per content id** — no previous-version browsing screen. When sync
or bundled bootstrap replaces a content item's active version
(`ContentImporter.importContentFile`, `docs/engineering/OFFLINE_FIRST.md`):

* `content_steps` are replaced wholesale (old rows deleted, new rows
  inserted) — a new version may have inserted, removed, or reordered
  steps.
* `step_progress` rows are **preserved** for step ids that still exist in
  the new version, and deleted only for step ids that no longer exist
  (`ContentStepDao`'s orphan-cleanup query) — this is more generous than
  the previous wholesale-delete-on-replace behaviour, since a flat content
  update is often a minor correction, not a full rewrite.
* `guided_reading_sessions` is kept if its `currentStepId` still exists in
  the new version, deleted otherwise.
* `reading_positions` (Full Reader's scroll index) is always reset on a
  version replacement — an index-based position cannot be meaningfully
  preserved once the step list itself may have changed shape.

A previous, since-superseded description of this document stated that
Android preserves previous content versions locally and falls back to the
newest non-revoked version when the active one is revoked (former PRD
FR-011) — that on-device fallback was never implemented in code; ADR 0012
removed the documentation for it, and ADR 0015 removed the underlying
`AmaliyahVersionStatus`/version-identity model it would have needed
entirely.

## Android Room tables

Android mirrors the flat hierarchy above (`ContentEntity`,
`ContentStepEntity` — see `data/local/entity/`) and adds reader-only state,
all now keyed by the stable content `id` rather than an immutable version
identifier:

### `content` / `content_steps` (implemented, ADR 0015)

`content`: `id` (primary key), `title`, `description`, `imageUrl`
(nullable), `category` (nullable), `version`, `order`, `isActive`,
`sourceName`, `sourceUrl`. `content_steps`: `id` (primary key),
`contentId` (foreign key, cascade delete), `position`, `arabicText`,
`translation`, `repeatTarget`. Replaces the former `AmaliyahEntity`/
`AmaliyahVariantEntity`/`AmaliyahVersionEntity`/`AmaliyahStepEntity`/
`ApprovalEntity` five-table hierarchy.

### `reading_positions` (implemented, Milestone 3; re-keyed by ADR 0015)

`contentId` (primary key), `itemIndex`, `itemOffset`,
`lastOpenedAtEpochMillis`. One row per content item — a version
replacement resets this row (see above). This is the Full Reader's minimum
reading-position persistence (`ReadingPositionEntity`,
`ReadingPositionDao`) and is deliberately still separate from
`guided_reading_sessions` below — Full Reader scroll position and Guided
Reader step/completion state are different shapes of progress, kept in
different tables rather than one overloaded table.

### `guided_reading_sessions` (implemented, Milestone 4; re-keyed by ADR 0015)

`contentId` (primary key), `currentStepId`, `lastOpenedAtEpochMillis`,
`completedAtEpochMillis` (nullable — set only after the user presses the
final completion confirmation, FR-007), `startedAtEpochMillis`. One row
per content item, mirroring `reading_positions`'s keying
(`GuidedReadingSessionEntity`, `GuidedReadingSessionDao`). `reader_mode`
needs no column since the table itself is guided-only, and `advance_mode`
(the automatic/manual progression preference) is a user preference in
DataStore (`ReaderSettings.guidedProgressionMode`), not per-content Room
state.

### `step_progress` (implemented, Milestone 4; re-keyed by ADR 0015)

`contentId`, `stepId` (composite primary key), `currentCount`,
`updatedAtEpochMillis` (`StepProgressEntity`, `StepProgressDao`). Completion
against a step's `repeatTarget` is derived at read time from content, not
stored as a redundant `is_complete` column.

Both tables are combined behind one `GuidedReadingRepository`
(`domain/repository/GuidedReadingRepository.kt`) rather than one repository
per table, per `CODING_STANDARD.md`'s no-duplicate-repository guidance.

### `favorites` (planned, Phase B `0.0.1` — not yet implemented)

`contentId` (primary key), `addedAtEpochMillis`. One row per content item
the user has favourited — keyed by the stable content `id`, not a version
identifier, since favouriting is a user-curation action on the item
itself. Backs both Beranda's favourites section (FR-019) and Jelajahi
Amaliyah's Favourite filter (FR-020).

### `recently_opened` (planned, Phase B `0.0.1` — not yet implemented)

`contentId` (primary key), `lastOpenedAtEpochMillis`. One row per content
item ever opened, updated (not inserted again) on each open — `@Upsert`,
same pattern as `reading_positions`/`guided_reading_sessions`. Deliberately
**separate** from completion history (`0.0.3` Aktivitas scope,
`docs/product/PRD.md` FR-021): opening a content item is not the same
event as completing it, and this table must not be read as if it were a
completion log.

### `tasbih_sessions` / `tasbih_history` (implemented, Milestone 9, `0.0.2`)

Unaffected by ADR 0015 — Standalone Tasbih has never referenced the
amaliyah content model. `tasbih_sessions` (`TasbihSessionEntity`): a
singleton row (fixed id), `currentCount`, `targetValue` (nullable — null
means unlimited), `targetPreset` (33 / 100 / unlimited / custom — never
99, per `docs/product/PRD.md` §0.0.2 requirements), an optional
`sessionName`, `startedAtEpochMillis`, `updatedAtEpochMillis`.
`tasbih_history` (`TasbihHistoryEntity`, autogenerated id): one row per
archived session (`sessionName`, `targetValue`, `finalCount`,
`startedAtEpochMillis`, `endedAtEpochMillis`), written by
`TasbihRepositoryImpl` whenever a non-zero-count session is reset **or**
its target is changed mid-session. Reused directly by Aktivitas (`0.0.3`,
Milestone 10).

### `amaliyah_completion_events` (implemented, Milestone 10, `0.0.3`)

`AmaliyahCompletionEventEntity` (autogenerated id): `amaliyahSlug`,
`amaliyahTitleId`, `versionNumber`, `completedAtEpochMillis`,
`durationMillis`. Deliberately kept under its original field names by ADR
0015 — this table is a denormalized snapshot log, not a live reference, so
it needed no schema change; only its call site changed
(`GuidedReaderViewModel.onConfirmCompletion` now passes the flat
`Content.id`/`Content.title`/`Content.version` as `amaliyahSlug`/
`amaliyahTitleId`/`versionNumber`). No foreign key to any content table —
the three fields are snapshots taken at completion time, so a later
content update or rename never changes past history (unlike
`reading_positions`/`guided_reading_sessions`/`step_progress`, which are
correctly re-keyed/pruned on version replacement, see above). Written
exactly once per valid Guided Reader completion action (FR-007), guarded
against a duplicate event on re-trigger. `durationMillis` is a real
snapshot (`completedAtEpochMillis - startedAtEpochMillis`, using
`GuidedReadingSession.startedAtEpochMillis` — set once when a session is
first created, never reset on step moves), never fabricated. Full Reader
has no completion concept, so only Guided Reader completions populate this
table.

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

### Content sync metadata (implemented, via `app_metadata`, no new table)

Remote sync bookkeeping (`ContentSyncMetadata`, `data/sync/`) is stored
through the existing generic `app_metadata` key-value table
(`AppMetadataEntity`/`AppMetadataDao`) rather than a new dedicated table.
One key: `content_last_sync` (`value` one of `SUCCESS`/`PARTIAL`/`FAILED`,
`updatedAtEpochMillis` the last *terminal* remote sync attempt — including
a terminal failure, which is what the 24-hour scheduling gate reads). No
manifest ETag or manifest-version bookkeeping exists — the catalog is
fetched plainly at most once every 24 hours, so there is nothing for
either key to usefully cache.

User preferences remain in DataStore, not Room.

## Current implementation status

Implemented: canonical flat domain model (`Content`/`ContentStep`), Room
entities/DAOs for it, dynamic catalog + content-file JSON schema,
transactional import shared by bundled and remote content
(`data/content/ContentImporter`), bundled bootstrap
(`data/local/content/BundledContentBootstrapper`), remote content
synchronisation (`data/remote/`, `data/sync/`, FR-010) against the static
Firebase Hosting catalog contract (ADR 0014/0015), `reading_positions`
(Full Reader reading-position persistence), and `guided_reading_sessions`/
`step_progress` (Guided Reader step/counter/completion persistence, also
used for cross-mode progress mapping when switching between Full and
Guided readers) — preserved by stable step id across a content update,
pruned only for genuinely removed steps (see above). `feedback_outbox`
remains removed from `0.0.1` scope — not merely deferred. Actually
deploying `content-hosting/` to Firebase Hosting (the project itself,
`sangusantri-81cc6`, is already linked; the CI pipeline that runs
`validate-content.mjs` + `firebase deploy` automatically is not yet
written) remains a separate task — the Android client against its
contract is implemented and degrades safely to bundled-only content until
a real deployment exists. `tasbih_sessions`/`tasbih_history` (`0.0.2`,
Milestone 9) and `amaliyah_completion_events` (`0.0.3`, Milestone 10) are
implemented and unaffected by ADR 0015. `favorites` and `recently_opened`
are planned for Phase B of the `0.0.1` Figma product-alignment work (not
yet implemented); the reminder model (Phase E, `0.0.4`) is
forward-documented only. See `docs/PROGRESS.md` for the authoritative
current state.

## Schema-freeze policy (pre-public-release) and the ADR 0015 exception

The application has not been publicly released, so the current Room schema
is a **future production baseline candidate**, not yet frozen. The general
pre-release convention established at Milestone 4 — reset the schema to a
clean version-1 baseline instead of writing a real migration, since there
are no production installs to protect — remains the default for future
schema changes. **ADR 0015 is an explicit, product-owner-directed exception
to that default**: rather than resetting to a new baseline and clearing
developer data, the version 1 → 2 change (dropping the Amaliyah/Variant/
Version/Approval hierarchy for the flat Content/ContentStep model) ships a
real, tested Room `Migration(1, 2)` (`MIGRATION_1_2`,
`data/local/database/Migrations.kt`) that preserves existing reading/guided/
counter progress across the change. This was a deliberate instruction for
this specific schema change, not a reversal of the general policy — a
future unrelated schema change may still use a clean baseline reset unless
told otherwise.

* Destructive migration (`fallbackToDestructiveMigration`) remains
  prohibited in any build that could reach a real user (ADR
  [0003](../decisions/0003-room-as-local-source-of-truth.md)) — this
  project has never enabled it, including for the ADR 0015 migration.
* Every schema change must still keep the Room schema internally coherent
  (foreign keys, indices, non-null constraints matching the domain model).

**This baseline-reset policy ends the moment the initial public schema
ships.** From that point on, every schema change MUST ship a real, tested
Room `Migration`, exactly as ADR 0015's already does — no further
version-reset-and-clear-app-data cycles once real installs hold real user
data.
