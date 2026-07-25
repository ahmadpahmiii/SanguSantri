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
  tested against tamper/mismatch (`SeedContentChecksum`).
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

## Required before backend synchronisation (FR-010)

* `network_security_config.xml` — must enforce HTTPS-only, no cleartext.
  Not urgent today: zero network code exists in the repository (Retrofit/
  OkHttp are declared in the stack but not yet added as dependencies).
* API timeouts and rate limits on every outbound call.
* Request-size limits on the feedback endpoint.
* Checksum verification and schema validation on downloaded packages —
  design already specified in `docs/engineering/OFFLINE_FIRST.md`; build to
  that spec directly.
* Immutable content versions, transactional import, rollback and
  revocation — same document.
* Backup and restore testing (backend-side; no backend exists yet).
* Structured logs and request IDs (backend-side).
* Backend dependency-vulnerability scanning.

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

## Required before quizzes, rankings, advertisements, or subscriptions (`0.4.0`–`0.5.0`)

* Abuse prevention and anti-cheating controls for the Nahwu quiz.
* Play Integrity API — only once there is something worth defrauding
  (quiz rankings, payments); see THREAT_MODEL.md.
* Purchase verification and fraud monitoring for subscriptions.
* Moderation procedures for quiz content and any user-submitted content.
* Advertising and analytics privacy review before any ad SDK is added.

## Optional / deferred hardening

Certificate pinning, root/tamper detection, screenshot/clipboard blocking,
and enterprise secret management are **not** required at any phase reached
by the current roadmap without a concrete triggering threat. See
`docs/security/THREAT_MODEL.md` for the reasoning — do not add these as
"security theatre" defaults.
