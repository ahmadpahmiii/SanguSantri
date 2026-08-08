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
  previous versions (FR-011, ADR 0012) — `content-hosting/`'s git history
  keeps immutable history instead (ADR 0014). Remote content
  synchronisation (FR-010) is implemented and ships in `0.0.1`. The
  Firebase Hosting deployment is **live** (`sangusantri-81cc6`,
  `https://sangusantri-81cc6.web.app/`) and `SANGU_CONTENT_API_BASE_URL` is
  configured (`gradle.properties`) — verified end-to-end on a real device:
  a fresh install's opportunistic sync fetched the live catalog and content
  files and recorded `content_last_sync = SUCCESS` in `app_metadata`. The
  app still runs fully functional on bundled content alone if the network
  or host is ever unavailable — remote sync is additive, never required.
* Android tests pass (including the sync/importer test suite); a clean
  checkout builds successfully. No backend service exists, and none is
  planned (ADR 0014) — only static-file validation applies, not
  server-side tests.
* Privacy policy exists; store listing assets exist; final logo and app
  icon exist.
* No critical or high-severity known defect remains.

## Additional Definition of Done for release `0.0.6`

Al-Qur'an Kemenag is releasable only when all acceptance criteria in
`docs/product/QURAN_PRD.md` pass, including:

* Production Kemenag access is confirmed for the SanguSantri application;
  username/token are injected from untracked local/CI secrets, never committed
  or logged, and the APK/native-string scan finds no accidental plain-text
  credential. Missing secrets or signing-certificate digest fail release
  assembly; signature mismatch fails closed at runtime.
* A fresh connected install successfully validates and atomically imports all
  114 surahs; an interrupted/invalid import exposes no partial Quran; Retry
  starts cleanly from the beginning.
* Previously initialised content, bookmarks, last-read state, and settings work
  offline. A failed seven-day refresh preserves the previous Room snapshot;
  cached tafsir remains available while stale refresh fails.
* No Kemenag Latin transliteration is persisted, rendered, logged, or exposed
  through accessibility semantics. No copy/share or audio surface exists.
* Both Quran reader modes, translation switch, long-press actions, tafsir,
  dark-theme entry/exit, hidden bottom bar, portrait-primary responsive layout,
  large font, TalkBack semantic long-click, and Activity/streak event rules are
  manually verified on a representative API 26 device and current Android.
* Every shipped Arabic font has recorded redistribution permission and passes
  exact-source glyph/harakat/Quranic-mark comparison. Candidate font files in
  `docs/design/assets/quran-fonts/` are not sufficient evidence by themselves.
* The privacy policy/Data Safety assessment covers direct Kemenag requests and
  local Quran state; backup rules for devotional state are verified.
* Source attribution is displayed exactly as approved without implying that
  SanguSantri is an official Kemenag application or that Kemenag endorsed
  unrelated app content.

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

Firebase Hosting has a metered free tier (storage, bandwidth); this
project's static JSON/text content is small enough that default quotas are
expected to be sufficient, but no billing/quota alert has been configured
yet — do this before the real Firebase project is deployed. No audio
storage/CDN is needed, and no downloadable-audio roadmap item currently
exists (see `docs/product/ROADMAP.md`). Revisit the audio part of this
section only if a future, explicit product decision reintroduces
downloadable audio.

Before `0.0.6`, record any Kemenag request quota/rate-limit guidance supplied
with the private access approval. The client performs a full 114-surah initial
fetch and at most one eligible full refresh per seven days per installation,
plus user-requested tafsir calls; test this request budget and implement polite
bounded retry without inventing an undocumented quota.

## Production credential ownership

Production credential ownership applies to Firebase deployment, Android
signing, and from `0.0.6` the Kemenag username/token. Record the human owner,
authorised application/environment, storage location (never the repository),
rotation/revocation contact, and last rotation without writing the credential
itself here. Kemenag token compromise requires coordination with LPMQ and a
new hardened app release under the direct-client decision in ADR 0016.
