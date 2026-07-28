# Production Readiness

Applies to any release-track task. This is the engineering-owned
readiness checklist — the product/legal/governance-owned equivalent
(content, legal, store assets) is `docs/product/PRD.md` §13 "Blocking
Production Inputs"; the two lists intentionally do not overlap.

Keep this proportionate to a project run by one developer serving an
eventual public userbase of thousands — not enterprise-scale process. Do
not add infrastructure ceremony (multi-region failover, dedicated on-call
rotations, formal change-advisory boards) that this project's scale does
not need.

## Definition of Done for release `0.0.1`

Release `0.0.1` is complete only when:

* Tahlil and Istighosah content are fully entered, both with complete
  harakat, Indonesian translations by ayah/segment, and documented sources;
  content usage rights have been reviewed; no placeholder/draft-labelled
  content remains in the release. Both are published, product-owner-accepted
  standard public amaliyah (`docs/product/PRD.md` §3.1, §6.7) —
  kyai/sesepuh approval and approval documents are **not** required for this
  category and do not block release, but remain required before publishing
  any higher-risk content.
* Fresh offline installation works; full reader works; guided reader
  works; automatic and manual progression work.
* Counter progress and reading position survive process death; reader
  settings persist.
* Indonesian localisation is complete; Arabic localisation and RTL are
  complete.
* Portrait, landscape, and tablet layouts work.
* Android retains only the current active content version per variant, not
  previous versions (FR-011, ADR 0012) — the backend keeps immutable
  history instead. Remote content synchronisation (FR-010) is implemented
  and ships in `0.0.1`; a real backend deployment is not yet a release
  blocker since the app runs fully functional on bundled content alone
  until one is configured (`SANGU_CONTENT_API_BASE_URL`).
* Android tests pass (including the sync/importer test suite); a clean
  checkout builds successfully. No backend service exists or is deployed
  yet, so no backend-side tests apply — only the Android client against
  its contract.
* Privacy policy exists; store listing assets exist; final logo and app
  icon exist.
* No critical or high-severity known defect remains.

## Current release-readiness gaps (engineering)

* **No CI pipeline.** The single largest concrete gap — see
  `docs/engineering/RELEASE_ENGINEERING.md`.
* **R8 shrinking** — fixed in this pass (`optimization.enable = true`),
  verified with `./gradlew assembleRelease`. Re-verify keep rules with the
  `r8-analyzer` skill once real feature code exists to shrink.
* **No signing config** — Blocking Production Input, governance-owned, not
  a code defect.
* `versionCode`/`versionName` are wired correctly (`1` / `"0.0.1"`).
* `targetSdk = 36`, `compileSdk = 37` — already ahead of the 31 Aug 2026
  API-36 Play requirement. No action needed.
* No staged-rollout, pre-launch-report, or feature-flag infrastructure
  exists yet — appropriate given there is no CI/CD to hang it off yet;
  build it alongside the CI workflow (PRD §4.4 requires feature flags for
  any unfinished feature exposed incrementally).

## Staged rollout (target process, once CI/signing exist)

* Roll out to an internal or closed testing track first, then a small
  percentage staged rollout on production.
* **Halt criteria**: crash-free session rate drops measurably versus the
  previous release, ANR rate increases, or a Critical-severity content
  error is reported (`docs/operations/CONTENT_GOVERNANCE.md`).
* **Rollback**: halt the staged rollout in Play Console; for a content-only
  issue, revoke the affected content version server-side and publish its
  replacement instead of an app rollback — this does not require a new APK.
  Propagation to devices is bounded by each device's 24-hour sync gate, not
  instantaneous — Android has no faster on-device fallback mechanism
  (superseded FR-011, ADR 0012).

## Backup policy

Room's default Android Auto Backup is currently unscoped — `backup_rules.xml`
and `data_extraction_rules.xml` are still the unmodified template.
`reading_positions`, `guided_reading_sessions`, and `step_progress` already
exist (Milestones 3–4); scope these files to exclude those tables from
cloud backup — reading/counter progress is local-only by design
(`docs/security/PRIVACY.md`) and must not
silently leave the device via Auto Backup.

## Cost and quota alerts

Not yet applicable — no metered infrastructure exists (no backend, no audio
storage/CDN), and no downloadable-audio roadmap item currently exists (see
`docs/product/ROADMAP.md`). Revisit this section only if a future, explicit
product decision reintroduces downloadable audio.

## Production credential ownership

Not yet applicable — no production credentials exist (no backend, no
signing key, no third-party SDK keys). When they are created: record who
owns each credential, where it is stored (never in the repository), and a
rotation cadence, in a location this document will link to once it exists.
