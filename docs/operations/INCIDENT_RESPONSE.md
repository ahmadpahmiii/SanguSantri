# Incident Response and Monitoring

Applies to any task involving crash reporting, logging, content revocation,
or recovery from a bad release or bad content push. Read alongside
`docs/operations/CONTENT_GOVERNANCE.md` for the content-specific revocation
process.

## Current state

No crash/ANR monitoring is wired yet. No server-side structured logging
exists or is planned — there is no backend (ADR 0014); Firebase Hosting is
static file serving with only basic CDN access logs, not application
logging. No named incident contact is documented anywhere. None of this
blocks current Android-only engineering work; all of it blocks public
release — see `docs/operations/PRODUCTION_READINESS.md`.

## Crash and stability monitoring

Play Console Vitals is free and should be default-on before any public
release — no third-party crash SDK is required to start. When crash
reporting is wired, redaction of Arabic reading text and counter values
must be verified before the first release (`docs/security/PRIVACY.md`) —
do not assume default SDK behavior is safe here.

## Content-version adoption monitoring

The Android sync client (FR-010, ADR 0012) exists; server-side adoption
tracking does not, and static file hosting (ADR 0014) has no natural way
to add it later without introducing exactly the dynamic backend that
decision rejected — Firebase Hosting's CDN access logs are not
per-content-version adoption data. If this signal is ever genuinely
needed, track what fraction of active installs have adopted the latest
published content version per amaliyah through a deliberate, minimal
mechanism decided at that time, not assumed to already exist. This is the
signal that tells the content-governance authority
(`docs/operations/CONTENT_GOVERNANCE.md`) whether a revocation has actually
propagated, not just whether the manifest says it should have — propagation
now depends entirely on each device's own 24-hour sync gate firing and
successfully downloading the new version, since Android no longer falls
back locally to a previous version (superseded FR-011).

## Server-side observability (not applicable, ADR 0014)

There is no backend and none is planned, so there are no request IDs, API
latency, or server-side error rates to observe — Firebase Hosting is
static file serving. Content sync success/failure is observable only on
the Android side (`ContentSyncMetadata.content_last_sync`,
`docs/engineering/ARCHITECTURE.md` §Remote content synchronisation), not
centrally aggregated. There is no feedback feature
(`docs/product/PRD.md` FR-012) to track a submission success rate for. Do
not record Arabic reading text, counter values, or personal devotional
history in logs or analytics (`docs/security/PRIVACY.md`).

## Reliability and recovery testing

* Process-death restoration is proven only for the DB/DataStore layer so
  far (existing instrumented tests). `reading_sessions`/`step_progress`
  need the same instrumented-test rigor once the reader UI exists —
  required before Serambi/reader ship, not optional polish.
* No disaster-recovery or restore-testing story exists yet — correctly not
  needed today (no backend or server-side user data, and none planned,
  ADR 0014). `content-hosting/`'s git history is itself the disaster
  recovery for published content; re-deploying is `firebase deploy
  --only hosting` from a known-good commit.
* No handling yet for partial/interrupted downloads or full-storage
  failure during sync — build to the package-import sequence already
  specified in `docs/engineering/OFFLINE_FIRST.md` directly, rather than
  discovering the failure modes iteratively in production.

## Content incident runbook

See `docs/operations/CONTENT_GOVERNANCE.md` for the full revocation
authority and severity-level process. This section covers only the
technical detection side: Android has no on-device previous-version
fallback (superseded FR-011, ADR 0012) — a revoked version is corrected by
publishing a new `version` (or hidden via a catalog entry's `isActive:
false`, ADR 0015) once the update is deployed to Firebase Hosting (ADR
0014), and each device only picks up the correction once its own 24-hour
sync gate fires
and successfully downloads the newly published replacement version. There
is no faster on-device mechanism; an urgent correction's actual propagation
speed is bounded by the sync gate, not instantaneous.

## Named contacts

Not yet documented. Before public release, record: a named
security/privacy contact (Play Store listing requires a support contact
regardless of release complexity), and the named content-revocation
authority from `docs/operations/CONTENT_GOVERNANCE.md`. A single-developer
project may have the same person in every role today — write the name down
anyway so the process survives the project growing past one person.
