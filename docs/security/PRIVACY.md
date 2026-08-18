# Privacy

Applies to any task touching user data, feedback, telemetry, or crash
reporting.

## Current state

No account identity or direct PII is collected in the current scope. DataStore
holds local preferences; Room holds public content plus local devotional
progress, reminders, tasbih/activity history, and other on-device state. None
of that state is uploaded. Keep this guest/local boundary as features land.

At `0.0.6`, Room also holds public Kemenag Quran/tafsir cache plus private
local Quran bookmarks, one last-read position, and reading-session events;
DataStore holds Quran display preferences. No account or analytics sync is
introduced.

Firebase Remote Config is used for application-update policy and the numeric
`quran_stable_version` trigger. Its fetch sends no Quran text, bookmark,
reading position, session, or preference; Firebase may process its installation
identifier and ordinary network/device metadata as part of the SDK request.
Crashlytics is also integrated for redacted technical failures. Neither service
is an analytics channel for devotional behaviour.

## Requirements

Release `0.0.1` does not require identity. The application must not upload:

* Reading history.
* Counter history.
* Exact devotional frequency.
* Unfinished session data.
* Local preferences.

This prohibition includes Quran bookmarks, Quran reading position/session
history, selected font, text size, line height, and translation preference.
Kemenag receives only the requests required to fetch public surah/ayat data
and an explicitly selected ayat's tafsir by remote id, plus ordinary network
metadata such as IP address. SanguSantri sends no account identity, bookmark,
streak, or reading-history payload to Kemenag.

After a complete Quran dataset exists, an ordinary app start checks only the
small Remote Config version. It makes no Kemenag corpus request unless that
target is strictly higher than the applied local version.

There is no public feedback feature or feedback endpoint in the current
roadmap. Content corrections are handled internally; do not introduce an
installation identifier or network outbox under an obsolete FR-012 design.

A public privacy policy is required before Google Play publication
(Blocking Production Input, `docs/product/PRD.md` §13) — not yet drafted.

## Telemetry

Do not record Arabic reading text, counter values, or personal devotional
history in logs or analytics, ever. This includes Kemenag response bodies,
tafsir text, request credentials, Quran bookmarks, and last-read positions.
Telemetry credentials must be
configurable so the debug application builds without production secrets.
Crash reporting is wired; redaction of Arabic reading text and counter values
must be verified before the first release — this matters
specifically at "thousands of users" scale, where default-verbose crash
payloads are a real exposure, not a theoretical one.

## Play Console requirements

The release inventory must include Firebase Remote Config, Firebase
Installations (transitive Remote Config dependency), and Crashlytics even
though Firebase Analytics is not integrated and devotional state is never sent.
A final Data Safety form still does not exist and remains a publication blocker.

Before `0.0.6` publication, the privacy policy and Data Safety assessment must
describe the direct Kemenag network dependency and local Quran state. Quran
content cache and devotional user-state tables must be explicitly reviewed
for Android Auto Backup; personal devotional state must not silently migrate
to another device under a public-content cache label.

## Location (`ACCESS_COARSE_LOCATION`) — Arah Kiblat only

Added 2026-08-17 with the myquran integration (ADR 0018). This is the app's first
and only runtime permission.

* **What it is used for, and nothing else:** computing the qibla bearing once.
  Prayer times do **not** use it — myquran keys schedules by kabupaten/kota, so
  the user picks their city and no location is involved.
* **Coarse, never precise.** A qibla bearing varies by well under a degree across
  a whole city, so precise location would buy nothing.
* **At most one fix, in the foreground, on demand.** The app prefers the platform
  `LocationManager`'s last known fix and only falls back to a single
  `getCurrentLocation` call — with a 10-second timeout, coarse providers only, GPS
  never — when no cached fix exists at all, which is the normal state right after
  the permission is first granted. It never registers a listener, never requests a
  stream of updates, and never reads location in the background.

  *(Corrected 2026-08-18: this bullet previously read "never starts a location
  update", which stopped being true when `DeviceLocationSource.requestSingleFix`
  was added to fix city detection silently failing on a freshly granted permission.
  The behaviour is deliberate; the documentation had simply not caught up.)*
* **Asked only on demand.** The prompt appears when the reader taps "Aktifkan arah
  kiblat" on the Jadwal Sholat screen — never on launch, never on any other screen.
* **Declared optional.** `uses-feature android:name="android.hardware.location"
  android:required="false"`. Denying it leaves every other feature working; the
  compass simply draws no needle rather than pointing somewhere arbitrary.
* **What leaves the device:** one `GET /qibla/{lat},{lon}` to api.myquran.com
  carrying the coordinates **truncated to two decimal places** (~1.1 km) by
  `coarseCoordinate` in `data/repository/KiblatRepositoryImpl.kt`. A coarse
  permission bounds the accuracy of the fix, not the number of digits the app
  transmits, so the truncation is what actually keeps the shared position
  city-level. Nothing else leaves — no identifier, no devotional state, no reading
  position.
* **What is stored:** the resulting bearing (a single float) in DataStore, so the
  compass works offline afterwards. The coordinates themselves are not persisted.

Before publication the Data Safety form must declare approximate location as
collected-and-shared-with-a-third-party for this one purpose, alongside the
existing Kemenag disclosure, and the privacy policy must name api.myquran.com as
the prayer-schedule and qibla source.
