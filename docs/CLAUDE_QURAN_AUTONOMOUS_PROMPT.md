# Claude Code — Autonomous Al-Qur'an Kemenag `0.0.6` Implementation

You are the autonomous implementation agent for the SanguSantri Android
repository. Begin immediately and continue until every implementation slice in
this prompt is complete or a genuine external input makes a remaining release
check impossible.

## Explicit product-owner authorization

The product owner explicitly requests implementation of the **entire approved
Al-Qur'an Kemenag `0.0.6` scope**, not one isolated milestone. This is the
specific authorization required by `CLAUDE.md` to execute the whole Quran PRD.

Do not stop after producing a plan. Do not ask for approval between milestones,
before routine file edits, before running Gradle, before installing a debug APK,
before using an available emulator, or before fixing failures caused by this
work. Make reasonable reversible decisions from repository evidence, record
them, and continue automatically to the next slice.

The CLI permission bypass removes routine tool confirmations. It does **not**
authorize destructive actions, secret exposure, production deployment, release
publication, external submissions, force-pushes, history rewrites, or changes
outside this objective.

## Objective and completion gate

Implement the complete standalone Quran feature described by:

- `docs/product/QURAN_PRD.md`
- `docs/design/QURAN_DESIGN_SYSTEM.md`
- `docs/engineering/QURAN_API_CONTRACT_DRAFT.md`
- `docs/decisions/0016-standalone-quran-kemenag-direct-api.md`
- the Quran mappings in `docs/design/FIGMA_HANDOFF.md`
- every applicable project-wide document selected by `CLAUDE.md`

All five delivery slices in QURAN_PRD section 14 belong to this one authorized
objective. A slice is a reviewable checkpoint, not a reason to end the session.
After validating and documenting one slice, immediately start the next.

Completion requires all implementable QUR-FR-001 through QUR-FR-020 behavior
and acceptance criteria, verified as far as available local inputs permit and
accurately documented. Screens compiling alone is not completion.

## Instruction precedence

1. Follow system/platform safety and tool constraints.
2. Read and obey repository-root `CLAUDE.md` completely.
3. Follow this explicit full-Quran objective.
4. Follow accepted ADRs and topic-owning documents, preferring newer and more
   specific evidence over stale prose.

This prompt expands the authorized milestone scope only. It does not weaken
Clean Architecture, religious-content safety, offline-first, privacy,
accessibility, security, testing, or release requirements.

## Start immediately

Before editing:

1. Read `CLAUDE.md` completely.
2. Read `docs/PROGRESS.md` and `docs/product/PRD.md` Related Documents.
3. Read the Quran sources above completely and the relevant parts of
   `FIGMA_HANDOFF.md`.
4. Read all project documents required by the `CLAUDE.md` matrix for
   data/Room/sync, UI/Compose, security/network/auth, privacy, testing, and
   release work.
5. Inspect Git status, both diffs, untracked files, Gradle, the Room baseline,
   DI, Navigation 3 shell, Beranda, Aktivitas/streak, preferences, and all
   existing Quran prototypes/assets.
6. Search for existing equivalents before creating a class, token, repository,
   use case, navigation mechanism, or preference.
7. State a concise execution checklist and likely files, then continue without
   confirmation.

The worktree may contain substantial user-owned staged, unstaged, untracked,
Quran, documentation, and unrelated IDE work. Preserve it. Never reset,
checkout, discard, overwrite, broadly reformat, or delete unrelated work.
Reconcile existing Quran work instead of blindly recreating it.

## Approved visual contract

Treat `docs/design/figma-export/quran/` HTML/JSON/PNG triplets as the screen and
state reference. Do not invent a conflicting visual system.

The canonical Arab-only sequence is:

1. `09-flowing-reader-arab-only-page.*` — normal full page;
2. `09b-flowing-reader-arab-only-selected.*` — identical page geometry with
   exactly one selected ayat;
3. `10-ayat-action-sheet.*` — the same selected page behind the action sheet.

The deleted `07-flowing-reader-arab-only.*` baseline is superseded. Never
restore or reference it. Use the approved dark Quran tokens and simple
basmalah asset. At a surah start show the compact category/surah/ayat-count
header; show one basmalah header for surahs 2–8 and 10–114, no extra one for
Al-Fatihah, and none for At-Taubah. Never claim pixel-identical printed-mushaf
composition because the API lacks line/glyph coordinates.

## Non-negotiable architecture and content rules

- One Android app module; reuse the existing Navigation 3 stack.
- Quran opens from Beranda, is not a bottom-nav destination, hides the existing
  bottom bar while active, and uses no second Activity.
- Room is the only UI-readable source of truth. Composables/ViewModels never
  render DTOs, call DAOs, or call the API directly.
- Preserve meaningful DTO/entity/domain separation. Do not add duplicate UI
  models without a real presentation-boundary need.
- Do not create generic base ViewModels/repositories/use cases/results,
  duplicate navigation systems, or duplicate themes/tokens.
- Create use cases only for meaningful/reusable business logic.
- Validate transport data completely, sort by numeric identity, and activate
  atomically.
- Preserve official Arabic, translation, annotation, and tafsir exactly. Never
  invent, normalize, repair, merge, translate, transliterate, scrape, or
  AI-correct Quran content.
- Never persist or display the API `teks` Latin transliteration.
- Add no copy, share, audio, download, notes, highlights, analytics, account
  sync, Quran Foundation, proxy, or other excluded feature.
- Never log Quran payloads, tafsir, bookmarks, positions, sessions, or secrets.
- User errors are concise Indonesian and expose no raw body, stack trace,
  header, token, or Arabic fragment.
- Quran `0.0.6` is separate from the older Figma-alignment Phases A–E. Apply
  normal testing unless a newer explicit decision says otherwise; do not use
  the temporary no-new-tests rule to skip Quran data/security coverage.

## Credential boundary

Implement ADR 0016 exactly:

- dedicated authenticated client restricted to the Kemenag origin;
- no real username/token in Git, Kotlin, XML, resources, assets, BuildConfig,
  docs, logs, exceptions, or tests;
- release values come from approved untracked local/CI secrets into generated
  native input outside tracked source;
- split/encode and reconstruct in native C++ only when needed;
- verify the expected release signing-certificate digest before returning it;
- strip native symbols and retain R8/resource shrinking;
- fail release assembly clearly when required production inputs are absent;
- use fakes/MockWebServer in debug/tests.

Do not hardcode a fake credential in production and do not claim NDK storage
makes extraction impossible. If approved secrets exist, use them without
printing them. If absent, implement and validate the boundary and negative
release gate, continue every independent milestone, and mark only live/release
verification blocked. Never invent or request the secret in chat.

## Font gates

- Reuse the supplied LPMQ Isep Misbah and Amiri Quran inputs; do not download an
  arbitrary replacement.
- Expose a font only after APK redistribution permission and fixed Kemenag-text
  glyph/shaping/clipping tests pass.
- Enable Amiri only under its packaged licence and after the corpus test.
- Keep LPMQ disabled if embedding permission lacks evidence.
- King Fahd stays a disabled design placeholder until its official font and
  licence/readme arrive. Never ship a substitute under that name.
- Fonts change rendering only, never stored strings.

## Continuous execution loop

For every slice:

1. Reconfirm acceptance against sources and current code.
2. Implement small coherent vertical increments.
3. Format/compile early and fix objective-caused failures.
4. Add focused tests for ordering/completeness, atomicity, identity
   preservation, repositories, restoration, and secret leakage.
5. Run applicable checks and inspect exact results.
6. Manually inspect relevant screens on an available emulator/device.
7. Review diffs for architecture violations, invented content, leaks,
   accessibility regressions, unrelated edits, and junk.
8. Update `docs/PROGRESS.md` with verified facts/results.
9. Optionally make a local checkpoint commit only if objective-owned files can
   be isolated without disturbing the user's index.
10. Immediately continue to the next slice without approval or a final report.

If a command fails, diagnose, fix objective-caused failures, and retry.
Distinguish pre-existing failures with evidence. Do not loop on unchanged
failures.

## Slice 1 — API, credential, Room, validation, sync

Implement QUR-FR-002/003/004 and data/security portions of 013/018:

- Exact envelope/DTO handling for all three documented endpoints.
- Dedicated Retrofit/OkHttp client with strict origin/header separation.
- Clean-baseline Room entities, keys, indices, foreign keys, DAOs, and database
  registration for `quran_surahs`, `quran_verses`, `quran_tafsir`,
  `quran_bookmarks`, `quran_reading_state`, `quran_reading_sessions`.
- Follow pre-public schema policy: update clean baseline when applicable, never
  add destructive fallback, and document any required developer reinstall.
- Domain models, repositories, mappers, meaningful validator/sync logic.
- Complete initial preparation: 114 unique surahs and all expected ayat,
  bounded concurrency, deterministic numeric ordering, one atomic activation.
- Seven-day unique refresh using `app_metadata`; failure preserves active data.
- On-demand local-first tafsir with `cachedAt` and seven-day stale refresh.
- Preserve bookmarks/last read across source replacement.
- Non-blocking bootstrap wiring.
- Fake/MockWebServer coverage for out-of-order Surah 114, missing/duplicate
  ayat, wrong surah, duplicate remote ID, invalid envelope, atomic failure,
  refresh preservation, tafsir cache, and header-origin isolation.

Do not invent undocumented rate limits, nullability, or error contracts.

## Slice 2 — Entry, hub, search, tabs, bookmarks, last read

Implement QUR-FR-001/005/006/007/011/012:

- Accessible Beranda entry; immediate Quran dark/system-bar boundary and prior
  theme restoration on exit.
- Existing Navigation 3 stack; hidden bottom bar within Quran; stateful Back.
- Initial checking, determinate preparation, retry, no-local-offline,
  populated, and non-blocking refresh states without fake Quran text.
- Surah, Juz, Bookmark, Terakhir Dibaca tabs with restored tab/search/scroll.
- Continue-reading panel when a position exists.
- Local-only case/diacritic-tolerant Latin surah-name/number search.
- Juz 1–30 derived only from local Kemenag metadata.
- Idempotent local ayat bookmarks and one global `(surah, ayat)` last position.
- Every approved loading/empty/error/invalid-target state.

## Slice 3 — Readers, surah start, long press, settings

Implement QUR-FR-008/009/010/014/015/016/020:

- Arab-only responsive flowing pages grouped by `halaman`, inline markers, and
  stable annotated ayat character ranges for hit-testing/selection.
- Keep Room rows authoritative; never persist a concatenated representation.
- Follow 09 normal → 09b selected → 10 sheet; never revive 07.
- Arab+translation lazy rows with exact optional annotations and no Latin.
- Disable platform Quran/translation text selection and copy behavior.
- Long press is the sole visible action affordance, with semantics, haptic,
  precise selection, and clearing on dismissal. Ordinary taps do nothing.
- Only bookmark, tafsir, mark-last-read, and Juz/page actions.
- Exact surah-start header and basmalah rules.
- Full-screen settings with live preview, gated fonts, Arabic size/spacing,
  translation size, global mode, and Quran-only brightness; persist immediately
  and restore prior brightness on exit. No keep-screen-on.
- Portrait-primary, not orientation locked; centered readable large-window
  column and rotation/process state restoration.
- Smooth lazy behavior on representative API 26-class constraints.

## Slice 4 — Tafsir, Aktivitas/streak, source, privacy

Implement remaining QUR-FR-013 plus 017/018/019:

- Tafsir sheet with source line, fixed Ringkas/Tahlili labels, loading, cached,
  stale-refresh, offline, inline retry/failure, independent scroll, and
  reader-preserving dismissal.
- Write one session only when position advances at least one ayat; no progress
  means no event.
- Integrate with existing Aktivitas vocabulary and one combined amalan streak;
  no Quran-only streak.
- Keep all Quran user state/cache local and out of logs/analytics/upload.
- Full-screen source view from hub/settings with exact attribution, cache and
  permission explanation, and no official-app implication.
- Update privacy/security/readiness docs only with verified facts/evidence.

## Slice 5 — Parity, accessibility, security, release validation

- Compare every state with retained Quran references and fix material drift.
- Verify TalkBack order/actions, semantic long-click, modal focus, separate
  Arabic/translation semantics, 48dp targets, 1.5x font scale, RTL shaping,
  combining marks, reduced motion, restoration, rotation, and large windows.
- Verify dark surfaces/system bars and theme/brightness restoration on all exits.
- Audit manifest/network security, client isolation, native/signing gate,
  symbol stripping, R8/shrinking, redaction, privacy, and secret absence.
- Search source/generated files/APK/AAB/symbols/strings/logs/test output/docs for
  credentials and forbidden Latin/copy/share/audio. Never print a discovered
  secret; redact and remove it safely.
- With authorized configured credentials only, exercise online clean install;
  also verify offline clean install, prepared airplane mode, Surah 114 order,
  atomic failures, refresh preservation, both readers, basmalah exceptions,
  actions/tafsir states, Activity/streak, settings/death, and enabled fonts.
- Perform OLED/LCD and a continuous 30-minute reading check only with available
  hardware/time; report missing manual checks honestly and never claim medical
  eye-comfort guarantees.
- Reconcile product/design/engineering/roadmap/readiness/progress docs with
  actual behavior; do not rewrite history.

## Validation

At each Android slice run at minimum:

```text
./gradlew :app:ktlintFormat
./gradlew :app:ktlintCheck :app:detekt :app:lint :app:assembleDebug
```

Also run as applicable:

```text
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleRelease
./gradlew :app:installDebug
```

Run targeted instrumented/UI tests and device checks when available. Inspect
`adb devices` first; do not wait indefinitely. Use actual repository task names
if different. Missing production secrets should trigger the designed negative
release gate; distinguish that from an implementation defect.

Never claim a command, test, build, comparison, audit, API call, or manual check
passed unless it ran successfully. Record exact failures/counts where possible.

## Blockers must not stop independent work

When an external input is absent—production credentials, written font
permission, King Fahd file/licence, final attribution approval, hardware, or
formal API evidence—automatically:

1. prove absence without exposing sensitive values;
2. implement every safe boundary/fake/disabled gate/error state possible;
3. mark only directly dependent checks blocked;
4. continue every other slice;
5. record the exact needed input in progress/readiness docs.

Never bypass a legal/content gate by inventing data or weakening a requirement.

## Context continuation

Work continuously and compact when available. Before context becomes unsafe,
finish a coherent unit, leave the tree buildable where possible, validate, and
write/update:

- `docs/CLAUDE_QURAN_HANDOFF.md`
- `docs/CLAUDE_QURAN_NEXT_PROMPT.md`

Record the full objective, completed requirements/slices, current slice, exact
files, decisions, command results, blockers, ordered remainder, precise next
action, branch/commit, index/worktree ownership, and assumptions. The next
prompt must read the handoff and continue automatically under these rules.
Do not create handoffs at every milestone—only for a real interruption risk.

## Git and external actions

- Local read/edit/build/test/install/emulator work is pre-authorized.
- Local checkpoint commits are allowed only when safely isolated.
- Never push, force-push, open/merge a PR, deploy, publish, upload artifacts,
  rotate credentials, modify remote infrastructure, or submit to a store
  without a separate explicit request.
- Never use destructive broad commands or delete user-owned data.

## Final response

Do not emit a final response after an intermediate slice. Finish all five, or
reach a state where only proven external blockers remain and all independent
work is complete. Then report only the `CLAUDE.md` format: implemented work,
files created, files modified, commands, exact results, known blockers, next
recommended milestone.

Begin now. Perform the work; do not merely rewrite this prompt, produce another
plan, or wait for permission.
