# Al-Qur'an Kemenag Design System

**Applies to:** every standalone Quran screen, overlay, system bar, and state
in release `0.0.6`.
**Status:** approved design direction; design-tool frames not yet created.

Read with `DESIGN_SYSTEM.md`, `ACCESSIBILITY.md`, and
`docs/product/QURAN_PRD.md`. This is a feature theme layered through the
existing `SanguSantriTheme`; it must not become a second application-wide
theme implementation or duplicate token system.

## 1. Experience direction

The Quran feature is a calm, focused reading room inside SanguSantri:

* always dark, even when the surrounding app is light;
* near-black reader canvas, charcoal-green navigation surfaces;
* restrained green emphasis, never decorative gradients;
* Arabic text is the strongest visual element;
* translation and metadata are clearly subordinate;
* plain high-contrast surfaces with no pattern behind Quran text;
* no persistent bottom navigation, advertising, audio player, or ornamental
  chrome competing with reading.

Dark mode is a product identity decision, not a medical eye-comfort claim.
Comfort for prolonged reading comes from configurable type, line spacing,
brightness, constrained width, and device testing as well as colour.

### Research basis

* Semantic background/surface/primary/on-colour roles follow the Material 3
  colour-system model rather than assigning colours per screen:
  `https://m3.material.io/styles/color/system/overview`.
* Text contrast is evaluated with the WCAG relative-luminance/contrast model:
  `https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html`.
* Android dark-theme behaviour, including dark system surfaces and avoiding
  theme flashes, follows the platform guidance:
  `https://developer.android.com/develop/ui/views/theming/darktheme`.

The sources justify semantic roles and measurable contrast, not a claim that
dark mode is universally healthier. The final comfort decision remains a
manual long-reading test with adjustable typography and brightness.

## 2. Dark colour roles

These roles reuse or extend the existing SanguSantri green/neutral ramps. Use
semantic role names in code; do not scatter hexadecimal literals through
composables.

| Quran role                |              Hex | Use                                   |
|---------------------------|-----------------:|---------------------------------------|
| `quranBackground`         |        `#050806` | Reader canvas and true dark gaps      |
| `quranSurface`            |        `#101713` | Hub, app bars, bottom sheets          |
| `quranSurfaceHigh`        |        `#1A1C19` | Selected cards, settings controls     |
| `quranPrimary`            |        `#7FDB9C` | Active tab, bookmark, focus, progress |
| `quranOnPrimary`          |        `#00391C` | Content on a solid primary action     |
| `quranPrimaryContainer`   |        `#00391C` | Selected preview/filter surface       |
| `quranOnPrimaryContainer` |        `#A1F5B9` | Content on selected surface           |
| `quranArabicText`         |        `#F1F1EB` | Main Quran Arabic                     |
| `quranTranslationText`    |        `#C3C8C0` | Indonesian translation                |
| `quranMutedText`          |        `#95A099` | Surah metadata, Juz/page, timestamps  |
| `quranOutline`            |        `#2D3933` | Hairline dividers and quiet borders   |
| `quranScrim`              | `#000000` at 64% | Modal background                      |
| `quranError`              |        `#FFB4AB` | Error text/icon                       |

Reference contrast on `quranBackground`/`quranSurface`:

* Arabic `#F1F1EB` on `#050806`: approximately 17.74:1.
* Translation `#C3C8C0` on `#101713`: approximately 10.70:1.
* Primary `#7FDB9C` on `#101713`: approximately 10.85:1.

Do not use pure white for long Quran text. Do not reduce muted text below
WCAG AA. Selected/unselected and success/error states require icon, label, or
shape differences in addition to colour.

## 3. Typography

### 3.1 Font candidates

Candidate binaries and provenance live under
`docs/design/assets/quran-fonts/`. LPMQ Isep Misbah and Amiri Quran are packaged
as Android font resources for the approved selector; the release gate still
requires the licence and glyph checks below.

1. **LPMQ Isep Misbah** — default candidate; closest source pairing for
   Kemenag `teks_msi_usmani`.
2. **Amiri Quran** — optional OFL candidate.
3. **King Fahd Uthmanic Hafs for smart devices** — optional official
   candidate after its file/readme is supplied and compatibility is proven.

Font selection is not permission to substitute a second Quran dataset. Every
font renders the same Kemenag string. The packaged Amiri Quran binary lacks
U+06D4, U+06D5, and U+08D6 used by the validated Kemenag corpus, so its Android
presentation explicitly falls back to LPMQ for the complete affected word;
this compatibility span is documented, changes no stored string, and avoids a
tofu box. Any remaining candidate that changes, drops, collides, or clips marks
is disabled rather than silently passing the release gate.

### 3.2 Reader defaults and ranges

| Role                    |               Default |   User range | Guidance                       |
|-------------------------|----------------------:|-------------:|--------------------------------|
| Arabic size             |                  24sp |      14–52sp | Slider, 2sp steps              |
| Arabic line height      |                 2.00× |   1.45–2.20× | Slider, live preview           |
| Translation size        |                  16sp |      14–24sp | Slider, 1sp steps              |
| Translation line height |                 1.55× |      Derived | Not a separate control         |
| Surah title             | Material `titleLarge` | System scale | Indonesian/Latin UI font       |
| Metadata                |  Material `bodySmall` | System scale | Never below accessible minimum |

Arabic uses RTL direction and centre/right alignment according to the active
display choice. Translation uses Indonesian LTR. Quranic combining marks must be
included in measured line height; never clip the font box to imitate tighter
screenshots.

The 24sp portrait default is the product-owner-approved first impression for
the primary 360dp-wide viewport with the packaged LPMQ default. The 2.00×
line-height default deliberately gives Quranic combining marks and dense lines
more vertical breathing room, while the 14sp minimum supports users who prefer
a compact overview without making that compact value the initial experience.
This is a first-impression default, not a universal readability claim: every
cleared Quran font must be recalibrated against the same verified corpus because
fonts with the same nominal `sp` can have materially different visual sizes.

## 4. Settings preview cards

The font selector is a vertical set of mutually exclusive preview cards:

* card title: source-facing font name;
* preview: the exact same verified Kemenag ayat fragment for every candidate;
* selected indicator: radio semantics plus green border/container;
* optional caption: `Standar Indonesia`, `Naskh`, or `Madinah` only when the
  label is factually verified;
* unavailable candidate: shown disabled with `Belum tersedia`, never selectable.

Although visually card-based, this is one radio group—not a multi-select
checkbox list. Tapping anywhere on a card selects it. The live preview below
the size and spacing sliders updates immediately. Each change persists to
DataStore as it is made and updates an already-open reader; Back keeps the
latest values. There is no separate Save button or uncommitted draft state.

## 5. Screen specifications

### 5.1 Quran hub

* Dark top app bar: `Al-Qur'an`, search, source/settings overflow as needed.
* Optional Terakhir dibaca card directly under the app bar; absent when no
  saved position exists and never represented as a tab.
* Three equal-width primary tabs: Surah, Juz, Bookmark.
* Lists are flat with separators or tonal selection; do not turn every row
  into a large card.
* Surah rows prioritise number, name, Arabic name, category, and ayat count.
* Juz rows prioritise Juz number and starting surah/ayat/page derived locally.
* Bookmark rows show enough position context to resume confidently.

### 5.2 Initial preparation

* Remain within the dark theme from the first frame.
* Use a calm centred status, determinate progress, completed-surah count, and
  concise explanation that the first preparation needs internet.
* No fake Quran sample text while loading.
* Error state contains one primary `Coba lagi` action and no technical detail.

### 5.3 Arab-only flowing reader

* `quranBackground` fills the screen behind edge-to-edge content.
* Minimal app bar: back, surah title, Juz/page context, settings/reader-mode
  action.
* At the beginning of a surah, show one compact, non-sticky header band with
  the exact Kemenag category on the left, surah name centred, and ayat count on
  the right. Use a dark primary-container treatment rather than the bright teal
  banner in the external reference screenshot; the header must remain
  subordinate to the Arabic reading text.
* Place the source-verified basmalah directly below that header for every surah
  except At-Taubah. Al-Fatihah's basmalah is its ayat 1 and must not be rendered
  a second time as a separate decorative header. Do not repeat the surah header
  or basmalah when scrolling through later pages of the same surah.
* The approved simple vector source is
  `design-export/quran/assets/basmalah-simple-amiri.svg`: path-only,
  `currentColor`, no frame or ornamental flourish. It uses an exact
  unvocalised basmalah published by LPMQ and Amiri Quran outlines. Product
  review accepted this treatment on 2026-08-08. Its Android derivative is
  `res/drawable/quran_basmalah_simple.xml`, with the Amiri OFL notice packaged
  at `res/raw/amiri_quran_ofl.txt`.
* Arabic flows RTL in a constrained column with inline ayat markers. This is
  the layout selected automatically by `Arab saja`; it is not a separate mode.
* Page transition may be vertical scrolling; do not imitate a printed page
  edge if exact printed composition is unavailable.
* Current page/Juz uses muted text or a small green accent, not a dominant
  banner.
* Long-press highlights only the annotated ayat range. A range spanning several
  visual lines receives a background on each affected line rather than turning
  the entire page into one selected rectangle.

### 5.4 Arab with translation reader

* One lazy item per ayat, separated by quiet `quranOutline` dividers.
* Arabic first; its translation below with generous separation. This layout is
  selected automatically by `Arab + terjemahan`.
* No visible per-ayat menu, button row, audio icon, copy, or share icon.
* Long-press opens the action sheet. Ordinary scrolling/tapping stays calm.
* Bookmark state may use a small non-interactive marker that does not look
  like a competing action.

### 5.5 Ayat action sheet

Sheet title identifies surah and ayat. Actions:

1. Tambahkan/Hapus bookmark.
2. Tafsir Kemenag.
3. Tandai terakhir dibaca.
4. Informasi Juz dan halaman.

No copy/share/audio action. The sheet is dismissible by system Back and a
visible close action.

The selected ayat remains visually identifiable behind the sheet. Use
`quranPrimaryContainer` for the selected range/row; do not use `quranError` or
red because selection is not a failure state. Clear selection when the sheet is
dismissed.

### 5.6 Tafsir sheet

* Heading: surah and ayat.
* Source line: Kementerian Agama RI.
* Loading: `Memuat tafsir Kemenag…`.
* Content sections: labels for concise `teks` and `tahlili`, final wording
  verified with the source contract/design review.
* Cached stale content remains visible while refreshing.
* Inline retry does not close or replace the reader behind the sheet.
* Long tafsir content scrolls independently.

### 5.7 Tampilan Al-Qur'an

This is a full-screen nested settings destination:

1. Live preview region.
2. Font preview cards.
3. Arabic-size slider.
4. Arabic-spacing slider.
5. Translation-size slider.
6. Arab saja / Arab + terjemahan control.
7. Quran brightness slider.

No light-mode toggle and no keep-screen-on control.

### 5.8 Source view

Use a full-screen nested **Sumber Al-Qur'an** destination reachable from the
hub overflow and the bottom of Tampilan Al-Qur'an. Show the approved
attribution, API/source name, offline-cache explanation, and permission scope
without exposing credentials. Do not collapse “official data source” into
“official Kemenag application.”

### 5.9 Aktivitas row

Use the existing Aktivitas row vocabulary. Quran entry content:

* title: `Membaca Al-Qur'an`;
* detail: surah and ayat range;
* timestamp;
* non-colour Quran/book marker only if an existing icon style supports it.

## 6. Interaction and motion

* Long-press uses platform haptic feedback and semantic long-click support.
* Do not add a visible tutorial/hint overlay.
* Mode changes use a restrained crossfade that respects reduced motion.
* Tab and page changes preserve scroll/position state.
* Tafsir and action sheets use standard Material motion; no decorative glow,
  parallax, page curl, or simulated paper animation.
* Brightness changes apply live only to the Quran window and restore on exit.

## 7. Accessibility

* Long-press ayat must expose an accessibility long-click action even though
  no visible action control exists.
* Arabic and translation are separate semantics blocks with correct language
  and reading order where platform support permits.
* Sheet focus begins at the heading/first action and never remains behind the
  modal.
* Font cards announce font name and selected state.
* Sliders announce value and update preview without focus loss.
* All visible controls meet 48dp minimum targets.
* Test font scale 1.5×, TalkBack, RTL shaping, OLED/LCD contrast, low
  brightness, rotation, and process recreation.

## 8. Portrait-primary layout

Primary design frames are 360×800 portrait. Implementation must not lock
orientation. Landscape/tablet use the same dark tokens and a centred reader
column with a maximum readable width; no navigation rail is introduced for
Quran. Rotation preserves active tab, display choice, ayat/page, sheets when
reasonable, and uncommitted settings state.

## 9. Design page and frame checklist

Create a new page named **`03 Al-Qur'an Kemenag`** in the existing product
design file. Minimum frames:

1. Quran hub — Surah.
2. Quran hub — Juz.
3. Quran hub — Bookmark empty/populated.
4. Quran hub — Terakhir dibaca card present/absent.
5. Initial preparation.
6. Initial offline/error.
7. Flowing reader — Arab saja, full Kemenag-page composition using the same
   baseline as the selected/action-sheet states.
8. Ayat reader — Arab + terjemahan.
9. Arab-only long-press selected range over that unchanged full-page baseline.
10. Ayat action sheet.
11. Tafsir loading.
12. Tafsir success.
13. Tafsir error/offline cache state.
14. Tampilan Al-Qur'an with font previews and sliders.
15. Source view.
16. Aktivitas with Quran session.

All frames must use editable layers/components, shared local variables, and
auto layout. Exact node IDs are added to `DESIGN_HANDOFF.md` after creation.

## 10. Visual acceptance gate

Before implementation is considered visually complete:

* Compare all enabled fonts on the same verified corpus.
* Inspect harakat/waqaf collisions at minimum/default/maximum sizes.
* Verify the palette on one OLED and one LCD device.
* Complete one uninterrupted 30-minute reading pass at medium and low
  brightness and record subjective issues without claiming medical evidence.
* Confirm no light surface flashes during entry, process restoration, dialogs,
  or system bars.
* Confirm page mode is labelled/documented as metadata grouping, not a printed
  mushaf facsimile.
