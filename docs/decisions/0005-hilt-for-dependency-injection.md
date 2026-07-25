# 0005: Hilt for dependency injection

## Status

Accepted

## Context

PRD 13.1 specifies Hilt. The app needs a DI graph for Room, DataStore, and
(in later milestones) Retrofit/OkHttp and repositories, without hand-rolled
service locators.

## Decision

Use Hilt (`com.google.dagger.hilt.android` Gradle plugin,
`hilt-android` + `hilt-android-compiler` via KSP). `SanguSantriApplication` is
annotated `@HiltAndroidApp`; `MainActivity` is `@AndroidEntryPoint`.
Milestone 0 adds two `SingletonComponent` modules: `DatabaseModule` (Room) and
`DataStoreModule` (preferences `DataStore`). Instrumented tests use
`hilt-android-testing` with a custom `HiltTestRunner` /
`HiltTestApplication`.

`androidx.hilt:hilt-navigation-compose` is deferred until the first
`@HiltViewModel` is introduced by a feature screen — Milestone 0 has no
ViewModels yet.

## Consequences

- New singletons (Retrofit, sync components, repositories) are added as
  additional `SingletonComponent` modules, not as ad hoc top-level objects.
- ViewModels use `@HiltViewModel` with constructor injection; Compose
  screens obtain them via `hiltViewModel()` once added.
