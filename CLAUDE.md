# SanguSantri Engineering Instructions

## Project

SanguSantri is a long-term Android application for public and pesantren-specific amaliyah.

Current target:

* Platform: Native Android
* UI: Jetpack Compose
* Package: `com.sangusantri.app`
* Minimum SDK: 26
* Current release: `0.0.1`
* Current content: Tahlil and Istighosah
* Architecture: Offline-first Clean Architecture

Before making changes, read:

1. `docs/product/PRD.md`
2. `docs/PROGRESS.md`
3. Relevant files in `docs/decisions/`

Do not implement the entire PRD unless explicitly requested.

## Current Engineering Priorities

1. Ship a small working release.
2. Keep religious content outside Kotlin source files.
3. Make all public amaliyah available offline.
4. Keep one canonical content model.
5. Preserve reading and counter progress.
6. Avoid premature backend and architecture complexity.

## Architecture Rules

Use these boundaries:

* UI: Compose, ViewModel, UI state and user actions.
* Domain: business models and meaningful business rules.
* Data: Room, DataStore, remote API, repositories and synchronisation.

Room is the source of truth for application content.

The UI must never render directly from network DTOs.

Do not create:

* `BaseViewModel`
* `BaseRepository`
* Generic `BaseUseCase`
* Pass-through use cases
* Duplicate models without a boundary reason
* Duplicate navigation systems
* Duplicate themes or design tokens
* DAO access inside ViewModels
* Network calls inside composables
* Religious content inside Kotlin files

Create a use case only when it contains meaningful or reusable business logic.

Use one Android Gradle application module until project complexity demonstrates a real need for modularisation.

## Compose Rules

* Use stateless screen composables.
* Use Route composables for ViewModel integration.
* Expose screen state with `StateFlow`.
* Collect state lifecycle-aware.
* Pass state and callbacks to child composables.
* Use stable keys in lazy lists.
* Keep business logic outside composables.
* Use string resources.
* Support RTL, landscape and large screens.
* Do not add `@Stable` or `@Immutable` without evidence.
* Do not use `GlobalScope`.
* Do not perform blocking work on the main thread.

## Content Safety

Claude must not:

* Invent Arabic amaliyah text.
* Invent translations.
* Automatically scrape and publish religious content.
* Claim that content has been approved.
* Modify approved content without creating a new version.

Development fixtures must be clearly labelled as non-production.

Production religious content requires an external kyai or sesepuh approval.

## Working Method

For every task:

1. Inspect the existing implementation.
2. Search before creating a new class.
3. State which files will change.
4. Implement only the requested milestone.
5. Run relevant formatting, build and tests.
6. Fix failures caused by the change.
7. Update `docs/PROGRESS.md`.
8. Do not repeat complete source files in the final response.

Never claim that a command passed unless it was actually executed successfully.

## Final Response Format

Report only:

* What was implemented
* Files created
* Files modified
* Commands executed
* Test results
* Known limitations
* Next recommended milestone
