# Sholawat dan Artinya — Product Requirements Document

**Document version:** 1.0
**Target release:** Android `0.0.8`
**Status:** Scaffolding implemented; content blocked on product owner
**Product owner:** Ahmad Fahmi Aisar
**Decision date:** 16 August 2026 (`/grilling` session, before any code was written)

## 1. Purpose

This document is the feature-level product source of truth for **Sholawat dan
Artinya** — reading sholawat (praise recitations upon the Prophet ﷺ) together
with their Indonesian translation, for actual sholawatan use. Per product
owner instruction, this feature gets its own dedicated PRD and progress doc
(`SHOLAWAT_PROGRESS.md`) rather than being folded into the shared
`docs/product/PRD.md` / `docs/PROGRESS.md`.

Implementation must also follow the project-wide architecture, content
safety, security, privacy, testing, and accessibility documents linked from
`docs/product/PRD.md`, and the content-safety rules in the repository's
`CLAUDE.md` (no invented Arabic text or translation, no Latin
transliteration, source must be recorded).

## 2. Product outcome

Users can open a sholawat from a simple list, read it Arabic-only in a large,
comfortable "recitation" size, and optionally switch to a compact view with
the Indonesian translation shown beneath each verse — fully offline after the
content has synced once, via the same content pipeline the rest of the app
already uses.

## 3. Scope (`0.0.8`)

In scope:

* A Beranda shortcut (supporting-feature card, alongside Pengingat/Belajar).
* A list/library screen of sholawat titles.
* One full reading page per sholawat, continuous scroll of all its verses.
* Indonesian translation only.
* A single global per-page toggle: Arabic-only (large font, default) vs.
  Arabic + translation (compact).
* Reuse of the existing offline-first content sync pipeline unchanged.

Explicitly out of scope for `0.0.8`:

* English translation or any language toggle.
* Listing inside Jelajahi Amaliyah (deliberately deferred, even though
  `docs/engineering/CONTENT_MODEL.md`'s category taxonomy already reserves a
  "Shalawat" filter value there).
* Any repetition counter / Tasbih integration.
* Bookmarks, favorites, or resume-last-position — stateless.
* A `docs/operations/CONTENT_GOVERNANCE.md` addendum documenting this
  feature's governance tier — deferred to a separate pass.
* Real sholawat content — see §13.

Added 2026-08-24 (bait layout), and explicitly still out of scope:

* **Stanza gaps.** Salamun Salam is *musammat* — 8 stanzas of 4 hemistichs,
  AAAB with a constant `-ām` refrain — so real stanza breaks exist. Nothing
  in the content marks them, and a "break every 4" rule would be wrong for
  couplet-form sholawat. This needs a second CMS field; deferred. The
  "Jeda antarbait" switch added on 2026-08-24 is **not** this: it spaces
  every bait equally and knows nothing about stanzas.
* **Mixed-layout items.** `layout` is per item, so an item whose opening is
  prose and whose body is qasidah cannot be rendered correctly in one pass.
  Known limitation, not currently present in any published item.
* Adjustable font size, verse numbers, tablet/landscape layouts, and
  reading-position persistence — all still out, per FR-SHL-007.

## 4. Information architecture

```
Beranda ("Sholawat" supporting-feature card, shown only once content exists)
  -> Sholawat list (grid of titles, same ContentCard component Explore uses)
       -> Sholawat reader (one page per title)
            [translation toggle in the top app bar]
```

Never a bottom-navigation destination. Never listed inside Jelajahi Amaliyah
for this milestone.

## 5. Core flows

1. **Discover.** From Beranda, tap the "Sholawat" card (shown only once at
   least one active Sholawat-category item exists — same convention as the
   Nahwu Quiz and Amaliyah cards).
2. **Browse.** The list screen shows every active sholawat title as a card;
   tapping one opens its reader.
3. **Read/recite.** The reader opens Arabic-only, large font, continuous
   scroll. Tapping the translation icon switches the whole page to the
   compact Arabic + translation layout; tapping again switches back. Leaving
   and reopening a sholawat always starts Arabic-only again (stateless).

## 6. Functional requirements

* **FR-SHL-001** The list screen shows only `Content` items whose `category`
  equals `Content.SHOLAWAT_CATEGORY` ("Shalawat"), sourced from
  `ContentRepository.observeActiveContent()` — no new repository method.
* **FR-SHL-002** The reader loads one item's `ContentDetail` via the existing
  `ContentRepository.getContentDetail(contentId)` — no new repository method,
  no new Room table.
* **FR-SHL-003** The reader renders each `ContentStep` as one verse: Arabic
  text via the existing `arabicTextStyle()`, translation via the existing
  `translationTextStyle()` (both from `core/designsystem/theme/
  ReaderTypography.kt`) — not a re-derivation of Arabic typography.
* **FR-SHL-004** *(revised 2026-08-24, reader settings)* The
  Arabic-only/with-translation toggle stays one control in the top app bar,
  but it now **writes to the shared `ReaderSettings.showTranslation`
  preference** rather than to local Compose state, so it survives leaving the
  screen. The same preference backs the switch inside the settings sheet, so
  the two controls can never disagree. This reverses the original
  stateless-v1 decision by product-owner approval; the default is unchanged
  from `ReaderSettings` (translation shown).
* **FR-SHL-005** *(revised 2026-08-24, twice: bait layout, then reader
  settings)* Type size is **user-controlled**, from the same
  `ReaderSettings` values the Full and Guided Readers use — Arabic size
  (default 28sp), translation size (default 16sp) and Arabic line spacing
  (default 1.9×). The reader no longer picks a size from its own mode: it
  previously forced `MAX_ARABIC_FONT_SIZE_SP` (40sp) whenever the
  translation was hidden, which rendered far larger than the Quran reader
  and could not be turned down. Both modes scroll normally; neither shrinks
  text to force a sholawat onto one screen.

  In the **bait** layout each hemistich has only half the width, so a single
  fixed size is wrong in both directions — short hemistichs would be
  needlessly small and long ones would overflow. Hemistich Arabic is
  therefore auto-sized (`TextAutoSize.StepBased`, `maxLines = 2`) with the
  user's own Arabic size as the **ceiling** and 62% of it as the floor, so
  the size stepper still drives this text; it simply cannot overflow a
  half-width cell. Typography still comes from the existing
  `arabicTextStyle()`/`translationTextStyle()` — the range is a width
  constraint, not a second Arabic type scale.
* **FR-SHL-013** The reader has an appearance settings sheet, reached from a
  top-bar control, which **reuses the Full/Guided Reader's
  `ReaderSettingsSheet`** rather than maintaining a second one: Arabic
  typeface (the app-wide `QuranArabicFont`), Arabic size, translation size,
  Arabic line spacing, and a translation switch — plus two Sholawat-only
  switches, "Bait dua kolom" and "Jeda antarbait". Every value persists in
  the shared `ReaderSettings` DataStore, so changing the Arabic size here
  changes it in the Amaliyah readers too. That is the intended product
  behaviour: one reading preference across every Arabic reading surface.
* **FR-SHL-014** "Bait dua kolom" can only ever **narrow** what the CMS
  allows. For an item the CMS marked `stacked` the switch is shown disabled
  with an explanatory caption rather than hidden — pairing prose would split
  its sentences across columns, so the user must not be able to force it.
* **FR-SHL-015** "Jeda antarbait" controls the vertical gap between bait
  rows. It is disabled whenever pairing is not actually in effect, since a
  one-verse-per-row layout has no baits to space apart. Stanza-level
  grouping remains out of scope — see §3.
* **FR-SHL-008** The CMS supplies a per-item `layout` flag on its detail
  endpoint, valued `bayt` or `stacked`, surfaced as `Content.layout`. The
  reader must not guess it: `salamun-salam` is paired verse (32 steps = 16
  baits) while `shalawat-munjiyat` is continuous prose, identical in step
  shape and category, and pairing prose into two columns splits sentences
  across columns.
* **FR-SHL-009** `stacked` is the default everywhere. An absent, null,
  blank, or unrecognised value resolves to `stacked`, never to `bayt` and
  never to a crash — the field is a free-text CMS column reaching a
  compiled-in client. Stacked reads correctly for any content; two columns
  do not, so the fallback is deliberately one-directional.
* **FR-SHL-010** In `bayt`, steps are paired in reading order: step *n*
  is the sadr and renders in the **right** column, step *n+1* is the ajuz
  and renders in the **left**, separated by a ✻ ornament. The pairing comes
  from `LayoutDirection.Rtl` placing the first child at the right edge, not
  from position arithmetic. When the translation is shown, each hemistich's
  translation sits under that hemistich, in two LTR `weight(1f)` cells at
  `translationTextStyle()`'s existing default size. **Column position alone
  carries the pairing** — no ordinal markers or other prefixes.
* **FR-SHL-011** An item flagged `bayt` with an **odd** step count renders
  its final hemistich alone in the right column, with an empty left cell.
  No step is ever dropped and the reader never crashes. This is a content
  error, not a supported shape: the CMS editor refuses to save `bayt` with
  an odd verse count, and this rule exists only for content already
  published before that check.
* **FR-SHL-012** The reader falls back to `stacked` regardless of the flag
  when two columns cannot hold: available width below **320dp**, or a system
  font scale above **1.3×**. The font-scale trigger is an accessibility
  requirement, not an aesthetic one — `docs/design/ACCESSIBILITY.md` mandates
  testing at 1.5×, and half-width cells with an auto-size floor and a
  two-line cap would answer a larger scale by shrinking the text back down,
  silently undoing the user's setting.
* **FR-SHL-006** Sholawat items are excluded from: Jelajahi Amaliyah
  (`ExploreViewModel`), Beranda's featured-Amaliyah section and "Amaliyah"
  main-feature gate (`SerambiViewModel`'s `activeContent`). Both exclusions
  filter on `category == Content.SHOLAWAT_CATEGORY`.
* **FR-SHL-007** *(narrowed 2026-08-24, reader settings)* No
  `ReadingPosition` and no `GuidedReadingSession` row is ever written by this
  feature — opening a sholawat always starts at the top, and (confirmed by
  inspection of `SerambiResumeCoordinator`) a sholawat can never appear in
  Beranda's "continue reading" widget. `ReaderSettings` **is** now written:
  appearance is a preference, not reading state, and resetting it on every
  open was the thing users actually noticed.

## 7. Data ownership

No new Room tables, DAOs, or domain models. Sholawat content lives in the
existing `content` / `content_steps` tables exactly like Tahlil/Istighosah,
distinguished only by `Content.category == Content.SHOLAWAT_CATEGORY`
("Shalawat"). Delivered the same way — Firebase Hosting static JSON, a new
`content-hosting/public/content/catalog.json` item plus a new package JSON —
through the unchanged `ContentApiService` / `ContentImporter` /
`ContentSyncManager` pipeline (ADR 0012, amended; ADR 0014).

## 8. Loading / empty / error states

* List screen: loading spinner; empty state ("Sholawat sedang disiapkan.")
  when no active Sholawat-category content exists yet.
* Reader: loading spinner; unavailable state if the content id resolves to
  nothing or has no steps; recoverable-error state with retry on an
  unexpected repository failure — all three reuse the existing generic
  `ReaderLoadingState` / `ReaderContentUnavailableState` /
  `ReaderRecoverableErrorState` composables from `feature/reader/components`.

## 9. Non-functional requirements

* Offline-first: once synced, fully readable without network, via the
  unchanged sync pipeline.
* No Latin transliteration anywhere in this feature.
* No English translation field in `0.0.8` (Indonesian-only, per product
  owner decision) — the existing `ContentStep.translation` field already
  fits this without a schema change.

## 10. Acceptance criteria

1. A Beranda "Sholawat" card appears only when at least one active
   Sholawat-category item exists, and never appears in Jelajahi Amaliyah's
   listing or Beranda's featured-Amaliyah section.
2. Tapping the card opens a list of sholawat titles; tapping a title opens
   that sholawat's own reading page.
3. The reading page opens Arabic-only, large font, scrollable.
4. Tapping the translation toggle switches to Arabic + Indonesian
   translation (compact), and back; the state resets to Arabic-only every
   time the page is freshly opened.
5. No bookmark, favorite, or resume-position UI or persistence exists for
   this feature.

## 11. Blocking production inputs

* **The actual sholawat titles, their Arabic text, and their Indonesian
  translation, together with a named, publicly accessible, trusted
  editorial source (publisher/URL)** — required before any real
  `content-hosting/public/content/catalog.json` entry or package JSON can be
  created for this feature. Per the repository's content-safety rules,
  Claude does not select or invent this content; the product owner supplies
  it. Until then, `0.0.8` ships as scaffolding only, verified with a
  temporary `[FIXTURE]`-labeled local entry that never reaches the release
  build.

## 12. Delivery sequence

1. **Scaffolding** (this pass) — list + reader screens, navigation, Beranda
   entry point, exclusion from the generic Amaliyah surfaces, unit test
   coverage for the new filtering/state logic.
2. **Content** (blocked, separate pass) — real catalog/package JSON once
   titles and source are supplied; manual on-device verification with real
   content; `CONTENT_GOVERNANCE.md` addendum documenting the risk tier
   actually used.
