# Quran UI state audit — 9 August 2026

Scope: numbered assets in `docs/design/figma-export/quran/` compared with the
Compose implementation for the standalone Al-Qur'an Kemenag feature. “Covered”
means the state has an explicit runtime branch and matching hierarchy; it does
not imply that every failure was forced on a device during this pass.

| No. | State                      | Runtime mapping                                                           | Status                                      |
|-----|----------------------------|---------------------------------------------------------------------------|---------------------------------------------|
| 01  | Hub — Surah                | `QuranHubTab.SURAH`, local search, inset Room list                        | Covered                                     |
| 02  | Hub — Juz                  | `QuranHubTab.JUZ`, locally derived Juz starts                             | Covered                                     |
| 03a | Bookmark populated         | Room bookmark rows                                                        | Covered                                     |
| 03b | Bookmark empty             | Dedicated empty message                                                   | Covered                                     |
| 04a | Terakhir dibaca present    | Optional outlined card above the three tabs                               | Revised and covered                         |
| 04b | Terakhir dibaca absent     | Card omitted; no fake empty tab/state                                     | Revised and covered                         |
| 05a | Initial checking           | Centred progress visual, title, description                               | Revised and covered                         |
| 05b | Initial preparation        | Preparation icon, determinate 0–114 progress and context                  | Revised and covered                         |
| 06a | Initial offline/no cache   | Offline visual, explanation and 48dp retry action                         | Revised and covered                         |
| 06b | Initial preparation failed | Error visual, explanation and 48dp retry action                           | Revised and covered                         |
| 06c | Background refresh         | Non-blocking inline status while Room content remains visible             | Newly covered                               |
| 06d | Refresh failed/cache kept  | Inline failure notice while cached Room content remains visible           | Newly covered                               |
| 07  | No asset with this label   | Flowing Arab-only reference is retained under 09                          | Catalog numbering gap; no runtime-state gap |
| 08  | Reader — Arab + terjemahan | Ordered ayat rows, translation and position metadata                      | Covered                                     |
| 09  | Reader — Arab saja         | Flowing page text with bracketed ayat numbers                             | Covered                                     |
| 09b | Arab-only selection        | Long-press selected range with haptic and semantic action                 | Covered                                     |
| 10  | Ayat action sheet          | Explicit-radius sheet and four actions                                    | Covered                                     |
| 11  | Tafsir loading             | Loading sheet branch                                                      | Covered                                     |
| 12  | Tafsir success             | Ringkas/tahlili content branch                                            | Covered                                     |
| 13a | Cached tafsir refreshing   | Cached content plus refresh status                                        | Covered                                     |
| 13b | Tafsir offline/no cache    | Unavailable branch without fabricated content                             | Covered                                     |
| 13c | Tafsir retryable error     | Error branch with retry action                                            | Covered                                     |
| 14  | Display settings           | Live preview; selectable LPMQ/Amiri; disabled King Fahd; sliders and mode | Revised and covered                         |
| 15  | Source                     | Full-screen source, provenance, offline and permission information        | Covered                                     |
| 16  | Aktivitas Quran session    | Existing Quran-session activity row/filter path                           | Covered                                     |
| 17  | Reader loading             | Reader-shaped skeleton placeholders                                       | Revised and covered                         |
| 18  | Invalid/deleted target     | Explicit error hierarchy and return-to-list action                        | Revised and covered                         |

## Cross-state decisions

- Hub content uses a 640dp maximum readable width and 16dp outer horizontal
  inset; rows retain an additional compact 8dp inner inset.
- The hub has exactly three equal-width tabs. Terakhir dibaca is card state,
  not navigation state.
- Fresh Quran preferences default to 24sp Arabic, 2.00× line spacing and 16sp
  translation. Arabic size spans 14–52sp in 2sp steps; persisted in-range
  values remain untouched.
- LPMQ Isep Misbah is the default packaged font. Amiri Quran is selectable.
  King Fahd stays visible but disabled because its asset is not available.
- Design HTML/JSON for all generated states was regenerated after the tab
  contract changed. The affected hub PNGs were re-rendered at 720×1600.

## Verification boundaries

- States 06a, 06b, 06d, 13b, 13c and 18 require controlled offline/failure or
  invalid-navigation injection for deterministic device capture.
- LPMQ redistribution permission and cross-version visual glyph comparison
  remain release gates; enabling the runtime choice does not close those gates.
- A full 6,236-ayat Room-corpus scan found zero missing code points in LPMQ and
  three in Amiri Quran (U+06D4, U+06D5, U+08D6). Amiri now applies a documented
  word-level LPMQ presentation fallback only when one of those signs occurs;
  the official Kemenag string and ayat annotations are unchanged. Runtime
  rendering across supported API versions still remains part of the release
  glyph gate.
