# Al-Qur'an Kemenag design references

This directory is the durable visual baseline for the standalone Quran
experience planned for release `0.0.6`. It complements—rather than replaces—
`docs/product/QURAN_PRD.md` and `docs/design/QURAN_DESIGN_SYSTEM.md`.

Each approved screen reference is stored as:

* a `720x1600` PNG for quick visual comparison;
* an editable HTML source rendered at `360x800` logical pixels;
* a JSON sidecar recording the product state, tokens, source-data constraints,
  and intentional omissions.

The HTML/JSON files are local design-reference sources, not exported Figma
node payloads. They exist because this design pass intentionally does not use
Figma MCP. If the screens are later recreated in Figma, retain the PNG names
and record the resulting node IDs in `docs/design/FIGMA_HANDOFF.md`.

Open `00-quran-state-catalog.html` for a local state picker. The catalog and
all generated frame contracts are indexed by `00-quran-state-catalog.json`.
Run `ruby generate-quran-catalog.rb` after editing the generator; then render
the changed HTML at device scale 2 to refresh its PNG.

## Complete state set

| Sequence | Screen | Files | Status |
|---|---|---|---|
| 01 | Hub — Surah | `01-quran-hub-surah.*` | Populated list and local search |
| 02 | Hub — Juz | `02-quran-hub-juz.*` | Room-derived navigation contract |
| 03a | Hub — Bookmark | `03a-quran-hub-bookmark-populated.*` | Populated |
| 03b | Hub — Bookmark | `03b-quran-hub-bookmark-empty.*` | Empty guidance |
| 04a | Hub — Terakhir Dibaca | `04a-quran-hub-recent-populated.*` | Populated |
| 04b | Hub — Terakhir Dibaca | `04b-quran-hub-recent-empty.*` | Empty guidance |
| 05a | Initial preparation | `05a-initial-checking.*` | Checking Room |
| 05b | Initial preparation | `05b-initial-preparation.*` | Determinate 72/114 progress |
| 06a | Initial preparation | `06a-initial-offline-error.*` | Offline without local data |
| 06b | Initial preparation | `06b-initial-preparation-error.*` | Retryable preparation failure |
| 06c | Hub refresh | `06c-hub-background-refresh.*` | Non-blocking refresh |
| 06d | Hub refresh | `06d-hub-refresh-failed-cache-kept.*` | Existing cache retained |
| 08 | Reader — Arab + terjemahan | `08-reader-arab-translation.*` | Exact Kemenag text, translation, and footnote |
| 09 | Flowing reader — Arab saja | `09-flowing-reader-arab-only-page.*` | Full page 593, ayat 1–23; canonical Arab-only baseline |
| 09b | Arab-only long-press selected range | `09b-flowing-reader-arab-only-selected.*` | Same page baseline with ayat 15 selected |
| 10 | Ayat action sheet | `10-ayat-action-sheet.*` | Four allowed actions over selected ayat 15 |
| 11 | Tafsir Kemenag | `11-tafsir-loading.*` | Online loading |
| 12 | Tafsir Kemenag | `12-tafsir-success.*` | Exact supplied Ringkas/Tahlili response |
| 13a | Tafsir Kemenag | `13a-tafsir-cached-refreshing.*` | Cached stale-while-refreshing |
| 13b | Tafsir Kemenag | `13b-tafsir-offline-no-cache.*` | Offline without cache |
| 13c | Tafsir Kemenag | `13c-tafsir-error-retry.*` | Retryable failure |
| 14 | Tampilan Al-Qur'an | `14-quran-display-settings.*` | Font cards, live preview, sliders |
| 15 | Sumber Al-Qur'an | `15-quran-source.*` | Attribution and read-only explanation |
| 16 | Aktivitas | `16-activity-quran-session.*` | Quran session in combined streak |
| 17 | Reader | `17-reader-loading.*` | Loading from Room |
| 18 | Reader | `18-reader-invalid-target.*` | Invalid/deleted target recovery |

## Content-safety rule

Never use image generation to invent Quran Arabic. The first reference uses
the exact Surah An-Nas ayat 1–6 strings supplied by the product owner from the
LPMQ Kemenag API, sorted numerically. Presentation adds only derived ayat
markers. A transparent crop from a product-owner-supplied screenshot is
retained only as a superseded placement reference. The active prototype uses
`assets/basmalah-simple-amiri.svg`: a restrained path-only SVG generated from
text published by LPMQ and shaped with the OFL-licensed Amiri Quran font.

The selected-range reference and subsequent Quran frames use the preserved
`data/al-fajr-89-kemenag-response.json` payload. That response is deliberately
out of order; prototypes must sort it by numeric ayat and must not copy its
Latin `teks` field into any visual artefact.

Tafsir success/cache frames read directly from
`data/an-nas-114-tafsir-6232-response.json`. Hub/Juz layouts avoid inventing
missing mappings: only metadata present in supplied Kemenag responses is shown
as a concrete start position. HTML/JSON never become runtime content; the
presentation layer must observe validated Room models.

### Arab-only reader baseline

`07-flowing-reader-arab-only.*` was removed by explicit product-owner decision
on 2026-08-08. It showed a short, centred An-Nas composition and must not be
used for implementation. The canonical Arab-only sequence is now:

1. `09-flowing-reader-arab-only-page.*` — normal full-page state;
2. `09b-flowing-reader-arab-only-selected.*` — the same page with one selected
   annotated ayat range;
3. `10-ayat-action-sheet.*` — the same selected page behind the modal sheet.

All three use the same full Kemenag `halaman = 593` composition and sorted ayat
1–23. Presentation work must not reintroduce the removed short-page layout.
