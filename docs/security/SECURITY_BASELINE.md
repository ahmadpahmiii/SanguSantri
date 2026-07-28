# Security Baseline

Applies to any security-relevant task: release configuration, network code,
auth, or anything handling user-submitted data. Read alongside
`docs/security/PRIVACY.md` and `docs/security/THREAT_MODEL.md` (which
covers what is deliberately *not* built yet, and why).

Controls are grouped by the release phase that requires them. Do not
implement a later phase's controls early — see THREAT_MODEL.md for why that
is a real cost, not free caution.

## Required before public release `0.0.1`

* Release build must not be debuggable; release R8/shrinking must be
  enabled — **done**, `app/build.gradle.kts` `optimization.enable = true`
  (fixed as part of this documentation pass; verified with
  `./gradlew assembleRelease`).
* No CI quality gate exists yet — **blocker**, see
  `docs/engineering/RELEASE_ENGINEERING.md`.
* No dependency-vulnerability scanning configured (no Dependabot or
  equivalent) — add alongside the CI workflow when it is built.
* No sensitive secrets in source or APK — currently true (no secrets exist
  in the repo yet); keep verifying as Retrofit/OkHttp and any API keys are
  added.
* Safe exported-component configuration — currently true: only
  `MainActivity` is exported, correctly, for the `LAUNCHER` intent-filter.
  No services, receivers, or providers are exported. Re-run the installed
  `android-intent-security` skill when deep links or content-detail
  navigation args are added.
* Development fixtures must never reach the release build — the bundled
  Tahlil/Istighosah fixtures are marked non-production
  (`docs/content-schema.md`); a release-blocking validation gate that fails
  the build when only such fixtures are bundled is **not yet implemented**
  — tracked, not silently skipped.
* Room migration safety — **done**: hand-written, schema-verified,
  `MigrationTestHelper`-tested migrations, no destructive fallback (ADR
  0003).
* Content package integrity — **done**: SHA-256 checksum verification,
  tested against tamper/mismatch (`ContentChecksum`), for both bundled and
  remote packages.
* `app/src/main/keepRules/rules.keep` is currently a stub; review real
  keep-rule needs with the `r8-analyzer` skill once feature code exists to
  shrink.
* Privacy policy required before Play publication — see
  `docs/security/PRIVACY.md`.
* Crash-report redaction — required once crash reporting is wired (not yet
  wired); Arabic reading text and counter values must never appear in crash
  payloads.
* No devotional history uploaded — see FR-012 and
  `docs/security/PRIVACY.md`.

## Remote content synchronisation (FR-010, ADR 0012)

Network code now exists (Retrofit/OkHttp, `data/remote/`, `data/sync/`) —
this section is no longer forward-looking.

* `network_security_config.xml` — **still outstanding**: this project has
  no explicit network security config yet. Now that real network code
  exists, this is a near-term gap, not a "not urgent" one — it must enforce
  HTTPS-only/no cleartext before any build is used against a real,
  non-`.invalid` backend host.
* API timeouts on every outbound call — **done**: 15s connect/read/write
  timeouts (`di/NetworkModule.kt`). Backend-side rate limiting remains a
  backend concern (not yet built).
* Checksum verification and schema validation on downloaded packages —
  **done**: `ContentPackageImporter`, shared with the bundled path
  (`docs/engineering/OFFLINE_FIRST.md`).
* Immutable content versions, atomic transactional replacement, and
  per-package failure isolation — **done**, same document. Revocation
  itself remains a backend-side authority action (`docs/operations/CONTENT_GOVERNANCE.md`);
  Android has no on-device previous-version fallback to reason about
  (superseded FR-011, ADR 0012).
* Response-size limit on downloaded packages — **done**: 5 MiB cap,
  streamed to a temporary file (`ContentRemoteDataSource`).
* Backup and restore testing (backend-side; no backend exists yet).
* Structured logs and request IDs (backend-side).
* Backend dependency-vulnerability scanning.
* No content package body, full Arabic text, secret, or response payload is
  logged (`ContentRemoteDataSource`/`ContentSyncWorker` log only ids,
  counts, HTTP status codes, and exception types).

## Required before authentication and private pesantren access (`0.1.0`–`0.2.0`)

* Credential Manager or an equivalent approved authentication approach —
  see the installed `verified-email` skill when this phase starts (it is
  currently out of scope and should not be consulted before then).
* Secure token storage and token rotation.
* Logout and revocation; server-side authorisation.
* Hashed, rotatable pesantren invitation codes (never plain text).
* Brute-force protection beyond basic rate limiting.
* Membership revocation must remove future access to private content;
  previously downloaded private content must be protected or removed after
  membership loss.
* Private cache deletion or access protection.
* Account and data deletion.

## Required before quizzes or rankings (`0.4.0`)

* Abuse prevention and anti-cheating controls for the Nahwu quiz.
* Play Integrity API — only once there is something worth defrauding (quiz
  rankings); see THREAT_MODEL.md.
* Moderation procedures for quiz content and any user-submitted content.

SanguSantri is currently a non-commercial application — advertising,
subscriptions, and purchase verification are not on the roadmap
(`docs/product/ROADMAP.md`) and have no controls tracked here. Add a phase
here only if a future, explicit product decision reintroduces monetisation.

## Optional / deferred hardening

Certificate pinning, root/tamper detection, screenshot/clipboard blocking,
and enterprise secret management are **not** required at any phase reached
by the current roadmap without a concrete triggering threat. See
`docs/security/THREAT_MODEL.md` for the reasoning — do not add these as
"security theatre" defaults.
