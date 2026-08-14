# Nahwu Quiz — Daily Challenge & Engagement Mechanics

**Document version:** 1.0
**Target release:** Folded into the still-unreleased Android `0.0.5` (Nahwu
Quiz has not shipped yet — see §2)
**Status:** Product decisions made via a grilling session with the product
owner; Android implementation not started
**Product owner:** Ahmad Fahmi Aisar
**Approved:** 13 August 2026

## 1. Purpose

Nahwu Quiz `0.0.5` (individual, guest, offline-first multiple-choice
practice, per ADR [0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md))
is already implemented end-to-end in the codebase — Landing, Daftar Paket,
Detail Paket, Instruksi, Pertanyaan/session, Hasil Kuis, and Riwayat screens
all exist and are wired into navigation from Beranda's "Belajar" entry. It
has never been released, and its bundled question bank is placeholder
`[FIXTURE]` content only (2 packages, 6 real-shaped questions total).

This document defines the engagement layer the product owner asked for on
top of that baseline — a daily challenge, a streak, in-session game feel,
and a real content plan — scoped specifically to stay inside SanguSantri's
existing guardrails (non-commercial, no accounts, no new backend, no
leaderboard through `0.0.5`). It does **not** authorise implementation by
itself; it is the shared-understanding artifact from that session, for a
future implementation pass to build against.

## 2. Relationship to the existing `0.0.5` baseline

Because Nahwu Quiz has never shipped, there is no released version to layer
a point release on top of. **Recommendation: fold this engagement layer
into `0.0.5` itself** rather than inventing a new version number — when
`0.0.5` finally ships, it ships with the daily challenge included. This is
a scheduling recommendation, not a locked decision; confirm before the
implementation pass if a split release is preferred instead.

Everything already built (package browsing, per-package practice sessions,
resume-in-progress, per-package Riwayat Skor) is **unchanged** by this
document. All new mechanics below live in a new, separate daily-challenge
flow.

## 3. Guardrails carried over unchanged

Confirmed explicitly in the grilling session — none of these are reopened
by this document:

* No login, no accounts, no pesantren representation, no user profile.
* No leaderboard, no social/competitive ranking, no server-verified
  scoring — local score/streak is never trusted for competitive ranking
  (ADR 0013).
* No new backend beyond Firebase Hosting static files (ADR
  [0014](../decisions/0014-firebase-hosting-static-content-delivery.md)) —
  every mechanic below is computed entirely on-device from the bundled
  question bank.
* Never a bottom-navigation destination — reached only from Beranda.
* No advertising, subscription, or monetisation of any kind
  (`docs/product/ROADMAP.md`).
* No sharing/export feature — considered and explicitly declined (see §9).

## 4. Approved product decisions

| Area                     | Decision                                                                                                                              |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| Social/competitive scope | Stay inside current guardrails now; leaderboard is documented as deferred future work only (§9), not built                            |
| Daily challenge shape    | A dedicated mode, separate from package practice: fixed question set, same for every user each calendar day, resets at local midnight |
| Daily challenge attempts | One attempt per calendar day                                                                                                          |
| Streak architecture      | Standalone to Nahwu Quiz — does **not** feed or read the existing Aktivitas streak                                                    |
| Streak driver            | Completing that day's daily challenge; regular package practice does not affect the streak                                            |
| Reward model             | Streak count only — no XP, levels, or badges                                                                                          |
| Missed-day policy        | Hard reset to zero the next day — no grace day, no freeze tokens                                                                      |
| In-session game feel     | Timer + combo bonus + rich sound/haptic/animation feedback — **daily challenge only**                                                 |
| Package-practice mode    | Unchanged: untimed, shows explanation after each answer, no new mechanics added                                                       |
| Re-engagement trigger    | No dedicated notification. Beranda's Nahwu Quiz entry gets a live streak/status indicator instead                                     |
| Sharing/virality         | Not built                                                                                                                             |
| Content structure        | Classical kitab tiers: Jurumiyah → Imrithi → Alfiyah (basic → advanced), replacing the two `[FIXTURE]` packages                       |
| Content authorship       | Product owner self-authors/sources, starting with Jurumiyah tier only (~60–90 real questions)                                         |

## 5. Daily Challenge mechanics

### 5.1 Question selection (offline, deterministic, no server)

* Eligible pool = every question in every package flagged as real content
  (see §7 schema note) — not scoped to a user's individual package
  progress, since the challenge is identical for every user that day.
* **Shuffled-cycle selection**, not independent-random-per-day: deterministically
  shuffle the full eligible pool once per cycle (seeded, e.g. by cycle
  number), then consume questions sequentially, N per day, through that
  shuffled order. When the shuffled list is exhausted, reshuffle with a new
  seed and start the next cycle. This guarantees **zero repeats within a
  cycle** and full pool coverage, rather than the weaker guarantee plain
  per-day random sampling gives — directly serves "menantang tiap harinya"
  without the daily set feeling recycled.
* Proposed default: **8 questions per day** (~2–3 minutes including the
  timer) — adjustable.

### 5.2 Timer, combo, feedback (proposed defaults, adjustable)

* **20 seconds per question.** Timeout auto-submits as incorrect, breaks
  the combo, and reveals the correct option — same visual treatment as the
  existing `Feedback Salah` state, just triggered by the clock instead of a
  wrong tap.
* **Combo** is a real-time, in-session-only counter of consecutive correct
  answers (e.g. "Kombo x3!" with escalating sound/haptic). Since there is
  no XP system and no sharing, combo has **no numeric scoring effect** — it
  is feedback/delight only. The session's peak combo is shown on the
  result screen as a personal-best-style stat, not shared or persisted
  beyond that attempt's own row.
* Rich feedback (sound, haptic, micro-animation) fires on every answer,
  correct or incorrect, matching this project's accessibility rules
  (icon + text together, never colour alone — `docs/design/
  ACCESSIBILITY.md`).

### 5.3 Streak

* Streak is **computed from the attempt log, not stored as a mutable
  counter** — count of consecutive calendar dates with a completed daily
  challenge, ending today or yesterday (yesterday still "alive" until local
  midnight passes). This mirrors the durable-event-log pattern Aktivitas
  already uses (`amaliyah_completion_events`, decoupled from mutable
  progress state) rather than reinventing a fragile stored counter that can
  drift out of sync.
* A day with zero completed daily-challenge attempts breaks the streak the
  following day (§4).

## 6. Beranda integration

Beranda's existing "Belajar" entry tile gains a live indicator, computed
from the same attempt log as §5.3:

* Streak count with a flame-style icon when the streak is ≥ 1.
* A visually distinct state for "today's challenge not yet played" vs.
  "already completed today" (e.g. filled vs. outline treatment) — this tile
  is now the **only** daily re-engagement trigger (no notification), so it
  needs to be genuinely noticeable, not a quiet badge.
* Falls back to the existing plain entry-point appearance when the streak
  is 0 and today is unplayed (no false urgency for a brand-new user).

Package practice remains reachable exactly as today, one level deeper
(Landing → "Lihat paket soal").

## 7. Data model additions (for the implementation pass)

Proposed, following this codebase's existing Nahwu Quiz model conventions
(`domain/model/NahwuQuizAttempt.kt` etc.) — not yet built:

* **`NahwuDailyChallengeAttempt`**: `id`, `challengeDate` (the local date
  this attempt is for — the seed), `questionIds` (ordered list, the day's
  selected set), `currentQuestionIndex`, `correctCount`, `totalCount`,
  `peakCombo`, `startedAtEpochMillis`, `completedAtEpochMillis`. One row per
  calendar date per device, mirroring `NahwuQuizAttempt`'s existing shape.
* **Streak**: derived by query over `NahwuDailyChallengeAttempt.challengeDate`
  (§5.3), not a stored field.
* **`nahwu_quiz_bank.json` schema**: add an explicit
  `includedInDailyChallenge: Boolean` flag per package (default `false`),
  rather than inferring eligibility from title text — set to `true` only
  once a package's content is real (non-`[FIXTURE]`). `schemaVersion`
  should bump accordingly.
* New Riwayat surface: a "Riwayat Tantangan Harian" list, separate from the
  existing per-package "Riwayat Skor Individual," reusing existing history
  UI patterns.

## 8. Content plan

* **Structure**: three packages replacing today's two fixtures — Jurumiyah
  (basic), Imrithi (intermediate), Alfiyah (advanced), matching the
  existing `Nahwu Dasar` / `Nahwu Lanjutan` basic→advanced framing already
  present in the fixture data.
* **Volume target**: Jurumiyah tier first, roughly 60–90 questions —
  enough for the shuffled-cycle mechanic (§5.1) to run ~2–3 months at 8
  questions/day before any repeat. `includedInDailyChallenge` flips to
  `true` for Jurumiyah once that volume is reached; Imrithi and Alfiyah
  ship later as separate content passes and join the eligible pool when
  ready.
* **Authorship**: product owner self-authors/sources this content. Per
  CLAUDE.md's content-safety rules, this is **not** a claim of kyai/ustaz
  review — nothing in the app or its copy may imply religious-authority
  sign-off that doesn't exist. Each question should cite its source kitab
  (e.g. *Matn al-Ājurrūmiyyah*) so provenance is inspectable later.
* **Risk classification** (per `docs/product/PRD.md` §3.1's risk-based
  publication model, `docs/operations/CONTENT_GOVERNANCE.md`): this model
  was written with amaliyah ritual text in mind and has never explicitly
  covered Nahwu grammar-drill content. Classical kitab-based grammar
  questions plausibly qualify as "standard, commonly practised, publicly
  accessible, trusted-source" material — i.e. product owner's own editorial
  acceptance (with the source cited) may be sufficient without mandatory
  ustaz sign-off, the same way this session's self-authorship decision
  already implies. **Recommendation: state this extension explicitly in
  `docs/operations/CONTENT_GOVERNANCE.md`** during the implementation pass
  so it isn't left ambiguous — this document flags the gap rather than
  silently assuming it.
* Fixture packages/questions must not reach a release build, per CLAUDE.md
  — real content fully replaces them before `0.0.5` ships.

## 9. Explicitly out of scope / deferred

Considered during the grilling session and deliberately not built now:

* **Leaderboard / social competitive ranking** — would require Accounts
  (`0.1.0`), a real backend beyond static hosting, anti-cheat, and a formal
  amendment to ADR 0013. A future, separately-scoped and separately-approved
  milestone, not a `0.0.5` deliverable.
* **Shareable result card** — considered as the main remaining
  organic-growth lever within current guardrails, explicitly declined.
  Revisit only as its own future decision.
* **Dedicated push notification** for the daily challenge — declined in
  favour of the Beranda status indicator (§6). Revisit if Beranda
  visibility proves insufficient once there's real usage data.
* **Aktivitas streak integration** — the quiz streak stays fully standalone,
  not merged into or read by Aktivitas.

## 10. Open parameters flagged for adjustment

These are concrete defaults proposed in this document, not independently
grilled — cheap to tune during implementation, called out so they aren't
mistaken for settled requirements:

* Daily challenge length (proposed: 8 questions).
* Per-question timer (proposed: 20 seconds).
* Timeout behaviour (proposed: counts as incorrect, breaks combo, reveals
  correct answer).
* Combo definition (proposed: visual/audio-only, no scoring effect).

## 11. Related documents

* ADR [0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md)
  — the guardrails this document stays inside.
* ADR [0014](../decisions/0014-firebase-hosting-static-content-delivery.md)
  — no-new-backend constraint.
* `docs/product/PRD.md` §3.1, `docs/operations/CONTENT_GOVERNANCE.md` —
  content risk-based publication model referenced in §8.
* `docs/product/GROWTH_RESEARCH.md` — the exploratory research that first
  flagged Nahwu Quiz as SanguSantri's most differentiated unbuilt feature
  and the basis for treating it as a priority DAU lever.
* `docs/design/design-export/future-releases/05-release-0.0.5-nahwu-quiz.md`
  — the existing approved visual spec for the base `0.0.5` screens this
  document adds to, not replaces.
* `docs/PROGRESS.md` — current actual implementation state (base `0.0.5`
  built, engagement layer in this document not yet started).

## 12. Next steps

1. Product owner confirms §2's fold-into-`0.0.5` recommendation (or
   requests a split release).
2. Author the Jurumiyah-tier real question bank (§8) — this blocks
   everything else, since the daily challenge has nothing real to select
   from until it exists.
3. Implementation pass: data model (§7), selection algorithm (§5.1),
   session mechanics (§5.2), streak query (§5.3), Beranda indicator (§6).
4. Update `docs/PROGRESS.md`, `docs/product/ROADMAP.md`, and
   `docs/operations/CONTENT_GOVERNANCE.md` (§8's flagged gap) as part of
   that implementation pass, not before content exists to validate the
   design against.
