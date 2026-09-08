# Coding Standard

Applies to any code-writing task. Read alongside
`docs/engineering/ARCHITECTURE.md` for layer boundaries.

## Compose rules

Claude must:

* Hoist screen state; keep composables side-effect-safe.
* Use lifecycle-aware `Flow` collection.
* Use stable keys in lazy collections.
* Pass state and callbacks into child composables instead of ViewModels.
* Keep business logic outside composables.
* Preserve reader state across configuration and process recreation.
* Add previews for reusable visual components.
* Use string resources — no hardcoded user-facing strings.
* Use dimension and typography tokens from `core/designsystem`.
* Support font scaling.
* Add semantics to counters and navigation controls.
* Avoid unnecessary recomposition.
* Never use `GlobalScope`, blocking work on the main thread, or `!!` without
  a documented invariant.

Do not add `@Stable` or `@Immutable` without evidence (a profiler trace
showing unnecessary recomposition), and do not store domain state with plain
`remember` — it does not survive process death.

## Prohibited patterns

Claude must not introduce:

* `BaseViewModel`, `BaseRepository`, generic `BaseUseCase`.
* A generic application-wide `UiState`.
* God ViewModels, god repositories, god composables.
* Network calls from composables; DAO calls from ViewModels; API services,
  sync managers or schedulers injected into ViewModels (see Data layer below).
* A second network-result type alongside `ApiResult`, or a second
  validation-result type alongside `Validation`.
* Hand-rolled `try`/`catch (IOException)` around a Retrofit call — use
  `safeApiCall`.
* Hardcoded Arabic religious content in Kotlin, hardcoded user-facing
  strings, hardcoded production URLs, secrets in source control.
* `GlobalScope`; a custom or partial destructive database migration that
  bypasses the product-owner-approved Room drop-all fallback; silent exception
  swallowing.
* Duplicate mappers, duplicate design tokens, multiple competing navigation
  frameworks.
* Alpha dependencies without justification.
* Empty interfaces, or interfaces created only to satisfy a diagram.
* Comments that merely restate the code.
* Fake religious content presented as real content (see `CLAUDE.md` Content
  Safety).
* Build-success claims without execution evidence.

## Data layer — one shape, no exceptions

Established 2026-09-08 by a principal-level review that found two contradictory
offline-first architectures running side by side, eight vocabularies for "did the
network call work", and the CMS client living in three ViewModels.

### The layering

```
Room (source of truth)   ←  LocalDataSource   ←┐
                                               ├─ RepositoryImpl  →  ViewModel
Retrofit (upstream)      ←  RemoteDataSource  ←┘
```

* **The repository owns the network.** It is the only layer that knows both
  sides exist. A repository interface that forbids itself from refreshing is not
  a repository, it is a DAO facade — and the network then has nowhere to live but
  the ViewModel.
* **A ViewModel never injects an API service, a sync manager, a scheduler, or a
  DAO.** If a screen needs a refresh, it collects a flow that refreshes itself.
* **A `LocalDataSource` owns transactional Room writes** — the multi-table,
  all-or-nothing kind. Single-table reads go straight through the DAO.
* **A `RemoteDataSource` owns `safeApiCall`, envelope unwrapping and structural
  validation.** Retrofit's `Response`, HTTP status codes and
  `SerializationException` stop there. Nothing above it imports `retrofit2`.
* Do not create a `*SyncManager` or `*Importer`. Those names describe a pipeline
  that no longer exists; the write path of a repository belongs in that
  repository. A `WorkManager` worker is the exception, and it calls the
  repository like everyone else.

### Reading: `networkBoundResource`

Every read that has both a cache and an upstream goes through
`core/result/NetworkBoundResource.kt`. Do not hand-roll "read cache, fire
refresh, re-read":

```kotlin
override fun observeThing(): Flow<Resource<List<Thing>>> = networkBoundResource(
    query = { dao.observeAll().map { it.map(Entity::toDomain) } },
    fetch = ::refreshThings,          // performs the refresh AND persists it
    isEmpty = List<Thing>::isEmpty,
)
```

`fetch` returns only an outcome, never data — everything rendered comes from
`query`, so a network DTO cannot reach the UI and a partial refresh cannot leave
the screen disagreeing with Room.

`Resource` carries `data` in all three states. A screen renders the cache on
`Error` and stays quiet; `Resource.isUnavailable()` is the one case worth
interrupting someone for (nothing cached *and* the refresh failed).

### Calling: `safeApiCall`

One function, `core/network/SafeApiCall.kt`. It catches `IOException` and
`SerializationException`, checks `isSuccessful`, null-checks the body, and logs
once. Compose it with `.unwrap(source)` for a service envelope and
`.validate(source, ::validator)` for structural checks:

```kotlin
suspend fun getThing(id: String): ApiResult<ThingDto> {
    val source = "thing/$id"
    return safeApiCall(source) { api.getThing(id) }.unwrap(source).validate(source, Validator::validate)
}
```

* **`ApiResult` is the only network-result type.** Do not invent
  `FooSyncResult`, `FooFetchOutcome`, or return a bare `Boolean` or
  `kotlin.Result` from a data source. `Failure.isRetryable` is the single answer
  to "should I try again", and computing it anywhere else means two answers.
* `runCatching { … } ?: error("unavailable")` around an HTTP call is prohibited:
  it erases the distinction between "offline, retry later" and "the server is
  answering wrongly, retrying is pointless".
* **`Validation` is the only validation-result type** (`core/validation/`).
  Validators return `Validation.of(reasonOrNull)`.
* A response body whose service reports success in a body field implements
  `ApiEnvelope`, so `unwrap` handles it — do not check `status`/`code` at the
  call site.

### Endpoints

Where two routes differ only by a path segment, take the segment as a parameter
against a closed enum. Four methods and a boolean at every call site
(`refresh(id, isSholawat = true)`) is how the wrong endpoint gets asked.

## Formatting

`.editorconfig` sets `ktlint_code_style = intellij_idea`. ktlint's own default
(`ktlint_official`) forces a class with an annotated constructor onto three
lines and indents the whole body an extra level; nobody chose it, and it left
the repo 5,124 violations out of compliance with itself.

Write:

```kotlin
@HiltViewModel
class FooViewModel @Inject constructor(
    private val repository: FooRepository,
) : ViewModel()
```

`ktlintFormat` is safe to run and `ktlintCheck` must be clean before any commit.

## No-duplication rule

Before adding a class, search the repository for an existing equivalent.
Keep one canonical class per responsibility, one canonical content model,
one canonical theme system, one canonical navigation state. Reuse existing
components when behaviour and appearance are genuinely equivalent. Extract
shared code only after a real duplication or stable common concept exists —
three similar lines is not a duplication problem.

## Working method

1. Inspect the existing repository and Gradle/version-catalog state before
   writing code.
2. Search for existing classes before creating new ones.
3. Consult relevant official Android documentation and installed
   `.agents/skills` where genuinely relevant to the task — do not apply an
   unrelated skill merely because it exists.
4. Produce a concise implementation plan and state which files will be
   created or changed before editing.
5. Implement only the requested milestone or feature.
6. Run relevant formatting, build, and test commands; fix failures caused
   by the change.
7. Report commands executed and their actual results — never claim a build
   or test passed without executing it.

## Reference policy

| Engineering category                           | Consult                                                                                                                    | Do not copy                                                                                                        |
|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| Compose navigation                             | `android/nav3-recipes` (stable), cross-checked against `compose-samples`                                                   | `nav3-recipes` multi-module wiring (`modular-hilt.md`/`modular-koin.md`) — this project stays single-module        |
| General app architecture (UI/domain/data, UDF) | `android/architecture-samples`                                                                                             | Its multi-module layout and one-use-case-per-repository-method pattern — pass-through use cases are forbidden here |
| Design system / reader layout                  | `compose-samples`: Jetnews (long-form reader text), Reply (adaptive list-detail), Jetcaster (offline-first patterns)       | Jetsnack's gradient/shape-heavy visual language — wrong tone for a devotional reader                               |
| Testing                                        | installed `testing-setup` skill; `compose-samples` test suites; `nowinandroid` only for Roborazzi screenshot-testing setup | `nowinandroid`'s module-per-feature test source-set layout                                                         |
| Offline-first sync (FR-010, implemented)       | `nowinandroid`, narrowly for sync-then-render-from-Room and WorkManager scheduling                                         | Its multi-module sync/data/datastore split and nav-graph merging                                                   |
| Adaptive layout                                | official Android adaptive-layout docs, installed `adaptive` skill, `compose-samples` Reply                                 | —                                                                                                                  |
| R8/shrinking                                   | installed `r8-analyzer` skill, official R8 docs                                                                            | —                                                                                                                  |
| Android security                               | installed `android-intent-security` skill, OWASP MASVS/MASTG, official Play policy docs                                    | —                                                                                                                  |
