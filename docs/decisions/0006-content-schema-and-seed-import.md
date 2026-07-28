# 0006: Versioned content schema and idempotent seed import

## Status

Accepted

## Context

Milestone 1 (PRD FR-001, §12.2, §10) needs a canonical amaliyah content model,
a bundled offline seed format, and an import path that is safe to run on
every launch without duplicating or corrupting local data.

## Decision

- **One canonical content model.** `Amaliyah` → `AmaliyahVariant` →
  `AmaliyahVersion` (+ `Approval`) → ordered `AmaliyahStep`s is defined once in
  `domain/model/` and reused by both reader modes (PRD 3.5), backed 1:1 by
  Room entities under `data/local/entity/`. `StepType`,
  `AmaliyahVersionStatus`, `ApprovalStatus`, `OwnerType`, and `Visibility` are
  plain Kotlin enums shared verbatim across the seed DTOs, Room entities, and
  domain models — the vocabulary has no per-layer meaning, so splitting it
  three ways would be duplication without a boundary reason (CLAUDE.md).
  Structurally different concerns (DTO parsing annotations, Room persistence
  annotations, plain domain data) still get separate classes per PRD 13.5.
- **Versioned JSON seed format** (`content-schema.md`,
  `schemaVersion: 1`) under `app/src/main/assets/content/`: one
  `manifest.json` listing packages with a declared SHA-256, and one file per
  immutable content version. Foreign keys are not repeated in the payload —
  the importer derives them from JSON nesting.
- **Import pipeline** (`data/local/seed/SeedContentImporter`): per package,
  read → verify checksum → parse → structurally validate
  (`SeedContentValidator`, pure Kotlin, no Android dependency) → import inside
  one `SanguSantriDatabase.withTransaction` block. Any failure at any stage
  yields a `SeedImportOutcome.Failed(reason)` for that package only; other
  packages in the same manifest are unaffected (PRD 12.4).
  **Superseded, Content Delivery Foundation (ADR 0012).** This
  seed-specific pipeline was renamed and extended into a shared,
  transport-agnostic pipeline once remote sync needed the same
  compare/import/replace logic bundled assets used:
  `SeedContentImporter`/`SeedContentValidator`/`SeedContentChecksum`/
  `SeedImportOutcome`/`SeedContentSource`/`AssetSeedContentSource` →
  `ContentPackageImporter`/`ContentPackageValidator`/`ContentChecksum`/
  `ContentImportOutcome` (`data/content/`) plus
  `BundledContentBootstrapper` (`data/local/content/`, reads
  `AssetManager` directly — no source-abstraction interface). The original
  decision recorded here (one canonical content model, a versioned JSON
  schema, checksum-verified transactional import, per-package idempotency)
  is otherwise unchanged — only the class names and the addition of
  version-compare/replace logic moved.
- **Idempotency** is a plain existence check: `amaliyah_versions.id` is the
  package's natural key, so re-running the importer against an
  already-imported version is a no-op (`AlreadyImported`), not a duplicate
  insert.
- **Migration, not destructive fallback.** Per ADR 0003's commitment, the
  content hierarchy tables are added via a hand-written `Migration(1, 2)`
  (`data/local/database/Migrations.kt`) whose SQL is copied verbatim from the
  Room-exported v2 schema, verified by an instrumented `MigrationTestHelper`
  test (`SanguSantriMigrationTest`) that upgrades a real v1 database and
  checks both the new tables and pre-existing `app_metadata` rows survive.

## Consequences

- Adding a variant field later still requires touching three files (DTO,
  entity, domain model) even though they're structurally distinct — accepted
  as the cost of PRD 13.5's boundary-duplication rule.
- **Updated, Milestone 6.** The bundled Tahlil/Istighosah packages are no
  longer non-production placeholders: both are real, NU Online/Quran NU
  Online-sourced content, explicitly accepted by the product owner as the
  `0.0.1` published release baseline under the risk-based publication model
  (`docs/product/PRD.md` §3.1, `docs/operations/CONTENT_GOVERNANCE.md`).
  `version.status` is `PUBLISHED`. `approval.status` (religious-authority
  approval) remains `PENDING` — optional for this standard-public-amaliyah
  content category, mandatory only for higher-risk content — and the app
  must never present it as if a kyai/sesepuh had signed off.
- `getLatestPublishedForVariant` only returns `PUBLISHED` versions. This is
  now how both bundled packages are resolved in every build (debug and
  release) — the previous `BuildConfig.DEBUG`-only fallback
  (`ContentRepositoryImpl.resolveVersion`) remains in place for any future
  content still in draft, but is no longer needed for Tahlil/Istighosah.
