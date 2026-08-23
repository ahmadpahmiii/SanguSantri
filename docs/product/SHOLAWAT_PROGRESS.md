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

---

## 2026-08-24 — Bait (two-column) layout, driven by a CMS `layout` flag

The reader can now render a qasidah the way it is printed: two hemistichs per
row, sadr on the right and ajuz on the left, separated by a ✻ ornament. Which
items get that is an editorial fact the CMS supplies — it cannot be inferred
from the text, because `salamun-salam` is paired verse and `shalawat-munjiyat`
is continuous prose with the identical step shape and category, and pairing
prose into two columns splits its sentences across columns.

### CMS side (`../cms`)

* `db/migrations/012_content_layout.sql` — `content.layout text not null
  default 'stacked'` plus a `content_layout_check` constraint; `salamun-salam`
  set to `bayt`. Applied to Supabase project `lzxikotwhhezxgbfbtrh`.
* The Go content API carries `layout` on the **detail** responses only. It is
  kept out of `listColumns` deliberately: the list is re-fetched on every
  Beranda resume, so a reader-only field there would make flipping one item's
  layout re-validate every card in the category.
* The Next.js admin editor gained a `LayoutRadioGroup` and refuses to save a
  `bayt` item with an odd verse count.
* Deployed to production and verified against the live URL.

### Android side

* `ContentDetailDto.layout: String? = null` (not on `ContentListItemDto`), new
  domain `ContentLayout { BAYT, STACKED }` with a total parser
  `String?.toContentLayout()` next to `Content.isSholawat`, `layout` on
  `Content`/`ContentEntity`/the entity mappers.
* `ContentImporter.importRemoteDetail` persists it on **both** detail write
  paths — including the steps-unchanged refresh, which is the path a pure
  layout flip actually arrives on. `importListItem` deliberately does not
  touch it.
* Room `@Database` version 9 → 10, `app/schemas/.../10.json` committed, KDoc
  version log extended. No `Migration` class: the standing
  `fallbackToDestructiveMigration(dropAllTables = true)` policy applies, so
  **every table is dropped**.
* New `feature/sholawat/components/SholawatBaytRow.kt`. The right-first order
  is `LayoutDirection.Rtl` doing its job — an RTL `Row` puts its first child at
  the right edge — not position arithmetic. Hemistich Arabic uses `BasicText`
  with `TextAutoSize.StepBased(18.sp..30.sp)` and `maxLines = 2`, keeping the
  existing `arabicTextStyle()` and `withQuranFontFallback()`.
* `SholawatReaderScreen` branches on `content.layout`: `items(steps.chunked(2))`
  for BAYT, today's `items(steps)` with `SholawatVerseBlock` for STACKED.
  While there, `largeArabicMode` stopped being a parameter — it was always
  passed as `!showTranslation`, so it is derived at the one call site.

### Validation

`assembleDebug` ✅ · `lint` ✅ · `detekt` — 0 findings in changed files (3
pre-existing failures remain in `PrayerScheduleRepository(Impl)` and
`AdzanPlaybackService`, untouched here) · `ktlintCheck` — 0 violations in
changed files · `testDebugUnitTest` — 192 run, 0 failed, including four new
`CmsApiContractTest` cases covering layout absent, `"stacked"`, `"bayt"`, and
a garbage value.

Manual on-device verification (Pixel 9 emulator, fresh install, real published
content from the production CMS): `salamun-salam` renders two-column with
reading order right-then-left per row; Room stores `layout = BAYT` with its 32
steps while `tahlil`/`istighosah` stay `STACKED`; the translation toggle puts
each translation under its own hemistich with `1`/`2` ordinals; at font scale
`1.5` the reader falls back to stacked full-width rows and honours the larger
scale rather than shrinking the Arabic back down.

### Known limitations

* **Stanza gaps are not rendered.** Salamun Salam is *musammat* (8 stanzas ×
  4 hemistichs, AAAB with a constant `-ām` refrain), so real breaks exist, but
  nothing in the content marks them and "every 4" would be wrong for
  couplet-form sholawat. Needs a second CMS field; deferred.
* **`layout` is per item**, so an item mixing a prose opening with a qasidah
  body cannot be rendered correctly. No published item does this today.
* The odd-step-count guard (final hemistich alone in the right column) holds by
  construction — `chunked(2)` drops nothing and the missing ajuz becomes a
  `Spacer` — but was **not** exercised on device: no published item has an odd
  count, and the CMS editor now refuses to create one.
* `SholawatBaytRow` does not wrap its text in a `SelectionContainer`, unlike
  `SholawatVerseBlock`; text in bait mode is therefore not selectable.
* Only `salamun-salam` is published in the Sholawat category right now, so the
  stacked-sholawat path was verified through the font-scale fallback rather
  than through a prose item.

---

## 2026-08-24 (later) — Reader appearance settings; layout becomes a user choice

The bait layout shipped earlier today was entirely CMS-driven and fixed-size.
Two product-owner decisions followed: the reader must let the user choose, and
the Arabic was rendering far too large on a real device.

### What changed

* **The 40sp problem is gone.** Arabic-only mode hard-coded
  `MAX_ARABIC_FONT_SIZE_SP` (40sp) whenever the translation was hidden. Both
  Sholawat components now read their sizes from the shared `ReaderSettings`,
  so the default is **28sp** — one step from the Quran reader's 27sp — and it
  is adjustable.
* **A settings sheet**, reached from a new top-bar control. It is the
  *existing* `ReaderSettingsSheet`, not a second one: Arabic typeface, Arabic
  size, translation size, Arabic line spacing and the translation switch all
  come for free, and the sheet gained one optional extra section for the two
  Sholawat-only switches — the same mechanism the Guided Reader already used
  for its progression-mode row.
* **"Bait dua kolom"** and **"Jeda antarbait"**, persisted in the shared
  `ReaderSettings` DataStore. Product-owner decision: one reading preference
  across every Arabic surface, so changing the Arabic size in Sholawat changes
  it in Tahlil too.
* Pairing is now three independent vetoes, all narrowing — the CMS must call
  the item paired verse, the user must not have turned pairing off, and two
  columns must still fit the width and font scale. For a `stacked` item the
  switch is shown **disabled with a caption** rather than hidden: pairing prose
  would split its sentences across columns, so it must not be forceable.
* The top-bar translation toggle now writes the persisted preference instead of
  local Compose state, so it survives leaving the screen and can never disagree
  with the sheet's switch.

### Refactors this forced

`ReaderSettingsSheet` had two optional control parameters and 11 functions,
both at detekt's limits. The optional sections are now one
`ReaderSettingsExtras` bundle, and the Sholawat rows moved to their own file.
`SholawatReaderScreen`'s top bar became its own composable. In passing,
`largeArabicMode` stopped being a parameter — it was always `!showTranslation`
at the one call site.

### Validation

`assembleDebug` ✅ · `lint` ✅ · `testDebugUnitTest` 192 run, 0 failed ·
`detekt` — 0 findings in changed files (the 3 pre-existing ones in
`PrayerScheduleRepository(Impl)` / `AdzanPlaybackService` remain) ·
`ktlintCheck` — 0 violations in changed files.

Manual on-device verification (Pixel 9 emulator, real published content): the
sheet opens with the font selector, three steppers and four switches; turning
"Bait dua kolom" off re-renders Salamun Salam one hemistich per row and greys
out "Jeda antarbait"; turning it back on with "Jeda antarbait" off tightens the
baits; the size stepper visibly resizes both layouts; every value survives a
force-stop and relaunch (confirmed against the DataStore file, which carries
`sholawat_two_column`, `sholawat_bait_gap` and `reader_arabic_font_size_sp`).

### Known limitations

* Stanza-level grouping is still not implemented — "Jeda antarbait" spaces
  every bait equally and knows nothing about stanzas. That still needs a second
  CMS field.
* The settings sheet is a plain `Column` in a `ModalBottomSheet`, so on a short
  screen the user must drag the sheet up to reach the last switches and the
  "Selesai" button. It is not independently scrollable.
* `ReaderSettings` now carries two Sholawat-only fields. That is the accepted
  cost of one shared preference store; if a third reader ever wants its own
  display switches, the store should be split rather than grown again.

