# Architecture

Applies to any UI, domain, or data-layer task. Read alongside
`docs/engineering/CODING_STANDARD.md` (naming/Compose rules) and
`docs/engineering/CONTENT_MODEL.md` (data shape).

## Technology stack

Kotlin, Jetpack Compose, Material 3, Navigation 3, Hilt, Kotlin coroutines,
Flow/StateFlow, Room, DataStore, WorkManager, Retrofit, OkHttp, Kotlinx
Serialization, AndroidX Lifecycle, AndroidX adaptive layout APIs, Gradle
Kotlin DSL + version catalog, JUnit, Compose UI testing, Android Lint,
Detekt, ktlint.

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
* A roadmap item (`0.2.0` pesantren membership, `0.4.0` Nahwu quiz)
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
│   ├── local (dao, database, entity)
│   ├── remote (api, dto)
│   ├── mapper
│   ├── repository
│   └── sync
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── feature
│   ├── home
│   ├── reader
│   ├── contentdetail
│   ├── settings
│   ├── feedback
│   └── about
├── navigation
└── di
```

`feature/` is currently empty — Serambi and the reader are not implemented
yet (`docs/PROGRESS.md`).

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

---

## Backend (planned — not started)

No `backend/` directory exists in this repository yet. Nothing below is
implemented; this section exists so the decision and shape are not lost
between now and whenever backend work actually starts. Do not build this
ahead of an explicit request (Current Engineering Priority #5: avoid
premature backend complexity).

### Decision

Go (latest stable) + PostgreSQL, Supabase-managed for initial production,
Supabase Storage for content packages/approval documents/future audio,
Supabase Studio as the temporary DB interface, a custom Go API between
Android and the database, and a Go admin CLI for validation and publication.
Android must not connect directly to PostgreSQL or expose Supabase service
credentials. See ADR
[0011](../decisions/0011-go-and-supabase-managed-postgresql-backend.md).

### Preferred stack

`net/http`, Chi router, `pgx`, `sqlc`, Goose (or equivalent SQL migration
tool), `log/slog`, OpenAPI 3.1, standard Go testing, Testcontainers/Docker
PostgreSQL for integration tests, `golangci-lint`. Avoid a heavy ORM — SQL
must stay visible, reviewable, and testable.

### Project structure

```text
backend/
├── cmd/{api,admin}
├── internal/{config,content,feedback,httpapi,storage,database}
├── migrations
├── queries
├── openapi
├── testdata
├── Dockerfile
├── compose.yaml
├── go.mod
└── README.md
```

### Rules

Package names describe business responsibilities. Interfaces are declared
by consumers — do not introduce a repository interface for every database
query. Database access accepts `context.Context`; every outbound operation
uses a timeout; transactions are explicit. Generated `sqlc` files are never
manually edited. Errors retain their original cause. Public API errors use
consistent codes. Logs are structured. Secrets come from environment
variables or a secret manager — no global mutable database client, no
business logic in HTTP handlers, no HTTP-specific types inside core content
logic.

### Initial public endpoints

* `GET /healthz` — service health.
* `GET /v1/config` — supported content schema, minimum app version, feature
  flags, maintenance state.
* `GET /v1/content/manifest` — active content versions, checksums, download
  locations, revocations, minimum app version, ETag.
* `GET /v1/content/packages/{versionID}` — immutable content package.
* `POST /v1/feedback` — anonymous correction feedback; body-size limits,
  input validation, basic rate limiting, request identifier, structured
  error response required.

### Admin CLI

`content validate|import|review|approve|publish|revoke|list|export`.
Publication must fail when approval is missing/invalid, Arabic text is
empty, required translation is empty, positions are duplicated, a repeat
target is invalid, a Quran reference is incomplete, a checksum cannot be
generated, or the schema is unsupported. Supabase Studio may edit draft
data but must not be the publication mechanism.
