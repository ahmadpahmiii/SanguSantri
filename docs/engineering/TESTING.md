# Testing Strategy

Applies to any task adding or changing behaviour that needs test coverage.
Claude must inspect and follow the official Android testing skill
(`testing-setup`) when establishing or extending test coverage — it
recommends testing business logic, preferring fakes, testing Room against
SQLite, using Compose UI tests, and explicitly verifying state restoration.

Per-milestone actual test results are logged in `docs/PROGRESS.md`, not
duplicated here — this document is the target scenario list, not a log.

## Android unit tests

Required coverage as features land: content manifest comparison, content
checksum validation, content version selection, sync scheduling gate
policy, guided automatic advancement, guided manual advancement, counter
increment, counter reset, completion eligibility, reading progress
restoration, repository local-first behaviour, sync failure retaining old
content, feedback outbox state transitions, reader settings mapping.

## Room tests

Use an in-memory Room database on Android instrumentation for: content
package import, duplicate import (idempotency), transaction rollback,
version replacement (atomic, never downgrading), version-scoped progress
reset on replacement, database migration. See `ContentPackageImporterTest`
for the existing pattern to extend — Android intentionally has no
previous-version retention to test (superseded FR-011, ADR 0012).

## Compose UI tests

Required flows as reader UI lands: open Serambi offline, open Tahlil,
switch reader mode, increment repeated reading, complete a guided step,
restore after Activity recreation, change Arabic font size, change theme,
open source details, submit feedback offline, render Arabic RTL interface,
render landscape layout, render tablet width, render font scale `1.5`.

Prefer semantic matchers. Use test tags only when semantic matching becomes
unreasonable.

## End-to-end tests

Maintain a small number of end-to-end journeys: fresh install → Tahlil →
guided reading → completion; existing content → remote update → automatic
activation; offline feedback → network restoration → submission; revoked
latest version → fallback to previous approved version.

## Backend tests (not yet applicable — no backend exists)

When backend work starts: manifest response (active-versions-only shape,
no conditional-request header), package retrieval, invalid feedback
payload, rate limiting, content validation, approval enforcement,
publishing transaction, revocation, database migration, storage failure,
context cancellation.
