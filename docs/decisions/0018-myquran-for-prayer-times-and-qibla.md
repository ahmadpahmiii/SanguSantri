# 0018: myquran for prayer times and qibla — and not for Quran or the hijri calendar

* Status: Accepted
* Date: 2026-08-17
* Supersedes: nothing
* Amends: ADR 0016 (adds a second, unrelated third-party API without touching the
  Quran-content rule)

## Context

The Beranda/Al-Qur'an revamp shipped a Jadwal Sholat + Kiblat screen with no data
source behind it. Until now it rendered a clearly-marked sample schedule in debug
builds and nothing at all in release, which was honest but not a feature.

[api.myquran.com v3](https://api.myquran.com/v3/doc) publishes prayer schedules,
qibla bearings, a hijri calendar, Quran text with per-ayah and per-surah audio,
inline tafsir from three works, and hadith. It needs no key, rate-limits at 120
requests/minute per IP, and serves `access-control-allow-origin: *`.

All four endpoint families were tested live before this decision, not read from
the spec alone.

## Decision

**1. Prayer schedules come from myquran.** `GET /sholat/kabkota/semua` for the
~517-entry city list and `GET /sholat/jadwal/{id}/{yyyy-MM}` for a whole month at a
time.

**2. The schedule is keyed by kabupaten/kota, not coordinates.** The API works
this way, so the user picks their city once and prayer times need **no location
permission at all**.

**3. A month is cached per fetch.** myquran returns all 31 days in one call, so
Room holds the month and the schedule keeps working offline for the rest of it.
Room stays the source of truth; the UI never renders from a DTO.

**4. Times are stored exactly as published.** `HH:mm` strings go into Room
unparsed; parsing happens once at the domain boundary and a value that will not
parse is dropped rather than guessed. A day missing any of its six entries is not
served as a schedule at all — a wrong prayer time is worse than a missing one.

**5. Qibla uses `GET /qibla/{lat},{lon}` with coarse location.** The kabkota
lookup returns no coordinates, so the bearing cannot reuse the city choice. The
app requests `ACCESS_COARSE_LOCATION` — only when the reader asks for the bearing,
never on open — reads the platform `LocationManager`'s last known fix, computes
once, and caches. Coarse is sufficient: a qibla bearing varies by well under a
degree across a city. The app never requests a precise fix and never starts an
active location request, so it neither spins up GPS nor adds Play Services.

**6. myquran gets its own unauthenticated HTTP client.** Three clients now exist
and stay separate: Firebase Hosting content (ADR 0014), credentialed Kemenag
Quran (ADR 0016), and this one. ADR 0016 §5 forbids sending Kemenag headers to any
non-Kemenag origin; separate clients make that structural rather than a
convention.

**7. myquran is NOT used for the Quran.** Kemenag remains the only Quran-content
API (ADR 0016 §2 stands unchanged). Its audio and extra tafsir were evaluated and
deliberately deferred — see "Considered and deferred".

**8. myquran is NOT used for the hijri calendar.** The app already computes hijri
dates offline with `java.time.chrono.HijrahDate`. Measurement showed myquran's
calendar does not match Kemenag's published dates either, so switching would add a
network dependency without fixing anything — see below, where the finding is that
*every* available computation disagrees with Kemenag on the dates that matter.

## Considered and deferred

**Quran audio.** myquran's per-ayah URL is a pure function of `(surah, ayah)` —
`cdn.myquran.com/audio/ayah/{surah:000}{ayah:000}.mp3`, verified across six ayahs
spanning the mushaf — and per-surah is `/audio/surah/{n}.mp3`. Adopting audio
would therefore need **no schema change, no DTO, no sync change and no API call**:
a URL builder plus a player. The work is the player, which the project has no
dependency for today (no Media3/ExoPlayer). Deferred by the product owner pending
a decision on whether audio joins the roadmap at all — `ROADMAP.md` currently
states it does not, which is also why the revamp's "Murottal" chip was left
unbuilt.

**Extra tafsir (quraish, jalalayn).** Would require calling myquran's Quran
endpoints — a second Quran-content API, which ADR 0016 §2 forbids — plus a schema
bump and per-work attribution in the tafsir sheet. Deferred.

**Full Quran switch.** Rejected on evidence: myquran's surah `name` field returns
the Latin name for all 114 surahs despite the spec's example showing Arabic, so
the reader's tenang header would lose its Arabic surah name; and myquran carries
no equivalent of Kemenag's `no_foot`/`teks_foot` footnotes, which the reader
renders today. It also re-publishes rather than being the official source.

**Hijri calendar — investigated further (2026-08-17), and the answer changed.**

The first comparison only checked whether the methods agreed with each other. Checking them against
what Indonesia actually observes tells a different story. Reverse-lookup of the dates that matter:

| Event                      | Kemenag / NU (published) | Muhammadiyah | myquran `standar` | umalqura (= this app) |
|----------------------------|--------------------------|--------------|-------------------|-----------------------|
| 1 Ramadan 1447             | **19 Feb 2026**          | 18 Feb 2026  | 18 Feb 2026       | 18 Feb 2026           |
| 1 Syawal 1447 (Idul Fitri) | **21 Mar 2026**          | —            | 20 Mar 2026       | 20 Mar 2026           |

**Every computational method available to us — including all three of myquran's — lands a day
before Kemenag on both dates, i.e. on Muhammadiyah's reckoning.** For an NU/pesantren audience that
is precisely the wrong side of the difference.

The reason is structural, not a bug in any of them: Kemenag does not publish a computed calendar as
final. It runs *sidang isbat* each year, combining hisab under the MABIMS criteria (hilal altitude
≥3°, elongation ≥6.4°) with *rukyatul hilal*. Muhammadiyah uses *wujudul hilal*, which needs only
that the moon has set after the sun — a lower bar, so its dates are often a day earlier. No
algorithm shipped in an app reproduces an isbat result in advance.

Consequences for this app:

* **myquran's `/cal` is not the fix.** It does not match Kemenag either, so swapping to it would
  add a network dependency and still show the wrong day.
* **The app's own `HijrahDate` is not wrong so much as differently-conventioned** — but the
  convention it follows is not the one this app's readers use for Ramadan and the Ied.
* **The honest short-term position** is to keep the offline computation for ordinary day-to-day
  dates, and never present 1 Ramadan / 1 Syawal / 10 Zulhijah as settled — those await isbat.
* **The real fix** is a small curated table of Kemenag's announced dates per year, shipped through
  the existing Firebase Hosting content pipeline (ADR 0014) rather than any API, used to anchor the
  calendar and hijri-recurring reminders. That is a content problem with a content solution.

**Decision (product owner, 2026-08-17): keep the app's own offline `HijrahDate` computation, and
treat Kalender Hijriah as production-ready on that basis.** No calendar or reminder code changes.

What that accepts, recorded so it is not rediscovered as a bug:

* Ordinary day-to-day hijri dates are correct and work offline, with no network dependency.
* On the year's headline dates the app will show Umm al-Qura, which lands a day before Kemenag/NU
  and alongside Muhammadiyah. Readers who follow the government/NU determination will see 1 Ramadan
  and Idul Fitri a day early in-app.
* `ReminderScheduleFormatter` uses the same computation, so the calendar and hijri-recurring
  reminders stay consistent with each other — which is the property that actually had to hold.

Revisiting later means shipping a curated table of Kemenag's announced dates through the Firebase
Hosting content pipeline (ADR 0014) and using it to anchor both surfaces. That remains the only
approach that can match an isbat result, and it needs no API.

## Consequences

* Room goes to version 6. Under the standing
  `fallbackToDestructiveMigration(dropAllTables = true)` policy this **drops every
  table**, so each user re-downloads the Quran once; bundled amaliyah content
  bootstraps again. Accepted by the product owner when this work was scoped.
* The app gains its first runtime permission (`ACCESS_COARSE_LOCATION`), declared
  optional with `uses-feature ... required="false"`. Everything except the qibla
  bearing works without it. See `docs/security/PRIVACY.md`.
* A third-party dependency now sits in a devotional path. myquran publishes the
  schedules; SanguSantri attributes it in the screen subtitle
  ("KAB. KUDUS · myquran.com") and never adjusts a published time.
* Beranda gains a setup affordance in the prayer block's place when no city is
  chosen — without it the block is hidden and Jadwal Sholat has no entry point.
* The debug-only sample schedule, its `BuildConfig.DEBUG` gate, `isSample`, and
  every "CONTOH" marker are deleted.
