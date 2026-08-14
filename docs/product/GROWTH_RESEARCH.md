# SanguSantri Growth Research — Value Proposition, DAU-Driving Features, and DAU Targets

**Document type:** Exploratory market/growth research. This is **not** a PRD, an
ADR, or an approved roadmap change — it exists to inform a future product
decision, not to make one. Nothing here authorises implementation; per
`CLAUDE.md`, only an explicitly requested milestone should be built.

**Author context:** Prepared by researching this repository's own product
documents first, then cross-checking claims about the market, competitors, and
DAU mechanics against external primary sources where available. Every
non-obvious external claim is cited; where only a secondary source could be
found, that is stated explicitly rather than presented as authoritative.

**Date:** 13 August 2026

---

## 1. Executive summary

**Candidate value propositions**, in descending order of how well they fit
what SanguSantri actually is today (not a generic "Islamic app" pitch):

1. **"The pesantren amaliyah companion, done right."** A focused, offline-first
   reader for the specific communal texts (Tahlil, Istighosah, eventually
   Nahwu drills) santri actually recite together, with correct Arabic/harakat
   and sourced translations, free of ads and invented content — a narrower,
   more trustworthy promise than a general-purpose Islamic super-app.
2. **"Your daily amaliyah habit, tracked honestly."** Aktivitas (streak,
   weekly summary, completion history) plus Tasbih plus Pengingat Amaliyah
   already form a habit loop; leaning into this — the way Duolingo leans into
   its streak — is the single highest-leverage lever available without new
   product scope (§5, §6).
3. **"Nahwu practice built for santri, not tourists."** Nahwu Quiz (`0.0.5`,
   approved but not yet implemented per `docs/PROGRESS.md`) is the one
   planned feature with **no equivalent in any competitor researched** — NU
   Online Super App, Muslim Pro, and the official Qur'an Kemenag app all
   cover prayer/Quran/dzikir ground already; none teach Arabic grammar for
   pesantren curricula. This is SanguSantri's structural differentiator, not
   its Quran or dzikir features, which are commoditised (§4).

**Top feature recommendations** (detail and constraint-fit in §5):

* Finish and ship **Nahwu Quiz `0.0.5`** — already-approved scope, and the
  single most differentiated, non-commodity feature found in this research.
* Ship the substantial **already-implemented but unreleased** `0.0.6`
  Al-Qur'an Kemenag and `0.0.7` Kalender Hijriah work sitting in the
  repository (§2) — a version bump and Play Store release, not new
  engineering, converts existing investment into DAU.
* Extend **Pengingat Amaliyah** toward daily (not only weekly) touchpoints —
  the biggest mechanic gap versus Muslim Pro's five-times-daily azan
  notifications, achievable without audio, ads, or new backend scope.
* A **home-screen widget** for Tasbih/streak — low-risk, no new backend, not
  currently planned anywhere in the docs.
* Flag, do not build yet: **communal/pesantren-group features** (shared
  targets, pesantren-scoped activity) are a plausible future DAU lever per
  general habit-app research, but require Accounts (`0.1.0`) and Pesantren
  Membership (`0.2.0`), which are explicitly out of scope through `0.0.7`
  (ADR 0013).

**DAU target headline: roughly 10,000–40,000 DAU as a 12–24 month range**,
assuming organic-only growth (no ad budget — the product is non-commercial)
reaching a meaningful slice of Indonesia's pesantren-affiliated population,
built from published Kemenag pesantren-population data and standard
habit/education-app stickiness ratios (§6). This is an estimate for
directional planning, not a commitment, and is dominated far more by
**distribution** (an institutional NU/pesantren-network partnership) than by
any single in-app feature — see §6 for the full reasoning chain and its
confidence caveats.

---

## 2. Current product baseline (from this repository)

Sourced from `docs/PROGRESS.md`, `docs/product/PRD.md`,
`docs/product/ROADMAP.md`, `docs/product/QURAN_PRD.md`, and ADRs 0013/0014/0016
— read directly for this research, not assumed from `CLAUDE.md`'s summary
text, which itself says to verify against `docs/PROGRESS.md`.

### 2.1 What is actually released today

`app/build.gradle.kts` currently declares `versionCode = 10`,
`versionName = "0.0.4"`. The released feature set, per the roadmap versions up
to and including `0.0.4`:

* **`0.0.1`** — offline-first Beranda (home) and Jelajahi Amaliyah
  (search/browse/filter) destinations, Tahlil (37 steps) and Istighosah (25
  steps) as Full Reader and Guided Reader experiences with integrated
  repetition counters, local favourites/recently-opened, light/dark theme,
  RTL Arabic support, and an optional 24-hour-gated background content sync
  against static files on Firebase Hosting (bundled content is always the
  mandatory baseline; sync is additive only).
* **`0.0.2`** — standalone Tasbih: 33/100/unlimited/custom targets, optional
  session naming, persisted unfinished counts, haptic feedback. First
  bottom-navigation shell (Beranda | Tasbih).
* **`0.0.3`** — Aktivitas: streak summary, this-week summary, filterable
  amaliyah-completion and tasbih-history sections, all backed by real local
  data only. Bottom nav becomes Beranda | Aktivitas | Tasbih (still current).
* **`0.0.4`** — Pengingat Amaliyah: personal schedules with Tahlil
  malam-Jumat and Istighosah weekly presets, Gregorian/Hijri date handling,
  notification permission flow, reschedule-after-reboot. **Weekly-cadence
  presets, not daily.**

### 2.2 Substantial unreleased work already in the repository

This is a material finding this research surfaced by reading
`docs/PROGRESS.md` in full rather than trusting `CLAUDE.md`'s narrative
alone: **`versionName` is still `0.0.4`**, but the milestone log shows a large
amount of `0.0.6` (Al-Qur'an Kemenag) and `0.0.7` (Kalender Hijriah) work
already implemented and manually verified on-device, dated 2026-08-08 through
2026-08-10 — well past the `0.0.4` release:

* **Al-Qur'an Kemenag (`0.0.6`)** — five delivery slices implemented: LPMQ
  Kemenag API sync into Room, Surah/Juz/Bookmark/Terakhir Dibaca hub, Arab
  saja and Arab + terjemahan readers, tafsir bottom sheet, dark-by-default
  theme with a since-added user-controlled Light mode, Aktivitas/streak
  integration, plus multiple bug-fix/hardening passes (credential digest
  fixes, a 16 KB page-size Play Console rejection fix, a per-surah sync
  retry/crash-safety net, a widened Arabic line-spacing range).
* **Kalender Hijriah (`0.0.7`)** — three delivery slices implemented in one
  pass (domain model including `Pasaran`/pasaran-cycle calculation, UI,
  agenda/provenance) per `docs/PROGRESS.md`'s 2026-08-09 entry, replacing a
  prior "Segera hadir" (Coming soon) placeholder on Beranda with the real
  feature.
* **Not yet implemented: Nahwu Quiz (`0.0.5`)** — despite being earlier in
  roadmap order than both of the above, `docs/PROGRESS.md`'s own
  "Next recommended milestone" notes (as late as the 2026-08-10 entry) point
  back to *resuming* Nahwu Quiz — it was skipped over, not completed, while
  development proceeded directly to Quran and Hijri Calendar work.

**Implication for this research:** the product's real state is more advanced
than a version-number read would suggest for two roadmap items, and less
advanced than roadmap order would suggest for one (Nahwu Quiz). A DAU-growth
plan should treat "ship what's already built" (`0.0.6`, `0.0.7`) and "finish
the one differentiated planned feature that's been skipped" (`0.0.5`) as the
lowest-cost near-term levers, ahead of any new/unplanned feature idea.

### 2.3 Standing constraints that bound every recommendation in this document

* **Non-commercial**: no advertising, subscriptions, or monetisation on any
  roadmap item; none should be added without an explicit product decision
  (`docs/product/ROADMAP.md`). This rules out ad-funded UA and "pay to
  remove ads" premium tiers as growth levers — SanguSantri cannot copy Muslim
  Pro's monetisation-funded UA model.
* **Content safety**: no invented Arabic/translations, no Latin
  transliteration anywhere (including the standalone Quran feature), no
  silently merged content versions, no claimed kyai/institutional approval
  that does not exist in writing. Risk-based publication: standard public
  amaliyah from an identified trusted source needs only the product owner's
  editorial acceptance; higher-risk content (private/pesantren-specific,
  disputed origin, doctrinally sensitive, tied to a specific
  ijazah/sanad/tarekat/pesantren authority) needs kyai/ustaz/sesepuh sign-off
  (`docs/product/PRD.md` §3.1, `docs/operations/CONTENT_GOVERNANCE.md`).
* **Navigation/backend scope lock**: bottom-navigation-only shell (no
  Navigation Rail on any window size) through `0.0.7`, no new bottom-nav
  destinations beyond Beranda | Aktivitas | Tasbih in this window (ADR 0013);
  no new Firebase backend product beyond Hosting, with exactly one narrow
  Remote Config exception already used for the in-app update gate (ADR
  0014, amended by ADR 0017). Any recommendation implying a new backend
  service, leaderboard server, or social feature runs into this wall.
* **Independence from NU/PBNU**: SanguSantri "MUST be presented as an
  independent product and MUST NOT imply that it is an official PBNU or NU
  application" unless a formal relationship exists (`docs/product/PRD.md`
  §2.2) — directly relevant given NU Online Super App is SanguSantri's
  closest structural competitor (§4) and carries the NU brand itself.

---

## 3. Market sizing / addressable santri population

**Confidence: low-to-medium.** No figure in this section comes from the raw
EMIS (Education Management Information System) portal itself — a direct
fetch of `satudata.kemenag.go.id`'s dataset page for santri counts returned an
empty/unloaded template, not populated data. All figures below are
data-journalism aggregations that cite Kemenag/EMIS as their source, not the
primary EMIS database read directly. Treat this section as directionally
useful, not precise.

* **Number of pondok pesantren nationally**: **42,391** as of Kemenag data
  reported around September–October 2025, across all 34 provinces, per
  GoodStats' aggregation of Kemenag figures. [GoodStats — 10 Provinsi dengan
  Pondok Pesantren Terbanyak
  2025](https://data.goodstats.id/statistic/10-provinsi-dengan-pondok-pesantren-terbanyak-2025-LlZsK).
  A separate Databoks/Katadata article citing the same period reports
  **42,433** active pesantren for academic year 2024/2025 — the small
  discrepancy (42,391 vs. 42,433) is itself evidence these are rounded,
  secondary aggregations rather than one canonical figure. [Databoks —
  42,000 Pesantren in Indonesia for 2024/2025, Concentrated in
  Java](https://databoks.katadata.co.id/en/education/statistics/68e38957916b1/42000-pesantren-in-indonesia-for-20242025-concentrated-in-java).
* **Geographic concentration**: Jawa Barat leads with 12,977 pesantren
  (~30.6% of the national total), followed by Jawa Timur (7,347) and Banten
  (6,776) — pesantren, and by extension the santri population, are heavily
  Java-concentrated. [GoodStats, same source as
  above](https://data.goodstats.id/statistic/10-provinsi-dengan-pondok-pesantren-terbanyak-2025-LlZsK).
* **Number of santri nationally — unusually volatile across EMIS snapshots,
  flagged explicitly as a data-quality caveat, not a real enrollment
  collapse**: per one aggregation of EMIS semester-by-semester data —
  academic year 2022/2023: 4,074,011 (odd/ganjil semester) rising to
  4,845,317 (even/genap semester); 2023/2024: 3,143,555 rising to 3,339,536;
  2024/2025: 3,221,332 (odd semester) but then **1,605,445** (even semester)
  — a sharp within-year drop that is far more consistent with an EMIS
  re-verification/deduplication cycle than an actual 50% collapse in
  enrolled santri (GoodStats' own source article on this figure is titled
  "Jumlah Santri Anjlok..." — "santri numbers plunge" — indicating Indonesian
  data journalists themselves flagged this as anomalous rather than a
  confirmed trend). The most recent snapshot found, for the 2025/2026 odd
  semester (reported ~23 October 2025), is **approximately 2.5 million**.
  [GoodStats — Jumlah Santri Anjlok 4 Tahun Terakhir, Pesantren Hadapi
  Tantangan
  Serius](https://data.goodstats.id/statistic/jumlah-santri-anjlok-4-tahun-terakhir-pesantren-hadapi-tantangan-serius-beLNe).
* **Working TAM range for this document**: treat the "currently enrolled,
  resident santri" population as **roughly 2.5–4.8 million**, spanning the
  low and high ends of the EMIS snapshots found, none of which could be
  independently verified against the raw EMIS database. This figure does
  **not** include pesantren alumni (a much larger, unquantified pool — no
  official alumni count was found anywhere in this research) or the general
  public who practise pesantren-style amaliyah without ever having been
  santri (SanguSantri's PRD explicitly includes both as target users,
  `docs/product/PRD.md` §2.4, but neither is sized by any source found).

**What this means for a DAU target**: SanguSantri's addressable population is
a genuine **niche vertical**, not a mass-market Muslim-lifestyle TAM.
Muslim Pro's ~150 million global downloads (§4) reflects a global,
denomination-agnostic prayer/Quran utility; SanguSantri's core audience —
santri, alumni, and NU-tradition-aligned amaliyah practitioners in
Indonesia — is at most single-digit millions, concentrated in Java. Any DAU
target must be anchored to this smaller number, not to what a general
Islamic-lifestyle app could theoretically reach (§6).

---

## 4. Competitive landscape

All Play Store data below was retrieved via direct app-store fetches
(apkpure.com mirrors, since direct Google Play listing fetches were truncated
before reaching install/rating data) and Similarweb, so **install counts
below are lower-confidence estimates from third-party listing mirrors, not
Google's own Play Console figures** — flagged per app.

### 4.1 NU Online Super App (`id.or.nu.app`) — closest structural competitor

Official Nahdlatul Ulama app. Feature set found: Al-Quran with translation
**and transliteration**, worldwide prayer times/notifications, Hijri
calendar with fasting schedule, Qibla compass, worship tutorials
(article/video), Yasin/Tahlil readings, **digital tasbih counter**,
zakat/inheritance calculators, integrated YouTube content, Hajj/Umrah
guidance, and "NUpedia" (an Islamic encyclopedia). Category: Lifestyle;
current version 2.26.6, 70.1 MB, requires Android 7.0+.
[apkpure.com listing, retrieved 2026-08-13] — install/rating counts were not
shown on this mirror. Separately, an NU.or.id article headline surfaced in
search reports the app reached **"2 juta orang"** (2 million people/downloads)
— this is NU's **own self-published claim about its own app**, not an
independent measurement, so it is flagged as organizational/near-primary
rather than independently verified. [nu.or.id — "NU Online Super App Diunduh
2 Juta Orang, Gus Ulil: Prestasi Membanggakan" — page could not be directly
fetched (403), citation is from the WebSearch result snippet only]

**Why this matters most**: NU Online Super App already bundles almost every
feature on SanguSantri's own long-term "pesantren super-app" vision
(`docs/product/PRD.md` §2.2) — Quran, tasbih, Hijri calendar, amaliyah
readings (Yasin/Tahlil) — under the NU brand itself, for free, today. It is
the single biggest reason SanguSantri's differentiation cannot be "we also
have a tasbih counter and a Hijri calendar" (commodity features NU Online
already ships) — it must be editorial rigour (content-safety governance,
sourced/versioned corrections), pesantren-specific scope (private variants,
future), and Nahwu Quiz, none of which NU Online offers.

### 4.2 Muslim Pro (

`com.bitsmedia.android.muslimpro`) — global-scale reference for engagement mechanics

Category: Lifestyle. Version 17.7.1, 100.1 MB. Feature set: location-based
prayer times with per-prayer notification customisation, full Quran with
audio recitation and multiple translations, Qibla finder, mosque/halal
restaurant locator. Freemium: ads in the free tier, Premium subscription for
ad removal and offline reading/listening. [apkpure.com listing, retrieved
2026-08-13]

Scale: reporting around the 2020 X-Mode location-data-sharing controversy put
Muslim Pro's downloads at **"nearly 150 million"** worldwide (one source
cited a lower ~98 million figure) — both from press investigations, not the
company's own stated figures, so treat as secondary/approximate.
[IBTimes — "Muslim Pro App Users' Information May Have Been Harvested By US
Military"](https://www.ibtimes.com/muslim-pro-app-users-information-may-have-been-harvested-us-military-3083342);
cross-referenced via [Vice/Motherboard's original
reporting](https://www.vice.com/en/article/muslim-apps-location-data-military-xmode/).
Per Similarweb's usage-ranking snapshot (2026-08-10), Muslim Pro ranks **#177
overall app usage in Indonesia** and **#156 in the Lifestyle category in the
US** — exact DAU/MAU figures were paywalled behind a Similarweb account and
could not be retrieved. [Similarweb — Muslim Pro app
statistics](https://www.similarweb.com/app/google-play/com.bitsmedia.android.muslimpro/statistics/)

**Why this matters**: Muslim Pro's core DAU-driving mechanic is five
scheduled daily-prayer-time notifications tied to real-world clock events —
a stronger natural touchpoint frequency than any single daily reminder.
SanguSantri's roadmap currently only has *weekly*-cadence Pengingat Amaliyah
presets (Tahlil malam Jumat, Istighosah weekly) — this is a legitimate gap
relative to a top competitor's engagement model (§5).

### 4.3 Qur'an Kemenag (`com.quran.kemenag`) — same official source, broader existing scope

The Ministry of Religious Affairs' own general-purpose Quran app: complete
30-juz text, two translation editions (2019 and 2002), tafsir in two formats
(ringkas and tahlili), **audio recitation**, prayer schedules, Hijri
calendar, and a QR-code authenticity verification feature. Category: Books &
Reference; 24.7 MB. [apkpure.com listing, retrieved 2026-08-13] — no
install/rating data was retrievable for this app on any source checked; it
appears to be a smaller/less-marketed government app than NU Online or
Muslim Pro.

**Why this matters**: SanguSantri's `0.0.6` Al-Qur'an Kemenag feature draws
from the exact same official LPMQ source this government app already
publishes for free, with audio and more translations than SanguSantri plans
to offer (SanguSantri explicitly excludes audio and Latin transliteration,
per `docs/product/QURAN_PRD.md`). SanguSantri's Quran feature cannot win on
source exclusivity or feature breadth — its only differentiators are reading
experience (dark-only Mushaf-like design, ADR 0016) and integration with the
rest of SanguSantri's habit loop (Aktivitas streak), not the text itself.

### 4.4 Tasbih/dzikir counter apps — validated category, no dominant single player

Multiple single-purpose Indonesian tasbih/dzikir counter apps exist on Play
Store (Tasbih Digital Dzikir Counter, Digital Tasbeeh Counter — Zikar,
Tasbih Digital Offline, Dhikr Counter, Zikirmatik Tasbih Digital, and others),
with user-reported ratings clustering around 4.4–4.8 stars per search-result
snippets (ratings were not independently verified against each app's live
Play Store page for this document — flagged as lower-confidence). Common
features across them: persistent counting across app restarts/reboots,
haptic/vibration feedback, dark mode, custom target lists. [WebSearch result
summary, retrieved 2026-08-13 — no single app was confirmed as a clear
category leader]

**Why this matters**: a standalone digital tasbih is a well-understood,
evergreen, low-moat utility category — SanguSantri's own Tasbih (`0.0.2`) is
already competitive on core mechanics (persistence, haptics, custom
targets). Its differentiation has to come from being *embedded* in the
amaliyah-reading and streak context (the Full Reader repetition shortcut,
FR-018, and Aktivitas tasbih history), not from out-executing single-purpose
counter apps on the counter itself.

---

## 5. Feature/mechanic recommendations, mapped to constraints

Each item states whether it fits an **already-planned milestone** or is
**new/unplanned scope** requiring its own future product decision.

| # | Recommendation                                                                                                                                                               | Fits                                                                                                                                                                                                                                                                            | Constraint check                                                                                                                                                                                                                                                                                                                             |
|---|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | Finish and release **Nahwu Quiz `0.0.5`**                                                                                                                                    | Already planned (ADR 0013), currently the one skipped roadmap item (§2.2)                                                                                                                                                                                                       | Individual/guest/offline-first, no login, no leaderboard through `0.0.5` per ADR 0013 — compatible as scoped. **Highest-priority recommendation**: it is both already-approved and the single feature with zero competitive overlap found in §4 — none of NU Online, Muslim Pro, or Qur'an Kemenag teach Nahwu/Sharaf.                       |
| 2 | Version-bump and **release the already-implemented `0.0.6` Al-Qur'an Kemenag and `0.0.7` Kalender Hijriah** work                                                             | Already planned and already substantially built per `docs/PROGRESS.md` (§2.2) — the remaining work is closing PRD §13's blocking production inputs (font licensing, production credentials, privacy-policy wording) and the release process itself, not new feature engineering | No constraint conflict — this is shipping already-approved scope. Highest ROI-per-effort item: converts sunk engineering cost into DAU without new design/build risk.                                                                                                                                                                        |
| 3 | Extend **Pengingat Amaliyah toward a daily (not only weekly) touchpoint** — e.g. a daily dzikir pagi/petang reminder alongside the existing weekly Tahlil/Istighosah presets | New/unplanned scope extension of `0.0.4`'s existing mechanism (same WorkManager/notification-permission plumbing, no new subsystem)                                                                                                                                             | No conflict: still a local notification, no ads, no new backend, no audio. Directly addresses the gap versus Muslim Pro's five-times-daily azan cadence (§4.2) without needing prayer-time/location APIs or audio, both of which are out of scope.                                                                                           |
| 4 | **Home-screen widget** for Tasbih quick-launch and/or streak status                                                                                                          | New/unplanned scope — not mentioned anywhere in `docs/design/` or `docs/product/` today (confirmed via repo search)                                                                                                                                                             | No constraint conflict — a widget is a standard Android surface, not a new backend or Firebase product. Lower-confidence recommendation: this is based on general mobile-engagement-pattern reasoning, not a specific cited case study found in this research (flagged explicitly, per the instruction not to invent authority for a claim). |
| 5 | Lean further into **Aktivitas streak visibility** on Beranda (the way Duolingo foregrounds its streak count)                                                                 | Enhancement of already-shipped `0.0.3` Aktivitas, no new milestone                                                                                                                                                                                                              | No conflict. Directly evidenced by Duolingo's own investor disclosures (§6.1) that streak-length distribution correlates with retention — the closest primary-source analog to a "streak drives DAU" mechanism this research found.                                                                                                          |
| 6 | **Communal/pesantren-group features** (shared reading targets, pesantren-scoped activity feeds, group challenges)                                                            | **Not currently planned; blocked** — requires Accounts (`0.1.0`) and Pesantren Membership (`0.2.0`), both explicitly out of scope through `0.0.7` (ADR 0013, `docs/product/ROADMAP.md`)                                                                                         | Flagged per the task: social/communal mechanics are a plausible DAU lever in habit apps generally, but must not be confused with monetisation — SanguSantri's non-commercial rule forbids ads/subscriptions, not free social features. Still, this is a multi-milestone-away idea, not a near-term recommendation.                           |
| 7 | Ads, subscriptions, or any monetisation-funded user-acquisition spend                                                                                                        | **Explicitly prohibited**                                                                                                                                                                                                                                                       | `docs/product/ROADMAP.md`: "no advertising, subscription, or monetisation roadmap item, and none should be added without an explicit product decision." Not recommended; noted only to make the boundary explicit.                                                                                                                           |

---

## 6. Realistic DAU targets

**This is an estimate for directional planning, not a commitment or a
forecast with statistical confidence** — it combines a low-confidence TAM
figure (§3) with industry-general stickiness ratios from secondary sources
(below), and should be revisited once SanguSantri has any real install/DAU
telemetry of its own (note: `docs/security/PRIVACY.md`/telemetry rules mean
SanguSantri does not currently collect the kind of usage analytics that
would let it self-measure this without a separate, explicit product/privacy
decision).

### 6.1 DAU-driving mechanics precedent (primary sources)

* **Duolingo's streak mechanic** is the clearest primary-source evidence
  found that a daily-completion streak drives DAU at scale: Duolingo reported
  **50+ million DAU in Q3 2025** (36% YoY DAU growth that quarter) and **23%
  YoY DAU growth in Q2 2026**, per its own investor disclosures. [Duolingo
  Q3 2025 press release — "Duolingo Surpasses 50 Million Daily Active Users,
  Grows DAU 36% and Revenue 41% in Third Quarter 2025 Year over
  Year"](https://investors.duolingo.com/news-releases/news-release-details/duolingo-surpasses-50-million-daily-active-users-grows-dau-36).
  Duolingo has separately disclosed that **~70% of Q3 2022 DAU held a streak
  longer than 7 days**, and 10+ million users held 365-day+ streaks —
  company commentary explicitly frames the streak as "a daily obligation,
  not a daily choice." [Duolingo shareholder letters, via
  investors.duolingo.com static-files, cross-referenced through search
  results] SanguSantri's existing Aktivitas streak (`0.0.3`) is structurally
  the same mechanic, at a vastly smaller scale.
* **Muslim Pro's prayer-time notification cadence** (§4.2) is the clearest
  comparable-category precedent for *notification frequency* as a DAU
  driver, though no DAU figure specific to Muslim Pro could be retrieved
  (Similarweb's DAU data was paywalled) — this is a mechanic-level
  precedent, not a DAU-number precedent.

### 6.2 Stickiness (DAU/MAU) benchmarks — secondary sources, flagged lower-confidence

No single named, freely-accessible benchmark report broke out a
"Religion/Spirituality" Play Store category specifically — this figure is
**unavailable**, not invented. The closest usable proxies, from analytics
vendor/aggregator blogs (not a single authoritative named report, so treated
as lower-confidence, directionally-consistent-across-multiple-sources
evidence rather than one citable number):

* A DAU/MAU ratio of **~20% is commonly cited as a "good" baseline** for a
  consumer app generally; ratios in the 15–25% range are typical for
  **education-category apps**, versus 40–60% for social/messaging and
  15–45% for gaming depending on subgenre. [vmobify — "DAU/MAU Stickiness
  Benchmarks by Category
  (2026)"](https://vmobify.com/blog/dau-mau-stickiness-benchmarks);
  cross-checked against similar ranges summarized from
  [ClevertapMixpanel/PostHog-style engagement-metric explainer content
  surfaced in the same search, retrieved 2026-08-13].
* Sensor Tower's own **State of Mobile 2025** report (a named, credible
  industry publication) provides category-level retention comparisons for
  Health & Fitness apps (e.g., 30-day retention of 31% for CashWalk, 20% for
  Sweatcoin) as illustrative of what "good" 30-day retention looks like in a
  daily-habit-adjacent category, though it does not publish a
  Religion/Spirituality-specific DAU/MAU figure either. [Sensor Tower — State
  of Mobile 2025](https://sensortower.com/state-of-mobile-2025)

SanguSantri's realistic stickiness sits closest to the **education-app
15–25% band**: like a language-learning or grammar-quiz app, its content is
inherently repeatable and streak-able (Tahlil/Istighosah repetition targets,
Tasbih, Nahwu Quiz), but it is not a five-times-a-day utility like a prayer
clock, and it is not a messaging app.

### 6.3 Reasoning chain to a DAU range

1. **TAM**: ~2.5–4.8 million currently enrolled santri nationally (§3,
   low-confidence), plus an unquantified but likely larger pool of pesantren
   alumni and general-public NU-tradition amaliyah practitioners — no source
   found sizes either of the latter two groups, so they are treated as
   directional upside, not counted numerically.
2. **Plausible organic install rate**: SanguSantri has no ad budget
   (non-commercial, §2.3) and no institutional NU/pesantren-network
   distribution deal today (and, per PRD §2.2, must not imply one it does
   not have). Its realistic distribution channel is word-of-mouth within
   pesantren networks, santri social media groups, and NU-aligned community
   sharing. Contrast with NU Online Super App's self-reported ~2 million
   downloads (§4.1) — that app has the NU brand and years of institutional
   promotion, an advantage SanguSantri explicitly does not have. A realistic
   organic install ceiling for an unbranded, single-developer app in this
   window is, by analogy, **one to two orders of magnitude smaller than NU
   Online's figure** — i.e., a low tens of thousands to a few hundred
   thousand installs over a 12–24 month organic-growth horizon, not millions.
3. **Stickiness**: applying the education-app-comparable 15–25% DAU/MAU band
   (§6.2) to a **monthly active** base assumed to be roughly half of
   cumulative installs (a standard rough heuristic for apps without paid
   reactivation, not a cited figure — flagged as an assumption).

| Scenario                                                                                                                                                                                 | Cumulative installs (12–24 mo, organic only) | Assumed MAU     | Stickiness | Resulting DAU                                                             |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------|-----------------|------------|---------------------------------------------------------------------------|
| Conservative (single-pesantren-cluster word-of-mouth)                                                                                                                                    | ~50,000                                      | ~25,000         | 20–25%     | **~5,000–6,000**                                                          |
| Base case (broader NU-community organic reach)                                                                                                                                           | ~150,000–200,000                             | ~75,000–100,000 | 18–22%     | **~14,000–22,000**                                                        |
| Optimistic (strong organic virality, no institutional deal)                                                                                                                              | ~400,000                                     | ~200,000        | 15–20%     | **~30,000–40,000**                                                        |
| Upside, not counted in the headline range (an explicit institutional pesantren-network or NU-affiliate distribution partnership, itself a `0.2.0`+ Pesantren-Membership-era possibility) | millions (NU-Online-like)                    | —               | —          | mid-hundred-thousands conceivable, but speculative beyond current roadmap |

**Headline range: ~10,000–40,000 DAU** as a realistic 12–24 month target,
spanning the base-case-to-optimistic scenarios above, assuming: (a) Nahwu
Quiz and the already-built Quran/Hijri Calendar features are actually
released (§5 #1–#2), (b) Aktivitas/streak/reminder mechanics are the primary
retention lever (§5 #3–#5), and (c) growth stays organic, consistent with
the non-commercial constraint. **The single biggest lever this research
found for moving this range upward is distribution — an institutional
pesantren-network or NU-affiliate partnership — not any individual in-app
feature.** That lever sits outside this document's scope (it is a
partnerships/business-development question, not a product-engineering one)
but should be flagged to the product owner as the dominant variable.

---

## 7. Sources

**Primary (repo, this project's own source of truth):**

* `docs/PROGRESS.md` — actual shipped/implemented state, read in full for
  milestone headers and the most recent ~1,000 lines of detail.
* `docs/product/PRD.md` — product scope, target users, constraints, `0.0.1`
  functional requirements.
* `docs/product/ROADMAP.md` — planned milestones `0.0.1`–`0.3.0`, navigation
  model.
* `docs/product/QURAN_PRD.md` — Al-Qur'an Kemenag `0.0.6` scope.
* `docs/decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md` —
  navigation-shell and Nahwu Quiz scheduling constraint.
* `docs/decisions/0014-firebase-hosting-static-content-delivery.md` —
  backend/Firebase-product scope constraint.
* `app/build.gradle.kts` — confirmed actual released `versionName`/`versionCode`.

**Primary (external, official/government/company-disclosed):**

* [GoodStats — 10 Provinsi dengan Pondok Pesantren Terbanyak 2025](https://data.goodstats.id/statistic/10-provinsi-dengan-pondok-pesantren-terbanyak-2025-LlZsK) —
  aggregates Kemenag pesantren-count data; not the raw EMIS portal itself, so treated as secondary
  despite citing a primary government source.
* [Databoks/Katadata — 42,000 Pesantren in Indonesia for 2024/2025, Concentrated in Java](https://databoks.katadata.co.id/en/education/statistics/68e38957916b1/42000-pesantren-in-indonesia-for-20242025-concentrated-in-java) —
  same caveat as above.
* [GoodStats — Jumlah Santri Anjlok 4 Tahun Terakhir](https://data.goodstats.id/statistic/jumlah-santri-anjlok-4-tahun-terakhir-pesantren-hadapi-tantangan-serius-beLNe) —
  EMIS-derived santri counts by semester; flagged for internal volatility.
* [labura.kemenag.go.id — Statistik Pondok Pesantren Tahun Ajaran 2025/2026 Semester Genap](https://labura.kemenag.go.id/statistik-pondok-pesantren-tahun-ajaran-2025-2026-semester-genap/) —
  a Kemenag regional-office subdomain; provided provincial breakdowns but no direct link to the
  underlying national EMIS dataset.
* `satudata.kemenag.go.id` dataset page for santri counts — attempted directly; returned an
  unpopulated template, no usable data. Noted as an attempted-but-failed primary-source lookup, not
  silently omitted.
* [Duolingo Q3 2025 investor press release](https://investors.duolingo.com/news-releases/news-release-details/duolingo-surpasses-50-million-daily-active-users-grows-dau-36) —
  DAU figures and streak-mechanic commentary, company-disclosed.
* [Sensor Tower — State of Mobile 2025](https://sensortower.com/state-of-mobile-2025) — named
  industry report; Health & Fitness retention figures used as an illustrative daily-habit-adjacent
  category comparator.

**Secondary / lower-confidence (explicitly flagged in-text above):**

* apkpure.com listings for NU Online Super App, Muslim Pro, and Qur'an
  Kemenag — third-party store mirrors, used because direct Google Play
  listing fetches were truncated before reaching install/rating data.
* [Similarweb — Muslim Pro app statistics](https://www.similarweb.com/app/google-play/com.bitsmedia.android.muslimpro/statistics/) —
  usage-rank data only; DAU/MAU figures were paywalled.
* [IBTimes](https://www.ibtimes.com/muslim-pro-app-users-information-may-have-been-harvested-us-military-3083342)
  and [Vice/Motherboard](https://www.vice.com/en/article/muslim-apps-location-data-military-xmode/)
  reporting on Muslim Pro's ~150 million download figure — press-reported, not company-confirmed.
* nu.or.id article reporting NU Online Super App's "2 juta orang" download claim — could not be
  fetched directly (403); cited from a WebSearch result snippet only, and is the organization's own
  self-reported figure, not independently verified.
* [vmobify — DAU/MAU Stickiness Benchmarks by Category (2026)](https://vmobify.com/blog/dau-mau-stickiness-benchmarks) —
  analytics-vendor blog aggregation, not a single named authoritative report; used only because no
  better-sourced category-specific figure could be found, and flagged as such in §6.2.
* WebSearch-summarized ratings for generic tasbih/dzikir counter apps (§4.4) — not independently
  verified against each app's live Play Store page.

**Explicitly unavailable** (searched for, not found, not invented):

* A Religion/Spirituality-specific DAU/MAU stickiness benchmark from any
  named industry report.
* Any official count of pesantren alumni or of general-public NU-tradition
  amaliyah practitioners in Indonesia.
* Muslim Pro's or NU Online Super App's actual DAU or MAU figures from any
  source (only aggregate download/install figures and usage-rank positions
  were found).
