# 0003: Room as the local source of truth

## Status

Accepted

## Context

PRD 12.1 and CLAUDE.md require that Room be the canonical local store for
application content and that the UI never render directly from network
responses. This is also official Android offline-first guidance.

## Decision

Add a single `SanguSantriDatabase` (Room) as the app's local source of truth,
provided as a Hilt singleton. Milestone 0 ships it with one infrastructure
entity, `app_metadata` (a generic key-value table), and no content entities —
the canonical content model (amaliyah / variant / version / step) is defined
when content import is implemented, not invented ahead of that milestone.

Room schema export is enabled (`room.schemaLocation`) from the start so
migrations are testable and destructive migration can be avoided, per PRD
16.1.

## Consequences

- Repositories and ViewModels added in later milestones must read from Room,
  never directly from Retrofit responses.
- The first schema migration (adding content entities) must ship an explicit,
  tested `Migration`, not `fallbackToDestructiveMigration`.
