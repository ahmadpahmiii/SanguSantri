# SanguSantri — Architecture, Documentation & Production-Readiness Audit

**Audited:** `ahmadpahmiii/SanguSantri`, branch `master`, commit `2bf914a` ("milestone 2"), 25 July 2026.
**Scope:** read-only. No code or documentation was modified.

Before anything else: **the framing of this request assumes more progress than the repository shows.** `docs/PROGRESS.md` documents Milestone 0 (project foundation) and Milestone 1 (content model + seed import) as complete. The latest commit is titled "milestone 2," but Serambi does not exist — `SanguSantriNavHost.kt` still renders a single placeholder destination, and `strings.xml` literally says *"Engineering foundation build — Serambi is not implemented yet."* There is no `feature/`, no ViewModel, no reader UI, no navigation beyond one placeholder screen anywhere in the codebase. This audit evaluates what actually exists and treats "Milestone 3 readiness" as the question it really is: *is this project on a track that will safely reach Milestone 3, given where it stands today.*

---

## Critical Findings

Only issues that should be fixed before further milestone work continues.

1. **Release build has R8/shrinking explicitly disabled.** `app/build.gradle.kts`: `buildTypes.release.optimization.enable = false`. This directly contradicts PRD §16.2 ("Release builds must enable R8 resource and code shrinking") and Definition-of-Done §20. `app/src/main/keepRules/rules.keep` exists and is wired via AGP 9's `keepRules` source set but is never exercised because optimization is off. This is a shippability blocker, not a style nit — fix before any release-track work.
2. **No CI exists.** There is no `.github/workflows/` directory at all, despite PRD §18 mandating GitHub Actions and §20 listing CI as a Definition-of-Done item. Nothing currently stops a broken build, a failing test, or an unformatted file from landing on `master`. Given the project is now public, this is the single most consequential engineering gap.
3. **PRD.md mixes product requirements with engineering/agent instructions that duplicate CLAUDE.md almost verbatim.** Roughly 60% of the document's 2,105 lines (§13 Android architecture, §14 Go architecture, §17.6/§18 CI commands, §23 "Claude Engineering Contract," §24 a scripted first-implementation prompt) is engineering process content, not product requirements. CLAUDE.md already states the architecture rules, prohibited patterns, and working method more concisely. Two sources of truth for the same rules is a maintenance liability, not a redundancy-tolerant safety net — see next finding.
4. **The duplication has already started to drift.** PRD §6.3 forbids "Latin transliteration" as one clause among many content rules; CLAUDE.md's Content Safety section — which exists specifically to enumerate these rules for the agent — does not restate it. Nothing is broken yet, but this is exactly the failure mode duplicated rules produce: the two documents will silently diverge over time, and whichever one a future agent reads first becomes the accidental source of truth.
5. **`docs/ROADMAP.md` is tracked in git and completely empty (0 bytes).** A future agent asked to "check the roadmap" will find nothing, when the real roadmap (10 releases, `0.0.1` through `0.5.0`) lives inside PRD §21–22 instead.
6. **No `README.md` anywhere.** The repository is now public with zero landing documentation — no project description, no setup instructions, nothing for a human or a cold-started agent to orient from.
7. **The design system has no spacing, shape, or elevation tokens yet** — only `Color.kt` (palette) and a single `bodyLarge` `TextStyle` in `Type.kt` exist. This is *appropriate* today (nothing consumes those tokens yet), but neither CLAUDE.md nor PRD §13.8 explicitly bans the visual anti-patterns this audit was asked to reject (card walls, gradients, glassmorphism, generic hero sections, pseudo-Arabic Latin fonts). Building Serambi against undefined restraint tokens and an unstated anti-pattern list is how you get a generic AI-generated home screen. Fix before Serambi implementation starts, not after.

---

## Keep

Current decisions and implementations that should remain unchanged.

The **ADRs (`docs/decisions/0001`–`0006`)** are the best-executed documentation in the repository: one decision per file, Status/Context/Decision/Consequences, genuinely short (27–61 lines), and grounded in real specifics (the AGP-9/KSP version-pinning story in ADR 0002 is exactly the kind of hard-won detail a decision record should preserve). Use this format as the template for every future decision, including the ones this audit recommends below.

The **seed content import pipeline** (`SeedContentImporter`, `SeedContentValidator`, `SeedContentChecksum`) is the strongest engineering in the repo: checksum verification → structural validation → transactional import, with per-package isolation and idempotency, backed by four real instrumented tests covering first import, duplicate-import idempotency, checksum-mismatch rejection, and mid-import rollback on a genuine SQLite constraint failure. When the remote sync importer (FR-010) is built, it should reuse this exact shape rather than becoming a second, parallel import implementation.

**Room as source of truth** is correctly implemented, not just documented: the domain layer never sees entities, mappers exist only at the data boundary, and the v1→v2 migration is hand-written, verified against the Room-exported schema JSON, and tested with `MigrationTestHelper` against a real upgraded database — no destructive fallback, exactly matching ADR 0003's commitment.

**Architectural restraint is real in code, not just asserted in docs.** There is no `BaseViewModel`, no generic `UseCase`, no pass-through wrapper, no premature `feature/` package — because nothing needs one yet. The "no ceremony" principle in CLAUDE.md §Architecture Rules is currently being honored by omission, which is the correct way to honor it.

The **single-Gradle-module decision (ADR 0001)** is correct at this size and should not be revisited by this audit's own instruction — do not read anything below as a nudge toward Now in Android–style modularization.

The **Gradle version catalog** reflects real judgment: AGP 9.2.1 / KSP 2.3.10 / Kotlin 2.2.10 compatibility was reasoned through and documented (ADR 0002), not blindly bumped. `kotlinx-coroutines-test` is deliberately pinned to 1.9.0 to match what the Compose BOM constrains — the kind of detail that saves the next agent hours of debugging a `NoSuchMethodError`.

The **detekt gate (`maxIssues: 0`)** is a real, enforced quality gate wired into the build, independent of the missing CI — it just needs CI to actually run it on every PR.

---

## Simplify or Remove

Rules, abstractions, or documents that add complexity without milestone value.

**PRD §13 (Android Technical Architecture) and §14 (Backend Technical Architecture)** should move out of the PRD entirely into a new `docs/architecture.md` — PRD §23.5 already names this file in its own target doc structure and it doesn't exist. Keeping stack/layer/package-boundary decisions in the PRD makes the product document 2× longer than it needs to be and forces every future product-only edit to wade through Compose rules and Go package conventions.

**PRD §17.6 (CI quality commands) and §18 (CI/CD)** should move into a new `docs/release-process.md`, alongside the Definition-of-Done checklist (§20) and the release-versioning mechanics (§4). This is operational content, not product requirements.

**PRD §23 (Claude Engineering Contract) and §24 (scripted first-implementation prompt) should be deleted outright**, not moved. §23 restates CLAUDE.md's prohibited-patterns list, working method, and completion-response format nearly line-for-line. §24 is a one-time bootstrap script that was useful before the repository existed and has no ongoing reference value now that the foundation is built — keeping it invites a future agent to "re-run" it against an existing codebase.

**PRD §21 (Planned Release Roadmap) and §22 (Future Pesantren Rules)** should move into `docs/ROADMAP.md`, which currently exists only as an empty placeholder. This both fixes the dead file and shortens the PRD to what it's actually for: specifying release `0.0.1`.

**Eight of the nineteen installed `.agents/skills` are not relevant to any milestone on the current roadmap** and should be removed until their triggering milestone is actually active: `camerax` (no camera feature anywhere in PRD or roadmap), `display-glasses-with-jetpack-compose-glimmer` (XR/glasses form factor, never mentioned), `engage-sdk-integration` (Play Engage/TV recommendations, out of scope), `wear-compose-m3` (Wear OS is not on the roadmap at all), `play-billing-library-version-upgrade` (monetization is roadmap item `0.5.0`), `verified-email` (auth is `0.1.0`, explicitly excluded from `0.0.1` per PRD §5.2), `appfunctions` (no AI-agent-shortcut workflows planned), `migrate-xml-views-to-jetpack-compose` (the project has no XML views to migrate — it's Compose-native from scratch). None of these cost anything at rest, but each one is a skill description a future agent has to read and discard on every relevant-sounding task; trim to what `0.0.1`–`0.2.0` actually need: `adaptive`, `agp-9-upgrade`, `android-cli`, `android-intent-security`, `edge-to-edge`, `navigation-3`, `r8-analyzer`, `styles`, `testing-setup`, `perfetto-sql`/`perfetto-trace-analysis` (fine to keep, low cost, genuinely useful once a reader UI exists to profile).

**No premature abstractions exist in the code today** — this section would normally flag `BaseViewModel`/generic-`UseCase`/interface-for-one-implementation problems, and there are none, because there is no feature code yet. The risk is entirely in the documentation layer (duplication) and in what gets built next, not in what's built now.

---

## Documentation Restructure

Target structure, and where existing sections move:

```text
README.md                          [NEW] project description, setup, links to docs/
CLAUDE.md                          [KEEP, trim] agent-instruction file; remove overlap once PRD is split
docs/
├── ROADMAP.md                     [POPULATE] ← PRD §21 (release roadmap) + §22 (future pesantren rules)
├── product/
│   └── PRD.md                     [TRIM] product-only: §1-12, §15, §19-20, §25 stay; engineering/CI/agent
│                                          sections below are removed or relocated
├── architecture.md                [NEW] ← PRD §13 (Android) + §14 (Backend) technical architecture
├── content-schema.md              [KEEP as-is] — precise, versioned, matches code exactly
├── content-workflow.md            [NEW] ← PRD §6 (content rules) + §15 (editorial/correction workflow)
├── testing.md                     [NEW] ← PRD §17 (testing strategy), cross-linked to PROGRESS.md's
│                                          per-milestone "Test results" sections rather than duplicating them
├── release-process.md             [NEW] ← PRD §18 (CI/CD) + §17.6 (quality commands) + §4 (release
│                                          versioning) + §20 (Definition of Done)
├── PROGRESS.md                    [KEEP] append-only milestone log; keep commit titles honest against it
└── decisions/                     [KEEP unchanged] — best-executed docs in the repo, use as the template
```

Do **not** add the `AGENTS.md` file PRD §23.5 currently calls for. CLAUDE.md already serves that exact purpose (Claude Code's own convention); a second file with the same content is the same duplication problem this audit is flagging, just moved one level up. Update PRD §23.5's target list to reference CLAUDE.md instead of asking for both.

---

## Design Review

**Design system today:** `Color.kt` defines a restrained green palette (`SantriGreen10`–`90`) plus warm neutral surfaces (`SantriNeutral10/90/95/99`) — this is the right visual direction and matches the brief's "restrained green identity, warm neutral reader surfaces" instruction well. `Theme.kt` wires Material 3 light/dark schemes with dynamic color intentionally disabled, which is correct and already documented (PRD §13.8, ADR-worthy but currently only a code comment — consider a short ADR here since it's a real, deliberate constraint). `Type.kt` currently defines exactly one text style (`bodyLarge`); there is no type scale, no Arabic-specific typography, no spacing scale, no shape/elevation tokens. That is appropriate for the current milestone — don't build tokens nothing consumes — but it means the design system is not yet ready to build Serambi against, since "typography and spacing as the main hierarchy" (the brief's stated preference) requires those scales to exist first.

**Serambi:** does not exist. `strings.xml` and `SanguSantriNavHost.kt` both confirm this explicitly. There is nothing to evaluate for "generic or AI-generated" appearance — the finding here is prospective, not retrospective: **the repository currently has no written anti-pattern list** (no cards/gradients/glassmorphism/generic-hero-section ban) anywhere in CLAUDE.md or PRD §13.8, and no shape/elevation/spacing tokens to build against. Combine those two gaps and the most likely first draft of Serambi from any agent — Claude included — is a Material "card wall" home screen with a hero header, exactly what this audit was asked to reject. Before Serambi implementation starts:
- Add spacing scale, a small shape set (2–3 corner radii, used deliberately, not per-component), and an elevation policy (prefer tonal surfaces / borders over shadow stacking) to `core/designsystem/theme`.
- Add the explicit reject/prefer list from this audit's brief into CLAUDE.md's Compose Rules section, so it's an enforced instruction rather than something only this one-off audit knows about.
- Source or provisionally license the Arabic typeface before any Arabic text renders — already correctly tracked as Blocking Production Input §25.8, not a new finding, just flagging it's a design-system dependency as much as a legal one.

**RTL, landscape, tablet, large-font:** `android:supportsRtl="true"` is already set in the manifest, correctly anticipating requirement FR-013. No locale resource folders (`values-in`, `values-ar`) exist yet, no adaptive-layout code exists yet — both are correctly deferred (nothing renders text yet), tracked honestly in PROGRESS.md's "Known limitations" rather than silently skipped. This is the right posture; just don't let it slip once Serambi lands, since FR-013/FR-014/§16.3 are specific and testable requirements, not aspirational language.

---

## Reference Policy

| Engineering category | Consult | Do not copy |
|---|---|---|
| Compose navigation | `android/nav3-recipes` (stable releases) first; cross-check real usage against `compose-samples` | `nav3-recipes`' `modular-hilt.md`/`modular-koin.md` multi-module wiring — SanguSantri stays single-module (ADR 0001) |
| General app architecture (UI/domain/data, UDF) | `android/architecture-samples` | Its multi-module Gradle layout, and its one-use-case-per-repository-method pattern — CLAUDE.md explicitly forbids pass-through use cases; architecture-samples is more ceremonious than this project needs |
| Design system / Material3 theming / reader-style layout | `compose-samples`: **Jetnews** for long-form reader text layout (directly analogous to Tahlil/Istighosah), **Reply** for adaptive list-detail, **Jetcaster** for offline-first content patterns | **Jetsnack**'s heavily custom-drawn, gradient-and-shape-heavy shopping visual language — wrong tone for a devotional reader; no sample's multi-module structure |
| Testing strategy (unit, Compose UI, screenshot) | the already-installed `testing-setup` skill; `compose-samples`' test suites for realistic patterns; `nowinandroid` **only** for its screenshot-testing (Roborazzi) setup | `nowinandroid`'s module-per-feature test source-set layout |
| Offline-first sync design (for FR-010, when built) | `nowinandroid`, narrowly for its sync-then-render-from-Room pattern and WorkManager scheduling | Its `:sync:work`/`:core:data`/`:core:datastore-proto` module split, and its multi-module nav-graph merging — the single `SanguSantriNavHost` is simpler and correct at this size |
| Adaptive layout (tablet/foldable) | official Android adaptive-layout docs + the installed `adaptive` skill; `compose-samples`' **Reply** as a concrete reference | — |
| R8/shrinking (once re-enabled) | the installed `r8-analyzer` skill + official R8 docs | — |
| Android security | the installed `android-intent-security` skill + OWASP MASVS/MASTG + official Play policy docs | — |

---

## Patch Plan

Files to create or modify. **No modifications were performed — this is a plan only.**

Create:
- `README.md`
- `docs/architecture.md`
- `docs/content-workflow.md`
- `docs/testing.md`
- `docs/release-process.md`
- `.github/workflows/ci.yml` (lint, detekt, ktlint check, unit tests, instrumented tests, assembleDebug/Release, per PRD §17.6)
- `.github/dependabot.yml` (or equivalent dependency-update/vulnerability scanning config)
- `app/src/main/java/com/sangusantri/app/core/designsystem/theme/Spacing.kt`, `Shape.kt` (or a combined `Dimens.kt`) — before Serambi UI work begins
- `docs/decisions/0007-disable-dynamic-color.md` (small ADR formalizing the already-real, currently comment-only decision in `Theme.kt`)

Modify:
- `app/build.gradle.kts` — re-enable release optimization (`optimization { enable = true }`) and confirm `rules.keep` actually shrinks/obfuscates correctly against the current dependency set
- `docs/product/PRD.md` — remove §13, §14, §17.6, §18, §21, §22, §23, §24; keep §1–12, §15, §16, §19, §20, §25
- `CLAUDE.md` — add the explicit visual anti-pattern list (no cards-as-default-container, no gradients, no glassmorphism, no oversized hero sections, no pseudo-Arabic Latin fonts, no ornamental backgrounds behind Arabic text) to the Compose Rules section
- `docs/ROADMAP.md` — populate with the relocated roadmap + future pesantren rules content
- `app/src/main/res/xml/backup_rules.xml`, `data_extraction_rules.xml` — scope explicitly (exclude the future `reading_sessions`/`step_progress`/`feedback_outbox` tables from cloud backup) before those entities ship
- `.agents/skills/` — remove the eight roadmap-irrelevant skill directories listed under Simplify or Remove

---

## Milestone 3 Readiness

**NOT READY.**

1. Milestone 2 (Serambi) has not started — the repository is at Milestone 1 complete. "Milestone 3 readiness" is not yet a meaningful question to ask of this codebase; the immediate question is Milestone 2 readiness, and on that narrower question the foundation (Room, seed import, DI, navigation skeleton, theme skeleton) is genuinely solid and ready to build on.
2. The release build currently ships with R8/shrinking disabled, contradicting the project's own stated policy — this must be fixed before any release-track milestone, not deferred to "later."
3. No CI exists, so nothing currently prevents a regression from landing silently as milestone work accelerates.
4. Documentation has drifted into duplication between PRD.md and CLAUDE.md, which will get worse, not better, as more milestones add more rules to both files — fix the structure now, while the fix is a few hours of moving text, not a week of reconciling contradictions.
5. The design system lacks the tokens and the explicit anti-pattern instructions needed to build Serambi without producing a generic result — this is a small, cheap fix that should land before, not after, the first Serambi screen is written.

None of these are deep architectural problems — the actual engineering (Room, migrations, seed import, DI, package boundaries) is executed at a genuinely high standard and needs no rework. The gaps are entirely in release hygiene, documentation structure, and the small amount of design-system groundwork Serambi will need. Close the five items above, then proceed to Serambi with confidence.

---

## Security Findings

*Classified: Required before public 0.0.1 (R-0.0.1) / Required before backend sync (R-Sync) / Required before auth or private access (R-Auth) / Required before quizzes/rankings/ads/subscriptions (R-Monetize) / Optional hardening (Opt).*

- **[R-0.0.1]** Release R8/shrinking disabled — see Critical Findings #1.
- **[R-0.0.1]** No CI quality gate — see Critical Findings #2.
- **[R-0.0.1]** No dependency-vulnerability scanning configured (no Dependabot, no OWASP Dependency-Check equivalent). Cheap to add alongside the new CI workflow.
- **[R-Sync]** No `network_security_config.xml` exists yet. Correctly not urgent — there is zero network code in the repository today (Retrofit/OkHttp are declared in the PRD stack but not yet added as dependencies or used). Must exist and enforce HTTPS-only, no cleartext, before `FR-010` sync work begins.
- **[Opt today / R-Sync if deep links are added]** Only one exported component exists (`MainActivity`, correctly `exported="true"` for the `LAUNCHER` intent-filter — this is required, not a finding). No services, receivers, or providers are exported. Re-run the already-installed `android-intent-security` skill when deep links or content-detail navigation args are added.
- **[Keep]** Content package checksum verification (`SeedContentChecksum`, SHA-256, tested against tamper/mismatch) is implemented and working correctly today — a real, functioning supply-chain control, not a gap.
- **[R-0.0.1]** `rules.keep` is currently a stub (only a commented-out WebView example). Once R8 is re-enabled, review actual keep-rule needs against the real dependency set using the `r8-analyzer` skill.

## Privacy and Compliance

- **[Keep]** No PII is collected anywhere in the current scope: no accounts exist, DataStore holds only reader preferences, Room holds only public content tables. This is genuinely privacy-clean today.
- **[On track]** Feedback (FR-012) is designed to exclude devotional/counter history from upload by requirement — not yet implemented (no `feature/feedback` package exists), so nothing to verify in code yet; hold the implementation to this requirement when it lands.
- **[R-0.0.1]** No privacy policy exists — already correctly tracked as Blocking Production Input §25.10 in the PRD. This blocks *release*, not continued engineering; no new finding here beyond confirming the tracking is honest.
- **[R-0.0.1]** No Data Safety form / third-party SDK inventory exists — none is needed yet (zero third-party SDKs beyond AndroidX/Hilt/Room, no analytics wired), but this becomes a real Play Console submission-time task once anything is added.
- **[R-0.0.1]** Crash reporting is not yet wired (PRD §19 recommends it). When added, redaction of Arabic reading text and counter values must be verified before the first release, not assumed — this specifically matters at "thousands of users" scale where default-verbose crash payloads are a real exposure.

## Reliability and Recovery

- **[Keep]** Migration strategy is real and tested — see Keep section above.
- **[Keep]** Seed import is transactional, checksum-verified, per-package isolated, and idempotent, with tests for all four success/failure paths including rollback.
- **[R-Sync]** No disaster-recovery or restore-testing story exists — correctly not needed yet (no backend, no server-side user data exists). Design this into the first sync implementation rather than retrofitting it.
- **[R-Sync]** No handling yet for partial/interrupted downloads or full-storage failure during sync — correctly deferred since sync isn't built, but PRD §12.4's package-import sequence (temp storage → size check → checksum → parse → validate → transactional import → activate → delete temp) already specifies the right shape; build to that spec directly rather than discovering it iteratively.
- **[Required before Serambi/reader ship]** Process-death restoration is proven only for the DB/DataStore layer so far — there's no reader UI state to restore yet. `reading_sessions`/`step_progress` (already scoped in PRD §11.2) need the same instrumented-test rigor the seed importer got.

## Observability and Incident Response

- **[R-0.0.1]** No crash/ANR monitoring wired yet — Play Console Vitals is free and should be default-on before any public release.
- **[R-Sync]** No backend structured logging, request IDs, API latency/error-rate tracking exist yet — correctly not needed until the Go API exists.
- **[R-0.0.1]** No incident runbook or named content-revocation authority is documented anywhere. The *data model* already supports revocation (`AmaliyahVersionStatus.REVOKED`, FR-011's fallback-to-previous-approved-version logic is implemented in the schema), but the *human process* — who has authority to revoke, how fast, and how a correction becomes a new approved version — isn't written down. This is cheap to add as a short section in the new `content-workflow.md` and matters even for the current bundled fixtures once real content replaces them.
- **[R-0.0.1]** No named security/privacy/support contact exists in the repo — the Play Store listing will require a support contact regardless of release complexity.

## Release Readiness

- **[R-0.0.1]** No CI pipeline — the single largest concrete release-readiness gap.
- **[Correctly deferred]** No signing config exists — this is explicitly tracked as Blocking Production Input §25.13, an engineering-adjacent but governance-owned task, not a code defect.
- **[R-0.0.1]** R8 disabled contradicts the project's stated release policy — see Critical Findings #1.
- **[Keep]** `versionCode`/`versionName` are wired correctly (`1` / `"0.0.1"`), matching PRD §4.1's versioning scheme.
- **[R-0.0.1]** No staged-rollout, pre-launch-report, or feature-flag infrastructure exists yet — appropriate given there's no CI/CD to hang it off of yet; build it alongside the CI workflow, since PRD §4.4 explicitly requires feature flags for any unfinished feature exposure and there will be several as milestones ship incrementally.
- **[Keep]** `targetSdk = 36`, `compileSdk = 37` — already ahead of the 31 Aug 2026 API-36 Play requirement PRD §13.2 itself calls out. No action needed.

## Deferred Security Controls

Explicitly extending the PRD's own stated judgement (§16.4: "Certificate pinning is not required for 0.0.1") — none of the following are warranted yet, and none should be added without a concrete triggering threat:

- **Certificate pinning** — Opt. No MITM threat model specific to a public content reader justifies this; reconsider only if a concrete targeted-interception threat emerges.
- **Root/tamper detection, Play Integrity API enforcement** — Opt. Reconsider only once payments (`0.5.0`) or pesantren membership codes (`0.2.0`) exist, where fraud has real cost. Adding it now is security theatre against a threat that doesn't exist yet.
- **Enterprise secret management (Vault/HSM-backed KMS)** — Opt. Environment-variable/Supabase-managed secrets, as PRD §14.1 already specifies, are sufficient at this scale.
- **Screenshot/clipboard blocking** — Opt. Only relevant once private pesantren content (`0.3.0`) exists; public devotional text has no confidentiality requirement to protect.
- **Brute-force protection beyond basic API rate limiting** — Opt today; **R-Auth** once invitation codes (`0.2.0`) or login (`0.1.0`) exist. Basic rate limiting, already scoped in PRD §14.5, is sufficient for the current account-free surface.
