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
- The bundled Tahlil/Istighosah fixtures are **non-production placeholders**:
  every Arabic/Indonesian text field is a bracketed placeholder, `version.status`
  is `DRAFT`, and `approval.status` is `PENDING` — the app must never present
  this as approved content. A release-blocking validation gate that fails the
  build when only such fixtures are bundled is **not yet implemented**;
  tracked as a follow-up, not silently skipped (PRD 6.3, 25).
- `getLatestPublishedForVariant` only returns `PUBLISHED` versions, so the
  current fixtures are importable and queryable but intentionally invisible
  to any future "default active version" read — consistent with never
  claiming unapproved content is approved.
