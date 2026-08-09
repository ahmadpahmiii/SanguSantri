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
* A roadmap item (`0.2.0` pesantren membership, `0.0.5` Nahwu quiz,
  `0.0.6` standalone Quran)
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
│   │   └── quran             planned Kemenag-only API client + DTOs (`0.0.6`)
│   ├── sync                  ContentSyncManager/Scheduler/Worker/Metadata
│   │   └── quran             planned initial/weekly Quran refresh (`0.0.6`)
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
│   ├── quran                 planned standalone Quran feature (`0.0.6`)
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

`feature/home` (Beranda, renamed from Serambi — design product-alignment
pass, `docs/reviews/design-product-alignment.md`), `feature/reader` (Full
Reader + the reading-mode gate), `feature/guidedreader` (Guided Reader),
`feature/tasbih` (Standalone Tasbih + Session History, `0.0.2`,
Milestone 9), and `feature/activity` (Aktivitas, `0.0.3`, Milestone 10)
are implemented; `feature/explore` (Jelajahi Amaliyah, `0.0.1`) is
scheduled but not yet implemented — do not create its package before the
milestone that needs it. `feature/quran` and its data packages are likewise
planned, not implemented; create them only when `0.0.6` is explicitly
requested. `feature/feedback`
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

Standalone Quran follows the same boundary rule with its own repository:
Kemenag DTO → validation/mapping → dedicated Quran Room tables → repository
Flow → UI. A composable or ViewModel never calls the Kemenag service or DAO
directly. The existing amaliyah content importer and Quran importer/sync must
not be merged into a generic abstraction: their wire contracts, update rules,
and failure semantics are materially different.

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
* Quran page/ayat readers must lazily render Room-backed records and must not
  parse or hold all 6,236 ayat in a composable or on the main thread.
* Long content must use lazy rendering; no full-document parsing on every
  recomposition.
* Content package parsing must run outside the main thread.
* Large audio files must never be loaded fully into memory.
* Release builds must enable R8 resource and code shrinking (`app/build.gradle.kts`
  `buildTypes.release.optimization.enable = true`, requires
  `android.r8.gradual.support=true` in `gradle.properties` under AGP 9.2.1 —
  re-verify this flag is still required on every AGP upgrade).

## Navigation destinations (bottom-navigation-only shell through 0.0.6)

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

At `0.0.6`, the same navigation owner adds Al-Qur'an Kemenag beneath a
Beranda entry. It does not add a top-level tab, Activity, NavHost, or second
theme system. The shared bottom bar is hidden throughout the Quran back stack;
the feature applies its dark-only Quran color scheme and restores the prior app
theme when popped. Layout is portrait-primary but orientation is not forced.

## Local user-state persistence ownership

Favourites, recently-opened, and (from `0.0.2`/`0.0.3`) Standalone Tasbih
sessions and Aktivitas history are all local-only, offline-first state —
same source-of-truth rule as existing reader progress
(`docs/engineering/OFFLINE_FIRST.md`). Follow the existing
per-concern-repository convention (`GuidedReadingRepository` already
combines two Room tables behind one repository, not two) rather than one
repository per table. Field-level detail:
`docs/engineering/CONTENT_MODEL.md`.

Quran bookmarks, one global last-read position, reading settings, cached
tafsir, and reading-session events join this local ownership model at `0.0.6`.
Only the public Kemenag content/tafsir fetch crosses the network boundary; no
personal Quran state is uploaded.

## Kemenag Quran data path (planned — `0.0.6`, ADR 0016)

Use a dedicated Retrofit/OkHttp client scoped to
`https://quran-api.lpmqkemenag.id/api-alquran/`. Its `username` and `token`
headers MUST only be attached to this host/client, never to Firebase content
requests. Initialisation fetches the complete 114-surah dataset, validates
identity/count/order/uniqueness, and commits the candidate in one Room
transaction. Retry restarts initialisation; no resumable staging protocol is
needed. A seven-day connected-network refresh follows the same complete,
atomic replacement rule and retains the prior Room snapshot on failure.
Tafsir is fetched by remote ayat id, cached independently, and refreshed after
seven days without blocking an available cached result. Full security and
credential rules live in `docs/security/SECURITY_BASELINE.md`; full product
semantics live in `docs/product/QURAN_PRD.md`.

---

## Remote content synchronisation (implemented — Android side, ADR 0012/0014/0015)

The Android client against the static catalog contract served by Firebase
Hosting (§Backend below, ADR 0014/0015) is implemented:
`BundledContentBootstrapper` (bundled assets) and `ContentSyncManager`
(remote — owns both the HTTP handling and the sync algorithm) both
delegate the actual Room write to one shared `ContentImporter` — neither
knows or cares which transport produced the bytes. Room remains the sole
source of truth; the network only ever updates Room, and the UI never
observes network state directly.

* **Scheduling**: `ContentSyncScheduler.enqueueIfStale()`, called from
  `SanguSantriApplication.onCreate()`, enqueues a unique one-time
  `ContentSyncWorker` (`ExistingWorkPolicy.KEEP`, name
  `sangu-santri-content-sync`) only when the last *terminal* sync attempt
  (success or failure) is 24+ hours old or has never happened. Not a
  periodic worker. `NetworkType.CONNECTED` is a hard constraint.
* **Failure handling**: `ContentSyncManager.sync()` returns one of three
  `SyncResult`s — `Completed`/`RetryableFailure`/`PermanentFailure`.
  Retryable failures (`IOException`, timeout, HTTP 408/429/5xx) — whether
  at the catalog level or for an individual item's content-file fetch —
  get bounded exponential-backoff retries of the *whole* sync
  (`Result.retry()`, 3 attempts total; items already imported earlier
  in the same attempt are skipped on retry since Room already matches
  them). Permanent catalog-level failures (unsupported schema,
  empty/malformed body, non-retriable 4xx) record a terminal failure and
  stop. Permanent item-level failures (id/version mismatch, invalid
  structure, non-retriable 4xx) reject only that item and let the rest of
  the catalog continue — Room is never touched for the rejected item, and
  the app never crashes.
* **Base URL**: `BuildConfig.CONTENT_API_BASE_URL`, set from the Gradle
  property `SANGU_CONTENT_API_BASE_URL` (`app/build.gradle.kts`), defaulting
  to a non-routable `https://content-api.sangusantri.invalid/` when unset.
  Supplying the real Firebase Hosting URL activates real remote sync with no
  code change. Retrofit headers are never used as a local/remote
  content-source switch. There is no conditional-request header at all — the
  catalog is small and checked at most once every 24 hours, so ETag/`304`
  caching was deliberately removed as unnecessary complexity.
* **Hilt Worker**: `SanguSantriApplication` implements
  `androidx.work.Configuration.Provider` with an injected
  `HiltWorkerFactory`; the manifest removes WorkManager's default
  `androidx-startup` initializer (`tools:node="remove"` on its
  `WorkManagerInitializer` meta-data) per official Hilt+WorkManager
  guidance.
* **Response-size limiting**: `ResponseSizeLimitInterceptor`
  (`data/remote/`), an OkHttp interceptor added in `NetworkModule`, rejects
  any response body over 5 MiB, transparently for both `ContentApiService`
  calls (ADR 0015 — this replaced the previous manual per-call streaming
  cap, which existed because the old `getPackage` endpoint returned a raw
  `ResponseBody` to stream manually; `getContent` now returns an
  already-parsed `ContentFileDto`, so there is no equivalent manual
  interception point).

Full behaviour, the sync algorithm, and retention rules:
`docs/engineering/OFFLINE_FIRST.md`, `docs/engineering/CONTENT_MODEL.md`,
ADR [0012](../decisions/0012-bundled-bootstrap-and-remote-sync.md), ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md).

## Backend (Firebase Hosting static content, ADR 0014/0015)

No `backend/` directory exists in this repository, and none is planned. ADR
[0011](../decisions/0011-go-and-supabase-managed-postgresql-backend.md)'s
Go + PostgreSQL service was never implemented and was superseded by ADR
[0014](../decisions/0014-firebase-hosting-static-content-delivery.md) before
implementation started: there is no dynamic API, no database, and no
Supabase project. Content is published as static files served by Firebase
Hosting. The Android sync client above (`ContentApiService`) already only
ever issues plain `GET` requests, so it requires **no code change** to
point at a real deployment — only `SANGU_CONTENT_API_BASE_URL` needs to be
set once `content-hosting/` is actually deployed.

### Decision

Firebase Hosting serving static JSON files under a top-level
`content-hosting/` directory (parallel to `app/`, not bundled into the
APK) — a real Firebase project (`sangusantri-81cc6`) is already linked to
it. No Firestore, no Cloud Functions, no other Firebase backend product —
see ADR 0014's Alternatives rejected. A Firebase MCP server is used only as
development/CI tooling to help manage and validate that static deployment;
it never ships in the APK and is never called from production Kotlin code
— see `docs/engineering/MCP_TOOLING.md`.

### Project structure

```text
content-hosting/
├── firebase.json          # Hosting config: public dir, ignore list, cache headers
├── .firebaserc             # links this directory to the sangusantri-81cc6 project
├── public/
│   ├── index.html          # default Firebase Hosting placeholder
│   ├── 404.html
│   └── content/
│       ├── catalog.json    # ContentCatalogDto shape (docs/content-schema.md, ADR 0015)
│       ├── packages/
│       │   ├── tahlil-v1.json
│       │   └── istighosah-v1.json
│       └── images/         # empty for now — no bundled amaliyah has an image yet
└── scripts/
    └── validate-content.mjs
```

Filenames under `packages/` must exactly match each catalog item's own
`contentUrl`, since Firebase Hosting resolves them as literal static file
paths, not templated routes.

### Rules

* `content-hosting/` files are authored and reviewed the same way bundled
  assets are (`app/src/main/assets/content/`) — structured JSON, no
  Kotlin/Go code, validated by `scripts/validate-content.mjs` before
  deploy.
* Publication is `firebase deploy --only hosting`, run after validation
  passes, never a manual upload of an unvalidated file. Deploying to the
  real, already-linked `sangusantri-81cc6` project is a shared-system
  action — confirm with the team before running it.
* A published content file is never edited in place — a correction bumps
  `version` in both the catalog entry and the content file (ADR 0008,
  unaffected). The directory's git history is the durable, append-only
  revision record that ADR 0011's Postgres tables would have been.
* No secrets or service-role credentials are needed for Android to read
  this content — it is public, static, and unauthenticated by design,
  consistent with §3.4 (no forced account).

### Public content paths

* `content/catalog.json` — every content item's display metadata and
  `contentUrl` (`docs/content-schema.md`). Fetched at most once every 24
  hours, no conditional-request header.
* `content/packages/{file}` — one content file per catalog item, referenced
  by that item's own `contentUrl`, not a templated path.

There is no `/healthz` or `/v1/config` equivalent — Firebase Hosting's own
availability is Google-managed, not this project's service to
health-check, and there is currently no feature-flag/maintenance-state
need beyond what the catalog itself already expresses. Public
content-correction feedback is not part of this contract either — feedback
was removed from product scope at Milestone 5 (`docs/product/PRD.md`
FR-012); content correction is an internal SanguSantri-team operation
(`docs/operations/CONTENT_GOVERNANCE.md`), not a network endpoint.

### CI validation (replaces the former Go admin CLI)

`content-hosting/scripts/validate-content.mjs` validates
`content-hosting/public/content/` before every deploy and fails the same
way the previously planned Go admin CLI's `content validate` would have:
duplicate catalog item id, Arabic text empty, required translation empty,
an invalid repeat target, a `contentUrl` with no matching file, an id/
version mismatch between a catalog entry and its content file, an
unsupported schema version, or (optionally, given a previous catalog to
compare against) a version that regresses. There is no interactive
publish tool and no Supabase Studio equivalent, and no checksum
verification (ADR 0015 — a monotonic integer version is sufficient) — the
only way to publish is committing a valid file and passing this script.
