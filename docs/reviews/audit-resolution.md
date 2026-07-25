# Resolution: Architecture, Documentation & Production-Readiness Audit

Resolves `docs/reviews/architecture-design-audit.md` (audited commit
`2bf914a`, 25 July 2026). That audit is preserved unmodified — this
document records what was done about it, not a replacement for it.

Read this once; it is not meant to be re-read by every future session (the
reading matrix in `CLAUDE.md` does not point here for normal task work).

## Accepted, implemented in this pass

| Finding | Action | Files |
|---|---|---|
| R8/shrinking disabled in release, contradicting stated policy | `optimization.enable = true`; required `android.r8.gradual.support=true` flag added (AGP 9.2.1's new DSL needs it, not mentioned in the audit); verified with `./gradlew assembleRelease` | `app/build.gradle.kts`, `gradle.properties` |
| `docs/ROADMAP.md` tracked and empty | Removed; content now lives at the mandated path | `docs/ROADMAP.md` (removed), `docs/product/ROADMAP.md` (new) |
| PRD/CLAUDE.md duplication and drift (incl. the Latin-transliteration rule present in PRD but missing from CLAUDE.md) | PRD trimmed to product-only content; CLAUDE.md trimmed to a short instruction file with a reading matrix; the missing Latin-transliteration rule is now explicit in CLAUDE.md Content Safety | `CLAUDE.md`, `docs/product/PRD.md` |
| PRD §13/14 (Android/backend architecture) mixed into product doc | Moved to `docs/engineering/ARCHITECTURE.md` (backend under a clearly-marked "planned, not started" heading) | `docs/engineering/ARCHITECTURE.md` |
| PRD §17.6/§18 (CI commands, CI/CD design) in product doc | Moved to `docs/engineering/RELEASE_ENGINEERING.md` | `docs/engineering/RELEASE_ENGINEERING.md` |
| PRD §23/§24 (Claude Engineering Contract, one-time bootstrap prompt) duplicate CLAUDE.md and have no ongoing value | Deleted outright, not moved, per the audit's own recommendation | `docs/product/PRD.md` |
| PRD §21/§22 (roadmap, future pesantren rules) belong in the roadmap doc | Moved | `docs/product/ROADMAP.md` |
| No documented visual anti-pattern list before Serambi work starts | Added explicit reject list (card walls, gradients, glassmorphism, hero sections, pseudo-Arabic Latin fonts, ornamental backgrounds behind Arabic text) | `docs/design/DESIGN_SYSTEM.md` |
| Design system has no spacing/shape/elevation tokens, and Serambi would be built against nothing | Documented as required *before the first Serambi screen*, not built now (would be Milestone 3 code, out of scope for this pass) | `docs/design/DESIGN_SYSTEM.md` |
| No `AGENTS.md` should be added (would duplicate CLAUDE.md) | Agreed, not created | — |
| No engineering/design/security/operations docs exist | Created the full set per the mandated structure | see file list below |
| ADRs 0001–0006 are the best-executed docs; use as template | Followed exactly for ADRs 0007–0011 | `docs/decisions/0007`–`0011` |

## Accepted with modification

| Finding | Audit's proposal | What was done instead | Reason |
|---|---|---|---|
| Move architecture content out of PRD | Single `docs/architecture.md` | `docs/engineering/ARCHITECTURE.md`, split further into `CODING_STANDARD.md`, `CONTENT_MODEL.md`, `OFFLINE_FIRST.md`, `TESTING.md`, `RELEASE_ENGINEERING.md` | The task's mandated doc tree specifies this split; it also gives `CLAUDE.md`'s reading matrix finer-grained targets, which is the actual token-efficiency win — one 2000-line architecture doc would be as expensive to load as the current PRD. |
| Add `docs/decisions/0007-disable-dynamic-color.md` | Standalone ADR | Documented as a rule in `docs/design/DESIGN_SYSTEM.md` instead | It's a design-system detail already implemented and stated in the PRD/code comment, not a durable cross-cutting technical decision on the level of the other ADRs. Five new ADRs were added for decisions that actually matched that bar (offline-first, immutable versions, no-auth MVP, no custom CMS, Go/Supabase backend) — adding a sixth for theming would be ADR sprawl against this task's own token-efficiency goal. |
| Create root `README.md` | New file at repo root | Not created | Outside both the allowed-changes list for this task and the mandated doc tree (which places `README.md` under `docs/`, not at root). `docs/README.md` was created instead as the documentation index. Root `README.md` is a reasonable follow-up for a future task with README explicitly in scope. |
| Backend architecture as its own doc | Implied by moving PRD §14 out | Folded into `docs/engineering/ARCHITECTURE.md` under a "planned — not started" heading rather than a separate file | No `backend/` directory exists at all; a dedicated file today would be pure ceremony against "do not create empty documents merely to match this structure." |

## Rejected

None. Every finding in the audit checked out against the actual repository
state (verified independently: read every file the audit cites, ran
`git show --stat HEAD`, confirmed the commit/milestone-numbering drift the
audit's opening paragraph flags).

## Deferred — documented as required, not implemented in this pass

| Finding | Why deferred | Where it's tracked now |
|---|---|---|
| `.github/workflows/ci.yml` + Dependabot | Not a "small correction" — new infrastructure (runner choice, action pinning, secrets) outside this task's allowed-changes scope and deserving its own implementation session with its own verification | `docs/engineering/RELEASE_ENGINEERING.md`, `docs/operations/PRODUCTION_READINESS.md` (flagged as the top release-readiness gap) |
| Removing 8 roadmap-irrelevant `.agents/skills` directories | `.agents/skills` is not in this task's allowed-changes list | Not tracked in a doc — this is a direct recommendation to the user: `camerax`, `display-glasses-with-jetpack-compose-glimmer`, `engage-sdk-integration`, `wear-compose-m3`, `play-billing-library-version-upgrade`, `verified-email`, `appfunctions`, `migrate-xml-views-to-jetpack-compose` can be removed whenever a follow-up task is authorized to touch that directory |
| `Spacing.kt`/`Shape.kt` design tokens | Real Milestone 3 (Serambi) implementation work, explicitly out of scope ("Do not implement Milestone 3 yet") | Requirement + rationale in `docs/design/DESIGN_SYSTEM.md` |
| Scoping `backup_rules.xml`/`data_extraction_rules.xml` | The tables they would exclude (`reading_sessions`, `step_progress`, `feedback_outbox`) don't exist yet — nothing to scope | `docs/operations/PRODUCTION_READINESS.md` §Backup policy |

## Files created

`docs/README.md`, `docs/product/ROADMAP.md`,
`docs/engineering/{ARCHITECTURE,CODING_STANDARD,CONTENT_MODEL,OFFLINE_FIRST,TESTING,RELEASE_ENGINEERING}.md`,
`docs/design/{DESIGN_SYSTEM,ACCESSIBILITY}.md`,
`docs/security/{SECURITY_BASELINE,PRIVACY,THREAT_MODEL}.md`,
`docs/operations/{CONTENT_GOVERNANCE,INCIDENT_RESPONSE,PRODUCTION_READINESS}.md`,
`docs/decisions/{0007-offline-first-public-content,0008-immutable-content-versions,0009-no-authentication-in-public-mvp,0010-no-custom-cms-in-initial-release,0011-go-and-supabase-managed-postgresql-backend}.md`,
`docs/reviews/audit-resolution.md` (this file).

## Files modified

`CLAUDE.md` (rewritten, trimmed), `docs/product/PRD.md` (trimmed, restructured),
`app/build.gradle.kts` (R8 re-enabled), `gradle.properties` (required AGP 9
flag added).

## Files removed

`docs/ROADMAP.md` (empty, superseded by `docs/product/ROADMAP.md`).

## Remaining risks

* **No CI.** Nothing currently prevents a regression from landing on
  `master` as milestone work accelerates. This is the single largest
  remaining gap and should be the next dedicated task, independent of
  Milestone 3.
* **Commit-message / milestone-number drift.** The current `HEAD` commit is
  titled "milestone 2" but its content matches `docs/PROGRESS.md`'s
  Milestone 1 (content model + seed import); Serambi (documented Milestone
  2) has not started. This is historical (an already-published commit) and
  was not corrected — rewriting published commit messages is a destructive
  operation outside this task's scope. Going forward, match commit titles
  to `docs/PROGRESS.md`'s milestone numbers before committing.
* **Design tokens don't exist yet.** `docs/design/DESIGN_SYSTEM.md` documents
  what's required before Serambi's first screen; nothing enforces it except
  the next session actually reading that document first.
* **Arabic typeface not sourced.** Still a Blocking Production Input; no
  change in status.

## Blockers before Milestone 3 (Serambi)

1. None of the documentation changes in this pass block starting Milestone
   3 — the foundation (Room, seed import, DI, navigation skeleton, theme
   skeleton) was already solid per the audit.
2. Before writing Serambi's first screen: add the spacing/shape/elevation
   tokens and Arabic-aware type scale described in
   `docs/design/DESIGN_SYSTEM.md` — this is real but small, expected to be
   part of the Milestone 3 session itself, not a separate pass.

## Blockers before public release `0.0.1`

Unchanged from the audit's own assessment, now tracked in
`docs/operations/PRODUCTION_READINESS.md` and `docs/product/PRD.md` §13:
CI pipeline, dependency-vulnerability scanning, crash monitoring wiring,
privacy policy, signing key, final Arabic typeface and approved production
content, and the rest of the Blocking Production Inputs list. None of these
were expected to be resolved by a documentation pass, and none were.
