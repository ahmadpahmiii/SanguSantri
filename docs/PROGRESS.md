# SanguSantri Engineering Progress

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

Canonical content model + offline seed import (PRD FR-001, §12.2): define
the Room content entities (amaliyah, variant, version, step), the JSON seed
schema under `app/src/main/assets/content/`, and the transactional import
that makes the release build fail validation until approved production
content is supplied.
