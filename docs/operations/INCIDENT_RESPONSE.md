# Incident Response and Monitoring

Applies to any task involving crash reporting, logging, content revocation,
or recovery from a bad release or bad content push. Read alongside
`docs/operations/CONTENT_GOVERNANCE.md` for the content-specific revocation
process.

## Current state

No crash/ANR monitoring is wired yet. No backend structured logging exists
(no backend exists). No named incident contact is documented anywhere. None
of this blocks current Android-only engineering work; all of it blocks
public release — see `docs/operations/PRODUCTION_READINESS.md`.

## Crash and stability monitoring

Play Console Vitals is free and should be default-on before any public
release — no third-party crash SDK is required to start. When crash
reporting is wired, redaction of Arabic reading text and counter values
must be verified before the first release (`docs/security/PRIVACY.md`) —
do not assume default SDK behavior is safe here.

## Content-version adoption monitoring

The Android sync client (FR-010, ADR 0012) exists; server-side adoption
tracking does not yet, since it requires the backend (not yet deployed).
Once the backend exists, track what fraction of active installs have
adopted the latest published content version per amaliyah. This is the
signal that tells the content-governance authority
(`docs/operations/CONTENT_GOVERNANCE.md`) whether a revocation has actually
propagated, not just whether the manifest says it should have — propagation
now depends entirely on each device's own 24-hour sync gate firing and
successfully downloading the new version, since Android no longer falls
back locally to a previous version (superseded FR-011).

## Backend observability (once backend exists)

Structured logs, request IDs, API latency, error rates, content sync
success rate. There is no feedback feature (`docs/product/PRD.md` FR-012)
to track a submission success rate for. Do not record Arabic reading text,
counter values, or personal devotional history in logs or analytics
(`docs/security/PRIVACY.md`).

## Reliability and recovery testing

* Process-death restoration is proven only for the DB/DataStore layer so
  far (existing instrumented tests). `reading_sessions`/`step_progress`
  need the same instrumented-test rigor once the reader UI exists —
  required before Serambi/reader ship, not optional polish.
* No disaster-recovery or restore-testing story exists yet — correctly not
  needed today (no backend, no server-side user data). Design this into
  the first sync implementation rather than retrofitting it later.
* No handling yet for partial/interrupted downloads or full-storage
  failure during sync — build to the package-import sequence already
  specified in `docs/engineering/OFFLINE_FIRST.md` directly, rather than
  discovering the failure modes iteratively in production.

## Content incident runbook

See `docs/operations/CONTENT_GOVERNANCE.md` for the full revocation
authority and severity-level process. This section covers only the
technical detection side: Android has no on-device previous-version
fallback (superseded FR-011, ADR 0012) — a revoked version stops being
listed as a variant's active version in the backend's manifest, and each
device only picks up the correction once its own 24-hour sync gate fires
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
