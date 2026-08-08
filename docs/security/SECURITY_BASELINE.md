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
* Content integrity — version comparison only, no checksum (ADR 0015 —
  a monotonic integer version is sufficient once content is authored and
  deployed from the same git repository that serves it; the previous
  SHA-256 checksum verification and `ContentChecksum` helper were removed).
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

* `network_security_config.xml` — **done**: `app/src/main/res/xml/
  network_security_config.xml` (`base-config cleartextTrafficPermitted="false"`),
  wired via `AndroidManifest.xml`'s `android:networkSecurityConfig`. A
  `<debug-overrides>` block permits cleartext only when the APK is
  debuggable — a platform-enforced guarantee, not a build convention — so
  `ContentSyncManagerTest` can talk to a local MockWebServer instance;
  release builds are HTTPS-only exactly as this bullet originally required.
* API timeouts on every outbound call — **done**: 15s connect/read/write
  timeouts (`di/NetworkModule.kt`). There is no server to rate-limit —
  Firebase Hosting has no request-processing logic of its own to abuse
  beyond standard CDN bandwidth limits (ADR 0014).
* Schema validation on downloaded content — **done**: `ContentValidator`/
  `ContentImporter`, shared with the bundled path
  (`docs/engineering/OFFLINE_FIRST.md`). No checksum verification any more
  (ADR 0015) — version comparison is the only integrity signal, sufficient
  for content authored and deployed from the same git repository that
  serves it.
* Immutable content versions, atomic transactional replacement, and
  per-item failure isolation — **done**, same document. Revocation itself
  remains a content-governance authority action (`docs/operations/
  CONTENT_GOVERNANCE.md`) enacted by publishing a corrected `version` (or
  setting a catalog entry's `isActive` to `false`) and redeploying, not a
  backend action; Android has no on-device previous-version fallback to
  reason about (superseded FR-011, ADR 0012).
* Response-size limit on downloaded content — **done**: 5 MiB cap,
  enforced transparently by `ResponseSizeLimitInterceptor` (an OkHttp
  interceptor, ADR 0015 — replaces the previous manual per-call streaming
  cap in `ContentSyncManager`, since `getContent` now returns an
  already-parsed DTO rather than a raw body to stream manually).
* Backup and restore testing — not applicable to a static hosting target;
  `content-hosting/`'s git history is the restore mechanism (ADR 0014).
* Structured logs and request IDs — not applicable; there is no backend
  and none is planned.
* Dependency-vulnerability scanning for the CI content-validation script
  and any Firebase MCP tooling used in CI (`docs/engineering/
  MCP_TOOLING.md`) — still outstanding.
* No content package body, full Arabic text, secret, or response payload is
  logged (`ContentSyncManager`/`ContentSyncWorker` log only ids,
  counts, HTTP status codes, and exception types).

## Required before standalone Al-Qur'an Kemenag (`0.0.6`)

ADR [0016](../decisions/0016-standalone-quran-kemenag-direct-api.md) accepts
a direct Android-to-Kemenag integration because the issued access is for the
SanguSantri application and no proxy will be built. This means the credential
can be made harder to extract, but cannot be made secret from a determined
attacker who controls the APK/device. The following hardening is mandatory:

* Never commit the real `username` or `token`, including in Kotlin, C++, Gradle
  files, resources, generated source, logs, tests, screenshots, or docs.
* Release builds read the credential from an untracked local/CI secret and
  generate native build input; the build must fail clearly when the required
  production value is absent. Tests/debug fixtures use an unmistakably fake
  credential.
* Reconstruct split/encoded fragments inside a small C++/NDK boundary only at
  request time, expose the shortest-lived value practical to Kotlin, strip
  native symbols, and keep release R8/resource shrinking enabled. Obfuscation
  is defence-in-depth, not a claim of secure storage.
* Verify the release signing-certificate digest before returning the native
  credential. A mismatch fails closed and never logs either the expected or
  actual credential material.
* Use a dedicated Kemenag Retrofit/OkHttp client. Its `username` and `token`
  interceptor must be host-scoped to `quran-api.lpmqkemenag.id`; it must never
  decorate Firebase or arbitrary redirected requests.
* HTTPS-only network security, bounded timeouts/response sizes, structural
  response validation, and redacted diagnostics are required. Never log
  headers, full URLs containing secrets, bodies, Arabic, translations, tafsir,
  bookmarks, or reading positions.
* Credential rotation requires issuing a new app release unless Kemenag
  provides a different authorised mechanism. A leaked/revoked credential is a
  release incident, not something Room synchronisation can repair.

Certificate pinning and root detection are not substitutes for protecting a
static client credential and remain deferred absent a separate concrete
threat. No code may claim the NDK makes the token safe from reverse
engineering.

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

## Required before quiz rankings (blocked on Accounts/Pesantren Membership)

Nahwu Quiz moved from `0.4.0` to `0.0.5` (ADR
[0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md)),
but `0.0.5` itself is individual/guest/offline-only — no ranking, no
pesantren representation, no server-verified competitive scoring, and
therefore none of this section's controls are required at `0.0.5`. This
section applies only once a *later*, currently unscheduled release adds
competitive quiz rankings (which needs Accounts `0.1.0`/Pesantren
Membership `0.2.0` first):

* Abuse prevention and anti-cheating controls for the Nahwu quiz.
* Play Integrity API — only once there is something worth defrauding (quiz
  rankings); see THREAT_MODEL.md.
* Moderation procedures for quiz content and any user-submitted content.
* Server-authoritative answer verification — local score/answer-key data
  is never trusted for ranking (the bundled `0.0.5` question JSON,
  including `correctOptionId`, is extractable from the APK/device by
  design; this is a documented, accepted limitation for the individual/
  offline release, not a gap to close before `0.0.5` ships).

SanguSantri is currently a non-commercial application — advertising,
subscriptions, and purchase verification are not on the roadmap
(`docs/product/ROADMAP.md`) and have no controls tracked here. Add a phase
here only if a future, explicit product decision reintroduces monetisation.

## Optional / deferred hardening

Certificate pinning, root/tamper detection, screenshot blocking, and
enterprise Vault/HSM management are **not** required at any phase reached by
the current roadmap without a concrete triggering threat. The `0.0.6` native
credential hardening above is required and is not enterprise secret
management. Clipboard actions are intentionally absent from the Quran UI, but
the app does not block system screenshots. See
`docs/security/THREAT_MODEL.md` for the reasoning — do not add these as
"security theatre" defaults.
