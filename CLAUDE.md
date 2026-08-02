# SanguSantri Engineering Instructions

SanguSantri is a long-term Android application for public and
pesantren-specific amaliyah.

* Platform: Native Android, Jetpack Compose, package `com.sangusantri.app`.
* Minimum SDK: 26.
* Current release: `0.0.3`. Current content: Tahlil and Istighosah.
* Architecture: offline-first Clean Architecture, one Gradle module.
* Current state: Milestones 0–6 (foundation; content model + content
  import; Serambi; Full Amaliyah Reader; local production content
  bootstrap; Guided Reader + integrated tasbih; content-wiring fix +
  Istighosah draft; content release baseline + reader mode switching;
  risk-based content publication governance) are complete — verify this
  against `docs/PROGRESS.md` before assuming otherwise; commit titles in
  `git log` are not a reliable milestone indicator (see
  `docs/reviews/audit-resolution.md`). A Figma product-alignment
  documentation pass (2026-07-26) has since renamed the home destination
  Serambi → **Beranda** and expanded `0.0.1`'s documented scope (Beranda
  rebuild, Jelajahi Amaliyah, reader TOC/repetition-shortcut) ahead of the
  matching implementation milestones — see `docs/design/FIGMA_HANDOFF.md`
  and `docs/reviews/figma-product-alignment.md` before assuming the
  current code matches the current docs for any of that work. Milestone 8
  (Content Delivery Foundation and Remote Synchronisation, 2026-07-28) has
  since replaced the seed-only bundled-content pipeline
  (`SeedContentSource`/`AssetSeedContentSource`/`SeedContentImporter`,
  deleted) with a shared `ContentPackageImporter` used by both bundled
  bootstrap and a new, implemented Android remote-content-synchronisation
  client (`data/remote/`, `data/sync/`) against a backend's contract — see
  ADR
  [0012](decisions/0012-bundled-bootstrap-and-remote-sync.md) and the
  rewritten `docs/product/PRD.md` FR-010/FR-011 before assuming remote
  sync "is not part of `0.0.1`" or that Android retains previous content
  versions — neither is true any more. A subsequent sync-simplification
  pass (2026-07-28, ADR 0012 amendment) removed manifest ETag/`304`
  handling, deleted `ContentRemoteDataSource`, renamed
  `ContentSyncCoordinator` → `ContentSyncManager`, and replaced the former
  six-case sync outcome with a three-case `SyncResult` — see that
  amendment before assuming the Milestone 8 class names above are still
  current. Milestone 9 (Standalone Tasbih and the bottom-navigation shell,
  2026-07-29) has since implemented `0.0.2`: `feature/tasbih` (counter,
  target presets/custom target, session naming, reset, session history)
  and the app's first real navigation shell (`navigation/
  TopLevelBackStack.kt`, `BottomNavigationBar.kt`). The product owner/tech
  lead separately approved, in the same session, a **bottom-navigation-
  only** scope through `0.0.5` (no Navigation Rail on any window-size
  class in that window) and moved Nahwu Quiz from `0.4.0` to `0.0.5` — see
  ADR [0013](decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md)
  before assuming `docs/design/DESIGN_SYSTEM.md`'s/`ARCHITECTURE.md`'s
  previously documented bar/rail plan, or `docs/product/ROADMAP.md`'s
  previous `0.4.0` Nahwu Quiz position, are still current. Milestone 10
  (Aktivitas, 2026-07-29) has since implemented `0.0.3`: `feature/activity`
  (streak, this-week summary, filterable amaliyah-completion and
  tasbih-history sections), a new durable `amaliyah_completion_events`
  table decoupled from the version-scoped progress tables ADR 0012 wipes on
  content replacement, and bottom nav is now Beranda | Aktivitas | Tasbih. A
  Firebase Hosting static content delivery decision (2026-08-02) has since
  dropped the Go + Supabase-managed PostgreSQL backend (ADR 0011) entirely
  — it was never implemented — in favour of static files served from a new
  `content-hosting/` directory via Firebase Hosting, with a Firebase MCP
  server used only as development/CI tooling (never an Android runtime
  dependency, never a Gradle dependency of `app/`) — see ADR
  [0014](decisions/0014-firebase-hosting-static-content-delivery.md),
  ADR 0011 (now Superseded), the amended ADR 0012/0010, and
  `docs/engineering/MCP_TOOLING.md` before assuming a Go backend is still
  planned anywhere in this project. This is a documentation/architecture
  decision only — no Android code changed, and the Android sync client's
  `ContentApiService` needs no code change either, since it already only
  issues plain `GET` requests that static files satisfy identically.
* SanguSantri is currently a **non-commercial application**: no advertising,
  subscriptions, standalone Quran feature, Quran API integration (Kemenag or
  Quran Foundation), or Quran audio is on the roadmap
  (`docs/product/ROADMAP.md`).

Do not implement the entire PRD unless explicitly requested. Implement only
the milestone actually asked for.

## Reading matrix — read only what the task needs

Always read first: `docs/product/PRD.md` §Related Documents (bottom of
file) tells you exactly which document below owns which topic — do not read
every document for every task.

| Task type                                 | Read                                                                                                                                                                           |
|-------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Any task                                  | `docs/PROGRESS.md` (current actual state)                                                                                                                                      |
| UI / Compose screen                       | `docs/engineering/CODING_STANDARD.md`, `docs/design/DESIGN_SYSTEM.md`, `docs/design/ACCESSIBILITY.md`, `docs/design/FIGMA_HANDOFF.md` (frame mapping and implementation order) |
| Data layer / Room / repository / sync     | `docs/engineering/ARCHITECTURE.md`, `docs/engineering/CONTENT_MODEL.md`, `docs/engineering/OFFLINE_FIRST.md`, `docs/content-schema.md`                                         |
| Security / network / auth                 | `docs/security/SECURITY_BASELINE.md`, `docs/security/THREAT_MODEL.md`                                                                                                          |
| Privacy / feedback / telemetry            | `docs/security/PRIVACY.md`                                                                                                                                                     |
| Release / CI / Gradle / signing           | `docs/engineering/RELEASE_ENGINEERING.md`, `docs/operations/PRODUCTION_READINESS.md`                                                                                           |
| Content entry / approval / correction     | `docs/engineering/CONTENT_MODEL.md`, `docs/operations/CONTENT_GOVERNANCE.md`                                                                                                   |
| Firebase MCP / `content-hosting/` tooling | `docs/engineering/MCP_TOOLING.md`, `docs/engineering/ARCHITECTURE.md` §Backend, ADR 0014                                                                                       |
| Testing                                   | `docs/engineering/TESTING.md`                                                                                                                                                  |
| Architecture decision review              | `docs/decisions/`                                                                                                                                                              |
| Product scope question                    | `docs/product/PRD.md`, `docs/product/ROADMAP.md`                                                                                                                               |

## Hard architecture constraints

* One Android Gradle application module until a real modularisation trigger
  appears (`docs/engineering/ARCHITECTURE.md`).
* UI / domain / data boundaries: Room is the source of truth; the UI must
  never render directly from network DTOs; no DAO access from ViewModels;
  no network calls from composables.
* Do not create: `BaseViewModel`, `BaseRepository`, generic `BaseUseCase`,
  pass-through use cases, duplicate models without a boundary reason,
  duplicate navigation systems, duplicate themes/design tokens. Full list:
  `docs/engineering/CODING_STANDARD.md`.
* Create a use case only when it contains meaningful or reusable business
  logic.
* Before adding a class, search the repository for an existing equivalent.

## Content safety — absolute, no phase exception

Claude must not:

* Invent Arabic amaliyah text, translations, or missing prayers.
* Add Latin transliteration to any amaliyah content.
* Automatically scrape and publish religious content.
* Correct religious content based solely on AI judgement.
* Claim that a kyai, sesepuh, or other religious authority approved content
  when none did, or imply institutional endorsement (e.g. by NU/PBNU or a
  source publisher) that does not exist in writing.
* Silently merge different content versions together.
* Modify a published version in place — corrections create a new version
  (ADR 0008).

Development fixtures used only to prove a feature works (bracketed
placeholder text, `[FIXTURE]`-style markers) must be clearly labelled
non-production and must never reach the release build.

**Content publication follows a risk-based model** (product-owner decision,
superseding the previous universal-approval rule; full detail:
`docs/operations/CONTENT_GOVERNANCE.md`, `docs/product/PRD.md` §3.1):
standard, commonly practised public amaliyah from an identified, publicly
accessible, trusted editorial source — with the source URL/publisher
recorded, extraction manually inspected for structural problems, and
Arabic text/translations kept exactly as sourced — may be published on the
product owner's explicit editorial acceptance alone; kyai/sesepuh sign-off
is optional for this category, not mandatory. Kyai, ustaz, sesepuh, or
other qualified religious review remains required for higher-risk content:
private/pesantren-specific, unclear or disputed origin, manually modified
beyond formatting, compiled by merging versions, internally translated,
doctrinally sensitive, or tied to a specific ijazah/sanad/tarekat/pesantren
authority. Publication status, source verification, internal editorial
acceptance, religious-authority approval, and institutional endorsement are
five distinct concepts — never collapse one into another in UI text or
documentation.

## Working method

For every task: inspect the existing implementation and Gradle state,
search before creating a new class, state which files will change,
implement only the requested milestone, run relevant formatting/build/test
commands and fix failures caused by the change, update `docs/PROGRESS.md`,
and do not repeat complete source files in the final response. Full method
and prohibited-pattern list: `docs/engineering/CODING_STANDARD.md`.

Never claim that a command passed unless it was actually executed
successfully.

## Temporary implementation-pass constraints (Figma product alignment)

These apply only to the phases implementing the Figma product-alignment
work (`docs/design/FIGMA_HANDOFF.md`, Phases A–E) and are not a permanent
change to engineering standards. Remove this section once that
implementation initiative concludes or the user says otherwise.

* Do not create Room migration classes or a migration chain, and do not
  add `fallbackToDestructiveMigration`. The app is pre-public-release
  (`docs/engineering/CONTENT_MODEL.md` schema-freeze policy); when a
  schema change is genuinely necessary, update the clean baseline schema
  and state plainly that local developer data must be cleared or the app
  reinstalled. Real migrations become mandatory again the moment the
  initial public schema ships — this does not delete that long-term rule.
* Do not add new unit, instrumented, or screenshot tests; do not spend the
  phase building test infrastructure; do not delete existing tests to
  avoid maintaining them; keep existing test sources compiling when
  production APIs change. Never claim a test passed without executing it.
* Minimum validation per Android implementation phase: `ktlintFormat`,
  `ktlintCheck`, `detekt`, `lint`, `assembleDebug` — plus `installDebug`
  and manual on-device verification whenever an emulator/device is
  available, with exactly what was manually checked reported. Do not run
  `testDebugUnitTest`/`connectedDebugAndroidTest` unless explicitly asked
  for regression testing.

## Final response format

Report only: what was implemented, files created, files modified, commands
executed, test results, known limitations, next recommended milestone.
