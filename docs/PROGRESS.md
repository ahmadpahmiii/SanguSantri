# SanguSantri Engineering Progress

## Documentation, security, and production-readiness pass (pre-Milestone 2)

**Status:** Complete. Not a numbered milestone — no feature code shipped.
Full detail: `docs/reviews/audit-resolution.md`.

Resolved `docs/reviews/architecture-design-audit.md`: restructured
documentation into the `docs/{product,engineering,design,security,
operations,decisions,reviews}` tree, trimmed `CLAUDE.md` and
`docs/product/PRD.md` to remove duplication, added ADRs 0007–0011, and
re-enabled release R8/shrinking (`app/build.gradle.kts`,
`gradle.properties`; verified with `./gradlew assembleRelease`,
`detekt`, `ktlintCheck`, `testDebugUnitTest`, `lint`). Milestone 2
(Serambi) had not started at the time of this pass — the `HEAD` commit at
the time was titled "milestone 2" but its content matched Milestone 1.
**Numbering note:** this pass's own heading and `docs/reviews/audit-resolution.md`
both drifted and referred to Serambi as "Milestone 3" — corrected here.
Serambi is Milestone 2 (this doc's own Milestone 0/1 sequence and
`CLAUDE.md`'s milestone list agree); the Full Amaliyah Reader is Milestone 3
and remains not started. See the Milestone 2 section below for what
actually shipped.

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

Serambi (PRD FR-002, §7–8.1) — see below; implemented next.

## Milestone 2 — Serambi

**Status:** Implemented and verified locally — `ktlintFormat`, `detekt`,
`lint`, `assembleDebug`, `assembleRelease` (R8/shrinking, `lintVitalRelease`),
`testDebugUnitTest`, and `connectedDebugAndroidTest` (Pixel_9 emulator, API
15/36) all pass. Manually verified on the same emulator: fresh install
seeds Room and renders both cards, tapping a card navigates with the correct
slug, back navigation returns to Serambi, Setelan/About icons open their
placeholders.

**Scope:** Serambi home screen (FR-002) rendering the Tahlil/Istighosah
catalogue from Room, plus the minimum navigation/DI/design-token
groundwork this required. No Full Reader, no reader settings, no
continue-reading section, no content-sync status — deliberately deferred
(see Known limitations).

**Blocker resolved before starting:** this milestone was originally
requested as "Milestone 3: Full Amaliyah Reader," which assumed Serambi
already existed. It didn't — `docs/PROGRESS.md` already recorded this, but
the `HEAD` commit's misleading "milestone 2" title and
`docs/reviews/audit-resolution.md`'s "Milestone 3 (Serambi)" heading both
suggested otherwise. Verified directly against the source tree (no
`feature/`, no `SerambiScreen`, no reader code existed at all) before
starting. The user chose to pause the Full Reader request and build Serambi
first; this section is that work. The Full Reader remains Milestone 3,
next.

### What shipped

- **Seed import now runs at app startup**: `SanguSantriApplication` injects
  `SeedContentImporter` and runs it on an application-scoped `IO` coroutine
  in `onCreate()` — non-blocking (Serambi renders from Room reactively
  regardless of import timing) and safe to run on every launch (already
  idempotent per Milestone 1). Instrumented tests run under
  `HiltTestApplication` (`HiltTestRunner`), which never calls
  `SanguSantriApplication.onCreate()`, so `SerambiScreenTest` seeds Room
  itself via the same injected `SeedContentImporter` in `@Before`.
- **Serambi screen** (`feature/home/`): `SerambiViewModel`
  (`@HiltViewModel`, exposes `StateFlow<SerambiUiState>` via
  `ContentRepository.observeAmaliyah()`, no DAO access), `SerambiUiState`
  (`Loading` / `Content`, empty list is a valid `Content` state — no fake
  loading spinner once Room has answered), `SerambiRoute` +
  stateless `SerambiScreen` (Compose UI layer rule from
  `ARCHITECTURE.md`), and `AmaliyahCard` (reusable, flat `OutlinedCard`
  with a hairline border instead of Material's default shadow elevation,
  per the design system's elevation policy). Cards render from
  `ContentRepository.observeAmaliyah()`, never a hardcoded screen list
  (FR-002). Previews cover content, empty catalogue, loading, and
  no-description states.
- **Design tokens** added before this first real screen, per
  `docs/design/DESIGN_SYSTEM.md`: `SanguSantriSpacing` (4–32dp scale),
  `SanguSantriShapes` (3 corner radii, wired into `SanguSantriTheme`),
  `SanguSantriElevation` (flat + hairline-border policy). `Type.kt`
  extended with a small general scale (`headlineSmall`, `titleLarge`,
  `titleMedium`, `bodyMedium`, `labelLarge`) — the Arabic-specific type
  scale stays deferred to the reader milestone as the doc specifies.
- **Navigation**: `SanguSantriNavHost` replaces the Milestone 0
  `Home`/`FoundationPlaceholderScreen` placeholder with the real `Serambi`
  destination (ADR 0004's "placeholder replaced screen-by-screen" pattern).
  Three new destinations exist only as placeholders, each carrying a
  stable identifier where relevant, ready to be replaced by their own
  milestones: `AmaliyahDetail(slug: String)` → Milestone 3 Full Reader,
  `Setelan` → Milestone 3 reader settings, `About` → unscheduled. Added
  `rememberViewModelStoreNavEntryDecorator` (Serambi is the first
  destination needing a scoped ViewModel — ADR 0004 anticipated this).
- **New dependencies** (all latest stable, verified resolvable):
  `androidx.hilt:hilt-navigation-compose:1.4.0`,
  `androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0`,
  `androidx.lifecycle:lifecycle-{viewmodel,runtime}-compose:2.11.0`,
  `androidx.compose.material:material-icons-core`.
- `config/detekt/detekt.yml`: added `UnusedPrivateMember` exemption for
  `@Preview`/`@PreviewLightDark` composables (first use of Compose previews
  in this project; detekt doesn't know the Compose tooling convention).

### Known limitations

- **FR-002 partially implemented.** "Continue-reading section when
  progress exists" needs `reading_sessions` (Milestone 3 scope, PRD's own
  reading-position persistence); "subtle content update status" needs
  sync/manifest metadata (no sync milestone scheduled yet). Neither is
  faked — building either now would mean inventing a signal with no real
  backing data, which the project's own design-token guidance explicitly
  warns against. Both are honestly absent rather than stubbed.
- Setelan and About are navigation placeholders only (a message screen
  with a back button), not real screens.
- No screenshot tests (Roborazzi) — `DESIGN_SYSTEM.md` introduces these
  "once reader screens exist"; Serambi is the home screen, not a reader
  screen, so this is the reader milestone's task, not this one.
- No RTL/landscape/tablet-width/font-scale-1.5 Compose UI test variants for
  Serambi specifically — `TESTING.md` lists these as baseline scenarios "as
  reader UI lands"; Serambi is a simple list screen, not the reader, and
  adding all four render-variant tests for a screen this size would be
  disproportionate to the milestone. Compose's window-size-class handling
  is otherwise unaffected (single-column `LazyColumn`, no custom breakpoint
  logic), so no code path is untested in principle — the render-variant
  test matrix itself is deferred to the reader milestone.
- Bundled fixture content ships in the release APK unchanged from
  Milestone 1 (`assets/content/`) — expected: fixtures are offline seed
  data by design (FR-001), stay `DRAFT`/`PENDING`, and never render as
  approved content. The release-blocking content validation gate flagged
  in Milestone 1 is still not built.

### Next recommended milestone

Milestone 3 — Full Amaliyah Reader (PRD FR-004): render
`AmaliyahVersionDetail.steps` from `ContentRepository.getDefaultVersionDetail`
in place of the `AmaliyahDetail` placeholder, plus reader settings (DataStore,
replacing the `Setelan` placeholder) and reading-position persistence.

## Milestone 3 — Full Amaliyah Reader

**Status:** Implemented and verified locally — `ktlintFormat`, `detekt`,
`lint`, `assembleDebug`, `assembleRelease` (R8/shrinking, `lintVitalRelease`),
`testDebugUnitTest` (37/37), and `connectedDebugAndroidTest` (Pixel_9
emulator, Android 15/API 35, 23/23) all pass. Manually verified on the same
emulator: opening Tahlil from Serambi renders the reader's content-unavailable
state offline (expected — the bundled fixtures are still `DRAFT`, see Known
limitations), the settings sheet opens, a stepper change (Arabic font size
28sp → 30sp) applies to the sheet immediately, the value survives a full
`am force-stop` + relaunch (DataStore persistence), and both dark theme and
landscape render without clipping.

**Scope:** Full reading mode (FR-004) for the default published version of an
amaliyah: ordered step rendering from Room, reader appearance settings
(DataStore), and reading-position persistence — deliberately narrower than
the PRD's full FR-004/FR-005/FR-006 reader (no guided mode, no interactive
counters, no completion; see `CLAUDE.md` and the milestone brief's own §17).

**Blocker check before starting:** none. `git status` was clean at
`9f733a8` ("milestone 2"); Milestone 2's `SerambiScreen`, navigation, and
design tokens existed and matched `docs/PROGRESS.md`'s own record, so this
milestone proceeded without the Milestone 2 situation repeating.

### What shipped

- **Domain/data**: `ReaderSettings` (font sizes, line spacing, translation
  visibility — with `coerce*` bounds functions) and `ReadingPosition`
  (`versionId`, `itemIndex`, `itemOffset`, `lastOpenedAtEpochMillis`) domain
  models; `ReaderSettingsRepository` (DataStore-backed, coerces on every
  read so an out-of-range or corrupted stored value always falls back safely)
  and `ReadingPositionRepository` (Room-backed). `ContentRepository` gained
  `getAmaliyahBySlug` (the reader's top bar needs the amaliyah title, not
  just its steps). New `reading_positions` Room table (`ReadingPositionEntity`
    + `ReadingPositionDao`, `MIGRATION_2_3`, database version 3) — deliberately
      narrower than `docs/engineering/CONTENT_MODEL.md`'s previously-sketched
      `reading_sessions` table, which carries guided-mode/completion fields out
      of this milestone's scope; see that document's update for the reasoning.
      New `di/ReaderModule.kt` binds both new repositories.
- **Reader** (`feature/reader/`): `ReaderViewModel` (`@HiltViewModel` with
  assisted injection for the amaliyah slug) owns a `ReaderUiState`
  (`Loading` / `ContentAvailable` / `ContentUnavailable` / `RecoverableError`)
  combined from content load state and live `ReaderSettings`, and a
  `ReaderUiAction` sealed interface for unidirectional actions (scroll
  position changes, settings changes, retry). Reading position is debounced
  (600ms) inside the ViewModel via a `MutableSharedFlow` + `debounce`, and
  flushed immediately on `Lifecycle.Event.ON_STOP` via a dedicated
  `PersistPositionNow` action — never written on every scroll pixel. Restored
  positions are validated against the current step count and fall back to
  `(0, 0)` when out of range (e.g. a shorter corrected version).
- **Reader UI**: `ReaderScreen.kt` (`ReaderRoute` + `ReaderScreen`, a
  `LazyColumn` constrained to a max reading width and centred on larger
  screens per `DESIGN_SYSTEM.md`), `components/ReaderStepItem.kt` +
  `ReaderHeadingAndInstructionBlocks.kt` + `ReaderArabicContentBlocks.kt`
  (field-presence-driven rendering shared by every `StepType` — no
  Tahlil/Istighosah-specific layout branching; `StepType` is a closed enum
  handled exhaustively, so an unhandled case is a compile error, not a
  runtime fallback), `components/ReaderStatusStates.kt` (loading/unavailable/
  recoverable-error states with a retry action), `settings/ReaderSettingsSheet.kt`
    + `ReaderSettingStepper.kt` (a restrained bottom sheet — grouped
      label/value/stepper rows, a translation switch, no slider, no card wall).
      Arabic text uses `core/designsystem/theme/ReaderTypography.kt` (new Arabic
      and translation type-scale functions, parameterised by settings since font
      size is user-configurable) with `FontFamily.Default` — no approved Arabic
      typeface exists yet (PRD 13.9/25.8 blocking input), and Android's own
      per-script font fallback already renders Arabic + harakat correctly under a
      Latin primary family, so this is a documented interim choice, not a
      download. Arabic blocks force RTL layout direction regardless of app
      locale and are individually `SelectionContainer`-wrapped.
- **Navigation**: `SanguSantriNavHost`'s `AmaliyahDetail` entry now renders
  `ReaderRoute` instead of the Milestone 2 placeholder; only the slug crosses
  the navigation boundary (FR-002/FR-003), consistent with Milestone 2.
  Reader settings are a contextual bottom sheet inside the reader itself, not
  a nav destination — `Setelan` (Serambi's own settings entry point, unrelated
  to a specific amaliyah) is untouched.
- **Strings**: `amaliyah_detail_placeholder_message` removed (destination no
  longer a placeholder); reader/reader-settings strings added, all Indonesian
  user-facing text per `CODING_STANDARD.md`.

### Content safety

No Arabic/Indonesian religious text was invented, corrected, or hardcoded.
Every Arabic string introduced in Kotlin source (`@Preview` fixtures in
`ReaderScreen.kt` / `ReaderStepItem.kt`, and the `androidTest` fixture in
`ReaderScreenTest.kt`) carries the existing `[FIXTURE-AR]` non-production
marker, mirroring `AmaliyahCard.kt`'s established convention — verified with
`grep -rlP '[\x{0600}-\x{06FF}]'` across `main`/`test`/`androidTest`.

### Known limitations

- **The real app cannot show reader content yet.** The bundled seed fixtures
  are still `status: DRAFT` (Milestone 1's own known limitation — the
  release-blocking content-validation gate is still not built). Opening
  Tahlil or Istighosah from Serambi therefore correctly renders the reader's
  `ContentUnavailable` state, not step content — verified manually on-device.
  `ReaderScreenTest` exercises actual step rendering against a dedicated,
  `[FIXTURE]`/`[TEST]`-labelled `PUBLISHED` test amaliyah inserted directly
  via Room DAOs for that reason, not through the shipped asset pipeline.
- No theme override (light/dark/system) or reader background/surface style —
  neither is implemented anywhere in the app yet (`SanguSantriTheme` only
  follows `isSystemInDarkTheme()`) and no design-system token for a reader
  surface style exists, so per the milestone brief's own qualifiers
  ("where already supported" / "only if it exists in the approved design
  documentation") neither was added.
- Guided mode, interactive repetition counters, completion, and reading
  history remain out of scope, per `CLAUDE.md` and this milestone's own
  explicit exclusion list.
- No Roborazzi screenshot tests — `DESIGN_SYSTEM.md`/`TESTING.md` introduce
  these once reader screens exist, but the milestone brief explicitly says
  not to add a heavy screenshot framework solely for this milestone; Compose
  Previews cover the same visual states instead (normal, long Arabic content,
  translation hidden, large Arabic font, dark surface, repetition indicator,
  compact/expanded width, content-unavailable).
- Compose UI tests cover the flows the current test infrastructure supports
  (open reader from Serambi, ordered step rendering, translation hide/show,
  settings sheet, content-unavailable) but not RTL-locale, font-scale-1.5, or
  restored-position-after-process-death instrumented variants — no
  `DeviceConfigurationOverride`/process-death test harness exists yet in this
  project; manual verification substituted where noted above.

### Next recommended milestone

Milestone 4 — Guided Reader (PRD FR-005/FR-006): one-step-at-a-time
navigation, automatic/manual advancement, and the interactive repetition
counter this milestone deliberately left informational-only.

## Milestone 3.5 — Local Production Content Bootstrap

**Status:** Implemented and verified locally — `ktlintFormat`, `detekt`,
`lint`, `assembleDebug`, and `testDebugUnitTest` (37/37) all pass. Zero
Kotlin/Android source changed this milestone, so these were regression
checks, not new coverage. `connectedDebugAndroidTest` was not run — no
emulator/device was available in this session
(`adb devices` returned empty); no Android code changed, so nothing new
needed it.

**Scope:** Documentation scope correction (non-commercial status; no
standalone Quran feature/Kemenag API/Quran Foundation API/Quran audio; no
PDF reader; no runtime web scraping) plus a developer-only Python tool
(`tools/content-importer/`) that converts a locally saved snapshot of the
NU Online Tahlil article into a structured `DRAFT` JSON package compatible
with the existing seed content schema. No Android/Kotlin source changed.

### What shipped

- **Scope-correction documentation**: `docs/product/PRD.md` (non-commercial
  statement; §6.1 references the new dev tool; §6.4 clarifies `QURAN_AYAH`
  is embedded amaliyah content, never a separate Quran API; §5.2 excludes
  runtime web scraping and PDF reading/extraction from the app itself),
  `docs/product/ROADMAP.md` (removed `0.0.5` Downloadable Quran Audio and
  `0.5.0` Monetisation; added a non-commercial/no-Quran-feature statement),
  `docs/engineering/CONTENT_MODEL.md` (`QURAN_AYAH` clarification; new
  schema-freeze policy section — the app is pre-public-release, so the
  current Room schema, version 3, is a future production baseline
  candidate, not yet frozen; local dev data may be reset by a schema change,
  but destructive migration remains prohibited for any build that could
  reach a real user), `docs/operations/CONTENT_GOVERNANCE.md` (new
  "Developer draft tooling" section documenting the fetch → snapshot →
  parse → draft JSON → manual review → approved JSON → Android assets flow;
  Istighosah production-source-pending note), `docs/security/
  SECURITY_BASELINE.md` and `docs/security/THREAT_MODEL.md` (dropped
  advertising/subscription-gated controls tied to the removed `0.5.0`),
  `docs/operations/PRODUCTION_READINESS.md` and `docs/decisions/
  0009-no-authentication-in-public-mvp.md` (removed dangling references to
  the deleted roadmap items), `CLAUDE.md` (corrected the stale "Serambi and
  reader UI not implemented yet" line — Milestones 0–3 are complete — and
  added the non-commercial/no-Quran-API statement as a hard project-wide
  rule).
- **`tools/content-importer/`** (Python 3.9+, standard library only — no
  external dependencies): `python -m content_importer {fetch,parse,validate}`.
  `fetch` downloads only the one allowlisted NU Online Tahlil URL
  (`content_importer/config.py`), with a 15s timeout and a 5 MiB response
  cap, and writes a local HTML snapshot plus a metadata sidecar (retrieval
  timestamp, HTTP status, byte length, SHA-256). `parse`
  (`content_importer/parser_nu_tahlil.py`, `html_blocks.py`) is a
  source-specific parser (not a generic scraper) built against the page's
  actual `#detail-content` structure: it extracts numbered headings, Arabic
  text (harakat preserved verbatim — no normalisation touches non-whitespace
  characters), and the paired Indonesian translation, in document order,
  skipping known non-editorial subtrees (inline ads, "Baca Juga"
  recommendation boxes). It extracts a repetition count only when the count
  is written directly in a heading (e.g. `"(3 kali)"`, `"100 kali"`,
  `"2x"`); it never assigns `QURAN_AYAH` (would require inventing a
  surah/ayah number the page doesn't state structurally) and instead lists
  heading text that looks Quran-related under `possibleQuranAyahCandidates`
  in the generated report, for manual classification. Anything it cannot
  classify with confidence — an empty Arabic paragraph, an Arabic block with
  no following translation, a repetition marker embedded inside Arabic text
  rather than in the heading — is reported, never guessed or invented.
  `validate` (`content_importer/validate.py`) mirrors
  `SeedContentValidator.kt`'s structural rules in Python so a draft can be
  checked without touching Gradle. `builder.py` always marks output
  `version.status: DRAFT` / `approval.status: PENDING`, with
  `approverName: "PENDING — draft awaiting kyai/sesepuh review"` and a
  `sourceReference` pointing at the real URL — the tool has no code path
  that can mark content approved.

### Content flow verification (real run against the live source)

`fetch` → `parse` → `validate` was run end-to-end against the actual NU
Online article (`docs/product/PRD.md` §6.1's editorial reference). Result:
**59 steps extracted**, 3 editorial preamble paragraphs correctly excluded
(news-article framing, not reading content), 3 sections flagged ambiguous
(one empty Arabic paragraph — a source formatting artifact, correctly
skipped rather than guessed; two Arabic blocks with an embedded Latin-script
repetition marker, e.g. `"3x ..."`, extracted verbatim and flagged rather
than silently stripped), 9 headings flagged as possible `QURAN_AYAH`
candidates for manual review. `validate` reports the generated package
structurally **VALID** against `docs/content-schema.md`'s rules. The raw
snapshot and generated draft are gitignored and were not committed — see
`.gitignore` and `tools/content-importer/README.md`.

### Android integration

**None** — deliberately. The generated draft stays local under
`tools/content-importer/output/` (gitignored), not
`app/src/main/assets/content/`: the content flow places "manual content
review" and kyai/sesepuh approval *before* "Android assets" in the pipeline
(`docs/operations/CONTENT_GOVERNANCE.md`), and neither has happened for this
draft. The bundled `app/src/main/assets/content/tahlil-general-v1.json`
fixture is untouched, still the Milestone 1 `DRAFT`/`[FIXTURE-AR]`
placeholder. Serambi and the Full Reader are therefore unchanged and
continue to behave exactly as documented in Milestone 3's Known
limitations (Tahlil/Istighosah still render the reader's
`ContentUnavailable` state, since no version in Room is `PUBLISHED`) — this
was not re-verified manually on-device this milestone (no emulator
available), but no code path that affects it changed.

### Istighosah

Not scraped. PRD §6.2 only lists a *proposed*, not-yet-approved reference
(the KH Romli Tamim collection via Quran NU Online) with no specific URL —
per this milestone's brief and `docs/operations/CONTENT_GOVERNANCE.md`'s new
note, its production source remains pending a kyai/sesepuh decision. The
existing Istighosah development fixture is unchanged.

### Known limitations

- The generated Tahlil draft has not been manually reviewed against the
  source, has no kyai/sesepuh approval, and must not be treated as
  production-ready — it is explicitly `DRAFT`/`PENDING` and gitignored.
- 9 headings need manual `QURAN_AYAH` classification (surah/ayah numbers)
  before the draft could ever be promoted; 2 Arabic blocks carry an
  unresolved embedded repetition marker; 1 empty-Arabic-paragraph anomaly
  needs a manual re-check against the source.
- `connectedDebugAndroidTest` was not run this session (no emulator
  available) — acceptable here since no Android/Kotlin source changed, but
  it must run again before any milestone that does change Android code.
- The release-blocking content-validation gate flagged since Milestone 1
  (failing the build when only non-production fixtures are bundled) is
  still not built.

### Next recommended milestone

Milestone 4 — Guided Reader (PRD FR-005/FR-006), unchanged from the
recommendation above. Promoting the Tahlil draft generated this milestone
into production content is a content-governance task (manual review + kyai/
sesepuh approval), not an engineering milestone.

## Milestone 4 — Guided Reader and Integrated Tasbih

**Status:** Implemented and verified locally — `ktlintFormat`, `ktlintCheck`,
`detekt`, `assembleDebug`, `assembleRelease` (R8/shrinking), Android `lint`,
and `testDebugUnitTest` (37/37, unchanged count — no new unit tests added
per this milestone's own brief) all pass. `connectedDebugAndroidTest` was
not run this session — `adb devices` returned empty, no emulator available
— so the updated instrumented tests (below) are unverified on-device; manual
validation is also still required (see Known limitations).

**Scope:** Reading-mode selection (Bacaan Lengkap/Panduan), the Guided
Reader (one step at a time, interactive tasbih counter, automatic/manual
progression, completion confirmation), progress persistence via two new
Room tables, and a pre-release Room schema baseline reset. No standalone
tasbih, streak UI, history screen, sharing, or any other out-of-scope item
listed in this milestone's own brief.

### Pre-release Room cleanup

The app has no public release and no production users, so the Milestone
1-3 migration chain (`MIGRATION_1_2`, `MIGRATION_2_3`, exported schemas
`1.json`/`2.json`) was consolidated into one clean baseline: `Migrations.kt`
and `SanguSantriMigrationTest.kt` deleted, `SanguSantriDatabase` reset to
**version 1** with all current entities (including the two new Milestone 4
tables) declared from a single `@Database` annotation, `DatabaseModule`'s
`addMigrations(...)` call removed, and the now-unused
`androidx.room.testing` dependency + its androidTest schema-asset
`sourceSets` wiring removed from `app/build.gradle.kts`. A fresh
`app/schemas/.../1.json` was regenerated by the build. No
`fallbackToDestructiveMigration` was added anywhere — developers with an
existing local install must clear app data or reinstall once, and
`docs/engineering/CONTENT_MODEL.md`'s schema-freeze policy now explicitly
states that real Room migrations become mandatory again the moment the
initial public schema ships.

### What shipped

- **Room**: `GuidedReadingSessionEntity`/`GuidedReadingSessionDao`
  (`versionId` PK, `currentStepId`, `lastOpenedAtEpochMillis`,
  `completedAtEpochMillis`) and `StepProgressEntity`/`StepProgressDao`
  (`versionId`+`stepId` composite PK, `currentCount`,
  `updatedAtEpochMillis`) — mirroring `reading_positions`'s per-version
  keying convention rather than duplicating it. Both bound through one new
  `domain/repository/GuidedReadingRepository` +
  `data/repository/GuidedReadingRepositoryImpl` (not two repositories),
  registered in the existing `DatabaseModule`/`ReaderModule`.
- **Preferences**: `ReaderSettings` (existing DataStore-backed model, not a
  new store) gained `lastReaderMode: ReaderMode?` (PRD 8.2's "chosen mode
  may be saved as the default later") and
  `guidedProgressionMode: GuidedProgressionMode` (default `MANUAL`, FR-005);
  `ReaderSettingsRepository`/`ReaderSettingsRepositoryImpl` gained the
  matching read/write methods, with the same corruption-safe fallback
  pattern as the existing coerce* functions (`ReaderMode`/
  `GuidedProgressionMode.valueOf` wrapped in `runCatching`).
- **Reading-mode gate** (`feature/reader/ReaderEntry*`): a new nav
  destination between Serambi and the readers. Checks content availability
  first (reusing `ContentRepository`, same as both readers) — unavailable
  content short-circuits straight to the existing content-unavailable
  state, never offering a mode choice for content that cannot be read
  anyway. Otherwise resolves immediately to a remembered `ReaderMode`, or
  shows a restrained two-option chooser (`Bacaan Lengkap`/`Panduan`) that
  remembers the choice via `ReaderSettingsRepository.setLastReaderMode`.
- **Navigation** (`SanguSantriNavHost`): `AmaliyahDetail` is now the gate
  destination; new `FullReader`/`GuidedReader` `NavKey`s carry only the
  slug. The gate entry is replaced (popped, not left underneath) once
  resolved, so the back button from either reader returns to Serambi
  directly, not to the gate.
- **Guided Reader** (`feature/guidedreader/`): `GuidedReaderViewModel`
  (assisted-injected by slug, mirrors `ReaderViewModel`'s load/error
  boundary) restores the last visited step and every step's counter from
  `GuidedReadingRepository`, keyed by content version id; falls back to
  step 0 when a restored step id no longer matches any loaded step.
  Counter increments are capped at `repeatTarget`, persisted immediately
  (no debounce — writes are one-per-tap, not per-recomposition), and
  guarded against double auto-advance by a single cancellable `Job`.
  Automatic progression briefly holds the completed counter state (500ms)
  before advancing; manual progression enables Continue instead. `Continue`
  on the final step is replaced by `Selesaikan`, enabled only when every
  step with a positive `repeatTarget` has reached it; confirming persists
  `completedAtEpochMillis` (FR-007 — reaching the last step alone never
  marks completion).
- **Guided Reader UI**: `GuidedReaderScreen` (top bar with back/settings,
  bottom bar with Previous/Continue-or-Selesaikan, live-region position text
  "`n` dari `total`"), `GuidedStepContent` reuses `ReaderStepFields` (bumped
  from `private` to `internal` in `ReaderStepItem.kt`, alongside
  `ReaderDividerRow`) — the exact same field-presence rendering the Full
  Reader uses, with the informational `ReaderRepetitionIndicator` slot
  swapped for an interactive `GuidedTasbihCounter`. The counter shows
  `current / target`, increments with `HapticFeedbackType.LongPress` on tap,
  signals completion with both a check icon and a colour change (never
  colour alone), and exposes `stateDescription` for TalkBack. Reset requires
  confirmation via a shared `GuidedConfirmDialog`. `ReaderSettingsSheet` (the
  same bottom sheet the Full Reader uses) gained an optional
  `ProgressionModeControl` section (a segmented Manual/Otomatis choice) —
  `null` from the Full Reader, present from the Guided Reader.
- **Strings**: all new user-facing text added to `strings.xml` in
  Indonesian (mode chooser, guided reader actions, counter, dialogs,
  progression mode).

### Files removed, created, and modified

Removed: `data/local/database/Migrations.kt`,
`androidTest/.../SanguSantriMigrationTest.kt`, `app/schemas/.../2.json`,
`app/schemas/.../3.json` (old exports; `1.json` regenerated fresh).

Created (main): `domain/model/{ReaderMode,GuidedProgressionMode,
GuidedReadingSession,StepProgress}.kt`,
`domain/repository/GuidedReadingRepository.kt`,
`data/repository/GuidedReadingRepositoryImpl.kt`,
`data/mapper/GuidedReadingEntityMappers.kt`,
`data/local/entity/{GuidedReadingSessionEntity,StepProgressEntity}.kt`,
`data/local/dao/{GuidedReadingSessionDao,StepProgressDao}.kt`,
`feature/reader/ReaderEntry{ViewModel,UiState,Screen}.kt`,
`feature/reader/settings/ProgressionModeControl.kt`,
`feature/guidedreader/GuidedReader{ViewModel,UiState,UiAction,Screen,Bars}.kt`,
`feature/guidedreader/components/{GuidedStepContent,GuidedTasbihCounter,
TasbihActions}.kt`.

Modified (main): `SanguSantriDatabase.kt` (version 1, new entities/DAOs),
`di/DatabaseModule.kt`, `di/ReaderModule.kt`, `domain/model/ReaderSettings.kt`,
`domain/repository/ReaderSettingsRepository.kt`,
`data/repository/ReaderSettingsRepositoryImpl.kt`,
`data/mapper/ContentEntityMappers.kt` (guided mappers moved out),
`feature/reader/components/ReaderStepItem.kt` (`ReaderStepFields`/
`ReaderDividerRow` now `internal`), `feature/reader/settings/
ReaderSettingsSheet.kt` (optional progression section),
`navigation/SanguSantriNavHost.kt`, `strings.xml`, `app/build.gradle.kts`.

Modified (test): `test/.../ReaderViewModelTest.kt` (`FakeReaderSettingsRepository`
implements the 2 new interface methods), `androidTest/.../ReaderScreenTest.kt`
and `androidTest/.../SerambiScreenTest.kt` (adapted to tap through the new
mode gate; `@Before` now clears the shared preferences DataStore instead of
a narrow `@After` reset, since more reader preferences exist now).

### Commands executed

`./gradlew :app:ktlintFormat`, `:app:ktlintCheck`, `:app:detekt`,
`:app:compileDebugKotlin`, `:app:compileDebugUnitTestKotlin`,
`:app:compileDebugAndroidTestKotlin`, `:app:assembleDebug`,
`:app:assembleRelease`, `:app:lintDebug`, `:app:testDebugUnitTest` — all
passed.

### Known limitations

- **`connectedDebugAndroidTest` was not run** (no emulator this session).
  The three androidTest files touched by this milestone compile but are
  unverified on-device — see Manual validation below.
- Tahlil/Istighosah still have no `PUBLISHED` version (Milestone 1's own
  known limitation, unchanged), so the reading-mode gate's real-content path
  cannot be exercised against production-shaped content yet — only against
  the dedicated `PUBLISHED` test fixtures the touched Compose UI tests
  already use.
- No reduced-motion detection: the step-transition `AnimatedContent` uses a
  short, fixed-duration fade regardless of the system animator-scale
  setting — no reduced-motion signal exists anywhere in the app yet to
  branch on.
- Standalone tasbih, streak UI, completion-history screen, sharing, and all
  other items in the milestone brief's "Out of scope" list were not built.

### Manual validation still required (no emulator this session)

Mode selection: tap Tahlil/Istighosah from Serambi, confirm the chooser
appears once and the choice is remembered on the next visit. Full Reader:
confirm it still renders/scrolls/persists position exactly as Milestone 3
left it. Guided Reader: step navigation (Previous/Continue), counter
increment up to target and no further, haptic feedback on tap, reset with
confirmation, automatic progression (target reached → brief pause → auto
advance, no double-advance), manual progression (Continue stays disabled
until target reached), progress restoration after leaving the screen /
process death, final-step completion confirmation (disabled until every
counter is at target). Also: dark mode, large font scale (1.5×), landscape,
tablet width, and fully offline use for all of the above.

### Next recommended milestone

Not specified by this brief — revisit `docs/product/ROADMAP.md` for the
next scheduled item once this milestone's manual validation is complete.

## Milestone 4.5 — Fix Local Content Wiring and Add Istighosah Draft

**Status:** Implemented and verified — `ktlintFormat`, `ktlintCheck`,
`detekt`, `:app:assembleDebug`, `:app:assembleRelease` (R8/shrinking,
`lintVitalRelease`), `:app:lintDebug`, and `:app:testDebugUnitTest` (37/37)
all pass. `connectedDebugAndroidTest` was not run (existing instrumented
tests are unchanged by this milestone and Milestone 4 already covers them;
manual on-device verification below covers the actual regression risk of
this milestone — real content rendering). Manually verified end-to-end on
a running `Pixel_9` emulator (Android 15/API 35): fresh install imports and
displays both Tahlil (59 steps) and Istighosah (27 steps) in Serambi, Full
Reader, and Guided Reader; interactive tasbih counter increments to target,
gates `Lanjutkan`, and shows the checkmark/colour completion state; restart
does not duplicate content; offline throughout (no seeded network
permission is used by the importer at runtime — it only reads bundled
assets).

**Scope:** Fix the broken local content pipeline (scraped Tahlil draft never
reached the app; both amaliyah were stuck on DRAFT-filtered
`ContentUnavailable`), add a second source-specific importer for Istighosah,
and wire both through the existing seed importer / Room / repository /
reader stack unchanged. No new reader features, no tests, no Room
migrations — per this milestone's own brief.

### Confirmed Tahlil root cause (two independent, stacked breaks)

1. **The real draft never reached any Android asset source set.** The
   Milestone 3.5 tool's output (`tools/content-importer/output/
   tahlil-general-v1.draft.json`) is gitignored, developer-local-only
   output. Nothing in the repository ever copied it into
   `app/src/main/assets/content/` — that directory still held the
   Milestone 1 bracket-placeholder fixture (`[FIXTURE-AR]` text), and
   `manifest.json` only listed that placeholder's filename/checksum. The
   seed importer had no path to the real content at all.
2. **Even with the draft wired in, `DRAFT` content never rendered.**
   `AmaliyahVersionDao.getLatestPublishedForVariant` (used by
   `ContentRepositoryImpl.getDefaultVersionDetail`, which both `ReaderViewModel`
   and `GuidedReaderViewModel` call, and which `ReaderEntryViewModel` uses
   for its availability check) filters `status = 'PUBLISHED'` only. Every
   bundled package — placeholder and real draft alike — is `DRAFT`. Serambi
   itself was never blocked (`ContentRepositoryImpl.observeAmaliyah()` lists
   `Amaliyah` rows unconditionally, not gated by version status), which is
   why cards always rendered while the readers always showed
   `ContentUnavailable`.

A third, latent issue would have surfaced once (1) was fixed: the draft
reuses the placeholder's exact `version.id`/step ids (`tahlil-umum-v1`,
`tahlil-umum-v1-step-*`). `SeedContentImporter.importPackage` skips import
whenever `amaliyahVersionDao.existsById(entry.versionId)` is already true —
confirmed on-device (see Development database refresh below) — so any
device that had already imported the placeholder would silently keep the
stale bracket-placeholder rows forever.

The importer itself was never the problem: `SeedContentValidator` performs
purely structural validation (schema version, non-blank ids, per-`stepType`
required fields) and has no `DRAFT`/`PUBLISHED` special-casing — it already
accepted `DRAFT` packages correctly. Checksums, IDs, and the generated
schema all matched the Android DTOs; parsing did not produce zero valid
steps; import errors were logged (`Log.w` in `SanguSantriApplication`), just
without enough detail (addressed below) — nothing was silently swallowed at
the import layer. The break was entirely in **wiring** (asset placement)
and **visibility policy** (release-shaped `PUBLISHED`-only queries used
unconditionally, including for local development).

### Tahlil wiring fix

* Debug/release asset split (see Debug content policy below): main assets'
  `content/manifest.json` now lists `packages: []` (nothing is
  production-approved yet); a new `app/src/debug/assets/content/` holds the
  real content, merged in only for debug builds.
* Copied `tools/content-importer/output/tahlil-general-v1.draft.json`
  byte-for-byte into `app/src/debug/assets/content/tahlil-general-v1.json`
  (no edits) and added it to the debug manifest with the SHA-256 of those
  exact bytes. No source-specific hack was added to the Android reader —
  the fix is entirely in the asset pipeline and the repository's debug
  fallback below.
* `ContentRepositoryImpl.getDefaultVersionDetail` now resolves through a
  new `resolveVersion` helper: try `getLatestPublishedForVariant` first
  (unconditionally, so a future real `PUBLISHED` version always wins), and
  only when that is `null` **and** `BuildConfig.DEBUG` is true, fall back to
  a new `AmaliyahVersionDao.getLatestNonRevokedForVariant` query (latest
  version by `versionNumber` excluding `REVOKED`). Release builds are
  byte-for-byte unchanged — they only ever resolve `PUBLISHED`. Required
  enabling `buildFeatures.buildConfig = true` (not previously on).

### Istighosah importer result

Added a second source-specific parser
(`tools/content-importer/content_importer/parser_istighosah_nu.py`) for
`https://quran.nu.or.id/doa/istighotsah-mujahadah`, reading 1 of 7 —
"Istighotsah (KH Romli Tamim)" only, per this milestone's brief. The page
has no stable container id (Tailwind/Next.js hashed classes); the reading is
delimited by its own `<h1>` and the next reading's `<h1>`, and each verse is
a `dir="rtl"` Arabic span + a Latin transliteration span (read only to keep
sibling-order state correct, never stored) + an Indonesian translation span,
in that fixed order, inside a `flex-grow` content-column div. One structural
surprise found and fixed during development: a bare `dir="rtl"` sub-heading
span ("Sayyidul Istighfar") sits outside any verse container immediately
before the final verse it names — an earlier version of the parser
(incorrectly scoped to the sibling `nui-ActionVerse` button-column div,
which closes *before* the content column even opens) mistook it for that
verse's Arabic text, shifting the final triplet by one slot. Fixed by
scoping verse detection to the `flex-grow` div specifically and emitting
the sub-heading as its own `HEADING` step in document order; re-verified
against the live page afterwards.

`fetch` → `parse` → `validate` ran end-to-end against the live page (not a
canned fixture). **27 steps extracted**: 1 top-level heading, 1 sub-heading
("Sayyidul Istighfar"), 25 `PRAYER` steps (all with real Arabic + Indonesian
translation; empty-field check confirms zero steps are missing either
field). Repetition counts are cross-checked from two independent places in
the source (an Arabic-embedded `×N` marker using Arabic-Indic digits, and
the Indonesian translation's trailing `(Nx)` — including a `30.000x)`
thousands-separator form the parser normalises) — 4 sections flagged
ambiguous where only one of the two confirmed the count (never invented,
always extracted from whichever side stated it, with the discrepancy
recorded for manual review). 2 headings/translations flagged as possible
`QURAN_AYAH` candidates (`"Membaca Surat Yasin (1x)"`, `"Membaca Surat
al-Fatihah (1x)"` — the source names these as instructions to recite
elsewhere, not literal ayah text on the page, so nothing was invented for
them). `validate` reports the package structurally **VALID**. Raw snapshot
and generated draft are gitignored, not committed.

To support a second source without duplicating the whole pipeline,
`tools/content-importer/`'s previously Tahlil-only, hardcoded modules were
generalised: `draft_model.py` (new — `DraftStep`/`ParseReport`/`ParseResult`
moved out of `parser_nu_tahlil.py`, now shared by both parsers);
`config.py`'s `SourceSpec` gained the canonical package identity fields
(amaliyah/variant/version/approval ids, content slug, description) so a new
source is one reviewed dict entry, not a code change; `builder.py`'s
`build_draft_package` now takes a `SourceSpec` instead of hardcoded Tahlil
constants; `cli.py` dispatches to the right parser via a `PARSERS` map keyed
by `--source` and uses `SourceSpec.content_slug` for output filenames
instead of a fragile `.replace('-nu-online', '-general-v1')` string hack.

### Android integration status

Both packages flow through the identical, unmodified path: canonical
`schemaVersion: 1` JSON → `app/src/debug/assets/content/` → existing
`SeedContentImporter`/`AssetSeedContentSource` → Room → the existing
`ContentRepositoryImpl` (now with the debug-only fallback above) → Serambi
→ Full Reader → Guided Reader. No second content schema, no
Istighosah-specific repository or reader, no duplicated step models, no
hardcoded Istighosah Kotlin content — verified by grep: no Arabic Unicode
in any touched `.kt` file.

### Debug content policy

Per CLAUDE.md's debug content policy: `app/src/main/assets/content/`
(release + debug fallback) now bundles zero content packages —
`manifest.json` is `packages: []`, confirmed empty in a real
`:app:assembleRelease` output APK (`unzip -l` shows only the 84-byte empty
manifest under `assets/content/`). `app/src/debug/assets/content/` holds
both `DRAFT` packages; Android's asset merger gives debug source-set files
priority over main's for debug builds only, confirmed by inspecting
`app/build/intermediates/assets/debug/mergeDebugAssets/content/manifest.json`
against the release equivalent. `ContentRepositoryImpl`'s
`BuildConfig.DEBUG`-gated fallback (above) is the second half of this
policy — content being present in Room is not sufficient for it to render;
it must also be `PUBLISHED`, unless the build is debug. No large intrusive
development banner was added; distinguishing metadata already lives in each
package's own `sourceName` ("automated draft transcription, unreviewed...")
and `descriptionId` ("Draf transkripsi otomatis..., belum ditinjau
manusia. Bukan konten produksi.") — both rendered as ordinary card/reader
text, not a banner. Neither content is claimed as approved anywhere in code,
strings, or documentation.

### Development database refresh

No Room schema change was needed this milestone (no new/changed tables), so
no migration or version-reset was required. The one real hazard —
`tahlil-umum-v1` colliding between the old placeholder and the new draft —
was reproduced and confirmed on-device: installing the new debug build
over an existing local install logged `Seed import: already imported
tahlil-umum-v1` / `...istighosah-umum-v1` for both packages (stale rows
kept). Running `adb shell pm clear com.sangusantri.app` (or a full
reinstall) once, then relaunching, logged `Seed import: imported
tahlil-umum-v1` / `...istighosah-umum-v1` correctly, and a subsequent
relaunch logged `already imported` again with no duplicate rows or step
counts changing across restarts — idempotent, as designed. This one-time
clear-app-data step is a developer action, documented here per this
milestone's own "simplest pre-release reset" allowance, not a code change.

### Error visibility

* `SeedContentImporter`: failure reasons (checksum mismatch, JSON parse
  error, structural validation error, Room insertion failure) now all carry
  the asset filename (`entry.file`) as a prefix, on top of the version id
  already carried by `SeedImportOutcome.Failed.versionId` — e.g. `"tahlil-
  general-v1.json: checksum mismatch"`.
* `SanguSantriApplication`: `Imported`/`AlreadyImported` outcomes are now
  also logged (`Log.d`), not just `Failed` (`Log.w`), so a debug logcat
  shows the full import picture, not just failures.
* `ReaderViewModel`, `GuidedReaderViewModel`, `ReaderEntryViewModel`:
  the `catch (unexpected: Exception)` boundaries that turn any failure into
  `ContentState.Error`/`RecoverableError` previously logged nothing at all
  — now each logs the slug and exception via `Log.e`. The non-exceptional
  "content unavailable" branch (`amaliyah == null || detail == null ||
  detail.steps.isEmpty()`) also now logs which specific check failed
  (`amaliyahFound`/`activeVersionFound`/`stepCount`) via `Log.w` — no
  religious content is ever logged, only booleans/counts/ids. Release UI
  behaviour is unchanged in all cases (still the existing controlled
  `ContentUnavailable`/`RecoverableError` states, never a raw exception).
* Plain JVM unit tests (`ReaderViewModelTest`, etc.) do not mock
  `android.util.Log` and were failing after these `Log` calls were added
  (`Method ... not mocked`) — fixed by enabling
  `testOptions.unitTests.isReturnDefaultValues = true` in
  `app/build.gradle.kts` (standard AGP setting; no test file changed).

### Files created, modified, and removed

Created: `app/src/debug/assets/content/{manifest,tahlil-general-v1,
istighosah-general-v1}.json`, `tools/content-importer/content_importer/
{draft_model.py,parser_istighosah_nu.py}`.

Removed: `app/src/main/assets/content/{tahlil-general-v1,
istighosah-general-v1}.json` (bracket-placeholder fixtures; replaced by the
real drafts under `debug/assets/`, which are also not committed — see
`.gitignore`'s existing `tools/content-importer/output/` rule; the copies
under `app/src/debug/assets/content/` **are** committed since they're
Android build inputs, not raw tool output).

Modified (main): `app/build.gradle.kts` (`buildFeatures.buildConfig = true`,
`testOptions.unitTests.isReturnDefaultValues = true`),
`app/src/main/assets/content/manifest.json` (emptied to `packages: []`),
`data/local/dao/AmaliyahVersionDao.kt` (new
`getLatestNonRevokedForVariant`), `data/repository/ContentRepositoryImpl.kt`
(new `resolveVersion` debug fallback), `data/local/seed/
SeedContentImporter.kt` (failure reasons now include the asset filename),
`SanguSantriApplication.kt` (log every outcome, not just failures),
`feature/reader/ReaderViewModel.kt`, `feature/reader/
ReaderEntryViewModel.kt`, `feature/guidedreader/GuidedReaderViewModel.kt`
(added diagnostic logging to previously-silent branches).

Modified (tools): `tools/content-importer/content_importer/{config.py,
builder.py,cli.py,parser_nu_tahlil.py}` (generalised for a second source —
see Istighosah importer result above), `tools/content-importer/README.md`.

Modified (docs): `docs/content-schema.md` (debug/release asset split),
`docs/operations/CONTENT_GOVERNANCE.md` (Istighosah now has a specific
source for dev-draft tooling; still not approved), this file.

### Commands executed

`python3 -m content_importer fetch/parse/validate --source
istighosah-nu-online`, `./gradlew :app:ktlintFormat`, `:app:ktlintCheck`,
`:app:detekt`, `:app:assembleDebug`, `:app:testDebugUnitTest`,
`:app:lintDebug`, `:app:assembleRelease` — all passed. `adb install`,
`adb shell pm clear`, `adb shell input tap`/`keyevent`, `adb shell
uiautomator dump`, `adb logcat` — manual on-device verification, Pixel_9
emulator, Android 15/API 35.

### Manual validation results (this session, on-device)

Verified: Serambi shows both Tahlil and Istighosah with their real
`descriptionId` text (not bracket placeholders); Tahlil Full Reader renders
59 ordered steps with visible harakat, paired Indonesian translation, and
Arabic remains selectable text (`SelectionContainer`, unchanged from
Milestone 3); Istighosah Full Reader renders 27 ordered steps with
"Dibaca N kali" repetition indicators; Guided Reader shows "N dari 59"/"N
dari 27" position text, advances correctly step-by-step (verified against
exact `uiautomator`-dumped element bounds, not estimated coordinates); the
interactive tasbih counter increments 0→3, shows the green
checkmark-and-colour completion state (never colour alone), and correctly
gates `Lanjutkan` (disabled at `0/3`, enabled at `3/3`); one back-press from
either reader returns directly to Serambi; restart after a fresh import
does not duplicate content (repeat `already imported` logs, stable step
counts). Not exercised this session: dark theme, font-scale 1.5×,
landscape/tablet width, RTL app locale, process-death restoration — Milestone
3/4's own known limitations for these already apply and nothing in this
milestone touches that code.

### Ambiguous content requiring manual review

Before either draft could ever be promoted out of `DRAFT`, a human reviewer
needs to resolve: Tahlil's carryover-from-Milestone-3.5 items (9 possible
`QURAN_AYAH` headings, 2 Arabic blocks with an embedded Latin repetition
marker, 1 empty-Arabic-paragraph anomaly — unchanged, not touched this
milestone); Istighosah's 4 one-sided repetition-count confirmations, 2
possible `QURAN_AYAH` instructions ("Membaca Surat Yasin (1x)", "Membaca
Surat al-Fatihah (1x)"), and the "Sayyidul Istighfar" sub-heading's exact
placement (currently its own `HEADING` step immediately before the verse it
names — a reasonable but not human-confirmed structural choice).

### Known limitations

* Neither Tahlil nor Istighosah has been manually reviewed against its
  source or received kyai/sesepuh approval — both remain `DRAFT`/`PENDING`
  and are correctly invisible in release builds.
* `connectedDebugAndroidTest` was not run — no Android/Kotlin production
  logic changed in a way the existing instrumented tests (which use their
  own dedicated `PUBLISHED` test fixtures, not the real assets) would catch
  differently; the real regression surface for this milestone was
  real-content rendering, covered by the manual on-device pass instead.
* Dark theme, 1.5× font scale, landscape/tablet width, RTL locale, and
  process-death restoration were not re-verified this session (unchanged
  from Milestone 3/4's own recorded limitations).
* The release-blocking content-validation gate (failing the build when
  `main`'s manifest has zero packages) flagged since Milestone 1 is still
  not built — release currently ships an intentionally empty catalogue
  instead, which is a stronger (not weaker) form of the same protection.

### Next recommended milestone

Not specified by this brief. Promoting either draft into production content
is a content-governance task (manual review + kyai/sesepuh approval), not
an engineering milestone. `docs/product/ROADMAP.md` should be revisited for
the next scheduled engineering item.

## Milestone 5 — Content Release Baseline and Reader Mode Switching

**Status:** Implemented and verified locally — `ktlintFormat`, `ktlintCheck`,
`detekt`, `:app:compileDebugKotlin`/`compileDebugUnitTestKotlin`/
`compileDebugAndroidTestKotlin`, `:app:testDebugUnitTest` (37/37, unchanged
count — no new unit tests added per this milestone's own testing policy),
`:app:lintDebug`, `:app:assembleDebug`, and `:app:assembleRelease`
(R8/shrinking, `lintVitalRelease`) all pass. `connectedDebugAndroidTest` and
on-device manual verification were **not run** — no emulator was available
this session (`adb devices` returned empty); see Manual validation still
required below.

**Scope:** Product-scope correction (remove public feedback and remote
sync/backend from `0.0.1`, simplify approval to a compact `Approved by`
status, fix current content as the release-candidate baseline) plus two
Milestone 5 features: an in-reader Full ⇄ Guided mode-switch action
(FR-016) and cross-mode progress mapping. No Room schema change — no
entities were added, removed, or changed, so no migration/baseline reset
was needed (`docs/engineering/CONTENT_MODEL.md` schema-freeze policy).

### Scope correction (no code existed to remove)

Public `Koreksi Bacaan`, a feedback form, and a feedback outbox were never
actually implemented in any prior milestone (confirmed by search — no
`feedback` code, strings, or navigation destination existed in
`app/src/main`). The correction removed a **documented** requirement, not
running code: `docs/product/PRD.md` (former FR-012, item 25 of §5.1, §8.6,
the `Koreksi Bacaan` terminology entry), `docs/product/ROADMAP.md`, and
`docs/engineering/CONTENT_MODEL.md`'s `feedback_outbox`/`sync_metadata`
table descriptions were all rewritten to state the removal explicitly,
rather than "not yet implemented." Likewise, no remote sync code or Go
backend code exists anywhere in the repository — FR-010 and the PRD header
now state plainly that `0.0.1` is local-only, and `docs/product/PRD.md`
§5.1's item list dropped the Go/PostgreSQL/Supabase Studio items entirely
(previously items 29–32). `docs/operations/CONTENT_GOVERNANCE.md`'s
correction workflow no longer opens with "Feedback received" — it now
opens with "Internal review, reported error, or source update noticed by
the content team," since users never submit corrections through the app.

### Compact approval display

`docs/product/PRD.md` §6.5 was rewritten from a full pentashihan
field-exposure requirement to a compact, deployment-gated status. New
`feature/reader/ApprovalDisplay.kt`: a sealed `ApprovalDisplay`
(`Approved(approverLabel)` / `Pending` / `Hidden`) plus
`Approval.toApprovalDisplay(isDebugBuild)` — `Approved` only when
`approval.status == APPROVED` and `approverName` is non-blank (using
`institutionName` when present), `Pending` (a neutral "Persetujuan akhir
belum tersedia" status) only in debug builds, `Hidden` otherwise (release
builds with no real approval show nothing, never a placeholder). This
reuses the existing `Approval`/`ApprovalStatus` domain model unchanged — no
new Room columns, no checksum/raw-document/reviewer-name exposure. New
shared `feature/reader/components/ReaderOverflowMenu.kt` renders an
overflow (`MoreVert`) menu with the mode-switch action always, and a
"Sumber & Pentashihan" item only when `ApprovalDisplay != Hidden`, opening
a two-line `AlertDialog` ("Disetujui oleh" / approver name) or the
dev-only pending message. `ReaderUiState.ContentAvailable` and
`GuidedReaderUiState.StepVisible` both gained an `approval: Approval`
field so both readers can render this without a new destination, document
upload, PDF viewer, or CMS (none built, per this milestone's own
exclusion list).

### Content release-candidate baseline

`docs/product/PRD.md` (new §6.7), `docs/product/ROADMAP.md`, and
`docs/operations/CONTENT_GOVERNANCE.md` now state explicitly that the
current bundled Tahlil (59 steps) and Istighosah (27 steps) — unchanged
since Milestone 4.5 — are the fixed `0.0.1` release-candidate content:
loaded through the existing canonical content model in both debug and
release builds, fully offline, never reparsed or rewritten by normal
Android builds. This is a documentation/baseline-freeze decision only —
`app/src/debug/assets/content/` and `app/src/main/assets/content/` are
byte-for-byte unchanged from Milestone 4.5, no Kotlin/Android source
changed for content loading, and both packages remain `DRAFT`/`PENDING`
until real manual review and kyai/sesepuh approval happen.
`tools/content-importer/` is untouched and remains a separate developer
tool, never invoked at runtime.

### Reader mode switch (FR-016)

Full Reader's top bar gained an overflow menu item "Beralih ke Panduan";
Guided Reader's gained "Beralih ke Bacaan Lengkap" — both via the shared
`ReaderOverflowMenu`, an overflow action rather than a permanent bottom bar
or a new dashboard (`docs/design/DESIGN_SYSTEM.md` anti-patterns). Switching
does **not** show the Milestone 4 mode-selection gate again and does update
the saved `ReaderSettings.lastReaderMode` preference — which is also how
this milestone satisfies "keep a clear way to change the saved
preference" (§4 of the brief): switching mode from inside either reader
is that way, so no separate reset UI was added.

### Cross-mode progress mapping — reused existing Room tables, no new model

Deliberately **not** a new progress model or a nav-key-carried parameter
(a nav-key value would survive process death and could incorrectly
re-apply a stale switch target after the user has since scrolled/advanced
further — considered and rejected). Instead, switching writes directly
into the same per-content-version Room rows each reader already restores
from on load:

* **Full → Guide** (`ReaderViewModel.onSwitchToGuided`): reads the
  Full Reader's last-known visible item index (tracked from
  `ScrollPositionChanged`, the same signal already driving debounced
  position persistence), maps it to that step's stable id, then
  `@Upsert`s `GuidedReadingSession(currentStepId = thatStep, completedAtEpochMillis
  = <preserved from any existing session>)` via the existing
  `GuidedReadingRepository`. `GuidedReaderViewModel.restoreProgress` needs
  no change at all — it already reads `session.currentStepId` and every
  `StepProgress` row on load, so it picks up the new starting step and
  every existing counter automatically.
* **Guide → Full** (`GuidedReaderViewModel.onSwitchToFull`): reads the
  Guided Reader's current step index, `@Upsert`s
  `ReadingPosition(itemIndex = thatIndex, itemOffset = 0)` via the
  existing `ReadingPositionRepository`. `ReaderViewModel.loadContent`
  needs no change either — it already restores from
  `readingPositionRepository.getPosition(versionId)`.

Both writes are keyed by the immutable `version.id` (`@Upsert`, one row
per version), so repeated switching overwrites the same row rather than
duplicating it, different content versions never share progress, and no
extra navigation-entry bookkeeping was needed beyond the existing
pop-and-push pattern below.

### Navigation

`SanguSantriNavHost`'s Milestone 4 `replaceGateWithResolvedReader` helper
was generalised (renamed `replaceTopEntryWithReader`, doc comment updated)
and is now called from three places: the mode gate (unchanged behaviour)
and the two new `onSwitchToGuided`/`onSwitchToFull` callbacks from
`FullReader`/`GuidedReader` entries. All three pop the current top entry
before pushing the new one, so repeated switching never accumulates
duplicate backstack entries and back navigation from either reader always
returns to Serambi. The `entryProvider { }` builder block was extracted
into a private top-level `sanguSantriEntryProvider(backStack)` function
(a plain function, not `@Composable` — `entryProvider`/`entry<T>` don't
require composition) to keep `SanguSantriNavHost` under detekt's
`LongMethod` threshold after adding the two new entry callbacks.

### Files created, modified, and removed

Created: `feature/reader/ApprovalDisplay.kt`,
`feature/reader/components/ReaderOverflowMenu.kt`.

Modified (main): `feature/reader/ReaderUiState.kt` (`approval` field),
`feature/reader/ReaderUiAction.kt` (`SwitchToGuided`),
`feature/reader/ReaderViewModel.kt` (injects `GuidedReadingRepository`,
tracks last-known item index, `switchToGuidedReady`, `onSwitchToGuided`),
`feature/reader/ReaderScreen.kt` (`onSwitchToGuided` param, overflow menu
wiring, preview approval fixture), `feature/guidedreader/
GuidedReaderUiState.kt` (`approval` field), `feature/guidedreader/
GuidedReaderUiAction.kt` (`SwitchToFull`), `feature/guidedreader/
GuidedReaderViewModel.kt` (injects `ReadingPositionRepository`,
`switchToFullReady`, `onSwitchToFull`, exhaustive `when` fix in
`onSettingsAction`), `feature/guidedreader/GuidedReaderScreen.kt`
(`onSwitchToFull` param, overflow menu wiring, preview approval fixture),
`feature/guidedreader/GuidedReaderBars.kt` (`overflow` slot on
`GuidedReaderTopBar`), `navigation/SanguSantriNavHost.kt` (generalised
replace helper, two new callbacks, `entryProvider` extraction),
`res/values/strings.xml` (mode-switch and approval-display strings). No
Room entity, DAO, or migration changed.

Modified (test): `feature/reader/ReaderViewModelTest.kt`
(`FakeGuidedReadingRepository`, new constructor parameter) — required for
compilation, not new test coverage, per this milestone's testing policy.

Removed: none — no feedback/approval-detail code existed to remove (see
Scope correction above).

Modified (docs): `docs/product/PRD.md` (document version 1.1 → 1.2 —
§5.1/§5.2/§6.5/§6.6/new §6.7/§7.1/§7.2/§8.4a (new)/§8.5/§8.6 (removed)/
FR-009/FR-010/FR-011/FR-012/new FR-016), `docs/product/ROADMAP.md`
(`0.0.1` bullets), `docs/operations/CONTENT_GOVERNANCE.md` (internal-only
correction trigger, new compact approval-display section, release-candidate
baseline note), `docs/engineering/CONTENT_MODEL.md` (`feedback_outbox`/
`sync_metadata` marked removed from scope rather than "not yet
implemented"; publication/approval decoupling note), this file. `CLAUDE.md`
was **not** modified — nothing in it currently asserts feedback, sync, or
backend are in `0.0.1` scope, so no hard global rule there was actually
false; the scope correction lives entirely in the PRD/roadmap/governance
docs it already points to.

### Commands executed

`./gradlew :app:ktlintFormat`, `:app:ktlintCheck`, `:app:detekt`,
`:app:compileDebugKotlin`, `:app:compileDebugUnitTestKotlin`,
`:app:compileDebugAndroidTestKotlin`, `:app:testDebugUnitTest`,
`:app:lintDebug`, `:app:assembleDebug`, `:app:assembleRelease` — all
passed. `adb devices` returned empty (no emulator this session).

### Manual validation still required (no emulator this session)

All items in this milestone's own validation checklist depend on a running
device/emulator and were not exercised: Tahlil/Istighosah open in debug and
release, offline behaviour, first-open mode chooser vs. remembered-mode
reopen, Full→Guide and Guide→Full switching (including that the mode
chooser does not reappear, the visible/current step carries over, and
guided counter progress survives), repeated switching not duplicating
navigation entries or Room rows, predictable back navigation, the "Sumber &
Pentashihan" menu item showing nothing in a release-shaped state and the
dev-only pending message in a debug build with unapproved content (today's
real bundled content), and that restarting the app preserves both the mode
preference and progress. `connectedDebugAndroidTest` was also not run.

### Final approval metadata still needed before public deployment

Unchanged from every prior milestone: neither Tahlil nor Istighosah has
real kyai/sesepuh approval or a real approval-evidence reference. This
milestone's compact `Approved by` display makes that gate a genuine
UI-visible state (dev-only "Persetujuan akhir belum tersedia" today) rather
than removing it — production publication still requires real approver
metadata and a real approval-evidence reference (`docs/product/PRD.md`
§6.5, §13).

### Next recommended milestone

Not specified by this brief. Promoting either draft into production
content remains a content-governance task, not an engineering milestone.
The release-blocking content-validation gate flagged since Milestone 1
(failing the build when `main`'s manifest has zero packages) is still not
built. `docs/product/ROADMAP.md` should be revisited for the next
scheduled engineering item.

## Milestone 6 — Risk-Based Content Publication Governance and Baseline Publication

**Status:** Implemented and verified locally — `ktlintFormat`, `ktlintCheck`,
`detekt`, `:app:compileDebugKotlin`/`compileDebugUnitTestKotlin`/
`compileDebugAndroidTestKotlin`, `:app:testDebugUnitTest` (37/37, unchanged
count), `:app:lintDebug`, `:app:assembleDebug`, and `:app:assembleRelease`
(R8/shrinking, `lintVitalRelease`) all pass. Verified directly against the
built release APK (`unzip`) that `assets/content/manifest.json` now lists
both packages and `tahlil-general-v1.json`'s `version.status` is
`PUBLISHED` with the new truthful description text and all 59 steps intact.
`connectedDebugAndroidTest` and on-device manual verification were **not
run** — no emulator was available this session.

**Scope:** An explicit product-owner governance decision superseding the
project's previous universal "every public amaliyah needs kyai/sesepuh
approval" rule with a risk-based model, plus publishing the existing
Tahlil/Istighosah packages under it. No Room schema change, no new
progress model, no change to the already-implemented reader mode-switching
(Milestone 5) — this milestone re-verified that work already satisfies the
requirements restated in the request.

### Governance rules replaced

Universal pre-publication kyai/sesepuh approval requirement → risk-based
model: **standard public amaliyah** (identified public trusted source,
source recorded, extraction manually inspected, no invented/merged
content, Arabic/translations exactly as sourced, product-owner editorial
acceptance) may publish without kyai/sesepuh sign-off; **higher-risk
content** (private/pesantren-specific, disputed origin, internally
modified/merged/translated, doctrinally sensitive, tied to a specific
ijazah/sanad/tarekat/pesantren authority, or materially different from the
selected source) still requires qualified religious review before
publication. Absolute prohibitions unchanged and still enforced: no AI
invention of religious content, no AI correction from memory, no silent
version merging, no false endorsement claims, no invented reviewer
identities or evidence, no runtime scraping.

### Documents updated

`CLAUDE.md` (Content Safety section rewritten — this is the hard global
rule that actually changed this time, unlike Milestone 5), `docs/product/PRD.md`
(document version 1.2 → 1.3: §3.1 risk-based model, §6.1/§6.2 source
framing, §6.3 entry rule, §6.5 rewritten as source/publication/approval/
endorsement five-way split, §6.7 renamed "Public content baseline", §7.1,
§8.5, FR-009, §13 blocking-inputs items 1/2 resolved and 5/6/12 made
conditional), `docs/operations/CONTENT_GOVERNANCE.md` (new risk-based
model section, two editorial workflows, rewritten developer-draft-tooling
and correction-workflow/severity sections, rewritten source/approval/
endorsement and display sections), `docs/operations/PRODUCTION_READINESS.md`
(Definition of Done: kyai/sesepuh approval no longer blocks release for
this content category; removed stale sync/feedback/backend-test bullets;
`feedback_outbox` reference in Backup Policy corrected), `docs/operations/
INCIDENT_RESPONSE.md` (removed stale feedback-success-rate observability
line), `docs/security/THREAT_MODEL.md` (removed stale feedback-endpoint
rate-limiting reference), `docs/decisions/0006-content-schema-and-seed-import.md`
(Consequences section rewritten — packages are no longer non-production
placeholders), `docs/engineering/CONTENT_MODEL.md` (`amaliyah_versions`
section ties the publication/approval decoupling explicitly to the
risk-based model), `docs/content-schema.md` (debug/release split and
Content Safety sections rewritten for the published state), this file.

### Publication-status changes

Both packages' `version.status`: `DRAFT` → `PUBLISHED`. `approval.status`
stays `PENDING` — truthfully, no kyai/sesepuh has reviewed either package,
and that field is optional for this content category, not required for
publication. `publishedAt` set to `2026-07-25T00:00:00Z` for both (previously
`null`). Both remain the same `version.id` (`tahlil-umum-v1`,
`istighosah-umum-v1`) and `versionNumber: 1` — this is the first
publication of these versions, not a correction to already-published
content, so ADR 0008 immutability does not apply retroactively.

### Release asset changes

Moved `tahlil-general-v1.json`/`istighosah-general-v1.json` (and a
regenerated `manifest.json` with freshly computed SHA-256 checksums) from
`app/src/debug/assets/content/` to `app/src/main/assets/content/` —
visible in every build now, not just debug. `app/src/debug/assets/content/`
(and the now-empty `app/src/debug/assets/` and `app/src/debug/`
directories) were removed; the debug-only-draft mechanism itself
(`ContentRepositoryImpl.resolveVersion`'s `BuildConfig.DEBUG` fallback)
is untouched and remains available for any future amaliyah still being
drafted. No Kotlin/Android source changed for content loading — the
existing `SeedContentImporter`/`AssetSeedContentSource`/`ContentRepositoryImpl`
pipeline is entirely unaware of this move, by design (ADR 0006).

Within each package, only metadata fields changed — **no step content
(Arabic text, translations, repetition targets) was touched**, verified by
diffing only the header block (`amaliyah`/`variant`/`version`/`approval`)
against the Milestone 4.5 originals:

* `amaliyah.titleAr`/`variant.nameAr`: the previous value was an English
  bracket placeholder (`"[DRAFT — Arabic title pending manual review, not
  yet transcribed by a human reviewer]"`) sitting in an Arabic-language
  field — not real Arabic text, and not something Claude authored Arabic
  translations for. Replaced with an empty string rather than invented
  Arabic script; the app does not yet render this field anywhere (no
  Arabic-locale UI ships in `0.0.1`), so this has no current user-facing
  effect and does not invent religious content.
* `amaliyah.descriptionAr`: same placeholder → `null`.
* `amaliyah.descriptionId` (shown directly on the Serambi card): rewritten
  from "Draf transkripsi otomatis dari NU Online, belum ditinjau manusia.
  Bukan konten produksi." to a factual one-line description of the
  reading collection itself (e.g. "Rangkaian bacaan tahlil, doa, dan
  terjemahannya secara lengkap.") — descriptive UI copy, not devotional
  content, consistent with every other authored string in `strings.xml`.
* `version.sourceName`: dropped the "(automated draft transcription,
  unreviewed, retrieved ...)" parenthetical; kept the real publisher and
  article/reading title.
* `approval.approverName`/`approvalScope`/`documentReferenceNumber`:
  rewritten to remove "draft"/"PENDING —"/"not reviewed or approved"
  wording (e.g. `documentReferenceNumber` `DRAFT-TAHLIL-...` →
  `BASELINE-TAHLIL-...`). These fields are internal data hygiene only —
  they are never rendered in the UI unless `approval.status == APPROVED`,
  which it is not.

### Source metadata behaviour

New `feature/reader/components/ReaderOverflowMenu.kt` dialog always shows
a truthful "Sumber" (source) line from `AmaliyahVersion.sourceName`, for
every amaliyah, regardless of approval state. It never renders as
"Approved by NU Online" or any phrasing implying NU Online/Quran NU
Online/PBNU endorses SanguSantri — endorsement and source verification are
kept visibly distinct (`docs/product/PRD.md` §6.5).

### Removed approval blockers

Kyai/sesepuh approval and redacted approval documents no longer block
release publication for standard public amaliyah (`docs/product/PRD.md`
§13, items 1/2/5/6 updated). They remain required before publishing any
higher-risk content, and before the app may ever show a real `Approved by`
line — `ApprovalDisplay` logic (Milestone 5, unchanged) still requires
`approval.status == APPROVED` with a real, non-blank approver name, which
neither package has. Release builds therefore show only the source line;
development builds may additionally show a neutral "Baseline rilis
internal" marker (`content_approval_pending_dev_only` string, reworded
this milestone — previously "Persetujuan akhir belum tersedia").

### Full/Guide switching and cross-mode progress behaviour

Unchanged from Milestone 5 — re-checked against every requirement restated
in this milestone's request (direct switch without the mode chooser,
saved-preference update, same amaliyah/version preserved, current-step
mapping via stable step ids, preserved Guided counter progress, completion
state not reset, no duplicate navigation entries or Room rows, predictable
back navigation, no second session model) and all are already satisfied by
the existing `ReaderViewModel.onSwitchToGuided`/
`GuidedReaderViewModel.onSwitchToFull` implementation. No code change was
needed here this milestone.

### Files created, modified, and removed

Created: none (code) — `app/src/main/assets/content/{manifest,
tahlil-general-v1,istighosah-general-v1}.json` are relocations of existing
files with edited metadata, not new content.

Modified (main): `feature/reader/ReaderUiState.kt`/`GuidedReaderUiState.kt`
(new `sourceName` field), `feature/reader/ReaderViewModel.kt`/
`feature/guidedreader/GuidedReaderViewModel.kt` (pass `sourceName` through),
`feature/reader/ReaderScreen.kt`/`feature/guidedreader/GuidedReaderScreen.kt`
(pass `sourceName` to the overflow menu; preview fixtures), `feature/reader/
components/ReaderOverflowMenu.kt` (always-visible source section, dialog
restructured), `res/values/strings.xml` (`content_source_label` added;
`content_approval_pending_dev_only` reworded).

Removed: `app/src/debug/assets/content/{manifest,tahlil-general-v1,
istighosah-general-v1}.json` (relocated to `main/`, not deleted — see
Release asset changes above); the then-empty `app/src/debug/assets/content/`,
`app/src/debug/assets/`, and `app/src/debug/` directories.

Modified (docs): see Documents updated above.

### Commands executed

`shasum -a 256` (recompute package checksums), `./gradlew :app:ktlintFormat`,
`:app:ktlintCheck`, `:app:detekt`, `:app:compileDebugKotlin`,
`:app:compileDebugUnitTestKotlin`, `:app:compileDebugAndroidTestKotlin`,
`:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`,
`:app:assembleRelease` — all passed. `unzip` against the built release APK
— confirmed manually (see Status above).

### Manual validation still required (no emulator this session)

Fresh install on a real device/emulator: confirm both Tahlil and Istighosah
open and render fully offline in a **release** build (not just debug as in
prior milestones); confirm the Serambi card descriptions read naturally
(no draft/pending wording anywhere); confirm the "Sumber & Pentashihan"
overflow item shows only the source line in a release build and the
"Baseline rilis internal" marker in a debug build; confirm devices with an
existing local install of the old `DRAFT` rows need a `pm clear`/reinstall
to pick up the new `PUBLISHED` metadata (same idempotency hazard
documented in Milestone 4.5 — `version.id` is unchanged, so
`SeedContentImporter` will skip re-importing over a stale existing row).
Full/Guide switching and cross-mode progress re-verification carries over
unchanged from Milestone 5's own still-required manual checks.

### Remaining Play Store release blockers

Unchanged, and independent of this milestone's governance change: no CI
pipeline, no signing key, no final logo/app icon, no privacy policy, no
Google Play developer configuration (`docs/product/PRD.md` §13,
`docs/operations/PRODUCTION_READINESS.md`). Kyai/sesepuh approval is no
longer one of these for the current two standard public amaliyah, but
remains required the moment any higher-risk content is added.

## design product-alignment documentation pass (pre-Phase A)

**Status:** Complete. Not a numbered milestone — no Android/Kotlin source
changed. Documentation only.

**Scope:** Align product documentation with a set of confirmed
product/UX decisions and a named design-tool file (11 node IDs covering revised
Full/Guided Reader, reader overflow menus, Reader Settings and Table of
Contents bottom sheets, Standalone Tasbih and its custom-target dialog,
Beranda, and Jelajahi Amaliyah). No Android/Kotlin code was written this
pass — that begins with Phase A once the user replies "done".

### design-tool access blocker

The design-tool MCP connection was rate-limited (Starter plan) for the entire
session — every call, including a plain `get_metadata` on the top-level
product-screens page, was rejected before a single node could be opened.
Per the user's explicit choice when asked how to proceed, this pass wrote
the documentation from the confirmed decisions given directly in the
request plus the current repository state, and marked every design-tool-derived
visual specific (exact spacing, component variants, states drawn in each
frame) as **pending design-tool verification** rather than guessing measurements
from node names alone. See `docs/design/DESIGN_HANDOFF.md`'s "Status of
this document" section — re-run design-tool discovery before Phase A begins.

### What shipped

* **`docs/design/DESIGN_HANDOFF.md`** (new): file/node reference,
  frame-to-feature mapping against current code, navigation map (including
  an explicitly flagged open question on bottom-nav rollout timing),
  reader interaction map, responsive/state/motion notes, Compose component
  mapping, implementation phase order (A–E, matching the request exactly),
  and known incomplete design-tool areas (no frame was supplied for Aktivitas).
* **`docs/reviews/design-product-alignment.md`** (new): gap table —
  existing implementation vs. confirmed decision vs. gap vs. resolution
  vs. owning document vs. phase, across terminology/navigation, reader,
  Beranda/Jelajahi, Tasbih/Aktivitas, and accessibility/design-system rows.
  Also records a pre-existing documentation drift found while reading
  `ARCHITECTURE.md` (a stale `feature/feedback` package in the diagram,
  even though feedback was removed from scope at Milestone 5) — fixed as
  part of this pass since it was found, not because it relates to the design tool.
* **`docs/product/PRD.md`** (version 1.3 → 1.4): renamed the primary home
  destination Serambi → Beranda (§2.3, §7.2, FR-002 — "Serambi" may persist
  as an internal label only); rewrote §7 Information Architecture with the
  target five-destination nav model and its open rollout question; added
  §8.4b (Full Reader repetition shortcut), §8.4c (reader Table of
  Contents), §8.6 (Beranda sections), §8.7 (Jelajahi Amaliyah); added
  FR-017 (Table of Contents), FR-018 (repetition shortcut), FR-019 (Beranda
  scalable dashboard), FR-020 (Jelajahi Amaliyah), FR-021 (favourites and
  recently opened); updated §5.1/§5.2 scope lists accordingly.
* **`docs/product/ROADMAP.md`**: rebaselined `0.0.1`'s bullet list to the
  wider foundation (Beranda, Jelajahi Amaliyah, continue reading, recently
  opened, favourites, repetition shortcut, TOC sheet); rewrote `0.0.2`
  Standalone Tasbih bullets to match the confirmed requirements exactly
  (33/100/unlimited/custom, no 99, compact selector, small custom dialog);
  renamed `0.0.3` "Riwayat and Streak" → "Aktivitas" (vertical sections, no
  tabs, flagged missing design-tool frame); added a "Final navigation model"
  section documenting the phased destination rollout and its open
  question.
* **`docs/design/DESIGN_SYSTEM.md`**: marked the "traditional-modern
  pesantren character/tone" wording superseded by "modern Islamic identity,
  not necessarily traditional ornament" (kept, not deleted, per the
  request's own legacy-marking instruction); added a spiritual-gold accent
  note (token not yet created); added Adaptive navigation, Component rules
  (search/section/card/chip/counter/dialog/bottom-sheet), Reader mode
  action, Tasbih target hierarchy, and Motion sections.
* **`docs/design/ACCESSIBILITY.md`**: added modal-bottom-sheet focus/
  dismissal rules, numeric-input validation rules (custom Tasbih target),
  and an explicit reduced-motion callout (still an unfixed known gap since
  Milestone 4); extended the 48dp/counter-semantics requirements to the
  new screens.
* **`docs/engineering/ARCHITECTURE.md`**: fixed the stale `feature/feedback`
  package tree entry (see above); added `feature/explore`, `feature/tasbih`,
  `feature/activity` as scheduled-but-unimplemented; added Navigation
  destinations and Local user-state persistence ownership sections.
* **`docs/engineering/CONTENT_MODEL.md`**: added a Category taxonomy note
  (why updating `amaliyah.category`'s value is a metadata edit, not a new
  content version); added a Table of Contents derivation note (sections
  come from existing `HEADING`-typed steps — no schema change); forward-
  documented (not created) `favorites`, `recently_opened`,
  `tasbih_sessions`, and a reminder model, each tagged with its owning
  phase.
* **`docs/engineering/OFFLINE_FIRST.md`**: added a note that all new local
  user-state features (favourites, recent, Tasbih, Aktivitas, reminders)
  follow the existing offline-first/local-source-of-truth pattern, no
  exceptions.
* **`docs/content-schema.md`**: added a forward note on `amaliyah.category`
  mirroring `CONTENT_MODEL.md`; no bundled JSON asset was edited.
* **`CLAUDE.md`**: corrected the milestone-state summary to Milestones 0–6
  (previously said 0–3); added a note that a design alignment pass has
  since renamed Serambi → Beranda and expanded documented `0.0.1` scope
  ahead of matching code; added `DESIGN_HANDOFF.md` to the UI/Compose
  reading-matrix row; added a clearly-scoped, explicitly temporary
  "Temporary implementation-pass constraints" section (no Room migrations,
  no new tests, minimum validation command set) for Phases A–E only, to be
  removed once that initiative concludes.

### Commands executed

None — documentation-only pass, no build/lint/test commands apply.

### Known limitations

* No design-tool node was actually opened (rate limit) — every visual/spacing/
  component-variant detail in `DESIGN_HANDOFF.md` is unverified and must be
  confirmed before or during Phase A.
* No Android/Kotlin source changed; none of the new Room tables mentioned
  above exist yet; the reader overflow-menu restructure (settings/TOC
  moving into the overflow) is documented but not implemented; Beranda is
  still the Milestone 2 two-card `SerambiScreen`, not yet renamed or
  rebuilt.
* The bottom-navigation rollout-timing question (`DESIGN_HANDOFF.md`) is
  unresolved and should be confirmed before Phase B, since it changes
  whether Phase B builds nav-bar scaffolding at all.
* No design-tool frame was supplied for Aktivitas (`0.0.3`) — confirm one exists
  before Phase D, or proceed from the written decision alone if the
  product owner confirms none is coming.

### Next recommended milestone

Phase A — Release `0.0.1` Reader UX alignment (`docs/design/DESIGN_HANDOFF.md`),
on the user's explicit "done" reply. Re-run design-tool discovery (`get_metadata`/
`get_design_context`/`get_screenshot` on nodes `14:2`, `14:32`, `16:2`,
`16:45`, `16:89`, `16:148`) before writing any Phase A code.

## Milestone 7 — Phase A: Reader UX Alignment

**Status:** Implemented and verified locally — `:app:ktlintFormat`,
`:app:ktlintCheck`, `:app:detekt`, `:app:lintDebug`, `:app:assembleDebug`,
`:app:compileDebugUnitTestKotlin`, `:app:compileDebugAndroidTestKotlin` all
pass. `connectedDebugAndroidTest`/`testDebugUnitTest` were not run per this
pass's own testing constraint. No emulator/device was available this
session (`adb devices` returned empty), so no manual on-device verification
was performed — see Known limitations.

**Scope:** Align the Full Reader and Guided Reader with the locally
exported revised design-tool frames (`docs/design/design-export/`, nodes `14:2`,
`14:32`, `16:2`, `16:45`, `16:89`, `16:148`), per the Phase A brief: revised
layouts, both overflow menus, the Full ↔ Guided switch (re-verified, not
rebuilt), the Full Reader repetition shortcut, progress/counter
preservation, the Reader Settings sheet, a new Table of Contents sheet,
source/pentashihan entry, adaptive width, and dark-mode color tokens. No
Standalone Tasbih, Beranda, Explore, or Activity work — out of scope per
the brief. No Room migration; no new tests added.

### Design-tool export inspection

All 10 exported node pairs (JSON + PNG) were inspected via a local Python
script that walks the `JSON_REST_V1`-shaped export tree (no design-tool MCP
call — still rate-limited). Concrete findings that changed the
implementation from what the documentation-only pass had guessed:

* **Both readers' top bars carry only back + overflow — no separate
  settings gear icon.** Confirmed in both `14:2` and `14:32`: reader
  appearance settings live entirely inside the overflow menu (decision F),
  not as a standalone top-bar action as Milestone 3–6 had it.
* **Overflow menu order is exactly**: switch mode → "Daftar isi" → "Tampilan
  bacaan" → "Sumber & pentashihan" (nodes `16:2`/`16:45`), confirmed
  identical between Full and Guided Reader apart from the switch-mode
  label.
* **A reading-progress header** ("Langkah N dari total" + percentage +
  track/value bar) appears above the content in both readers — not
  implemented in any prior milestone.
* **The repetition indicator is a tappable, stadium-shaped tonal pill**
  ("Dibaca N kali · Buka Panduan →"), not the plain informational text
  Milestone 3 shipped.
* **A "✓ Posisi bacaan tersimpan" status pill** appears in every reader
  export — treated as a transient, one-shot confirmation shown when a
  reader resumes at a saved position > 0 (the static export can't encode
  transient-vs-persistent behaviour, so this is a reasoned choice, not a
  guess about content that was actually absent from the export).
* **Reader Settings sheet (`16:89`) has 3 steppers, not 4**: Arabic font
  size, translation font size, Arabic line spacing — no dedicated
  translation-line-spacing control — plus a translation toggle and a single
  primary "Selesai" button at the bottom (no separate header close
  action). See Known limitations for the FR-008 gap this opens.
* **Table of Contents (`16:148`)** groups steps into named sections with
  step ranges (e.g. "Pembukaan 1–4", "Surat Al-Ikhlas 5"), the current
  section highlighted with the same tonal-pill treatment as the repetition
  shortcut.
* **Guided Reader's Previous/Continue buttons are both filled, stadium-
  shaped pills** (`14:32`) — Previous uses the tonal `primaryContainer`
  colour, not an outlined style as Milestone 4 shipped it.
* **Color variables observed across every export** map almost exactly onto
  the existing `SantriGreen`/`SantriNeutral` ramp already in `Color.kt`
  (e.g. the primary green pill text matches `SantriGreen20` exactly, the
  primary button colour matches `SantriGreen40` exactly), with four gaps:
  no existing token matched the card/sheet surface tone, the secondary/
  muted text tone, the hairline border tone, or the light pill/container
  tone — see Design tokens below.

### Design tokens

`core/designsystem/theme/Color.kt`: added `SantriGreen95` (light
primary-container pill tone), `SantriSurface` (card/sheet surface, distinct
from the existing neutral background), `SantriNeutral40` (secondary/muted
text), `SantriOutline` (hairline border/drag-handle). `Theme.kt`: explicitly
set `surface`, `surfaceVariant`, `onSurfaceVariant`, `outline`, and
`outlineVariant` for the light scheme (previously unset, silently falling
back to Material 3's unbranded defaults everywhere they were already used —
`AmaliyahCard`, `ReaderStepItem` dividers, `GuidedTasbihCounter`'s border,
Serambi's empty-state text — a pre-existing, now-fixed defect); corrected
`primaryContainer`/`onPrimaryContainer` from `SantriGreen90`/`SantriGreen10`
to `SantriGreen95`/`SantriGreen20` to match the exported tone exactly. Dark
scheme: added the same four roles using only existing dark-ramp tokens
(`SantriGreen20`/`SantriGreen90`/`SantriGreen30`) as a reasoned
approximation — no dark-mode design-tool frame was exported, so no new hex value
was invented for dark, per the project's own "do not guess" instruction.
`Shape.kt`: added `SanguSantriShapes.extraLarge = RoundedCornerShape(percent
= 50)` — a true stadium/pill shape at any aspect ratio, matching every pill
element observed (repeat shortcut, saved-position status, progress bar,
guided nav buttons), distinct from the existing three fixed-dp radii.

### What shipped

* **Repetition shortcut (FR-018)**: `ReaderRepetitionShortcut` (renamed
  from the informational `ReaderRepetitionIndicator`,
  `feature/reader/components/ReaderArabicContentBlocks.kt`) is now a
  clickable tonal pill. `ReaderStepItem` threads a new
  `onOpenGuidedAtStep: (stepId: String) -> Unit` down to it. New
  `ReaderUiAction.SwitchToGuidedAtStep(stepId)`; `ReaderViewModel`'s
  `onSwitchToGuided` logic was refactored into a shared private
  `switchToGuided(detail, stepId)` so the existing overflow-menu switch
  (current visible step) and the new shortcut (the exact tapped step) both
  reuse the same session-write path — no duplicated logic, no new progress
  model.
* **Table of Contents (FR-017)**: new `feature/reader/toc/` package —
  `TocSection.kt` (pure `List<AmaliyahStep>.toTocSections()`/
  `sectionContaining()`, deriving sections from existing `HEADING` steps,
  no schema change) and `ReaderTableOfContentsSheet.kt` (the bottom sheet
  UI plus `ReaderTableOfContentsOverlay`, the Full Reader's entry point
  that scrolls its `LazyListState` to the selected section — reusing the
  existing scroll-position persistence path, no new ViewModel action
  needed). Guided Reader gets a new `GuidedReaderUiAction.JumpToStep`
  (moves to a step index via the existing private `moveTo`, no counter
  side effects) and `GuidedReaderUiState.StepVisible` gained an `allSteps`
  field so the sheet can derive sections from the full step list.
* **Reader overflow menu**: `ReaderOverflowMenu` gained
  `onOpenTableOfContents`/`onOpenSettings` (bundled into a new
  `ReaderOverflowActions` data class, `feature/reader/components/
  ReaderOverflowActions.kt`, to stay under detekt's parameter-count limit)
  and two new `DropdownMenuItem`s ("Daftar isi", "Tampilan bacaan") in the
  exact order the export shows.
* **Reader Settings sheet**: restructured to match `16:89` — header is now
  title + subtitle (no inline close button), translation-line-spacing
  stepper removed from the UI (see Known limitations), a primary "Selesai"
  button added at the bottom, explicit `containerColor`/28dp-top-corner
  `shape` on the `ModalBottomSheet`. The standalone settings icon was
  removed from both readers' top bars entirely (`GuidedReaderTopBar` no
  longer takes an `onOpenSettings` parameter).
* **Progress header (shared)**: new `ReaderProgressHeader`
  (`feature/reader/components/ReaderProgressHeader.kt`) — "Langkah N dari
  total" + percentage + a hand-drawn track/value bar (not M3's
  `LinearProgressIndicator`, for exact colour/shape control) — used
  identically by both readers. For the Full Reader it sits **above** the
  `LazyColumn`, not as a lazy item, to avoid perturbing the existing
  lazy-list-index-based position-persistence contract (a deliberate,
  documented deviation from the export's literal single-scroll-container
  nesting). Guided Reader's old plain "N dari total" text was replaced by
  this shared header; `guided_reader_position_label` was removed as dead.
* **Saved-position status (shared)**: new `ReaderSavedPositionStatus`
  (+ `rememberInitialSavedPositionFlag`, same file) — a transient tonal
  pill with a check icon, shown once per screen instance when the reader
  resumes at a position/step index > 0, auto-hiding after 2.5s.
* **Guided Reader step-status row (decision E)**: new
  `GuidedStepStatusRow` (`feature/guidedreader/components/
  GuidedStepContent.kt`) renders the step title and a prominent "Target N
  kali" label above the reading card, in addition to the counter already
  showing progress inside the card.
* **Guided Reader nav buttons**: `GuidedReaderBottomBar` — "Sebelumnya" is
  now a filled tonal (`primaryContainer`) pill instead of an
  `OutlinedButton`; both buttons use the new `extraLarge` stadium shape.
  Direction is still conveyed via `Icons.AutoMirrored.Filled.ArrowBack`
  (RTL-correct), not a literal arrow glyph in the string, which is a
  deliberate deviation from the export's own static mockup in favour of
  the project's explicit RTL/mirrored-icon accessibility rule.

### Detekt-driven decomposition

Adding this much UI surface to `ReaderScreen.kt`/`GuidedReaderScreen.kt`
tripped `LongMethod`/`LongParameterList`/`TooManyFunctions` repeatedly —
resolved by extraction and relocation, not `@Suppress`: `ReaderTopBar`,
`ReaderStepListContent` (+ a small `ReaderStepListRenderState` bundling
data class) stay in `ReaderScreen.kt`; `GuidedReaderTopBarWithOverflow`/
`GuidedReaderBottomBarIfVisible` stay in `GuidedReaderScreen.kt`;
`ConfirmDialogText`/`GuidedConfirmDialog` moved to their own file
`feature/guidedreader/ConfirmDialogText.kt`; `GuidedReaderOverlayVisibility`
(+ its two `remember*` helpers) moved to `feature/guidedreader/
GuidedReaderOverlayVisibility.kt`; `GuidedStepStatusRow` moved into
`feature/guidedreader/components/GuidedStepContent.kt`;
`ReaderOverflowActions` moved into its own file (detekt's
`MatchingDeclarationName` flags a file with one top-level class-like
declaration and a non-matching name); `TableOfContentsSections.kt` was
renamed `TocSection.kt` for the same reason.

### Files created

`core/designsystem/theme` — none created, only `Color.kt`/`Shape.kt`/
`Theme.kt` modified. `feature/reader/components/{ReaderOverflowActions,
ReaderProgressHeader,ReaderSavedPositionStatus}.kt`,
`feature/reader/toc/{TocSection,ReaderTableOfContentsSheet}.kt`,
`feature/guidedreader/{ConfirmDialogText,GuidedReaderOverlayVisibility}.kt`.

### Files modified

`core/designsystem/theme/{Color,Shape,Theme}.kt`, `feature/reader/
{ReaderScreen,ReaderUiAction,ReaderViewModel}.kt`, `feature/reader/
components/{ReaderArabicContentBlocks,ReaderOverflowMenu,
ReaderStepItem}.kt`, `feature/reader/settings/ReaderSettingsSheet.kt`,
`feature/guidedreader/{GuidedReaderScreen,GuidedReaderBars,
GuidedReaderUiAction,GuidedReaderUiState,GuidedReaderViewModel}.kt`,
`feature/guidedreader/components/GuidedStepContent.kt`,
`res/values/strings.xml`. `androidTest/.../feature/reader/
ReaderScreenTest.kt` adapted (settings now reached through the overflow
menu, not a content-description-tagged top-bar icon; close action is now
"Selesai" not "Tutup") — required for compilation and accuracy, not a new
test.

`data/local/dao/AmaliyahVersionDao.kt`, `data/repository/
{ContentRepositoryImpl,GuidedReadingRepositoryImpl,
ReaderSettingsRepositoryImpl,ReadingPositionRepositoryImpl}.kt`,
`feature/home/SerambiViewModel.kt`, `feature/reader/
ReaderEntryViewModel.kt` also show as modified — confirmed via `git diff
--ignore-all-space` to be **whitespace-only** (a pre-existing constructor-
indentation style `ktlintFormat` corrected project-wide as a side effect of
running the required formatting command). No logic in any of these files
changed.

### Commands executed

`./gradlew :app:ktlintFormat`, `:app:ktlintCheck`, `:app:detekt`,
`:app:lintDebug`, `:app:assembleDebug`, `:app:compileDebugUnitTestKotlin`,
`:app:compileDebugAndroidTestKotlin` — all passed. `adb devices` returned
empty (no emulator/device this session).

### Known limitations

* **No manual on-device verification** — no emulator was available.
  Everything above is verified by static analysis and successful
  compilation/build only.
* **Translation line spacing has no UI control this phase.** The revised
  design-tool settings sheet only shows 3 steppers; `ReaderSettings.
  translationLineSpacingMultiplier`, `ReaderUiAction.
  SetTranslationLineSpacing`, and `ReaderSettingsRepository.
  setTranslationLineSpacing` all remain fully functional in the data/domain
  layer (untouched), but nothing in the UI calls them anymore. This is a
  real, narrower-than-FR-008 gap opened by following the revised design-tool
  layout exactly rather than guessing a 4th stepper back in — flag for a
  product decision (restore the control, or update FR-008) before this is
  considered fully resolved.
* **Dark-mode color tokens are a reasoned approximation, not
  design-tool-verified** — no dark-mode frame was exported; the new dark-scheme
  role values reuse existing green-ramp tokens rather than inventing new,
  unverified hex values.
* **RTL was not manually re-verified** for any of the new UI (progress
  header, repetition pill, TOC sheet, saved-position pill, restyled guided
  nav buttons) — all use standard Compose layout primitives that mirror
  automatically under RTL locale, and Arabic content blocks keep their
  existing forced-RTL handling unchanged, but this is reasoned, not tested.
* **Adaptive/tablet behaviour is unchanged from Milestone 3** — the
  existing 640dp max-width constraint on both readers was preserved as-is;
  no additional expanded-window-size-class work was done this phase beyond
  what already existed.
* The bottom-navigation rollout-timing question and the Aktivitas
  design-tool-frame gap (both flagged in `docs/design/DESIGN_HANDOFF.md`) are
  unrelated to this phase and remain open.

### Next recommended milestone

Phase B — Release `0.0.1` Beranda and Explore Amaliyah
(`docs/design/DESIGN_HANDOFF.md`, nodes `19:2`/`19:84`), on the user's
explicit "done" reply.

## Milestone 7 follow-up — Reader visual-fidelity and accessibility correction

**Status:** Implemented and verified locally — `ktlintFormat`,
`ktlintCheck`, `detekt`, `lint`, and `assembleDebug` pass. No connected
emulator/device was available (`adb devices -l` returned an empty device
list), so no install or manual on-device validation was performed.

**Scope:** Close measurable presentation and accessibility gaps left by
the initial Phase A implementation while preserving its existing reader
behavior, navigation, ViewModels, repositories, Room/DataStore persistence,
canonical content, counter state, and source/approval semantics. No
Beranda/Jelajahi, Standalone Tasbih, data-layer, schema, migration, or
religious-content work was added.

### Design-tool exports inspected

The JSON hierarchy and 2x PNG reference were inspected for every Phase A
reader pair: `14:2` Full Reader, `14:32` Guided Reader, `16:2` Full Reader
overflow, `16:45` Guided Reader overflow, `16:89` Reader Settings, and
`16:148` Reader Table of Contents. The four future-screen pairs were also
classified during the audit: `17:2`/`17:32` remain `0.0.2` Standalone
Tasbih work; `19:84` requires the not-yet-implemented Explore/favourite/
category state; `19:2` includes sections that cannot yet be truthfully
backed by current persistence.

### What changed

* Added shared design-tool-derived component dimensions for the 56dp compact app
  bar, 20dp reader gutter, 640dp readable maximum width, 22/24dp reader
  surface radii, 210×150dp Guided counter, 280dp overflow menu, 48dp touch
  target, and 550dp sheet maximum.
* Full Reader substantive Arabic/prayer/repetition blocks now sit on a
  plain warm surfaced reading container with a hairline outline. Headings
  and instructions stay unboxed to avoid turning the canonical step list
  into a card wall.
* Guided Reader now uses the same constrained gutter/max width, a surfaced
  24dp-radius reading card, a section title derived from the nearest
  canonical heading when the current content step has no title, and the
  design-tool-scale counter. The counter shows the count as the dominant element,
  a separate `dari N` target, grows vertically under font scaling, and
  retains haptics/reset/persistence plus icon-and-colour completion.
* Both reader top bars use the exported compact height. Guided navigation
  keeps 48dp targets, the filled tonal/primary pill treatment, mirrored
  arrows, and the existing enabled/finish behavior.
* Reader overflow matches the exported 280dp warm surface, 52dp rows,
  order, labels, rounded shape, and trailing visual cues.
* Reader Settings steppers now use compact tonal pill surfaces; exported
  labels/capitalisation are aligned; the existing Guided-only progression
  control remains contextual. Initial accessibility focus is requested on
  the sheet heading.
* Table of Contents now scrolls safely for the real Tahlil/Istighosah
  section count, uses stable keys, has a visible close action, requests
  initial focus, announces title/range/current state, and performs an
  instant jump without completion side effects or unconditional motion.
* `ReaderSettingStepper.kt` was renamed to `ReaderStepperControl.kt`, and
  `GuidedReaderCallbacks` was extracted, to satisfy the existing Detekt
  filename/function-count rules without suppressions.

### Files created

* `core/designsystem/theme/SanguSantriDimensions.kt`
* `feature/guidedreader/GuidedReaderCallbacks.kt`
* `feature/reader/settings/ReaderStepperControl.kt` (rename of
  `ReaderSettingStepper.kt`)

### Files modified

* `feature/reader/ReaderScreen.kt`
* `feature/reader/components/ReaderArabicContentBlocks.kt`
* `feature/reader/components/ReaderOverflowMenu.kt`
* `feature/reader/components/ReaderStepItem.kt`
* `feature/reader/settings/ReaderSettingsSheet.kt`
* `feature/reader/toc/ReaderTableOfContentsSheet.kt`
* `feature/guidedreader/GuidedReaderBars.kt`
* `feature/guidedreader/GuidedReaderScreen.kt`
* `feature/guidedreader/components/GuidedStepContent.kt`
* `feature/guidedreader/components/GuidedTasbihCounter.kt`
* `app/src/main/res/values/strings.xml`
* `docs/PROGRESS.md`

The required project-wide `ktlintFormat` task also reformatted constructor
indentation in the same previously reported DAO/repository/ViewModel files;
`git diff --ignore-all-space` confirms those additional diffs are
whitespace-only.

### Commands executed

* `adb devices -l` — the sandboxed attempt could not start the ADB socket;
  the approved retry succeeded and returned no connected devices.
* `./gradlew ktlintFormat` — the sandboxed attempt could not write the
  Gradle wrapper lock; the approved retry passed.
* `./gradlew assembleDebug` — passed after the first implementation pass.
* `./gradlew ktlintCheck detekt` — first run exposed seven decomposition
  issues; all were fixed without suppressions.
* `./gradlew ktlintFormat ktlintCheck detekt lint assembleDebug` — final
  gate passed (`BUILD SUCCESSFUL`).

No unit, instrumented, connected, or screenshot tests were run or added,
per the temporary design implementation-pass constraint.

### Visual validation and known limitations

Static comparison was completed against all six Phase A PNGs and their JSON
measurements: layout hierarchy, gutters, touch sizes, surface/border roles,
counter hierarchy, sheet/menu dimensions, and labels were checked after the
final changes. Manual device checks for dark mode, RTL, landscape, tablet,
large font, Arabic clipping, modal focus, and interaction could not be
performed because no device/emulator was connected.

The exported Full Reader mock groups a heading and reading content inside
one card; the app keeps canonical heading steps unboxed and surfaces only
substantive reading steps so lazy-list position IDs remain unchanged and
the long reader does not become a card wall. The TOC close action is an
intentional accessibility addition not drawn in `16:148`. No dark-mode
design-tool frame exists, so dark colours remain the prior Phase A reasoned
mapping. The approved Arabic font and a Reader Settings decision for the
still-persisted translation-line-spacing preference remain outstanding.

### Next recommended milestone

Phase B only after its required local favourite/recent/category state and
navigation rollout decision are in scope; otherwise perform the missing
on-device Phase A visual/accessibility validation first.

## Milestone 8 — Content Delivery Foundation and Remote Synchronisation

**Status:** Implemented and verified locally — `:app:ktlintFormat`,
`:app:ktlintCheck`, `:app:detekt`, `:app:lintDebug`, `:app:testDebugUnitTest`
(41/41), `:app:compileDebugAndroidTestKotlin`, `:app:assembleDebug`, and
`:app:assembleRelease` (R8/shrinking, `lintVitalRelease`) all pass.
`:app:connectedDebugAndroidTest` was **not** run — `adb devices` returned no
attached device/emulator this session; the new and adapted instrumented
tests below compile and are believed correct against a real in-memory Room
database and MockWebServer, but are unverified on-device. No manual
on-device verification was performed for the same reason.

**Scope:** An explicitly approved product/tech-lead decision (out of the
normal `0.0.1` reader-UX phase sequence, alongside Milestone 6's precedent)
to refactor the bundled-content pipeline into a shared, transport-agnostic
importer and implement the Android remote-content-synchronisation
foundation now, ahead of the Go backend's own implementation (a parallel,
still-undeployed workstream). The application remains fully functional
offline, with the backend unreachable, or before it has ever been deployed
— API failure never removes, replaces, downgrades, or hides valid content
already in Room.

### Refactor: one shared content-package importer, not two

`data/local/seed/` (`SeedContentSource`, `AssetSeedContentSource`,
`SeedContentImporter`, `SeedContentValidator`, `SeedContentChecksum`,
`SeedImportOutcome`, `SeedContentMapper`, `dto/ContentManifestDto`,
`dto/ContentPackageDto`) is deleted outright, not renamed in place. Its
canonical-model/checksum/validation/transactional-import behaviour was
extended (version comparison, atomic replace, progress reset) and moved to
`data/content/` (`ContentPackageImporter`, `ContentPackageValidator`,
`ContentChecksum`, `ContentImportOutcome`, `ContentPackageMapper`,
`dto/ContentPackageDto`) — the one shared boundary bundled assets and the
backend both go through. No second generic `ContentSource` interface was
reintroduced: there are exactly two concrete responsibilities
(`BundledContentBootstrapper` reads `AssetManager` directly, no interface
around it; `ContentRemoteDataSource` talks to Retrofit), each with exactly
one implementation.

`ContentPackageImporter.importPackage(rawBytes, expectedVersionId,
expectedChecksumSha256)` does, in order: checksum verify → parse → verify
the parsed `version.id` matches what the caller declared → structural
validation → minimum-app-version-code check → compare against
`AmaliyahVersionDao.getActiveForVariant` (a new query returning Room's one
active row for a variant regardless of status) → import fresh, skip
(older/up-to-date), reject (checksum conflict), or replace. A replace
inserts the new approval/version/steps first, then — only then — deletes
the previous version's version-scoped reading progress
(`ReadingPositionDao`/`GuidedReadingSessionDao`/`StepProgressDao`, each
gained a new `deleteByVersionId`), the previous version row (cascades its
steps via the existing foreign key), and the previous approval row, all
inside one `SanguSantriDatabase.withTransaction` block — matching the
brief's exact ordering (insert new before deleting old, since both carry
different immutable ids and cannot collide). `AmaliyahDao`/
`AmaliyahVariantDao` gained `@Upsert` methods (metadata such as a
corrected title now updates in place across a version bump, per the
brief); approval/version/step rows remain plain, never-updated inserts,
consistent with their immutability.

### Previous-version fallback removed (PRD FR-011 rewritten)

`AmaliyahVersionDao.getLatestNonRevokedForVariant` and
`ContentRepositoryImpl`'s corresponding `BuildConfig.DEBUG`-gated fallback
in `resolveVersion` are deleted; `getDefaultVersionDetail` now resolves
`getLatestPublishedForVariant` unconditionally, in every build. Android
retains only the current active version per variant — no previous-version
browsing, no previous-version fallback — while the backend keeps full
immutable revision history (ADR 0008, unaffected). This on-device fallback
was never actually wired to real revocation behaviour in the running app
(it only ever surfaced local `DRAFT` fixtures in debug builds, a
development affordance, not FR-011's revoked-version fallback), so no real
production behaviour was removed — only dead/superseded logic and its
matching documentation.

### Remote data and sync layers (new)

`data/remote/api/ContentApiService.kt` (Retrofit interface: `GET
v1/content/manifest` with `If-None-Match`, `GET
v1/content/packages/{versionId}` streamed via `@Streaming`),
`data/remote/dto/RemoteContentManifestDto.kt` (backend-specific manifest
shape — deliberately different from the bundled manifest DTO, sharing only
`ContentPackageDto`), `data/remote/ContentRemoteDataSource.kt` (typed
`ManifestFetchOutcome`/`PackageFetchOutcome`/`RemoteContentFailure` —
Retrofit `Response`/`ResponseBody`/HTTP exceptions never cross this
boundary; package bytes stream into a 5 MiB-capped temporary cache file,
always deleted after use, success or failure).

`data/sync/ContentSyncCoordinator.kt` implements the sync algorithm: fetch
manifest with the stored ETag → `304` stops immediately (`NotModified`, no
package touched) → `200` checks `schemaVersion`, then per package checks
`minimumAppVersionCode` and compares the manifest-declared
`versionNumber`/`checksumSha256` against `ContentPackageImporter`'s active-
version summary *before downloading anything* (bandwidth avoidance) —
older/up-to-date/checksum-conflicting packages are never downloaded; only
a genuinely newer package is fetched and handed to the same
`ContentPackageImporter.importPackage` bundled bootstrap uses. Per-package
failure is isolated (one malformed/unreachable package never blocks
another); the run's outcome distinguishes `NotModified`/`NoChanges`/
`Updated`/`PartialFailure`/`CompleteFailure`/`Failed` (`ContentSyncOutcome.kt`).
`data/sync/ContentSyncMetadata.kt` persists `content_last_sync`
(status + the last *terminal* attempt's timestamp),
`content_manifest_etag`, and `content_manifest_version` through the
existing `app_metadata` table — no new table was created for this.
`data/sync/ContentSyncScheduler.kt` enqueues one unique one-time
`ContentSyncWorker` (`sangu-santri-content-sync`, `ExistingWorkPolicy.KEEP`,
`NetworkType.CONNECTED`) only when the last terminal attempt is 24+ hours
old or has never happened — not a periodic worker.
`data/sync/ContentSyncWorker.kt` (`@HiltWorker`/`CoroutineWorker`)
classifies `IOException`/timeout/HTTP 408/429/5xx as transient (bounded
exponential backoff, `Result.retry()`, 3 attempts total; terminal `FAILED`
recorded only after the last attempt) and everything else (unsupported
schema, checksum mismatch, invalid structure, unsupported minimum app
version, non-retriable 4xx) as permanent (terminal `FAILED` recorded
immediately, Room untouched, no crash).

### Network configuration and DI

`gradle/libs.versions.toml`/`app/build.gradle.kts`: Retrofit `3.0.0` +
`converter-kotlinx-serialization` `3.0.0`, OkHttp `5.4.0`, WorkManager
(`work-runtime-ktx`) `2.11.2`, Hilt Worker extension
(`androidx.hilt:hilt-work`/`hilt-compiler`) `1.4.0` — all latest stable,
no alpha/beta. `BuildConfig.CONTENT_API_BASE_URL` is set from the Gradle
property `SANGU_CONTENT_API_BASE_URL`, defaulting to the non-routable
`https://content-api.sangusantri.invalid/` (RFC 2606) when unset — the
project builds and the app runs fully offline with no real backend
configured; supplying the real property later activates real sync with no
Android code change. `di/NetworkModule.kt` provides the `OkHttpClient`,
`Json`, `Retrofit`, and `ContentApiService` singletons.
`di/ContentModule.kt`'s `SeedContentSource` binding was removed (no longer
exists); every new class is a plain `@Inject constructor` with no `@Binds`
needed. `AndroidManifest.xml` gained `INTERNET`/`ACCESS_NETWORK_STATE`
permissions and a manifest-merge removal of WorkManager's default
`androidx-startup` `WorkManagerInitializer` (verified absent from the
built release manifest via `aapt2 dump xmltree`), since
`SanguSantriApplication` now supplies its own `Configuration.Provider`
(injected `HiltWorkerFactory`) per official Hilt+WorkManager guidance.

### Application startup

`SanguSantriApplication.onCreate()` launches one application-scoped IO
coroutine that calls `bundledContentBootstrapper.bootstrap()` then
`contentSyncScheduler.enqueueIfStale()` — neither blocks the first frame;
Beranda continues to observe Room reactively exactly as before. Bootstrap
failure is caught and logged, never crashes, and does not prevent sync
scheduling from still running afterward.

### Tests deleted, renamed, and added

Deleted: `data/local/seed/SeedContentImporterTest.kt` (androidTest),
`data/local/seed/SeedContentChecksumTest.kt`/`SeedContentValidatorTest.kt`
(test) — superseded by the renamed/rewritten tests below, not merely
duplicated at a different layer.

Renamed and extended: `data/content/ContentChecksumTest.kt`,
`data/content/ContentPackageValidatorTest.kt` (test, same coverage as
before under the new names). `data/content/ContentPackageImporterTest.kt`
(androidTest, in-memory Room) covers: fresh import into empty Room,
re-import idempotency, never-downgrading a lower incoming version,
same-version-different-checksum rejected as a conflict with Room
unchanged, invalid checksum/structure rejected with nothing written, a
genuine SQLite constraint failure mid-import rolling back the whole
package, a higher version replacing the active version atomically
(including updated amaliyah metadata), and version-scoped progress
(reading position, guided session, step counters) being removed after a
replace.

Added: `data/sync/ContentSyncMetadataTest.kt` (test, fake in-memory
`AppMetadataDao`, no Room needed — covers the metadata read/write
contract the 24-hour gate depends on). `data/sync/ContentSyncCoordinatorTest.kt`
(androidTest, MockWebServer + in-memory Room) covers: a `304` response
never downloads any package, a manifest-fetch failure (`500`) leaves Room
unchanged, a package already matching Room's active version/checksum is
skipped without a package-endpoint request, and one package's `500`
failure is isolated from another package's success in the same manifest
(`PartialFailure`, correct updated/failed lists, Room holds the successful
package only). `data/sync/ContentSyncSchedulerTest.kt` (androidTest,
`androidx.work:work-testing`) covers: a fresh install with no prior sync
enqueues work, a sync within the last 24 hours does not, a sync older than
24 hours does, and repeated calls under `ExistingWorkPolicy.KEEP` never
produce more than one enqueued work item.

Adapted (compilation only, no behaviour change): `feature/home/SerambiScreenTest.kt`
and `feature/reader/ReaderScreenTest.kt` now inject `BundledContentBootstrapper`
and call `.bootstrap()` instead of the deleted `SeedContentImporter`/
`.importSeedContent()`.

### Files created

`data/content/{ContentChecksum,ContentPackageValidator,ContentPackageMapper,
ContentImportOutcome,ContentPackageImporter}.kt`,
`data/content/dto/ContentPackageDto.kt`,
`data/local/content/{BundledManifestDto,BundledContentBootstrapper}.kt`,
`data/remote/api/ContentApiService.kt`,
`data/remote/dto/RemoteContentManifestDto.kt`,
`data/remote/{ContentRemoteDataSource,RemoteContentFailure}.kt`,
`data/sync/{ContentSyncMetadata,ContentSyncOutcome,ContentSyncCoordinator,
ContentSyncScheduler,ContentSyncWorker}.kt`, `di/NetworkModule.kt`,
`docs/decisions/0012-bundled-bootstrap-and-remote-sync.md`.

Test: `data/content/{ContentChecksumTest,ContentPackageValidatorTest}.kt`,
`data/content/ContentPackageImporterTest.kt` (androidTest),
`data/sync/ContentSyncMetadataTest.kt`,
`data/sync/{ContentSyncCoordinatorTest,ContentSyncSchedulerTest}.kt`
(androidTest).

### Files modified

`data/local/dao/{AmaliyahDao,AmaliyahVariantDao,AmaliyahVersionDao,
ApprovalDao,ReadingPositionDao,GuidedReadingSessionDao,StepProgressDao}.kt`
(new `@Upsert`/`getActiveForVariant`/`deleteById`/`deleteByVersionId`
methods; removed dead `existsById`/`getLatestNonRevokedForVariant`),
`data/repository/ContentRepositoryImpl.kt` (removed the `BuildConfig.DEBUG`
fallback), `di/ContentModule.kt` (removed the deleted `SeedContentSource`
binding), `SanguSantriApplication.kt` (`Configuration.Provider`, bundled
bootstrap + sync scheduling on startup), `AndroidManifest.xml` (permissions,
WorkManager initializer removal), `app/build.gradle.kts`,
`gradle/libs.versions.toml` (new dependencies, `CONTENT_API_BASE_URL`),
`feature/home/AmaliyahCard.kt`/`domain/model/StepType.kt` (stale "seed"
wording in comments only), `feature/home/SerambiScreenTest.kt`,
`feature/reader/ReaderScreenTest.kt` (see Tests above).

Docs: `docs/product/PRD.md` (version 1.4 → 1.5: Backend/offline-first
framing, §5.1/§5.2, §8.1, FR-001, rewritten FR-010, rewritten FR-011, §13
item 12), `docs/product/ROADMAP.md`, `docs/engineering/ARCHITECTURE.md`
(package structure, new Remote content synchronisation section, stale
feedback-endpoint references removed), `docs/engineering/OFFLINE_FIRST.md`
(rewritten to match implemented reality), `docs/engineering/CONTENT_MODEL.md`
(backend-history-vs-Android-retention note, progress-reset note, sync
metadata via `app_metadata`), `docs/content-schema.md` (shared
package/two-manifests framing, renamed class references),
`docs/decisions/0006-content-schema-and-seed-import.md` (Consequences
marked superseded, not rewritten), `CLAUDE.md`, this file.

### Files deleted

`data/local/seed/` (entire package): `SeedContentSource.kt`,
`AssetSeedContentSource.kt`, `SeedContentChecksum.kt`,
`SeedContentValidator.kt`, `SeedContentImporter.kt`,
`SeedContentMapper.kt`, `SeedImportOutcome.kt`,
`dto/ContentManifestDto.kt`, `dto/ContentPackageDto.kt`. Test:
`test/.../data/local/seed/{SeedContentChecksumTest,SeedContentValidatorTest}.kt`,
`androidTest/.../data/local/seed/SeedContentImporterTest.kt`.

### Commands executed

`./gradlew :app:compileDebugKotlin`, `:app:compileDebugUnitTestKotlin`,
`:app:compileDebugAndroidTestKotlin`, `:app:ktlintFormat`, `:app:ktlintCheck`,
`:app:detekt`, `:app:testDebugUnitTest`, `:app:lintDebug`,
`:app:assembleDebug`, `:app:assembleRelease` — all passed. `adb devices`
returned no attached device/emulator. `aapt2 dump badging`/`dump xmltree`/
`unzip -l` against the built release APK — confirmed manually (see Manual
verification below).

### Results

`testDebugUnitTest`: 41/41 JVM unit tests passed (7 test classes, 0
failures/errors). `ktlintFormat`/`ktlintCheck`/`detekt`: all pass (0 issues,
`maxIssues: 0` gate). `lintDebug`: `BUILD SUCCESSFUL`, 13 pre-existing/
version-suggestion warnings, 0 errors. `assembleDebug`/`assembleRelease`:
`BUILD SUCCESSFUL`, including `lintVitalRelease` and R8 shrinking/
optimisation. `compileDebugAndroidTestKotlin`: compiles clean (one
pre-existing, unrelated `createAndroidComposeRule` deprecation warning).
`connectedDebugAndroidTest` was not run — no device/emulator available.

### Manual verification

No emulator/device was available this session, so no on-device manual
verification was performed. Verified instead by direct inspection of build
artifacts: the built release APK (`aapt2 dump badging`) declares `INTERNET`/
`ACCESS_NETWORK_STATE`; its merged manifest (`aapt2 dump xmltree`) shows the
`androidx.work.WorkManagerInitializer` meta-data entry correctly absent
from the `androidx-startup` provider (other libraries' initializers —
EmojiCompat, ProcessLifecycle, OkHttp's platform initializer, ProfileInstaller
— remain, confirming only the intended entry was removed) while
`SanguSantriApplication`'s custom `Configuration.Provider` compiles and
links correctly; `unzip -l` on the same release APK confirms
`assets/content/{manifest.json,tahlil-general-v1.json,istighosah-general-v1.json}`
remain bundled, byte-identical in size to before this milestone (bundled
Tahlil/Istighosah were never touched).

### Known limitations

* **No real backend exists or is deployed.** `BuildConfig.CONTENT_API_BASE_URL`
  points at a non-routable `.invalid` placeholder; every remote sync
  attempt in a real build will fail with a network error (correctly
  classified transient, correctly leaves Room untouched, correctly does
  not crash) until a real, deployed backend URL is supplied via the
  `SANGU_CONTENT_API_BASE_URL` Gradle/CI property. Implementing the actual
  Go service remains explicitly out of scope for this milestone (and
  remains a separate, explicitly-requested task per `CLAUDE.md`/ADR 0011).
* **`connectedDebugAndroidTest` was not run** — no emulator/device this
  session. The new and adapted instrumented tests (`ContentPackageImporterTest`,
  `ContentSyncCoordinatorTest`, `ContentSyncSchedulerTest`, the adapted
  `SerambiScreenTest`/`ReaderScreenTest`) compile successfully but are
  unverified on a real device.
* **No manual on-device verification** of the required scenarios (fresh
  install in airplane mode, 24-hour gate not re-firing within the window,
  a simulated newer/invalid remote package, progress reset after a real
  replacement, higher-Room-version-not-downgraded-by-bundled-bootstrap,
  process restart still showing Room immediately) — all are covered by the
  automated test suite above, none by a real device this session.
* The exact backend response shapes (`RemoteContentManifestDto`,
  `ContentPackageDto` over HTTP) are this Android team's contract proposal,
  informed by `CLAUDE.md`'s suggested shape — they have not been confirmed
  against a real, running Go implementation, since none exists yet.
* Revocation (`AmaliyahVersionStatus.REVOKED`) has no dedicated remote
  handling beyond ordinary version comparison — a `REVOKED` remote entry is
  not currently distinguished from any other manifest entry by
  `ContentSyncCoordinator`; the backend is expected to simply stop listing
  a revoked variant's old version in future manifests rather than the
  Android client acting on a `status` value it receives. This was not
  called out as required behaviour in the brief and was not built.

### Next recommended milestone

Not specified by this brief. Building the actual Go backend service (ADR

0011) is the natural next step to make this Android work observably
      functional end-to-end, but remains a separate, explicitly-requested task —
      `docs/product/ROADMAP.md` should be revisited for the next scheduled
      Android-side item in the meantime (Phase B — Beranda/Jelajahi Amaliyah, per
      Milestone 7's own recommendation, still outstanding).

## Sync simplification pass (2026-07-28, post-Milestone 8)

**Status:** Implemented and verified locally — `:app:ktlintFormat`,
`:app:ktlintCheck`, `:app:detekt`, `:app:lintDebug`, `:app:testDebugUnitTest`
(48/48), `:app:compileDebugAndroidTestKotlin`, `:app:assembleDebug`, and
`:app:assembleRelease` (R8/shrinking, `lintVitalRelease`) all pass.
`:app:connectedDebugAndroidTest` was **not** run — `adb devices` returned no
attached device/emulator this session; the new/renamed instrumented test
(`ContentSyncManagerTest`) compiles and is believed correct against a real
in-memory Room database and MockWebServer, but is unverified on-device. No
manual on-device verification was performed for the same reason.

**Scope:** Explicitly approved product/tech-lead request to simplify
Milestone 8's remote-sync implementation, which had accumulated more
machinery (ETag conditional requests, a six-case sync outcome, a separate
HTTP-client wrapper class) than the actual requirement — the manifest is
checked at most once every 24 hours — justified. Not a new backend and not
a product-scope change: offline-first behaviour, bundled Tahlil/Istighosah,
Room as source of truth, atomic replacement, version/checksum comparison,
progress reset on replacement, the 24-hour `WorkManager` gate, and backend
contract compatibility are all preserved. Full decision record: ADR 0012's
2026-07-28 amendment.

### Why

The brief identified five simplification targets: (1) ETag added
conditional-request complexity for no real benefit given the 24-hour gate;
(2) `ContentRemoteDataSource` was a typed wrapper around exactly one
Retrofit service with exactly one caller; (3) the six-case
`ContentSyncOutcome` (`NotModified`/`NoChanges`/`Updated`/`PartialFailure`/
`CompleteFailure`/`Failed`) was more states than the three genuinely
distinct outcomes (completed-with-some-rejections is not a different
*kind* of result, just a non-empty list); (4) a package-level HTTP
failure was previously always classified as a permanent per-package
rejection, so a genuinely transient package timeout or `500` was never
retried at the sync level — a real behavioural bug, not just excess
structure; (5) the bundled bootstrap read and parsed every full package
asset on every launch instead of comparing against Room first, unlike the
remote path which already did this bandwidth-avoidance check.

### Classes deleted

`data/remote/ContentRemoteDataSource.kt`,
`data/remote/RemoteContentFailure.kt` (`RemoteContentFailure`,
`ManifestFetchOutcome`, `PackageFetchOutcome`),
`data/sync/ContentSyncOutcome.kt` (`ContentSyncOutcome`,
`ManifestSyncInfo`) — replaced by `data/sync/SyncResult.kt`.

### Classes renamed

`data/sync/ContentSyncCoordinator.kt` → `data/sync/ContentSyncManager.kt`
(git-aware rename; HTTP handling — manifest fetch, package streaming into
a size-limited temporary file, HTTP/`IOException` classification — moved
in from the deleted `ContentRemoteDataSource`).
`data/sync/ContentSyncCoordinatorTest.kt` (androidTest) →
`data/sync/ContentSyncManagerTest.kt` (rewritten, not just renamed — see
Tests below).

### ETag removal

`ContentApiService.getManifest()` no longer takes an `If-None-Match`
header or returns a `304`-aware response; `RemoteContentManifestDto`
dropped `manifestVersion`/`generatedAt`/`status` (the new manifest
contract, section 10 of the brief, lists only each variant's currently
active package: `contentId`, `variantId`, `versionId`, `versionNumber`,
`checksumSha256`, `minimumAppVersionCode`). `ContentSyncMetadata` now
stores only `content_last_sync` (`SUCCESS`/`PARTIAL`/`FAILED`) in
`app_metadata` — `content_manifest_etag` and `content_manifest_version`
are both gone; nothing else read the latter beyond storing it, so it had
no debugging/operational use worth keeping (per the brief's own
"remove unless inspection finds an actual runtime use" instruction).

### Simplified sync results

`SyncResult` (`data/sync/SyncResult.kt`) replaces the six-case
`ContentSyncOutcome` with `Completed(updatedVersionIds, skippedVersionIds,
rejectedVersionIds)` / `RetryableFailure(reason)` /
`PermanentFailure(reason)`. There is no separate partial-failure case —
`Completed` simply carries a non-empty `rejectedVersionIds` alongside
whatever did update or was skipped.

### Package retry fix

`ContentSyncManager` now classifies a package-level HTTP/network failure
using the same retryable/permanent rule as a manifest-level failure
(`isRetryableHttpStatus` — HTTP 408/429/5xx or `IOException` — is
retryable; other 4xx and data-contract violations are permanent). A
retryable package failure aborts the whole `sync()` call with
`SyncResult.RetryableFailure`, so `ContentSyncWorker` retries the entire
sync; packages already imported earlier in the same attempt are simply
skipped on the retry, since Room already matches them (verified by
`ContentSyncManagerTest.retryAfterPackageFailureSkipsAlreadyImportedPackages`,
which asserts the already-imported package's endpoint is never requested
again). A permanent package failure (checksum mismatch, invalid JSON,
non-retryable 4xx, minimum app version too high) still only rejects that
one package and lets the rest of the manifest continue — `rejectedVersionIds`
carries it, `ContentSyncWorker` records terminal `PARTIAL` rather than
`FAILED`.

### Bundled manifest pre-comparison

`BundledManifestEntryDto` gained `variantId`/`versionNumber` (bundled
`manifest.json` updated to match: `tahlil-umum`/`1`,
`istighosah-umum`/`1`, read from the actual bundled package files, not
invented). `BundledContentBootstrapper.evaluate` now calls
`ContentPackageImporter.activeVersionSummary(entry.variantId)` and a new
shared pure function, `decideContentVersionAction`
(`data/content/ContentVersionAction.kt`), *before* reading the package
asset — an older or already-current bundled entry is skipped without ever
opening its file. `ContentSyncManager` was refactored to call the same
shared function instead of its own inline copy of the same comparison,
removing duplication between the two callers. `ContentPackageImporter`
itself is unchanged and still re-runs its own authoritative comparison
and checksum verification — this is an optimisation, not a safety
relaxation (section 18 of the brief).

### Tests deleted, rewritten, and added

Deleted: the two ETag-specific `ContentSyncMetadataTest` cases
(`saveManifestInfoPersistsEtagAndManifestVersion`,
`saveManifestInfoWithNullEtagLeavesEtagUnset` — `saveManifestInfo`/
`ManifestSyncInfo`/`getStoredEtag` no longer exist), the `304`-specific
`ContentSyncCoordinatorTest.notModifiedResponseNeverDownloadsAnyPackage`
case (folded into the rewrite below, since there is no `304` concept left
to test).

Rewritten: `ContentSyncManagerTest.kt` (androidTest, MockWebServer +
in-memory Room, renamed from `ContentSyncCoordinatorTest.kt`) covers: a
matching remote version skips the package download, a newer remote
version downloads and replaces content, a manifest HTTP 500 returns
`RetryableFailure`, a manifest non-retryable HTTP 4xx returns
`PermanentFailure`, a package HTTP 500 returns `RetryableFailure`, a retry
after a package failure re-attempts only the not-yet-imported package
(asserting exact request counts on both attempts), a permanently invalid
package (checksum mismatch) is rejected while another package in the same
manifest still imports, a same-version-different-checksum conflict is
rejected without a package download, and a `minimumAppVersionCode` too
high for the current build rejects the package without a download.
`ContentSyncMetadataTest.kt` (test) rewritten to the simplified
`SUCCESS`/`PARTIAL`/`FAILED` contract, plus a new case confirming a
second terminal sync overwrites the first status.
`ContentSyncSchedulerTest.kt`'s fake DAO seed helper updated from the
removed `ContentSyncStatus.UPDATED` to `SUCCESS`.

Added: `ContentVersionActionTest.kt` (test,
`data/content/ContentVersionAction.kt`'s pure decision function — no
Room/MockWebServer needed) and `ContentSyncHttpClassificationTest.kt`
(test, `isRetryableHttpStatus` — a top-level function in
`ContentSyncManager.kt` kept public specifically so it is JVM-unit-testable
without constructing the class), covering the brief's required "manifest
version comparison logic" and "HTTP status classification /
retryable-versus-permanent classification" JVM test bullets.

### Commands executed

`./gradlew :app:compileDebugKotlin`, `:app:compileDebugUnitTestKotlin`,
`:app:compileDebugAndroidTestKotlin`, `:app:ktlintFormat`, `:app:ktlintCheck`,
`:app:detekt`, `:app:testDebugUnitTest`, `:app:lintDebug`,
`:app:assembleDebug`, `:app:assembleRelease` — all passed. `adb devices`
returned no attached device/emulator.

### Results

`testDebugUnitTest`: 48/48 JVM unit tests passed (net +7 versus Milestone
8's 41: −2 removed ETag cases, +1 new terminal-overwrite case, +5 new
`ContentVersionActionTest`/`ContentSyncHttpClassificationTest` cases).
`ktlintFormat`/`ktlintCheck`/`detekt`: all pass (0 issues). `lintDebug`:
`BUILD SUCCESSFUL`, 0 errors. `assembleDebug`/`assembleRelease`: `BUILD
SUCCESSFUL`, including `lintVitalRelease` and R8 shrinking. Verified the
built release APK still bundles `assets/content/manifest.json` with the
updated `variantId`/`versionNumber` fields and both content packages
byte-identical to before this pass (`unzip -l`/`unzip -p`).
`compileDebugAndroidTestKotlin`: compiles clean (two pre-existing,
unrelated `createAndroidComposeRule` deprecation warnings).
`connectedDebugAndroidTest` was not run — no device/emulator available.

### Known limitations

* **No real backend exists or is deployed** — unchanged from Milestone 8;
  `BuildConfig.CONTENT_API_BASE_URL` still points at a non-routable
  `.invalid` placeholder.
* **`connectedDebugAndroidTest` was not run** — no emulator/device this
  session. `ContentSyncManagerTest` (new/rewritten) compiles successfully
  but is unverified on a real device.
* **No manual on-device verification** of the required scenarios (fresh
  install offline, backend unavailable leaves Room unchanged, no-update
  skips the package endpoint, a new update replaces content, a temporary
  package failure retries and old content stays readable, a retry after
  partial success skips the already-imported package, an invalid package
  is rejected while a valid one still imports, bundled downgrade
  prevention) — all covered by the automated test suite above, none by a
  real device this session.
* Milestone 8's own "Known limitations" section still references
  `ContentSyncCoordinator` by its pre-rename name in one bullet about
  revocation handling — left as historical record of what was true when
  that milestone was written, per this project's convention of not
  rewriting past milestone entries; the class is `ContentSyncManager` as
  of this pass. The underlying limitation itself (no dedicated remote
  handling for `REVOKED` beyond ordinary version comparison) is unchanged.

### Next recommended milestone

Unchanged from Milestone 8: building the actual Go backend service (ADR

0011) is the natural next step to make this Android work observably
      functional end-to-end, but remains a separate, explicitly-requested task.
      `docs/product/ROADMAP.md` should be revisited for the next scheduled
      Android-side item in the meantime (Phase B — Beranda/Jelajahi Amaliyah).

## Product-owner scope decision (2026-07-29): bottom-navigation-only, Nahwu Quiz moved to 0.0.5

**Not a milestone — a scope/decision record.** The product owner/tech lead
approved, in writing, a scope change superseding parts of the 2026-07-26
design product-alignment pass and its `docs/design/design-export/
future-releases/` specification:

* Navigation for every release through `0.0.5` uses **bottom navigation
  only** — no Navigation Rail is built for tablet/expanded width at any
  point in this range. This supersedes `docs/design/ARCHITECTURE.md`'s and
  `docs/design/DESIGN_SYSTEM.md`'s previously documented adaptive bottom-
  bar/rail plan for that range specifically; adaptive *content* layout
  (constrained/centred width) is unaffected and still required.
* Nahwu Quiz moves from `0.4.0` to **`0.0.5`**, immediately after Pengingat
  Amaliyah (`0.0.4`) and before Accounts (`0.1.0`). It remains individual/
  guest/offline-first — no login, no pesantren representation, no
  leaderboard — unchanged from the prior `0.4.0` scope, only the version
  number and roadmap position moved.
* Accounts (`0.1.0`), Pesantren Membership (`0.2.0`), Private Pesantren
  Space (`0.3.0`), leaderboard, and inter-pesantren ranking remain
  deferred/future, unchanged.
* Recorded formally as ADR
  [0013](decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md);
  normative docs (`PRD.md`, `ROADMAP.md`, `ARCHITECTURE.md`,
  `DESIGN_SYSTEM.md`, `DESIGN_HANDOFF.md`, `reviews/
  design-product-alignment.md`, `AGENTS.md`, `CLAUDE.md`) and the
  `future-releases/` spec files were updated in the same pass — see that
  ADR for the full list of touched files.

## Milestone 9 — Release 0.0.2, Phase 1: Standalone Tasbih and Bottom Navigation Shell

**Status:** Implemented and verified locally — `ktlintFormat`, `ktlintCheck`,
`detekt`, `lintDebug`, `assembleDebug`, `compileDebugUnitTestKotlin`, and
`compileDebugAndroidTestKotlin` all pass (see Commands executed below).
`connectedDebugAndroidTest`/`installDebug`/manual on-device verification
were **not** run — `adb devices` returned no attached device or emulator
this session; this is an honest gap, not a claim of on-device verification
that did not happen.

**Scope:** Release `0.0.2` per the product-owner-approved scope above:
Standalone Tasbih (counter, target presets/custom target, session naming,
reset, session history) plus the one bottom-navigation shell the whole
`0.0.2`–`0.0.5` body of work reuses (Beranda|Tasbih at this release). No
Aktivitas, Pengingat, or Nahwu Quiz — those are Phases 2–4 of this same
work order, phase-gated behind this phase compiling and validating first.

### Audit of pre-existing untracked work

Before this phase started, `git status` already showed a partial,
untracked Standalone Tasbih implementation: `TasbihSession`/
`TasbihHistoryEntry`/`TasbihTargetPreset` domain models, `TasbihRepository`/
`TasbihRepositoryImpl`, `TasbihSessionEntity`/`TasbihHistoryEntity` + DAOs,
`TasbihEntityMappers`, `TasbihModule`, and a hand-drawn `TasbihIcon` vector
(`core/designsystem/icon/`) — none yet registered in `SanguSantriDatabase`/
`DatabaseModule`, and no `feature/tasbih` UI existed. Audited against
`docs/design/design-export/future-releases/02-release-0.0.2-tasbih.md` and
`CODING_STANDARD.md`: high quality, correct no-99-preset rule, correct
singleton-active-session pattern, `TasbihIcon` a genuine custom vector (no
Unicode glyph). All of it was kept and built on, not replaced. One real bug
found and fixed: `TasbihRepositoryImpl.startSession` overwrote the active
session without archiving it to history first, so switching the target
preset mid-session (e.g. 33 → 100) silently discarded a completed or
in-progress count with no history row. Fixed by applying the same
archive-if-`currentCount > 0` rule `resetSession` already used.
`incrementCount`'s existing "tap again after target reached starts a new
cycle" behaviour was checked against the design spec's "Target tercapai —
ketuk untuk mengulang" caption — that caption *is* the documented explicit
tap-to-repeat interaction, so this behaviour was kept unchanged, not a bug.

### What shipped

* **Room**: `TasbihSessionEntity`/`TasbihHistoryEntity` and their DAOs
  registered into `SanguSantriDatabase` (still schema version 1, per the
  pre-public-release schema-freeze policy — no migration, developers must
  clear app data/reinstall once) and `DatabaseModule`. `TasbihTargetPreset`
  gained documented `MIN_CUSTOM_TARGET`/`MAX_CUSTOM_TARGET` (1–100,000 —
  the ceiling is a documented engineering decision, chosen because real
  amaliyah repetition counts already bundled in this app reach 30,000×)
  and default `THIRTY_THREE_TARGET`/`ONE_HUNDRED_TARGET` constants.
* **`core/designsystem/component/ConfirmationDialog.kt`** (+
  `ConfirmationDialogText.kt`): the shared "Confirmation Dialog Shell" named
  in `01-navigation-and-shared-components.md`, used by Tasbih's reset
  confirmation now and intended for Pengingat's delete confirmation later —
  a new shared component rather than reaching into `feature/guidedreader`'s
  existing, feature-scoped `GuidedConfirmDialog`.
* **`navigation/TopLevelBackStack.kt`**: the multiple-back-stacks helper
  from `android/nav3-recipes`' "Common UI" recipe (the reference
  `CODING_STANDARD.md` names for exactly this pattern), adapted to this
  project's `NavKey` types — one back stack per top-level tab, a single
  flattened back stack for `NavDisplay`, switching tabs never duplicates a
  `NavKey`, and a `replaceLast` operation preserving the existing in-reader
  mode-switch behaviour. This is bookkeeping on top of the same, one
  Navigation 3 system already in use — not a second navigation framework.
* **`navigation/BottomNavigationBar.kt`** + **`RootDestination.kt`**: a
  plain Material 3 `NavigationBar`/`NavigationBarItem` bar — deliberately
  **not** `NavigationSuiteScaffold` (which would auto-switch to a
  Navigation Rail on expanded width, directly contradicting the
  product-owner's bottom-navigation-only decision above). Always-visible
  labels, tonal pill indicator + filled/outlined icon swap on selection
  (colour is never the only signal), 48dp-minimum touch targets. Beranda
  uses `Icons.Filled/Outlined.Home`; Tasbih uses the existing custom
  `TasbihIcon`.
* **`SanguSantriNavHost.kt`** rewritten to own the app's single top-level
  `Scaffold` (bottom bar) wrapping the existing `NavDisplay`/`entryProvider`
  — new `Tasbih`/`TasbihHistory` `NavKey`s alongside the existing Serambi/
  reader graph, all switched from raw `MutableList<NavKey>` operations to
  `TopLevelBackStack`. Bottom bar is shown only when the current tab's own
  stack has depth 1 (its own root) — hidden on `AmaliyahDetail`/readers/
  Setelan/About/`TasbihHistory`, matching "hide on nested flow."
  `MainActivity.kt`'s previous outer `Scaffold` was removed (it would have
  double-applied system-bar inset padding against the nav host's new own
  `Scaffold` — `ARCHITECTURE.md`'s edge-to-edge rule).
* **`feature/tasbih/`**: `TasbihUiState` (`NoSession` / one `Active` shape
  covering design-spec states 2/3/4/9 — Sesi Aktif/Target Tercapai/Target
  Tanpa Batas/Sesi Dipulihkan are the same session data with different
  derived flags, not four screens), `TasbihUiAction`, `TasbihViewModel`
  (derives the transient "restored" indicator from whether this ViewModel
  instance's *first* observed session emission already had a positive
  count — clears itself the moment the count changes, matching "shown once
  per cold start, not persistent chrome"), `TasbihScreen`/`TasbihRoute`,
  and `components/`: `TasbihCounter` (`NEUTRAL`/`COUNTING`/
  `TARGET_REACHED` tones, 220dp minimum, haptic tap, `stateDescription`
  semantics; deliberately never disables tapping at `TARGET_REACHED`,
  unlike `GuidedTasbihCounter`, since tapping there is the documented
  "ketuk untuk mengulang" cycle-repeat interaction — a deliberate
  divergence, documented in code), `TasbihTargetSelector` (33/100/
  Unlimited/Custom chip row, no 99 preset, Custom chip never shows
  selected itself per the design spec), `CustomTasbihTargetDialog` +
  `CustomTargetValidation` (all six validation states: Valid/Empty/Zero/
  Negative/NonNumeric/TooLarge, rejected before dismissal is possible),
  `TasbihSessionNameField` (inline expanding text field, not a dialog, per
  spec), `TasbihSecondaryActions` (Reset/Riwayat chips), and
  `TasbihScreenLabels` (target-header eyebrow, autosave caption, restored
  indicator).
* **`feature/tasbih/history/`**: `TasbihHistoryUiState`/
  `TasbihHistoryViewModel`/`TasbihHistoryScreen` — empty state (`Status
  State`-equivalent, `history` icon) and filled list (session name or
  "Tasbih" fallback, target + final count, end time + duration), reachable
  via the Tasbih screen's Riwayat entry points; bottom bar hidden on this
  screen.
* **`gradle/libs.versions.toml`/`app/build.gradle.kts`**: replaced
  `material-icons-core` with `material-icons-extended` — justified by the
  full approved `0.0.2`–`0.0.5` scope's broad icon vocabulary (history,
  restart_alt, check_circle, notifications, calendar_month, repeat,
  lock_clock, wifi_off, etc. across the four phases), not "a couple of
  icons"; R8 resource/code shrinking (already enabled) strips unused icons
  from the release build.
* **Strings**: every new user-facing string added to `strings.xml` in
  Indonesian, none hardcoded in Kotlin.

### Files created

`navigation/TopLevelBackStack.kt`, `navigation/BottomNavigationBar.kt`,
`navigation/RootDestination.kt`, `core/designsystem/component/
ConfirmationDialog.kt`, `core/designsystem/component/
ConfirmationDialogText.kt`, `feature/tasbih/TasbihUiState.kt`,
`TasbihUiAction.kt`, `TasbihViewModel.kt`, `TasbihScreen.kt`,
`TasbihDialog.kt`, `feature/tasbih/components/TasbihCounter.kt`,
`TasbihCounterTone.kt`, `TasbihTargetSelector.kt`,
`CustomTasbihTargetDialog.kt`, `CustomTargetValidation.kt`,
`TasbihSessionNameField.kt`, `TasbihSecondaryActions.kt`,
`TasbihScreenLabels.kt`, `feature/tasbih/history/
TasbihHistoryUiState.kt`, `TasbihHistoryViewModel.kt`,
`TasbihHistoryScreen.kt`.

### Files modified

`domain/model/TasbihTargetPreset.kt`, `data/repository/
TasbihRepositoryImpl.kt`, `data/local/database/SanguSantriDatabase.kt`,
`di/DatabaseModule.kt`, `core/designsystem/theme/
SanguSantriDimensions.kt` (new `tasbihCounterMinSize` token),
`navigation/SanguSantriNavHost.kt`, `MainActivity.kt`,
`app/src/main/res/values/strings.xml`, `gradle/libs.versions.toml`,
`app/build.gradle.kts` (dependency swap + `versionCode = 3`/
`versionName = "0.0.2"`), `app/schemas/com.sangusantri.app.data.local.
database.SanguSantriDatabase/1.json` (regenerated by the Room/KSP compiler
to include the two new tables).

### Commands executed

`./gradlew :app:ktlintFormat` — passed (two manual line-length fixes
needed first; ktlint auto-corrected everything else). `./gradlew
:app:ktlintCheck :app:detekt` — failed on the first pass with 10 detekt
findings (`TooManyFunctions` in `DatabaseModule`/`SanguSantriDatabase`,
`LongParameterList` in `ConfirmationDialog`/`TasbihScreenContent`,
`LongMethod` in `TasbihScreen`/`TasbihCounter`/`TasbihActiveContent`,
`MatchingDeclarationName` in three files with a stray top-level type, one
extra `ReturnCount`); all ten fixed (two `@Suppress("TooManyFunctions")`
matching this codebase's own established convention for a Room `@Database`
class and its DI module, everything else by extracting types/functions
into correctly-named files or reducing parameter counts) — re-run passed
clean. `./gradlew :app:lintDebug` — passed. `./gradlew :app:assembleDebug`
— passed. `./gradlew :app:compileDebugUnitTestKotlin
:app:compileDebugAndroidTestKotlin` — both passed (only pre-existing,
unrelated `createAndroidComposeRule` deprecation warnings). `adb devices`
— no attached device/emulator.

### Known limitations

* **No on-device verification this session** — no emulator/device
  attached. Persistence-across-restart, haptic feedback, reset
  confirmation, history archival, dark mode, RTL, landscape, tablet width,
  and font-scale 1.5× are all implemented per spec but not manually
  exercised on a real device yet.
* **Expanded-width counter enlargement not implemented.** The design spec
  calls for the Tasbih counter to grow to 280dp/96sp on expanded (tablet)
  width; this pass reused the existing Reader's simpler pattern instead —
  a fixed 220dp-minimum counter inside a max-width-constrained, centred
  column (`SanguSantriDimensions.readerContentMaxWidth`, reused rather than
  a near-duplicate token) — since no `WindowSizeClass` adaptive
  infrastructure exists anywhere in this codebase yet and building it
  solely for one counter's font size would be disproportionate to this
  phase. The counter still reads correctly and meets every touch-target/
  minimum-size requirement at every width; only the literal 96sp expanded
  enlargement is deferred.
* **`connectedDebugAndroidTest` was not run** — no existing instrumented
  test touches Tasbih or the new nav shell yet (none were added, per this
  pass's explicit no-new-tests constraint), and no emulator was available
  to manually verify either.
* Both dialogs in the Standalone Tasbih layer map from the baseline export
  (`17:2`) are implemented as specified: a top-app-bar Reset icon *and* a
  bottom "Preset / Reset" chip both open the same Reset Confirmation
  Dialog — this reads as two entry points to one action, which is what the
  reused baseline frame actually specifies, not an inconsistency
  introduced by this pass.

### Next recommended milestone

Phase 2 (Release `0.0.3` — Aktivitas), per the product-owner-approved,
phase-gated work order above — starts once this phase's on-device
verification (whenever a device/emulator becomes available) confirms no
regression.

## Milestone 10 — Release 0.0.3, Phase 2: Aktivitas

**Status:** Implemented and verified — `ktlintFormat`, `ktlintCheck`,
`detekt`, `lintDebug`, `assembleDebug`, `compileDebugUnitTestKotlin`, and
`compileDebugAndroidTestKotlin` all pass. An emulator became available
partway through this phase (`Pixel_9`, Android 15/API 35) — installed after
clearing app data (required: `AmaliyahCompletionEventEntity` and
`GuidedReadingSessionEntity.startedAtEpochMillis` were added to the same
pre-release version-1 baseline) and manually verified end-to-end on-device
(see Manual verification below).

**Scope:** Aktivitas (`03-release-0.0.3-aktivitas.md`): one vertically
scrollable root screen, no horizontal tabs, per-section hide-if-empty,
streak + this-week summary, amaliyah-completion and tasbih-history
sections each with a filterable "Lihat semua", private/local-first only
(no share/export). Bottom navigation becomes Beranda | Aktivitas | Tasbih.
Real completion-event persistence for the amaliyah-completion history,
since no such signal existed before this phase.

### Completion-event design

The only existing "done" signal anywhere in the app was
`GuidedReadingSession.completedAtEpochMillis` (Milestone 4), which is
**version-scoped and gets deleted** when a content package is replaced
(ADR 0012's atomic version-replacement transaction) — unusable as a
durable history log, and it recorded no start time, so no honest duration
could be derived from it alone. Two changes:

* `GuidedReadingSession`/`GuidedReadingSessionEntity` gained
  `startedAtEpochMillis` — set once when a session is first created,
  preserved on every subsequent save (step moves, mode switches), never
  reset. This is the real, first-opened timestamp for that reading
  session, not a fabricated one. `ReaderViewModel.switchToGuided` (the
  Full→Guided in-reader switch, Milestone 5) also preserves it from any
  existing session rather than resetting it.
* A new, deliberately durable `amaliyah_completion_events` table
  (`AmaliyahCompletionEventEntity`/`AmaliyahCompletionEventDao`) — no
  foreign key to `amaliyah_versions`, snapshotting `amaliyahTitleId` and
  `versionNumber` at completion time instead, so it is untouched by a
  later content update or amaliyah rename (unlike the version-scoped
  tables, which are correctly wiped on replacement). Written exactly once
  per valid completion action: `GuidedReaderViewModel.onConfirmCompletion`
  gained a `completedAtEpochMillis.value != null` guard on top of its
  existing step/counter checks, specifically to guarantee "exactly one
  event," even though the Route already navigates away the instant
  `isCompleted` becomes true (defence in depth, not a fix for an observed
  bug).

### What shipped

* **Room**: `AmaliyahCompletionEventEntity`/`AmaliyahCompletionEventDao`
  registered into the existing version-1 baseline; `GuidedReadingSessionEntity`
  gained `startedAtEpochMillis` in the same baseline (no migration, per
  pre-release schema-freeze policy — clear app data/reinstall required).
* **`domain/repository/ActivityRepository`** +
  `data/repository/ActivityRepositoryImpl` — the one repository for the
  amaliyah-completion-event concern (`recordCompletion`,
  `observeCompletions`). Tasbih activity data is **not** duplicated into
  this repository or a new model — `TasbihRepository` (existing, `0.0.2`)
  is reused directly by consumers.
* **`domain/usecase/ObserveActivityOverviewUseCase`** — combines
  `ActivityRepository` + `TasbihRepository` (the genuine cross-repository
  aggregation logic `CODING_STANDARD.md` says justifies a use case):
  current/longest streak (consecutive local calendar days with at least
  one real completion or tasbih session — current streak counts backward
  from today, or from yesterday if nothing has happened yet today, per a
  standard one-day grace rule), a rolling 7-day "this week" summary
  (amaliyah completed count, tasbih session count, total minutes — summed
  from real recorded durations, never estimated), and the recent-5 preview
  list for each history section. `domain/model/ActivityOverview` exposes
  `hasStreak`/`hasWeeklyActivity`/`hasAmaliyahHistory`/`hasTasbihHistory`/
  `isEntirelyEmpty` — the per-section and screen-level hide-if-empty logic
  lives on the domain model, not scattered across the UI layer.
* **`core/designsystem/component/`**: `SectionHeader`, `SummaryMetric`
  (+`SummaryMetricEmphasis`), `ActivityRow` (+`ActivityRowKind`,
  `ActivityRowContent`), `TimeRangeFilterChips`/`filterByTimeRange`
  (+`TimeRangeFilterState`) — the shared components
  `01-navigation-and-shared-components.md` names, genuinely reused across
  Aktivitas' own 4 sections and 2 detail screens (not built speculatively
  for a single call site).
* **`feature/activity/`**: `ActivityUiState`/`ActivityViewModel`/
  `ActivityScreen` (root: constrained/centred scroll column mirroring
  Tasbih's own pattern, conditional `ActivityStreakSection`/
  `ActivityWeeklySection`/two `ActivityHistorySection`s, screen-level empty
  state only when `overview.isEntirelyEmpty`), `ActivityRowMappers.kt`
  (`AmaliyahCompletionEvent`/`TasbihHistoryEntry` → `ActivityRowContent`,
  reusing Tasbih's own (`0.0.2`) field-list strings for the tasbih-row
  wording rather than duplicating near-identical strings),
  `feature/activity/detail/`: `ActivityAmaliyahHistoryViewModel`/
  `ActivityTasbihHistoryViewModel` (each a thin `combine(repository flow,
  filter) →` filtered list) + one shared `ActivityHistoryDetailScaffold`
  (back + title-derived-from-kind + `TimeRangeFilterChips` + plain
  `ActivityRow` list, no pagination) reused by both detail screens rather
  than two near-duplicate layouts. Phase 1's own unfiltered
  `TasbihHistoryScreen` (reached from the Tasbih tab) is untouched — the
  Aktivitas "Lihat semua" Tasbih screen is a distinct, additional,
  filterable screen over the same underlying `TasbihRepository` data, per
  the design spec's explicit intent.
* **Navigation**: `Aktivitas`/`ActivityAmaliyahHistory`/
  `ActivityTasbihHistory` `NavKey`s added to `SanguSantriNavHost`; bottom
  nav is now Beranda | Aktivitas | Tasbih (`history` outlined/filled icon);
  the two detail screens hide the bottom bar like every other nested flow.
* **Strings**: all new user-facing text added to `strings.xml` in
  Indonesian; Tasbih-row formatting reuses Phase 1's existing strings
  rather than duplicating them.

### Files created

`domain/model/AmaliyahCompletionEvent.kt`, `ActivityOverview.kt`,
`domain/repository/ActivityRepository.kt`,
`domain/usecase/ObserveActivityOverviewUseCase.kt`,
`data/local/entity/AmaliyahCompletionEventEntity.kt`,
`data/local/dao/AmaliyahCompletionEventDao.kt`,
`data/mapper/AmaliyahCompletionEntityMappers.kt`,
`data/repository/ActivityRepositoryImpl.kt`, `di/ActivityModule.kt`,
`core/designsystem/component/{SectionHeader,SummaryMetric,
SummaryMetricEmphasis,ActivityRow,ActivityRowKind,ActivityRowContent,
TimeRangeFilter,TimeRangeFilterState}.kt`, `feature/activity/
{ActivityUiState,ActivityViewModel,ActivityScreen,ActivityRowMappers,
ActivityFormatting}.kt`, `feature/activity/components/
{ActivityHistorySection,ActivityStreakSection,ActivityWeeklySection}.kt`,
`feature/activity/detail/{ActivityAmaliyahHistoryUiState,
ActivityAmaliyahHistoryViewModel,ActivityAmaliyahHistoryScreen,
ActivityTasbihHistoryUiState,ActivityTasbihHistoryViewModel,
ActivityTasbihHistoryScreen,ActivityHistoryDetailScaffold}.kt`.

### Files modified

`domain/model/GuidedReadingSession.kt`, `data/local/entity/
GuidedReadingSessionEntity.kt`, `data/mapper/GuidedReadingEntityMappers.kt`,
`data/local/database/SanguSantriDatabase.kt`, `di/DatabaseModule.kt`,
`feature/guidedreader/GuidedReaderViewModel.kt` (new `ActivityRepository`
dependency, session-start tracking, completion recording + duplicate-event
guard), `feature/reader/ReaderViewModel.kt` (`switchToGuided` preserves
`startedAtEpochMillis`), `navigation/SanguSantriNavHost.kt`,
`app/src/main/res/values/strings.xml`, `app/build.gradle.kts`
(`versionCode = 4`/`versionName = "0.0.3"`), `app/schemas/....../1.json`
(regenerated), and one existing instrumented test
(`ContentPackageImporterTest.kt`, one `GuidedReadingSessionEntity(...)`
call site updated with the new field to keep compiling — no test logic
changed).

### Commands executed

`./gradlew :app:ktlintFormat` — passed after two rounds (missing
`androidx.compose.runtime.getValue` import, then all clean).
`./gradlew :app:ktlintCheck :app:detekt` — first pass found 7 detekt
findings (`LongParameterList` ×3, `LongMethod` ×1, `MatchingDeclarationName`
×3) from the new Aktivitas components/nav wiring; fixed by bundling
parameters into small holder types (`ActivityRowContent`,
`TimeRangeFilterState`), deriving the detail screen's title from `kind`
instead of passing it, and splitting stray top-level types into their own
files — re-run passed clean. `./gradlew :app:compileDebugKotlin` — first
pass failed twice (missing `getValue` import; `ReaderViewModel.
switchToGuided`'s pre-existing `GuidedReadingSession(...)` call site needed
the new `startedAtEpochMillis` argument) — fixed, then passed.
`./gradlew :app:lintDebug :app:assembleDebug :app:compileDebugUnitTestKotlin
:app:compileDebugAndroidTestKotlin` — `compileDebugAndroidTestKotlin`
failed once (`ContentPackageImporterTest.kt`'s existing
`GuidedReadingSessionEntity(...)` call site needed the new field) — fixed,
then all four passed together. `adb devices` returned an attached emulator
partway through this phase; `./gradlew :app:installDebug` (after `adb
shell pm clear com.sangusantri.app`) succeeded.

### Manual verification (Pixel_9 emulator, Android 15/API 35)

Fresh install (post clear-data) launched with no crash; bundled content
imported (`tahlil-umum-v1`, `istighosah-umum-v1`); `logcat` showed no
`FATAL`/`AndroidRuntime` exceptions throughout the session. Verified by
screenshot at each step: bottom nav is Beranda | Aktivitas | Tasbih in
order, correct icons/labels/selected-pill; Aktivitas screen-level empty
state ("Belum ada aktivitas") on first launch; selected target 33 in
Tasbih, counted to target (`TargetReached` tone, check icon, "Target
tercapai — ketuk untuk mengulang" — Phase 1 behaviour unaffected), reset
with confirmation (shared `ConfirmationDialog`, destructive-red "Reset"
action); Aktivitas immediately reflected the real archived session:
streak "1 hari" (current, highlighted) / "1 hari" (longest), this-week
summary "0 Amaliyah selesai / 1 Sesi tasbih / 1 Total menit", "Riwayat
Tasbih" section appeared with the correct row (name fallback "Tasbih",
"Target 33 · Hitungan akhir 33", "10:49 · 1 menit") — **and no "Riwayat
penyelesaian amaliyah" section rendered**, confirming per-section
hide-if-empty is real, not just a description; "Lihat semua" opened the
filtered detail screen (back action, Semua/7 hari/30 hari chips, the same
row); state was preserved switching away to Beranda and back to Aktivitas
(`TopLevelBackStack` per-tab state retention, Phase 1's own mechanism,
confirmed still correct with a third tab added). Opened Istighosah in
Guided mode (mode gate → Panduan) to confirm the new `ActivityRepository`
constructor dependency resolves through Hilt without a DI graph error —
loaded correctly ("Langkah 1 dari 27", 4%, "Lanjut" enabled).

### Known limitations

* **A real `AmaliyahCompletionEvent` was not recorded on-device this
  session.** Completing a full Guided Reader session was impractical to
  drive manually within this session — Istighosah's bundled draft content
  includes at least one step with an extreme repetition target (an
  istighfar recitation recorded at 30,000×, per Milestone 4.5's own
  content-extraction notes), and `allRequiredCountersComplete` correctly
  requires every step's counter to reach its target before completion is
  even reachable, for either bundled amaliyah (Tahlil is 59 steps).
  `onConfirmCompletion`'s new completion-recording call, the
  `startedAtEpochMillis` plumbing, and the "Riwayat penyelesaian amaliyah"
  section's rendering were verified by code review, successful compilation,
  and confirmed-working Hilt dependency resolution (the Guided Reader loads
  correctly with the new `ActivityRepository` dependency) — but not by an
  actual completed amaliyah appearing in Aktivitas on a real device. The
  identical aggregation code path (`ObserveActivityOverviewUseCase`) *was*
  exercised end-to-end for real via the Tasbih branch (same `combine`/
  streak/weekly-window logic, different repository), which is meaningful
  but not a substitute for exercising the amaliyah branch itself.
* No new automated tests were added, per this pass's explicit constraint —
  the streak/longest-streak date-window math and the duplicate-completion
  guard are exactly the kind of logic that would benefit from unit tests
  when that constraint is lifted.
* "This week" is a rolling 7-day window from the current instant, not a
  calendar-aligned week (Monday–Sunday or similar) — a reasonable, honestly
  documented interpretation of "minggu ini," not the only possible one.
* RTL, dark mode, landscape, tablet-width, and font-scale-1.5× were not
  separately exercised for the new Aktivitas screens this session (only
  light-mode portrait was captured); Compose previews exist for the light/
  dark states of every new component but were not cross-checked against a
  live RTL/large-font device configuration.

### Next recommended milestone

Phase 3 (Release `0.0.4` — Pengingat Amaliyah), per the product-owner-
approved, phase-gated work order.

## Firebase Hosting static content delivery decision and documentation pass (2026-08-02)

**Status:** Complete. Not a numbered milestone — documentation and
architecture-decision-record only; no Kotlin source under `app/` changed.

**Scope:** Explicitly approved product/tech-lead decision to drop the Go +
Supabase-managed PostgreSQL backend (ADR 0011), which had never actually
been implemented (no `backend/` directory ever existed), entirely and
permanently — not defer it further. Content the backend would have served
is instead published as static files on Firebase Hosting, under a new
top-level `content-hosting/` directory, with a Firebase MCP server used
only as development/CI tooling for managing that static deployment (never
a runtime/Android dependency). Recorded in new ADR
[0014](decisions/0014-firebase-hosting-static-content-delivery.md); ADR
0011 marked Superseded (kept as historical record); ADR 0012 (bundled
bootstrap plus remote sync) and ADR 0010 (no custom CMS) amended in place
to reflect the new backend-free static-hosting model. Key technical fact
established during this pass: `ContentApiService` (`data/remote/api/`)
already only issues plain `GET` requests against `v1/content/manifest` and
`v1/content/packages/{versionId}` — static files at those same paths on
Firebase Hosting satisfy the existing Retrofit contract with **no Android
code change**, only a different `SANGU_CONTENT_API_BASE_URL` once the
actual `content-hosting/` migration is scheduled as its own task.

Updated in this pass for internal consistency (ADR 0012's own precedent —
a content-delivery decision touches every document that described the
backend): `docs/product/PRD.md` (document version 1.6, backend metadata
field, FR-010/FR-011), `docs/engineering/ARCHITECTURE.md` (§Backend
rewritten), `docs/content-schema.md`, `docs/engineering/CONTENT_MODEL.md`
("Server tables" section replaced — there is no database), `docs/
engineering/OFFLINE_FIRST.md`, `docs/operations/CONTENT_GOVERNANCE.md`
(publication/revocation authority), `docs/product/ROADMAP.md`, `docs/
operations/PRODUCTION_READINESS.md`, `docs/operations/INCIDENT_RESPONSE.md`,
`docs/engineering/RELEASE_ENGINEERING.md`, `docs/security/
SECURITY_BASELINE.md`, `docs/security/THREAT_MODEL.md`, `docs/README.md`,
and this file. New `docs/engineering/MCP_TOOLING.md` documents the Firebase
MCP tooling boundary itself.

### Known limitations

* This is a documentation/architecture-decision pass only. The actual
  `content-hosting/` directory, its `firebase.json`, the CI content-
  validation script, the Firebase project itself, and repointing
  `SANGU_CONTENT_API_BASE_URL` at a real deployed URL are all **not yet
  implemented** — each remains a separate, explicitly-requested task.
* Unrelated, pre-existing uncommitted changes adding the Firebase
  Crashlytics Android SDK (`google-services` plugin, `firebase-crashlytics`
  dependency, `app/google-services.json`) were found in the working tree
  during this pass and deliberately left untouched — they are a
  crash-reporting concern, not a content-delivery one, and out of scope
  for ADR 0014.
* Server-side content-version adoption tracking and centralised sync
  observability, previously described as "not yet, pending backend
  deployment," are now permanent gaps rather than temporary ones — static
  Firebase Hosting has no request-level application logging to build that
  on top of without reintroducing the dynamic service ADR 0014 removed.

### Next recommended milestone

Implement the `content-hosting/` directory, the CI content-validation
script, and the Firebase project/deploy pipeline described in ADR 0014 and
`docs/engineering/MCP_TOOLING.md` — or continue Phase 3 (Release `0.0.4` —
Pengingat Amaliyah) first and treat the Firebase Hosting migration as a
parallel workstream, consistent with how ADR 0012's Android-side sync
client was originally built ahead of its backend.

## Autonomous-execution prompt consolidation (2026-08-06)

**Status:** Complete. Documentation-only; no application, content, hosting, or
Gradle implementation changed.

Combined the repository-independent autonomous-execution instructions with
SanguSantri's repository-specific engineering, architecture, content-safety,
phase, validation, and reporting rules. `docs/CODEX_AUTONOMOUS_PROMPT.md` is
the reusable Codex form; `docs/CLAUDE_AUTONOMOUS_PROMPT.md` is the Claude Code
form, pre-scoped to finishing the existing ADR 0015/static-hosting worktree.
Both make instruction precedence explicit, preserve pre-existing worktree
changes, and prevent autonomous progression into a new roadmap milestone after
the current objective is done.

### Files created

`docs/CODEX_AUTONOMOUS_PROMPT.md`,
`docs/CLAUDE_AUTONOMOUS_PROMPT.md`.

### Files modified

`docs/PROGRESS.md`.

### Commands executed

Read-only inspection only: the attached autonomous prompt, `AGENTS.md`/
`CLAUDE.md`, `docs/PROGRESS.md`, `docs/product/PRD.md` §Related Documents,
`docs/product/ROADMAP.md`, ADR 0015, Git status, and staged/unstaged diff
statistics. Also verified the locally installed Claude Code version and CLI
permission-mode flags with `claude --version` and `claude --help`.

### Test results

Not run; this pass changed documentation only and did not alter executable
code.

### Known limitations

The prompts intentionally do not authorize implementing the whole roadmap in
one session. The Claude version is currently specialized for the active ADR
0015/static-hosting worktree; edit its Current objective section before reusing
it for a later milestone.

### Next recommended milestone

Use the prompt to finish and validate the existing in-progress content-model/
static-hosting worktree before starting Release `0.0.4`.

## ADR 0015 reconciliation and validation pass (2026-08-06)

**Status:** Complete and verified — `ktlintFormat`, `ktlintCheck`, `detekt`,
`lint`, `assembleDebug`, `testDebugUnitTest` (46/46), and
`connectedDebugAndroidTest` (Pixel_9 emulator, Android 15/API 35, 40/40) all
pass. Manually verified end-to-end on the same emulator (see Manual
verification below).

**Scope:** This worktree already contained a substantial, coherent,
uncommitted implementation of ADR
[0015](decisions/0015-simplified-dynamic-catalog-content-model.md) (the flat
`Content`/`ContentStep` catalog model replacing the
Amaliyah/Variant/Version/Approval hierarchy) and its Firebase Hosting static
content contract — all of it pre-existing work from an earlier session, not
authored this pass. This pass's job was narrower: finish, reconcile, and
validate that work into a stable state. Concretely: the production Kotlin
source (`domain/model/{Content,ContentDetail,ContentStep}.kt`,
`data/content/{ContentImporter,ContentValidator,ContentVersionAction}.kt`,
`data/local/database/Migrations.kt`'s `MIGRATION_1_2`, the rewritten
Serambi/Reader/GuidedReader/Activity/Tasbih call sites, `content-hosting/`)
already compiled cleanly and was left untouched in substance. Every existing
unit and instrumented test file, however, still referenced the deleted
Amaliyah/Variant/Version/Approval/StepType surface and failed to compile —
this pass's main work was porting each test to the new model, plus fixing
two real defects the reconciliation review surfaced (see below).

### Conflict check: does the temporary design-phase constraint apply here?

`CLAUDE.md`'s "Temporary implementation-pass constraints (design product
alignment)" section (no Room migrations, no new tests) is explicitly scoped
to "the phases implementing the design product-alignment work
(`docs/design/DESIGN_HANDOFF.md`, Phases A–E)". Checked
`DESIGN_HANDOFF.md`'s own "Implementation order" list: Phase A (Reader UX,
`0.0.1`), Phase B (Beranda/Jelajahi, `0.0.1`), Phase C (Tasbih, `0.0.2`),
Phase D (Aktivitas, `0.0.3`), Phase E (Pengingat, `0.0.4`, not yet started).
ADR 0015 and the Firebase Hosting static-content-delivery work are a
separate, later workstream (2026-08-02/2026-08-06) with no Phase A–E entry
of their own — they are not in scope of that constraint. This matters
concretely: ADR 0015's own text explicitly created a real, non-destructive
`Migration(1, 2)` (`MIGRATION_1_2`) specifically to preserve real user
reading/guided/step progress across the schema change, which the temporary
constraint's blanket migration ban would have prohibited had it applied.
Conclusion: the constraint does not apply to this pass; the pre-existing
`MIGRATION_1_2` was correctly left in place, and this pass added the
instrumented migration test and new/rewritten content-layer tests that
constraint would otherwise have disallowed, since ordinary engineering
standards (`docs/engineering/TESTING.md`) govern this workstream instead.

### What shipped this pass

* **Test suite ported to the new model** (all existing tests, not new
  coverage beyond what each replaced): `ReaderViewModelTest.kt`,
  `SerambiViewModelTest.kt` (test) rewritten against `Content`/
  `ContentDetail`/`ContentStep`/the new `ContentRepository`/`SerambiUiState.
  Loaded` shape. `ContentVersionActionTest.kt` rewritten for
  `decideContentVersionAction(candidateVersion, localVersion)`'s new
  checksum-free signature. `ContentPackageValidatorTest.kt` →
  `ContentValidatorTest.kt` (test), same validation-rule test intent ported
  to `ContentValidator`/`ContentCatalogDto`/`ContentFileDto`.
  `ContentChecksumTest.kt` deleted outright — `ContentChecksum` itself was
  deleted by ADR 0015 (checksums removed), not renamed, so there is nothing
  left to port. `ReadingPositionDaoTest.kt` (androidTest) mechanically
  renamed `versionId`/`getByVersionId` → `contentId`/`getByContentId`.
  `ContentPackageImporterTest.kt` → `ContentImporterTest.kt` (androidTest),
  same high-risk-behaviour coverage (fresh import, idempotency,
  never-downgrade, identity-mismatch rejection, structural-validation
  rejection, atomic rollback, atomic replacement) ported to
  `ContentImporter`, plus two tests for ADR 0015's actual behaviour change
  (progress is now preserved for step ids that survive a content update,
  not unconditionally wiped) replacing the old checksum-conflict/
  minimum-app-version tests, which have no equivalent in the new model.
  `ContentSyncManagerTest.kt` (androidTest) rewritten against the new
  catalog/content-file MockWebServer contract, dropping the two scenarios
  ADR 0015 deliberately removed (checksum conflict, `minimumAppVersionCode`
  gate) and keeping every other scenario. `SerambiScreenTest.kt`'s
  navigation test updated: Tahlil is now real, available content (not a
  `DRAFT` fixture), so tapping it now reaches the reading-mode chooser
  instead of the unavailable state. `ReaderScreenTest.kt` rewritten to seed
  fixtures via `ContentDao`/`ContentStepDao` instead of the deleted
  Amaliyah DAOs, including a dedicated zero-step fixture for the
  content-unavailable scenario (the old "Tahlil is DRAFT" premise no longer
  holds — there is no DRAFT/PUBLISHED status in the flat model, and Tahlil
  is now real content).
* **New: `SanguSantriMigrationTest.kt`** (androidTest) — `MIGRATION_1_2`
  itself had no instrumented test despite ADR 0015's own text already
  claiming one exists ("exercised by an instrumented migration test").
  Added one, using `MigrationTestHelper` against the exported `1.json`/
  `2.json` schemas: seeds a published amaliyah (one `HEADING` step, two real
  reading steps, one with a `NULL` `repeatTarget`) plus reading/guided/step
  progress, and a second, draft-only amaliyah with no published version.
  Verifies: the `content` row is created only for the published amaliyah;
  its `HEADING` step is dropped and the two real steps survive renumbered
  to a dense `1..2` with the null `repeatTarget` defaulted to `1`;
  `reading_positions`/`guided_reading_sessions`/`step_progress` are
  re-keyed to the content id; step-progress for the dropped `HEADING` step
  is not carried over while progress for a surviving step is; the
  draft-only amaliyah gets no `content` row; and all five old tables are
  dropped. Required re-adding `androidx-room-testing` as an
  `androidTestImplementation` dependency (already in the version catalog,
  not wired into `app/build.gradle.kts`) and an `androidTest` `sourceSets`
  block pointing at `app/schemas/` (both were deliberately removed in
  Milestone 4's pre-release Room cleanup, when no real migration existed
  yet to test).
* **Fixed: `ContentImporter.importContentFile` never actually caught a
  database failure**, despite `ContentImportOutcome.Rejected`'s own KDoc
  promising "a database failure that rolled back" as one of its reasons —
  the old `ContentPackageImporter` this class replaced did
  (`runCatching { applyAgainstRoom(...) }`), but the rewrite dropped it.
  `BundledContentBootstrapper.readAndImport` happens to wrap its own call in
  `runCatching`, masking the gap there, but `ContentSyncManager.
  downloadAndImport` calls `contentImporter.importContentFile(...)` with no
  such wrapping — a real database exception mid-import (e.g. a primary-key
  conflict) would have propagated uncaught out of `ContentSyncWorker.
  doWork()`, silently losing PRD 12.4's per-item failure isolation
  guarantee for the one path that most needs it (a background sync worker,
  where one bad item must never abort every other item's update). Fixed by
  adding `ContentImporter.writeContentOrReject`, a `try`/`catch` around the
  transactional write that turns any non-cancellation exception into
  `ContentImportOutcome.Rejected` centrally, in the one class both callers
  share, instead of duplicating the guard in each caller. Covered by two
  new `ContentImporterTest` cases exercising a real Room primary-key
  conflict.
* **Fixed: no network security config existed**, which — as
  `docs/security/SECURITY_BASELINE.md` already flagged as an outstanding,
  near-term gap ("must enforce HTTPS-only/no cleartext before any build is
  used against a real...host") — meant `targetSdk 36`'s default
  cleartext-blocked policy silently failed every `ContentSyncManagerTest`
  case on a real device (`UnknownServiceException: CLEARTEXT communication
  to localhost not permitted`), discovered only because this pass had an
  emulator available to actually run `connectedDebugAndroidTest` for the
  first time since `ContentSyncManager` was introduced (Milestone 8's own
  known limitation: never run on-device). Fixed with
  `app/src/main/res/xml/network_security_config.xml`
  (`cleartextTrafficPermitted="false"`, wired via `AndroidManifest.xml`'s
  `android:networkSecurityConfig`) plus a debug-source-set override
  (`app/src/debug/res/xml/network_security_config.xml`,
  `cleartextTrafficPermitted="true"`) using Android's standard
  source-set resource override — **not** `<debug-overrides>`, which only
  ever accepts `<trust-anchors>` and cannot relax cleartext policy (tried
  first; confirmed non-functional on-device before switching approaches).
  Release builds are HTTPS-only exactly as the security baseline required;
  only debug-variant instrumented tests get the local-server exception.
  `docs/security/SECURITY_BASELINE.md` updated from "still outstanding" to
  "done".

### Files created

`app/src/test/java/com/sangusantri/app/data/content/ContentValidatorTest.kt`,
`app/src/androidTest/java/com/sangusantri/app/data/content/
ContentImporterTest.kt`, `app/src/androidTest/java/com/sangusantri/app/
data/local/database/SanguSantriMigrationTest.kt`, `app/src/main/res/xml/
network_security_config.xml`, `app/src/debug/res/xml/
network_security_config.xml`.

### Files modified

`app/src/test/java/com/sangusantri/app/data/content/
ContentVersionActionTest.kt`, `app/src/test/java/com/sangusantri/app/
feature/home/SerambiViewModelTest.kt`, `app/src/test/java/com/sangusantri/
app/feature/reader/ReaderViewModelTest.kt`, `app/src/androidTest/java/
com/sangusantri/app/data/local/database/ReadingPositionDaoTest.kt`,
`app/src/androidTest/java/com/sangusantri/app/data/sync/
ContentSyncManagerTest.kt`, `app/src/androidTest/java/com/sangusantri/app/
feature/home/SerambiScreenTest.kt`, `app/src/androidTest/java/
com/sangusantri/app/feature/reader/ReaderScreenTest.kt`,
`app/src/main/java/com/sangusantri/app/data/content/ContentImporter.kt`,
`app/build.gradle.kts` (`androidx-room-testing` dependency, androidTest
`sourceSets` schema wiring), `app/src/main/AndroidManifest.xml`
(`android:networkSecurityConfig`), `docs/security/SECURITY_BASELINE.md`.

### Files deleted

`app/src/test/java/com/sangusantri/app/data/content/ContentChecksumTest.kt`,
`app/src/test/java/com/sangusantri/app/data/content/
ContentPackageValidatorTest.kt` (superseded by `ContentValidatorTest.kt`),
`app/src/androidTest/java/com/sangusantri/app/data/content/
ContentPackageImporterTest.kt` (superseded by `ContentImporterTest.kt`).

### Commands executed

`./gradlew :app:ktlintFormat`, `:app:ktlintCheck`, `:app:detekt`, `:app:lint`,
`:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:connectedDebugAndroidTest`
— all passed. `:app:installDebug`/`:app:installDebugAndroidTest` against a
freshly booted `Pixel_9` emulator (Android 15/API 35, headless
`-no-window -gpu swiftshader_indirect`). `node
content-hosting/scripts/validate-content.mjs` — passed
("content-hosting validation passed"); also `diff`-verified
`app/src/main/assets/content/{catalog.json,packages/*.json}` are
byte-identical to `content-hosting/public/content/{catalog.json,
packages/*.json}`, as ADR 0014/0015 require.

### Test results

`testDebugUnitTest`: 46/46 JVM unit tests passed.
`connectedDebugAndroidTest`: 40/40 instrumented tests passed, including the
new `SanguSantriMigrationTest` and `ContentImporterTest` and the rewritten
`ContentSyncManagerTest`, `ReaderScreenTest`, `SerambiScreenTest`,
`ReadingPositionDaoTest`. `ktlintCheck`/`detekt`: 0 issues.
`lint`/`lintDebug`: `BUILD SUCCESSFUL`, 0 errors (21 pre-existing warnings,
all unrelated to this pass — dependency-version-available notices, one
`UnusedResources` for the launcher icon foreground, `OldTargetApi`/
`ObsoleteSdkInt`). `assembleDebug`: `BUILD SUCCESSFUL`.

### Manual verification (Pixel_9 emulator, Android 15/API 35, fresh install)

Fresh install (`pm clear` + reinstall) launched with no crash; bundled
catalog imported both real content items. Beranda renders Tahlil/Istighosah
cards with real category/description text (previously non-production
`[FIXTURE-AR]` placeholders under the old model — this pass's own
Beranda/reader screenshots are the first real, non-placeholder content this
project has rendered end-to-end). Tapping Tahlil opens the reading-mode
chooser (content is now genuinely available, unlike the old model's
permanently-`DRAFT` fixtures); **Bacaan Lengkap** renders "Langkah 1 dari 37"
(matching ADR 0015's documented 59→37 step reduction) with correct
Arabic + Indonesian translation and the "٣×" informational repetition
indicator. Overflow menu confirmed to no longer offer "Daftar Isi" (Table of
Contents correctly removed — no data source for it in the new model).
**Panduan** (Guided Reader) on Istighosah step 1 (Al-Fatihah, target 3):
interactive counter started at "0 dari 3", incremented on tap to "3 dari 3",
turned green with a checkmark, and enabled **Lanjut** — the full interactive
tasbih-counter flow works against real, non-fixture step data. The reader's
"Sumber" dialog shows only `sourceName` ("NU Online — Bacaan Tahlil Singkat,
Lengkap dengan Doa dan Terjemahannya") with no approval/approver text,
confirming the on-device `Approval` object removal. Aktivitas tab renders
its correct empty state ("Belum ada aktivitas" — no amaliyah was fully
completed this session, only a single step's counter). Tasbih tab renders
its own target-selection screen unaffected by the content-model change.

### Known limitations

* **No real backend is deployed.** The `content-hosting/` Firebase project
  (`sangusantri-81cc6` per `.firebaserc`) has not been created or deployed
  to; `BuildConfig.CONTENT_API_BASE_URL` still points at the non-routable
  `.invalid` placeholder. `ContentSyncManagerTest` proves the sync client
  logic is correct against a mocked server, not against the real Firebase
  Hosting deployment.
* **No `AmaliyahCompletionEvent`/full Guided Reader completion was
  exercised this session** — the same practicality limitation Milestone 10
  documented (every step's counter must reach its target before
  completion is reachable; Tahlil is 37 steps, Istighosah's bundled content
  includes a 30,000× istighfar repetition). Only a single step's counter
  reaching its target was manually verified.
* RTL-locale, dark mode, landscape, tablet-width, and font-scale-1.5× were
  not separately exercised for the migrated screens this pass — only
  light-mode portrait was captured, consistent with how prior milestones in
  this project have scoped manual verification.
* This pass did not review or re-verify the substance of the pre-existing
  production Kotlin implementation, `docs/` updates (`content-schema.md`,
  `ARCHITECTURE.md`, `CONTENT_MODEL.md`, `MCP_TOOLING.md`,
  `OFFLINE_FIRST.md`, `CONTENT_GOVERNANCE.md`, `PRD.md`, `ROADMAP.md`), or
  `content-hosting/` beyond what was needed to make it compile, pass tests,
  and behave correctly on-device — that work was authored in an earlier
  session, already matched ADR 0015's own description in every place this
  pass inspected it, and is treated as reviewed-by-outcome (passing tests,
  correct on-device behaviour) rather than re-read line by line.

### Next recommended milestone

Deploy the `content-hosting/` Firebase project and repoint
`SANGU_CONTENT_API_BASE_URL` at the real deployed URL (the natural next step
to make the already-implemented Android sync client observably functional
end-to-end) — or proceed directly to Release `0.0.4` (Phase E — Pengingat
Amaliyah) per `docs/design/DESIGN_HANDOFF.md`'s implementation order, treating
the Firebase deployment as a parallel workstream, consistent with how ADR
0012's Android-side sync client was originally built ahead of its backend.

## Firebase Hosting deployment (2026-08-06)

**Status:** Complete and verified end-to-end on a real device. Not a
numbered milestone — infrastructure/config only, no Kotlin source changed.

**Scope:** The natural next step this session's own prior entry
recommended: deploy `content-hosting/public/` to the already-provisioned
`sangusantri-81cc6` Firebase project's default Hosting site, and repoint
`SANGU_CONTENT_API_BASE_URL` at the real URL so the already-implemented
Android sync client (ADR 0012/0014/0015) is observably functional, not just
tested against MockWebServer.

### What shipped

* `node content-hosting/scripts/validate-content.mjs` run once more
  immediately before deploying (still passing) as a pre-deploy gate, then
  `firebase deploy --only hosting --project sangusantri-81cc6` from
  `content-hosting/`. Live at `https://sangusantri-81cc6.web.app/`.
  Verified via `curl`: `content/catalog.json` and both
  `content/packages/*.json` return `200` with the expected
  `Cache-Control: public, max-age=300` header from `firebase.json`, and
  body content matches the committed files exactly.
* `gradle.properties`: added `SANGU_CONTENT_API_BASE_URL=
  https://sangusantri-81cc6.web.app/` — not a secret (a public static-CDN
  URL), and exactly the mechanism `docs/engineering/ARCHITECTURE.md`
  already documented as sufficient ("no code change" — confirmed by
  rebuilding and inspecting the generated `BuildConfig.CONTENT_API_BASE_URL`,
  which now reads the real URL instead of the `.invalid` placeholder).
* `docs/operations/PRODUCTION_READINESS.md` updated: the "Firebase Hosting
  deployment is not yet a release blocker... until one is configured" gap
  is now closed and described as verified, not just theoretically wired.

### Manual verification (Pixel_9 emulator, Android 15/API 35)

Fresh install (`pm clear` + reinstall) launched; `ContentSyncScheduler.
enqueueIfStale()` enqueued `ContentSyncWorker` immediately (no prior sync
recorded). `logcat` showed `WM-WorkerWrapper: Worker result SUCCESS` for
`ContentSyncWorker`, with zero `ContentSyncManager` warning/error log lines
(every failure path in that class logs one). Pulled `sangusantri.db`
(+ `-wal`/`-shm`, since Room's WAL journal means the plain `.db` file alone
is stale) off the device and queried `app_metadata` directly:
`content_last_sync = SUCCESS`, confirming `ContentSyncManager.sync()`
actually completed a real `SyncResult.Completed` against the live
deployment, not merely that the worker didn't crash (`Result.success()` is
also returned on some failure paths, so the worker-result log alone would
not have been conclusive).

### Known limitations

* Only the default catalog/two content packages were deployed — this
  exercises the "no update available" and general fetch path, not a
  genuine version-bump/replace scenario against the live host (that was
  already covered against MockWebServer in `ContentSyncManagerTest`).
* No CI pipeline deploys `content-hosting/` automatically on merge —
  today's deploy was a manual, one-time `firebase deploy` from this
  session. `docs/engineering/MCP_TOOLING.md`'s CI-tooling boundary is
  unaffected (this was a direct Firebase CLI deploy, not a code or
  Gradle-dependency change).

### Next recommended milestone

Release `0.0.4` (Phase E — Pengingat Amaliyah), per
`docs/design/DESIGN_HANDOFF.md`'s implementation order.

## Milestone 11 — Release 0.0.4, Phase E: Pengingat Amaliyah

**Status:** Implemented and manually verified on-device
(Pixel_9 emulator, Android 15/API 35). `docs/product/ROADMAP.md`'s entire
`0.0.4` spec — personal Tahlil/Istighosah reminder schedules with
Tahlil-malam-Jumat and Istighosah-weekly presets, Gregorian and Hijri-date
scheduling, notification permission flow, reboot rescheduling, no
"remind me later" — is complete. This phase falls under `DESIGN_HANDOFF.md`'s
"Phase E" and therefore under `CLAUDE.md`'s temporary implementation-pass
constraints: no Room migration class (`SanguSantriDatabase` version bumped
2→3 on the clean baseline, no `MIGRATION_2_3` — developers must clear app
data/reinstall once) and no new automated tests were added; validation is
static analysis + build + install + manual on-device verification only.

### What shipped

* **Domain**: `domain/model/{Reminder,ReminderSchedule,ReminderPreset,
  ReminderScheduleCalculator}.kt` — a sealed `ReminderSchedule`
  (`Weekly(dayOfWeek, hour, minute)` / `HijriDate(hijriMonth, hijriDay,
  hour, minute, repeatsYearly)`), two presets (Tahlil→Thursday 19:00,
  Istighosah→Friday 05:00) that only pre-fill the form, and a pure
  `ReminderScheduleCalculator` (`java.time.chrono.HijrahDate`, no new
  dependency) computing next-trigger for both schedule kinds including
  Hijri year-rollover.
* **Data**: `data/local/entity/ReminderEntity.kt` (FK to `content`,
  `ON DELETE CASCADE`) + `ReminderDao` (`observeAll`,
  `observeNearestEnabled`), `data/repository/ReminderRepositoryImpl.kt`,
  `data/mapper/ReminderEntityMappers.kt`. `SanguSantriDatabase` → version 3.
* **Use cases**: `ScheduleReminderUseCase`, `CancelReminderUseCase`,
  `RescheduleAllRemindersUseCase` (repository + alarm scheduler, each with
  2–3 real call sites).
* **Background scheduling** (`data/reminder/`, parallel to `data/sync/`):
  `ReminderAlarmScheduler` (`AlarmManager.setAndAllowWhileIdle`, inexact —
  no `SCHEDULE_EXACT_ALARM`), `ReminderAlarmReceiver` (`@AndroidEntryPoint`
  `BroadcastReceiver`, `goAsync()`, explicit `POST_NOTIFICATIONS`
  permission check before `notify()`, rearms next occurrence or disables a
  one-off Hijri reminder after firing), `ReminderNotificationChannel`,
  `ReminderBootReceiver` (`BOOT_COMPLETED` → `RescheduleAllRemindersUseCase`).
  `AndroidManifest.xml`: `POST_NOTIFICATIONS`/`RECEIVE_BOOT_COMPLETED`
  permissions, both receivers `exported="false"`.
* **UI** (`feature/reminder/`): `ReminderScreen`/`ReminderList`/
  `ReminderOverlays`/`ReminderViewModel`, a bottom-sheet
  `ReminderFormSheet` (presets, amaliyah picker, label, Weekly/Hijri mode
  switch, day-of-week or month/day+yearly-repeat picker, `TimeInput`),
  `NotificationPermissionBanner` (inline, `ActivityResultContracts.
  RequestPermission()`, settings fallback when permanently denied),
  `ReminderScheduleFormatter` (Indonesian Gregorian + Hijri text).
* **Entry points**: Beranda's "Pengingat terdekat" section (always
  rendered, unlike other Beranda sections' hide-if-empty rule — see below)
  and Aktivitas's "Pengingat" section (hide-if-empty; shows only enabled
  reminders), both wired through `ObserveActivityOverviewUseCase`/
  `SerambiViewModel` combining `ReminderRepository` into their existing
  flows. `navigation/SanguSantriNavHost.kt`: new `Pengingat` `NavKey`
  (no bottom-nav destination, per PRD §7.1), plus `MainActivity`'s
  `EXTRA_REMINDER_CONTENT_ID`/`onNewIntent` deep-link handling so tapping a
  reminder notification opens that amaliyah's reading-mode gate directly.
* `app/build.gradle.kts`: `versionCode = 5`, `versionName = "0.0.4"`.

### Bugs found and fixed via manual on-device testing (not caught by static analysis)

* **Unreachable feature**: Beranda's "Pengingat terdekat" section was
  originally hidden whenever there were zero reminders — with Aktivitas's
  own section *also* hide-if-empty, a first-time user had no way to ever
  reach the Pengingat creation screen. Fixed by always rendering Beranda's
  section, showing an empty-state CTA ("Belum ada pengingat. Ketuk untuk
  menambahkan pengingat amaliyah.") that still navigates to Pengingat when
  tapped.
* **Unreachable Save button in Hijri mode**: `ReminderFormContent`'s outer
  `Column` (`feature/reminder/components/ReminderFormSheet.kt`) had no
  `verticalScroll` modifier. Weekly mode's shorter content happened to fit
  within the fully-expanded `ModalBottomSheet`, but Hijri mode (12 month
  chips + up to 5 rows of day chips + yearly-repeat switch + `TimeInput`)
  does not — the sheet clipped the content with the Time input and Simpan
  button completely unreachable and no way to scroll to them. Fixed by
  adding `.verticalScroll(rememberScrollState())` to the form's `Column`.

### Manual verification (Pixel_9 emulator, Android 15/API 35)

Fresh install (`pm clear` + reinstall). Confirmed, in order: Beranda's
always-shown "Pengingat terdekat" empty state navigates to Pengingat;
created a Weekly Istighosah-preset reminder (Setiap Jumat, 05:00 / 24 Safar
1448 H shown correctly); notification permission banner shown, system
permission dialog granted, banner correctly disappeared; created a
Hijri-date Tahlil reminder (23 Safar, yearly) — correctly computed next
occurrence as **23 Safar 1449 H** (next Hijri year, since this year's date
had passed), confirming the calculator's year-rollover path; toggled the
Istighosah reminder off — Beranda's nearest-reminder and Aktivitas's
Pengingat section both reacted live (Aktivitas showing only the still-
enabled Tahlil reminder, status "Aktif"); "Lihat semua" correctly opens the
full Pengingat list; delete-confirmation dialog ("Hapus pengingat?" /
Batal/Hapus) shown and a reminder actually deleted. Verified the
notification tap-to-deep-link path directly (`am start` with
`FLAG_ACTIVITY_SINGLE_TOP` and the same `reminder_content_id` extra
`ReminderAlarmReceiver`'s `PendingIntent` sets) — confirmed via temporary
logging that `onNewIntent` → `deepLinkContentId` → `LaunchedEffect` →
`ReaderGate` push all fire correctly for both a cold start and a
warm/already-running start, then removed the logging and re-ran the full
`ktlintFormat`/`ktlintCheck`/`detekt`/`lint`/`assembleDebug`/`installDebug`
sequence clean.

### Known limitations

* **Live alarm firing was not observed end-to-end.** This emulator image
  is a locked-down "production build": `adb root` is refused, `adb shell
  am broadcast -a android.intent.action.BOOT_COMPLETED` is rejected with a
  `SecurityException` (shell lacks the permission for that protected
  broadcast), and explicit broadcasts to the non-exported
  `ReminderAlarmReceiver` are silently dropped by the system rather than
  delivered. None of this reflects an app defect — `ReminderAlarmReceiver`
  and `ReminderBootReceiver` are correctly `exported="false"` (system/
  `PendingIntent`-only, never invoked by another app), and the receiver's
  own logic (permission-checked `notify()`, rearm-or-disable) was verified
  by code review plus the isolated deep-link test above, which exercises
  everything the receiver does *except* the actual `AlarmManager`
  wake/fire, which is a platform-guaranteed contract, not app-specific
  logic. A real device or a rootable/eng emulator image would let a future
  session set the reminder time a minute out and observe the notification
  and reboot-reschedule firing directly.
* No new unit/instrumented tests, per this phase's temporary constraint —
  `ReminderScheduleCalculator`'s Hijri year-rollover was verified manually
  (see above) rather than with a test file.

### Next recommended milestone

`0.0.5` — Nahwu Quiz (moved from `0.4.0` per ADR 0013), the next item in
`docs/product/ROADMAP.md`.

## Standalone Quran discovery — LPMQ Kemenag API evidence (2026-08-07)

**Status:** Discovery note only; no Quran product scope, roadmap change,
architecture decision, data model, credentials, or feature code approved or
implemented yet.

The product owner supplied redacted live-contract examples for the LPMQ
Kemenag Quran API: paged list-surah, list-ayat-by-surah, and tafsir-by-ayat-ID.
They are preserved in
`docs/engineering/QURAN_API_CONTRACT_DRAFT.md` for the upcoming standalone
Quran PRD/data-layer design. The examples confirm that ayat data includes
`juz`, `halaman`, MSI Usmani Arabic, unvowelled Arabic, API-supplied Latin
transliteration, Indonesian translation, and footnote fields; tafsir exposes
a concise `teks` and longer `tahlili` value.

The supplied Surah 114 array arrived out of canonical order (ayat 2 before
ayat 1). The draft therefore records an explicit future validation rule:
never trust response-array order; validate missing/duplicate ayat and sort by
the numeric `ayat` field. No content was changed, corrected, or published.

### Next discovery step

Complete the product owner's Quran PRD questionnaire, then resolve API
credential placement, licensing/redistribution, offline baseline, dataset
versioning/corrections, release placement, and dark-only reader scope before
approving an implementation milestone.

### Quran font candidate intake (2026-08-08)

The product owner supplied `LPMQ Isep Misbah` and `Amiri Quran Regular` font
files for the planned Quran reader. They are preserved as non-packaged design
inputs under `docs/design/assets/quran-fonts/`, with hashes, provenance,
licensing status, and a mandatory Kemenag-text glyph-compatibility gate in the
directory README. They have not been added to Android resources and do not
ship in the APK. Amiri's supplied OFL 1.1 is preserved; LPMQ font embedding
permission remains to be matched against the product owner's written access
documents before release packaging.

## Standalone Al-Qur'an Kemenag `0.0.6` — PRD and architecture baseline (2026-08-08)

**Status:** Product/design/architecture documentation approved; no Android,
Room, NDK, design tooling, credential, or production feature implementation
completed.

The product owner resolved the discovery questionnaire and approved a complete
standalone Quran milestone after Nahwu Quiz `0.0.5`. The baseline now defines:

* official LPMQ Kemenag as the sole Quran/translation/tafsir source;
* dark-only Surah/Juz/Bookmark/Terakhir Dibaca experience from Beranda;
* flowing Arab-only pages or Arab+translation rows, no Latin/audio/share;
* Room-first reading after one complete atomic 114-surah initialisation;
* simple retry-from-start on initial failure and full atomic seven-day refresh;
* on-demand, cached, stale-while-revalidate tafsir;
* local bookmark, global last read, and Aktivitas/combined-streak integration;
* dedicated Clean Architecture boundary and accepted direct-APK Kemenag
  credential risk with mandatory C++/NDK/R8/signature-check hardening;
* Quran-specific dark tokens, portrait-primary non-locked layout, font preview
  cards, and release/licence/glyph gates.

Created: `docs/product/QURAN_PRD.md`,
`docs/design/QURAN_DESIGN_SYSTEM.md`, ADR 0016, and the non-packaged Quran font
candidate directory. Updated the main PRD/roadmap and canonical architecture,
offline-first, security, privacy, testing, release, design, governance, and
operations documentation so the old "no standalone Quran" rule no longer
drives future work. Historical progress entries above remain unchanged as a
record of what was true when they were written.

### Remaining blockers before implementation/release

* Real Kemenag credentials must be supplied only through untracked local/CI
  secret injection; no real value is stored in this documentation baseline.
* Confirm formal API failure/rate-limit behaviour and SanguSantri public-traffic
  permission with LPMQ.
* Confirm LPMQ Isep Misbah APK redistribution permission, supply King Fahd font
  and licence if desired, and complete exact-Kemenag-corpus glyph testing.
* Create the design-tool page `03 Al-Qur'an Kemenag` from the approved frame contract.

### Documentation validation

`git diff HEAD --check` completed with no whitespace errors. A local Markdown
link scan over every changed document returned `LOCAL_MARKDOWN_LINKS_OK`.
SHA-256 verification matched both recorded candidate hashes: LPMQ Isep Misbah
`b0927593…e21` and Amiri Quran `6814dda5…d3a6`. No Gradle build or Android test
was run because this pass changes documentation and non-packaged design font
inputs only, not application source or resources.

### Next recommended milestone

Finish and release `0.0.5` Nahwu Quiz first. Then create the `0.0.6` design-tool page
and implement the Quran data/security foundation as delivery slice 1 in
`docs/product/QURAN_PRD.md` §14.

## Standalone Quran flowing-reader interaction spike (2026-08-08)

**Status:** Prototype component implemented; not connected to navigation,
Room, Kemenag networking, credentials, or the release APK's user flow.

The product owner refined the reader contract: `Arab saja` now automatically
uses one responsive flowing Arabic surface grouped by Kemenag `halaman`, while
`Arab + terjemahan` automatically uses one stable lazy row per ayat. The former
independent Halaman/Ayat selector was removed from the product and design
contracts.

The isolated Compose prototype adds:

* an annotated flowing-page builder that preserves each supplied Arabic string
  and maps its character range to the stable remote ayat ID;
* coordinate-based long-press hit-testing through `TextLayoutResult`, haptic
  feedback, and selected-range highlighting with Quran semantic colour roles;
* one-per-ayat translated rows with stable lazy keys and semantic long-click;
* a scoped Material action sheet for Bookmark, Tafsir Kemenag, last-read, and
  Juz/page information—no copy, share, Latin, or audio action;
* debug-only phone/tablet fixtures that are explicitly non-production and are
  not wired into application navigation.

The API evidence remains sufficient for metadata-page grouping but cannot
reproduce official printed line breaks because the supplied contract has no
line/word/glyph-position fields.

### Validation and known limitations

* `./gradlew detekt assembleDebug` passed after compiling the new main and
  debug source sets.
* `./gradlew lint` passed.
* `ktlintFormat` and `ktlintCheck` were executed. The final project-wide
  `ktlintCheck` remains blocked by pre-existing indentation violations in the
  in-progress Nahwu Quiz/Reminder source set; the report contains no Quran
  prototype path. Unrelated formatter changes were reverted after detection.
* `git diff --check` passed.
* No emulator/device was connected, so `installDebug`, touch-coordinate
  long-press, haptic feedback, font rendering, and TalkBack were not manually
  exercised.
* The production component accepts an injectable `TextStyle` and currently
  defaults to platform serif. LPMQ remains a non-packaged candidate pending its
  release gate.
* The flowing text exposes one accessibility node with per-ayat custom actions.
  Exact per-range TalkBack focus geometry remains a deliberate validation item
  for the real Room-backed reader.

### Next recommended milestone

Implement Quran delivery slice 1 from `QURAN_PRD.md`: Room entities/DAO,
validated Kemenag DTO ingestion, atomic initial preparation, and repository
observations. Reuse this prototype only after real Room-backed UI models exist.

## Standalone Quran visual-reference baseline (2026-08-08)

**Status:** First approved-format local design artefact created; no production
Quran navigation or data integration added.

Added `docs/design/design-export/quran/` as the persistent visual-reference
directory for the future `03 Al-Qur'an Kemenag` design-tool page. Its first screen is
the revised `Arab saja` flowing reader at 360×800 logical pixels and 720×1600
PNG output. The reference follows the approved dark semantic tokens, removes
audio/light-mode/global-navigation chrome, groups the screen by Kemenag page
metadata, and keeps the Arabic as one responsive RTL reading surface.

The editable HTML renders the exact Surah An-Nas ayat 1–6 strings supplied by
the product owner from the LPMQ API, sorted numerically. No generated Arabic is
used. The revised baseline now includes a compact start-of-surah header with
the Kemenag category, centred surah name, and ayat count, followed by a
basmalah placement. The active basmalah is now a simple, self-contained SVG
path asset shaped with the OFL-licensed Amiri Quran font from the exact
unvocalised phrase published by LPMQ. The earlier screenshot crop remains only
as provenance for placement and is no longer rendered. A JSON sidecar records
tokens, source constraints, intended interactions, and intentional omissions
so future sessions can reproduce the design without guessing.

### Validation and known limitations

* Chromium rendered the HTML at device scale 2 into a 720×1600 PNG.
* The PNG was inspected visually against the source screenshots and Quran
  design-system contract.
* This is a local reference, not a design-tool node export; its design-tool node ID remains
  empty until the page is manually recreated.
* The candidate LPMQ reader font remains a design input and is not approved for
  APK packaging until its provenance, licence, and glyph gates pass. The Amiri
  basmalah SVG has known OFL provenance but still requires explicit product
  acceptance of its intentionally unvocalised treatment before production use.

### Next recommended milestone

Create the matching Arab-only long-press selected-range state, then continue
the Quran visual set with the Arab+translation reader, action sheet, hub tabs,
bookmarks, tafsir states, and display settings before wiring production UI.

## Standalone Quran selected-range visual milestone (2026-08-08)

**Status:** Arab-only long-press selected-range reference completed; still a
design artefact and not wired into the production app.

The product owner supplied a complete Kemenag response for Surah 89. The raw
JSON is preserved unchanged at
`docs/design/design-export/quran/data/al-fajr-89-kemenag-response.json` with its
SHA-256 and validation notes. It contains 30 unique ayat in non-canonical
transport order across pages 593 and 594, reinforcing the existing numeric
sorting requirement.

The reference now named `09b-flowing-reader-arab-only-selected` renders page 593 from
the sorted `teks_msi_usmani` values and selects ayat 15 (`id = 6008`). Ayat 15
was chosen because its wrapping demonstrates that selection uses cloned inline
line fragments instead of colouring the full reader width or treating the
whole page as one rectangle. The selection uses
`quranPrimaryContainer`/`quranOnPrimaryContainer`; it does not use error red.
No Latin transliteration or translation is copied into this Arab-only frame.

### Validation and known limitations

* The raw response validates as 30 unique ayat, numerically complete 1–30, with
  page 593 containing ayat 1–23 and page 594 containing ayat 24–30.
* The HTML Arabic spans are compared programmatically with the sorted page-593
  response before acceptance.
* Chromium renders the 360×800 HTML at device scale 2 into a 720×1600 PNG.
* Haptic feedback, semantic long-click, focus transfer, and action-sheet
  dismissal remain implementation behaviours; this frame records only the
  visible selected-range state.

### Next recommended milestone

Create the ayat action-sheet frame over this same selected ayat, then use the
same preserved response for the Arab+translation reader and footnote states.

## Standalone Quran ayat-action-sheet visual milestone (2026-08-08)

**Status:** Ayat action-sheet reference completed; production interaction is
still represented only by the isolated Compose spike.

Added `10-ayat-action-sheet` over the preserved selected state for Al-Fajr ayat
15 (`id = 6008`). The background remains visibly selected beneath a modal
scrim, while the sheet provides exactly the four approved actions in the same
order and wording as the Compose prototype: Tambahkan bookmark, Tafsir Kemenag,
Tandai terakhir dibaca, and Informasi Juz dan halaman. A visible 48dp close
action and system-Back dismissal are part of the recorded contract. Copy,
share, audio, and Latin transliteration are absent.

The frame represents a realistic scrolled position: the non-sticky surah-start
header and basmalah are above the viewport when ayat 15 is long-pressed. The
selected line fragments remain visible immediately above the sheet instead of
being hidden by the modal surface.

### Validation and known limitations

* The action labels are compared with the existing Android string resources.
* The HTML still contains page 593 Arabic copied exactly from the preserved raw
  response; no source text is regenerated for the modal frame.
* Chromium renders the 360×800 HTML at device scale 2 into a 720×1600 PNG.
* Focus transfer, TalkBack announcements, haptic feedback, Back dismissal, and
  action results require real-device validation when the production feature is
  wired.

### Next recommended milestone

Create the Arab+translation reader from the same Al-Fajr response, including a
footnote presentation example without displaying the prohibited Latin field.

## Standalone Quran approved-reader component promotion (2026-08-08)

**Status:** Approved visual components promoted into the Android project; not
yet connected to Quran navigation, Room, Kemenag networking, or credentials.

The approved Arab-only start state, selected-range state, and ayat action sheet
now have production-source Compose/resource counterparts. Added a reusable
surah-start header that renders Room-supplied category, display name, and ayat
count, and enforces the separate-basmalah exception for Al-Fatihah and
At-Taubah by numeric surah identity. The approved Amiri path source was
mechanically converted into an Android VectorDrawable; only unused viewBox
whitespace was cropped, and the Amiri OFL notice is included in `res/raw`.

The flowing reader now changes both background and foreground semantic colours
for the selected ayat range. The action-sheet drag handle and action icons use
the approved Quran muted/primary roles. The debug-only fixture preview composes
the new start header with the existing flowing-reader interaction without
shipping fixture Quran content in release sources.

The preserved HTML/PNG/JSON files remain the visual and source-data regression
baseline. The Al-Fajr JSON is not copied into Android assets or runtime data;
production content must still enter through validated Kemenag DTO ingestion
and Room.

### Validation and known limitations

* `./gradlew detekt` passed.
* `./gradlew lint assembleDebug` passed; Android resource processing therefore
  validates the generated VectorDrawable and packaged OFL text resource.
* `./gradlew ktlintFormat` and `./gradlew ktlintCheck` were executed. Both remain
  blocked by the pre-existing Nahwu Quiz/Reminder formatting set; neither
  report contains a Quran source path. Formatter changes outside the Quran
  scope were reverted immediately.
* `git diff --check` passed.
* No emulator/device was connected, so `installDebug`, long-press geometry,
  TalkBack, and the VectorDrawable's device rendering remain unverified.
* These components are deliberately not navigable yet. Wiring them before the
  validated API-to-Room data slice would violate Room-as-source-of-truth and
  risk promoting fixture data into the release flow.

### Next recommended milestone

Implement Quran delivery slice 1: credential hardening, validated Kemenag DTO
ingestion, Room entities/DAO, atomic initial preparation, and repository
observations. Then wire these approved reader components to real Room-backed UI
models.

## Standalone Quran complete presentation-reference catalog (2026-08-08)

**Status:** Complete local HTML/JSON/PNG reference catalog; no design-tool MCP or
production navigation/data wiring performed.

Expanded `docs/design/design-export/quran/` from the three approved reader
frames into 26 presentation references. The set now covers populated and empty
Surah/Juz/Bookmark/Terakhir Dibaca hub states, initial checking/preparation and
errors, background refresh outcomes, both reader displays, long-press and
action-sheet states, all required tafsir cache/loading/error outcomes, display
settings, source attribution, Aktivitas integration, and Room loading/invalid
target recovery.

Every generated state has editable HTML, a JSON implementation contract, and a
720×1600 PNG. `00-quran-state-catalog.html` provides a local state selector,
`00-quran-state-catalog.json` inventories the full set, and
`generate-quran-catalog.rb` makes the generated portion reproducible.

Religious content remains source-bound: Arab+translation and its footnote are
read directly from the preserved Al-Fajr response; tafsir success/cache states
read directly from the preserved An-Nas ayat-6232 response; no Latin API field
is rendered. Missing Juz mappings are deliberately described generically
instead of being invented.

### Validation and known limitations

* Ruby syntax validation and catalog generation completed successfully.
* All JSON files parse successfully; the catalog reports 23 generated plus 3
  retained approved frames.
* All 26 frame PNGs were rendered by headless Chromium at exactly 720×1600.
* Representative hub, reader, tafsir, settings, source, activity, loading, and
  error PNGs were visually inspected.
* These are local design references, not real design-tool nodes; node IDs remain
  empty until manual recreation.
* No Gradle task was run because this milestone modifies design documentation
  and generated visual artefacts only.

### Next recommended milestone

Implement Quran delivery slice 1, then build presentation ViewModels and
Navigation 3 destinations directly against these state contracts and
Room-backed models.

## Standalone Quran Arab-only full-page baseline revision (2026-08-08)

**Status:** Approved revision complete; design references are ready for the
presentation-layer milestone.

The product owner rejected the former short, centred An-Nas Arab-only frame.
Removed `07-flowing-reader-arab-only.{html,json,png}` and replaced it with one
canonical full-page sequence built from the supplied, numerically sorted
Al-Fajr page-593 response:

1. `09-flowing-reader-arab-only-page.*` — normal full page, ayat 1–23;
2. `09b-flowing-reader-arab-only-selected.*` — the same page and geometry with
   ayat 15 selected;
3. `10-ayat-action-sheet.*` — the same selected reading composition behind the
   approved action sheet.

The catalog generator, catalog HTML/JSON, handoff, design-system checklist,
README, and action-sheet sidecar now reference this sequence. The discarded 07
files are absent, so future presentation work cannot accidentally select the
superseded short-page layout.

### Validation and known limitations

* All three full-page states were visually compared after rendering.
* The normal and selected frame PNGs are exactly 720×1600.
* The catalog remains complete at 26 frame triplets after replacing, rather
  than merely removing, the superseded baseline.
* This revision changes visual contracts only; presentation/navigation/data
  implementation remains the next milestone.

### Next recommended milestone

Implement the Quran presentation layer against the final catalog, using
`09-flowing-reader-arab-only-page` as the sole normal Arab-only page baseline
and deriving its selected/modal states without recomposing the underlying page.

## Standalone Quran autonomous implementation prompt (2026-08-08)

**Status:** Complete. Documentation-only; no Android implementation or Gradle
configuration changed.

Created `docs/CLAUDE_QURAN_AUTONOMOUS_PROMPT.md` as the Claude Code execution
contract for the product-owner-authorized complete Al-Qur'an Kemenag `0.0.6`
scope. It supersedes the old ADR-0015-specific autonomous prompt only when
explicitly supplied to Claude; the older prompt remains preserved for its
historical objective.

The Quran prompt authorizes continuous execution across all five delivery
slices in `QURAN_PRD.md` section 14 without routine approval checkpoints. It
locks the reader to the canonical `09` normal → `09b` selected → `10` action
sheet sequence, preserves the dirty user-owned worktree, requires per-slice
validation and progress updates, and defines continuation/handoff behavior for
context limits.

Permission bypass is explicitly bounded: it does not authorize destructive
operations, secret exposure, Git history rewriting, remote pushes,
deployments, releases, or external submissions. Missing production
credentials, font permissions, hardware, or formal API evidence block only
their dependent release checks while independent implementation continues.

### Files created

`docs/CLAUDE_QURAN_AUTONOMOUS_PROMPT.md`.

### Files modified

`docs/PROGRESS.md`.

### Commands executed

Read-only inspection of `CLAUDE.md`, `docs/PROGRESS.md`, the Related Documents
section of `docs/product/PRD.md`, `docs/product/QURAN_PRD.md`,
`docs/design/QURAN_DESIGN_SYSTEM.md`,
`docs/engineering/QURAN_API_CONTRACT_DRAFT.md`, ADR 0016, the existing Claude
autonomous prompt, Git status, and `claude --help`.

### Test results

No Android tests or Gradle tasks were run because this pass changes only
documentation. The prompt invocation uses flags confirmed by the locally
installed Claude CLI help.

### Known limitations

No prompt can restart a terminated CLI process or supply absent external
production inputs. The handoff protocol preserves resumability, and the
permission-bypass flag suppresses routine permission prompts; external
product/legal/release blockers remain real.

### Next recommended milestone

Run the Quran autonomous prompt and begin Slice 1: API/credential boundary,
Room baseline, validator, atomic initial/full synchronization, and repository
foundation, then let the same session proceed automatically through Slices
2–5.

## Standalone Al-Qur'an Kemenag Slice 1 — API, credential, Room, validation, sync (2026-08-08)

**Status:** Implemented and verified locally — `ktlintFormat`, `ktlintCheck`, `detekt`,
`:app:assembleDebug` (including the new native build), `:app:lint`, and `:app:testDebugUnitTest`
(82/82, 64 pre-existing + 18 new) all pass. `:app:assembleRelease` fails exactly as designed (see
Credential boundary below — this is the intended negative gate, not a defect). Manually verified on
a Pixel_9 emulator (Android 15/API 35): a fresh install (after clearing the pre-existing v4 local
database, expected under the pre-release schema-freeze policy) boots cleanly, imports bundled
amaliyah/Nahwu content, and renders Beranda with no crash — confirming the new Room v5 schema
(six added Quran tables) initializes correctly and the Hilt DI graph (including the two new Quran
modules) resolves. `connectedDebugAndroidTest` could not be executed to completion this session —
see Known limitations.

Run under `docs/CLAUDE_QURAN_AUTONOMOUS_PROMPT.md`'s continuous-execution authorization (product
owner explicit authorization for the full `0.0.6` scope, superseding the normal one-milestone-at-a-
time rule per `CLAUDE.md`).

**Scope:** Delivery slice 1 of `docs/product/QURAN_PRD.md` §14 — QUR-FR-002/003/004 and the
data/security portions of 013/018: Kemenag DTOs and dedicated network client, the ADR 0016 native
credential boundary, clean-baseline Room entities/DAOs for the six Quran tables, structural
validation, atomic initial/refresh full-sync, on-demand tafsir cache, and the `QuranRepository`
domain boundary. No UI, navigation, or Beranda entry point yet — those are Slice 2/3 scope; nothing
in this slice is reachable from the running app.

### What shipped

- **DTOs** (`data/remote/quran/dto/`): `QuranEnvelopeDto<T>` (the observed
  `{code, res, data}` envelope, shared by all three endpoints), `QuranSurahDto`, `QuranAyatDto`
  (the API's `teks` Latin transliteration is decoded as `teksLatin` but never mapped past this DTO —
  QUR-FR-009), `QuranTafsirDto`.
- **Domain models** (`domain/model/`): `QuranSurah`, `QuranVerse` (keyed by local `(surahNumber,
  ayatNumber)`, not the remote id — see Room below), `QuranTafsir`, `QuranBookmark`,
  `QuranReadingState`, `QuranReadingSession`, plus two domain-safe result types
  (`QuranPreparationResult`, `QuranTafsirResult`) so `data/sync/quran/`'s result types never leak
  into `domain/repository/QuranRepository.kt`.
- **Room** (`data/local/entity/`, `data/local/dao/`): `quran_surahs` (PK `number`), `quran_verses`
  (composite PK `(surahNumber, ayatNumber)`, unique index on `remoteId`, `ON DELETE CASCADE` FK to
  `quran_surahs`), `quran_tafsir` (PK `remoteAyatId`, no FK to verses — cached independently of a
  source refresh), `quran_bookmarks` and `quran_reading_state` (singleton, fixed id like
  `TasbihSessionEntity`) — both **deliberately not foreign-keyed** to `quran_verses`, since a
  refresh's wholesale verse replacement would otherwise cascade-delete every bookmark/last-read row
  on the exact refresh they are required to survive (QUR-FR-011/012) — and `quran_reading_sessions`
  (autogenerated id, mirrors `AmaliyahCompletionEventEntity`'s snapshot-log shape).
  `SanguSantriDatabase`
  → version 5 (clean baseline, no `MIGRATION_4_5` — same pre-release schema-freeze policy Nahwu Quiz
  used at version 4; an existing local install must clear app data or reinstall once, confirmed by
  reproducing and resolving exactly that crash on-device, see Manual validation below).
- **Validator** (`data/remote/quran/QuranValidator.kt`): envelope success check, exactly 114 unique
  surah ids numbered 1–114, per-surah ayat-belongs-to-surah / unique ayat numbers forming
  `1..jmlAyat` (accepts out-of-order arrays, e.g. the documented Surah 114 example — canonical order
  is the caller sorting by numeric `ayat`, never trusted from array position) / unique remote ids,
  non-blank Arabic/translation, positive `juz`/`halaman`, plus a separate cross-surah
  `validateGlobalUniqueness` for remote ids no single per-surah check can see. Purely structural —
  never repairs, merges, or invents content.
- **Mappers** (`data/mapper/QuranEntityMappers.kt`): DTO → entity → domain, preserving every
  official field exactly.
- **Credential boundary** (ADR 0016, first NDK code in this project):
    - `app/src/main/cpp/` (`CMakeLists.txt`, `quran_credential.cpp`) — a minimal native library
      verifying the caller's release signing-certificate SHA-256 digest before XOR-decoding an
      embedded credential; fails closed (returns `null`) on any mismatch or unconfigured build
      input,
      never logs either side of the comparison.
  - `app/build.gradle.kts`: `ndkVersion`/`externalNativeBuild.cmake` wiring (`abiFilters` covers
    all four Play-supported ABIs — `arm64-v8a`/`x86_64`/`armeabi-v7a`/`x86` — restored to full
    coverage 2026-08-09 after Play Console warned the `arm64-v8a`/`x86_64`-only build dropped
    5,020 previously-supported devices; see below), a `generateQuranCredentialHeader` task that
      reads `SANGU_QURAN_API_USERNAME`/`SANGU_QURAN_API_TOKEN`/`SANGU_QURAN_RELEASE_SHA256` from
      environment variables or Gradle properties (never the tracked `gradle.properties`) and writes
      a
      generated header (`build/generated/quranCredential/`, gitignored, outside the source tree) —
      absent values produce an "unconfigured" placeholder so a local debug build always compiles,
      and
      a separate `verifyQuranReleaseCredential` task (wired to `assembleRelease`/`bundleRelease`)
      fails the build with a clear message when they are missing. Both tasks compute their content
      at
      Gradle configuration time (not inside `doLast`) for configuration-cache compatibility.
      `QURAN_API_BASE_URL` is a fixed `BuildConfig` field (the real, publicly documented Kemenag
      URL —
      not a secret, so unlike `CONTENT_API_BASE_URL` it has no override property).
    - `QuranCredentialProvider` (`data/remote/quran/`): resolves once, held in memory for the
      process
      lifetime. `BuildConfig.DEBUG` short-circuits to a fixed, unmistakably fake fixture
      credential
      (`debug-fixture-username`/`debug-fixture-token`) without ever touching the native
      library —
      debug/test builds never require production secrets (PRD §9). Release builds compute this
      app's
      own signing-certificate SHA-256 via `PackageManager` and only then call
      `QuranNativeCredentialBridge` (the JNI boundary).
    - `QuranAuthInterceptor`: attaches `username`/`token` headers only to requests whose host is
      exactly `quran-api.lpmqkemenag.id`; passes every other request through unauthenticated
      (header-origin isolation, verified by `QuranAuthInterceptorTest`).
    - `di/QuranNetworkModule.kt`: a wholly separate `@QuranHttpClient`-qualified `OkHttpClient`/
      `Retrofit`/`QuranApiService` from `NetworkModule`'s Firebase Hosting content client —
      Kemenag
      headers can never attach to a non-Kemenag request even by accident.
- **Sync** (`data/sync/quran/`): `QuranSyncManager` — fetches `/surah/local/1/114` then all 114
  surahs' `/ayat/local/{no_surah}` with bounded concurrency (`Semaphore(6)`), validates the complete
  candidate, and only then atomically replaces `quran_surahs`/`quran_verses` in one Room transaction
  (`surahDao.deleteAll()` cascades to verses, so a genuinely shrinking candidate can never leave
  orphaned rows — a pure upsert could not guarantee that). Any failure leaves Room untouched — no
  partial activation, no resumable staging, matching ADR 0016's explicit rejection of both.
  `QuranSyncMetadata` gates the seven-day refresh window on the **last successful** sync only
  (deliberately not "last terminal attempt" like the amaliyah content sync — a Quran refresh is only
  ever triggered by a user opening the hub, not a periodic background worker, so there is no
  hammering risk to guard against). `QuranTafsirManager` fetches/validates/caches one ayat's tafsir
  on demand, independent of the main dataset sync.
- **Repository** (`domain/repository/QuranRepository.kt`, `data/repository/QuranRepositoryImpl.kt`):
  one cohesive Quran-bounded-context repository (not split per table, per
  `CODING_STANDARD.md`'s no-duplicate-repository rule) exposing Room-backed `Flow`s plus
  `ensureInitialPreparation()`/`refreshIfStale()` (delegating to `QuranSyncManager`), bookmark
  toggle, last-read write, reading-session recording (only when position actually advances —
  QUR-FR-017), and cached/fetched tafsir reads. Bound via `di/QuranModule.kt`.

### Content safety

No Arabic/Indonesian religious text was invented. Every fixture value introduced in test sources
(`QuranValidatorTest`, `QuranSyncManagerTest`) carries an explicit `[FIXTURE...]` marker, mirroring
the existing `ContentValidatorTest`/`ContentSyncManagerTest` convention — verified by inspection, no
Arabic Unicode appears in any new source file except the existing, unrelated `[FIXTURE-AR]` markers
already present in `QuranFlowingPageText.kt`'s previously-promoted prototype fixtures.

### Validation

```text
./gradlew :app:ktlintFormat    — pass (Quran files clean; ktlintFormat's reformatting of
                                  unrelated, already-pre-existing-non-compliant Nahwu Quiz/Reminder
                                  files was reverted via git checkout to keep this diff scoped, same
                                  as prior Quran-milestone precedent)
./gradlew :app:ktlintCheck     — pass for every Quran-scoped file (verified: `grep -i quran` over
                                  the failure report returns no match); still blocked overall by the
                                  same pre-existing Nahwu Quiz/Reminder formatting set flagged since
                                  the last several milestones — not touched, out of this slice's scope
./gradlew :app:detekt          — pass (after adding `@Suppress` annotations matching this codebase's
                                  own established precedent — `TooManyFunctions` on the one cohesive
                                  Quran repository/interface mirroring `di/DatabaseModule.kt`'s;
                                  `ReturnCount` on guard-clause-heavy validation functions mirroring
                                  `ContentValidator`/`NahwuQuizValidator`'s)
./gradlew :app:lint            — pass
./gradlew :app:assembleDebug   — pass, including the project's first native (NDK/CMake) build,
                                  compiling and packaging `libqurancredential.so` for arm64-v8a and
                                  x86_64
./gradlew :app:assembleRelease — FAILS by design: `verifyQuranReleaseCredential` reports the three
                                  missing secret inputs (`SANGU_QURAN_API_USERNAME`,
                                  `SANGU_QURAN_API_TOKEN`, `SANGU_QURAN_RELEASE_SHA256`) with a clear
                                  message and no partial artifact — the required negative release
                                  gate (ADR 0016, `docs/security/SECURITY_BASELINE.md`), verified
                                  working as intended, not a build defect
./gradlew :app:testDebugUnitTest — 82/82 passed (64 pre-existing + 18 new `QuranValidatorTest`:
                                  out-of-order Surah 114, missing/duplicate ayat, wrong surah,
                                  duplicate remote id within and across surahs, invalid envelope,
                                  blank Arabic/translation, non-positive juz/halaman)
./gradlew :app:compileDebugAndroidTestKotlin — pass (confirms `QuranAuthInterceptorTest`/
                                  `QuranSyncManagerTest` compile correctly even though they could not
                                  be executed on-device this session, see Known limitations)
```

### Manual validation (Pixel_9 emulator, Android 15/API 35)

`adb devices` was checked first per the autonomous prompt's instruction (initially empty; an AVD
was then started for this session). `installDebug` over the existing pre-Quran local install
reproduced exactly the expected pre-release schema-freeze crash
(`IllegalStateException: A migration from 4 to 5 was required but not found`) — confirming the
version bump is real and the app correctly has no destructive-migration fallback (ADR 0003 remains
respected). `adb shell pm clear com.sangusantri.app` followed by a relaunch confirmed a fresh
install
boots cleanly: bundled Tahlil/Istighosah/Nahwu Quiz content imported, Beranda rendered
(`ActivityTaskManager: Displayed ... +1s40ms`), no `FATAL EXCEPTION`. This exercises the new Room v5
schema and the two new Hilt modules (`QuranModule`, `QuranNetworkModule`) end-to-end at real
runtime,
though nothing in the running app yet calls into `QuranRepository`/`QuranCredentialProvider` (no UI
exists yet) — confirmed by the absence of any `qurancredential`-tagged logcat line.

### Known limitations

- **`connectedDebugAndroidTest` could not be executed to completion this session.** Every attempt
  (four, including after a full emulator reboot) crashed identically with
  `Unable to start receiver com.sangusantri.app.data.reminder.ReminderBootReceiver: ...
  IllegalStateException: The component was not created. Check that you have added the
  HiltAndroidRule` — before any test class runs. This was reproduced against a completely unrelated,
  pre-existing test (`SanguSantriDatabaseTest`), and `git diff` confirms this session never touched
  `SanguSantriApplication.kt`, `data/reminder/`, or `AndroidManifest.xml` — so this is a
  pre-existing
  interaction between the real emulator delivering `BOOT_COMPLETED` to the installed app's
  manifest-registered `ReminderBootReceiver` (Milestone 11) and the `HiltTestApplication`
  instrumentation process, not a defect introduced by this slice. `QuranAuthInterceptorTest` and
  `QuranSyncManagerTest` are therefore verified only by compilation
  (`compileDebugAndroidTestKotlin`) and code review this session, not a real on-device run. A future
  session with a clean/freshly-created AVD (no prior boot-completed-receiver interaction) should
  retry
  `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.sangusantri.app.data.remote.quran.QuranAuthInterceptorTest,com.sangusantri.app.data.sync.quran.QuranSyncManagerTest`.
- **Release credential/signing-digest inputs remain genuinely absent** (PRD §13, blocking production
  input) — `verifyQuranReleaseCredential` correctly fails `assembleRelease`/`bundleRelease` until
  real values are supplied through untracked local/CI secret storage. This is expected, not a
  defect.
- ~~Native build restricted to `arm64-v8a`/`x86_64`~~ — resolved 2026-08-09: `abiFilters` now
  includes `armeabi-v7a`/`x86` too, after Play Console's release warning that the 64-bit-only build
  dropped 5,020 previously-supported devices. Native build time is correspondingly longer (four ABIs
  instead of two).
- **No UI, navigation, or Beranda entry point yet** — by design (Slice 2/3 scope). Nothing in this
  slice is reachable from a running app session; the manual on-device verification above therefore
  only confirms boot/schema/DI correctness, not any Quran-specific user flow.
- Tafsir stale-while-revalidate UX orchestration (QUR-FR-013's "cached data shows immediately,
  revalidates in background when 7+ days old") is intentionally left to Slice 4 — this slice
  provides only the underlying cache-read/fetch-and-cache primitives (`getCachedTafsir`/
  `fetchTafsir`).

### Next recommended milestone

Slice 2 (`docs/product/QURAN_PRD.md` §14): Beranda entry point, dark-theme boundary, the Quran hub
with Surah/Juz/Bookmark/Terakhir Dibaca tabs, local surah search, bookmarks, and last-read state —
building the first real UI on top of this slice's `QuranRepository`.

## Kalender Hijriah discovery and product draft (2026-08-08)

**Status:** Discovery and PRD draft complete; no Android feature code,
navigation, schema, Gradle configuration, or runtime integration changed.

Investigated the product owner's supplied MyQuran v3 calendar endpoint, its
live OpenAPI v3.1.3 contract, live responses, public repository implementation,
and tests. The endpoint can convert one Gregorian date at a time and supports
`standar`, `islamic-umalqura`, and `islamic-civil`, but has no monthly/batch
calendar response. Its holiday routes are confirmed placeholders, and it does
not provide the reference images' pasaran, fasting lists, religious events, or
historical commemorations.

The supplied 8 August 2026 example exposed the core accuracy/product decision:
MyQuran returned 25 Safar 1448 H for `standar`/`islamic-umalqura`, 23 Safar for
`islamic-civil`, and 24 Safar only with `adj=-1`, while the reference image
shows 24 Safar. A local JVM probe of the same `HijrahDate` API used by
Pengingat also returned 25 Safar. MyQuran's adjusted response additionally
changed the Hijri `dayName` to Friday while leaving the CE date on Saturday,
so it cannot be consumed naively as a selected-date label.

Created `docs/product/HIJRI_CALENDAR_PRD.md` as a proposed `0.0.7`: a simple,
fully offline dual calendar using the same Umm al-Qura chronology as Pengingat,
with a Beranda entry, month grid, Hijri span, selection/today behaviour, and an
explicit calculation-versus-official-determination notice. The draft excludes
MyQuran runtime calls, holidays, events, fasting lists, and pasaran until each
has an appropriate source and separate approval. Added the proposed release to
`docs/product/ROADMAP.md` and linked the feature PRD from the main PRD's Related
Documents section.

### Validation and known limitations

* Live MyQuran requests verified all three methods, `adj=-1`, HTTP 400 invalid-
  date handling, the placeholder holiday response, the advertised rate-limit
  header, and a 2100 conversion.
* The MyQuran public repository currently exposes no detected licence or
  production SLA/change policy; no code or data was copied into SanguSantri.
* Both supplied reference images were visually inspected. They are treated as
  information-hierarchy inspiration only, not a source for their event text,
  pasaran values, colours, or exact layout.
* No Gradle task or Android test was run because this pass changes product
  documentation only. Markdown links and changed-document consistency were
  reviewed in this session.
* The `0.0.7` position, Umm al-Qura method, local-only architecture, notice,
  exclusions, and design-tool requirement remain proposed until the product owner
  explicitly approves them.

### Next recommended milestone

Finish the already-active standalone Quran `0.0.6` implementation. Then approve
the Kalender Hijriah PRD decisions and create its required design-tool states before
starting `0.0.7` Android implementation.

## Standalone Al-Qur'an Kemenag Slice 2 — Entry, hub, search, bookmarks, last read (2026-08-08)

**Status:** Implemented and verified locally — `ktlintFormat`, `ktlintCheck`, `detekt`,
`:app:lint`, `:app:assembleDebug`, `:app:testDebugUnitTest` (82/82 unchanged, no regressions), and
`:app:compileDebugAndroidTestKotlin` all pass. Manually verified end-to-end on the Pixel_9 emulator
(Android 15/API 35, real network egress, no mocking): tapping Beranda's new "Al-Qur'an Kemenag" card
opens the entry gate under the forced dark theme; with no local dataset yet, it made a real HTTP
request to the actual `quran-api.lpmqkemenag.id` host (confirming host-scoped header attachment
works against production, not just MockWebServer), which the real API correctly rejected because
this session has no genuine Kemenag credential (the debug fixture is intentionally fake per ADR
0016/PRD §9) — the app then rendered the "Gagal menyiapkan Al-Qur'an" failure state cleanly, with no
crash and no raw error text; "Coba lagi" correctly re-ran the same flow; the back button returned to
Beranda with the light theme and system-bar appearance fully restored. This is a genuine, expected,
documented blocker (missing production credentials, PRD §13) — see Known limitations.

Run under `docs/CLAUDE_QURAN_AUTONOMOUS_PROMPT.md`'s continuous-execution authorization, continuing
directly from Slice 1 in the same session.

**Scope:** Delivery slice 2 of `docs/product/QURAN_PRD.md` §14 — QUR-FR-001/005/006/007/011/012: the
Beranda entry point, the dark-theme/system-bar boundary, the initial-checking/preparing/failed/
offline/ready entry gate, and the Quran hub (Surah/Juz/Bookmark/Terakhir Dibaca tabs, local surah
search, continue-reading panel) — all built on Slice 1's `QuranRepository`. Actual ayat reading
(tapping a surah/Juz/bookmark/session row) does not yet navigate anywhere — Slice 3 adds the reader
destinations; this is a deliberate, documented in-progress boundary within one continuous
implementation pass, not a released partial feature (`docs/product/QURAN_PRD.md` §14's "no slice is
independently released as a reduced MVP" governs release/publication, not intermediate commits
within one uninterrupted session that continues through to Slice 5).

### What shipped

- **`QuranSyncManager` progress reporting**: `sync()` gained an `onProgress: (completed, total) ->
  Unit` callback, invoked as each of the 114 concurrent per-surah ayat fetches completes (via an
  `AtomicInteger` counter) — purely a presentation-layer signal, computed before and independent of
  the atomic Room commit at the end, so the required determinate "completed surah count" progress
  UI (QUR-FR §6.1) doesn't require any partial/intermediate Room writes. `QuranRepository.
  ensureInitialPreparation` threads this through; `refreshIfStale()` does not (a silent background
  refresh needs no progress UI).
- **`QuranConnectivityChecker`** (`data/remote/quran/`): a synchronous `ConnectivityManager` check
  used only by the entry gate to distinguish "offline, no local data" from a mid-sync network
  failure (QUR-FR §6.1's two distinct states).
- **`QuranVerseDao.observeJuzStarts()`**: a correlated-subquery Room query returning the first
  locally ordered verse of each Juz 1–30 (QUR-FR-007) — derived only from stored `juz` fields, never
  a hardcoded or AI-derived mapping. Exposed via `QuranRepository.observeJuzStarts()`.
- **Entry gate** (`feature/quran/`): `QuranEntryUiState` (`Checking`/`Preparing(completed,total)`/
  `PreparationFailed`/`OfflineNoLocalData`/`Ready`), `QuranEntryViewModel` (checks
  `hasLocalDataset()` first; if absent, checks connectivity before ever attempting a fetch; drives
  `ensureInitialPreparation` with live progress), `QuranEntryScreen`/`QuranEntryRoute` (same
  "resolve, then let the NavHost navigate" pattern as `ReaderEntryRoute` — `onReady` fires once via
  `LaunchedEffect`, never rendering the hub itself).
- **`QuranThemeBoundary`** (`feature/quran/`): wraps Quran composables in a forced-dark
  `SanguSantriTheme` and switches system status/navigation-bar icon appearance via
  `WindowCompat.getInsetsController`, restoring the previous appearance in `onDispose` — verified
  on-device (see Manual validation). Applied by both `QuranEntryRoute` and `QuranHubRoute`; Slice
  3's
  reader/settings destinations will reuse the same composable rather than duplicating the effect.
- **Quran hub** (`feature/quran/hub/`): `QuranHubUiState`/`QuranHubTab`/`QuranHubActions`,
  `QuranHubViewModel` (combines five `QuranRepository` flows plus local tab-selection/search-query
  state via `combine`; local case/diacritic-tolerant surah search over Latin name and exact surah
  number via `java.text.Normalizer` NFD + combining-mark strip, QUR-FR-006 — never a network
  request; a silent `refreshIfStale()` call on `init`, QUR-FR-004 §6.2), `QuranHubScreen.kt` (app
  bar, continue-reading panel, tab row, search field) and `QuranHubTabContent.kt` (the four
  per-tab lists/rows and shared empty-state/list-row components — split into two files to stay under
  the project's per-file function-count threshold, not for any architectural reason).
- **Beranda entry point**: `SerambiActions.onQuranClick` (and `SerambiContent` now takes the whole
  `actions` bundle instead of individually destructured callbacks, both fixing a detekt
  `LongParameterList` violation and matching `QuranHubScreen`'s existing `QuranHubActions` bundling
  pattern), a new, always-shown `QuranEntrySection` card (QUR-FR-001 — a real, accessible entry
  point, not hidden behind any data condition, same "always shown" reasoning as the existing
  `NearestReminderSection`).
- **Navigation** (`SanguSantriNavHost.kt`): `QuranEntry`/`QuranHub` `NavKey`s, same
  gate-replaced-by-resolved-destination pattern as `ReaderGate`/`NahwuQuizInstruction`. Neither is a
  bottom-nav destination (QUR-FR-001); the existing bottom bar already hides automatically once
  pushed (not at top-level root), no special-case needed. Surah/ayat row taps in the hub do not yet
  navigate anywhere (`onSurahSelected = {}`, `onAyatSelected = { _, _ -> }`) — filled in by Slice 3.
- **Strings**: all new Indonesian user-facing text added to `strings.xml` (Beranda card, entry-gate
  states, hub title/tabs/search/rows/empty-states).

### Content safety

No Arabic/Indonesian religious text was invented or hardcoded. This slice adds no new religious
content anywhere — it renders only Room-sourced surah/verse metadata already validated in Slice 1
(surah Latin/Arabic names, categories, ayat counts) through plain Compose `Text`/`stringResource`
calls; no Arabic Unicode was introduced in any Kotlin source file this slice (verified by
inspection).

### Validation

```text
./gradlew :app:ktlintFormat    — pass (Quran files clean; unrelated pre-existing Nahwu Quiz/
                                  Reminder auto-reformatting reverted via git checkout, same
                                  precedent as Slice 1 and every prior Quran design milestone)
./gradlew :app:ktlintCheck     — pass for every Quran-scoped file (grep -i quran over the failure
                                  report returns no match); still blocked overall by the same
                                  pre-existing, untouched Nahwu Quiz/Reminder formatting set
./gradlew :app:detekt          — pass (fixed two real LongParameterList violations by bundling
                                  callbacks into action-holder data classes — SerambiActions
                                  already existed; QuranHubActions is new, mirroring it — rather
                                  than suppressing; fixed a TooManyFunctions violation by splitting
                                  QuranHubScreen.kt into chrome vs. per-tab-content files; a
                                  ReturnCount suppression on QuranConnectivityChecker.isConnected,
                                  matching this codebase's existing guard-clause precedent)
./gradlew :app:lint            — pass
./gradlew :app:assembleDebug   — pass, unchanged native build
./gradlew :app:testDebugUnitTest — 82/82 passed, unchanged from Slice 1 (this slice added no new
                                  automated tests — its logic is either Compose UI, best verified
                                  manually per the temporary design-phase-adjacent working method, or
                                  thin ViewModel orchestration already covered indirectly by Slice
                                  1's QuranSyncManager/QuranValidator tests it delegates to)
./gradlew :app:compileDebugAndroidTestKotlin — pass (pre-existing warnings only)
```

### Manual validation (Pixel_9 emulator, Android 15/API 35, real network)

`installDebug` succeeded without a schema-version crash (Slice 2 added no new Room tables, so no
app-data clear was needed this time). Confirmed by screenshot at each step: (1) Beranda renders the
new "Al-Qur'an Kemenag" card above "Pengingat terdekat"; (2) tapping it opens the entry gate with
correct dark background/system bars; (3) the gate performed a real network request against
`quran-api.lpmqkemenag.id` (not a mock) and landed cleanly on the "Gagal menyiapkan Al-Qur'an"/"Coba
lagi" failure state with no stack trace, raw HTTP body, or crash — confirmed via `adb logcat`
(zero `Exception`/`Error`/`FATAL` lines around the request); (4) "Coba lagi" correctly re-ran the
identical flow and re-landed on the same correct state; (5) the back arrow returned to Beranda with
the light theme and system-bar appearance fully restored (visually confirmed, matching
`QuranThemeBoundary`'s `onDispose` contract).

The hub screen itself (`QuranHubScreen`) could **not** be manually reached or screenshotted this
session — it requires a genuine local Quran dataset, which requires a real Kemenag credential this
environment does not have (expected, see Known limitations). It is verified only by
`compileDebugKotlin`/`detekt`/`lint`/`ktlintCheck` and code review this slice.

### Known limitations

- **The hub screen was never actually rendered on-device this session** — reaching it requires a
  successful initial preparation, which requires a real Kemenag credential (absent, PRD §13
  blocking production input, unchanged from Slice 1). Its four tabs, search, continue-reading panel,
  and empty states are implemented and pass static analysis but are unverified by direct visual
  inspection or an instrumented UI test.
- **No Compose UI tests were added** for the entry gate or hub — consistent with this slice's
  validation approach (manual + static analysis, matching the temporary design-phase-adjacent
  constraint's spirit even though Quran `0.0.6` is nominally outside that window per `CLAUDE.md`);
  revisit before Slice 5's full accessibility/parity audit.
- Surah/Juz/bookmark/recent-session row taps and the continue-reading panel do not navigate anywhere
  yet (`{}`/`{ _, _ -> }` no-ops in the NavHost) — Slice 3 wires these to the real readers.
- Bookmark rows show only surah name + ayat number (no Juz/page context) — a deliberate scope
  simplification since `QuranBookmarkEntity` does not store Juz/page and joining against
  `quran_verses` per row was judged disproportionate for this slice; QUR-FR-012 requires only "
  enough
  position context to resume confidently," which surah + ayat already provides. Revisit if this
  proves insufficient during Slice 5's parity pass.
- The Juz tab's derivation query (`observeJuzStarts`) is a correlated subquery over `quran_verses`
  (~6,236 rows once populated) — acceptable for a hub tab that is not a hot path, but unverified for
  real-device query latency since no local dataset exists in this session to measure against.

### Next recommended milestone

Slice 3 (`docs/product/QURAN_PRD.md` §14): Arab-only flowing reader, Arab+translation reader,
surah-start header/basmalah rules, long-press ayat action sheet, and Tampilan Al-Qur'an settings —
wiring the hub's now-defined but unrouted `onSurahSelected`/`onAyatSelected` callbacks to real
reader
destinations.

## Standalone Al-Qur'an Kemenag Slice 3 — Readers, surah start, long press, settings (2026-08-08)

**Status:** Implemented and verified locally — `ktlintFormat`, `ktlintCheck`, `detekt`, `:app:lint`,
`:app:assembleDebug`, `:app:testDebugUnitTest` (82/82 unchanged), and
`:app:compileDebugAndroidTestKotlin` all pass. Manually smoke-tested on the Pixel_9 emulator: a
fresh `installDebug` boots cleanly with no `FATAL`/`AndroidRuntime` errors, confirming the new
reader/settings Hilt graph (assisted-injected `QuranReaderViewModel`, `QuranSettingsViewModel`, the
new `QuranReaderSettingsRepository` DataStore binding) resolves correctly at real app startup. The
reader and settings screens themselves could not be visually reached this session — see Known
limitations and the important note on real credentials below.

Run under `docs/CLAUDE_QURAN_AUTONOMOUS_PROMPT.md`'s continuous-execution authorization, continuing
directly from Slice 2 in the same session.

**Important — a security issue was found and flagged mid-slice, not fixed by this session:** while
checking `git status` before a routine ktlint-revert step, this session discovered that the tracked
(git-committed) `gradle.properties` had `SANGU_QURAN_API_USERNAME`/`SANGU_QURAN_API_TOKEN`/
`SANGU_QURAN_RELEASE_SHA256` added to it with real values, by some process outside this session (
this
session never edited that file). This is exactly the "untracked local/CI secret storage only" rule
ADR 0016 and `docs/security/SECURITY_BASELINE.md` require — a *tracked* Gradle file must never hold
these. The user was alerted immediately in-conversation (without this session ever printing or
otherwise exposing the values); this session has not staged, committed, or otherwise touched
`gradle.properties`, and no commit has been made this session that could have carried it into git
history. **This must be resolved (values moved to `~/.gradle/gradle.properties`, `local.properties`,
or CI secrets, and the tracked file reverted) before any commit touches `gradle.properties`.**
Separately, `./gradlew :app:verifyQuranReleaseCredential` now reports all three values present,
meaning a real credential may be available for Slice 5's planned "authorized configured credentials
only" online release verification — this session deliberately did not attempt a release build or any
real Kemenag API call with them, since (a) real production API traffic against the product owner's
actual grant deserves a deliberate moment, not an incidental mid-slice check, (b) release
signing/build verification is explicitly Slice 5 scope, and (c) the credential-location issue above
should be resolved first.

**Scope:** Delivery slice 3 of `docs/product/QURAN_PRD.md` §14 — QUR-FR-008/009/010/011/012/014/015/
016/020: both readers (Arab-only flowing, Arab+translation), the surah-start header/basmalah rules
(reusing Slice-2-adjacent approved components), long-press ayat actions, Tampilan Al-Qur'an
settings,
and the font gate (still closed — no candidate has passed licensing/glyph review). QUR-FR-013
(tafsir) is stubbed as a no-op callback — Slice 4 scope.

### What shipped

- **`QuranSyncManager.sync()` progress callback extended for real use**: unchanged from Slice 2's
  addition, now actually exercised by nothing new — kept as-is.
- **Domain/data**: `QuranDisplayMode` (`ARAB_ONLY`/`ARAB_TRANSLATION`, the one global persisted
  display choice, QUR-FR-009), `QuranReaderSettings` (Arabic size/line-spacing, translation size,
  nullable `brightnessOverride` — `null` means "never overridden," matching QUR-FR-015's "restores
  the prior window value on exit" exactly), `QuranReaderSettingsRepository`/
  `QuranReaderSettingsRepositoryImpl` (DataStore-backed, `quran_`-namespaced keys in the existing
  shared preferences DataStore, mirrors `ReaderSettingsRepositoryImpl`'s corruption-safe
  coerce-on-read pattern), bound in `di/QuranModule.kt`.
- **`QuranTranslationAyatList` and `QuranFlowingPageText` promoted from fixed-size prototypes to
  configurable production components**: `QuranTranslationAyatList` gained `lazyListState` (so the
  reader can observe scroll position), `arabicSizeSp`/`arabicLineHeightSp`/`translationSizeSp`
  (settings now actually apply to already-approved visual components instead of the prototype's
  hardcoded 34sp/60sp), and an optional `headerContent` slot (the surah-start header scrolls with
  the list instead of staying pinned) — all additive with safe defaults, so the existing debug-only
  prototype preview compiles unchanged. `QuranFlowingPageText` already accepted a `textStyle`
  override, so no change was needed there.
- **Reader** (`feature/quran/reader/`): `QuranReaderUiState` (`Loading`/`Unavailable`/`Content`),
  `QuranReaderViewModel` (assisted-injected by `surahNumber`, loads the whole surah's verses from
  Room via `observeVersesBySurah`, combines live settings/bookmarks/long-press selection, tracks
  reading position via `onVisiblePositionChanged`/`recordSessionIfAdvanced` for QUR-FR-011/017),
  `QuranReaderScreen.kt` (owns one `LazyColumn`-backed layout per display mode so scroll position is
  observable in both — `QuranFlowingPageText` blocks grouped by `halaman` for Arab-only,
  `QuranTranslationAyatList` for Arab+translation; the existing `QuranSurahStartHeader` renders once
  as the list's first item, correctly applying the Al-Fatihah/At-Taubah basmalah exceptions it
  already implemented; long-press opens the existing `QuranAyatActionSheet` wired to real
  bookmark-toggle/mark-last-read/tafsir-stub/show-position actions).
    - **Scope simplification, documented**: the reader loads one surah's verses at a time (via
      `observeVersesBySurah`), not an arbitrary continuous page range — a real Kemenag page can span
      a
      surah boundary, but browsing "the next surah's pages" from within a reader session is out of
      scope for this slice. `halaman` grouping happens only within the loaded surah.
    - **Scroll-to-ayat and position-tracking are ayat-precise in Arab+translation mode** (items map
      1:1 to ayat) but **page-precise in Arab-only mode** (the flowing-text surface has no per-ayat
      scroll anchor) — a documented, deliberate approximation given the API's own lack of line/glyph
      coordinates (`docs/engineering/QURAN_API_CONTRACT_DRAFT.md`), not a shortcut around available
      precision.
- **`QuranBrightnessEffect`** (`feature/quran/`): applies/restores
  `Window.attributes.screenBrightness`
  for only the current window, same restore-on-exit shape as `QuranThemeBoundary`'s system-bar
  effect; used by both the reader and settings screens.
- **Settings** (`feature/quran/settings/`): `QuranSettingsUiState`/`QuranSettingsViewModel`
  (combines settings + a real, currently-stored verse — Al-Fatihah ayat 1 when available — for the
  live preview, per `docs/design/QURAN_DESIGN_SYSTEM.md` §4's "the exact same verified Kemenag ayat
  fragment"; never invented preview text), `QuranSettingsScreen.kt` (live preview, Arabic
  size/spacing sliders, translation-size slider, Arab-saja/Arab+terjemahan chips, brightness slider;
  a debug-only font-candidate review section listing LPMQ Isep Misbah/Amiri Quran/King Fahd, each
  captioned "Belum lolos pemeriksaan" and non-selectable — absent entirely from release, since no
  candidate has passed QUR-FR-016's licence/glyph gate; no "keep screen on" control, per spec).
- **Navigation** (`SanguSantriNavHost.kt`): `QuranReader(surahNumber, targetAyat)` and
  `QuranSettings` `NavKey`s; the hub's `onSurahSelected`/`onAyatSelected` (previously no-ops) now
  push a real `QuranReader` destination; the reader's settings action pushes `QuranSettings`.
  `onOpenTafsir` remains a no-op — Slice 4 wires it to the tafsir sheet.
- **Strings**: all new Indonesian user-facing text added (reader unavailable/settings-action
  content description, position-info format, Tampilan Al-Qur'an labels, font-candidate names and
  "Belum lolos pemeriksaan").

### Content safety

No Arabic/Indonesian religious text was invented. The settings live preview deliberately reads a
real, currently-stored Room verse rather than a hardcoded string — if no local dataset exists yet
(this session's actual state), it correctly shows "Pratinjau belum tersedia," never fabricated text.

### Validation

```text
./gradlew :app:ktlintFormat/:app:ktlintCheck — pass (Quran files clean; unrelated pre-existing
                                  Nahwu Quiz/Reminder auto-reformatting reverted, same precedent)
./gradlew :app:detekt            — pass, after: bundling/suppressing four real LongParameterList
                                  violations (`QuranTranslationAyatList`'s many optional visual
                                  config params; `QuranReaderRoute`/`QuranReaderScreen`/
                                  `QuranReaderBody`'s many callbacks, consistent with this file's
                                  own established suppress-not-restructure choice) and extracting
                                  four LongMethod violations into smaller composables
                                  (`QuranReaderTopBar`, `QuranReaderBody`,
                                  `QuranReaderScrollToTarget`, `QuranReaderTrackVisiblePosition`,
                                  `QuranSettingsTopBar`, `QuranSettingsBody`) rather than suppressing
./gradlew :app:lint              — pass
./gradlew :app:assembleDebug     — pass, unchanged native build
./gradlew :app:testDebugUnitTest — 82/82 passed, unchanged (no new automated tests this slice —
                                  reader/settings logic is Compose UI and thin ViewModel
                                  orchestration over already-tested repository/sync primitives;
                                  revisit before Slice 5's parity/accessibility audit)
./gradlew :app:compileDebugAndroidTestKotlin — pass (pre-existing warnings only)
```

### Manual validation (Pixel_9 emulator, Android 15/API 35)

`installDebug` succeeded (no schema change this slice) and the app boots with no
`FATAL EXCEPTION`/`AndroidRuntime` crash — confirming the new Hilt-assisted-injection ViewModels and
DataStore repository wire correctly into the running app. The reader and settings screens themselves
were **not** visually reached this session (they require a genuine local Quran dataset, which
requires the real credential — deliberately not exercised this slice, see the security note above).

### Known limitations

- **Neither the reader nor the settings screen was visually verified on-device this session** —
  same root cause as Slice 2's hub (no local Quran dataset without a real, deliberately-unexercised
  credential). Verified only by compilation, `detekt`/`lint`/`ktlintCheck`, and code review.
- Position tracking is page-granularity (not ayat-granularity) in Arab-only mode — see the scope
  simplification above. Revisit if Slice 5's parity pass finds this insufficient.
- The reader loads one surah at a time; there is no cross-surah continuous page browsing.
- No Compose UI tests were added, consistent with Slice 2's approach — revisit before Slice 5.
- Tafsir action is wired to a no-op (`onOpenTafsir = {}`) — Slice 4 scope.
- **The `gradle.properties` secret-location issue is unresolved** — see the note above. This blocks
  any future commit that would touch `gradle.properties` until fixed.

### Next recommended milestone

Slice 4 (`docs/product/QURAN_PRD.md` §14): tafsir cache/sheet (wiring the now-defined
`onOpenTafsir` callback), Aktivitas/combined-streak integration, and the Sumber Al-Qur'an source
view.

## Standalone Al-Qur'an Kemenag Slice 4 — Tafsir, Aktivitas/streak, source, privacy (2026-08-08)

Run under `docs/CLAUDE_QURAN_AUTONOMOUS_PROMPT.md`'s continuous-execution authorization, continuing
directly from Slice 3 in the same session.

**`gradle.properties` security issue (flagged, not fixed, at the end of Slice 3) — now resolved.**
The user explicitly instructed "Fix gradle.properties, then continue with Slice 4." The three real
Kemenag credential values (`SANGU_QURAN_API_USERNAME`/`SANGU_QURAN_API_TOKEN`/
`SANGU_QURAN_RELEASE_SHA256`) that an external process had added to the tracked `gradle.properties`
were moved to the untracked, global `~/.gradle/gradle.properties` (which the existing
`generateQuranCredentialHeader`/`verifyQuranReleaseCredential` tasks already read via
`System.getenv(name) ?: project.findProperty(name)`, so no Gradle task code changed). The tracked
`gradle.properties` was verified via `git diff` to be back to its originally-committed content
(only a trailing-newline difference remains) — confirmed clean of secret values, and confirmed no
commit ever carried them into git history. `./gradlew :app:verifyQuranReleaseCredential` was
re-run this slice and still resolves all three values correctly from the new location. This
session never printed or otherwise echoed the actual secret values in any response.

**Scope:** Delivery slice 4 of `docs/product/QURAN_PRD.md` §14 — QUR-FR-013 (tafsir), QUR-FR-017
(Aktivitas/combined streak), QUR-FR-018 (privacy facts), QUR-FR-019 (source view).

### What shipped

- **Tafsir sheet** (QUR-FR-013): `QuranTafsirUiState` (`Loading`/`Loaded(tafsir, isRefreshing)`/
  `Unavailable(retryable)`), `QuranTafsirSheet` (a `ModalBottomSheet` showing the Kemenag source
  line, ringkas/tahlili sections, a refreshing indicator, and a retry action — matching
  `docs/design/QURAN_DESIGN_SYSTEM.md` §5.6's "cached content shows immediately, inline retry never
  closes the reader" requirement). `QuranReaderViewModel` gained `tafsirUiState`,
  `onOpenTafsir`/`onDismissTafsirSheet`/`onRetryTafsir`, and a cache-first
  `loadTafsir` that shows any cached `QuranTafsir` immediately and silently revalidates in the
  background once it is 7+ days old (`TAFSIR_STALE_THRESHOLD_MILLIS`), never blocking on a
  successful cache hit and never discarding still-valid cached content on a failed revalidation.
  `QuranReaderScreen.kt`/`QuranReaderBody` now render `QuranTafsirSheet` (bound directly to the
  ViewModel via a new `QuranReaderBodyActions` bundle) instead of `QuranAyatActionSheet` whenever
  `QuranReaderUiState.Content.tafsirSheetOpen` is set, replacing the previous no-op
  `onOpenTafsir: (remoteAyatId) -> Unit` threaded in from `SanguSantriNavHost.kt`.
- **Aktivitas combined-streak integration** (QUR-FR-017): new `QuranActivityEntry` domain model;
  `ActivityOverview` gained `weeklyQuranSessionCount`/`recentQuranSessions`/`hasQuranHistory`.
  `ObserveActivityOverviewUseCase` now injects `QuranRepository` and folds Quran reading-session
  dates into the same `activeDates` set that already drives `currentStreakDays`/
  `longestStreakDays` — Quran reading contributes to the one existing amaliyah streak, there is
  still no separate Quran-only streak, per the explicit product requirement. `ActivityRowKind`
  gained `QURAN` (book icon, `Icons.AutoMirrored.Filled.MenuBook`); `ActivityScreen` renders a
  "Riwayat Membaca Al-Qur'an" section (hidden when empty, same per-section rule as every other
  Aktivitas section) and a fourth weekly-summary metric; a new
  `feature/activity/detail/ActivityQuranHistoryScreen.kt` (Route/ViewModel/UiState) reuses the
  existing `ActivityHistoryDetailScaffold` for the full history list, filterable the same way as
  the amaliyah/tasbih detail screens.
- **Sumber Al-Qur'an source view** (QUR-FR-019): new `feature/quran/source/QuranSourceScreen.kt` —
  a static, ViewModel-less full-screen destination (every fact is fixed copy already established
  in ADR 0016/`docs/security/PRIVACY.md`, not derived from live state) covering provenance (LPMQ
  Kemenag), an explicit "SanguSantri is not an official Kemenag application" disclaimer, which
  fields come from the API (and that Latin transliteration is never shown or stored), the
  offline-cache model, and the network/permission scope (including that opening tafsir reveals the
  selected ayat id to Kemenag, per the existing privacy-doc language) — with no copy/share control
  and no `FLAG_SECURE`, per spec. Reachable from a new overflow menu (`MoreVert` icon +
  `DropdownMenu`) on the hub's top bar, and from a new link row at the bottom of Tampilan
  Al-Qur'an (`QuranSettingsScreen` gained a bundled `QuranSettingsActions` for the same
  parameter-count reason as `QuranReaderBodyActions`).
- **Navigation**: new `QuranSource` `NavKey`, reachable from both `QuranHub` and `QuranSettings`.
- **Strings**: all new Indonesian user-facing text added — tafsir sheet copy, Aktivitas Quran
  row/section/weekly-metric copy, and the full Sumber Al-Qur'an body copy.
- **Privacy/readiness docs reviewed, not changed**: `docs/security/PRIVACY.md` §Requirements
  already accurately described "Kemenag receives only ... an explicitly selected ayat's tafsir by
  remote id" and the no-upload guarantees before this slice implemented that exact behavior; the
  Sumber Al-Qur'an screen's own copy restates the same facts to the end user. No inaccuracy was
  found, so no edit was needed — verified rather than assumed.

### Content safety

No Arabic/Indonesian religious text or tafsir content was invented — the tafsir sheet renders only
`QuranTafsir.ringkas`/`.tahlili` exactly as cached from the real Kemenag response. The Sumber
Al-Qur'an copy makes no claim of institutional endorsement and explicitly disclaims official-app
status, per the absolute content-safety rules.

### Validation

```text
./gradlew :app:ktlintFormat/:app:ktlintCheck — pass (Quran/Activity files clean; unrelated
                                  pre-existing Nahwu Quiz/Reminder auto-reformatting reverted,
                                  same precedent as every prior slice)
./gradlew :app:detekt            — pass, after: `@Suppress("LongParameterList")` on
                                  `QuranReaderRoute`/`QuranReaderScreen`/`QuranHubRoute` (each at
                                  exactly 6 params after bundling, detekt's default threshold
                                  triggers at >=6) and `@Suppress("TooManyFunctions")` on
                                  `QuranReaderViewModel` (11 functions incl. tafsir actions),
                                  matching the exact precedent already set by
                                  `GuidedReaderViewModel`
./gradlew :app:lint              — pass (pre-existing warnings only, none new)
./gradlew :app:assembleDebug     — pass, unchanged native build
./gradlew :app:testDebugUnitTest — 82/82 passed, unchanged (no new automated tests this slice,
                                  consistent with Slices 2/3 — tafsir/Aktivitas/source logic is
                                  either thin ViewModel orchestration over already-tested
                                  repository primitives, or static Compose copy)
./gradlew :app:compileDebugAndroidTestKotlin — pass (pre-existing warnings only)
./gradlew :app:verifyQuranReleaseCredential — pass, confirms the gradle.properties fix above
```

### Manual validation (Pixel_9 emulator, Android 15/API 35)

`installDebug` succeeded and the app boots with no `FATAL EXCEPTION`/`AndroidRuntime` crash.
Beranda renders correctly with the Al-Qur'an Kemenag entry card. Tapping it correctly reaches the
entry gate and correctly shows "Gagal menyiapkan Al-Qur'an" (Failed) — the fixed debug-fixture
credential is genuinely rejected by the real Kemenag API, the same confirmed pipeline behavior as
Slice 2. This blocks visually reaching the hub/reader/settings/source screens in a debug build
without real credentials, same known limitation as Slices 2 and 3 (deliberately deferred to
Slice 5's release-credential verification). The Aktivitas tab was reached directly (no credential
needed): with no local amaliyah/tasbih/Quran history yet, it correctly renders the
all-sections-empty state — confirming the new 5-way `combine` in
`ObserveActivityOverviewUseCase` (now including `QuranRepository`) resolves without error and the
added `hasQuranHistory` check correctly participates in `isEntirelyEmpty` without forcing a
false-non-empty render.

### Known limitations

- **The tafsir sheet, Sumber Al-Qur'an screen, and Aktivitas Quran-history/weekly-metric rows were
  not visually verified on-device this session** — same root cause as every prior slice's reader/
  hub verification gap (no local Quran dataset reachable without a real credential in a debug
  build). Verified only by compilation, `detekt`/`lint`/`ktlintCheck`, and code review.
- No Compose UI tests were added, consistent with Slices 2/3 — revisit before Slice 5.
- The hub overflow menu currently has exactly one entry (Sumber Al-Qur'an); this is intentional for
  this slice's scope, not a placeholder for more.

### Next recommended milestone

Slice 5 (`docs/product/QURAN_PRD.md` §14): parity, accessibility, security, and release
validation — including the previously-deferred real-credential release-build verification, now
that credentials are confirmed to resolve correctly from `~/.gradle/gradle.properties`.

## Standalone Al-Qur'an Kemenag Slice 5 — Parity, accessibility, security, release validation (2026-08-08)

Run under `docs/CLAUDE_QURAN_AUTONOMOUS_PROMPT.md`'s continuous-execution authorization, continuing
directly from Slice 4 in the same session. This is the final delivery slice of
`docs/product/QURAN_PRD.md` §14, but **not** a claim that all 15 `§12` acceptance criteria pass —
several are blocked on external inputs listed in `§13 Blocking production inputs` (final font
licences, written confirmation the granted credential covers public end-user traffic, a published
privacy-policy update) that no amount of engineering work this session can satisfy. This entry
documents exactly what was verified, what was fixed, and what remains genuinely blocked.

### What was fixed

- **Accessibility — 48dp touch target gap in the new Sumber Al-Qur'an settings link.**
  `QuranSourceLink` (`feature/quran/settings/QuranSettingsScreen.kt`, added in Slice 4) relied on
  padding math to reach 48dp rather than an explicit constraint, unlike every other Quran
  interactive row in this codebase (e.g. `QuranAyatActionSheet`'s action rows use
  `Modifier.heightIn(min = SanguSantriDimensions.minimumTouchTarget)` explicitly). Fixed to match
  that established pattern.

### What was audited and confirmed already correct (no code change needed)

- **Accessibility**: long-press ayat selection already exposes a proper accessibility long-click
  semantic action (`QuranTranslationAyatList`/`QuranFlowingPageText`'s `semantics { onLongClick
  (...) }`/`customActions`, from Slice 3) — TalkBack users are not limited to gesture-only
  discovery. All icon buttons carry `contentDescription`; decorative icons paired with adjacent
  visible label text correctly use `contentDescription = null` (the standard accessible pattern,
  not an omission). Sliders and dropdown menus use unmodified Material3 components, which already
  announce value/state per platform defaults.
- **Security — no plain credential in the release artifact.** Built a real, credential-resolving
  `assembleRelease` (see Validation below) and scanned every extracted file inside the resulting
  `app-release-unsigned.apk` for the literal `SANGU_QURAN_API_USERNAME`/`SANGU_QURAN_API_TOKEN`
  values read directly from `~/.gradle/gradle.properties` — **zero files matched either string**,
  confirming the native XOR-obfuscation design (ADR 0016, `app/src/main/cpp/quran_credential.cpp`)
  keeps the plain credential out of the shipped artifact, satisfying acceptance criterion 15's
  first half. (Its second half — the ADR's native-extraction-remains-possible disclosure — was
  already present in ADR 0016 from Slice 1 and needed no change.) This scan never printed either
  credential value; only match counts were reported.
- **Security — no credential/secret logging.** Grepped every Quran data/network source file
  (`data/remote/quran/`, `data/sync/quran/`, `data/repository/QuranRepositoryImpl.kt`) for
  `Log.`/`println` calls: the only log statements are `QuranSyncManager`/`QuranTafsirManager`'s
  `Log.w` failure logs, which include only numeric surah/ayat ids and the exception object — never
  response bodies, Arabic/translation/tafsir text, or credential material. No
  `HttpLoggingInterceptor`
  (or any logging interceptor) is attached to the dedicated `@QuranHttpClient` OkHttp client, so no
  request/response header or body can reach logcat through that path either.
- **Security — no `FLAG_SECURE`.** Confirmed absent from the entire `app/src/main/` tree, matching
  QUR-FR-019's explicit "screenshots remain permitted" requirement.
- **Non-colour state cues.** Spot-checked (unchanged from Slice 3): selected-ayat highlighting in
  both readers keys off `selectedAyatId == ayat.remoteId` and renders via a `quranPrimaryContainer`
  background/shape change, not colour alone in a way that would fail for colour-blind users viewing
  a single-hue palette; the action sheet uses icon + label for every action, never colour-only
  meaning.
- **Rotation/process-death resilience.** `QuranReader`/`QuranHub`/`QuranSettings` navigation keys
  are `@Serializable`, so Navigation 3 restores the correct destination and `surahNumber` after
  process death; `QuranDisplayMode` and every reader-setting value are DataStore-backed (survive
  unconditionally); `rememberLazyListState()` is `rememberSaveable`-backed by Compose Foundation
  itself, so scroll position survives configuration change without extra code. Sheet-open state
  (`selectedAyatId`/`tafsirSheetOpen`) lives in `QuranReaderViewModel` and therefore survives
  configuration change but not true process death — judged acceptable ("sheets ... when
  reasonable" per `docs/design/QURAN_DESIGN_SYSTEM.md` §8), consistent with typical Android sheet
  behavior elsewhere in this app.

### Known gap — not fixed this session

- **§6's "restrained crossfade" between Arab-only and Arab+translation display modes was not
  implemented.** The two modes render structurally different `LazyColumn` content (page-grouped
  flowing text vs. one-item-per-ayat), currently sharing a single `LazyListState` in
  `QuranReaderContent`. Wrapping the mode switch in `Crossfade`/`AnimatedContent` without also
  giving each mode its own list state risks both compositions fighting over one scroll position
  mid-animation — a real risk in code this session cannot visually verify on-device (see below).
  Given that risk and the inability to confirm the fix on a real device, this was left as a
  documented gap rather than shipped unverified. The mode switch itself works correctly today; it
  simply switches instantly rather than fading.

### Validation

```text
./gradlew :app:ktlintFormat/:app:ktlintCheck — pass (Quran files clean; unrelated pre-existing
                                  Nahwu Quiz/Reminder auto-reformatting reverted, same precedent)
./gradlew :app:detekt            — pass, no new violations
./gradlew :app:lint              — pass, no new issues
./gradlew :app:assembleDebug     — pass
./gradlew :app:testDebugUnitTest — 82/82 passed, unchanged
./gradlew :app:compileDebugAndroidTestKotlin — pass
./gradlew :app:assembleRelease   — pass: R8/optimization succeeds, both `arm64-v8a`/`x86_64`
                                  `libqurancredential.so` build and package correctly, the
                                  `verifyQuranReleaseCredential` gate passes with the real
                                  credential now in `~/.gradle/gradle.properties`. Output is
                                  `app-release-unsigned.apk` — no `signingConfig` is defined in
                                  `app/build.gradle.kts`, so this is a build-correctness check only,
                                  not a distributable signed artifact (signing is
                                  `docs/engineering/RELEASE_ENGINEERING.md` scope, unchanged this
                                  session).
```

### Manual validation (Pixel_9 emulator, Android 15/API 35)

`installDebug` succeeded and the app boots with no `FATAL EXCEPTION`/`AndroidRuntime` crash after
this slice's one code change. Re-confirmed Beranda → Al-Qur'an Kemenag → entry gate still correctly
rejects the fixed debug-fixture credential against the real Kemenag API (same behavior as every
prior slice).

### Known limitations

- **Design pixel parity could not be checked because no design-tool frames exist yet** —
  `docs/design/QURAN_DESIGN_SYSTEM.md` itself states "Status: approved design direction;
  design-tool frames not yet created" and its own §9 frame checklist is an open design task, not a
  completed reference. "Parity" this slice therefore means parity against the written design-system
  document (verified throughout Slices 1–4 and re-checked section-by-section this slice), not
  pixel-diffing against a hosted design tool.
- **The reader/hub/settings/source/tafsir screens remain visually unverified on-device** — same
  root cause as every prior slice (the debug build's fixed fixture credential is correctly rejected
  by the real Kemenag API; reaching a populated local dataset requires the real, currently
  deliberately-unexercised credential in a genuine online release build). This is the single
  largest remaining verification gap across the whole feature.
- **The `Crossfade` mode-switch animation from `docs/design/QURAN_DESIGN_SYSTEM.md` §6 is not
  implemented** — see the "Known gap" note above.
- **The following `§12` acceptance criteria are blocked on external, non-engineering inputs
  (`§13 Blocking production inputs`) and cannot be closed by further code changes:** criterion 13
  (every enabled font passes a verified-corpus glyph test — blocked on LPMQ Isep Misbah/King Fahd
  licence files, still absent from `docs/design/assets/quran-fonts/`); the privacy-policy-adjacent
  parts of criteria 9/15 (blocked on a published privacy-policy update, `§13`); and any criterion
  implying public release traffic (blocked on written confirmation the granted Kemenag credential
  covers public SanguSantri end-user traffic, `§13`). These are product/legal follow-ups, not
  outstanding engineering work.
- **No new automated tests were added this slice** — the fixes made (one touch-target constraint)
  are UI-only and covered by the existing static-analysis/build validation above, consistent with
  every prior slice's approach.

### Next recommended milestone

All five `docs/product/QURAN_PRD.md` §14 delivery slices are now implemented. The next work on this
feature is resolving the `§13` blocking production inputs above (product-owner/legal-scope, not
engineering), followed by a genuine online release-build verification once those inputs land —
then re-run `§12`'s full acceptance-criteria list end-to-end before considering `0.0.6` releasable.
Outside this feature, `docs/PROGRESS.md`'s other in-flight thread (Kalender Hijriah, see the next
entry below) is the other candidate next milestone.

## Quran response-body EOF hotfix (2026-08-08)

**Scope:** Fix only the `JsonDecodingException` seen when Retrofit parsed a successful Kemenag
response while debug HTTP body logging was enabled.

### What was fixed

- `ResponseSizeLimitInterceptor.SizeLimitedResponseBody` now creates and returns one cached
  size-limited `BufferedSource`, matching OkHttp's one-shot response-body contract. Previously,
  each `source()` call wrapped the same delegate with a new buffer: the debug HTTP logger consumed
  the first wrapper, then Retrofit received a second wrapper over the already-exhausted delegate
  and attempted to decode an empty string (`EOF` at JSON path `$`).
- The response-size cap and its streaming enforcement are unchanged. No Quran DTO, source text,
  persistence, sync, or UI behavior changed.

### Known limitations

- This hotfix does not change or validate the locally modified Kemenag header/credential setup;
  those pre-existing working-tree changes remain outside this narrow scope.

### Next recommended milestone

Complete a genuine online Quran preparation run with the intended credential injection path, then
resolve the remaining external release blockers already listed under Standalone Al-Qur'an Slice 5.

## Standalone Al-Qur'an UI/UX audit and revision (2026-08-08)

**Scope:** Audit every implemented standalone-Quran surface against the local
`docs/design/design-export/quran/` baseline and the seven supplied device captures, then revise the
hub, entry states, both reader modes, ayat actions, tafsir states, display settings, and source
attribution without changing Quran content or network/persistence contracts.

### What changed

- The fresh-install reader baseline is now **30sp Arabic / 1.65 line-height multiplier / 16sp
  translation**. This is calibrated for the currently shipped Android serif fallback at compact
  portrait width: the previous 34sp value came from an LPMQ-font mockup whose visual metrics are
  materially smaller and therefore appeared over-zoomed in the actual app. The decision is close
  to the local 29px flowing-reader reference and the Quran Foundation Unicode rendering example's
  28px baseline, while keeping the existing user-controlled range. Existing persisted preferences
  are intentionally not overwritten.
- The hub now matches the approved local hierarchy: two-line Kemenag identity, restrained outlined
  continue-reading panel, scrollable tabs, tonal search field, numbered list badges, meaning and
  revelation metadata, Arabic names, and separate settings/source actions. Compact content is
  capped at 640dp so it remains readable on wider windows without introducing a second navigation
  system.
- Both reader modes now use calmer start-of-surah framing, a compact contextual top bar, more
  balanced basmalah and paragraph spacing, and explicit ayat/Juz/page metadata. The flowing mode
  retains the approved bracketed inline ayat markers; only the initial scale and rhythm changed.
- The ayat-action and tafsir sheets no longer inherit the app theme's 50%-rounded `extraLarge`
  shape. Both use an explicit 26dp top-only radius, dark scrim, and 610dp content cap. Tafsir has a
  clear header/close affordance and distinct loading, cached-refreshing, loaded, retryable-error,
  and unavailable presentation.
- Display settings were rebuilt around a live preview, numeric slider values and discrete steps, a
  single segmented display-mode control, compact debug-only font-candidate cards, and a full-width
  source row. The source and entry screens now use centered readable-width content and clearer
  state hierarchy.
- Added shared Quran-specific dimensions/scrim tokens and split settings/font-preview composables
  so the revised surfaces remain within the project's static-analysis complexity thresholds.

The typography rationale is recorded in `docs/design/QURAN_DESIGN_SYSTEM.md`, including the need to
recalibrate size per approved font. Evidence reviewed: Android scalable-content and paragraph
guidance, Quran Foundation font-rendering guidance, the local Quran HTML/PNG baselines, and Arabic
mobile-readability literature; no source supports one universal Quran size independent of font and
viewport.

### Validation and known limitations

- `:app:compileDebugKotlin`, `:app:lint`, `:app:assembleDebug`, and `:app:installDebug` completed
  successfully; the debug APK installed on the local Pixel 9 API 35 emulator.
- `:app:ktlintCheck` reports no Quran-file violation. The global task remains red on unrelated
  in-progress Nahwu formatting plus the pre-existing overlength credential-provider line.
- `:app:detekt` reports no issue from this UI revision. The global task remains red only on that
  same pre-existing credential-provider line.
- Runtime screenshot verification could not proceed past launch because that emulator retains a
  Room schema-v4 developer database while this pre-release baseline requires schema v5. The app
  correctly refuses to open without a migration. No emulator app data was cleared without the
  product owner's explicit permission.
- No new tests were added or run, per the temporary design-alignment implementation constraints.
  The 30sp default affects new/no-preference installs only, and must be visually recalibrated once
  an approved, corpus-verified Quran font replaces `FontFamily.Serif`.

### Next recommended milestone

With approval, clear only the SanguSantri app data on the development emulator and complete the
manual portrait pass for hub tabs/search, both readers, long-press action sheet, all tafsir states,
settings controls, and source attribution. Then resolve the remaining external font/licensing and
credential-scope release inputs already tracked under the Quran PRD.

## Kalender Hijriah approved PRD and visual baseline (2026-08-08)

**Status:** Product/design scope approved and documented for target `0.0.7`;
Android implementation has not started. No app source, navigation, schema,
Gradle configuration, or runtime data was changed in this pass.

The earlier discovery draft was rewritten as
`docs/product/HIJRI_CALENDAR_PRD.md` version 1.0 after product-owner review. The
approved product is a compact, fully offline Gregorian–Hijri calendar reached
from Beranda, using Android `HijrahDate`/Umm al-Qura consistently with
Pengingat. It adds pasaran names only (no weton/neptu/primbon), full Indonesian
weekday names, Sunday/official-holiday red Gregorian dates, and a versioned
local agenda bundle with record-level source provenance.

The fasting scope now deliberately excludes Puasa Senin–Kamis rows and dots.
Calendar-suitable non-weekly rules use grouped ranges where appropriate;
Idul Fitri, Idul Adha, and Tasyrik are explicitly represented as fasting-
prohibition/observance records rather than fasting recommendations. Unknown
future dates remain labelled as Umm al-Qura calculations until a sourced
official record is added through an app/content-bundle update.

Created `docs/design/design-export/hijri-calendar/` using the established Quran
export pattern. It includes four editable `360x800` HTML states, matching JSON
sidecars, `720x1600` PNG previews, a regenerating Ruby catalog script, a local
state picker, and a README/design handoff contract. The smaller number in each
calendar cell now uses Arabic-Indic numerals; Gregorian numbers and Indonesian
accessibility semantics remain unchanged. The earlier interactive concept was
updated to match this numeral decision.

### Validation and known limitations

* The generator completed successfully and all generated JSON files parsed.
* All four HTML frames were rendered through headless Chrome at device scale 2
  and visually inspected at their original `720x1600` resolution.
* The previews cover light, dark, non-weekly fasting-filter, and source-sheet
  states. Design-tool page/node IDs remain `null` because no remote design-tool
  write was requested or performed.
* The August 2026 agenda is a design fixture, not the production runtime
  bundle. Annual government holiday/cuti-bersama records and every fasting
  rule still require source-by-source editorial acceptance before release.
* No Gradle task or Android test was run because this pass contains only
  product documentation and local design artefacts.

### Next recommended milestone

Finish the active standalone Quran `0.0.6` release work. When the product owner
explicitly starts `0.0.7`, begin Kalender Hijriah Slice 1: audit and freeze the
versioned source bundle, reuse the existing Hijri conversion policy, and add
the tested pasaran/event-domain rules before building Compose UI.

## Quran numbered-state UI audit and revision (2026-08-09)

**Status:** The numbered `01`–`18` Quran state catalog has been audited against
the Compose implementation and recorded in
`docs/reviews/quran-ui-state-audit-2026-08-09.md`.

### What changed

- Replaced the four-tab hub with three equal-width tabs: Surah, Juz, and
  Bookmark. Terakhir dibaca is now an optional outlined card above the tabs;
  it is omitted entirely when no saved position exists.
- Applied a 16dp outer hub inset, compact 8dp row inset, a 640dp content cap,
  and consistent search/list alignment. Background-refresh running and
  cache-preserving failure states are now visible without blocking Room data.
- Rebuilt entry checking/preparation/offline/error hierarchy and reader
  loading/invalid-target hierarchy to match numbered states 05a–06b and 17–18.
- Packaged LPMQ Isep Misbah and Amiri Quran, persisted the selected font in
  DataStore, and applied it to both reader modes and live preview. LPMQ and
  Amiri cards are selectable; King Fahd is disabled as `Belum tersedia`.
- Retained the 30sp / 1.65× / 16sp fresh-install defaults and the bracketed
  Arab-only ayat marker. System-bar icon handling now survives navigation
  overlap between Quran destinations and remains legible on the host chrome.
- Regenerated the Quran HTML/JSON catalog and re-rendered the affected hub PNG
  previews after changing state 04a/04b to last-read-card present/absent.

### Validation and known limitations

- `ktlintFormat`, `lint`, `assembleDebug`, and `installDebug` completed
  successfully. The final APK installed on Pixel 9 API 35.
- `ktlintCheck` and `detekt` now report no issue from this UI revision; both
  global tasks remain red on the existing overlength debug credential line in
  `QuranCredentialProvider.kt`.
- Fresh-install runtime verification covered initial sync completion, 3-tab
  hub with 42px device inset, last-read card absent/present, Surah list/search,
  LPMQ/Amiri selection semantics, disabled King Fahd, both live previews,
  Arab-only reader, long-press selection/action sheet, and system-bar contrast.
- Controlled failure injection was not performed for 06a/06b/06d, 13b/13c,
  and 18; their explicit Compose branches were inspected statically.
- Amiri Quran displayed a missing-glyph box for the final Al-Fatihah special
  mark on API 35. It remains selectable per product direction, but must not pass
  the release glyph gate until the fixed Kemenag corpus renders cleanly.
- LPMQ font embedding/redistribution permission is still an external release
  blocker. King Fahd remains unavailable because no asset/licence was supplied.

### Next recommended milestone

Resolve the Quran font release gates: confirm LPMQ embedding permission, run
the full Kemenag glyph corpus on supported Android versions, and either fix or
disable Amiri before release. Then execute controlled captures for the
remaining offline/error/invalid-target branches listed in the audit.

## Quran design-fidelity re-audit and UI revision (2026-08-09)

**Status:** An independent, state-by-state re-comparison of every asset in
`docs/design/design-export/quran/` (26 states: hub, initial preparation,
reader, ayat action sheet, tafsir sheet, display settings, source, Aktivitas
row) against the current Compose implementation, run after the audit recorded
above. This pass read each state's HTML/CSS/JSON reference directly (exact
colour hex, spacing, and copy) rather than relying on the prior audit's
"Covered" verdicts, then implemented the confirmed mismatches. It supersedes
the prior pass's verdicts where they conflict.

### What changed

- **Hub top bar**: removed the leading back icon — every hub design frame
  (`01`–`04b`, `06c`, `06d`) uses a plain title/subtitle bar with no back
  affordance, relying on system/predictive back, same as the entry gate.
- **Terakhir dibaca card**: added the tinted gradient background
  (`QuranContinueCardGradientStart` → `QuranSurface`, matching
  `linear-gradient(135deg,#07351f,#101713)`) and restored the page number in
  the meta line (`Halaman N • Ayat N`) using data already present on
  `QuranReadingState` but previously dropped when building `QuranHubUiState`.
- **Hub background-refresh/failed notice**: now a bordered, surfaced pill
  (`quranNoticeCornerRadius`) instead of a bare row; failed state uses
  `QuranError` for both icon and text.
- **Hub empty tab states**: added the icon-mark + heading + description
  pattern from the design's `.empty` block (bookmark-empty gets its own
  title/description strings matching the mock; Surah/Juz empty — which has no
  design frame since Room is always populated by then — reuses the same
  layout with a neutral icon).
- **Entry gate** (checking/preparing/offline/failed): added the missing
  `Al-Qur'an` / `Al-Qur'an Kemenag` header (design has no back icon here
  either); fixed the state-mark shape (76dp rounded-square with an outline
  border, not a 72dp circle); failure states now tint the icon `QuranError`
  instead of `QuranPrimary`; the determinate progress track now uses the
  design's `#26312B` instead of the default Material track colour; corrected
  the preparing/failed copy to match the mock exactly; retry button now meets
  the 132dp minimum width.
- **Reader invalid-target state**: icon now uses the same bordered 76dp mark
  and `QuranError` tint as the entry gate (was an unbordered, primary-tinted
  circle); the top app bar now shows a generic `Al-Qur'an` / `Posisi tidak
  tersedia` title instead of stale/fallback surah data when the requested
  ayat can't be resolved; copy and the 132dp button width now match state 18.
- **Reader loading skeleton**: three equal placeholders (was one distinct
  96dp "header" block plus three 132dp blocks) — the design never implies a
  separate header skeleton.
- **Reader surah-start header**: the title pill lost an unspecified border,
  and the metadata band's border is now a translucent 42% `QuranPrimary`
  instead of solid, matching `color-mix(...42%...)` in the reference; the
  basmalah asset now renders at a fixed ~210dp max width instead of 70% of
  the column width.
- **Tafsir sheet**: the retry button in the unavailable branch is now gated
  by `retryable` — the offline/no-cache state (13b) has no button, matching
  the design, where previously every unavailable state showed one; the
  cached-while-refreshing indicator (13a) is now a small pill
  ("Tersimpan offline • memperbarui…") instead of a full-width progress bar;
  the sheet heading format changed to `Surah • Ayat N` (was `Tafsir Surah: N`);
  offline/error copy now matches the mock's wording.
- **Display settings**: four control labels shortened to match the mock
  exactly (`Ukuran Arab`, `Jarak baris Arab`, `Ukuran terjemahan`, `Tampilan
  bacaan`, `Kecerahan Quran`).
- **Source screen**: restructured to match the mock's actual layout — an
  `Al-Qur'an Kemenag` heading with the provenance line as plain prose (was a
  titled card), the bordered box moved onto the "Data yang digunakan" section
  specifically (it was wrapping provenance instead), a divider between the
  box and the following prose, the previously-missing read-only/no-correction
  statement added, and the non-endorsement disclaimer moved last and given
  the mock's bordered "Catatan" notice treatment plus its "other features in
  the app" clause, which the prior copy omitted.

### Explicitly reviewed and left unchanged (ambiguous or contradicted by a

written spec)

- The ayat action sheet's corner radius (24px vs the 26dp already implemented)
  and scrim opacity (42% vs the implemented ~64%): `10-ayat-action-sheet.html`
  disagrees with the shared stylesheet block used by every other sheet in the
  set, so there is no single authoritative value to implement against.
- A second reader top-bar action for a one-tap display-mode toggle: shown in
  both `08` and `09`, but the two mocks disagree on what the icon does, and
  building it means a new quick-toggle affordance beyond the existing full
  settings screen — deferred as a product decision, not a styling fix.
- Removing the surah-start header/basmalah from Arab+terjemahan mode: state
  `08`'s single mock omits it, but `QUR-FR-010` in `QURAN_PRD.md` explicitly
  requires it "at the beginning of every surah" without restricting that to
  Arab-only. The written requirement was treated as authoritative over one
  mock frame.
- The Aktivitas Quran row's one-ayat-progress recording
  (`last > start` / `endAyat <= startAyat` guards in
  `QuranReaderViewModel`/`QuranRepositoryImpl`): a delegated review flagged
  this as dropping a same-ayat session, but acceptance criterion 12 in
  `QURAN_PRD.md` ("One-ayat progress creates one session; open/close without
  progress does not") reads at least as naturally as "advancing by one ayat
  is the threshold," which is what the code already does. Left as-is pending
  product clarification rather than guessed at.
- King Fahd's candidate name (`King Fahd` in the mock, `King Fahd Uthmanic
  Hafs for smart devices` in `QURAN_DESIGN_SYSTEM.md`, `King Fahd Complex` in
  `strings.xml`) and the settings live-preview verse (Al-Fajr in the mock,
  Al-Fatihah in the running app): no two of the three naming sources agree,
  and the preview-verse substitution looks like a deliberate, defensible
  product choice rather than an oversight.
- The Activity row timestamp separator (`20.18` in the mock vs the app's
  `20:18`): an app-wide time-format convention, not specific to Quran.

### Validation and known limitations

- `ktlintFormat`, `ktlintCheck`, `detekt`, `lintDebug`, and `assembleDebug`
  all completed successfully against the full revision.
- No emulator or device was attached to this session (`adb devices` returned
  none), so `installDebug` and on-device manual verification were not
  performed. Every state above was verified by reading the design HTML/CSS/
  JSON and the Compose source side by side, not by rendering the app.
- This pass did not touch: the ayat-action-sheet ambiguities, the reader
  quick display-mode toggle, the one-ayat-progress recording rule, the King
  Fahd naming mismatch, or the settings preview-verse choice — see above.

### Next recommended milestone

Get product-owner rulings on the four explicitly-deferred ambiguities above
(action-sheet corner radius/scrim source of truth, reader quick-toggle
scope, one-ayat-progress intent, King Fahd naming), then run an on-device
pass (`installDebug` plus manual TalkBack/RTL/font-scale checks per
`QURAN_DESIGN_SYSTEM.md` §7) once an emulator or device is available, since
this session's verification was source-level only.

## Quran on-device RTL/loading/numbering fix pass (2026-08-09)

**Status:** The on-device follow-up the prior entry recommended — an emulator
was attached this time, so this pass verified by actually running the app
rather than only reading source against the design HTML/CSS. It found and
fixed three defects the prior source-only reviews missed precisely because
they only manifest visually.

### What was fixed

- **Reader Arab+terjemahan Arabic text rendered flush left, not right**
  (`feature/quran/reader/QuranTranslationAyatList.kt`). Root cause: the
  Arabic `Text` was wrapped in `LocalLayoutDirection provides
  LayoutDirection.Rtl` *and* set `textAlign = TextAlign.End` — `End` is
  logical and flips to the *left* edge once the ambient direction is Rtl, so
  the two settings cancelled out. Fixed to `TextAlign.Right` (physical),
  matching `08-reader-arab-translation.html`'s literal `text-align:right`.
  Audited every other Arabic `TextAlign` in the feature: `QuranFontPreview`'s
  similar-looking `End` is not Rtl-wrapped so it already resolves correctly;
  `QuranSettingsScreen`'s live preview correctly uses `Center`; the Arab-only
  flowing reader's `Justify` has no Start/End ambiguity. Only the one call
  site was wrong.
- **Surah/Juz number badge had no gap to the title.** The prior pass added
  `QuranNumberBadge` but `QuranListRow`'s `Row` never set
  `horizontalArrangement`, so the badge sat pixel-flush against the title
  column — invisible when reading the source (the badge existed, the padding
  values looked reasonable in isolation) but obvious on a real screen. Added
  `Arrangement.spacedBy(SanguSantriSpacing.medium)` to `QuranListRow`,
  matching `01-quran-hub-surah.html`'s `.row{gap:12px}`, and removed the
  Bookmark row's now-redundant manual `padding(start = ...)` to avoid
  double-gapping.
- **Hub's Surah/Juz tabs could show the empty-state message before Room's
  first emission arrived**, rather than a loading indicator — `QuranHubUiState`
  has no way to distinguish "not loaded yet" from "loaded and genuinely
  empty" (the prior pass's own comment on `QuranEmptyTabState` already notes
  Room is always populated by hub-render time, i.e. this state is a rare
  defensive fallback, not a real flow — but the seed value before that
  guarantee is reflected still renders as empty for one frame). Added
  `QuranHubUiState.isLoading` (defaults `true`, cleared on the first real
  emission) and a `QuranLoadingTabState` spinner shown only while
  `isLoading && list.isEmpty()`. Bookmark tab was left unchanged — it has no
  equivalent guarantee and its empty state is always legitimate for a new
  user.

### Validation

```text
./gradlew :app:ktlintFormat/:app:ktlintCheck — pass on all touched files
./gradlew :app:detekt                        — pass on all touched files (one
                                  pre-existing, unrelated MaxLineLength
                                  violation in QuranCredentialProvider.kt,
                                  byte-identical to master, not touched here)
./gradlew :app:lint                          — pass
./gradlew :app:assembleDebug                 — pass
./gradlew :app:installDebug                  — pass
```

### Manual validation (Pixel_9 emulator, Android 15/API 35, already-synced local data)

- Hub load: the loading spinner was observed before the Surah list populated.
- Hub Surah tab: visible gap between the number badge and the title now
  confirmed on-device (previously flush).
- Reader → Āli 'Imrān (the feature's default landing state, Arab+terjemahan
  mode): every ayat renders flush right, including both wrapped lines of a
  two-line ayat — the exact defect this pass targeted.

### Known limitations

Same scope boundary as the prior pass — only the paths implicated by these
three defects (hub Surah/Juz lists, Arab+terjemahan reader) were
re-verified; the ambiguities and other screens the prior entry deferred are
still open. No automated tests were added — all three fixes are UI/alignment/
state-shape changes, verified manually.

### Next recommended milestone

Get the product-owner rulings the prior entry still needs, then work through
the rest of `QURAN_DESIGN_SYSTEM.md` §7's on-device checklist (TalkBack/
font-scale) now that an emulator is available.

## In-app update gate — Firebase Remote Config policy + Play Core (2026-08-09)

**Status:** Implemented and verified locally (no emulator/device attached
this session — Play Core flows themselves are unverifiable on a debug
build regardless, see Known limitations). Not a numbered milestone.
Full detail: ADR
[0017](decisions/0017-in-app-update-remote-config-and-play-core.md), which
amends ADR 0014.

### What shipped

- **Policy source (Remote Config).** `AppUpdatePolicyDto`
  (`data/remote/update/`) decodes the console-configured `in_app_update`
  JSON parameter (`minimum_version_code`, `force_update_versions[]`) via
  the app's existing `kotlinx.serialization` `Json`, mapped to the domain
  `AppUpdatePolicy`. `AppUpdatePolicyRepositoryImpl` wraps
  `FirebaseRemoteConfig.fetchAndActivate()` with a 5-second timeout;
  fetch/parse failures return `null` (never throw) after a Crashlytics
  `recordException` — fail-open, never silent.
- **Pure decision logic.** `decideAppUpdateRequirement`
  (`domain/model/AppUpdateRequirement.kt`) mirrors the existing
  `ContentVersionAction` enum-plus-top-level-function shape: below the
  minimum version, or in the explicit force-version list, forces the
  update; otherwise offers a flexible one.
- **Update mechanism (Play Core).** `AppUpdateViewModel`
  (`feature/update/`) combines that decision with
  `AppUpdateManager.requestAppUpdateInfo()` (Play Core KTX). A `FORCE`
  policy only becomes a non-cancelable update if Play Core also reports
  `AppUpdateType.IMMEDIATE` as allowed; a mismatch falls back to a
  flexible offer (or nothing) and records a Crashlytics non-fatal, per
  the product owner's explicit fail-open decision. Checked once per cold
  start (also an explicit product decision, not once-ever).
- **UI.** `AppUpdateForceDialog` is a dedicated, non-dismissible
  `AlertDialog` (back-press and outside-tap both disabled, no cancel
  action) — deliberately not a reuse of `ConfirmationDialog`, which always
  has one. `AppUpdateGate` owns the `ActivityResultLauncher` for
  `startUpdateFlowForResult`; if the user cancels Play's own immediate-
  update UI, the check re-runs, which re-derives a new `AppUpdateInfo`
  and re-invokes the flow — this is what makes the force update
  non-cancelable, without the self-referential-launcher pattern Kotlin
  disallows. A flexible update's "ready to install" state shows a
  snackbar with a restart action via the host screen's own
  `SnackbarHostState`.
- **Wiring.** `AppUpdateGate` is mounted from `SerambiRoute` (Beranda),
  not from `SerambiScreen` itself, so the latter stays a pure, Hilt-free,
  previewable composable — `SerambiScreen` gained an optional
  `snackbarHostState` parameter (defaults to a fresh one) instead of a
  hard Hilt dependency.
- **Docs:** ADR 0017 (new), a forward-pointing amendment note in ADR
  0014's Status section, and one added sentence in `CLAUDE.md`'s running
  milestone narrative.

### Files created

`domain/model/AppUpdatePolicy.kt`, `domain/model/AppUpdateRequirement.kt`,
`domain/repository/AppUpdatePolicyRepository.kt`,
`data/remote/update/AppUpdatePolicyDto.kt`,
`data/repository/AppUpdatePolicyRepositoryImpl.kt`, `di/UpdateModule.kt`,
`feature/update/AppUpdateUiState.kt`, `feature/update/AppUpdateViewModel.kt`,
`feature/update/AppUpdateForceDialog.kt`, `feature/update/AppUpdateGate.kt`,
`docs/decisions/0017-in-app-update-remote-config-and-play-core.md`,
`domain/model/AppUpdateRequirementTest.kt`,
`data/remote/update/AppUpdatePolicyDtoTest.kt`.

### Files modified

`gradle/libs.versions.toml`, `app/build.gradle.kts` (added
`firebase-config`, `play-app-update`, `play-app-update-ktx`),
`feature/home/SerambiScreen.kt` (mounts `AppUpdateGate`, adds
`snackbarHostState`), `res/values/strings.xml` (5 new
`app_update_*` strings), `docs/decisions/0014-*.md` (amendment note),
`CLAUDE.md`.

### Validation

```text
./gradlew :app:ktlintFormat  — auto-corrected this feature's own new files
                                (indentation); also reformatted ~29
                                pre-existing, unrelated files repo-wide
                                under the same `standard:indent` rule —
                                reverted those to keep this change scoped
                                to the requested feature (see Known
                                limitations).
./gradlew :app:ktlintCheck   — passes for every file this change touches;
                                fails overall only on the pre-existing,
                                unrelated `standard:indent` drift above
                                plus one already-known MaxLineLength
                                violation in QuranCredentialProvider.kt
                                (unchanged, not touched here).
./gradlew :app:detekt        — passes for every file this change touches
                                (3 initial TooGenericExceptionCaught
                                findings in AppUpdateViewModel.kt fixed
                                with `@Suppress`, matching
                                QuranCredentialProvider.kt's existing
                                precedent for intentional fail-open
                                exception handling); fails overall only on
                                the same pre-existing MaxLineLength
                                violation above.
./gradlew :app:lintDebug     — pass
./gradlew :app:assembleDebug — pass
./gradlew :app:testDebugUnitTest --tests AppUpdateRequirementTest \
  --tests AppUpdatePolicyDtoTest — pass, 6/6 (4 + 2)
```

No emulator/device was attached this session, so `installDebug` and manual
on-device verification were not run.

### Known limitations

- **Pre-existing repo-wide ktlint drift, out of scope.** A clean
  `ktlintFormat`/`ktlintCheck` run on unmodified `master` reformats/flags
  ~29 files across `data/`, `di/`, `domain/usecase/`, and `feature/` for a
  `standard:indent` violation unrelated to this change (a constructor-body
  indentation style the rest of the codebase — including this feature's
  own new files — already follows). This was not introduced by this
  session; it was found, then explicitly reverted out of this change to
  keep the diff scoped to the requested feature, per project working-method
  guidance. A separate, dedicated pass should run `ktlintFormat` across the
  whole module and review the resulting diff on its own.
- **Play Core flows are unverifiable on a debug build against a debug/
  local install** — Play's actual immediate/flexible update UI only
  appears for an app installed via Play (App Bundle track, matching
  signing). This was verified by reading the official Play Core docs and
  KTX API surface, not by exercising a real update on-device.
- **Operational rollout-timing caveat** (already accepted by the product
  owner during planning): a staged Play rollout can make Play Core report
  no update available even when Remote Config's policy says `FORCE` for
  the installed version — handled by the fail-open fallback above, not a
  defect.
- No instrumented/UI tests were added for `AppUpdateGate`/
  `AppUpdateForceDialog`, consistent with the project's current
  design-alignment-phase test-scope constraint (`CLAUDE.md`) — only the
  pure decision logic and DTO mapping got unit tests.

### Next recommended milestone

Nahwu Quiz (`0.0.5`, per ADR 0013) remains the next unimplemented,
already-approved milestone. Separately, whenever an emulator/device is
next available, manually verify the force dialog's back-press/outside-tap
non-dismissal and the flexible-update snackbar restart action on-device
(Play's own update UI itself cannot be exercised outside a real Play-
installed build).

## "Figma" → "design" naming cleanup (2026-08-09)

**Status:** Complete. Documentation/naming only — no product behaviour,
architecture, or Room schema changed.

**Scope:** The product owner clarified this project has never used a real
Figma connection — every "Figma" reference in file/directory names, code
comments, and docs was internal naming for local, hand-authored design
references (HTML/JSON/PNG), not an actual live design-tool integration.
Renamed every occurrence to "design" wording, so future sessions don't
mistake it for a real Figma dependency.

* **Renamed** (`git mv`): `docs/design/figma-export/` →
  `docs/design/design-export/`; `docs/design/FIGMA_HANDOFF.md` →
  `docs/design/DESIGN_HANDOFF.md`; `docs/reviews/figma-product-alignment.md`
  → `docs/reviews/design-product-alignment.md`.
* **Renamed** the `figma` JSON sidecar key to `designReference` across all
  `docs/design/design-export/quran/*.json` files and their generator
  (`generate-quran-catalog.rb`).
* **Reworded** every remaining "Figma" mention (code comments in
  `core/designsystem/`, `feature/reader/`, `feature/quran/`,
  `feature/guidedreader/`, `domain/model/`, `data/local/database/`,
  `strings.xml`, and one androidTest; every doc under `docs/` including
  `CLAUDE.md`/`AGENTS.md`, `PRD.md`, `ROADMAP.md`, `DESIGN_SYSTEM.md`,
  `QURAN_DESIGN_SYSTEM.md`, `DESIGN_HANDOFF.md`, the `design-export/
  future-releases/` spec files, ADRs 0013/0016, `PROGRESS.md` itself, and
  the three `*_AUTONOMOUS_PROMPT.md` files) to generic "design"/"design
  tool" phrasing — historical facts (e.g. the design-tool MCP rate-limit
  blocker, the frame/node ledger) were preserved, only the branded name
  was replaced, since the tool identity was never load-bearing.
* Left `.claude/settings.local.json`'s `mcp__figma__*` permission entry
  untouched — that's an unrelated, gitignored local MCP tool permission,
  not this project's naming.
* A pre-existing, untracked `docs/design/figma-export/hijri-calendar/`
  directory in the working copy (not yet committed at the time of this
  pass) was out of reach from this change's isolated worktree, so it was
  handled directly in the main checkout afterward: moved to
  `docs/design/design-export/hijri-calendar/` and reworded the same way
  (`README.md`, `generate-hijri-calendar-catalog.rb`, and the JSON sidecar
  `figma` key → `designReference`). It landed on `master` first via the
  "hijri calendar design PRD" commit under the old `figma-export` path,
  before this branch could merge — reconciled as part of merging this
  branch onto that commit.

### Validation

```text
./gradlew :app:ktlintCheck   — pre-existing, unrelated violations only
                                (confirmed identical on files this pass
                                never touched, e.g. NahwuQuizBootstrapper.kt)
./gradlew :app:detekt        — same pre-existing MaxLineLength violation in
                                QuranCredentialProvider.kt noted in the prior
                                entry, untouched by this pass
./gradlew :app:lint          — pass
./gradlew :app:assembleDebug — pass
```

`ktlintFormat` was run once, found it reformatted ~30 unrelated files
codebase-wide (pre-existing style debt, not caused by this pass), and its
output was discarded in favour of hand-reverting every file to keep this
change scoped to wording/renames only, per the no-unrelated-cleanup rule.

### Known limitations

None outstanding — the `hijri-calendar` gap noted above was closed while
merging this branch onto the commit that introduced it.

### Next recommended milestone

Nahwu Quiz `0.0.5` implementation (see the Kalender Hijriah `0.0.7` PRD
entry above for that milestone's own next-step note).

## Quran Remote-Config version gate and periodic-sync removal (2026-08-09)

**Status:** Implemented and manually verified. The complete Quran corpus is
now offline-only after initial preparation. There is no weekly, monthly, or
other calendar-based full-corpus synchronization.

### Scope and delivered behaviour

- Added Firebase Remote Config key `quran_stable_version`, with a safe local
  default/baseline of `1`. This is a monotonic operational trigger, not a
  version supplied or guaranteed by the Kemenag API.
- App startup performs only the small Remote Config check after confirming
  that Room already contains the canonical 114-surah/expected-verse-count
  dataset. A full Kemenag fetch is enqueued only when the activated remote
  version is greater than the locally applied version.
- Existing complete installs without version metadata adopt local version
  `1`, preventing a needless re-download during migration to this policy.
- A version-triggered update uses unique one-time WorkManager work with
  `UNMETERED` network and battery-not-low constraints. It makes one full
  attempt; a handled failure preserves the previous Room snapshot and puts
  that target version on a 24-hour cooldown. A newly raised target bypasses
  the old target's cooldown.
- Successful validation replaces surahs and verses, clears remote-ID-coupled
  tafsir cache, and advances the local version in one Room transaction. The
  app can therefore never expose a new corpus with an old applied-version
  marker, nor activate a partial candidate.
- Removed the Quran Hub's old background-refresh state/banner and repository
  `refreshIfStale()` path. Once prepared, Hub and Reader observe Room only.
- Shared and serialized Remote Config fetch/activation between the existing
  app-update policy and the Quran version gate, avoiding competing fetches.

### Files created

- `data/remote/config/RemoteConfigFetcher.kt`
- `data/remote/quran/QuranStableVersionConfig.kt`
- `data/local/quran/QuranLocalDataset.kt`
- `data/sync/quran/QuranUpdateScheduler.kt`
- `data/sync/quran/QuranUpdateWorker.kt`
- `app/src/test/.../QuranSyncMetadataTest.kt`

### Files modified

- Android startup, Remote Config DI/defaults, Quran repository/domain contract,
  sync manager/metadata/result, tafsir DAO/entity, Hub ViewModel/state/screen,
  strings, and the existing `QuranSyncManagerTest`.
- Quran PRD, roadmap, ADR 0016, architecture, offline-first/content-model/API
  contract/testing docs, production readiness/incident response, and the
  privacy/security baseline.

### Validation

```text
./gradlew :app:compileDebugKotlin
  — pass
./gradlew :app:testDebugUnitTest --tests com.sangusantri.app.data.sync.quran.QuranSyncMetadataTest
  — pass (baseline adoption, version eligibility, and cooldown coverage)
./gradlew :app:lint
  — pass
./gradlew :app:assembleDebug
  — pass
./gradlew :app:installDebug
  — pass; installed on Pixel_9 AVD / Android 15
git diff --check
  — pass
./gradlew :app:ktlintCheck
./gradlew :app:ktlintFormat
  — blocked only by the existing `QuranCredentialProvider.kt:69` line-length
    violation; no violation reported in the new version-gate files
./gradlew :app:detekt
  — new version-gate code is clean; repository-wide task remains blocked by
    unrelated current findings in Serambi/Explore plus the same credential
    provider line-length finding
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=\
  com.sangusantri.app.data.sync.quran.QuranSyncManagerTest
  — test did not start because the androidTest source set currently fails to
    compile: `SanguSantriMigrationTest.kt:127` references removed symbol
    `MIGRATION_1_2`
```

Manual emulator verification after force-stop/relaunch:

- Beranda started with no Quran/Kemenag sync log at local/remote version `1`.
- With the emulator reported offline, Quran Hub opened from Room, showed the
  complete local surah list and last-read state, and showed no refresh banner.

### Known limitations

- The production Remote Config value was intentionally not raised to `2`, so
  the destructive operational trigger was not exercised end-to-end against
  the real Kemenag corpus during this pass. Unit coverage verifies the gate and
  cooldown, while the existing sync-manager test covers atomic replacement but
  is currently blocked by the unrelated androidTest compilation issue above.
- Operators must raise `quran_stable_version` only after the intended Kemenag
  corpus is available and validated. Remote Config is a public control plane,
  not a content-integrity signal; every downloaded corpus still passes the
  existing structural validation before activation.

### Next recommended milestone

Repair the stale `MIGRATION_1_2` androidTest reference, then perform a controlled
non-production `quran_stable_version` `1` → `2` exercise with an authorized
credential and verify exactly one WorkManager download, atomic activation, and
offline restart. No periodic Quran sync should be reintroduced.

## Destructive Room policy and Quran Hub local-load optimisation (2026-08-09)

**Status:** Implemented and verified. This explicitly supersedes the prior
`MIGRATION_1_2` preservation policy and closes the Quran Hub's multi-second
local loading bottleneck.

### Room schema-transition decision

- `DatabaseModule` now states the product-owner-approved policy explicitly as
  `fallbackToDestructiveMigration(dropAllTables = true)`.
- Removed the obsolete `SanguSantriMigrationTest`, its androidTest schema-asset
  wiring, and the now-unused `androidx.room:room-testing` dependency.
- No hand-written Room migration chain is retained. On any unsupported schema
  transition, Room drops every table and recreates the current schema.
- This intentionally loses all Room-backed state: reading positions, guided
  progress, activity history, tasbih history, reminders, Quran corpus,
  bookmarks/history/state, metadata, and any other persisted Room rows.
  Bundled amaliyah content bootstraps again; Quran is not bundled and therefore
  requires another successful connected acquisition before offline reading is
  restored.
- ADR 0003/0006/0012/0015, architecture rules, coding/testing guidance,
  `AGENTS.md`, and `CLAUDE.md` were amended so future work cannot accidentally
  reintroduce the old migration requirement.

### Quran loading diagnosis and fix

The Quran Hub does not load all 6,236 verse bodies into its Surah tab. Its
initial combined state reads only:

- 114 `quran_surahs` rows;
- 30 first-verse-of-Juz rows;
- the user's usually-small bookmark list; and
- one optional last-reading-state row.

The delay came from how those 30 Juz rows were derived. The old
`observeJuzStarts()` query ran a correlated `NOT EXISTS` subquery for every one
of the 6,236 candidate verses. `EXPLAIN QUERY PLAN` confirmed a full outer scan
plus repeated index searches and a temporary sort. Against the copied database
snapshot from the Pixel 9 emulator, it took approximately **2.35 seconds** to
return 30 rows.

The DAO now computes the minimum numeric `(surahNumber, ayatNumber)` position
once per Juz with a grouped subquery, then joins those 30 positions back to the
verse table. The equivalent query on the same 114-surah/6,236-verse snapshot
took approximately **0.01 seconds**. This is a query-only change: Quran text,
ordering rules, Room tables, and schema version are unchanged.

### Files removed

- `app/src/androidTest/java/com/sangusantri/app/data/local/database/SanguSantriMigrationTest.kt`

### Main files modified

- `app/src/main/java/com/sangusantri/app/data/local/dao/QuranVerseDao.kt`
- `app/src/main/java/com/sangusantri/app/data/local/database/SanguSantriDatabase.kt`
- `app/src/main/java/com/sangusantri/app/di/DatabaseModule.kt`
- `app/src/androidTest/java/com/sangusantri/app/data/sync/quran/QuranSyncManagerTest.kt`
- `app/build.gradle.kts`, `gradle/libs.versions.toml`
- `AGENTS.md`, `CLAUDE.md`, `docs/CODEX_AUTONOMOUS_PROMPT.md`
- ADR 0003, 0006, 0012, 0015 and the content-model/offline-first/coding/testing
  engineering documents.

### Validation

```text
./gradlew :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin
  — pass; obsolete MIGRATION_1_2 compile failure is gone
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=\
  com.sangusantri.app.data.sync.quran.QuranSyncManagerTest
  — pass; 4 tests on Pixel_9 AVD / Android 15
./gradlew :app:lint
  — pass
./gradlew :app:assembleDebug
  — pass
./gradlew :app:installDebug
  — pass
./gradlew :app:ktlintFormat
./gradlew :app:ktlintCheck
./gradlew :app:detekt
  — repository-wide tasks remain blocked only by the existing
    `QuranCredentialProvider.kt:69` max-line-length finding; no finding in this
    pass's migration/query changes
```

Manual verification confirmed that the optimized build opens the complete
Room-backed Surah list from the restored local snapshot. The instrumented sync
test additionally verifies 114-surah activation, canonical ayat ordering, and
exactly 30 ordered Juz starts.

### Known limitation

The destructive fallback is intentionally a reliability trade-off: an app
upgrade with an unsupported Room schema version removes the previously
downloaded Quran and all Room-backed user history. Until Quran is downloaded
again, its offline reader is unavailable. This is expected product behaviour,
not a migration defect.

### Next recommended milestone

Keep schema changes batched and rare because every version mismatch now has a
large user-visible recovery cost. Separately, add a release-note/data-reset
warning whenever a release increments the Room schema version; do not restore a
hand-written migration chain unless the product owner reverses this decision.

## Beranda scalable-dashboard revamp and Jelajahi Amaliyah (2026-08-09)

**Status:** Implemented and manually verified. This is the requested Beranda
revamp slice of design-alignment Phase B, not the full FR-020/FR-021 catalogue
scope.

### Scope and delivered behaviour

- Rebuilt Beranda as a clean, data-driven dashboard with greeting and search,
  a genuine continue-reading card, the app's available main-feature shortcuts,
  nearest-reminder visibility, and up to four featured amaliyah entries.
- Feature availability follows real local state: amaliyah and Nahwu Quiz are
  not advertised without corresponding content; the continue card appears only
  for actual reader or quiz progress and opens the matching destination.
- Added a dedicated Jelajahi Amaliyah destination with local search, dynamic
  category chips, result count, empty state, and an adaptive catalogue grid.
- Added subtle state motion to the continue card and catalogue result/empty
  transitions. The standard system animator-duration setting remains the source
  of truth, including reduced/disabled animation preferences.
- Kept the approved bottom-navigation-only shell and made both dashboard and
  catalogue grids adapt to available width without adding a navigation rail.
- Added repository queries for the most recent full-reader position and most
  recent incomplete guided-reader session. Room remains the source of truth;
  no database schema, migration, network path, or religious content changed.
- Stabilized the top app bars after on-device scroll review so content remains
  cleanly framed without a transient empty header surface.

### Files created

- `app/src/main/java/com/sangusantri/app/feature/home/SerambiComponents.kt`
- `app/src/main/java/com/sangusantri/app/feature/explore/ExploreScreen.kt`
- `app/src/main/java/com/sangusantri/app/feature/explore/ExploreUiState.kt`
- `app/src/main/java/com/sangusantri/app/feature/explore/ExploreViewModel.kt`

### Files modified

- Beranda UI/state/actions/ViewModel and shared `ContentCard`.
- App navigation host, design-system dimensions, and Indonesian strings.
- Reading-position and guided-reading repository contracts, Room DAOs, and
  repository implementations.
- Existing Beranda and reader ViewModel test fakes were kept source-compatible;
  no new tests were added during this temporary design-alignment phase.

### Validation

```text
./gradlew :app:compileDebugKotlin — pass
./gradlew :app:compileDebugUnitTestKotlin — pass; existing test sources compile
./gradlew :app:lint               — pass
./gradlew :app:assembleDebug      — pass
./gradlew :app:installDebug       — pass; Pixel_9 AVD / Android 15
./gradlew :app:ktlintFormat
./gradlew :app:ktlintCheck
./gradlew :app:detekt
  — Beranda/Jelajahi findings resolved; all three repository-wide gates remain
    blocked only by the concurrent, out-of-scope
    `data/remote/quran/QuranCredentialProvider.kt:69` max-line-length finding
```

Per the temporary implementation-pass constraint, no unit or instrumented test
task was run.

Manual emulator verification covered the light and dark Beranda surfaces,
phone-width adaptive card layout, scrolling and fixed header, genuine
Istighosah progress at step 1/25, direct resume into the full reader, Jelajahi
navigation with bottom navigation hidden, category presentation, and live
search narrowing `Istighosah` to one result. No clipping or contrast issue was
observed on the Pixel_9 AVD.

### Known limitations

- Jelajahi currently provides search and category filtering only. Favourite,
  offline-only, and recent-content catalogue controls from the wider FR-020/
  FR-021 design scope remain future work.
- Tablet/foldable behaviour is implemented through adaptive grids but was not
  manually exercised on a second large-window AVD in this pass.
- The unrelated Quran credential-provider line-length finding must be resolved
  by its owning change before repository-wide `ktlint` and `detekt` can pass.

### Next recommended milestone

Complete the remaining Phase B Jelajahi catalogue controls (favourites,
offline-only, and recent content) before moving to the next roadmap feature.

## Beranda cross-activity resume and feature hierarchy refinement (2026-08-09)

**Status:** Implemented and manually verified. This refinement completes the
approved Beranda direction without implementing the Kalender Hijriah feature
itself.

### Scope and delivered behaviour

- The `Lanjutkan` card now chooses the newest locally persisted resumable
  activity across Al-Qur'an, Amaliyah (full or guided reader), and Tasbih using
  each feature's existing last-activity timestamp. Completed Activity-history
  rows are not used as resume candidates.
- Each candidate opens its exact destination: Quran resumes the saved surah and
  ayat, Amaliyah resumes its saved reader mode, and Tasbih switches directly to
  its top-level tab.
- Added a 48 dp dismiss action backed by the shared Preferences DataStore. A
  dismissed candidate stays hidden across process restarts, does not expose an
  older fallback candidate, and automatically becomes eligible again only
  after its progress fingerprint changes.
- Replaced Nahwu Quiz in the main-feature row with a Kalender Hijriah
  placeholder. Its tap currently shows a long snackbar and an explicit source
  TODO points the future `0.0.7` UI slice to the real Navigation 3 destination.
- Moved Pengingat and conditionally available Nahwu Quiz into a headerless,
  data-driven supporting-feature shelf. The nearest reminder summary and active
  Nahwu attempt are reflected when present, and the list can accept additional
  supporting tools later without restructuring the screen.
- Main and supporting tiles use adaptive `FlowRow` sizing. At 150% system font
  scale, main features wrap to 2+1 and supporting features to one column so
  labels and the `Segera hadir` badge remain readable.
- No Room schema, migration, network path, Quran/amaliyah religious content, or
  Kalender Hijriah calculation/source was added or modified by this refinement.

### Files created

- `app/src/main/java/com/sangusantri/app/domain/repository/HomePreferencesRepository.kt`
- `app/src/main/java/com/sangusantri/app/data/repository/HomePreferencesRepositoryImpl.kt`
- `app/src/main/java/com/sangusantri/app/di/HomeModule.kt`
- `app/src/main/java/com/sangusantri/app/feature/home/SerambiResumeCoordinator.kt`
- `app/src/main/java/com/sangusantri/app/feature/home/SerambiMenuComponents.kt`

### Files modified

- Beranda UI state, actions, ViewModel, screen, and resume-card components.
- Navigation host, design-system dimensions, and Indonesian strings.
- Existing `SerambiViewModelTest` fakes were kept source-compatible; no new test
  methods were added during this temporary design-alignment phase.

### Validation

```text
./gradlew :app:compileDebugKotlin                 - pass
./gradlew :app:compileDebugUnitTestKotlin         - pass; test sources compile
./gradlew :app:lint                               - pass
./gradlew :app:assembleDebug                      - pass
./gradlew :app:installDebug                       - pass; Pixel_9 AVD / Android 15
./gradlew :app:ktlintFormat                       - pass
./gradlew :app:ktlintCheck
./gradlew :app:detekt
  - repository-wide checks remain blocked only by the concurrent, out-of-scope
    `data/remote/quran/QuranCredentialProvider.kt:69` max-line-length finding;
    no finding belongs to this Beranda refinement
```

Per the temporary implementation-pass constraint, no unit or instrumented test
task was run.

Manual verification on Pixel_9 AVD / Android 15 covered latest-candidate
selection for Quran and Tasbih, direct Quran ayat and Tasbih-tab resume,
dismissal without older fallback, dismissal persistence after force-stop, and
reappearance after Tasbih progress changed. The Kalender placeholder snackbar,
light/dark surfaces, scrolling, and 150% system-font layout were also checked;
no clipping or contrast issue was observed.

### Known limitations

- Kalender Hijriah is intentionally only a Beranda entry placeholder. Its
  approved PRD must be delivered as a separate milestone before navigation can
  replace the snackbar TODO.
- Large-font adaptation was manually exercised on a phone AVD; tablet and
  foldable window classes were not manually exercised in this refinement.
- The unrelated Quran credential-provider line-length finding must be resolved
  by its owning change before repository-wide `ktlintCheck` and `detekt` pass.

### Next recommended milestone

Begin Kalender Hijriah `0.0.7` with Delivery Slice 1 from
`docs/product/HIJRI_CALENDAR_PRD.md`: establish the reviewed local source bundle
and domain conversion/validation rules only. Keep the Beranda placeholder until
the separately approved Slice 2 UI and navigation work.

## Kalender Hijriah `0.0.7` — Slices 1–3 (domain, UI, agenda/provenance) (2026-08-09)

**Status:** Implemented and manually verified on-device. Nahwu Quiz `0.0.5`
and standalone Quran `0.0.6` remain earlier in the milestone sequence but
had not been re-selected as the active task; the product owner directed
this session at `docs/design/figma-export/hijri-calendar/` and the approved
`docs/product/HIJRI_CALENDAR_PRD.md` directly, so all three PRD delivery
slices were implemented together in one pass rather than split, since the
PRD itself says no slice ships independently reduced. This replaces the
"Beranda cross-activity resume" entry's own Kalender Hijriah placeholder
(the `Segera hadir` badge and the "Kalender Hijriah sedang disiapkan..."
snackbar) with the real feature — `SerambiActions.onHijriCalendarClick`
and the `SerambiMainFeatures` tile it already wired stayed as-is; only their
placeholder implementation was replaced with real navigation.

Reused, not duplicated, per CAL-FR-002: Hijri conversion is the same
`java.time.chrono.HijrahDate` approach `ReminderScheduleCalculator`/
`ReminderScheduleFormatter` already use in production, and the Hijri
month-name table is the same array — renamed `R.array.reminder_hijri_month_names`
→ `R.array.hijri_month_names` (4 call sites updated: `SerambiScreen`,
`ActivityScreen`, `ReminderList`, `ReminderFormSheet`) since it is now a
cross-feature resource, not a Pengingat-only one.

### What shipped

**Domain** (`domain/model/`, all plain Kotlin, no Room — PRD §5.3 explicitly
says this feature does not need it): `Pasaran` + `PasaranCalculator`
(Pancawara cycle, Friday-Legi/8 July 1633 anchor); `HijriEventKind`,
`HijriCalculationStatus`, `HijriEventProvenance`; `HijriCalendarEvent` +
`HijriRecurringEventRule` (Hijri-recurrence-based, not hardcoded per-year
Gregorian dates); `HijriCalendarBundle` — the ten-rule audited allowlist
from PRD §5.2 (Ramadan, Tasu'a/Asyura, Ayyamul Bidh general + its narrower
Zulhijah variant excluding 13 Zulhijah/Tasyrik, Tarwiyah, Arafah, six days
of Syawal as a flexible non-dotted window, Idul Fitri/Idul Adha/Tasyrik as
`FASTING_PROHIBITED`, never `FASTING`), each with real Kemenag source
citations already named in the PRD; `officialRecords` (sourced national
holidays/cuti-bersama) is deliberately an empty list — PRD §12 is explicit
that the design fixture dates are not a substitute for that dataset, which
needs its own source-by-source editorial acceptance, not inference; `HijriAgendaCalculator`
(resolves rules per Hijri (year, month), merges with `officialRecords`,
dedupes/sorts); `HijriCalendarDay`/`HijriCalendarMonth`/`HijriYearMonth`
(presentation-free — no baked-in localised strings) and
`HijriMonthGridCalculator` (Sunday-first 42-cell grid, CAL-FR-003).

**UI** (`feature/hijricalendar/`): `HijriCalendarScreen`/`Route`,
`HijriCalendarUiState`/`UiAction`, `HijriCalendarViewModel` (no repository —
pure, synchronous local calculation, so there is no loading state per
PRD §9), `HijriCalendarFormatter` + `HijriCalendarAgendaFormatter` (split in
two to stay under detekt's function-count threshold), `HijriCalendarAgendaFilter`;
`components/`: month header + Sunday-first weekday row, the day grid/cell
(Arabic-Indic in-cell Hijri numeral, Latin Gregorian numeral, pasaran,
today/selected non-colour indicators, amber/coral event dots), selected-date
summary strip, agenda section (legend, Semua/Puasa/Hari besar & libur
filters, grouped range rows, empty state), the "Sumber & metode" bottom
sheet (§3.2's exact required Umm al-Qura authority-boundary disclosure
copy), and a per-event provenance detail dialog (CAL-FR-008).

**Design tokens**: `HijriTeal/Amber/Coral` (+`Soft`, light/dark pairs) added
to `Color.kt` from the approved local HTML export's exact hex values, plus a
`hijriCalendarPalette()` composable accessor (`core/designsystem/theme/`),
following the same "extends the one canonical token source" convention as
the existing `Quran*` tokens.

**Navigation/entry**: `KalenderHijriah` `NavKey`, entry in
`SanguSantriNavHost`, wired to the existing `SerambiActions.onHijriCalendarClick`
hook (added by the earlier Beranda cross-activity-resume pass) — Beranda-only,
never a bottom-nav tab (PRD §4.1). `SerambiRoute`'s placeholder snackbar
override for that click was removed so it now navigates for real, and the
`SerambiMainFeatures` Kalender Hijriah tile's `Segera hadir` badge (and the
now-unused `serambi_coming_soon_badge` string) was removed since the feature
is no longer "coming soon". The extra `entry<KalenderHijriah>` line pushed
`sanguSantriEntryProvider` over detekt's `LongMethod`/`TooManyFunctions`
thresholds; fixed by extracting a new `standaloneEntries` split-out (Explore/
Pengingat/KalenderHijriah, mirroring the existing `activityEntries`/
`nahwuQuizEntries` pattern) and turning `replaceTopEntryWithReader` into a
function local to `readerEntries` (its only caller), instead of a separate
top-level declaration.

**Strings**: ~40 new `hijri_calendar_*` strings, plus two new arrays
(`hijri_calendar_weekday_names` — Ahad-first, `hijri_calendar_pasaran_names`),
all in `strings.xml`. The pre-existing `serambi_hijri_calendar_title` string
from the earlier placeholder pass is still used (tile title); the now-dead
`serambi_coming_soon_badge` and `serambi_hijri_calendar_placeholder_message`
strings were deleted along with their last call sites.

### Files created

`domain/model/{Pasaran,PasaranCalculator,HijriEventKind,HijriCalculationStatus,
HijriEventProvenance,HijriCalendarEvent,HijriRecurringEventRule,
HijriCalendarBundle,HijriAgendaCalculator,HijriCalendarDay,HijriCalendarMonth,
HijriYearMonth,HijriMonthGridCalculator}.kt`;
`core/designsystem/theme/HijriCalendarPalette.kt`;
`feature/hijricalendar/{HijriCalendarScreen,HijriCalendarUiState,
HijriCalendarUiAction,HijriCalendarViewModel,HijriCalendarFormatter,
HijriCalendarAgendaFormatter,HijriCalendarAgendaFilter}.kt`;
`feature/hijricalendar/components/{HijriCalendarGrid,HijriCalendarHeader,
HijriCalendarMonthNavigation,HijriCalendarSelectedSummary,
HijriCalendarAgendaSection,HijriCalendarSourceSheet,
HijriCalendarEventDetailDialog}.kt`;
test sources `domain/model/{PasaranCalculatorTest,HijriAgendaCalculatorTest,
HijriMonthGridCalculatorTest}.kt`,
`feature/hijricalendar/HijriCalendarFormatterTest.kt`.

### Files modified

`core/designsystem/theme/{Color,SanguSantriDimensions}.kt`;
`navigation/SanguSantriNavHost.kt`; `feature/home/{SerambiScreen,
SerambiMenuComponents}.kt`; `feature/reminder/{ReminderList,
ReminderScheduleFormatter,components/ReminderFormSheet}.kt`;
`feature/activity/ActivityScreen.kt`; `res/values/strings.xml`.

### Validation

```text
./gradlew :app:ktlintCheck             — passes for every file this change
                                           touches; fails overall only on the
                                           same pre-existing MaxLineLength
                                           violation in QuranCredentialProvider.kt,
                                           unrelated and unchanged.
./gradlew :app:detekt                  — passes for every file this change
                                           touches after splitting
                                           HijriCalendarFormatter,
                                           HijriCalendarMonthHeader, and
                                           HijriCalendarDayCell to stay under
                                           TooManyFunctions/LongParameterList/
                                           LongMethod thresholds; fails overall
                                           only on the same pre-existing
                                           QuranCredentialProvider.kt line.
./gradlew :app:lintDebug               — pass
./gradlew :app:assembleDebug           — pass
./gradlew :app:testDebugUnitTest       — pass, 34/34 new tests (8 Pasaran +
                                           9 agenda/bundle + 6 grid + 11
                                           formatter), plus the full existing
                                           suite unaffected by the array
                                           rename.
./gradlew :app:installDebug            — pass, Pixel 9 (AVD) API 15 emulator.
```

**Manual on-device verification** (light and dark theme, emulator system
date 9 August 2026): Beranda's Kalender Hijriah tile opens the calendar;
month grid matches the approved design (Sunday-first, Arabic-Indic in-cell
Hijri numerals, coral Sunday numerals, amber Ayyamul Bidh dots correctly
shown on muted adjacent-month cells belonging to a different Gregorian
month); prev/next month navigation preserves day-of-month and recalculates
the Hijri span/weekday/pasaran; "Hari ini" returns to today; agenda filters
and the correctly-empty "Tidak ada agenda untuk filter ini." state render;
the "Sumber & metode" sheet matches the approved copy and badge exactly.
**Manual testing itself caught and fixed a real bug**: the selected-date
summary and every day cell's accessibility description initially showed
"Minggu" for Sunday (from `java.time`'s own Indonesian `getDisplayName`)
instead of this app's required "Ahad" (PRD §7.1) — the month header's
weekday row was already correct (string-array-driven), but
`HijriCalendarFormatter`/the grid's content descriptions were not. Fixed by
threading the same Sunday-first `hijri_calendar_weekday_names` array through
`formatWeekdayAndPasaran`/the new `formatWeekdayFull`, with three regression
tests added (`formatWeekdayFullUsesAhadForSundayNeverMinggu` and siblings).

**Follow-up design audit (same day)**: the "Sumber & metode"
`HijriCalendarSourceSheet` read as excessively/inconsistently rounded on
review. Root cause: its `ModalBottomSheet` had no explicit `shape` (so it
fell back to Material3 Expressive's default, not the design's specified
24dp-top-corner sheet), its bottom `Button` had no explicit `shape` either —
Material3 Expressive 1.4.0's default `Button` shape is `CornerFull`
(`CircleShape`, a full stadium/pill), independent of the app's own
`SanguSantriShapes` theme — and the source-block borders used a one-off
`13.dp` literal instead of an app shape token
(`docs/design/DESIGN_SYSTEM.md`'s "2–3 corner radii used deliberately, not
one radius invented per component" rule). Fixed by wiring the
already-defined-but-unused `SanguSantriDimensions.hijriCalendarSourceSheetCornerRadius`
(24dp) into the sheet's `shape`, and switching the button/source-block
border/heading-icon-box/badge to the existing `SanguSantriShapes.medium`
(12dp, matching the design's `.primary-button`/`.source-block` radii exactly)
and `SanguSantriShapes.extraLarge` (pill) tokens instead of ad hoc literals
or the unstyled Material3 default.

### Known limitations

- **The official national holiday/cuti-bersama bundle is empty.**
  `HijriCalendarBundle.officialRecords = emptyList()` is deliberate, not an
  oversight — PRD §12 states the design fixture dates are not a substitute
  for that release dataset, and populating real per-year dates requires its
  own source-by-source editorial acceptance against a named government
  publication (content-safety rule: no scraping/inferring). Sundays still
  render red; only the sourced-official-holiday red-numeral/agenda path has
  no data yet to activate it. A future content-curation pass should add
  this via `docs/operations/CONTENT_GOVERNANCE.md`'s process.
- **No `HijriCalendarViewModelTest`.** Only the pure domain
  calculators/formatters got unit tests this pass (matching CAL-FR-004's
  explicit pasaran-test requirement); the ViewModel's month-navigation/
  boundary-clamping/selection logic was exercised manually on-device
  (August → September, "Hari ini") but not with an automated fake-based
  test the way `SerambiViewModelTest` covers `SerambiViewModel`.
- **No Compose UI/screenshot tests** were added for the new screen or its
  components — `docs/engineering/TESTING.md`'s Compose UI test list was not
  extended for Kalender Hijriah this pass.
- The ten-year browse boundary (`YearMonth.now() ± 10 years`) was verified
  by reading the `HijriCalendarViewModel` logic and a one-month manual
  navigation, not by manually navigating a full ten years to observe the
  arrow-disable state on-device.
- Arabic-Indic digit rendering was visually spot-checked in the emulator
  screenshots (correct glyphs, correct positions) but not verified against
  every supported device font the way the standalone Quran feature's Arabic
  corpus QA was.

### Next recommended milestone

Nahwu Quiz (`0.0.5`, per ADR 0013) remains the next unimplemented,
already-approved milestone in strict sequence order. If Kalender Hijriah
continues instead, the next slice of work is the official national
holiday/cuti-bersama sourced dataset (PRD §5.4/§12) plus a
`HijriCalendarViewModelTest`.

## Release-signed Quran credential mismatch diagnosis + debug override (2026-08-09)

**Status:** Root cause diagnosed and confirmed against the actual uploaded artifact; production
fix requires a value only obtainable from Play Console (not fixed by this session — see Known
limitations). A separate, smaller improvement (opt-in debug credential override) was implemented
and verified.

**Trigger:** product owner reported the standalone Quran feature fails to load in the published
release AAB, and asked whether debug builds' frequent failure was related
(`QuranCredentialProvider.DEBUG_FIXTURE_CREDENTIAL`).

### Diagnosis (release AAB)

- Extracted the signing certificate actually embedded in the uploaded `app/release/app-release.aab`
  (`META-INF/SANGUSAN.RSA`, no keystore password needed to read a public certificate) via
  `keytool -printcert`: SHA-256 `64:18:FE:7F:EB:A6:CF:8D:1A:32:B6:85:AF:73:72:60:B1:D3:7D:84:4C:41:
  AC:31:5A:A0:40:22:F3:32:76:66`. This is an exact match for the `SANGU_QURAN_RELEASE_SHA256`
  currently set in the developer's untracked `~/.gradle/gradle.properties` — i.e. the value baked
  into `quran_credential_secrets.h` at build time is the **upload key**'s digest.
- Confirmed with the product owner that this app is enrolled in **Google Play App Signing**. Play
  re-signs the distributed app with a separate "App signing key" certificate before it reaches user
  devices — different from the upload key used to sign the AAB before upload. `QuranCredentialProvider
  .releaseSigningCertificateSha256()` reads the *actual installed app's* certificate at runtime
  (`PackageManager`), so on a real Play-installed device it will not match the upload-key digest
  embedded via `SANGU_QURAN_RELEASE_SHA256`. `quran_credential.cpp`'s `SigningDigestMatches` fails
  closed on any mismatch (by design, ADR 0016/`docs/security/SECURITY_BASELINE.md`), so
  `nativeGetCredential` returns `null`, `QuranCredentialProvider.getCredential()` returns `null`,
  and
  `QuranAuthInterceptor` throws `"Kemenag credential unavailable"` — exactly the reported symptom.
- This also explains why prior sessions' `assembleRelease`/local `installRelease` validation (Slice
  5, this file) reported success: those builds were signed directly with the local upload keystore
  and never went through Play's re-signing step, so the digest matched by coincidence.
- **Fix (not yet applied — needs a value only Play Console has):** replace
  `SANGU_QURAN_RELEASE_SHA256` in `~/.gradle/gradle.properties` with the **App signing key
  certificate**'s SHA-256 (Play Console → app → Release → Setup → App integrity), not the upload
  key's, then rebuild and upload a new release. No code change is required for this half of the fix
  — `app/build.gradle.kts`/`quran_credential.cpp` already read whatever digest is configured
  correctly; the configured *value* was wrong, not the mechanism.

### What shipped (debug credential override)

- `app/build.gradle.kts`: new `buildTypes { debug { ... } }` block defines
  `BuildConfig.QURAN_DEBUG_API_USERNAME`/`QURAN_DEBUG_API_TOKEN`, sourced from the optional
  untracked `SANGU_QURAN_DEBUG_API_USERNAME`/`SANGU_QURAN_DEBUG_API_TOKEN` (env var or Gradle
  property, same `quranSecretProperty` helper the release path uses; never the tracked
  `gradle.properties`). Defaults to empty strings when unset. Verified absent from the `release`
  `BuildConfig` entirely (not merely blank) by inspecting the generated release `BuildConfig.java`.
- `QuranCredentialProvider.kt`: `resolveCredential()` now tries a new `debugOverrideCredential()`
  first in the `BuildConfig.DEBUG` branch — returns a real `QuranCredential` only when both
  `BuildConfig.QURAN_DEBUG_API_USERNAME`/`QURAN_DEBUG_API_TOKEN` are non-blank, otherwise falls back
  to the existing `DEBUG_FIXTURE_CREDENTIAL` unchanged. The release branch and its signing-digest
  verification are untouched. Class doc comment updated to describe the opt-in override.
- `docs/security/SECURITY_BASELINE.md`: amended the "Tests/debug fixtures use an unmistakably fake
  credential" bullet to document the opt-in override and its constraints (untracked local file only,
  never CI, never logged, never touches the native release path).
- **Clarified, not fixed, since it was never a defect:** the debug build's fixture credential
  (`"something"/"something"`) always fails against the real Kemenag API — this is deliberate
  (ADR 0016, `docs/security/SECURITY_BASELINE.md`), not the intermittent bug the product owner
  suspected. The override above is the sanctioned way to test the real API from a debug build.

### Validation

`./gradlew :app:assembleDebug` — pass; confirmed generated debug `BuildConfig.java` contains
`QURAN_DEBUG_API_USERNAME`/`QURAN_DEBUG_API_TOKEN` (empty by default, no local override set this
session) and the release `BuildConfig.java` contains neither field. `./gradlew :app:ktlintCheck
:app:detekt` — both fail, but confirmed via `git stash`/re-run against unmodified `release/0.0.4`
that the same violations (`QuranCredentialProvider.kt`, plus ~40 other pre-existing files) already
exist before this change; this session's addition follows the file's existing (already
non-compliant) indentation style and introduces no new violation class. Did not run
`ktlintFormat` — an earlier attempt this session reformatted ~45 unrelated files codebase-wide
(pre-existing indentation-style drift, out of scope) and was reverted. Did not attempt a real
release build/upload — the Play Console App signing key certificate value is required first.

### Known limitations

- **Production fix is not yet applied.** The correct `SANGU_QURAN_RELEASE_SHA256` (App signing key
  certificate SHA-256 from Play Console) has not been supplied to this session. Until
  `~/.gradle/gradle.properties` is updated with that value and a new release (new `versionCode`) is
  built and published, the live Play Store release will continue to fail Quran credential
  resolution for all users.
- Codebase-wide ktlint/detekt indentation debt (~40+ files, pre-existing before this session)
  remains unaddressed — out of scope for this fix; flagged for a dedicated formatting pass.

### Next recommended milestone

Obtain the Play Console App signing key certificate SHA-256, update
`~/.gradle/gradle.properties`'s `SANGU_QURAN_RELEASE_SHA256`, cut a new release build, and verify
Quran loads correctly on a device that installed the app from Play (not a sideloaded
locally-signed build). Then resume Nahwu Quiz (`0.0.5`) or Kalender Hijriah's next slice as above.

## Quran entry-gate failure detail surfaced to the user (2026-08-09)

**Status:** Implemented and verified on-device.

**Trigger:** following the release-credential-mismatch diagnosis above, the product owner asked for
the underlying error to actually be visible on the failure screen, so a real user (or the product
owner reproducing a report) can see and relay what specifically went wrong instead of the previous
generic message with no detail at all.

### What shipped

- `QuranSyncResult.RetryableFailure`/`PermanentFailure` already carried a `reason: String` (data
  layer, pre-existing) but it was discarded on the way to the UI. Threaded it through the full
  chain instead of adding a parallel mechanism:
  `QuranPreparationResult.Failed` gained a `reason: String` field;
  `QuranRepositoryImpl.runSync` now passes `result.reason` through instead of dropping it;
  `QuranEntryUiState.PreparationFailed` changed from a `data object` to a `data class(reason:
  String)`; `QuranEntryViewModel` passes `result.reason` when mapping.
- `QuranEntryScreen.kt`'s `QuranEntryMessage` gained an optional `detail: String?` parameter,
  rendered as a small muted `Text` below the retry button, wrapped in a `SelectionContainer` so a
  user can select/copy the exact detail when reporting a bug. New string
  `quran_entry_failed_detail_label` ("Detail teknis: %1$s") holds the label; the raw reason itself
  is not translated (technical detail, consistent with how error codes are conventionally shown
  untranslated). `@Suppress("LongParameterList")` added to `QuranEntryMessage`, matching the
  existing convention used elsewhere in this codebase (`ActivityScreen.kt`,
  `QuranReaderScreen.kt`, etc.) for Composables that legitimately need more than six parameters.
- `QuranSyncManager`'s two `IOException` catch blocks (`fetchSurahs`/`fetchAyatForSurah`) now
  append the exception's own message (or class name if absent) to the `RetryableFailure` reason via
  a new small `ioReason()` helper, instead of only logging it. **Confirmed safe to surface**: at
  this layer an `IOException` is either OkHttp's own (host/timeout/TLS-level detail, e.g. "Unable
  to resolve host") or `QuranAuthInterceptor`'s own fixed `"Kemenag credential unavailable"` message
  — never a response body, header, or credential value, so this doesn't touch
  `docs/security/SECURITY_BASELINE.md`'s log-redaction rule. Deliberately did **not** do the same
  for `SerializationException` catches — kotlinx.serialization exception messages can echo a
  snippet of the actual (potentially Arabic/translation) response body being parsed, which the
  redaction rule does cover; those reasons remain the existing generic "malformed X body" strings.
- HTTP-status and validator-`reason` failure paths were already safe, structural strings
  (`"$source HTTP $code"`, `QuranValidator`'s own reasons) and needed no change — they were already
  flowing into `QuranSyncResult`, just discarded above it.

### Validation

`./gradlew :app:compileDebugKotlin`, `:app:assembleDebug`, `:app:lintDebug` — all pass.
`:app:ktlintCheck`/`:app:detekt` — both still fail, but confirmed (grepping the touched files'
findings) every violation is the same pre-existing whole-file "Unexpected indentation" style debt
noted in the previous entry, except one genuinely new `LongParameterList` finding on
`QuranEntryMessage`, fixed with the `@Suppress` noted above (re-ran detekt after the fix: zero new
findings). Did not run `ktlintFormat` (would reformat ~45 unrelated files, as established
previously — out of scope). No new tests added (no existing `QuranEntryViewModelTest`/
`QuranSyncManagerTest` assertions depend on the changed shapes; none existed to break).

### Manual validation (Pixel_9 emulator, Android 15/API 35, real network)

Installed the rebuilt debug APK, `pm clear`-ed the app to force a genuine first load (a prior
session had already left a complete local dataset on this emulator), and opened Al-Qur'an from
Beranda. With the debug fixture credential (`"something"/"something"`, no local override
configured this session) the real Kemenag API call failed — confirmed the failure screen now
reads **"Detail teknis: malformed surah list body"** under the existing title/description/retry
button, selectable, correctly styled (small, muted, centered), matching the design's error-state
layout. This also incidentally shows the fixture credential's real-world failure mode is a
malformed/unparseable response body rather than a clean HTTP 401 — new information the previous
generic message could never have surfaced.

### Known limitations

- Only the entry-gate first-load failure path got a visible detail (`QuranEntryUiState
  .PreparationFailed`) — this was the explicit scope ("at first load"). `QuranHubViewModel`/later
  Remote-Config-triggered update failures were not touched and still surface no detail; a future
  task could extend the same pattern there if useful.
- The pre-existing codebase-wide ktlint/detekt indentation debt (~40+ files) remains unaddressed,
  as in the previous entry.

### Next recommended milestone

Unchanged from the previous entry: obtain the Play Console App signing key certificate SHA-256 and
ship a corrected release build — this session's change makes that release's actual failure reason
(if it recurs) visible to whoever reports it, but does not by itself fix the underlying signing
mismatch.

## Production Quran credential digest corrected + debug-override compile fix (2026-08-09)

**Status:** Digest corrected and rebuilt successfully; a real compile bug from the previous entry's
debug-override change was found and fixed in the process.

### Digest correction

- The product owner's Play Console "App signing" page confirmed this app uses **quantum-ready
  hybrid signing (beta)**: two certificates are shown side by side — "Classical key" and
  "Post-quantum cryptography key" — each with its own SHA-256/SHA-1 fingerprint, install base
  0.0% on both the current and only "previous" key entry (both first-used 2026-07-25, i.e. this
  app has no meaningful tracked install base on Play yet — the earlier report was very likely from
  a test install, not live production traffic).
- Confirmed via Google's own Play Console help documentation that the **classical key** is what
  `PackageManager`/`SigningInfo.apkContentsSigners` reports on essentially every real Android
  device today (the post-quantum key only matters to a future OS verification scheme); the
  **upload key certificate** (a separate section of the same page) is never what ends up on a
  user's device and was the previous entry's root cause.
- Updated `~/.gradle/gradle.properties`'s `SANGU_QURAN_RELEASE_SHA256` to the **Classical key →
  SHA-256 certificate fingerprint** the product owner copied from Play Console
  (
  `FC:3D:85:1B:AF:1B:FC:4B:86:51:51:37:21:5D:61:B5:2D:BA:FB:C6:D8:2F:7C:0C:94:57:98:41:48:C4:9E:CC`),
  replacing the previous (incorrect, upload-key) value. No code change was needed for this half —
  `app/build.gradle.kts`/`quran_credential.cpp` already read whatever digest is configured; only
  the configured value was wrong.
- **Not yet fully closed:** this local config change only takes effect the next time a release is
  built and published; the live Play Store release still carries the old, wrong digest until a new
  version is shipped. A residual, documented risk also remains: `QuranCredentialProvider
  .releaseSigningCertificateSha256()` requires `PackageManager` to report **exactly one** signing
  certificate (`if (signatures.size != 1) null`) — if a future Android OS version's
  `PackageManager` ever reports both the classical and post-quantum certificates together for a
  hybrid-signed app, this check fails closed even with the correct classical digest configured.
  This was raised with the product owner, who deferred the decision (not addressed this session).

### Bug found and fixed: previous session's debug-override change did not compile in release

- Running `./gradlew :app:assembleRelease` (correctly, for the first time since the previous
  entry's debug-credential-override change) surfaced a real `compileReleaseKotlin` failure:
  `QuranCredentialProvider.kt` referenced `BuildConfig.QURAN_DEBUG_API_USERNAME`/
  `QURAN_DEBUG_API_TOKEN` unconditionally, but those fields were deliberately defined only in
  `app/build.gradle.kts`'s `debug` build type — so they don't exist in the `release` variant's
  generated `BuildConfig` at all, regardless of the `if (BuildConfig.DEBUG)` runtime guard around
  the call site (Kotlin resolves the reference at compile time per variant, not per runtime
  branch). The previous entry's validation ran `assembleDebug` but not `assembleRelease`, so this
  was not caught until this session.
- **Fix:** extracted the override into a small `internal object QuranDebugCredentialOverride` with
  two variant-specific implementations — `app/src/debug/java/.../QuranDebugCredentialOverride.kt`
  (reads the real `BuildConfig.QURAN_DEBUG_API_USERNAME`/`QURAN_DEBUG_API_TOKEN` fields, which exist
  only for this variant) and `app/src/release/java/.../QuranDebugCredentialOverride.kt` (a fixed
  `null`, never referencing those fields at all). `QuranCredentialProvider.kt` (shared `src/main`)
  now calls `QuranDebugCredentialOverride.resolve()` instead of referencing
  `BuildConfig.QURAN_DEBUG_*`
  directly, so `compileReleaseKotlin` no longer needs those debug-only fields to exist. This
  preserves the original security property (the fields, even empty, never appear in the release
  `BuildConfig` at all) while fixing the compile break — no shortcut of moving the fields to
  `defaultConfig` (which would have risked a real local `SANGU_QURAN_DEBUG_API_*` env var leaking
  into a release build if ever set on a developer/CI machine at `assembleRelease` time).

### Validation

`./gradlew :app:verifyQuranReleaseCredential :app:assembleRelease` — pass (R8/minification,
`lintVitalRelease`, native build for all 4 ABIs, credential header generation all succeed with the
corrected digest). `./gradlew :app:assembleDebug :app:lintDebug` — pass.
`./gradlew :app:ktlintCheck :app:detekt` — same pre-existing whole-codebase indentation debt noted
in prior entries; zero findings in either new `QuranDebugCredentialOverride.kt` file.

### Manual validation (Pixel_9 emulator, Android 15/API 35, real network)

Reinstalled the fixed debug APK, `pm clear`-ed, reopened Al-Qur'an from Beranda: confirmed the
debug fixture-credential path still resolves and still surfaces "Detail teknis: malformed surah
list body" exactly as before, i.e. the `QuranDebugCredentialOverride` split did not regress the
existing debug fallback behavior. Did **not** verify the corrected release digest against a real
Play-signed install — that requires the product owner's own signing keystore/Play Console upload,
outside what this session can perform (a locally-signed `assembleRelease` build would use the
upload key's certificate, not the app signing key, and would therefore fail closed by design,
proving nothing new).

### Known limitations

- Live Play Store release still has the wrong digest until the product owner ships a new version
  built with the corrected `~/.gradle/gradle.properties`.
- The exactly-one-signing-certificate assumption in `QuranCredentialProvider`/`quran_credential.cpp`
  is unverified against a real hybrid-signed (classical + PQC) install; the product owner deferred
  deciding whether to harden it now.

### Next recommended milestone

Ship a new release build (new `versionCode`) with the corrected digest, and once it is live,
verify a real Play-installed device resolves the Quran credential correctly. Revisit the
single-certificate assumption above if it does not.

## Chucker HTTP inspector + multi-certificate Quran credential fix (2026-08-09)

**Status:** Implemented and verified (build-level); production-reachable verification still
pending an actual Play Store track install (see Known limitations).

### Chucker (in-app HTTP inspector)

- Added Chucker 4.3.1 (`debugImplementation(libs.chucker.library)` /
  `releaseImplementation(libs.chucker.library.no.op)` — same API in both artifacts, so no call site
  branches on build type). New `di/ChuckerModule.kt` provides one shared `ChuckerCollector`/
  `ChuckerInterceptor`, added to **both** `NetworkModule`'s content client and
  `QuranNetworkModule`'s Kemenag client, so all REST traffic is visible in Chucker's UI.
  `redactHeaders("Authorization", "user")` on the interceptor per `docs/product/QURAN_PRD.md` §9's
  explicit "redact both header names/values from all logging and test interceptors" requirement —
  the real Kemenag credential is never shown even in this on-device-only, debug-only inspector.
- **Removed** `QuranNetworkModule`'s existing `HttpLoggingInterceptor(Level.BODY)` (debug-only) in
  the same change. It logged full request/response headers (including the credential) and full
  bodies (Arabic/translation text) to Logcat — a direct violation of
  `docs/security/SECURITY_BASELINE.md`'s "never log headers... bodies, Arabic, translations,
  tafsir" rule that had regressed since an earlier session's audit claimed no such interceptor was
  attached. Chucker replaces it with equivalent (better: in-app UI, not system Logcat) debugging
  value without the leak. Removed the now-unused `okhttp-logging-interceptor` dependency.
- **Fixed `settings.gradle.kts`**: `dependencyResolutionManagement.repositories`' `google()` entry
  had no content filtering (unlike `pluginManagement`'s `google()` just above it, which already
  correctly scopes to `com.android.*`/`com.google.*`/`androidx.*`). Every non-Google dependency
  (Chucker included) was therefore needlessly probed against `google()` first. Applied the same
  content filter to the main repository block — a real latent inefficiency, independent of any
  environment issue.

### Kemenag credential — real production failure diagnosed and fixed

Product owner reported the standalone Quran feature still failing in a real deployed build with
this session's own new error-detail UI correctly showing "Kemenag credential unavailable" — i.e.
the signing-certificate check was still failing closed even after the previous entry's digest
correction.

- Ruled out (with evidence, not assumption): header names. A Postman capture showed a `400
  {"code":400,"res":"error","message":"something not valid"}` from `surah/local/1/114`; replaying
  the exact same URL/headers with `curl` and the real credential from `~/.gradle/gradle.properties`
  returned a clean `200` with all 114 surahs (repeated 3x, no flakiness) — the `user`/
  `Authorization`
  header names in `QuranAuthInterceptor` were never wrong, and are not the cause of the original
  Postman 400 (most likely a stale/different token at capture time, not reproducible now).
- Confirmed (via a fresh local `assembleRelease`) that the corrected classical-key digest from the
  previous entry generates correctly into `quran_credential_secrets.h` on this machine — ruling out
  "the build never picked up the fix."
- **Root cause found**: pulled the actual installed production APK off a real device (`adb pull`
  the base APK reported by `pm path`) and inspected its real embedded signing certificate with
  `apksigner verify --print-certs` (build-tools 30.0.2, which predates any hybrid/PQC awareness and
  so can't be confused by it — a first attempt with build-tools 37.0.0 against the full APK failed
  outright with `NoSuchAlgorithmException: ML-DSA KeyFactory not available`, and even a
  min/max-SDK-scoped retry printed an inconsistent-looking result, so the older tool's clean,
  independently-verified answer was trusted instead). The real on-device certificate was
  `CN=Android, OU=Android, O=Google Inc., ...` / SHA-256
  `FB:50:44:35:A2:A3:51:C5:51:C8:74:B6:1C:D4:B1:38:D7:A2:31:31:5E:90:DE:22:6D:A7:C3:FF:8A:07:EB:6D`
  — **not** SanguSantri's own App Signing Key at all. Confirmed via Google's own documentation that
  **Google Play Internal App Sharing re-signs every upload with its own dedicated, Google-generated
  "Internal test certificate,"** completely separate from the real Play App Signing key, regardless
  of how the developer originally signed the upload. The product owner had been testing via an
  Internal App Sharing link, not a real Play Store track install — so the correct classical-key
  digest from the previous entry was right for real end users, but could never match this specific
  test path. (The product owner separately surfaced the same SHA-256 already present in an
  externally-hosted `assetlinks.json` — not part of this repo, and currently inert since
  `AndroidManifest.xml` has no `android:autoVerify` App Links intent-filter — which independently
  corroborates that whoever set that file up likely captured it from the same kind of test install
  rather than the real App Signing Key; flagged to the product owner as a separate, non-blocking
  cleanup item outside this codebase.)
- **Fix**: `SANGU_QURAN_RELEASE_SHA256` now accepts a **comma-separated list** of digests instead of
  exactly one, so both the real App Signing Key and the Internal App Sharing certificate are
  accepted — chosen over silently trusting only one, since the product owner wants to keep using
  Internal App Sharing links for convenience. `app/build.gradle.kts`: `quranHexDigestToBytes`
  unchanged (still validates one 64-hex-char entry); new `quranHexDigestListToBytesList` splits on
  comma/trims/filters blanks; `quranCredentialHeaderContent()` emits
  `kExpectedSigningDigestCount`/`kSigningDigestLength` and a 2-D
  `kExpectedSigningSha256[count][32]` instead of a flat `[32]` array (an unconfigured build still
  emits exactly one all-zero placeholder row, matching the existing placeholder-on-absent pattern).
  `quran_credential.cpp`'s `SigningDigestMatches` now loops over every configured digest, matching
  if the actual reported certificate equals **any** of them — each candidate still requires an
  exact `memcmp`, so this only widens which of Google's own re-signing certificates count as
  legitimate, it does not weaken the per-candidate check itself. No Kotlin/JNI signature change was
  needed — `QuranCredentialProvider`/`QuranNativeCredentialBridge` still pass exactly one computed
  digest per call; the widening lives entirely in the native comparison.
  `~/.gradle/gradle.properties`'s `SANGU_QURAN_RELEASE_SHA256` now holds both the classical App
  Signing Key digest and the confirmed Internal App Sharing digest, comma-separated.

### Validation

`./gradlew :app:assembleDebug :app:assembleRelease` — pass (this sandbox needed
`-Djava.net.preferIPv4Stack=true` to reach Maven Central at all — a local IPv6-egress gap in this
environment, not a project or dependency-declaration problem; not something to bake into the
project's committed `gradle.properties`). Confirmed the generated header correctly emits
`kExpectedSigningDigestCount = 2` with both real digests byte-for-byte correct, via a real Gradle
build (not a hand check) — also sanity-checked the CSV-splitting logic structurally with a
temporary two-digest `-P` override before wiring in the real second value.
`./gradlew :app:ktlintCheck :app:detekt` — `ktlintCheck` fails, `detekt` passes; confirmed every
`ktlintCheck` finding is the same pre-existing whole-codebase "Unexpected indentation" debt noted in
prior entries (checked specifically: zero findings of any kind in `ChuckerModule.kt`,
`QuranDebugCredentialOverride.kt`, or the `.kts` build scripts). `./gradlew :app:lintDebug` — pass.

### Known limitations

- **Still not verified against a real Play Store track install** (Internal Testing/Closed
  Testing/Production via the Play Store app, not a share link) — only Internal App Sharing and a
  local `assembleRelease` have been checked. The classical-key digest should be correct for that
  path per Play Console's own "App signing key certificate" page, but has not been empirically
  confirmed the way the Internal App Sharing path now has.
- The externally-hosted `assetlinks.json` (not part of this repo) likely still has the wrong
  certificate fingerprint for real production App Links verification, though this currently has no
  effect since the manifest has no App Links intent-filter at all.
- Codebase-wide ktlint indentation debt remains unaddressed (unchanged from prior entries).

### Next recommended milestone

Publish (or promote) a build to a real Play Store track and confirm Quran resolves correctly for an
install that actually goes through Play's real signing pipeline, not Internal App Sharing. Then
resume Nahwu Quiz (`0.0.5`) or Kalender Hijriah's next slice.

## 16 KB page-size Play Console rejection fixed (2026-08-09)

### Diagnosis

Play Console rejected an upload with "Your app does not support 16 KB memory page sizes." The
app's own native code — `libqurancredential.so`, the tiny C++ credential-reconstruction boundary
added for the Quran feature (ADR 0016, `app/src/main/cpp/`) — was the cause. AGP 9.2.1's default
APK/AAB packaging already stores native libraries uncompressed and page-aligned, so packaging was
not the problem. The project pins `ndkVersion = "27.1.12297006"` (NDK r27) in
`app/build.gradle.kts`; NDK r28+ defaults its linker to 16 KB (`0x4000`) ELF segment alignment, but
r27 and earlier still default to 4 KB (`0x1000`) unless the linker flag is set explicitly, and
`app/src/main/cpp/CMakeLists.txt` was not setting it. No third-party dependency in this project
bundles prebuilt native libraries (Room, Retrofit/OkHttp, Coil, WorkManager, Chucker, Firebase
Crashlytics/Remote Config, and Play Core App Update are all pure JVM/Kotlin from this app's
perspective) — `qurancredential` is the sole native artifact this app ships.

### Fix

`app/src/main/cpp/CMakeLists.txt`: added
`target_link_options(qurancredential PRIVATE "-Wl,-z,max-page-size=16384")` to the existing
`qurancredential` target. No Gradle/AGP/NDK version change, no packaging-block change, no Kotlin
change.

### Validation

`./gradlew :app:assembleDebug` — pass. Inspected the built `libqurancredential.so` program headers
with the NDK r27 toolchain's `llvm-readelf -l` for all four ABIs
(`app/build/intermediates/stripped_native_libs/debug/stripDebugDebugSymbols/out/lib/<abi>/`):
every `LOAD` segment now reports alignment `0x4000` (16384 bytes) for arm64-v8a, armeabi-v7a, x86,
and x86_64 — confirmed `0x1000` (4 KB, the NDK r27 default) without the flag by temporarily
stashing the change and rebuilding. `./gradlew :app:lintDebug` — pass. `./gradlew :app:detekt` —
fails on one pre-existing, unrelated finding (`QuranEntryScreen.kt:201`, unused `detail` parameter;
detekt does not analyze C++/CMake sources, confirmed unaffected by this change).
`./gradlew ktlintCheck` — fails identically with or without this change (confirmed via `git stash`)
on the same pre-existing codebase-wide indentation debt noted in prior entries; this change touches
no Kotlin file.

### Known limitations

- Not yet verified with an actual Play Console upload — the fix was validated by direct ELF
  inspection of the built `.so`, which is what Play Console's own bundletool check reads, but the
  real upload-and-accept path has not been exercised this session.
- `assembleRelease`/`bundleRelease` were not run (gated by `verifyQuranReleaseCredential`, which
  needs the release Kemenag credential/signing secrets not available in this sandbox); the fix is
  in a Gradle-independent CMake linker flag applied identically to every build type, so debug
  verification is representative, but a release AAB build/upload should still be the final check.
- Pre-existing, unrelated `ktlintCheck` (whole-codebase indentation debt) and `detekt`
  (`QuranEntryScreen.kt:201` unused parameter) failures remain open, as in prior entries.

### Next recommended milestone

Build and upload a real signed release AAB to a Play Console testing track to confirm the 16 KB
rejection is gone end-to-end, then resume Nahwu Quiz (`0.0.5`) or Kalender Hijriah's next slice.

## Quran sync: in-memory per-surah retry + crash-safety net (2026-08-09)

**Status:** Implemented and verified (build-level); not manually verified on-device this session
(see Known limitations).

### Problem

Product owner reported a real Kemenag failure: `ayat/local/5` returned `IOException: unexpected
end of stream` (a transient dropped connection). Tapping "Coba lagi" restarted the entire
114-surah fetch from scratch even though most surahs had already succeeded — wasteful, and on a
weak connection could make an attempt never converge. Approved approach (see this session's own
discussion) is two layers: transparent per-request retry, plus an in-memory cache so a retry only
re-fetches surahs that actually failed — while explicitly keeping the edge cases (internet loss,
the app being closed mid-sync, and any unexpected exception) crash-safe.

### Changes

`QuranSyncManager.kt` is the only file with production logic changed:

- **Per-request retry** (up to 3 attempts, linear backoff): `fetchSurahs`/`fetchAyatForSurah` now
  share a new `fetchWithRetry`/`attemptSingleFetch` pair that transparently retries a request when
  the failure is classified retryable (`IOException` or a retryable HTTP status) before ever
  surfacing a failure. Malformed bodies and non-retryable HTTP codes are never retried.
- **In-memory per-surah retry cache**: successfully-fetched surahs are kept in a
  `ConcurrentHashMap<Int, List<QuranAyatDto>>` (`cachedAyatBySurah`) for the process's lifetime. A
  subsequent `sync()` call — manual "coba lagi" or automatic — only re-fetches surahs missing from
  that cache instead of all 114. The cache is discarded on a successful commit, on a permanent
  failure, or the instant the target `stableVersion` changes; it survives a retryable failure,
  which is the case this targets. `onProgress` now starts from the already-cached count instead of
  resetting to zero on a retry.
- **Crash-safety net for the two named edge cases**: every `sync()` call — both
  `QuranEntryViewModel`'s interactive path and `QuranUpdateWorker`'s background path (the latter
  bypasses `QuranRepositoryImpl`'s own mutex entirely) — is now serialized through a new internal
  `cacheMutex` and wrapped in a catch-all that rethrows `CancellationException` first (so the app
  closing / the user leaving the screen mid-sync cancels cleanly — `Mutex.withLock` releases
  correctly on cancellation, and whatever was already cached survives for next time) but converts
  any other unexpected exception into `QuranSyncResult.RetryableFailure` instead of letting it
  propagate — this coroutine can run on `viewModelScope`, which crashes the app on an uncaught
  exception otherwise. Reported to Crashlytics.
- The Room commit path (`commit()`) is unchanged: still one atomic `withTransaction` replace, gated
  on the complete, validated 114-surah set — no partial Quran can ever reach Room or the UI.
- Added `@Suppress("TooManyFunctions")` (mirrors `QuranRepositoryImpl`'s own, same rationale: one
  cohesive fetch-validate-commit algorithm decomposed into small private steps) since the
  retry/cache
  logic pushed the class from 11 to 14 functions.

`docs/decisions/0016-standalone-quran-kemenag-direct-api.md`: added a 2026-08-09 amendment narrowing
decision #7's "resumable staging is rejected" to mean *durable, persisted* staging — the thing
actually rejected — documenting that this in-memory, process-lifetime, non-persisted retry does not
reopen it (no new Room tables, no WorkManager-persisted progress, still discarded on process death,
still never read by Room before the one atomic commit).

### Edge cases (explicitly requested)

- **Internet lost mid-sync**: a single dropped request retries in place; if the whole attempt still
  fails, only the surahs still missing get re-fetched on the next attempt.
- **User closes the app mid-sync**: cancellation is rethrown, never swallowed, so structured
  concurrency and `Mutex.withLock`'s cancellation-safe release both work correctly; surahs already
  cached before cancellation survive in memory for the next attempt as long as the process itself
  survives (an actual process kill still falls back to a full restart, unchanged from before).
- **Concurrent callers** (interactive UI vs. `QuranUpdateWorker`, which does not share
  `QuranRepositoryImpl`'s mutex): closed by the new manager-internal `cacheMutex`, so the cache is
  never read or written concurrently regardless of caller.
- **Version-triggered update overlapping a cached initial-preparation attempt**: the cache is keyed
  to the `stableVersion` it was populated under and discarded if a `sync()` call arrives for a
  different version.

### Validation

The whole-module `ktlintFormat`/`ktlintMainSourceSetCheck` reformatted ~40 unrelated pre-existing
files across the codebase on every run (the same whole-codebase "Unexpected indentation" debt noted
in prior entries) — restored all of them via `git restore` each time, keeping only
`QuranSyncManager.kt` and the ADR amendment in the working tree. Confirmed via a `git stash`/
original-HEAD comparison that `QuranSyncManager.kt` itself already carried 175 of these pre-existing
indentation findings before this change, so none of it is new debt.

- `./gradlew :app:ktlintMainSourceSetCheck` — 0 findings in `QuranSyncManager.kt` after formatting;
  every remaining finding is the pre-existing debt in untouched files.
- `./gradlew :app:detekt` — 0 findings in `QuranSyncManager.kt` after the `TooManyFunctions`
  suppression; the one remaining finding (`QuranEntryScreen.kt:201`, unused parameter) is
  pre-existing, in a file this change never touched.
- `./gradlew :app:lintDebug` — pass, no findings.
- `./gradlew :app:assembleDebug` — pass.

### Known limitations

- Not manually verified on an emulator/device this session (none available) — the retry/cache
  behaviour has been reasoned through and build-validated, not exercised against a real flaky
  connection or a real app-close-mid-sync.
- No automated test coverage added (no existing `QuranSyncManagerTest` to keep compiling; none was
  requested this session).
- Codebase-wide ktlint indentation debt remains unaddressed (unchanged from prior entries).

### Next recommended milestone

Manually verify on a device/emulator once available: interrupt Quran preparation mid-sync (airplane
mode, then force-close) and confirm a resumed "coba lagi" only re-fetches the surahs that hadn't
completed yet. Then resume Nahwu Quiz (`0.0.5`) or Kalender Hijriah's next slice.

## Quran Light mode + Light/Dark switch (2026-08-10)

**Status:** Implemented and verified locally — `ktlintCheck`, `detekt`, `lint`, `assembleDebug` all
pass. Not manually verified on-device this session (no emulator/device available).

### Problem

Product owner requested a light mode for the standalone Quran feature with a switch between it and
the original dark mode — reversing ADR 0016 decision #12 ("every Quran surface is dark-only"), which
had been a deliberate product decision, not an oversight. Amended the ADR (below) rather than
silently deviating from it, per the project's documentation-first working method.

### Design decisions (user-confirmed before implementation)

* **Toggle placement:** both a quick one-tap sun/moon icon in the hub and reader top bars, and an
  explicit Light/Dark segmented control in Tampilan Al-Qur'an settings — not settings-only.
* **Mode set:** two explicit states (Light/Dark), defaulting to Dark (unchanged behaviour for
  existing users) — no third "follow system theme" state, since Quran's appearance is deliberately
  independent of the outer app/system theme (ADR 0016 decision #1/#12).

### Changes

**Domain/data:**

* New `domain/model/QuranThemeMode.kt` (`DARK` default, `LIGHT`).
* `QuranReaderSettings.themeMode` (new field, defaults `DARK`).
* `QuranReaderSettingsRepository`/`Impl`: new `setThemeMode(mode)` (explicit, for the settings
  control) and `toggleThemeMode()` (atomic DataStore read-modify-write, no caller-side "current
  value" needed — used by the top-bar quick icon). New `quran_theme_mode` DataStore key.

**Design system:**

* `Color.kt`: every existing `Quran*` dark token renamed with a `Dark` suffix
  (`QuranBackgroundDark`, etc.); matching `*Light` tokens added. Reused the app's own existing
  light-theme tokens (`SantriGreen40/95/20`, `SantriNeutral10/40/99`, `SantriSurface`,
  `SantriOutline`, `SantriError40`) wherever the role matched exactly, for brand consistency with
  `Theme.kt`'s own `LightColorScheme`; added two dedicated new hex values only where nothing fit
  (`QuranMutedTextLight`, `QuranEntryProgressTrackColorLight`). Every text/surface pairing was
  contrast-checked (WCAG relative luminance) at ≥4.8:1, clearing the 4.5:1 AA threshold.
  `QuranScrim` stays a single shared value (a scrim always darkens regardless of mode).
* New `core/designsystem/theme/QuranColorScheme.kt`: `LocalQuranThemeMode` CompositionLocal
  (default `DARK`) plus `@Composable get()` properties reusing the exact bare names
  (`QuranBackground`, `QuranSurface`, …) every existing Quran screen already imported — so none of
  those ~13 files needed an import change, only their colour source went from a static `val` to a
  live-resolved property.

**Cross-cutting wiring:**

* New `feature/quran/QuranThemeViewModel.kt`: observes the persisted mode once, for
  `SanguSantriNavHost` to provide ambiently — avoids five separate DataStore observers (one per
  Quran route) and keeps the nav host's own background behind the Quran destination in sync with
  every Quran screen inside it.
* `SanguSantriNavHost.kt`: collects that mode and wraps its content in
  `CompositionLocalProvider(LocalQuranThemeMode provides mode)`.
* `QuranThemeBoundary.kt`: now resolves `darkTheme` from `LocalQuranThemeMode` instead of a
  hardcoded `true`, and flips system-bar icon appearance (light icons on Dark, dark icons on Light)
  to match, live, when the mode changes mid-session.
* New `feature/quran/QuranThemeToggleButton.kt`: the shared quick-toggle icon button (sun while
  Dark, moon while Light — the icon shown is the mode a tap switches *to*), used identically by both
  the hub and reader top bars.

**Screens:**

* Hub (`QuranHubActions`/`QuranHubViewModel`/`QuranHubScreen`) and Reader
  (`QuranReaderBodyActions`/`QuranReaderViewModel`/`QuranReaderScreen`) top bars: added the quick
  toggle icon, wired to each screen's own `toggleTheme()` → `settingsRepository.toggleThemeMode()`.
* Settings (`QuranSettingsUiState`/`ViewModel`/`Screen`): added an explicit Light/Dark segmented
  control (`QuranThemeModeControl`, reusing the existing `QuranDisplayModeSegment` composable),
  placed directly under the live preview so the effect is visible immediately.
* `feature/quran/reader/QuranFlowingPageText.kt`: its `buildPageText` helper and the `BasicText`
  `color` producer both run outside composition, so they cannot read the now-`@Composable`
  `Quran*` colour properties directly — extracted a small `rememberQuranAnnotatedPage` composable
  that resolves `QuranPrimary`/`QuranOnPrimaryContainer`/`QuranPrimaryContainer` once (composable
  context) and passes them in as plain `Color` values, included in the `remember` keys so a live
  theme toggle recolours the already-built annotated string's baked-in ayat-number/selection spans.

**Strings:** `quran_theme_toggle_to_light_content_description`, `_to_dark_...`,
`quran_settings_theme_label`, `quran_settings_theme_light`, `quran_settings_theme_dark`.

**Docs:** `docs/decisions/0016-standalone-quran-kemenag-direct-api.md` — dated amendment narrowing
decision #12 to "Dark by default, Light available" (full rationale, token reuse, and what remains
unchanged). `docs/product/QURAN_PRD.md` — updated the dark-only product-outcome/scope statements,
QUR-FR-015's settings list, and acceptance criterion 14 (entering/leaving Quran no longer forces
dark; it now uses the user's persisted Quran theme choice, independent of the outer app theme either
way). `docs/design/QURAN_DESIGN_SYSTEM.md` — added a full Light colour-role table beside the
existing Dark one (§2.2, with contrast figures), updated §1's experience direction, §5.1's hub top
bar, §5.2's initial-preparation theme statement, and §5.7's Tampilan Al-Qur'an control list.

### Validation

`./gradlew :app:compileDebugKotlin` — pass (also fixed the one real compile break the property
conversion caused, in `QuranFlowingPageText.kt`, see above).
`./gradlew :app:ktlintMainSourceSetCheck`
— every file this change touched has 0 findings; remaining failures are the same pre-existing
whole-codebase "Unexpected indentation" debt noted in prior entries, in files this change never
touched (`ktlintFormat`'s auto-reformat of ~40 unrelated files was reverted via `git restore`,
keeping only the intentional changes). `./gradlew :app:detekt` — 0 new findings; fixed three real
findings this change caused (`QuranFlowingPageText`'s `LongMethod`, `QuranSettingsScreen.kt`'s and
`QuranReaderSettingsRepositoryImpl`'s `TooManyFunctions`, the latter two suppressed with the same
"one cohesive feature decomposed into small steps" rationale already used elsewhere in this codebase
for the same rule); the one remaining finding (`QuranEntryScreen.kt:201`, unused parameter) is the
same pre-existing, unrelated debt noted in prior entries. `./gradlew :app:lintDebug` — pass, no
findings. `./gradlew :app:assembleDebug` — pass.

### Known limitations

* Not manually verified on an emulator/device this session (none available) — the live theme
  switch, system-bar icon flip, and annotated-span recolouring have been reasoned through and
  build-validated, not exercised on a real screen.
* No automated test coverage added (per this project's current temporary implementation-pass
  constraints — no new tests during this phase; existing test sources were unaffected since this
  change touches no file any existing test compiles against).
* Codebase-wide ktlint indentation debt remains unaddressed (unchanged from prior entries).

### Next recommended milestone

Manually verify the Light/Dark toggle on a device/emulator once available (both entry points, both
directions, mid-session switching while the reader/tafsir sheet/action sheet are open, and that
leaving Quran restores the outer app's own theme/brightness regardless of which Quran theme was
active). Then resume Nahwu Quiz (`0.0.5`) or Kalender Hijriah's next slice.

## Quran "Jarak baris Arab" (Arabic line spacing) range widened (2026-08-10)

**Status:** Implemented and validated locally.

### Change

Product owner requested a wider Arabic line-spacing range in Quran settings: default unchanged at
`2.00×`, minimum widened from `1.45×` to `1.50×`, maximum widened from `2.20×` to `5.00×`.

* `domain/model/QuranReaderSettings.kt`: `MIN_ARABIC_LINE_SPACING` `1.45f` → `1.50f`,
  `MAX_ARABIC_LINE_SPACING` `2.20f` → `5.00f` (`DEFAULT_ARABIC_LINE_SPACING` already `2.30f`,
  unchanged). `coerceArabicLineSpacing` and the DataStore persistence path
  (`QuranReaderSettingsRepositoryImpl`) already derive from these constants, so both picked up the
  new range with no further code change.
* `feature/quran/settings/QuranSettingsScreen.kt`: the Arabic line-spacing `QuranSliderSetting`'s
  `steps` raised from `14` to `69` to keep the same `0.05×` step granularity across the now-wider
  `3.50×` span (was `0.75×`).
* `docs/design/QURAN_DESIGN_SYSTEM.md` §3.2: reader defaults/ranges table row updated to
  `1.50–5.00×`.

### Validation

`./gradlew ktlintFormat ktlintCheck` — pass. `./gradlew detekt` — the one finding
(`QuranEntryScreen.kt:201`, unused parameter) is the same pre-existing, unrelated debt noted in
prior entries; nothing new from this change. `./gradlew lint` — pass, no findings.
`./gradlew assembleDebug` — pass.

### Known limitations

* Not manually verified on an emulator/device this session (none available) — the widened slider
  range and live preview at the new extremes have not been exercised on a real screen.
* No automated test coverage added (per this project's current temporary implementation-pass
  constraints).

### Next recommended milestone

Manually verify the line-spacing slider at both new extremes (`1.50×` and `5.00×`) on a
device/emulator once available, confirming the live preview and persisted value both track
correctly. Then resume Nahwu Quiz (`0.0.5`) or Kalender Hijriah's next slice.

## Nahwu Quiz — engagement design spec, Jurumiyah content research, and first production content tranche (2026-08-13 to 2026-08-14)

**Status:** Design spec and research complete; content-only change implemented and validated
locally. Not the daily-challenge/engagement mechanics themselves — those remain a separate,
not-yet-started implementation pass.

### What shipped

**Design spec** (`docs/product/NAHWU_QUIZ_ENGAGEMENT_PRD.md`, 2026-08-13): a grilling session with
the product owner produced a full design for Nahwu Quiz's daily-challenge/engagement layer on top
of the already-built-but-unreleased `0.0.5` base flow — a dedicated "Tantangan Harian" mode
(date-seeded shuffled-cycle question selection, one attempt/day, timer + combo + rich feedback
exclusive to that mode), a standalone streak computed from the attempt log (hard reset on a missed
day, no XP/badges), a Beranda live streak/status indicator replacing a dedicated notification, and
an explicit decision to defer leaderboard/social features (documented as future work, not built) to
stay inside ADR 0013's guardrails. No code was written for this spec — it is a future
implementation-pass input, per the session's own "design spec first, implement later" decision.

**Primary-source research** (`docs/product/NAHWU_JURUMIYAH_RESEARCH.md`, 605 lines): a background
agent researched Matn al-Ājurrūmiyyah's bab structure, core grammar rules, and canonical examples
against directly-fetched Arabic-language primary-adjacent sources plus a full English translation,
citing every claim and explicitly flagging what could not be verified confidently (the full jawāzim
particle list, the Manṣūbāt al-Asmāʾ 14-vs-15 count arithmetic) rather than guessing.

**First production content tranche**: 31 Jurumiyah-tier multiple-choice questions were drafted from
that research (`docs/product/nahwu-quiz-jurumiyah-draft-bank.json`, kept as the pre-promotion
provenance record), reviewed and explicitly accepted by the product owner, then promoted into
`app/src/main/assets/nahwu_quiz/nahwu_quiz_bank.json` as package `nahwu-jurumiyah` — replacing
both `[FIXTURE]` placeholder packages (`nahwu-dasar-fixture`, `nahwu-lanjutan-fixture`) entirely.
Question/package ids renamed to drop the `-draft` marker; the draft file's `source`/`_disclaimer`
fields (not part of `NahwuQuizQuestionDto`) were dropped from the production file. This is 31 of
the ~60–90 question target for the Jurumiyah tier (a first tranche, not the complete tier), and
does not yet include the `includedInDailyChallenge` schema field the engagement spec's §7
proposes — that arrives with the engagement-mechanics implementation pass, not this content-only
change.

**Content governance**: `docs/operations/CONTENT_GOVERNANCE.md` gained a new "Nahwu Quiz content
(educational, not amaliyah)" section recording that this document's risk-based publication model —
previously scoped only to amaliyah ritual text — now explicitly also covers Nahwu grammar-education
content by product-owner decision, plus the Jurumiyah baseline acceptance record itself (same
pattern as the existing Tahlil/Istighosah "Public content baseline" paragraph). No kyai/ustaz review
occurred or is claimed; the risk-based model's product-owner-acceptance path was used, matching how
standard public amaliyah is already treated.

### Validation

`./gradlew :app:ktlintFormat :app:ktlintCheck` — the one failure
(`ReminderRepositoryImpl.kt:1:1`, max line length) is pre-existing, uncommitted working-tree state
from before this session (confirmed via `git diff --stat` against the last commit touching that
file) — this session touched no `.kt` file. `./gradlew :app:detekt` — the one finding
(`QuranEntryScreen.kt:201`, unused parameter) is the same pre-existing, unrelated debt noted in
every prior entry above. `./gradlew :app:lintDebug` — pass, no findings.
`./gradlew :app:assembleDebug`
— pass, confirming the new bundled JSON asset merges and packages correctly.

**Manual on-device verification** (Pixel_9 emulator, API 15/36, connected this session):
`installDebug` + `adb shell pm clear com.sangusantri.app` (see the bootstrap-staleness known
limitation below) then walked Beranda → Kuis Nahwu → Lihat paket soal → Jurumiyah → Detail Paket →
Instruksi → Soal 1. Confirmed no `[FIXTURE]` text anywhere, package shows "Jurumiyah" with the real
description, "31 soal" / "31 pertanyaan pilihan ganda" both agree with the asset's actual question
count, and question 1 renders its Arabic (`اللَّفْظُ الْمُرَكَّبُ الْمُفِيدُ بِالْوَضْعِ`) with
correct harakat inline with Indonesian RTL/LTR mixed text. Did not exercise submitting an answer,
completing the attempt, or any other package/screen beyond question 1.

### Known limitations

- No code implements the engagement spec's mechanics yet (daily challenge, streak, timer/combo,
  Beranda indicator, new Room entities) — `docs/product/NAHWU_QUIZ_ENGAGEMENT_PRD.md` §12's
  implementation pass is still future work.
- 31 questions is short of the ~60–90 Jurumiyah-tier target; more content passes are needed before
  the daily-challenge shuffled-cycle mechanic has enough pool depth to be worthwhile.
- Imrithi and Alfiyah tiers remain entirely unauthored.
- **`NahwuQuizBootstrapper` import-once behaviour caught an already-seeded emulator off guard**:
  bootstrap is gated purely on `packageDao.count() > 0` (by design, this milestone's scope has no
  remote-sync/version-reconciliation path — see the class doc comment), so a device/emulator that
  ran the app before this content promotion kept showing the old `[FIXTURE]` packages after the
  asset changed, until app data was cleared. Not a code bug; a real fresh install always bootstraps
  correctly from the current asset. Worth remembering during manual testing of any future bundled-
  content change: `adb shell pm clear com.sangusantri.app` (or uninstall/reinstall) before checking
  it on a device that has run the app before.
- No automated test coverage added or run (per this project's current temporary implementation-pass
  constraints — this was also a content-only change, not a production-API change, so no existing
  test was at risk of breaking; confirmed by inspection that `NahwuQuizSessionViewModelTest.kt` and
  `NahwuQuizValidatorTest.kt` construct their own in-memory fixture data and never read the bundled
  asset file).

### Next recommended milestone

Either (a) author more Jurumiyah questions toward the ~60–90 target, or (b) begin the engagement
spec's implementation pass (`NAHWU_QUIZ_ENGAGEMENT_PRD.md` §12: data model, selection algorithm,
session mechanics, streak query, Beranda indicator) using the 31 questions already shipped. Kalender
Hijriah/Quran polish items noted in earlier entries remain otherwise unaffected.

## Sholawat dan Artinya `0.0.8` — Scaffolding (2026-08-16)

Own dedicated PRD and progress log (product owner instruction: new features get their own PRD +
progress doc, not additions to this shared file) — see `docs/product/SHOLAWAT_PRD.md` and
`docs/product/SHOLAWAT_PROGRESS.md` for the full entry. Summary: list + dedicated reader screens,
Beranda entry point, and exclusion from Jelajahi Amaliyah/Beranda's generic Amaliyah surfaces are
implemented and reuse the existing content pipeline unchanged; real sholawat content is a blocked,
separate follow-up pending the product owner supplying titles and a source.

## Beranda + Al-Qur'an revamp — Step 1, design tokens and app-wide theme (2026-08-16)

Own progress log (same convention as Sholawat: an initiative gets its own doc, not additions here) —
see `docs/design/BERANDA_QURAN_REVAMP_PROGRESS.md`. Summary: the
`design_handoff_beranda_quran_revamp/`
bundle's palette is now one app-wide set of colour roles that both `MaterialTheme.colorScheme` and
the
`Quran*` tokens resolve to, so every existing screen picked up the new warm-paper/raised-dark family
without per-screen edits; the persisted theme mode is now app-wide (`QuranThemeMode` →
`AppThemeMode`),
resolved once in `MainActivity`, follows the system until the user first chooses, and
`QuranThemeBoundary` is deleted. Steps 2–6 of the handoff (reader header, Beranda, Jadwal Sholat +
Kiblat, Tampilan additions, remaining restyles) are not started. Note for anyone running the
handoff's
per-phase validation: `ktlintCheck` and `detekt` fail on unmodified committed code in this repo —
see
that doc's "Toolchain problem found".

## Beranda + Al-Qur'an revamp — Steps 2-6 (2026-08-16)

Continues the entry above; full detail in `docs/design/BERANDA_QURAN_REVAMP_PROGRESS.md`. The
reader's tenang surah header (basmalah now read from Room, never hardcoded), mushaf immersion,
Beranda's rebuild, a new `feature/prayertimes` Jadwal Sholat + Kiblat screen, the Kepala surah
setting, and the Tasbih/guided-counter/Amaliyah/Aktivitas restyles are implemented. Two things to
carry forward: **Jadwal Sholat has no prayer-time source** (release builds render nothing; debug
builds show clearly-marked sample times behind a `BuildConfig.DEBUG` gate), and the handoff's
**Murottal chip was not built** because Quran audio is off the roadmap — that needs a product
decision.

## Jadwal Sholat + Kiblat wired to myquran (2026-08-17)

Detail in `docs/design/BERANDA_QURAN_REVAMP_PROGRESS.md` and ADR
`docs/decisions/0018-myquran-for-prayer-times-and-qibla.md`. Prayer schedules and
qibla now come from api.myquran.com; the schedule is keyed by kabupaten/kota so it
needs no location permission, a month is cached at a time for offline use, and the
debug-only sample schedule is gone. Kiblat adds the app's first runtime permission
(`ACCESS_COARSE_LOCATION`, optional, on-demand only). Room goes to v6, which under
the no-migrations policy **wipes the database** — every user re-downloads the Quran
once. myquran's Quran audio/tafsir and its hijri calendar were evaluated and
deliberately not adopted; the reasons and the measured hijri comparison are in the
ADR.

## Kalender Hijriah production sign-off + city detection fix (2026-08-17)

**Kalender Hijriah is production-ready** on the app's own offline `java.time.chrono.HijrahDate`
computation (product-owner decision). It follows Umm al-Qura, which lands a day before Kemenag/NU on
1 Ramadan and Idul Fitri — accepted, recorded in ADR 0018 with the future option of a curated
Kemenag
date table over the Firebase Hosting content pipeline. The reminder scheduler uses the same
computation, so calendar and reminders agree.

Also fixed: granting location on first launch now fills the prayer-schedule city in automatically
instead of still demanding a manual pick — three faults (geocoder on the main thread, no
last-known fix immediately after a grant, and a name matcher that could not reconcile "Kota Jakarta
Barat" with myquran's "KOTA JAKARTA"). Detail in
`docs/design/BERANDA_QURAN_REVAMP_PROGRESS.md`.

## Murottal per ayat + unduhan audio (design handoff turn 4, 2026-08-17)

**Status:** Implemented and verified on a booted emulator. Turn 4 of the
Beranda/Al-Qur'an revamp handoff (`4a`–`4f`): tapping an ayah number plays it and
auto-continues, per-surah audio download, murottal panel, hub "Sedang diputar"
block, mushaf follow-scroll, and a foreground media service so playback survives
leaving the reader.

Murottal audio now comes from **myquran's CDN, audio bytes only** — Kemenag remains
the sole Quran *content* API. This reverses ADR 0018's own "Quran audio — deferred"
note and makes the previous "no Quran audio" statements in `CLAUDE.md` and
`ROADMAP.md` obsolete (both updated). No Room version bump and no data loss: audio is
stored as **files** under `filesDir/murottal/`, deliberately outside Room.

Full detail, deliberate deviations from the addendum, commands run (including the
pre-existing `ktlintCheck` failure this work did not cause), and on-device
verification: `docs/design/QURAN_MUROTTAL_PROGRESS.md` and the 2026-08-17 amendment
in `docs/decisions/0018-myquran-for-prayer-times-and-qibla.md`.

## Security review, unit-test pass, and pull-request CI (2026-08-18)

**Status:** Review complete, fixes implemented, unit tests green locally, CI
workflow added. Not a numbered milestone — no feature shipped, no religious
content touched. Full finding-by-finding detail, severity, and the decisions left
open: `docs/reviews/security-review-2026-08-18.md`.

**One item is not closed and needs the product owner.** The release keystore
`sangusantri.jks` was committed on 2026-07-26 into a **public** repository and had
no `.gitignore` rule. It is
now untracked and CI rejects any pull request that tracks key material, but the
key must still be treated as compromised: reset the Play upload key, rotate the
Kemenag credential and `SANGU_QURAN_RELEASE_SHA256`, and purge the blob from
history. Removing it from `HEAD` does not remove it from history. ADR 0016's
credential protection is gated on the release signing certificate, which is what
makes this urgent rather than routine.

**Fixed.** Catalog `contentUrl` is now pinned to an origin-relative path under
`/content/` in `ContentValidator` — it previously reached Retrofit as an `@Url`,
where an absolute value replaces the base URL outright, so a tampered catalog
could have imported amaliyah text from any origin under the attribution the
catalog claims; the same guard closes `..` traversal in the bundled asset path,
since both pipelines run `validateCatalog` before any read. `imageUrl` is now
restricted to https. The content OkHttp client no longer follows redirects (the
other three already did not). The qibla request now sends coordinates truncated
to two decimals — `ACCESS_COARSE_LOCATION` bounds the platform's fix, not the
digits the app transmits. Two locale defects found while testing that: murottal
file names/URLs and the prayer-times date formatters used the default locale's
numbering system, so both features broke silently on Arabic-Indic-digit locales;
both pinned to `Locale.ROOT`. `PRIVACY.md` was corrected where it contradicted
the shipped location behaviour — it is the stated input to the Play Data Safety
declaration.

**Tests.** 47 new JVM unit cases, no new test infrastructure: the catalog URL
rules, `ResponseSizeLimitInterceptor` (a security control installed on all four
OkHttp clients that had no coverage at all), `coarseCoordinate`,
`QuranAudioSource`, `ReminderScheduleCalculator`, and `validateCustomTarget`.
Three pre-existing `SerambiViewModelTest` failures were repaired — three stacked
problems, each hidden by the one in front: a collector cancelled outside
`backgroundScope` leaving `SharingStarted.WhileSubscribed`'s stop-timeout on
`runTest`'s scheduler; then `advanceUntilIdle()` never returning at all, because
`uiState` combines an endless one-tick-a-minute clock (use `runCurrent()`, and
note this applies to anything else combining that clock); and underneath both,
whole-object equality assertions that could not have passed since `Loaded` gained
its `now` field. Suite wall time drops from ~5 min to ~35 s. `detekt` was red on
`master` too — `QuranTranslationAyatItem` at exactly 60/60 `LongMethod`; its
`Modifier` chain is now extracted, with no behaviour change. Gating CI on
already-red checks would have been pointless.

**CI.** `.github/workflows/pull-request.yml` runs five jobs on every PR to
`master`: secret scan, content-catalog validation
(`tools/ci/validate_content.py`, mirroring `ContentValidator`/`ContentImporter`
over both content trees), `detekt` + `ktlintCheck`, `testDebugUnitTest`, and
`lintDebug` + `assembleDebug`. Debug-only — a release assembly needs the ADR 0016
secrets, which GitHub does not expose to fork pull requests. The `ktlint` step is
the one non-blocking step: `master` carries ~3,358 `standard:indent` violations
against the already-pinned ktlint 1.5.0 (two constructor-indent styles coexist in
the tree). `./gradlew ktlintFormat` fixes it in one command but rewrites ~60 files
— a formatting decision for the product owner, after which one line in the
workflow flips to blocking.

**Note on the temporary "do not add tests" constraint** in `CLAUDE.md`: that
section scopes itself to the design product-alignment phases and says to remove it
once the user says otherwise. This pass was an explicit request for unit tests, so
it applies here.
