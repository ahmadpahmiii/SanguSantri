# SanguSantri Engineering Instructions

SanguSantri is a long-term Android application for public and
pesantren-specific amaliyah.

* Platform: Native Android, Jetpack Compose, package `com.sangusantri.app`.
* Minimum SDK: 26.
* Current release: `0.0.1`. Current content: Tahlil and Istighosah.
* Architecture: offline-first Clean Architecture, one Gradle module.
* Current state: Milestones 0–3 (foundation, content model + seed import,
  Serambi, Full Amaliyah Reader) are complete — verify this against
  `docs/PROGRESS.md` before assuming otherwise; commit titles in `git log`
  are not a reliable milestone indicator (see
  `docs/reviews/audit-resolution.md`).
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

| Task type | Read |
|---|---|
| Any task | `docs/PROGRESS.md` (current actual state) |
| UI / Compose screen | `docs/engineering/CODING_STANDARD.md`, `docs/design/DESIGN_SYSTEM.md`, `docs/design/ACCESSIBILITY.md` |
| Data layer / Room / repository / sync | `docs/engineering/ARCHITECTURE.md`, `docs/engineering/CONTENT_MODEL.md`, `docs/engineering/OFFLINE_FIRST.md`, `docs/content-schema.md` |
| Security / network / auth | `docs/security/SECURITY_BASELINE.md`, `docs/security/THREAT_MODEL.md` |
| Privacy / feedback / telemetry | `docs/security/PRIVACY.md` |
| Release / CI / Gradle / signing | `docs/engineering/RELEASE_ENGINEERING.md`, `docs/operations/PRODUCTION_READINESS.md` |
| Content entry / approval / correction | `docs/engineering/CONTENT_MODEL.md`, `docs/operations/CONTENT_GOVERNANCE.md` |
| Testing | `docs/engineering/TESTING.md` |
| Architecture decision review | `docs/decisions/` |
| Product scope question | `docs/product/PRD.md`, `docs/product/ROADMAP.md` |

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
* Claim that content has been approved.
* Modify approved content in place — corrections create a new version
  (ADR 0008).

Development fixtures must be clearly labelled non-production and must never
reach the release build. Production religious content requires an external
kyai or sesepuh approval (`docs/operations/CONTENT_GOVERNANCE.md`).

## Working method

For every task: inspect the existing implementation and Gradle state,
search before creating a new class, state which files will change,
implement only the requested milestone, run relevant formatting/build/test
commands and fix failures caused by the change, update `docs/PROGRESS.md`,
and do not repeat complete source files in the final response. Full method
and prohibited-pattern list: `docs/engineering/CODING_STANDARD.md`.

Never claim that a command passed unless it was actually executed
successfully.

## Final response format

Report only: what was implemented, files created, files modified, commands
executed, test results, known limitations, next recommended milestone.
