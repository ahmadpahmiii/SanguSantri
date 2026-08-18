# Security and architecture review — 2026-08-18

Scope: whole-repository review at `master` (the commit titled "quran audio", 2026-08-17
— hashes below the history rewrite of 2026-08-18 no longer resolve), weighted toward security
Scope: whole-repository review at `master` (`ba42f62`), weighted toward security
and content security as requested, plus the pull-request CI that did not exist
before this pass. Reviewed the manifest and component export surface, all four
OkHttp stacks, the Kemenag credential boundary (ADR 0016), the content
import/sync pipeline (ADR 0015), murottal storage, reminders/`PendingIntent`,
location handling, and the Room/DAO access boundary.

**Overall:** the security architecture is better than most Android codebases of
this size. Four separate OkHttp clients so a credential cannot reach a
non-Kemenag origin, HTTPS pinned by a release-only network security config, every
`PendingIntent` immutable, no exported components beyond the launcher activity,
no `WebView`, no raw SQL anywhere, backup/device-transfer excluded for all
devotional state, and a native credential path gated on the release signing
certificate. The findings below are gaps in an otherwise deliberate design, not
symptoms of a careless one.

Nothing in this review touched religious content. No Arabic text, translation,
attribution, or approval status was added, altered, or removed.

---

## Findings

Severity is about impact on this app's users and content integrity, not CVSS.

| #  | Severity     | Finding                                                          | Status                                 |
|----|--------------|------------------------------------------------------------------|----------------------------------------|
| 1  | **Critical** | Release keystore committed to git history                        | Untracked; **rotation still required** |
| 2  | **High**     | Catalog `contentUrl` could redirect content import to any origin | Fixed                                  |
| 3  | Medium       | Catalog `imageUrl` unvalidated, handed straight to Coil          | Fixed                                  |
| 4  | Medium       | Content OkHttp client followed redirects off-origin              | Fixed                                  |
| 5  | Medium       | Full-precision coordinates sent to a third party                 | Fixed                                  |
| 6  | Medium       | Murottal and prayer times break on non-ASCII-digit locales       | Fixed                                  |
| 7  | Low          | `PRIVACY.md` contradicted the shipped location behaviour         | Fixed                                  |
| 8  | Low          | Unit test suite red on `master` (one test hung indefinitely)     | Fixed                                  |
| 9  | Low          | `detekt` red on `master`                                         | Fixed                                  |
| 10 | Low          | `ktlintCheck` red on `master` (~3,300 violations)                | **Not fixed — needs a decision**       |
| 11 | Info         | ViewModels depend on concrete `data/audio` classes               | Accepted, documented                   |

---

### 1. Release keystore committed to git — Critical

`sangusantri.jks` was tracked, added 2026-07-26 by the commit titled "jks", and
`.gitignore` carried no rule for signing material. **The repository is public**, so
for roughly three weeks the release signing key was downloadable by anyone. Anyone
who cloned it in that window still has it.
`sangusantri.jks` is tracked, added in commit `1d232ed` ("jks"), and `.gitignore`
carried no rule for signing material. Anyone who has ever cloned this repository
— or who obtains it later, since it is in history — has the release signing key.

This is worse here than in a typical app because ADR 0016's entire Kemenag
credential protection is built on the release signing certificate: the native
library reconstructs the credential only after matching
`kExpectedSigningSha256`. Whoever holds this key can sign an APK that satisfies
that check and extract the credential, and can ship a build users' devices will
accept as an update to SanguSantri.

**Done here:** added `*.jks`, `*.keystore`, `keystore.properties` to
`.gitignore`, and `git rm --cached sangusantri.jks` (the file is untouched on
disk). A CI job now fails any pull request that tracks key material.

**History purge — done 2026-08-18.** `git filter-repo --invert-paths --path
sangusantri.jks --force` over all refs. Verified afterwards: the blob is absent
from every reachable object, no commit on any branch carries a `.jks`/`.keystore`
path, and the pre-rewrite commits no longer resolve locally. 60 commits remain of
61 — the commit titled "jks" added nothing else, so removing the file left it
empty and it was pruned. Nine local Codex checkpoint refs that pinned the blob
were deleted first. Both `master` and `release/0.0.4` were rewritten; both carried
it. Full pre-rewrite state is recoverable from
`~/Documents/project/SanguSantri-BACKUP-2026-08-18/full-history.bundle`.

A rewrite changes every commit hash from 2026-07-26 onward, so any hash cited in
these documents before that date is dead; references were converted to dates.

**Still required, and only the product owner can do it:**

1. **Rotate the upload key.** Generate a new one and complete Google Play's
   upload-key reset. Play App Signing means the *app signing* key is held by
   Google and was never in this repository — that is the genuine mitigation and
   the reason this is recoverable. The upload key still must be replaced.
   *The purge does not substitute for this.* The key was public for roughly three
   weeks; anyone who took a copy still has it, and no amount of history rewriting
   reaches them.
2. Ask GitHub Support to purge the cached/dangling objects. Force-pushing makes
   the old commits unreachable, but GitHub keeps them fetchable by direct SHA
   until it garbage-collects, and only Support can force that. The repository has
   **0 forks**, which is what makes this worth doing at all — a single fork would
   have retained the objects permanently and outside anyone's control.
3. `SANGU_QURAN_RELEASE_SHA256` must be updated once the signing certificate
   changes, or release builds will fail closed at the credential check. The
   Kemenag credential itself was **never committed** — verified across all 61
   original commits — so rotating it is prudent hygiene rather than a response to
   a known leak.

### 2. Catalog `contentUrl` was an unbounded fetch instruction — High

`ContentApiService.getContent` takes the catalog's `contentUrl` as a Retrofit
`@Url`. Retrofit resolves an **absolute** `@Url` by replacing the configured base
URL outright. Nothing validated the field, so a catalog entry reading
`https://elsewhere.example/tahlil.json` would have had the app fetch, validate as
structurally fine, and import amaliyah text from an origin nobody vetted — under
the sourced attribution the catalog claims. The same string is also handed to
`AssetManager.open` by `BundledContentBootstrapper` after a `removePrefix`, where
`..` segments were equally unconstrained.

This is a content-integrity finding first and a network finding second. It is the
one path in the app by which text a reader treats as authoritative could be
replaced wholesale.

Fixed in `ContentValidator.isOriginRelativeContentPath`: `contentUrl` must be an
origin-relative path under `/content/`, with no `//` (which would make it
protocol-relative and equally off-origin), no `..` segment, no backslash, no
whitespace. Placed in the shared validator rather than at either call site
because both the remote sync and the bundled bootstrap run `validateCatalog`
before any read — one guard covers both pipelines and any future third one.

Both production catalogs already use exactly this shape, so nothing real is
rejected; `catalogProductionContentUrlShapeIsAccepted` locks that in.

### 3. Catalog `imageUrl` unvalidated — Medium

Same catalog, same trust boundary: `imageUrl` goes straight to Coil. A tampered
catalog could point every card at a third-party host and turn the app into an IP
beacon, or supply a `file://`/`content://` URI. Now required to be `https://` or
absent. Cleartext was already blocked by the network security config; this closes
the rest.

### 4. Content OkHttp client followed redirects — Medium

The Quran, myquran, and murottal clients all set `followRedirects(false)`. The
content client — the one that fetches religious text — did not. That would let
the host hand back a redirect to another origin and undo finding 2's pin. Now
consistent with the other three.

### 5. Full-precision coordinates sent to myquran — Medium (privacy)

`KiblatRepositoryImpl` built `"${location.latitude},${location.longitude}"` and
sent it in a URL path to `api.myquran.com`. `ACCESS_COARSE_LOCATION` bounds how
accurate the platform's *fix* is; it does nothing about how many digits the app
transmits, and `Location.latitude` returns full `Double` precision regardless.
This is the most identifying thing this app sends anywhere, and it travelled in a
path segment, which is the most loggable part of a request.

Now truncated to two decimal places (~1.1 km) by `coarseCoordinate`, formatted
with `Locale.US` so a comma-decimal device cannot corrupt the `lat,lon` segment.
Zero functional cost — the code's own comment already notes the bearing varies by
well under a degree across a whole city.

### 6. Locale-dependent formatting in machine-readable strings — Medium

Two instances of the same defect, found while writing tests for finding 5.

`QuranAudioSource.positionalKey` used `"%03d%03d".format(...)`, which renders
through `Locale.getDefault()`'s numbering system. Persian and several Arabic
locales default to Arabic-Indic numerals — a realistic setting for this app's
readers. On such a device every murottal CDN URL becomes `٠٨٩٠٠٤.mp3`, every
request 404s, and no stored file name ever matches `parseFileName`. Murottal
would appear simply broken, with nothing in the logs to explain it.

`PrayerScheduleRepositoryImpl`'s three `DateTimeFormatter.ofPattern(...)` calls
bound the default locale's `DecimalStyle` the same way. Those formatters produce
Room primary keys and myquran path segments, and `PUBLISHED_TIME` *parses*
myquran's ASCII `HH:mm` responses — a parse that fails is swallowed
(`runCatching{}.getOrNull()`), so the prayer schedule would silently come back
empty.

Both pinned to `Locale.ROOT` (`ISO_LOCAL_DATE` for the date, which is
locale-independent by construction).

### 7. `PRIVACY.md` contradicted the code — Low

The document stated the app "never starts a location update". It does:
`DeviceLocationSource.requestSingleFix` issues one `getCurrentLocation` call when
no cached fix exists, which is the normal state right after the permission is
first granted — added deliberately, per its own comment, to fix city detection
silently failing.

The behaviour is right; the documentation was stale. This matters beyond tidiness
because `PRIVACY.md` is the stated input to the Play Data Safety declaration, and
that declaration has to describe what the app actually does. Corrected, with the
correction dated and explained, and the coordinate truncation from finding 5
documented alongside it.

### 8. Unit test suite red on `master` — Low

Three `SerambiViewModelTest` cases failed before any change in this pass, each
after burning the full 60-second `runTest` timeout — roughly three of the suite's
five minutes spent waiting to fail. Three independent problems were stacked in
those tests, and each was hidden by the one in front of it:

1. They collected a `StateFlow` with `toList` into a `launch`ed job and cancelled
   it. Cancelling the last collector makes `SharingStarted.WhileSubscribed` start
   its stop-timeout `delay` inside `viewModelScope`, which shares `runTest`'s
   scheduler but is not a child of the test coroutine — so `runTest` waits out its
   limit for work it cannot advance. Collecting in `backgroundScope`, which
   `runTest` tears down without awaiting, is the fix.

2. With that resolved, `advanceUntilIdle()` stopped returning **at all** — no
   timeout, just a pegged CPU core, because `uiState` combines an endless
   one-tick-a-minute clock flow and there is always another tick to advance to.
   Worth flagging beyond this file: `advanceUntilIdle()` can never be used on any
   flow in this codebase that combines that clock. `runCurrent()` is correct here —
   every emission these tests assert on is scheduled at virtual time zero.

3. Underneath both, two of the three asserted whole-object equality against
   `SerambiUiState.Loaded(...)` built with default field values. `Loaded` gained a
   `now: LocalTime` fed by that same clock when the Beranda prayer block landed, so
   those assertions could not have passed since. Now field-wise, matching the third
   test in the file, which was already written that way.

None of this is a production bug. A CI gate on an already-red suite is worthless,
so it had to be cleared before the gate was worth adding.

### 9. `detekt` red on `master` — Low

`QuranTranslationAyatItem` in `feature/quran/reader/QuranTranslationAyatList.kt`
tripped `LongMethod` at exactly 60/60 lines — untouched by this pass, and failing
before it.

Fixed by lifting the composable's `Modifier` chain into
`Modifier.ayatItemSurface`, which had grown longer than the content it wrapped.
Behaviour is identical; the `selected || isPlaying` condition becomes a single
`highlighted` parameter since both paint the same tint. The extension is
`@Composable` because `QuranPrimaryContainer` is a theme-aware composable getter
and must be read in composition.

### 10. `ktlintCheck` red on `master` — Low, needs a decision

Pristine `HEAD` fails `./gradlew ktlintCheck` with ~3,358 `standard:indent`
violations against the ktlint engine the project already pins (1.5.0 — this is
not version drift). The source tree contains two incompatible constructor
indentation styles; `ContentImporter` indents its body 8 spaces under
`@Inject constructor`, `QuranAudioStore` indents 4. ktlint wants the first
everywhere.

Not fixed here. `./gradlew ktlintFormat` resolves it in one command but rewrites
~60 files and 3,300 lines, which would have buried this pass's security changes
and is a formatting decision for the product owner rather than a review finding.

Consequence for CI: the `ktlint` step is `continue-on-error: true` with a comment
saying exactly why. Run the reformat as its own commit, then flip that one line to
`false`. `detekt`, `lintDebug`, `assembleDebug` and the unit tests are all
blocking from the start.

### 10. ViewModels depend on concrete `data/audio` classes — Info, accepted

`QuranHubViewModel`, `QuranReaderViewModel` and `QuranSettingsViewModel` inject
`QuranAudioStore`, `QuranAudioDownloadManager` and `QuranMurottalPlayer`
directly, which are `data/` classes rather than `domain/repository` interfaces.
Three other ViewModels do the same with `NahwuQuizBootstrapper` and
`QuranConnectivityChecker`.

The hard rule in `CLAUDE.md` — no DAO access from ViewModels — is not violated;
verified by grep, nothing outside `data/` imports a DAO. Adding a
`QuranAudioRepository` interface with exactly one implementation to satisfy the
softer boundary would be the abstraction `CODING_STANDARD.md` explicitly
prohibits. Left as is, recorded here so it reads as a decision rather than an
oversight.

---

## Changes made

**Security fixes**

- `data/content/ContentValidator.kt` — `contentUrl` origin pinning and `imageUrl`
  scheme restriction (findings 2, 3)
- `di/NetworkModule.kt` — `followRedirects(false)` on the content client (4)
- `data/repository/KiblatRepositoryImpl.kt` — `coarseCoordinate` truncation (5)
- `data/audio/QuranAudioSource.kt` — `Locale.ROOT` positional key (6)
- `data/repository/PrayerScheduleRepositoryImpl.kt` — `Locale.ROOT` formatters (6)
- `.gitignore` — signing material (1)
- `sangusantri.jks` — untracked (1)
- `feature/quran/reader/QuranTranslationAyatList.kt` — `Modifier` chain extracted
  so `detekt` passes (9); no behaviour change

**Tests** — 47 new cases, all JVM unit tests, no new test infrastructure:

- `ContentValidatorTest` (+14) — every rejected `contentUrl`/`imageUrl` shape,
  plus the production shape as a regression guard
- `ResponseSizeLimitInterceptorTest` (new, 4) — the response-size cap had no
  coverage at all despite being installed on all four clients; covers the
  `Content-Length` pre-check, the exact boundary, and the chunked case where
  `Content-Length` is absent and only the streaming guard applies
- `CoarseCoordinateTest` (new, 4) — truncation, signs, and locale independence
- `QuranAudioSourceTest` (new, 10) — URL/file-name derivation, round-tripping,
  locale independence, and every stray-file shape `parseFileName` must reject so
  a `.part` file is never counted as playable offline
- `ReminderScheduleCalculatorTest` (new, 10) — weekly and Hijri next-occurrence
  math against a pinned `now`, including the strict "not now" boundary
- `CustomTargetValidationTest` (new, 9) — the custom Tasbih target guard,
  including that Arabic-Indic digits are accepted (they are, via
  `Character.digit`, and that is the right behaviour for this app)
- `SerambiViewModelTest` — three pre-existing failures repaired (finding 8);
  suite wall time drops from ~5 min to ~35 s, since the three 60-second timeouts
  are gone

`app/build.gradle.kts` gains `testImplementation(libs.okhttp.mockwebserver)`;
the artifact was already in the version catalog for instrumented tests.

**CI** — `.github/workflows/pull-request.yml`, five jobs on every PR to `master`:

| Job             | Runs                                                   | Blocking                 |
|-----------------|--------------------------------------------------------|--------------------------|
| Secret scan     | tracked key material, literal Kemenag credentials      | yes                      |
| Content catalog | `tools/ci/validate_content.py` over both content trees | yes                      |
| Static analysis | `detekt`, `ktlintCheck`                                | detekt only (finding 10) |
| Unit tests      | `testDebugUnitTest`                                    | yes                      |
| Lint and build  | `lintDebug`, `assembleDebug`                           | yes                      |

Debug-only by design: a release assembly needs the ADR 0016 secrets, which
GitHub does not expose to pull requests from forks, and the build is already
written to compile without them. `permissions: contents: read` at workflow level.

`tools/ci/validate_content.py` mirrors `ContentValidator` and `ContentImporter`
in Python and additionally checks that every `contentUrl` resolves to a file that
exists, that each file's `id`/`version` match the catalog entry naming it, that
attribution fields are present, and that no hosted version is older than its
bundled counterpart — which would import once on a fresh install and then be
skipped by every subsequent sync.

---

## Not addressed

- **Certificate pinning** for `quran-api.lpmqkemenag.id` and `api.myquran.com`.
  Worth considering given the credential, but pinning a third party's
  certificate means an outage whenever they rotate, with no server-side control
  and a store release as the only fix. A product decision, not a review fix.
- **Play Integrity / attestation** on the Kemenag credential path. The signing
  certificate check is the current control and is reasonable for a non-commercial
  app; strengthening it only matters after finding 1's rotation is complete.
- `app/google-services.json` is tracked. That is normal for Firebase Android and
  its API key is a client identifier rather than a secret, but confirm the key is
  restricted to this package name and SHA-1 in the Google Cloud console.
- **Instrumented tests were not run** — no emulator in this session. The
  workflow does not run them either; `connectedDebugAndroidTest` needs an
  emulator runner, which is a separate cost/latency decision.
- **R8 was not exercised.** There is no `proguard-rules.pro`; release relies
  entirely on consumer rules shipped by kotlinx.serialization, Retrofit, OkHttp,
  Room and Hilt, which is usually correct but is not verified by anything. No
  `assembleRelease` ran in this session (it needs the ADR 0016 secrets), and the
  pull-request workflow cannot run one either, since GitHub withholds secrets
  from fork pull requests. A separate `push`-triggered or manually dispatched
  release workflow — where secrets *are* available — is the right place for that,
  and would catch a shrinking regression before a store upload rather than after.
