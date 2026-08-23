# SanguSantri Engagement Research — Comfort, Frequency, Challenge, and Uniqueness

**Document type:** Exploratory product research. **Not** a PRD, an ADR, or an
approved roadmap change. Nothing here authorises implementation; per
`CLAUDE.md`, only an explicitly requested milestone gets built. Every idea
below is a candidate for a future product decision.

**Question asked:** what would make SanguSantri *more comfortable to use*,
*opened more often*, *more challenging*, and *genuinely unlike other apps*.

**Method:** the repository's own state was read first (`docs/PROGRESS.md`,
`ROADMAP.md`, the bundled content packages, and the actual `feature/` source
tree), then external claims were checked by search. Confidence is flagged per
claim; where only a search snippet could be retrieved, that is said outright
rather than dressed up as verified.

**Date:** 23 August 2026. **Companion document:**
[`GROWTH_RESEARCH.md`](GROWTH_RESEARCH.md) (13 Aug 2026) — read it for TAM,
DAU targets, and the NU Online / Muslim Pro / Qur'an Kemenag competitive
landscape, which this document does not repeat. **Its §2 baseline is now
stale — see §1 below.**

---

## 1. Baseline correction: what actually shipped since 13 August

`GROWTH_RESEARCH.md` §2 describes a product at `versionName 0.0.4` whose
newest work was Quran and Kalender Hijriah. Ten days later the tree says
`versionCode = 11`, `versionName = "0.0.5"`, and `docs/PROGRESS.md` records a
large amount of engagement-relevant work that document never saw:

| Shipped since 13 Aug                                             | Engagement significance                                                                                                                                  |
|------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Jadwal Sholat + Kiblat (myquran, ADR 0018)                       | The app now knows the five daily prayer clock events. This is the single biggest new lever (§3.1).                                                       |
| Adzan notification **with audio** + alarm plumbing               | `data/prayeralarm/` — scheduler, receiver, channels, playback service. Five daily fired events already exist.                                            |
| Three home-screen widgets                                        | `PrayerTimesWidgetProvider`, `…WideWidgetProvider`, `…AyatWidgetProvider`. `GROWTH_RESEARCH.md` recommended a widget as unplanned scope — it now exists. |
| Murottal per-ayat playback + per-surah download                  | First audio surface in the app; `keepScreenOn` exists here and **only** here.                                                                            |
| Mushaf page mode, swipe both directions, audio-driven page turns | Quran reading is now a genuinely competitive surface.                                                                                                    |
| Ayat Hari Ini (Beranda header, share card, widget)               | Fully implemented **except its endpoint** — still on `FixtureAyatHariIniRemoteSource`.                                                                   |
| Sholawat `0.0.8` scaffolding                                     | Blocked on the product owner supplying titles + sources.                                                                                                 |
| Nahwu Quiz: engagement spec + 31 real Jurumiyah questions        | Spec written, content started, **mechanics unimplemented**.                                                                                              |

**Two things are built and not earning anything yet**: Ayat Hari Ini (waiting
on a CMS endpoint) and Nahwu Quiz's daily-challenge layer (spec'd, not coded).
Both appear again in the ranked shortlist for that reason.

---

## 2. Comfort — remove friction from what users already do

Comfort ideas are ranked by annoyance-removed per line-of-code. These are all
small; none needs a product decision beyond "yes".

### 2.1 Keep the screen awake in the amaliyah reader

`grep` for `keepScreenOn` returns exactly one call site:
`feature/quran/reader/QuranReaderScreen.kt`, and only while murottal is
playing. A full Tahlil is 37 steps; Istighosah is 25. The screen sleeps
mid-recitation and the user must unlock with wet or raised hands. The Quran
reader already proves the pattern (`QuranKeepScreenOnEffect`).

**Effort:** hours. **Reuse:** existing effect, existing `ReaderSettings`
DataStore. **Risk:** none. This is the highest comfort-per-effort item found.

### 2.2 Auto-advance / auto-scroll in the readers

`autoScroll` does not appear anywhere in the source. `GuidedProgressionMode`
already models `MANUAL` progression, so the seam exists. A slow, pausable
auto-advance (with a speed control, off by default) is what turns the reader
from "a thing you tap through" into "a thing you can recite alongside".

**Effort:** small–medium. **Risk:** must never skip a step whose
`repeatTarget` is unmet — the counter is the correctness boundary.

### 2.3 Audio for Tahlil and Istighosah

The clearest comfort gap: Quran has murottal, amaliyah has nothing. Reciting
Tahlil while driving or walking is impossible today.

**This is content-blocked, not code-blocked.** The playback stack
(`data/audio/`, `MediaSessionService`, one shared ExoPlayer) is already built
and generic. What is missing is a lawful, attributable recording — inventing
or scraping one violates the content-safety rules outright, and murottal's own
precedent (ADR 0018 amendment) is that audio came from an identified source
with an attributed reciter. Treat this as "product owner must commission or
license a recitation", not as an engineering task.

### 2.4 Smaller wins worth batching

* Reader theme parity — the Quran reader gained a user-controlled Light mode
  (2026-08-10); confirm the amaliyah reader offers the same choice.
* One-hand reachability: the repetition counter and next-step control should
  both sit in the bottom third. (Verify on device before changing anything —
  the Guided Reader was designed for this and may already comply.)
* Ship the `arabicFont` app-wide setting's discoverability — it was unified
  across readers on 2026-08-22 but users only find it inside the Quran.

---

## 3. Frequency — more reasons to open the app, from infrastructure that exists

### 3.1 Wirid ba'da sholat, wired to the adzan alarm ★ top recommendation

The app now fires five accurate, location-aware, audio-backed notifications a
day and does nothing with them afterwards. The user hears adzan, prays, and
opens *another* app for wirid.

The proposal: after each prayer alarm, a follow-up notification (or an action
on the same one) that opens a short wirid ba'da sholat — read in the existing
reader, counted with the existing tasbih, logged into the existing
`amaliyah_completion_events` table, feeding the existing streak.

Why this is the strongest frequency lever available:

* It converts an existing 5×/day system event into an in-app session. Every
  other idea in this section has to *manufacture* a touchpoint; this one is
  already firing.
* Reuse is near-total: `PrayerAlarmScheduler` / `PrayerAlarmReceiver` /
  `PrayerNotificationChannels` for the trigger, the reader for display, Tasbih
  for counting, Aktivitas for the streak. New code is the wiring plus a
  content package.
* `GROWTH_RESEARCH.md` §5 item 3 identified "a daily rather than weekly
  touchpoint" as the gap versus Muslim Pro. Prayer times shipped after that
  document was written; the gap is now closable with roughly a package of
  content and a notification action.

**Positioning note (medium confidence, from search):** the well-executed
habit-tracking dzikir apps in this space — *Dzikir Pagi Petang* and
*Dzikir Pagi Petang Sesuai Sunnah*, the latter explicitly sourced from Yazid
bin Abdul Qadir Jawas' *Doa dan Wirid* — carry habit trackers, memorisation
modes and reminders, and are unambiguously salafi in editorial framing. NU-
tradition users get NU Online Super App, which is broad but not habit-shaped.
**A well-built wirid habit loop in the NU/pesantren idiom appears
underserved.** Retrieved 23 Aug 2026 from Play Store listings and secondary
Indonesian tech articles; install counts were not retrievable, so this is a
qualitative gap claim, not a measured market gap.

**Constraint check:** the wirid text is new religious content and goes through
`docs/product/PRD.md` §3.1's risk-based model. Standard, commonly practised
public wirid ba'da sholat from an identified published source (e.g. an NU-
published susunan) takes the product-owner-acceptance path. Anything tied to a
specific ijazah, tarekat, or pesantren (Ratib al-Haddad and Ratib al-Attas
plausibly included) needs kyai/ustaz sign-off — do not lump them together.

### 3.2 Ship the Ayat Hari Ini endpoint

The entire client is done — Room table, sync manager, repository, Beranda
header, share card, third widget — and it is serving
`FixtureAyatHariIniRemoteSource`. This is a daily-changing home surface and a
share surface (the strongest organic-acquisition mechanic in the app, since
there is no ad budget) sitting behind one missing CMS endpoint.

**Effort:** CMS work, not Android work. **Highest ratio of value-unlocked to
Android-code-written of anything in this document.**

### 3.3 Dzikir pagi and petang

The same plumbing as §3.1, two more daily touchpoints, and a natural home for
the NU-idiom positioning above. Do it *after* §3.1, sharing its content
pipeline and notification pattern rather than building a second one.

### 3.4 Put the streak where it is seen

The streak lives in Aktivitas — a destination the user must choose to visit.
Duolingo's disclosed mechanic (cited in `GROWTH_RESEARCH.md` §6.1) works
because the streak is unavoidable. Surface it on Beranda and on the widgets
that already exist. **Cheap; no new data, no new backend.**

One honest caveat: a streak that can only be advanced by a weekly amaliyah is
a *misleading* daily streak. §3.1 and §3.3 are what make a daily streak
truthful. Ship the touchpoints first, then promote the streak.

---

## 4. Challenge — something to be measurably better at

### 4.1 Implement Nahwu Tantangan Harian ★

Spec'd in full (`NAHWU_QUIZ_ENGAGEMENT_PRD.md`: date-seeded question
selection, one attempt per day, timer, combo, standalone streak, Beranda
indicator), content started (31 accepted Jurumiyah questions), **zero code
written**. `GROWTH_RESEARCH.md` already named Nahwu Quiz the one feature with
no competitive equivalent found. It has now been skipped over twice while
Quran and prayer-times work proceeded.

This is the only "challenge" mechanic that is already designed *and* already
approved. Everything else in this section is a new product decision.

### 4.2 Target khatam Al-Qur'an

The Quran reader logs reading events into Aktivitas already. A khatam plan —
pick a horizon (30 days, Ramadan, one juz per week), get a daily page target
and a behind/ahead indicator — turns passive reading into a tracked
commitment.

Not unique: Tarteel ships Smart Goals ("Surah Al-Kahf every Friday", "Review
Juz 30"), streak heatmaps and home-screen widgets as of its 2026 updates
(retrieved 23 Aug 2026; from Tarteel's own marketing pages and app listings,
so treat feature descriptions as vendor claims). Quran.com-family apps have
comparable reading goals. **SanguSantri would be catching up here, not
leading** — but it is catching up on infrastructure it already owns.

### 4.3 Istiqomah target, not just a streak

A streak asks "did you do *something* today". A target asks "did you do *what
you committed to*" — Tahlil every malam Jumat, Istighosah weekly, wirid after
Subuh. The user sets it; Aktivitas measures against it. This fits SanguSantri's
weekly-cadence amaliyah far better than a borrowed daily-streak mechanic, and
it is honest in a way a padded daily streak is not.

### 4.4 Setoran hafalan nadhom (see §5.2)

---

## 5. Uniqueness — what no incumbent combines

Ranked by *defensibility*, with an honest note on what already exists.

### 5.1 Daftar Arwah + jadwal selametan and haul ★★

The single most differentiated idea found, and it is nearly free given what is
already built.

The user keeps a private local list of the deceased they pray for. From a date
of death, the app computes the traditional commemoration schedule —
3, 7, 40, 100, 1000 days (*telung dino, mitung ndino, matangpuluh, nyatus,
nyewu*) and the annual haul — and schedules reminders. On the day, one tap
opens Tahlil, and at the tawassul step the reader shows *their* names.

Verified against the bundled content: `tahlil-v1.json` already contains the
khususon step naming "bapak kami, ibu kami, kakek kami, nenek kami, guru
kami… dan bagi ahli kubur/arwah yang menjadi sebab kami berkumpul di sini".
The user's names belong **beside** that step as a display card — never
injected into the Arabic, never merged into the content package. That
boundary keeps it inside the content-safety rules; violating it would mean
generating religious text from user input.

What it reuses: `feature/reminder/` (0.0.4), `feature/hijricalendar/` (0.0.7,
including the Hijri conversion for haul), the reader, Aktivitas. New surface
is one table and one screen.

Why it is defensible: it is emotionally weighted, recurring by construction,
and specific to Indonesian NU practice — precisely the ground a global app
like Muslim Pro will never cover and the ground SanguSantri's editorial
governance already claims. It is also the strongest *word-of-mouth* feature in
this document; families discuss selametan dates.

**Honesty check — this is not virgin territory.** A Play Store app,
*Yasin Tahlil Selametan Premium* (`id.web.idm.yasin`), is described as storing
a list of names and auto-computing the 3/7/40/100/1000-day and haul schedule,
ad-free, with text/PDF/image export. A direct fetch of its Play listing
returned only the page shell — **this is a search-snippet claim, not a
verified feature list**; before committing, install it and look. What
SanguSantri would add is integration the standalone app cannot have: a real
Hijri calendar, real reminders, a real versioned Tahlil reader, and activity
history — not novelty.

**Also verify before building:** the observance schedule itself is religious-
practice content. The commonly cited set (3/7/40/100/1000 + haul, with the
Javanese names) is standard NU-community practice with no specific Qur'anic or
hadith text prescribing the intervals — Indonesian religious media state this
plainly. Present it as tradition the user is tracking, never as a ruling the
app is issuing, and never imply the app decides when a selametan is due.
Retrieved 23 Aug 2026: [detik — Tahlilan 3, 7, 40, 100 Hari, Apakah
Wajib?](https://www.detik.com/hikmah/doa-dan-hadits/d-8302292/tahlilan-3-7-40-100-hari-apakah-wajib),
[NU Online — Susunan Bacaan Tahlil, Doa Arwah
Lengkap](https://nu.or.id/syariah/susunan-bacaan-tahlil-doa-arwah-lengkap-dan-terjemahannya-drr3t).

### 5.2 Lalaran nadhom, closed-loop with Nahwu Quiz ★

*Lalaran* — chanting nadhom repeatedly until memorised — is a daily pesantren
routine, not an occasional one. The bundled Nahwu Quiz bank is already
Jurumiyah-tier.

The loop nobody currently closes: **read the nadhom → lalaran with a looping
audio/tempo aid → self-assess the hafalan → get quizzed on the *same* matn by
Nahwu Quiz → see it in Aktivitas.**

Existing apps cover only the first step. *Nadhoman Santri*, *Nadhom Alfiyah
Ibnu Malik*, and *Kitab Imrithi dan Terjemah* are text/audio presentations of
the nadhom with no memorisation tracking and no assessment tied to the same
text (retrieved 23 Aug 2026 from Play Store and APKPure listings — feature
descriptions are from listing text, not hands-on inspection). SanguSantri
already owns the assessment half, which is the expensive half.

**Constraints:** nadhom text is educational religious content under the same
risk-based model that already covers Nahwu Quiz (recorded in
`CONTENT_GOVERNANCE.md`); audio has the same commissioning problem as §2.3.
Scope it to Jurumiyah first — the matn already in the quiz bank — not Alfiyah.

### 5.3 Make the editorial governance visible

SanguSantri's genuinely unusual asset is invisible to users: sourced,
versioned content, corrections that create a new version rather than mutating
one, a documented distinction between "the product owner accepted this" and
"a kyai approved this". Every competitor ships religious text with no
provenance at all.

A modest "dari mana teks ini" surface — source, publisher, version, and what
the approval status actually means — is close to free (compact source
attribution already exists) and is the trust story the whole content pipeline
was built for. **Constraint:** never render editorial acceptance as religious
approval, and never imply NU/PBNU endorsement (PRD §2.2, §6.5).

### 5.4 Mode Imam / jamaah — interesting, expensive, defer

One phone leads a communal tahlil and nearby phones follow the step. Genuinely
unlike anything found. It also means device-to-device networking, a new
permission surface, and a synchronisation model — against a codebase whose
standing rules forbid new backends and speculative subsystems. **Recorded as
an idea; not recommended now.**

### 5.5 Blocked on Accounts / Pesantren Membership

Group targets, pesantren-scoped activity, khataman berjamaah (splitting 30 juz
across a group), leaderboards. All require `0.1.0`/`0.2.0` and ADR 0013 keeps
them out. Unchanged from `GROWTH_RESEARCH.md` §5 item 6 — noted so the
boundary stays explicit.

---

## 6. Shortlist

Ordered by value ÷ effort, using only what the repository already contains.

| #  | Item                                         | Bucket        | Effort    | Mostly reuses                       | New product decision?  |
|----|----------------------------------------------|---------------|-----------|-------------------------------------|------------------------|
| 1  | Ayat Hari Ini endpoint (§3.2)                | Frequency     | CMS only  | Everything — client is done         | No — already approved  |
| 2  | Keep screen awake in amaliyah reader (§2.1)  | Comfort       | Hours     | `QuranKeepScreenOnEffect`           | No                     |
| 3  | Wirid ba'da sholat on the adzan alarm (§3.1) | Frequency ★   | Medium    | `data/prayeralarm/`, reader, tasbih | Yes + content sourcing |
| 4  | Nahwu Tantangan Harian (§4.1)                | Challenge ★   | Medium    | Spec + 31 questions already exist   | No — spec approved     |
| 5  | Daftar Arwah + jadwal selametan/haul (§5.1)  | Uniqueness ★★ | Medium    | Reminder + Hijri calendar + reader  | Yes                    |
| 6  | Streak on Beranda and widgets (§3.4)         | Frequency     | Small     | Aktivitas data, existing widgets    | No                     |
| 7  | Auto-advance in the readers (§2.2)           | Comfort       | Small–med | `GuidedProgressionMode`             | Minor                  |
| 8  | Istiqomah target (§4.3)                      | Challenge     | Medium    | Aktivitas                           | Yes                    |
| 9  | Target khatam (§4.2)                         | Challenge     | Medium    | Quran reading events                | Yes                    |
| 10 | Lalaran nadhom loop (§5.2)                   | Uniqueness    | Large     | Nahwu Quiz half exists              | Yes + content sourcing |

If only one thing is done: **#3**. It is the only idea that multiplies an
event the app already generates five times a day, and it makes every streak
mechanic in the app honest for the first time.

---

## 7. What this document deliberately does not recommend

* Ads, subscriptions, or any monetisation — prohibited without an explicit
  product decision (`ROADMAP.md`).
* A new bottom-nav destination — ADR 0013 locks the shell to
  Beranda | Aktivitas | Tasbih.
* Any new Firebase product beyond Hosting and the one Remote Config exception
  (ADR 0014, amended by ADR 0017).
* Any content invented, scraped, AI-corrected, or transliterated into Latin.
  Three ideas here (§2.3 audio, §3.1 wirid, §5.2 nadhom) are gated on a human
  sourcing real content through the governance model — that gate is the
  feature working as designed, not an obstacle to route around.

---

## 8. Related documents

* [`GROWTH_RESEARCH.md`](GROWTH_RESEARCH.md) — TAM, DAU targets, competitive
  landscape. §2 superseded by §1 here.
* [`NAHWU_QUIZ_ENGAGEMENT_PRD.md`](NAHWU_QUIZ_ENGAGEMENT_PRD.md) — the daily
  challenge design referenced in §4.1.
* [`AYAT_HARI_INI.md`](AYAT_HARI_INI.md) — the missing endpoint in §3.2.
* [`ROADMAP.md`](ROADMAP.md), [`PRD.md`](PRD.md) §3.1,
  [`../operations/CONTENT_GOVERNANCE.md`](../operations/CONTENT_GOVERNANCE.md)
  — the constraints every recommendation was checked against.
