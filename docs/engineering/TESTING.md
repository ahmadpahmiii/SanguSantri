# Testing Strategy

Applies to any task adding or changing behaviour that needs test coverage.
Claude must inspect and follow the official Android testing skill
(`testing-setup`) when establishing or extending test coverage — it
recommends testing business logic, preferring fakes, testing Room against
SQLite, using Compose UI tests, and explicitly verifying state restoration.

Per-milestone actual test results are logged in `docs/PROGRESS.md`, not
duplicated here — this document is the target scenario list, not a log.

## Android unit tests

Required coverage as features land: content manifest validation, content
version selection, sync scheduling gate
policy, guided automatic advancement, guided manual advancement, counter
increment, counter reset, completion eligibility, reading progress
restoration, repository local-first behaviour, sync failure retaining old
content, reader settings mapping.

Standalone Quran `0.0.6` adds: out-of-order ayat sorting, duplicate/missing
ayat rejection, expected surah-count validation, complete-candidate atomicity,
seven-day refresh eligibility, failed-refresh old-snapshot retention,
stale-while-revalidate tafsir, global last-read updates, bookmark idempotency,
and reading-session eligibility only after advancing at least one ayat.

## Room tests

Use an in-memory Room database on Android instrumentation for: content
package import, duplicate import (idempotency), transaction rollback,
version replacement (atomic, never downgrading), version-scoped progress
reset on replacement, database migration. See `ContentPackageImporterTest`
for the existing pattern to extend — Android intentionally has no
previous-version retention to test (superseded FR-011, ADR 0012).

Quran Room coverage must prove: a complete 114-surah import commits once;
failed or cancelled initialisation leaves no partial source snapshot; a failed
weekly refresh preserves the prior snapshot and user state; remote ayat ids
remain unique while `(surah, ayat)` is the stable local identity; tafsir and
bookmark/reading-state relations survive source refresh correctly.

## Compose UI tests

Required flows as reader UI lands: open Serambi offline, open Tahlil,
switch reader mode, increment repeated reading, complete a guided step,
restore after Activity recreation, change Arabic font size, change theme,
open source details, render Arabic RTL interface,
render landscape layout, render tablet width, render font scale `1.5`.

Quran Compose coverage must include dark-only entry from both app themes,
theme restoration on exit, hidden bottom navigation, Surah/Juz/Bookmark/last-
read tabs, loading/error/retry states, flowing Arab-only pages versus
Arab+translation rows, semantic long-click actions without visible row clutter,
tafsir bottom sheet, settings live preview, RTL Arabic semantics, large font,
and portrait-primary layout without forced-orientation assumptions.

Prefer semantic matchers. Use test tags only when semantic matching becomes
unreasonable.

## End-to-end tests

Maintain a small number of end-to-end journeys: fresh install → Tahlil →
guided reading → completion; existing content → remote update → automatic
activation; invalid remote content → current Room version remains readable.

Add one Quran journey: fresh install → connected full initialisation → read
and advance → bookmark → open tafsir → restart offline → resume/page render/
cached tafsir. Add failure journeys for initialisation retry-from-start and
seven-day refresh retaining old Room content.

Security/release tests for `0.0.6` must scan source/APK/native strings and logs
for the real Kemenag credential, verify the release build fails without secret
injection, confirm headers are attached only to the Kemenag host, exercise the
release-signature mismatch path, and verify no Latin transliteration is stored
or exposed through UI semantics. Font QA must cover every supported Quran mark
and ligature with the exact Kemenag sample corpus before a candidate ships.

## Backend tests (not yet applicable — no backend exists)

When backend work starts: manifest response (active-versions-only shape,
no conditional-request header), package retrieval, invalid feedback
payload, rate limiting, content validation, approval enforcement,
publishing transaction, revocation, database migration, storage failure,
context cancellation.
