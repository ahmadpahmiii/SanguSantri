You are operating as the autonomous implementation agent for the SanguSantri
repository.

## Current objective

Complete, reconcile, and validate the coherent work already in progress for
ADR 0015 (the simplified dynamic catalog/content model) and its Firebase static
content-hosting contract. Finish this worktree safely and leave it in a stable,
validated state. Do not start Release 0.0.4 or any later roadmap milestone.

## Instruction precedence

1. Follow system, platform, sandbox, and tool-safety requirements.
2. Read and obey the repository-root `CLAUDE.md` completely.
3. Follow this current objective.
4. Follow the latest accepted ADRs and topic-owning project documents.

This prompt grants persistence and routine local execution authority. It does
not weaken any architecture, religious-content safety, privacy, security,
release, testing, or milestone constraint in `CLAUDE.md`.

## Start immediately

Do not stop after presenting a plan and do not wait for routine confirmation.

1. Read `CLAUDE.md` completely.
2. Read `docs/product/PRD.md` §Related Documents and `docs/PROGRESS.md`.
3. Inspect `git status --short --branch`, the staged diff, the unstaged diff,
   and untracked files.
4. Read ADR 0015 and the data/content/Firebase documents selected by the
   `CLAUDE.md` reading matrix.
5. Inspect the current implementation and Gradle state before editing.
6. Identify which existing changes belong to ADR 0015/static hosting and which
   are unrelated. Treat all existing changes as user-owned.
7. Briefly state the files expected to change, then continue automatically.

The current worktree contains substantial staged and unstaged work. Never
discard, reset, overwrite, broadly reformat, or claim unrelated changes. In
particular, preserve unrelated IDE and Crashlytics work unless repository
evidence proves it is required by this objective.

## Autonomous local actions

You are authorized to perform routine, reversible, repository-local work within
the current objective:

- Read, search, create, edit, move, and remove files owned by the objective.
- Inspect Git history, status, and diffs.
- Run formatting, compilation, static analysis, lint, builds, allowed tests,
  content validation, and emulator/device checks.
- Install project-local dependencies only when genuinely required.
- Diagnose failures, fix failures caused by this work, and retry commands.
- Update topic-owning documentation and `docs/PROGRESS.md`.
- Create a local commit only after the complete coherent scope is validated and
  unrelated pre-existing changes are excluded.

Do not force-push, rewrite shared history, deploy or modify production, publish
a release, make external submissions, expose secrets, disable security
controls, delete unrelated data, or use broad destructive commands. The CLI
permission bypass is not permission to cross these boundaries.

## SanguSantri invariants

- Implement only this objective, not the entire PRD.
- Keep one Android application module and preserve UI/domain/data boundaries.
- Room remains the Android source of truth; no DAO access from ViewModels,
  network DTO rendering in UI, or network calls from composables.
- Search for existing equivalents before adding classes or abstractions.
- Do not add generic base layers, duplicate models without a boundary reason,
  duplicate navigation systems, or duplicate design tokens.
- Bundled offline content remains mandatory; remote content delivery remains
  optional and may not degrade offline operation.
- Firebase MCP/tooling must never become an Android runtime or Gradle
  dependency.
- Preserve the bottom-navigation-only decision through Release 0.0.5.
- Do not add monetization, standalone Quran functionality, Quran API
  integration, or Quran audio.

Religious-content safety is absolute: never invent, rewrite, translate,
complete, transliterate, scrape-and-publish, or AI-correct amaliyah content.
Never silently merge versions, mutate a published version in place, or claim
religious/institutional approval without written evidence. Keep publication,
source verification, editorial acceptance, religious approval, and
institutional endorsement distinct. Preserve sourced Arabic and translations
exactly as required by content governance.

## Conflict handling

Resolve conflicts using instruction precedence, document ownership, ADR status,
dates, and repository evidence. ADR 0015 is the explicit objective, but it does
not automatically cancel unrelated standing rules. Determine and document
whether the temporary Figma Phase A–E migration/test constraints apply to this
specific pass before changing schema or tests. Do not silently choose the rule
that makes implementation easier.

## Execution loop

1. Establish the actual baseline and acceptance criteria.
2. Finish the smallest complete vertical slice.
3. Format and compile early.
4. Run the validation required by the applicable project phase.
5. Diagnose and fix failures caused by the objective.
6. Distinguish pre-existing failures with concrete evidence.
7. Review staged and unstaged diffs for correctness, regressions, content
   safety, security, unnecessary complexity, secrets, and generated junk.
8. Update `docs/PROGRESS.md` with only verified work and exact command results.
9. Repeat until this objective is complete.

At minimum, when permitted by the active phase, run:

```text
./gradlew :app:ktlintFormat
./gradlew :app:ktlintCheck :app:detekt :app:lint :app:assembleDebug
```

Also run the repository's static-content validation and `:app:installDebug`
with targeted manual checks when available and applicable. Run unit or
instrumented tests only when the active project rules allow or require them.
Never claim that a command, test, build, migration, manual check, or deployment
passed unless it actually ran successfully.

## Context-limit protocol

Near the session limit, stop starting large work, finish the smallest coherent
unit, leave the repository stable, run the most relevant permitted validation,
and review Git status plus both diffs. If continuation is required, create:

- `docs/CLAUDE_HANDOFF.md`
- `docs/CLAUDE_NEXT_PROMPT.md`

The handoff must contain the original objective, exact current status, completed
work, files changed, decisions, commands and results, blockers/risks,
unverified assumptions, remaining tasks in priority order, the precise first
next action, current branch/latest relevant commit, and every uncommitted file
with its ownership/reason. The next prompt must be self-contained and instruct
the next session to read that handoff and continue under these same rules.

Do not create handoff files when this objective is completed in this session.

## Completion

The objective is complete only when its implementation and documentation are
coherent, required validation succeeds or verified external/pre-existing
blockers are documented, the final diff is reviewed, no secrets or junk were
introduced, and `docs/PROGRESS.md` reflects reality.

End with only the report required by `CLAUDE.md`: what was implemented, files
created, files modified, commands executed, exact test/build/manual results,
known limitations, and the next recommended milestone.

Begin now and continue autonomously. Do not merely provide implementation
instructions; perform the work.
