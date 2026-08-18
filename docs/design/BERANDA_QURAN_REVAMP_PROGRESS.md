# Beranda + Al-Qur'an Revamp — Progress

Implementation log for the design handoff bundle
`design_handoff_beranda_quran_revamp/` (README.md + `SanguSantri.dc.html`), which revamps Beranda,
adds Jadwal Sholat + Kiblat, and restyles the Quran hub/readers/settings, Amaliyah readers,
Aktivitas, and Tasbih. Its "Suggested order of work" is the step numbering used below.

Scope note: the handoff supersedes parts of `docs/design/DESIGN_SYSTEM.md` and
`docs/design/QURAN_DESIGN_SYSTEM.md` on colour. It adds exactly one new feature — Jadwal Sholat +
Kiblat (step 4) — whose UI is built but which has no prayer-time source yet; see step 4's
"Needs an API".

## Step 1 — Design tokens and app-wide theme mode (2026-08-16)

**Status:** Complete.

### What changed

**One palette, app-wide.** The handoff's "new app-wide palette" table is byte-identical to the
revised Quran palette, so it is implemented as one set of roles rather than two parallel ones:

- `Color.kt` gains ten app-wide Light/Dark role pairs (background, surface, text, muted text,
  translation text, outline, primary, onPrimary, tint, onTint). Five light values reuse existing
  tokens (`SantriGreen40`, `SantriNeutral10/40/99`, `SantriSurface`); the rest are new.
- Every `Quran*Dark`/`Quran*Light` token is now an alias of its app-wide equivalent. This lands all
  seven changed dark values and all five changed light values from the handoff without touching a
  single Quran screen's imports. Arabic-on-canvas contrast moves ≈17.7:1 → ≈14.8:1, still far above
  AA, with the OLED halation the revamp was called for.
- `Theme.kt` builds both Material 3 schemes from those roles. Because nothing outside `Theme.kt`
  reads a `Santri*` colour directly — every screen renders from `MaterialTheme.colorScheme` — the
  new palette reaches Beranda, Aktivitas, Tasbih, and the Amaliyah readers with no per-screen edits.
- `surfaceContainer*` roles are now set (all collapsed onto surface, matching the design's two
  neutrals and zero elevation). Left unset they fell back to Material 3's baseline lavender, which
  is what the bottom navigation bar had been rendering.

**One theme mode, app-wide.** Previously the persisted mode themed only the Quran destination.

- `QuranThemeMode` → `AppThemeMode`, `LocalQuranThemeMode` → `LocalAppThemeMode` (moved to
  `Theme.kt`), `QuranThemeViewModel` → `AppThemeViewModel` (moved to the app root),
  `QuranThemeToggleButton` → `ThemeToggleButton` (moved to `core/designsystem/component`). The
  DataStore key is unchanged, so no migration and no lost setting.
- `MainActivity` resolves the mode once and applies it to `SanguSantriTheme`, `LocalAppThemeMode`,
  and system-bar icon appearance.
- The persisted value is now nullable: `null` means the user has never chosen, and the app follows
  the system setting until they do (product-owner decision — the previous DARK default was harmless
  while Quran-only, but app-wide it would have flipped every existing install to dark).
- `QuranThemeBoundary` is **deleted** along with its five call sites: with one app-wide theme its
  `SanguSantriTheme` wrap was a no-op and its system-bar handling moved up. `SanguSantriNavHost`
  also loses its own theme-mode read and its Quran-only `containerColor` special case.
- `QuranReaderSettingsRepository.toggleThemeMode()` is **deleted**. `ThemeToggleButton` reads the
  resolved mode and passes its opposite to `setThemeMode`, which is also what turns an unset
  (system-following) value into an explicit choice.
- Tampilan Al-Qur'an's Mode warna control reads `LocalAppThemeMode` instead of a `UiState` field,
  so it shows the effective mode even before the user has chosen one.

### Deliberately deferred

- The nine dark-green **"block" roles** (next-prayer panel) — no consumer until step 3's Beranda
  prayer block. Landing unused colour constants now buys nothing.
- `QuranContinueCardGradientStart*` is kept but is on death row: the handoff makes "Terakhir dibaca"
  a flat block with no gradient. It goes when the hub is restyled.
- Component-level elevation. Material 3 still applies tonal elevation where components ask for it;
  the design says elevation none. That is per-component work in steps 2–6.

### Files

Modified: `core/designsystem/theme/Color.kt`, `Theme.kt`, `QuranColorScheme.kt`, `MainActivity.kt`,
`navigation/SanguSantriNavHost.kt`, `domain/model/QuranReaderSettings.kt`,
`domain/repository/QuranReaderSettingsRepository.kt`,
`data/repository/QuranReaderSettingsRepositoryImpl.kt`, `feature/quran/QuranEntryScreen.kt`,
`feature/quran/hub/QuranHubScreen.kt`/`QuranHubViewModel.kt`/`QuranHubActions.kt`,
`feature/quran/reader/QuranReaderScreen.kt`/`QuranReaderViewModel.kt`/`QuranFlowingPageText.kt`,
`feature/quran/settings/QuranSettingsScreen.kt`/`QuranSettingsViewModel.kt`/
`QuranSettingsUiState.kt`,
`feature/quran/source/QuranSourceScreen.kt`, `res/values/strings.xml` (two content-description keys
renamed off the `quran_` prefix).

Renamed: `domain/model/QuranThemeMode.kt` → `AppThemeMode.kt`,
`feature/quran/QuranThemeViewModel.kt` → `AppThemeViewModel.kt`,
`feature/quran/QuranThemeToggleButton.kt` → `core/designsystem/component/ThemeToggleButton.kt`.

Deleted: `feature/quran/QuranThemeBoundary.kt`.

### Validation

`assembleDebug` ✅ · `compileDebugUnitTestKotlin` ✅ (no test source referenced the renamed API) ·
`lint` ✅ · `installDebug` ✅ + manual verification on a Pixel 9 emulator: Beranda in light and in
system-dark, first launch correctly following the system, the Quran hub's toggle flipping the whole
app (Beranda included) while the system stayed dark, and the choice surviving a cold restart. The
Quran entry screen renders correctly with `QuranThemeBoundary` gone.

`detekt` ❌ and `ktlintCheck` ❌ — **both fail on unmodified, committed code** and did so before this
change; see "Toolchain" below.

### Toolchain problem found (pre-existing, not fixed here)

`./gradlew ktlintFormat` reformats ~50 files this change never touched: it re-indents every
annotated-constructor class body by four spaces, a style the checked-in code does not use, and
`ktlintCheck` then reports violations against ktlint's own output. That churn was reverted, so the
diff contains no drive-by reformatting. `ktlintCheck` currently reports violations in 47 files,
nearly all pristine at `HEAD`.

`detekt` fails with one weighted issue: an unused `detail` parameter in
`feature/quran/QuranEntryScreen.kt:200`, whose only use sits inside a commented-out block. Both the
parameter and the comment are present at `HEAD`, untouched by this change.

Both need a decision — pin/align the ktlint version and config to the checked-in style (or reformat
the repo once, deliberately, in its own commit), and either restore or remove that commented-out
detail block. Until then the handoff's per-phase validation list cannot pass on ktlint/detekt for
reasons unrelated to the work being validated.

### Next

Step 2 — the reader's "tenang" surah header + basmalah, and the mushaf flowing text.

---

## Steps 2–6 — reader, Beranda, Jadwal Sholat, Tampilan, restyles (2026-08-16)

**Status:** Complete, with the deliberate gaps listed under "Not built" below.

### Step 2 — Reader

- **Surah header** (`QuranSurahStartHeader.kt`): the surah name set in the reader's own Arabic face
  at 33sp/1.95, one muted caps line, a 52×1dp hairline, then the basmalah. This is the only
  treatment — the previous three-column metadata band, its `QuranSurahHeaderVariant` enum and the
  "Kepala surah" control in Tampilan were removed on the product owner's decision (2026-08-17).
- **The basmalah is read from Room** — Al-Fatihah ayat 1, the exact stored Kemenag string — not
  hardcoded and not the old SVG. When the dataset is not prepared the header draws no basmalah at
  all rather than substituting anything.
- **Mushaf mode** gets the caps juz/page strip, a 2dp page-progress track with a "593 / 604"
  readout, and single-tap immersion that clears the chrome.
- **Ayat rows** follow design 2c: a 25dp tint circle holding the ayah number, its juz/page origin,
  bookmark and overflow affordances, 17dp padding, hairline separators, and an 8dp tinted
  background when selected.
- Reader defaults moved to the handoff's 27sp / 2.4× (from 24sp / 2.3×).

### Step 3 — Beranda

Rebuilt to §1: greeting row with the app-wide theme toggle and search (no bell — dropped in
review), the next-prayer block, four menu tiles, the continue row with its progress track, and the
curated amaliyah scroller. The top app bar is gone, and with it the Setelan/Tentang entry points —
both were `PlaceholderScreen`s with no content, so their destinations were deleted rather than left
unreachable.

The nine dark-green "block" roles deferred in step 1 landed here (`Color.kt` + `BlockColors.kt`).

### Step 4 — Jadwal Sholat + Kiblat

New `feature/prayertimes`: countdown block ticking every second, six rows with independent
per-prayer reminder toggles that persist, the current row as a filled tint block bled into the
padding, and the kiblat card. **No prayer-time source is wired** — see "Needs an API" below.

### Step 5 — Tampilan Al-Qur'an

Added the "Kepala surah" segmented control (Tenang / Band). The rest of §6 — live preview, mode
warna, font cards, the three sliders, tampilan bacaan, source note — already existed.

### Step 6 — Amaliyah, Aktivitas, Tasbih

- **Tasbih**: 236dp surface-filled counter circle whose 1dp border turns primary at the target, the
  count at 74sp/300, and the four target presets as one segmented container.
- **Guided reader**: the same circle two sizes down (184dp) with the "✓ Tercapai" line.
- **Amaliyah full reader**: steps are plain hairline-separated rows at 22dp padding instead of
  bordered cards, and the top bar gained the mode subtitle, the app-wide theme toggle, and a tint
  "Panduan" pill.
- **Aktivitas**: the streak section is now one surface card with the streak as a 34sp light number
  plus an inline unit and the record opposite it; stat numbers across the screen are 22sp/300.

### Not built (deliberate, with reasons)

- **Murottal.** Handoff §4's reader footer specifies a `play_arrow` "Murottal" chip. Quran audio is
  explicitly off the roadmap (`CLAUDE.md`, `docs/product/ROADMAP.md`). Not built; needs a product
  decision before it can be.
- **The reader's 56dp control bar** (chevrons, translate toggle, format_size). Its centrepiece is
  the murottal chip; the rest duplicates controls the top bar already carries.
- **Hizb in the mushaf strip.** The stored Kemenag data carries juz and page per ayat, no hizb.
  Inventing one is not an option, so the strip reads "JUZ n · HAL n".
- **"DITANDAI" bookmark flag** on the mushaf strip — needs per-page bookmark state the reader does
  not currently hold.
- **Aktivitas' seven day dots** — need per-day completion history the activity overview does not
  carry. The streak number and record are shown; the dots are not approximated from them.
- **Mushaf last-line centring.** `text-align-last: center` has no Compose equivalent without custom
  layout. Justification is applied; the last line falls where it falls.
- **Guided reader's centred, card-free step layout** (§8) — the counter and top bar are done, the
  step itself still renders in its card.
- **Named greeting.** The design greets "Fahmi"; the app has no accounts and no user name, so the
  greeting row shows the app name.

### Needs an API — nothing else in the revamp does

**Jadwal Sholat + Kiblat is the only surface in this revamp with no data source.**

- **Prayer times.** No provider is wired. `PrayerScheduleRepositoryImpl` emits `null` in release
  builds, so Beranda's block and the Jadwal Sholat schedule simply do not render — Beranda's
  standing "a section with no data is not rendered" rule doing the work. In **debug** builds it
  emits the handoff's own review times, flagged `isSample = true`, and every surface that shows them
  also shows a "CONTOH — jadwal sholat belum tersambung ke sumber resmi" marker. The
  `BuildConfig.DEBUG` gate is what keeps that fixture out of a release build, per `CLAUDE.md`.
  Wiring a real source means deleting that class, not adding to it.
- **Hijri + Gregorian date line** in the countdown block (§2) — not rendered; Kalender Hijriah owns
  hijri conversion and is not plumbed into this screen yet.
- **Location.** "Kudus" is part of the sample. A real schedule needs either a user-chosen city or a
  location permission the app does not currently request.
- **Kiblat bearing.** Computing it needs the user's coordinates. The compass face is drawn but no
  needle is: a needle at an arbitrary angle is worse than none. The card says so.
- **Per-prayer reminder toggles** already persist (DataStore) and are real user state — but nothing
  schedules a notification from them yet, since there is no real time to schedule against.

Everything else the revamp touched already reads real local data: the Quran surfaces from Room, the
amaliyah scroller and continue row from the content catalogue and reading position, Aktivitas from
its own tables, Tasbih from its session store.

### Validation

`assembleDebug` ✅ · `compileDebugUnitTestKotlin` ✅ (three existing test call sites updated for the
new constructor parameters; no new tests, per the temporary implementation-pass constraints) ·
`detekt` ✅ · `lint` ✅ · `installDebug` ✅.

Manually verified on a Pixel 9 emulator: the tenang header and its Room-sourced basmalah in both
reader modes, mushaf immersion on tap, the new ayat rows, Beranda's prayer block / tiles / continue
row / scroller, Jadwal Sholat's ticking countdown and current-row highlight, and Tasbih's counter
and segmented target selector. The full Amaliyah reader's new top bar and the Aktivitas streak card
were compiled and reviewed in code but not photographed (Aktivitas had no data on the test device).

`ktlintCheck` still fails repo-wide for the pre-existing reason recorded in step 1 — the files
touched here carry no violations of their own beyond that same constructor-indent conflict.

---

## Jadwal Sholat + Kiblat wired to myquran (2026-08-17)

**Status:** Complete. This closes the one "Needs an API" gap the revamp left open.

Full rationale, the endpoint evidence, and the deferred Quran/calendar decisions
are in ADR [0018](../decisions/0018-myquran-for-prayer-times-and-qibla.md).

### What changed

- **New unauthenticated network client** (`data/remote/prayertimes/`,
  `di/PrayerTimesNetworkModule.kt`) — the app's third, kept separate so a Kemenag
  credential can never reach a non-Kemenag origin (ADR 0016 §5).
- **Room v6** adds `prayer_cities` and `prayer_schedule_days`. A whole month is
  fetched per call and cached, so the schedule works offline for the rest of it.
- **City picker** behind the design's `tune` action. Prayer times need **no
  location permission** — myquran keys schedules by kabupaten/kota.
- **Kiblat** requests `ACCESS_COARSE_LOCATION` only when the reader taps to enable
  it, computes the bearing once via `/qibla/{lat},{lon}`, and caches it. The
  compass needle is drawn only when a real bearing exists, and the card stays
  tappable to recompute after travelling.
- **Hijri + gregorian date line** in the countdown block, from the app's own
  offline `HijrahDate` — deliberately not myquran's `/cal`, which would make an
  already-offline computation network-dependent.
- **Deleted:** the debug-only sample schedule, its `BuildConfig.DEBUG` gate,
  `PrayerSchedule.isSample`, and every "CONTOH" marker.

### Bug found and fixed during verification

With no city chosen the prayer block does not render — and it was the **only**
entry point to Jadwal Sholat, so the screen was unreachable on a fresh install.
Beranda now shows a "Jadwal sholat — pilih kota Anda" setup row in its place. That
is a setup affordance, not an invented schedule, so the "a section with no data is
not rendered" rule still holds for the times themselves.

### Known limitations

- **Room v6 wipes the database.** Under the standing no-migrations policy every
  user re-downloads the Quran once. Accepted when this work was scoped.
- **Terbit and dhuha are fetched and stored but not shown** — the design lists six
  rows, and those two are not among them. They are in Room whenever the screen
  wants them.
- **Nothing schedules a notification from the per-prayer bells yet.** The flags
  persist; wiring them to the existing reminder scheduler is separate work.
- **No automated tests**, per the temporary implementation-pass constraints. The
  existing `SerambiViewModelTest` fake was extended to keep compiling.

### Validation

`assembleDebug` ✅ · `compileDebugUnitTestKotlin` ✅ · `detekt` ✅ · `lint` ✅ ·
`installDebug` ✅.

Manually verified end to end on a Pixel 9 emulator, against the live API:

- fresh install (post-wipe) shows the setup row, and it reaches Jadwal Sholat;
- the city list loads from `/sholat/kabkota/semua` and searches ("kudus" → KAB.
  KUDUS);
- selecting it fetches the month and renders **04.17 / 04.27 / 11.44 / 15.04 /
  17.41 / 18.51**, matching a direct `curl` against the API exactly;
- the date line reads "Senin, 17 Agustus 2026 · 4 Rabiul Awal 1448" (umalqura —
  myquran's `standar` would say 5, the documented divergence);
- the location prompt appears only on the kiblat action, and the computed bearing
  was verified correct for the position the emulator actually reports (19.25°,
  which is the true qibla from the emulator's default Mountain View location —
  `adb emu geo fix` would not move it, so the Kudus value of 294.36° was confirmed
  against the API directly rather than on-device);
- **offline**: with wifi and data disabled, Beranda still renders the full cached
  schedule.

`ktlintCheck` still fails repo-wide for the pre-existing constructor-indent reason
recorded in step 1; the files added here carry no violations of their own.

### City picker redesign (2026-08-17)

The first city picker rendered with a huge dome instead of top corners, which
swallowed its own title. **Root cause:** `SanguSantriShapes.extraLarge` is
deliberately `RoundedCornerShape(percent = 50)` — a stadium for pills — and
Material 3 defaults `ModalBottomSheet`'s shape to `shapes.extraLarge.top()`. Half
of a full-height sheet is a dome. Every other sheet in the app already passed an
explicit shape; this one was the only caller trusting the default.

Fixed at the source as well as locally: `Shape.kt` now states the trap, and
`SanguSantriDimensions.sheetTopCornerRadius` (26dp, per handoff §Radii) is the
shared token to pass. The existing sheets were left alone — they already work.

The sheet was then rebuilt around the fact that it is a 517-row list:

- opens fully expanded (`skipPartiallyExpanded = true`) — scanning a long
  alphabetical list in a half sheet is miserable;
- the search field takes focus on open, because the list starts at "KAB. ACEH
  BARAT" and nobody scrolls to "KUDUS" by hand;
- filled tint search field with a leading search icon, a clear button, and
  explicit 16dp corners (the default text-field shape inherits the same pill
  radius that caused the dome);
- the selected city is pinned above the results with a check while browsing, so
  the sheet always answers "which one am I on?" — and unpinned during a search,
  where a row unrelated to the query is noise;
- `imePadding` keeps results above the keyboard; the list carries bottom padding
  so the last row is not flush against it;
- 56dp minimum row height, hairline separators, a real no-results state naming
  the query, and a close button in the header.

Verified on device: the picker, search ("kud" → KAB. KUDUS), the no-results state,
and selection.

### Follow-up round (2026-08-17)

- **Quran mushaf page bar removed.** The 2dp track and "593 / 604" readout at the foot of each
  flowing page are gone — the juz/page strip above already says where you are, so it was a second
  answer to a question the page had already answered.
- **Location is now offered on first launch** so the prayer schedule can set itself up. Granting
  resolves the device's coarse position to a kabupaten/kota through the platform `Geocoder` and
  selects it (myquran's kabkota lookup returns no coordinates, so the platform geocoder is the only
  bridge). Denying is a normal outcome: the prayer section then reads "Pilih kota Anda untuk
  menampilkan jadwal, atau izinkan lokasi", and Jadwal Sholat offers both paths side by side. The
  prompt is marked shown either way, so it asks once and never nags.
  A resolved name that matches no city fails explicitly ("Kota tidak dapat dideteksi") rather than
  guessing — a wrong city means wrong prayer times.
- **The kiblat compass is live.** `rememberDeviceHeading()` reads `TYPE_ROTATION_VECTOR` (the
  platform's fused orientation, so the needle is steady rather than jittery), and the needle points
  at `qibla − deviceHeading`, animating as the phone turns. The animation crosses 0°/360° the short
  way, so walking past north no longer whips the needle a full turn. Low sensor accuracy swaps the
  hint to the figure-of-eight calibration message; a device with no rotation-vector sensor falls
  back to the absolute bearing rather than a needle that never moves. No permission is involved —
  sensors are not a protected resource.
- `DeviceLocationSource` now holds the one last-known-fix implementation, shared by the qibla
  bearing and city detection instead of being duplicated.

Verified on a clean install: the first-launch prompt appears; denying shows the requested wording;
"Izinkan lokasi" resolves the emulator's position to **KAB. KUDUS** and fetches its schedule; the
bearing computes to **294°** with the live-heading hint active and the needle drawn north-west.

The Quran page-bar removal is a pure deletion and was verified by build only — re-checking it on
device would have meant re-downloading all 114 surahs after the schema wipe.

### City detection fixed, calendar signed off (2026-08-17)

Reported: after granting location, Jadwal Sholat still demanded a city. Reproduced on a real
Galaxy A52 (Android 14) in Jakarta. **Three separate faults, all of which had to be fixed:**

1. **The geocoder ran on the main dispatcher.** `viewModelScope.launch` defaults to
   `Dispatchers.Main`
   and `Geocoder.getFromLocation` is a blocking IPC/network call, so it threw — and `runCatching`
   swallowed it. Detection failed silently every time. `DeviceLocationSource` now runs entirely on
   `Dispatchers.IO`, and detection with it.
2. **No position right after the grant.** The code only ever read the *last known* fix, and an app
   granted location for the first time usually has none cached. Added a single-shot
   `LocationManagerCompat.getCurrentLocation` (coarse providers only, 10s timeout, cancels with the
   coroutine) used when the cache is empty. The qibla bearing takes the same path, so it works first
   try too.
3. **The name matcher was too strict, and ambiguous where it wasn't.** The geocoder returns
   "Kota Jakarta Barat"; myquran has a single "KOTA JAKARTA" for the whole city, so exact matching
   never matched. Worse, "KOTA BANDUNG" and "KAB. BANDUNG" both reduce to "BANDUNG", and the old
   `firstOrNull` would have silently picked one — a wrong-city, wrong-prayer-times bug waiting to
   happen. The matcher now takes exact matches first, then a city whose bare name is a leading whole
   word of the candidate ("KOTA JAKARTA" ⊂ "Kota Jakarta Barat"), disambiguates kota vs kabupaten by
   the candidate's own prefix, and returns `CityDetection.Ambiguous` — which opens the picker
   pre-filtered — rather than guessing when several cities remain plausible.

Beranda also says "Mencari kota Anda dari lokasi…" while detection runs, instead of showing the
"pilih kota" prompt to someone who just granted permission.

Failure logging follows `QuranTafsirManager`'s convention but logs **counts only, never the resolved
place names** — those are the reader's location and do not belong in logcat.

Verified on the device: fresh install → grant → Beranda shows the schedule for **KOTA JAKARTA**
automatically, with no picker and no warnings logged.

### Kalender Hijriah — signed off for production

Product owner decision: keep the app's own offline `HijrahDate` computation. Kalender Hijriah is
production-ready on that basis. The accepted trade-off (Umm al-Qura lands a day before Kemenag/NU on
1 Ramadan and Idul Fitri) and the future option (a curated Kemenag date table over the existing
content pipeline) are recorded in ADR 0018 so the divergence is not later rediscovered as a bug.
