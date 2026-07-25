# 0001: Single Android Gradle module

## Status

Accepted

## Context

SanguSantri's release `0.0.1` scope (Tahlil, Istighosah, one reader, offline
seed content) does not yet have more than one app-facing surface. PRD 13.3
recommends package boundaries that can later be extracted into Gradle feature
modules, but does not require modularisation yet.

## Decision

Use one `:app` Gradle application module. Enforce layer boundaries (`core`,
`data`, `domain`, `feature`, `navigation`, `di`) as package structure inside
that module instead of as separate Gradle modules.

## Consequences

- Faster build and simpler dependency graph while the app is small.
- Package boundaries must be respected by convention (code review, not the
  build graph) until a real modularisation need appears — for example a
  second app surface or build-time pain from a single module.
- Revisit this decision when PRD roadmap items (`0.2.0` pesantren membership,
  `0.4.0` Nahwu quiz) introduce genuinely separable feature surfaces.
