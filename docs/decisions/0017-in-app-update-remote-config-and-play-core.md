# 0017: In-app update gate — Firebase Remote Config policy + Play Core update flow

## Status

Accepted (2026-08-09). Amends ADR
[0014](0014-firebase-hosting-static-content-delivery.md): that ADR
"explicitly rejected" any new Firebase backend product beyond Hosting
(Crashlytics was already a separately-scoped exception). This decision adds
a second, narrowly-scoped exception — **Firebase Remote Config** — for one
purpose only: serving the in-app update policy described below. Firestore,
Cloud Functions, Realtime Database, and every other Firebase backend
product remain rejected exactly as ADR 0014 states.

## Context

The product owner wants the app to be able to force users below a minimum
version to update, and to nudge (without blocking) users on an older but
still-acceptable version. Google Play's In-App Updates API (Play Core)
implements the actual update UI and download/install flow, but it has no
concept of "this specific version must update" — it only knows whether
Play has a newer version staged for the device. The missing piece is a
policy source: something this app can read at runtime to decide, given the
installed `versionCode`, whether the available Play update should be
forced or merely offered.

The product owner had already configured a Firebase Remote Config
parameter for this in the Firebase console — key `in_app_update`, JSON
value:

```json
{
    "minimum_version_code": 4,
    "force_update_versions": [
        1,
        2
    ]
}
```

Two options were considered for the policy source: keep it in Remote
Config (already configured, changeable without a Play release), or publish
it as a static JSON file on the existing Firebase Hosting content
distribution (ADR 0014, no new Firebase product needed). Firebase Hosting
was rejected here — see Alternatives rejected — so this ADR records the
resulting, deliberate exception to ADR 0014.

## Decision

**Firebase Remote Config supplies the policy; Google Play In-App Updates
(Play Core) supplies the update mechanism.** Neither replaces the other.

* The Remote Config parameter `in_app_update` is read as-is (key kept
  exactly as already configured in the console; no rename). Its JSON is
  decoded into `AppUpdatePolicyDto`
  (`data/remote/update/AppUpdatePolicyDto.kt`) via the app's existing
  `kotlinx.serialization` `Json` instance, then mapped to the domain model
  `AppUpdatePolicy` (`domain/model/AppUpdatePolicy.kt`).
* `AppUpdatePolicyRepositoryImpl` (`data/repository/`) wraps
  `FirebaseRemoteConfig.fetchAndActivate()`, bridged to a suspend function
  with a 5-second timeout. Any fetch or parse failure returns `null` —
  never throws — after recording the failure to Crashlytics (never
  silently).
* `decideAppUpdateRequirement` (`domain/model/AppUpdateRequirement.kt`) is
  a pure function, mirroring the existing `ContentVersionAction` shape (no
  DI, no side effects): `installedVersionCode < minimumVersionCode` **or**
  `installedVersionCode in forceUpdateVersionCodes` → `FORCE`; otherwise →
  `FLEXIBLE`.
* `AppUpdateViewModel` (`feature/update/`) combines that decision with
  Play Core's own `AppUpdateInfo` (via `AppUpdateManager.
  requestAppUpdateInfo()`) to decide the actual UI state: a policy
  decision of `FORCE` only becomes `RequireForceUpdate` if Play Core also
  reports `AppUpdateType.IMMEDIATE` as allowed; otherwise it falls back to
  offering a flexible update, or to doing nothing, and the mismatch is
  recorded to Crashlytics as a non-fatal (fail-open — a Remote Config
  outage or an update-availability mismatch must never trap the user on a
  dead-end screen).
* The check runs once per cold start (not once-ever), triggered from
  Beranda (`AppUpdateGate`, mounted from `SerambiRoute`) — the product
  owner's explicit choice over a once-ever check, so that a user who
  ignores a flexible update, or who is later added to `force_update_
  versions`, is re-evaluated on their next app open rather than never
  again.
* A forced update is **not cancelable**: `AppUpdateForceDialog` disables
  back-press and outside-tap dismissal, offers only an "update now" action,
  and if the user cancels Play's own immediate-update UI, the check is
  re-run immediately, which re-invokes the same flow.
* A flexible update is dismissible; once downloaded, a snackbar with a
  restart action completes the install (`AppUpdateManager.
  requestCompleteUpdate()`).

## Alternatives rejected

* **Publish the policy as a static JSON file on Firebase Hosting (ADR
  0014's existing exception), avoiding a new Firebase product** —
  rejected for this feature specifically. The product owner had already
  configured Remote Config with the exact key/value needed, and Remote
  Config's own console UI (real-time param editing, no deploy step) is a
  materially better fit for an update-policy toggle a product owner may
  need to change urgently (e.g. discovering a bad release and needing to
  force-update everyone off it) than editing and deploying a file through
  `content-hosting/`. Reusing Hosting would have meant discarding
  already-working console configuration for no behavioural benefit.
* **A remote config value embedded in the existing content manifest/sync
  response instead of a separate mechanism** — rejected; update policy is
  unrelated to amaliyah content versioning (ADR 0012/0015) and coupling
  the two would make an update-policy change require a content sync cycle,
  defeating the point of an independently, instantly updatable switch.
* **Building custom force-update detection without Play Core** (e.g. an
  app-side "minimum version" check that just shows a Play Store deep link)
  — rejected; Play Core's In-App Updates API is Google's supported
  mechanism for in-app download/install without leaving the app, and
  reimplementing that flow manually would be strictly worse UX for no
  benefit.
* **Looping `startUpdateFlowForResult` from inside the launcher's own
  result callback** — rejected as an implementation detail: Kotlin does
  not allow a local `val` launcher to be referenced inside its own
  initializer lambda. Re-running `checkForUpdate()` on cancellation
  instead re-derives a new `AppUpdateInfo`/UI-state instance, which a
  separate `LaunchedEffect(uiState)` picks up to re-invoke the flow —
  same non-cancelable behaviour, no self-reference needed.

## Consequences

* ADR 0014's "no new Firebase product beyond Hosting" statement now has
  two explicit, narrowly-scoped exceptions: Crashlytics (crash reporting)
  and Remote Config (in-app update policy only). Any future Firebase
  product addition still needs its own ADR — this is not a general
  reopening of ADR 0014.
* `firebase-config:23.1.0` and `play-app-update`/`play-app-update-ktx:
  2.1.0` are new `app/build.gradle.kts` dependencies.
* The `in_app_update` Remote Config parameter is now a runtime dependency
  of every cold start; if it is ever deleted from the console, the app
  fails open (no update prompt at all), not a crash — see fail-open
  behaviour above.
* Debug builds set `minimumFetchIntervalInSeconds = 0` so console changes
  are visible immediately during development; release builds use Remote
  Config's throttled fetch interval, so a policy change in the console may
  take up to that interval to reach already-installed release builds — an
  accepted operational tradeoff, not a bug.
* This is additive: no existing Firebase Crashlytics, sync, or content
  code changes as part of this ADR.
