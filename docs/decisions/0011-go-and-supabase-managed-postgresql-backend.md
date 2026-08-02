# 0011: Go + Supabase-managed PostgreSQL backend

## Status

Superseded by ADR [0014](0014-firebase-hosting-static-content-delivery.md)
(2026-08-02) — the product owner and tech lead decided to drop the backend
entirely in favour of static content on Firebase Hosting, before this ADR
was ever implemented (no `backend/` directory ever existed). Kept below as
a historical record of the decision that was later reversed; do not use it
to justify new backend work without first reading ADR 0014.

## Context

PRD §5.1 and the backend architecture section require a Go public content
API, a Go content administration CLI, and a PostgreSQL database, with
Supabase providing managed Postgres, object storage, and a temporary admin
UI (Supabase Studio) for release `0.0.1`. This is recorded as an ADR now,
before implementation starts, because it is a genuine cross-cutting
technology commitment (language, database, managed-infra vendor) that later
sessions should not silently second-guess or replace piecemeal.

## Decision

Backend: Go (latest stable), `net/http` + Chi, `pgx`, `sqlc`, Goose (or
equivalent) migrations, `log/slog`, OpenAPI 3.1 — no heavy ORM, SQL stays
visible and reviewable. Database: PostgreSQL, managed by Supabase for
initial production, with Supabase Storage for content packages/approval
documents/future audio and Supabase Studio as the temporary (not
permanent, not publication-capable) database interface. Full detail:
`docs/engineering/ARCHITECTURE.md` §Backend.

Android must never connect directly to PostgreSQL or receive Supabase
service-role credentials — all access goes through the Go API.

## Consequences

* No backend code should be written using a different language or a
  different managed-database vendor without superseding this ADR first.
* Supabase is explicitly a managed-infrastructure choice, not a
  permanent architectural coupling — `docs/engineering/ARCHITECTURE.md`
  already scopes Supabase Studio as temporary and publication-incapable, so
  outgrowing Supabase later (e.g. self-hosted Postgres) does not require
  changing the Go/Postgres decision, only the hosting decision.
* Until this is implemented, `docs/engineering/ARCHITECTURE.md` §Backend
  and this ADR are the only places backend shape is recorded — do not let
  backend detail leak back into `docs/product/PRD.md`.
