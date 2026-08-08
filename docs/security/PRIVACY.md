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
When crash reporting is wired (not yet), redaction of Arabic reading text
and counter values must be verified before the first release — this matters
specifically at "thousands of users" scale, where default-verbose crash
payloads are a real exposure, not a theoretical one.

## Play Console requirements (not yet needed)

No Data Safety form or third-party SDK inventory exists — none is needed
yet (zero third-party SDKs beyond AndroidX/Hilt/Room, no analytics wired).
This becomes a real Play Console submission-time task once any SDK with
data collection is added — re-visit this document at that point rather than
building the inventory speculatively now.

Before `0.0.6` publication, the privacy policy and Data Safety assessment must
describe the direct Kemenag network dependency and local Quran state. Quran
content cache and devotional user-state tables must be explicitly reviewed
for Android Auto Backup; personal devotional state must not silently migrate
to another device under a public-content cache label.
