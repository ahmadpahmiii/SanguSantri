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
* Network calls from composables; DAO calls from ViewModels.
* Hardcoded Arabic religious content in Kotlin, hardcoded user-facing
  strings, hardcoded production URLs, secrets in source control.
* `GlobalScope`; destructive database migration; silent exception
  swallowing.
* Duplicate mappers, duplicate design tokens, multiple competing navigation
  frameworks.
* Alpha dependencies without justification.
* Empty interfaces, or interfaces created only to satisfy a diagram.
* Comments that merely restate the code.
* Fake religious content presented as real content (see `CLAUDE.md` Content
  Safety).
* Build-success claims without execution evidence.

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
