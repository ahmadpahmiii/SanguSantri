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

## Figma product-alignment documentation pass (pre-Phase A)

**Status:** Complete. Not a numbered milestone — no Android/Kotlin source
changed. Documentation only.

**Scope:** Align product documentation with a set of confirmed
product/UX decisions and a named Figma file (11 node IDs covering revised
Full/Guided Reader, reader overflow menus, Reader Settings and Table of
Contents bottom sheets, Standalone Tasbih and its custom-target dialog,
Beranda, and Jelajahi Amaliyah). No Android/Kotlin code was written this
pass — that begins with Phase A once the user replies "done".

### Figma access blocker

The Figma MCP connection was rate-limited (Starter plan) for the entire
session — every call, including a plain `get_metadata` on the top-level
product-screens page, was rejected before a single node could be opened.
Per the user's explicit choice when asked how to proceed, this pass wrote
the documentation from the confirmed decisions given directly in the
request plus the current repository state, and marked every Figma-derived
visual specific (exact spacing, component variants, states drawn in each
frame) as **pending Figma verification** rather than guessing measurements
from node names alone. See `docs/design/FIGMA_HANDOFF.md`'s "Status of
this document" section — re-run Figma discovery before Phase A begins.

### What shipped

* **`docs/design/FIGMA_HANDOFF.md`** (new): file/node reference,
  frame-to-feature mapping against current code, navigation map (including
  an explicitly flagged open question on bottom-nav rollout timing),
  reader interaction map, responsive/state/motion notes, Compose component
  mapping, implementation phase order (A–E, matching the request exactly),
  and known incomplete Figma areas (no frame was supplied for Aktivitas).
* **`docs/reviews/figma-product-alignment.md`** (new): gap table —
  existing implementation vs. confirmed decision vs. gap vs. resolution
  vs. owning document vs. phase, across terminology/navigation, reader,
  Beranda/Jelajahi, Tasbih/Aktivitas, and accessibility/design-system rows.
  Also records a pre-existing documentation drift found while reading
  `ARCHITECTURE.md` (a stale `feature/feedback` package in the diagram,
  even though feedback was removed from scope at Milestone 5) — fixed as
  part of this pass since it was found, not because it relates to Figma.
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
  tabs, flagged missing Figma frame); added a "Final navigation model"
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
  (previously said 0–3); added a note that a Figma alignment pass has
  since renamed Serambi → Beranda and expanded documented `0.0.1` scope
  ahead of matching code; added `FIGMA_HANDOFF.md` to the UI/Compose
  reading-matrix row; added a clearly-scoped, explicitly temporary
  "Temporary implementation-pass constraints" section (no Room migrations,
  no new tests, minimum validation command set) for Phases A–E only, to be
  removed once that initiative concludes.

### Commands executed

None — documentation-only pass, no build/lint/test commands apply.

### Known limitations

* No Figma node was actually opened (rate limit) — every visual/spacing/
  component-variant detail in `FIGMA_HANDOFF.md` is unverified and must be
  confirmed before or during Phase A.
* No Android/Kotlin source changed; none of the new Room tables mentioned
  above exist yet; the reader overflow-menu restructure (settings/TOC
  moving into the overflow) is documented but not implemented; Beranda is
  still the Milestone 2 two-card `SerambiScreen`, not yet renamed or
  rebuilt.
* The bottom-navigation rollout-timing question (`FIGMA_HANDOFF.md`) is
  unresolved and should be confirmed before Phase B, since it changes
  whether Phase B builds nav-bar scaffolding at all.
* No Figma frame was supplied for Aktivitas (`0.0.3`) — confirm one exists
  before Phase D, or proceed from the written decision alone if the
  product owner confirms none is coming.

### Next recommended milestone

Phase A — Release `0.0.1` Reader UX alignment (`docs/design/FIGMA_HANDOFF.md`),
on the user's explicit "done" reply. Re-run Figma discovery (`get_metadata`/
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
exported revised Figma frames (`docs/design/figma-export/`, nodes `14:2`,
`14:32`, `16:2`, `16:45`, `16:89`, `16:148`), per the Phase A brief: revised
layouts, both overflow menus, the Full ↔ Guided switch (re-verified, not
rebuilt), the Full Reader repetition shortcut, progress/counter
preservation, the Reader Settings sheet, a new Table of Contents sheet,
source/pentashihan entry, adaptive width, and dark-mode color tokens. No
Standalone Tasbih, Beranda, Explore, or Activity work — out of scope per
the brief. No Room migration; no new tests added.

### Figma export inspection

All 10 exported node pairs (JSON + PNG) were inspected via a local Python
script that walks the `JSON_REST_V1`-shaped export tree (no Figma MCP
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
approximation — no dark-mode Figma frame was exported, so no new hex value
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
  Figma settings sheet only shows 3 steppers; `ReaderSettings.
  translationLineSpacingMultiplier`, `ReaderUiAction.
  SetTranslationLineSpacing`, and `ReaderSettingsRepository.
  setTranslationLineSpacing` all remain fully functional in the data/domain
  layer (untouched), but nothing in the UI calls them anymore. This is a
  real, narrower-than-FR-008 gap opened by following the revised Figma
  layout exactly rather than guessing a 4th stepper back in — flag for a
  product decision (restore the control, or update FR-008) before this is
  considered fully resolved.
* **Dark-mode color tokens are a reasoned approximation, not
  Figma-verified** — no dark-mode frame was exported; the new dark-scheme
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
  Figma-frame gap (both flagged in `docs/design/FIGMA_HANDOFF.md`) are
  unrelated to this phase and remain open.

### Next recommended milestone

Phase B — Release `0.0.1` Beranda and Explore Amaliyah
(`docs/design/FIGMA_HANDOFF.md`, nodes `19:2`/`19:84`), on the user's
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

### Figma exports inspected

The JSON hierarchy and 2x PNG reference were inspected for every Phase A
reader pair: `14:2` Full Reader, `14:32` Guided Reader, `16:2` Full Reader
overflow, `16:45` Guided Reader overflow, `16:89` Reader Settings, and
`16:148` Reader Table of Contents. The four future-screen pairs were also
classified during the audit: `17:2`/`17:32` remain `0.0.2` Standalone
Tasbih work; `19:84` requires the not-yet-implemented Explore/favourite/
category state; `19:2` includes sections that cannot yet be truthfully
backed by current persistence.

### What changed

* Added shared Figma-derived component dimensions for the 56dp compact app
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
  Figma-scale counter. The counter shows the count as the dominant element,
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
per the temporary Figma implementation-pass constraint.

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
Figma frame exists, so dark colours remain the prior Phase A reasoned
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
