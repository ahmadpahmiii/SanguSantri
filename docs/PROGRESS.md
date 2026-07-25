# SanguSantri Engineering Progress

## Documentation, security, and production-readiness pass (pre-Milestone 3)

**Status:** Complete. Not a numbered milestone — no feature code shipped.
Full detail: `docs/reviews/audit-resolution.md`.

Resolved `docs/reviews/architecture-design-audit.md`: restructured
documentation into the `docs/{product,engineering,design,security,
operations,decisions,reviews}` tree, trimmed `CLAUDE.md` and
`docs/product/PRD.md` to remove duplication, added ADRs 0007–0011, and
re-enabled release R8/shrinking (`app/build.gradle.kts`,
`gradle.properties`; verified with `./gradlew assembleRelease`,
`detekt`, `ktlintCheck`, `testDebugUnitTest`, `lint`). Milestone 2
(Serambi) has not started — see Milestone 1 below for the actual current
implementation state; the `HEAD` commit is titled "milestone 2" but its
content matches Milestone 1.

## Milestone 0 — Android project foundation

**Status:** Implemented and verified locally — `ktlintFormat`, `detekt`,
`assembleDebug`, `assembleRelease`, `lint`, `test`, `connectedDebugAndroidTest`,
and a full `build` all pass (see Test Results in the implementation report).

**Scope:** Repair and establish the Android project foundation only. No
Tahlil/Istighosah content, Serambi, reader screens, counters, content
synchronisation, backend, auth, ads, or subscriptions.

### What shipped

- Fixed the application namespace/package from the scaffolded
  `com.sangusantri` to the PRD-mandated `com.sangusantri.app`, and moved
  sources into PRD 13.3 package boundaries (`core/designsystem`,
  `data/local`, `di`, `navigation`).
- Gradle version catalog (`gradle/libs.versions.toml`) with Hilt, Room,
  DataStore, Navigation 3, KSP, detekt, and ktlint at current stable
  versions; AGP/Kotlin/Gradle kept at their scaffolded, mutually-verified
  versions (AGP 9.2.1 requires Gradle 9.4.1 minimum, which is what the
  wrapper already pins).
- Jetpack Compose + Material 3 with a green Islamic identity theme
  (`SanguSantriTheme`), dynamic color intentionally disabled (PRD 13.8).
- Edge-to-edge via `enableEdgeToEdge()`, `adjustResize` already set in the
  manifest.
- Hilt DI graph: `SanguSantriApplication`, `MainActivity` as
  `@AndroidEntryPoint`, `DatabaseModule`, `DataStoreModule`.
- Navigation 3 skeleton (`SanguSantriNavHost`) with a single placeholder
  destination — explicitly not Serambi.
- Room skeleton: `SanguSantriDatabase` with one infrastructure entity
  (`app_metadata`); schema export enabled for future migration testing.
- DataStore preferences skeleton (`sanguSantriPreferencesDataStore`).
- Test foundations: JUnit4 local unit test, `HiltTestRunner` +
  `HiltTestApplication`, in-memory Room instrumented tests, DataStore
  instrumented test, Hilt DI graph instrumented test, Compose UI smoke test.
  `kotlinx-coroutines-test` is pinned to `1.9.0` (not the newer 1.11.0) to
  match the version the Compose BOM strictly constrains
  `kotlinx-coroutines-core` to — a higher `-test` version throws
  `NoSuchMethodError` at runtime against that older core.
- Deterministic formatting/lint: ktlint (`.editorconfig`) and detekt
  (`config/detekt/detekt.yml`), Android Lint left at AGP defaults.
- ADRs: `docs/decisions/0001`–`0005` (single module, built-in Kotlin/KSP,
  Room as source of truth, Navigation 3, Hilt).

### Test results

- `./gradlew test`: 2/2 JVM unit tests passed (`AppMetadataEntityTest`).
- `./gradlew connectedDebugAndroidTest` (Pixel_9 emulator, API 15/36):
  7/7 instrumented tests passed — `AppPackageNameTest`, `MainActivityTest`,
  `SanguSantriDatabaseTest` (3), `SanguSantriPreferencesTest`,
  `HiltModulesInstrumentedTest`.
- `./gradlew detekt`, `./gradlew lint`, `./gradlew ktlintFormat` /
  `ktlintCheck`: all pass.
- `./gradlew assembleDebug`, `assembleRelease`, `build`: all pass, including
  `lintVitalRelease`.

### Known limitations

- No canonical content model yet — deliberately deferred to the milestone
  that implements seed content import, so it is not invented ahead of that
  work.
- No Arabic typeface bundled; blocking production input per PRD 25.8.
- No localisation resources (`values-in`, `values-ar`) yet — deferred to the
  milestone that ships user-facing strings.
- `androidx.hilt:hilt-navigation-compose` and
  `androidx.lifecycle:lifecycle-viewmodel-navigation3` are not yet added;
  add them with the first `@HiltViewModel` screen.

### Next recommended milestone

Canonical content model + offline seed import (PRD FR-001, §12.2) — see
Milestone 1 below.

## Milestone 1 — Content foundation

**Status:** Implemented and verified locally — `ktlintFormat`, `detekt`,
`lint`, `testDebugUnitTest`, `connectedDebugAndroidTest` (Pixel_9 emulator,
API 15/36), and a full `build` (including `assembleRelease` /
`lintVitalRelease`) all pass.

**Scope:** Canonical amaliyah content model, versioned JSON seed schema,
non-production Tahlil/Istighosah fixtures, Room entities/DAOs/repository, and
an idempotent transactional seed importer. No Serambi, reader UI, counters,
network sync, backend, or auth — deliberately deferred to later milestones.

### What shipped

- **Domain model** (`domain/model/`): `Amaliyah`, `AmaliyahVariant`,
  `Approval`, `AmaliyahVersion`, `AmaliyahStep`, `AmaliyahVersionDetail`, and
  the shared `StepType`/`AmaliyahVersionStatus`/`ApprovalStatus`/
  `OwnerType`/`Visibility` enums (PRD 10, 11.1). `domain/repository/ContentRepository`
  defines the read contract; see ADR 0006 for the model-duplication
  reasoning.
- **Room** (`data/local/entity`, `data/local/dao`): `AmaliyahEntity`,
  `AmaliyahVariantEntity`, `ApprovalEntity`, `AmaliyahVersionEntity`,
  `AmaliyahStepEntity` with foreign keys/indices matching PRD 11.1, one DAO
  per entity following the existing `AppMetadataDao` convention.
  `SanguSantriDatabase` bumped to version 2 with a hand-written, schema-verified
  `MIGRATION_1_2` (no destructive fallback, per ADR 0003) and `Converters`
  for the shared enums.
- **Seed content schema** (`docs/content-schema.md`,
  `data/local/seed/dto/`): versioned (`schemaVersion: 1`) JSON manifest +
  package format under `app/src/main/assets/content/`. Bundled
  `manifest.json`, `tahlil-general-v1.json`, `istighosah-general-v1.json` are
  **non-production fixtures** — every Arabic/Indonesian field is a bracketed
  placeholder, `version.status = DRAFT`, `approval.status = PENDING` (PRD
  6.3, Content Safety).
- **Import pipeline** (`data/local/seed/`): `SeedContentChecksum` (pure
  SHA-256), `SeedContentValidator` (pure structural validation),
  `SeedContentSource`/`AssetSeedContentSource`, `SeedContentImporter`
  (checksum-verified, structurally-validated, transactional via
  `withTransaction`, per-package idempotent via an `existsById` check,
  per-package failure isolation per PRD 12.4).
- **Repository**: `ContentRepositoryImpl` reads Room via
  `data/mapper/ContentEntityMappers.kt`; bound through a new
  `di/ContentModule.kt` alongside the new DAOs registered in
  `di/DatabaseModule.kt`.
- ADR `docs/decisions/0006-content-schema-and-seed-import.md`.

### Test results

- `./gradlew testDebugUnitTest`: 16/16 JVM unit tests passed
  (`AppMetadataEntityTest`, `SeedContentChecksumTest`,
  `SeedContentValidatorTest`).
- `./gradlew connectedDebugAndroidTest` (Pixel_9 emulator, API 15/36): 12/12
  instrumented tests passed, including the four required seed-import
  scenarios in `SeedContentImporterTest` (first import, duplicate import is
  idempotent, invalid checksum rejected with no writes, a genuine SQLite
  constraint failure mid-import rolls back the whole package) and
  `SanguSantriMigrationTest` (migrate 1→2 with `MigrationTestHelper`,
  preserving existing `app_metadata` rows).
- `./gradlew detekt`, `./gradlew lint`, `./gradlew ktlintFormat` /
  `ktlintCheck`: all pass.
- `./gradlew assembleDebug`, `assembleRelease`, `build`: all pass, including
  `lintVitalRelease`.

### Known limitations

- No release-blocking content validation gate yet: the release build does
  not currently fail when only non-production fixtures are bundled (PRD 6.3,
  25). The fixtures are clearly marked non-production and never reach
  `PUBLISHED`/`APPROVED` status, but the enforcement mechanism itself is
  deferred — tracked, not silently skipped.
- Seed import is not yet wired into `SanguSantriApplication` startup; there
  is no Serambi to observe its result yet; this is the natural first thing
  the next milestone's bootstrap flow will call.
- `ContentRepository.getDefaultVersionDetail` only returns `PUBLISHED`
  versions, so it correctly returns `null` for the current `DRAFT` fixtures —
  expected until production content is approved and published.

### Next recommended milestone

Serambi (PRD FR-002, §7–8.1): wire `SeedContentImporter` into app startup,
render the Tahlil/Istighosah catalogue from `ContentRepository.observeAmaliyah()`,
and add the offline-first Compose UI test (open Serambi in airplane mode).
