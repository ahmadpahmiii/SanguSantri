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
