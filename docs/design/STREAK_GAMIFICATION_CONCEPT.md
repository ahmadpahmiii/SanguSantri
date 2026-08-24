# Amalan Harian — daily streak concept

**Status:** **Implemented 2026-08-24** (Phases A and B below). The Aktivitas card,
the Beranda pill, Mode Udzur, the two targets and the streak rules all ship;
Phase C (milestone du'a/hadith content) does not — see §6.

**Scope:** the daily-consistency layer of Aktivitas (`0.0.3`) — what makes a day
count, what the user sees, and what it would take to build. Requested
2026-08-24: "daily achieve like the fire streaks … so user just have adrenaline
to do tasbih or read the quran with 3 page."

**Decided by the product owner, 2026-08-24:**

1. **Two targets only — dzikir and Al-Qur'an.** Amaliyah is not a daily target.
2. **Both must land** for the day to count toward the streak.
3. **Hari maaf exists only for udzur.** No weekly grace day for everyone.

Still open: milestone content (§8.4) and whether a daily reminder is in scope
(§8.5).

**Related:** `docs/product/PRD.md`, `docs/design/DESIGN_SYSTEM.md`,
`docs/design/ACCESSIBILITY.md`, `ObserveActivityOverviewUseCase`.

---

## 1. What exists today, and why it does not motivate anyone

`ObserveActivityOverviewUseCase` already computes `currentStreakDays` and
`longestStreakDays` from real event timestamps, and `ActivityStreakSection`
renders them as one line of text inside Aktivitas.

Three things are missing, and all three are why it produces no pull:

1. **A day costs nothing.** Any single event — one tasbih tap, one ayat — makes
   the day active. A streak you cannot fail measures app-opening, not amaliyah.
2. **Nothing says what today still needs.** The number looks backwards. There is
   no *kurang 1 lagi*, which is the whole mechanism being asked for.
3. **It hides where the decision happens.** Beranda is the screen people open;
   the streak sits a tab away and disappears entirely at zero (`hasStreak`).

## 2. Design principles — this is ibadah, not a game

| Principle                   | What it forbids                                                                          |
|-----------------------------|------------------------------------------------------------------------------------------|
| **Istiqamah, not points**   | No XP, coins, levels, or power-ups. The only counter is days.                            |
| **No shame**                | A broken streak is never red, never "you lost it". It resets quietly and offers today.   |
| **No riya**                 | No leaderboard, no friends, no sharing of streak counts.                                 |
| **No manufactured urgency** | At most one gentle reminder a day, none after Isya, no countdowns.                       |
| **Never fabricate**         | Every number comes from a real recorded event, as `ActivityOverview` already guarantees. |
| **Udzur is normal**         | Menstruation, illness and travel are part of the religion, not a failure (§5).           |

## 3. The rule

```
        Dzikir  ✓   +   Al-Qur'an  ✓        →  hari lengkap, streak +1
        Dzikir  ✓   +   Al-Qur'an  —        →  belum lengkap, streak berhenti
        Sedang udzur:  Dzikir ✓             →  hari lengkap, streak +1
        Sedang udzur:  tidak ada apa-apa    →  streak dijeda, tidak putus
```

| Target    | Default   | Counts when                                                         |
|-----------|-----------|---------------------------------------------------------------------|
| Dzikir    | 1 sesi    | a tasbih round is archived to Riwayat today (any target, ≥ 1 count) |
| Al-Qur'an | 3 halaman | ≥ 3 distinct mushaf pages touched by today's reading sessions       |

Both are required. Amaliyah completions still appear in Aktivitas history — they
are simply not part of the daily goal.

**Consequence, stated plainly:** a user who never opens the Qur'an reader can
never hold a streak. That is the point of a 2-of-2 rule, but it is also the one
way this design can feel unfair to a dzikir-only user, so the Qur'an target is
kept deliberately small (3 halaman ≈ a few minutes) and the copy never implies
the user has failed at anything.

**Streak rules**

- The day boundary is local midnight, device timezone — the rule the existing
  streak already uses. (Fajr is religiously tempting and technically miserable;
  rejected.)
- Backdating is impossible. Only events actually recorded today count.
- Today never breaks the streak while it is still in progress; the break is
  evaluated for days that have already passed.
- A missed day resets the current streak to 0. The record
  (`longestStreakDays`) is never reset — that is the number that survives.
- No weekly hari maaf. The only forgiveness is udzur (§5).

## 4. The screens

Full visual treatment, every state, and the copy table: the design page
published for this concept (see §9). The two placements are:

**Aktivitas** — the Amalan Harian card replaces today's `ActivityStreakSection`
as the first block: today's two rows, the streak count with its record, seven
day dots, and the Mode Udzur control. Each row taps straight into the feature
that satisfies it (Tasbih, the Qur'an reader). A card that only reports is half
the feature; the tap-through is what turns intention into action.

**Beranda** — one compact pill under the greeting row: flame, day count, seven
dots, and what is still missing today. Tapping it opens Aktivitas. At zero it
does not vanish — it reads *Mulai hari ini*, because a hidden card can never
start a streak.

## 5. Mode Udzur — the only hari maaf

A woman who is menstruating does not read the Qur'an or pray. With a 2-of-2 rule
she would break her streak every single month, and an app that burns a 40-day
streak for that is telling her she has sinned, which is both wrong and cruel.

- A **"Sedang udzur"** toggle in the Amalan Harian card. It may also be offered
  once after two consecutive missed days — as an option, never as an accusation.
- While active, the Qur'an target is **suspended**: dzikir alone completes the
  day and extends the streak. A day with nothing at all is **bridged** — skipped
  when counting consecutive days, so the streak survives but does not grow.
- No reason is ever asked for. Nothing is stored beyond which dates were marked.
- Turning it off ends udzur today (not yesterday) and resumes the normal 2-of-2
  rule; the days already marked stay marked.

## 6. Milestones

3, 7, 30, 40, 100 and 365 days. The one-time bottom sheet is built and fires at
those streak lengths (highest reached one only; dismissing it absorbs the lower
ones), but it deliberately carries **no du'a or hadith text**: scripture needs a
named published source and editorial approval (`CLAUDE.md` content safety), none
has been supplied, and inventing or paraphrasing one is never acceptable. It says
the plain true thing instead — "Masya Allah — %d hari berturut-turut" — and the
sourced text drops into the same sheet once decision §8.4 is answered.
No badge shelf, no collectibles.

## 7. What it took to build

**No Room schema change was needed:**

- Dzikir → `tasbih_history` rows dated today. Reliable since 2026-08-24, when a
  completed round started being archived the moment it reaches its target.
- Qur'an halaman → `quran_reading_sessions` (surah + ayat range) joined against
  `quran_verses.page`, which the Kemenag import already stores and indexes.
  `COUNT(DISTINCT page)` over a day's ranges is the whole computation, so the
  "3 halaman" wording is literally measurable — no new data, no estimation.
- Goal-aware streak → an extension of `ObserveActivityOverviewUseCase`; the
  day-set walk already exists and only its per-day predicate changes.

**Needs new persistence — DataStore, not Room,** which keeps the destructive
migration policy out of this entirely:

- Udzur dates (a set of ISO dates plus "active since").
- Milestone "already celebrated" flags.

**Phasing**

| Phase | Content                                                        | Status                                          |
|-------|----------------------------------------------------------------|-------------------------------------------------|
| A     | Two targets, goal-aware streak, the Aktivitas card, Mode Udzur | **Shipped**                                     |
| B     | The Beranda pill with tap-through                              | **Shipped**                                     |
| C     | Milestones and the completion moment                           | Sheet shipped **without** du'a/hadith text (§6) |

Udzur is inside Phase A, not deferred: with a 2-of-2 rule the feature is not
shippable without it.

**One rule the build settled that this document did not:** turning Mode Udzur
*off* ends udzur **today**, not yesterday. Days already spent in it stay marked,
but today reverts to an ordinary 2-of-2 day. Keeping today marked would have left
the card saying "sedang udzur" beside a switch that is off, and would have told a
woman whose udzur has just ended that she still may not read.

## 8. Decisions

| # | Question                                                                                                                 | Status                                    |
|---|--------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|
| 1 | Which daily targets                                                                                                      | **Decided** — dzikir + Al-Qur'an only     |
| 2 | One target or all of them                                                                                                | **Decided** — both must land              |
| 3 | Hari maaf                                                                                                                | **Decided** — udzur only, no weekly grace |
| 4 | Milestone du'a/hadith: who supplies and approves the text, from which published source                                   | **Open** — blocks Phase C only            |
| 5 | A single daily reminder: in scope, and at what time? Would reuse Pengingat `0.0.4`, not add a second notification system | **Open** — blocks nothing                 |

Are the two targets user-editable, or fixed at 1 sesi / 3 halaman? The design
page assumes **fixed for now**, adjustable later, since an editable target
weakens the streak's meaning and adds a settings surface nobody asked for.

## 9. Design page

The visual design — every card state, the anatomy and tokens, and the full
Indonesian copy table — is published as an artifact:
<https://claude.ai/code/artifact/6a2c3835-f788-4254-8765-14eff71235a1>
