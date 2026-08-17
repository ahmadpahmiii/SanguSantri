# Sholawat dan Artinya — Progress Log

Feature-specific progress doc (product owner instruction: new features get
their own PRD + progress doc, not additions to the shared `docs/PROGRESS.md`
/ `docs/product/PRD.md`). Normative scope lives in `SHOLAWAT_PRD.md`; this
file is only the dated implementation log, mirroring `docs/PROGRESS.md`'s
per-milestone entry style.

A short pointer entry is also added to the shared `docs/PROGRESS.md` and
`docs/product/ROADMAP.md` so overall project state stays discoverable from
one place.

## Sholawat dan Artinya `0.0.8` — Scaffolding (2026-08-16)

**Status:** Scaffolding implemented. Real content is a separate, blocked
follow-up (see `SHOLAWAT_PRD.md` §11) — the product owner has not yet
supplied sholawat titles or a source, so no real catalog/package JSON exists
yet and none was invented.

Scope reached via a `/grilling` interview session before any code was
written (full decision set: `SHOLAWAT_PRD.md`).

### What shipped

**Data layer:** none — deliberately reused unchanged. `ContentRepository`,
`ContentImporter`, `ContentSyncManager`, `ContentApiService`, and the
`content`/`content_steps` Room tables already handled this generically;
confirmed by inspection before writing any code. Added one companion
constant, `Content.SHOLAWAT_CATEGORY = "Shalawat"` (`domain/model/
Content.kt`), so the category-based filtering below has one canonical string
instead of a typo-prone literal in three places.

**`feature/sholawat/` (new package):** `SholawatListScreen`/`ViewModel`/
`UiState` — lists only `Content.SHOLAWAT_CATEGORY` items, same `ContentCard`
component Explore uses. `SholawatReaderScreen`/`ViewModel`/`UiState` —
Hilt assisted-injection ViewModel (same pattern as `ReaderViewModel`), loads
one `ContentDetail`, local (non-persisted) Arabic-only/with-translation
toggle defaulting to Arabic-only, reuses `ReaderLoadingState`/
`ReaderContentUnavailableState`/`ReaderRecoverableErrorState` from
`feature/reader/components` for the non-content states.
`components/SholawatVerseBlock.kt` — renders one verse via the existing
`arabicTextStyle()`/`translationTextStyle()` functions
(`core/designsystem/theme/ReaderTypography.kt`), not a re-derivation of
Arabic/RTL text rendering.

**Navigation:** `SholawatList`/`SholawatReader(contentId)` `NavKey`s and
entries added to `navigation/SanguSantriNavHost.kt`'s existing
`standaloneEntries` group (same bucket as `Explore`/`Pengingat`/
`KalenderHijriah`).

**Beranda entry point:** `SerambiActions.onSholawatClick` (defaulted `= {}`,
following the `onHijriCalendarClick` precedent so no other call site needed
touching); a new supporting-feature card in `SerambiMenuComponents.kt`'s
`SerambiSupportingFeatures`, gated on a new `SerambiUiState.Loaded
.hasSholawatContent` flag computed in `SerambiViewModel` — same
"don't show an entry point to a dead end" convention `showNahwuQuiz`/
`showAmaliyah` already use.

**Exclusion from the generic Amaliyah surfaces (a correction found during
implementation, not anticipated by the original plan):** the approved plan
assumed only `ExploreViewModel` (Jelajahi Amaliyah) needed a
`category == Content.SHOLAWAT_CATEGORY` exclusion filter. Reading
`SerambiScreen`/`SerambiUiState` during implementation showed Beranda's own
"Amaliyah pilihan" featured section and its "Amaliyah" main-feature tile
gate both read the *same* unfiltered `ContentRepository.observeActiveContent()`
result, and would have routed a tapped Sholawat card through the old
Full/Guided reader — the wrong reader entirely. Fixed by filtering
`SerambiViewModel`'s `activeContent` the same way, while separately keeping
a raw (unfiltered) flow to compute `hasSholawatContent`. Also inspected
`SerambiResumeCoordinator` — confirmed (not just assumed) that it only ever
surfaces content with an existing `ReadingPosition`/`GuidedReadingSession`
row, which this stateless feature never writes, so no further change was
needed there.

**Tests:** `SholawatListViewModelTest`, `SholawatReaderViewModelTest`
(new), `ExploreViewModelTest` (new — none existed before), and one new test
method on the existing `SerambiViewModelTest` — all following the existing
`Fake*Repository` + `MainDispatcherRule` pattern, covering the
category filter/exclusion and toggle-default behaviour this milestone adds.

**Docs:** this file; `SHOLAWAT_PRD.md`; `docs/product/ROADMAP.md` (added the
`0.0.8` entry, and corrected its stale `0.0.7` "not started" line to match
`docs/PROGRESS.md`'s actual "implemented and verified" status);
`docs/product/PRD.md` §Related Documents pointer.

### Verification

`ktlintFormat`/`ktlintCheck`/`detekt`: no violations in any file this milestone
created or modified. Both tasks fail at the whole-module level, but only on
~40 pre-existing files this milestone never touches (confirmed by reverting
them to `HEAD` and reproducing the same failures) plus one pre-existing
`QuranEntryScreen.kt` issue — none of that is caused by this change.
`lintDebug`: passed (one pre-existing, unrelated warning in
`SerambiMenuComponents.kt`'s untouched `featureCell`/`supportingCell`
functions, just shifted line numbers). One real `detekt` `LongParameterList`
finding *was* caused by this change (`SerambiSupportingFeatures` reached 6
params) — fixed by collapsing `showNahwuQuiz: Boolean` + `nahwuDescription:
String` into one `nahwuDescription: String?`. `assembleDebug`: succeeded.
New/changed unit tests (`SholawatListViewModelTest`,
`SholawatReaderViewModelTest`, `ExploreViewModelTest`,
`SerambiViewModelTest`): 10/10 passing.

Manual on-device verification (Pixel 9 emulator, fresh install, a temporary
`[FIXTURE]`-labeled bundled catalog entry added only for this check and
reverted immediately after — never committed): Beranda's "Sholawat" card
appears only once the fixture content exists; opens the list screen showing
the fixture title; opening it lands in Arabic-only large-print mode; the
toggle correctly switches to compact Arabic + Indonesian translation and
back; a 3-verse fixture scrolls and wraps correctly in both modes; back
navigation returns correctly through reader → list → Beranda; the fixture
item does **not** appear in Jelajahi Amaliyah (confirmed: still shows "2
amaliyah", no "Shalawat" filter chip) or in Beranda's "Amaliyah pilihan"
section.

### Known limitations

* No real sholawat content — blocking production input, tracked in
  `SHOLAWAT_PRD.md` §11.
* No `docs/operations/CONTENT_GOVERNANCE.md` addendum yet for this content
  category — deliberately deferred to the content-delivery follow-up pass.
* The approved Arabic typeface gap (`ReaderTypography.kt`'s documented
  `FontFamily.Default` interim choice) is inherited as-is; not this
  milestone's problem to solve.

### Next recommended milestone

Supply the real sholawat titles, Arabic text, Indonesian translations, and a
named source (per `SHOLAWAT_PRD.md` §11), then do the content-delivery pass:
real `content-hosting/` catalog/package JSON, manual on-device verification
with real content, and the `CONTENT_GOVERNANCE.md` addendum.
