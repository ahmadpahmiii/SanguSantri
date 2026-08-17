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
* **FR-SHL-004** The Arabic-only/with-translation toggle is one control per
  page, defaulting to Arabic-only, implemented as local (non-persisted)
  Compose state — never the shared `ReaderSettings` DataStore toggle the
  Full/Guided Amaliyah reader uses.
* **FR-SHL-005** Arabic-only mode uses `ReaderSettings.MAX_ARABIC_FONT_SIZE_SP`
  (40sp); with-translation mode uses `ReaderSettings.DEFAULT_ARABIC_FONT_SIZE_SP`
  (28sp) for the Arabic line and `ReaderSettings.DEFAULT_TRANSLATION_FONT_SIZE_SP`
  (16sp) for the translation line — reusing the existing documented range,
  not a new one. Both modes scroll normally; neither shrinks text to force a
  sholawat onto one screen.
* **FR-SHL-006** Sholawat items are excluded from: Jelajahi Amaliyah
  (`ExploreViewModel`), Beranda's featured-Amaliyah section and "Amaliyah"
  main-feature gate (`SerambiViewModel`'s `activeContent`). Both exclusions
  filter on `category == Content.SHOLAWAT_CATEGORY`.
* **FR-SHL-007** No `ReadingPosition`, `GuidedReadingSession`, or
  `ReaderSettings` row is ever written by this feature — confirmed stateless,
  and confirmed (by inspection of `SerambiResumeCoordinator`) that this also
  means a sholawat can never appear in Beranda's "continue reading" widget.

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
