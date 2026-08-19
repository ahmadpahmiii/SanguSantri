# Content Schema (`schemaVersion: 1`)

Defines the canonical dynamic-catalog JSON format (PRD 12.2, FR-001, FR-010,
ADR [0015](decisions/0015-simplified-dynamic-catalog-content-model.md))
consumed identically by two producers: bundled Android assets
(`app/src/main/assets/content/`), hand-authored JSON validated in CI
(`tools/ci/validate_content.py`) before being committed; and the CMS API
(`../../cms/api`, deployed on Vercel), which serves the same shapes from the
Supabase database the CMS writes to. The static Firebase Hosting tree that
ADR [0014](decisions/0014-firebase-hosting-static-content-delivery.md)
introduced is gone — see `../../cms/docs/engineering/API.md` for the live
request/response reference. `ContentValidator`/`ContentImporter`
(`data/content/`) are the one shared validation/import boundary for both —
there is no bundled-only or remote-only copy of this schema. This is the
only place Arabic/Indonesian amaliyah text may live — never inside Kotlin
source (PRD 12.2, CLAUDE.md).

## One catalog, one content-file contract

Two payload kinds, identical whether they come from bundled assets or the
CMS API:

* **`catalog.json`** (`ContentCatalogDto`) — lists every content item's
  display metadata plus where to fetch its content file. Never carries
  step data itself.
* **A content file per item** (`ContentFileDto`, e.g. `packages/tahlil-v1.json`)
  — one content item's ordered reading steps plus source attribution.

There is no separate bundled-vs-remote manifest shape any more (ADR 0015
superseded the previous two-manifest design, ADR 0012) — the same
`catalog.json` format is read by `BundledContentBootstrapper` from
`app/src/main/assets/content/catalog.json` and by `ContentSyncManager` from
the CMS API's `GET /api/v1/catalog`.

The one shape difference is where a content file *lives*: bundled
`contentUrl`s point into the asset tree (`/content/packages/tahlil-v1.json`),
the API's point at its own route (`/api/v1/content/tahlil`). Both are
origin-relative, which is what `ContentValidator` pins — an absolute
`contentUrl` would let a tampered catalog pull amaliyah text from any host.

## Layout and debug/release split (introduced Milestone 4.5, published Milestone 6)

Android merges asset source sets per build type; a file at the same relative
path in `debug` wins over `main` for debug builds, and `main` alone is used
for release. This project uses that to keep any *unapproved* draft content
out of release builds while it is still being prepared, per CLAUDE.md's
debug content policy:

```text
app/src/main/assets/content/     # published content, visible in every build
├── catalog.json                 # items: [tahlil, istighosah]
└── packages/
    ├── tahlil-v1.json           # Milestone 6 baseline, migrated to the flat schema (ADR 0015)
    └── istighosah-v1.json       # Milestone 6 baseline, migrated to the flat schema (ADR 0015)

app/src/debug/assets/content/    # currently empty — reserved for a future
                                  # package still being drafted/reviewed,
                                  # not yet accepted for publication
```

Tahlil and Istighosah moved from `debug/` to `main/` in Milestone 6: both
are now the product owner's accepted, published `0.0.1` content baseline
(standard public amaliyah, `docs/product/PRD.md` §3.1, §6.7), so they are
visible in release builds like any other published content. The debug/
release split mechanism itself remains available for a future amaliyah
still being drafted and not yet accepted.

`BundledContentBootstrapper` is unaware of this split — it just reads
whatever `content/catalog.json` the build merged in and hands each item's
bytes to `ContentImporter`. There is no `DRAFT`-vs-`PUBLISHED` status field
in the new schema at all (ADR 0015 dropped the on-device `Approval`/status
object along with the rest of the old hierarchy) — `isActive` in the
catalog is the only publication-relevant flag: `isActive: false` hides an
item from Beranda without deleting it or its local progress.

## `catalog.json`

| Field                  | Type    | Required | Notes                                                                                                    |
|------------------------|---------|----------|------------------------------------------------------------------------------------------------------------|
| `schemaVersion`        | int     | yes      | Must equal `1`.                                                                                            |
| `items[]`              | array   | yes      | One entry per content item.                                                                               |
| `items[].id`           | string  | yes      | Stable content identifier and Android navigation key. Unique across the catalog.                          |
| `items[].title`        | string  | yes      | Beranda card title.                                                                                        |
| `items[].description`  | string  | yes      | Beranda card description.                                                                                 |
| `items[].imageUrl`     | string  | no       | Absolute or `/content/images/...`-relative URL. Omitted (not `null`) when no image exists yet.             |
| `items[].category`     | string  | no       | Free-form category label shown on the card if present.                                                    |
| `items[].version`      | int     | yes      | Plain incrementing integer, minimum `1`. Compared against Room's local version — no checksum (ADR 0015). |
| `items[].contentUrl`   | string  | yes      | Path to this item's content file, always rooted at `/content/...` (see Import behaviour below).           |
| `items[].order`        | int     | yes      | Beranda sort key (`ORDER BY order ASC`).                                                                    |
| `items[].isActive`     | boolean | yes      | `false` hides the item from Beranda without deleting its Room row or local progress.                        |

## Content file (e.g. `packages/tahlil-v1.json`)

One content item's metadata plus its ordered steps. `id`/`version` are
repeated here (not just in the catalog) so `ContentImporter` can verify a
fetched file actually matches the catalog entry that named it.

```json
{
  "schemaVersion": 1,
  "id": "istighosah",
  "version": 1,
  "sourceName": "...",
  "sourceUrl": "...",
  "steps": [
    {
      "id": "istighosah-umum-v1-step-02",
      "arabicText": "بِسْمِ اللهِ الرَّحْمَنِ الرَّحِيْمِ",
      "translation": "Dengan nama Allah Yang Maha Pengasih lagi Maha Penyayang.",
      "repeatTarget": 3
    }
  ]
}
```

| Field                    | Type   | Required | Notes                                                                                 |
|--------------------------|--------|----------|------------------------------------------------------------------------------------------|
| `schemaVersion`          | int    | yes      | Must equal `1`.                                                                          |
| `id`                     | string | yes      | Must match the catalog entry's `id` — importer rejects the file otherwise.               |
| `version`                | int    | yes      | Must match the catalog entry's `version` — importer rejects the file otherwise.          |
| `sourceName`             | string | yes      | Human-readable source attribution, shown in the Reader's source-info dialog.             |
| `sourceUrl`              | string | yes      | Source URL, recorded per the risk-based publication model (`docs/product/PRD.md` §3.1). |
| `steps[]`                | array  | yes      | Ordered reading steps — array order is the step's position, no explicit position field.  |
| `steps[].id`             | string | yes      | Stable step identifier, unique within the file. Referenced by `step_progress`/`guided_reading_sessions`. |
| `steps[].arabicText`     | string | yes      | Never blank.                                                                              |
| `steps[].translation`    | string | yes      | Never blank. The value is translated text, not an identifier (renamed from `translationId`). |
| `steps[].repeatTarget`   | int    | yes      | Minimum `1`. Every step has a repeat target — there is no "non-repeating" step kind.      |

There is no step "type" (`HEADING`/`INSTRUCTION`/`ARABIC_TEXT`/
`QURAN_AYAH`/`PRAYER`/`REPEATED_READING`/`DIVIDER`/`CLOSING` are all gone,
ADR 0015) and no per-step title, instruction text, or Quran-reference
fields — every step has exactly the four fields above. A previous,
since-superseded version of this document described a richer step model
with those fields; ADR 0015 replaced it because the flat shape is
sufficient for every real step this project's content has ever needed and
is far simpler to validate, render, and reason about.

### Structural validation (`ContentValidator`)

Enforced before any database write:

* Catalog: `schemaVersion` supported; every item's `id` non-blank and
  unique; `version` a positive integer; `title`/`description`/`contentUrl`
  non-blank.
* Content file: `schemaVersion` supported; `id`/`version` non-blank/positive;
  `sourceName`/`sourceUrl` non-blank; `steps` non-empty; step `id` values
  non-blank and unique; every step's `arabicText`/`translation` non-blank;
  every step's `repeatTarget` at least `1`.

## Import behaviour (`ContentImporter`, shared by bundled and remote — ADR 0012/0015)

1. Read/fetch `catalog.json`; validate `schemaVersion` and catalog
   structure. For every item, `refreshCatalogMetadata` unconditionally
   upserts the cheap fields (`title`/`description`/`imageUrl`/`category`/
   `order`/`isActive`) for a row Room already has — this is how
   `isActive: false`, reordering, and renaming propagate without fetching
   the item's content file at all.
2. Compare `items[].version` against Room's local `content.version` for
   that `id` (`decideContentVersionAction`): no local row → import; a
   lower incoming version → skipped, Room never downgraded; equal version →
   skipped (safe to re-run on every launch or sync, no checksum needed —
   ADR 0015 dropped `checksumSha256` entirely, a monotonic integer is
   sufficient once content is authored and deployed from the same git
   repository); a higher incoming version → import.
3. For an item actually worth importing, read/fetch its content file
   (bundled: strip the `contentUrl`'s `/content/` prefix and read the
   equivalent asset path; remote: `ContentApiService.getContent(contentUrl)`,
   response-size-limited by `ResponseSizeLimitInterceptor`). Verify the
   parsed file's `id`/`version` match the catalog entry that named it, then
   run structural validation. Failure at either step → item rejected,
   nothing written, the rest of the catalog is unaffected.
4. Write: a fresh import inserts the `content` row and its `content_steps`
   rows. A replace additionally preserves `step_progress` rows for step
   ids that still exist, deletes `step_progress`/`guided_reading_sessions`
   for step ids that no longer exist, and resets `reading_positions` (an
   index-based scroll position cannot be meaningfully preserved once the
   step list itself changes shape). Either way, all writes for one item
   happen inside one `SanguSantriDatabase.withTransaction { }` block — any
   failure rolls back the entire item atomically; no partial rows remain.

Each item is imported independently: one malformed or stale content file
must never block or partially corrupt another. Full algorithm and
failure/retry semantics: `docs/engineering/OFFLINE_FIRST.md`.

## Content safety

The packages bundled under `app/src/main/assets/content/packages/`
(Tahlil, Istighosah) are transcriptions from `tools/content-importer/`
against identified, publicly accessible sources (NU Online, Quran NU
Online), manually inspected for structural problems and explicitly
accepted by the product owner as the `0.0.1` published release baseline —
standard public amaliyah under the risk-based publication model
(`docs/product/PRD.md` §3.1, `docs/operations/CONTENT_GOVERNANCE.md`).
Claude must not invent or transcribe religious text from memory (CLAUDE.md,
PRD §6.3); both packages' Arabic text and translations remain exactly as
extracted from their source. The ADR 0015 migration to this flat schema
copied every surviving step's `arabicText`/`translation` verbatim from the
previously published package files — nothing was reworded. Section-heading
steps that carried only Indonesian title text and no Arabic body (real
content, e.g. "Ayat Kursi", "Doa Tahlil") were dropped rather than given
fabricated Arabic text, per an explicit product-owner decision recorded in
ADR 0015 — this is a structural omission of already-non-Arabic metadata,
not a correction or alteration of any Arabic/translation content. Any
future package still being drafted and not yet accepted for publication
belongs under `app/src/debug/assets/content/` instead (see above), so it
never reaches a release build before that decision is made.
