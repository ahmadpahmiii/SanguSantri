# Privacy

Applies to any task touching user data, feedback, telemetry, or crash
reporting.

## Current state

No PII is collected anywhere in the current scope: no accounts exist,
DataStore holds only reader preferences, Room holds only public content
tables. This is genuinely privacy-clean today — keep it that way as
features land.

## Requirements

Release `0.0.1` does not require identity. The application must not upload:

* Reading history.
* Counter history.
* Exact devotional frequency.
* Unfinished session data.
* Local preferences.

Feedback (FR-012) uploads only the minimum technical and content context
needed to investigate a report: anonymous installation identifier, app
version, amaliyah/variant/content-version/step IDs, feedback category, user
description, device locale, timestamp. It must never include the user's
devotional history or counter history — hold the implementation to this
requirement exactly when `feature/feedback` is built.

A public privacy policy is required before Google Play publication
(Blocking Production Input, `docs/product/PRD.md` §13) — not yet drafted.

## Telemetry

Do not record Arabic reading text, counter values, or personal devotional
history in logs or analytics, ever. Telemetry credentials must be
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
