# 0015: Simplified dynamic catalog content model, superseding ADR 0014's contract-preservation decision

## Status

Accepted

## Context

ADR [0014](0014-firebase-hosting-static-content-delivery.md) replaced the Go/Supabase backend
with static files on Firebase Hosting, but explicitly preserved the *existing* content contract
unchanged: the manifest/package shape, `variantId`/`versionId`/`versionNumber`/`checksumSha256`
identity model, and the claim that "no Android code changes" were needed. The product owner has
since decided that preservation itself was the wrong call — that contract (inherited from the
original Amaliyah/AmaliyahVariant/AmaliyahVersion/AmaliyahStep/Approval hierarchy, ADR 0006) is
unnecessarily complex for what is, in practice, a short, flat list of catalog items with ordered
reading steps. This ADR supersedes ADR 0014's contract-preservation and "no code change" claims
specifically; ADR 0014's actual backend decision (Firebase Hosting, static files, no Firestore/
Functions) is unchanged and remains in effect.

## Decision

**One flat `Content` per catalog item, no variant/version/approval hierarchy.** The former
four-level hierarchy (`Amaliyah` → `AmaliyahVariant` → `AmaliyahVersion` → `AmaliyahStep`, plus a
separate `Approval`) collapses into two tables: `content` (`id, title, description, imageUrl,
category, version, order, isActive, sourceName, sourceUrl`) and `content_steps` (`id, contentId,
position, arabicText, translation, repeatTarget`). `id` is a stable content identifier and the
navigation key (previously the amaliyah `slug` served this role implicitly; it is now explicit).
There is no separate `contentId`/`variantId`/`versionId` triple, no `checksumSha256`, no
`minimumAppVersionCode` per package, and no on-device `Approval` object.

**`version` is a plain incrementing integer per content id**, not an opaque immutable version
identifier. ADR [0008](0008-immutable-content-versions.md) (published content is immutable,
corrections create a new version, never mutated in place) is unaffected in spirit: a correction
still means publishing `version: N+1` with new step data, never editing an already-published file.
What changes is only the *representation* of that guarantee — a monotonic integer compared with
`>`/`==`/`<` (`decideContentVersionAction`) instead of a UUID-like id plus a separate checksum
comparison.

**Every step has the same shape — no step "type."** The former `StepType` enum (`HEADING`,
`INSTRUCTION`, `ARABIC_TEXT`, `QURAN_AYAH`, `PRAYER`, `REPEATED_READING`, `DIVIDER`, `CLOSING`) and
its associated optional fields (`titleId`/`titleAr`/`instructionId`/`instructionAr`/
`quranSurahNumber`/`quranAyahStart`/`quranAyahEnd`/`audioGroupId`) are removed. Every
`ContentStep` has exactly `arabicText`, `translation` (renamed from `translationId` — the value is
translated text, not an identifier), and `repeatTarget` (now required, minimum `1`, never `null`).
Array order in the content file is the step's position; there is no explicit `position` field on
the wire.

**Catalog + content-file contract** (shared verbatim between bundled assets and Firebase Hosting,
per ADR 0014):

```jsonc
// catalog.json
{
  "schemaVersion": 1,
  "items": [
    {
      "id": "tahlil", "title": "Tahlil", "description": "...",
      "imageUrl": null, "category": "Tahlil dan Doa",
      "version": 1, "contentUrl": "/content/packages/tahlil-v1.json",
      "order": 1, "isActive": true
    }
  ]
}
```

```jsonc
// packages/tahlil-v1.json
{
  "schemaVersion": 1, "id": "tahlil", "version": 1,
  "sourceName": "...", "sourceUrl": "...",
  "steps": [
    { "id": "tahlil-umum-v1-step-02", "arabicText": "...", "translation": "...", "repeatTarget": 1 }
  ]
}
```

**Content authors never invent Arabic text.** Where a section previously used a `HEADING` step
with only Indonesian title text and no Arabic body (real content, e.g. Tahlil's "Ayat Kursi",
"Doa Tahlil" section markers), the new schema has no field to hold a title-only step. Per an
explicit product-owner decision made during this migration (not an AI content judgement — see
`CLAUDE.md`'s absolute content-safety rules), these heading-only steps are **dropped**, not
converted into fabricated Arabic-text steps: Tahlil goes from 59 steps to 37 (25 `ARABIC_TEXT` +
12 `PRAYER`, all real reading content, all text copied verbatim); Istighosah goes from 27 to 25.
No Arabic text or translation was invented, altered, or reworded anywhere in this migration — only
whole heading-only steps were omitted, and every remaining step's `arabicText`/`translation` is
byte-identical to the previously published source content.

**Room migration, not a destructive reset.** `SanguSantriDatabase` moves from version 1 to 2 via
an explicit `Migration(1, 2)` (`MIGRATION_1_2`, `data/local/database/Migrations.kt`) —
`fallbackToDestructiveMigration` is not used. For each amaliyah, the migration resolves its default
variant's latest published version via the same joins `ContentRepositoryImpl.getDefaultVersionDetail`
used to perform at query time, writes one `content` row (keyed by the amaliyah's `slug`) and its
surviving `content_steps` rows (heading steps dropped, remaining steps renumbered to a dense
`1..N` sequence), then re-keys `reading_positions`/`guided_reading_sessions`/`step_progress` from
`versionId` to that same content id via a join against the old tables before dropping them.
Content metadata fields with no old-schema source (`imageUrl`, `order`, `isActive`) get a
placeholder (`NULL`/`0`/`1`) — safe because `BundledContentBootstrapper` unconditionally refreshes
every catalog item's metadata on the very next launch regardless of version, so the placeholder
never survives past the first post-migration bootstrap.

**Progress preservation is more generous than before, not less.** Previously, *any* version
replacement deleted all version-scoped reading/guided/step progress unconditionally (ADR 0012).
Under the flat model, a content update preserves `step_progress` rows for step ids that still
exist after the update and only deletes rows for steps that were genuinely removed
(`ContentStepDao`'s orphan-cleanup queries); a `guided_reading_sessions` row is kept if its
`currentStepId` still exists, deleted otherwise. `reading_positions` (Full Reader's scroll index)
is still reset on a step-list change, since an index-based position cannot be meaningfully
preserved once the underlying step list itself changes shape.

**The Android sync client's transport contract is unchanged; only the payload shape is.**
`ContentApiService` still does exactly two plain `GET`s (`content/catalog.json`, and a per-item
`@Url` fetch using the catalog's own `contentUrl`) — this part of ADR 0014 was accurate and stands.
What ADR 0014 got wrong was claiming the *payload* itself (manifest/package DTOs, checksum
verification) would stay the same; those DTOs, `ContentPackageImporter`, `ContentPackageValidator`,
`BundledManifestDto`, and `RemoteContentManifestDto` are all replaced by
`ContentCatalogDto`/`ContentFileDto` and `ContentImporter`/`ContentValidator`. Response-size
limiting (`docs/security/SECURITY_BASELINE.md`) moves from a manual per-call streaming cap
(`ContentSyncManager` streaming to a temp file) to a transparent OkHttp interceptor
(`ResponseSizeLimitInterceptor`), since `ContentApiService.getContent` now returns an
already-deserialized `ContentFileDto` rather than a raw `ResponseBody` to stream manually.

**No on-device approval object; source attribution remains.** `Approval`/`ApprovalStatus` and the
Reader's "Approved by" display are removed from the Android app entirely — a product-owner
decision made explicitly in this pass, not an AI content-governance judgement. `sourceName`/
`sourceUrl` remain first-class `Content` fields and are still shown in the Reader's source-info
dialog; only the separate religious-authority-approval object and its display are gone.
Publication status, source verification, and religious-authority approval remain distinct
concepts at the *content-governance process* level (`docs/operations/CONTENT_GOVERNANCE.md`) —
this decision only removes the on-device data structure and UI for the third one, since the
Android app never needs to render it.

**Table of Contents (FR-017) is removed, not adapted.** The Reader's Table of Contents bottom
sheet derived its sections entirely from `HEADING` steps' titles. With no `HEADING` step type and
no per-step title field, there is no data left to build a TOC from — `TocSection`,
`toTocSections()`, `ReaderTableOfContentsSheet`, and the "Buka Daftar Isi" overflow-menu action are
deleted rather than kept as dead code around an empty list.

## Alternatives rejected

* **Keeping ADR 0014's "no code change" claim and layering a thin catalog adapter over the old
  DTOs** — rejected; this would mean maintaining the entire old
  Amaliyah/Variant/Version/Approval/StepType surface area *and* a translation layer to a simpler
  external contract, which is more code and more concepts than either the old model alone or the
  new flat model alone.
* **Converting heading-only steps into steps with fabricated or reused Arabic placeholder text** —
  rejected outright; this would violate `CLAUDE.md`'s absolute rule against inventing Arabic
  amaliyah text, regardless of how minor the placeholder might seem.
* **Keeping `checksumSha256`** even though version comparison alone now suffices — rejected; a
  monotonic integer version fully determines "is this content stale" without needing byte-level
  integrity verification for content authored and committed directly to the same git repository
  that deploys it (unlike the previous era where a package might be independently republished by
  a separate Go publication pipeline with its own byte-level output).
* **A destructive Room migration (`fallbackToDestructiveMigration`)** to simplify the schema
  change — rejected; not needed, and remains prohibited regardless (ADR 0003, ADR 0012).

## Consequences

* `docs/content-schema.md`, `docs/engineering/ARCHITECTURE.md`, `docs/engineering/CONTENT_MODEL.md`,
  `docs/engineering/OFFLINE_FIRST.md`, `docs/operations/CONTENT_GOVERNANCE.md`,
  `docs/product/PRD.md`, `docs/product/ROADMAP.md`, and `docs/engineering/MCP_TOOLING.md` all
  needed updates in this same pass to describe the new catalog/content-file contract instead of
  the manifest/package/variant/version contract — see each document's own revision.
* ADR 0014's Firebase Hosting decision, repository layout intent, and MCP-as-tooling-only boundary
  are all unchanged by this ADR — only its contract-preservation claim is superseded.
* Developers with an existing local install must clear app data or reinstall if the Room migration
  is ever skipped or fails partway (it is not expected to, and is exercised by an instrumented
  migration test) — this is the same developer-facing caveat every prior schema change in this
  project has carried.
* Any future correction to the catalog/content-file shape must be reflected in
  `ContentCatalogDto`/`ContentFileDto` and `docs/content-schema.md` together, exactly as ADR 0012
  required for the superseded manifest/package DTOs.
