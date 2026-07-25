# 0004: Navigation 3 for Compose navigation

## Status

Accepted

## Context

PRD 13.1 requires a stable, Compose-native navigation system with explicit
back-stack ownership. Navigation 3 (`androidx.navigation3`) reached a stable
release and is designed exclusively for Compose, unlike Navigation 2.

## Decision

Use Navigation 3 (`navigation3-runtime`, `navigation3-ui`) with
`kotlinx.serialization` for `@Serializable` `NavKey` routes and
`rememberNavBackStack` for a back stack that survives configuration changes
and process death.

Milestone 0 ships a single placeholder destination (`Home`) to prove the
`NavDisplay` + back-stack wiring compiles and renders; it is intentionally not
Serambi. The `androidx.lifecycle:lifecycle-viewmodel-navigation3` add-on is
deferred until the first destination has a ViewModel.

## Consequences

- Only one level of navigation nesting and no deep links are supported by the
  current recipes used; revisit if a feature milestone needs them.
- Real destinations replace the placeholder screen-by-screen; the
  `entryProvider` `when` block grows with them.
