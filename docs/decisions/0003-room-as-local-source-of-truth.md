# 0003: Room as the local source of truth

## Status

Accepted — migration policy amended 2026-08-09

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

Room schema export remains enabled (`room.schemaLocation`) so the current
schema is reviewable. The product owner has since explicitly selected
`fallbackToDestructiveMigration(dropAllTables = true)` for every unsupported
schema transition instead of retaining a hand-written migration chain.

## Consequences

- Repositories and ViewModels added in later milestones must read from Room,
  never directly from Retrofit responses.
- An unsupported schema transition drops every Room table, including user
  state. Bundled content bootstraps again; non-bundled Quran data must be
  downloaded again before offline reading is restored. This accepted data-loss
  risk supersedes the original explicit-migration requirement.
