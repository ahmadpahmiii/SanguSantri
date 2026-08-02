# Architecture

Applies to any UI, domain, or data-layer task. Read alongside
`docs/engineering/CODING_STANDARD.md` (naming/Compose rules) and
`docs/engineering/CONTENT_MODEL.md` (data shape).

## Technology stack

Kotlin, Jetpack Compose, Material 3, Navigation 3, Hilt (+ Hilt Worker
extension), Kotlin coroutines, Flow/StateFlow, Room, DataStore, WorkManager,
Retrofit, OkHttp, Kotlinx Serialization, AndroidX Lifecycle, AndroidX
adaptive layout APIs, Gradle Kotlin DSL + version catalog, JUnit, Compose UI
testing, Android Lint, Detekt, ktlint. Retrofit/OkHttp/WorkManager/Hilt
Worker are implemented (content sync foundation, ADR 0012) — see Remote
content synchronisation below; they are no longer a future-tense entry in
this list.

Use the latest mutually compatible stable versions available when
implementation begins. Do not use alpha or beta dependencies when a stable
alternative exists. Navigation 3 is preferred over Navigation 2 because it
is Compose-native with explicit back-stack ownership (ADR
[0004](../decisions/0004-navigation-3-for-compose-navigation.md)).

## SDK configuration

* `minSdk = 26`
* `compileSdk = 37` (or later stable equivalent)
* `targetSdk = 36`

Target API 36 avoids an immediate migration: new apps and updates submitted
from 31 August 2026 must target Android 16/API 36 or higher. Current config
already satisfies this — no action needed.

## Gradle modules

One `:app` Gradle application module (ADR
[0001](../decisions/0001-single-android-gradle-module.md)). Do not create
feature modules yet. Package boundaries below are enforced by convention
(code review), not the build graph, until a real modularisation need
appears.

### Modularisation triggers

Revisit the single-module decision only when one of these becomes true —
not preemptively:

* Multiple developers frequently editing the same package and blocking each
  other.
* Unacceptable local or CI build times attributable to module size.
* A component becomes independently reusable outside this app.
* A strong visibility-boundary requirement appears (e.g. a package must be
  hidden from another team's code).
* Dynamic feature delivery is required.
* A roadmap item (`0.2.0` pesantren membership, `0.0.5` Nahwu quiz)
  introduces a genuinely separable feature surface with its own release
  cadence.

Do not copy Now in Android's multi-module layout as a default; it solves
problems this project does not have yet.

## Package structure

```text
com.sangusantri.app
├── app                      SanguSantriApplication, MainActivity
├── core
│   ├── common
│   ├── designsystem
│   ├── model
│   └── util
├── data
│   ├── content              ContentPackageImporter, ContentPackageValidator,
│   │   └── dto                ContentChecksum, ContentImportOutcome — shared
│   │                          canonical package contract (bundled + remote)
│   ├── local (dao, database, entity)
│   │   └── content          BundledContentBootstrapper (reads AssetManager)
│   ├── remote (api, dto)     ContentApiService (Retrofit) + DTOs only
│   ├── sync                  ContentSyncManager/Scheduler/Worker/Metadata
│   ├── mapper
│   └── repository
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── feature
│   ├── home
│   ├── explore
│   ├── reader
│   ├── guidedreader
│   ├── tasbih
│   ├── activity
│   ├── contentdetail
│   ├── settings
│   └── about
├── navigation
└── di
```

`data/content`, `data/local/content`, `data/remote`, and `data/sync` are
implemented (ADR 0012, this is not a future-tense sketch). `data/local/seed`
(`SeedContentSource`/`AssetSeedContentSource`/`SeedContentImporter`) was
removed — bundled assets and remote packages now share one canonical
`ContentPackageImporter` rather than a seed-specific abstraction; see
`docs/content-schema.md`.

`feature/home` (Beranda, renamed from Serambi — Figma product-alignment
pass, `docs/reviews/figma-product-alignment.md`), `feature/reader` (Full
Reader + the reading-mode gate), `feature/guidedreader` (Guided Reader),
`feature/tasbih` (Standalone Tasbih + Session History, `0.0.2`,
Milestone 9), and `feature/activity` (Aktivitas, `0.0.3`, Milestone 10)
are implemented; `feature/explore` (Jelajahi Amaliyah, `0.0.1`) is
scheduled but not yet implemented — do not create its package before the
milestone that needs it. `feature/feedback`
was removed from this diagram: public content-correction feedback was
removed from `0.0.1` scope at Milestone 5 (`docs/product/PRD.md` FR-012)
and no feedback code exists or is planned. `feature/contentdetail` and
`feature/settings` remain unimplemented placeholders as before
(`docs/PROGRESS.md`).

## Layer rules

**UI layer**: renders immutable UI state, sends user actions, contains
presentation logic. Does not access DAOs or API services. Does not contain
database entities or network DTOs. Each major screen uses a `Route`
composable for ViewModel/navigation wiring, a stateless `Screen` composable,
`UiState`, `UiAction`, and `UiEffect` only for genuine one-time effects.
ViewModels expose `StateFlow` and follow unidirectional data flow.

**Domain layer**: plain Kotlin. Contains repository contracts and business
models. Create a use case only when logic combines multiple repositories, is
used by multiple ViewModels, has meaningful business rules, or extracting it
materially improves testing. Do not create a pass-through use case merely to
call one repository method.

**Data layer**: owns repositories, local/remote data sources, sync
resolution, DTO/entity mapping. Exposes domain models. Room is the canonical
source for content reads (ADR
[0003](../decisions/0003-room-as-local-source-of-truth.md)) — screens and
ViewModels must never render directly from network responses.

## Model duplication rules

Separate models are required when boundaries differ: network DTO, Room
entity, domain model. Do not create an additional UI model when the domain
model is already suitable for rendering. Mappings live at boundaries only —
do not map the same object through unnecessary intermediate classes.

## Edge-to-edge

Use `enableEdgeToEdge()` before `setContent`. Apply insets through Material
components, `Scaffold` padding, or one intentional inset strategy — never
apply duplicate system-bar padding. Consult the installed `edge-to-edge`
skill during implementation.

## Performance

* Serambi must render local content without waiting for network.
* Reader scrolling must remain smooth on a typical API 26 device.
* Long content must use lazy rendering; no full-document parsing on every
  recomposition.
* Content package parsing must run outside the main thread.
* Large audio files must never be loaded fully into memory.
* Release builds must enable R8 resource and code shrinking (`app/build.gradle.kts`
  `buildTypes.release.optimization.enable = true`, requires
  `android.r8.gradual.support=true` in `gradle.properties` under AGP 9.2.1 —
  re-verify this flag is still required on every AGP upgrade).

## Navigation destinations (bottom-navigation-only through 0.0.5)

**Implemented, Milestone 9.** Target IA through `0.0.5` (`docs/product/
PRD.md` §7.1, ADR
[0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md)):
Beranda / Aktivitas / Tasbih behind a **bottom navigation bar only** — no
Navigation Rail on any window-size class, including expanded/tablet, in
this window (supersedes this section's earlier adaptive bar-on-compact/
rail-on-expanded description). `SanguSantriNavHost` owns the app's single
top-level `Scaffold` (bottom bar) wrapping the existing `NavDisplay`/
`entryProvider` (Navigation 3, ADR 0004) — one navigation system, not a
second competing framework. `navigation/TopLevelBackStack.kt` implements
the multiple-back-stacks pattern from `android/nav3-recipes`' "Common UI"
recipe (one back stack per top-level tab, flattened for `NavDisplay`,
switching tabs never duplicates a `NavKey`, each tab's state survives
switching away and back). `navigation/BottomNavigationBar.kt` is a plain
Material 3 `NavigationBar`/`NavigationBarItem` — deliberately **not**
`NavigationSuiteScaffold` (whose entire purpose is exactly the adaptive
bar/rail switch this window's product decision forbids); the AndroidX
adaptive-layout APIs and installed `adaptive` skill remain the right tool
for adaptive *content* layout (constrained/centred max-width columns) and
for any future release that does introduce a rail. The reading-mode gate
(`AmaliyahDetail`), both readers, Reader Table of Contents/Settings
sheets, and Tasbih Session History stay reachable *through* their owning
tab, not as their own bottom-nav destinations — hidden from the bottom bar
whenever the current tab's own back stack is deeper than its root
(`TopLevelBackStack.isAtTopLevelRoot`). The gate's existing "replace,
don't push" backstack pattern (`replaceTopEntryWithReader`) is unaffected,
now implemented via `TopLevelBackStack.replaceLast`.

## Local user-state persistence ownership

Favourites, recently-opened, and (from `0.0.2`/`0.0.3`) Standalone Tasbih
sessions and Aktivitas history are all local-only, offline-first state —
same source-of-truth rule as existing reader progress
(`docs/engineering/OFFLINE_FIRST.md`). Follow the existing
per-concern-repository convention (`GuidedReadingRepository` already
combines two Room tables behind one repository, not two) rather than one
repository per table. Field-level detail:
`docs/engineering/CONTENT_MODEL.md`.

---

## Remote content synchronisation (implemented — Android side, ADR 0012/0014)

The Android client against the static content contract served by Firebase
Hosting (§Backend below, ADR 0014) is implemented: `BundledContentBootstrapper`
(bundled assets) and
`ContentSyncManager` (remote — owns both the HTTP handling and the sync
algorithm, since the 2026-07-28 sync simplification removed the separate
`ContentRemoteDataSource` wrapper) both delegate the actual Room write to
one shared `ContentPackageImporter` — neither knows or cares which
transport produced the bytes. Room remains the sole source of truth; the
network only ever updates Room, and the UI never observes network state
directly.

* **Scheduling**: `ContentSyncScheduler.enqueueIfStale()`, called from
  `SanguSantriApplication.onCreate()`, enqueues a unique one-time
  `ContentSyncWorker` (`ExistingWorkPolicy.KEEP`, name
  `sangu-santri-content-sync`) only when the last *terminal* sync attempt
  (success or failure) is 24+ hours old or has never happened. Not a
  periodic worker. `NetworkType.CONNECTED` is a hard constraint.
* **Failure handling**: `ContentSyncManager.sync()` returns one of three
  `SyncResult`s — `Completed`/`RetryableFailure`/`PermanentFailure`.
  Retryable failures (`IOException`, timeout, HTTP 408/429/5xx) — whether
  at the manifest level or for an individual package download — get
  bounded exponential-backoff retries of the *whole* sync
  (`Result.retry()`, 3 attempts total; packages already imported earlier
  in the same attempt are skipped on retry since Room already matches
  them). Permanent manifest-level failures (unsupported schema,
  empty/malformed body, non-retriable 4xx) record a terminal failure and
  stop. Permanent package-level failures (checksum mismatch, invalid
  structure, unsupported minimum app version, non-retriable 4xx) reject
  only that package and let the rest of the manifest continue — Room is
  never touched for the rejected package, and the app never crashes.
* **Base URL**: `BuildConfig.CONTENT_API_BASE_URL`, set from the Gradle
  property `SANGU_CONTENT_API_BASE_URL` (`app/build.gradle.kts`), defaulting
  to a non-routable `https://content-api.sangusantri.invalid/` when unset.
  Supplying the real Firebase Hosting URL activates real remote sync with no
  code change. Retrofit headers are never used as a local/remote
  content-source switch. There is no conditional-request header at all — the
  manifest is small and checked at most once every 24 hours, so ETag/`304`
  caching was deliberately removed as unnecessary complexity.
* **Hilt Worker**: `SanguSantriApplication` implements
  `androidx.work.Configuration.Provider` with an injected
  `HiltWorkerFactory`; the manifest removes WorkManager's default
  `androidx-startup` initializer (`tools:node="remove"` on its
  `WorkManagerInitializer` meta-data) per official Hilt+WorkManager
  guidance.

Full behaviour, the sync algorithm, and retention rules:
`docs/engineering/OFFLINE_FIRST.md`, `docs/engineering/CONTENT_MODEL.md`,
ADR [0012](../decisions/0012-bundled-bootstrap-and-remote-sync.md).

## Backend (Firebase Hosting static content, ADR 0014)

No `backend/` directory exists in this repository, and none is planned. ADR
[0011](../decisions/0011-go-and-supabase-managed-postgresql-backend.md)'s
Go + PostgreSQL service was never implemented and was superseded by ADR
[0014](../decisions/0014-firebase-hosting-static-content-delivery.md) before
implementation started: there is no dynamic API, no database, and no
Supabase project. Content is published as static files served by Firebase
Hosting. The Android sync client above (`ContentApiService`) already only
ever issues plain `GET` requests, so it requires **no code change** for
this — only `SANGU_CONTENT_API_BASE_URL` needs to point at the deployed
Firebase Hosting domain once `content-hosting/` is actually deployed, which
remains a separate, explicitly-requested task.

### Decision

Firebase Hosting serving static JSON files under a new top-level
`content-hosting/` directory (parallel to `app/`, not bundled into the
APK). No Firestore, no Cloud Functions, no other Firebase backend product —
see ADR 0014's Alternatives rejected. A Firebase MCP server is used only as
development/CI tooling to help manage and validate that static deployment;
it never ships in the APK and is never called from production Kotlin code
— see `docs/engineering/MCP_TOOLING.md`.

### Project structure

```text
content-hosting/
├── firebase.json          # Hosting config: public dir, rewrites, ignore list
├── v1/
│   ├── config.json        # supported schema version, min app version, feature flags
│   └── content/
│       ├── manifest.json  # RemoteContentManifestDto shape (docs/content-schema.md)
│       └── packages/
│           ├── tahlil-umum-v1
│           └── istighosah-umum-v1
```

Filenames under `packages/` must exactly match the `versionId` values
`ContentApiService.getPackage(versionId)` requests, since Firebase Hosting
resolves them as literal static file paths, not templated routes.

### Rules

* `content-hosting/` files are authored and reviewed the same way bundled
  assets are (`app/src/main/assets/content/`) — structured JSON, no
  Kotlin/Go code, validated by CI before deploy.
* Publication is `firebase deploy --only hosting`, run by CI after
  validation passes, never a manual upload of an unvalidated file.
* A published package file is never edited in place — a correction adds a
  new version file and updates `manifest.json` to point at it (ADR 0008,
  unaffected). The directory's git history is the durable, append-only
  revision record that ADR 0011's Postgres tables would have been.
* No secrets or service-role credentials are needed for Android to read
  this content — it is public, static, and unauthenticated by design,
  consistent with §3.4 (no forced account).

### Public content paths

* `v1/config.json` — supported content schema, minimum app version, feature
  flags, maintenance state, as static data. Replaces the previously planned
  `GET /v1/config` endpoint; there is no `/healthz` equivalent, since
  Firebase Hosting's own availability is Google-managed, not this project's
  service to health-check.
* `v1/content/manifest.json` — each variant's currently active published
  package only (`contentId`, `variantId`, `versionId`, `versionNumber`,
  `checksumSha256`, `minimumAppVersionCode`); no conditional-request
  header, no full revision history (`docs/content-schema.md`).
* `v1/content/packages/{versionId}` — immutable content package, one static
  file per version.

Public content-correction feedback is not part of this contract — feedback
was removed from product scope at Milestone 5 (`docs/product/PRD.md`
FR-012); content correction is an internal SanguSantri-team operation
(`docs/operations/CONTENT_GOVERNANCE.md`), not a network endpoint.

### CI validation (replaces the former Go admin CLI)

A CI script validates `content-hosting/` before every deploy and must fail
the same way the previously planned Go admin CLI's `content validate` would
have: approval missing/invalid, Arabic text empty, required translation
empty, duplicated positions, an invalid repeat target, an incomplete Quran
reference, a checksum that cannot be reproduced, an unsupported schema
version, a `manifest.json` entry with no matching package file, or a
`versionNumber` that regresses. There is no interactive publish tool and no
Supabase Studio equivalent — the only way to publish is committing a valid
file and passing CI.
