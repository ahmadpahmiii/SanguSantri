# Quran font candidates

These files are the provenance copies for the app-wide Arabic font selector.
LPMQ Isep Misbah and Amiri Quran were copied into `app/src/main/res/font` by
explicit product-owner direction on 9 August 2026; KFGQPC HAFS Uthmanic Script
followed on 23 August 2026. KFGQPC Nastaleeq was packaged the same day and
**withdrawn hours later** — see the rejected list. The picker is no longer
Quran-only: the same selection drives the Full/Guided amaliyah readers and the
Sholawat reader too.

## Supplied candidates

### LPMQ Isep Misbah

* File: `LPMQ-Isep-Misbah.ttf`
* Internal family/full name: `LPMQ Isep Misbah`
* Foundry: LPMQ
* File metadata copyright: Lajnah Pentashihan Mushaf Al-Qur'an, 2018
* SHA-256:
  `b0927593ebd07550b879c31e36085d1bb44f7a066b9824fad2b62822b7887e21`
* Supplied by the product owner on 8 August 2026.
* Official download reference:
  `https://lajnah.kemenag.go.id/info-lpmq/unduhan/quran-kemenag/font-lpmq-isep-misbah.html`

The font file did not arrive with a separate licence file. Do not infer a
redistribution licence from download availability alone. The product owner
states that SanguSantri has official LPMQ/Kemenag API access; font embedding
and redistribution must still be covered by that written permission or a
separate explicit font licence before this candidate moves into Android
resources.

### Amiri Quran Regular

* File: `AmiriQuran-Regular.ttf`
* Internal family/full name: `Amiri Quran` / `Amiri Quran Regular`
* Foundry: ALIF
* SHA-256:
  `6814dda5c41a412ce873da0551b11fa924d01f79592e6294b702911e49b9d3a6`
* Supplied by the product owner on 8 August 2026.
* Licence: SIL Open Font License 1.1, preserved verbatim in
  `AmiriQuran-OFL.txt`.
* Upstream: `https://github.com/aliftype/amiri`

### KFGQPC HAFS Uthmanic Script

* File: `KFGQPC-HAFS-Uthmanic-Script.ttf`
* Internal family/full name: `KFGQPC HAFS Uthmanic Script` (version 2.2)
* Foundry: King Fahd Glorious Quran Printing Complex
* SHA-256:
  `aa68bffce289b4c0ebac68e90502eb69e42356abcd1603cb2b8e99c2c723f145`
* Supplied by the product owner on 23 August 2026. This is the
  "Unicode Uthmanic Font (Hafs Narration) for smart devices" download from
  `https://qurancomplex.gov.sa/en/techquran/dev/`, not a verse-image or
  per-page package.
* Licence: KFGQPC electronic EULA, preserved verbatim in `KFGQPC-EULA.txt` —
  free to use, copy and distribute; **must not be sold, modified, altered or
  reverse engineered**. `fsType` is 0 (installable embedding). The
  no-modification clause is why the file is shipped byte-identical and is never
  subset to save APK size.

## Rejected candidates (23 August 2026)

Supplied in the same batch and deliberately **not** packaged:

* **QPC V4 Tajweed** (`QPC V4 Tajweed - TTF.ttf`). Its internal name is
  `QCF4001_COLOR`: this is page 1 of the 604-file per-page QCF4 family, 108
  glyphs, and its `cmap` contains no Arabic-block code points at all. It maps
  private glyph codes that only quran.com's V4 per-page glyph API produces, so
  it cannot render Kemenag Unicode text — every one of the 72 code points the
  stored corpus uses is missing. The colour machinery itself is fine (COLR
  version 0 + CPAL with 6 palettes, which Android has supported since API 26);
  the blocker is the text pipeline, and adopting it would mean 604 font files
  plus a second, non-Kemenag content source — contrary to ADR 0016 §2.
* **DigitalKhatt V2** (`Digital Khatt V2 - OTF.otf`, SIL OFL 1.1). Misses 8 of
  the 72 corpus code points, covering 5036 occurrences — U+06E4 (2098),
  U+0657 (1257), U+0656 (991), U+08D6 (562) and others. Falling those back to
  LPMQ would repaint a large share of every page in a second typeface, which
  defeats the point of choosing a face. It is also CFF2-outline with no `fvar`,
  which is not worth the API-26 loading risk for a face that cannot be shown
  intact anyway.
* **KFGQPC Nastaleeq** (`KFGQPC-Nastaleeq.ttf`, SHA-256
  `de174e33ae14cb581097940de298c8bb9cfa60f7a34d7d0ab0ba2bd5126f912c`, same
  KFGQPC EULA). Packaged on 23 August 2026 on a `cmap`-only reading of the gate
  and **withdrawn the same day**: it passes cmap coverage but fails mark
  positioning. Eight of the Quranic marks the corpus uses — U+06D7 (3647
  occurrences), U+06E4 (2099), U+06DA (1538), U+06E2 (546), U+06D6 (502),
  U+06D8 (86), U+06DB (36), U+06DF (27) — are either given a non-mark GDEF
  class or carry no `MarkBasePos`/`MarkMarkPos` anchor, so HarfBuzz draws them
  at the pen position instead of over their base letter. U+06E4's glyph is
  0.70 em wide, so `الۤمّۤ` (Al-Baqarah 1) collapsed into an illegible blot.
  About 8400 occurrences would have to fall back — roughly 9% of the words on a
  page — which makes the "choice" of face meaningless. The font is a general
  Urdu/Persian Nastaliq text face, not a mushaf face; it was never built to
  carry waqf marks. It renders amaliyah and sholawat Arabic beautifully, but the
  selection is app-wide by design, so it cannot be offered for those alone.
  The provenance copy is kept here; the Android resource was deleted.
* **All `.woff2` files.** Android's font stack does not read WOFF or WOFF2 in
  any form — `res/font`, `assets`, or downloadable fonts. Only TTF/OTF are
  usable; the WOFF2 copies are web-only and were discarded.

## Selection UX

The approved direction is a single-choice font selector rendered as small
preview cards, each showing the same verified Kemenag-sourced Arabic sample.
Although the product owner described the control as "checkboxes", the
choices are mutually exclusive and must therefore use radio-button semantics
for accessibility and state correctness. The selected card is announced as
selected; tapping either the card or its control selects it.

The picker offers three packaged faces, one full-width row each, all rendering
the same stored Kemenag ayat so the choice is made by reading the sample rather
than by recognising a font name.

## Compatibility gate

Font selection must never alter the stored Kemenag Quran text. Before a font
is offered to users, render a fixed corpus from `teks_msi_usmani` that covers
the Quranic combining marks and special signs present in the API, then compare
every candidate for missing glyphs, mark collisions, clipping, shaping
differences, and Android-version consistency. A visually attractive preview
alone is not sufficient approval.

### Gate result, 23 August 2026

Measured with fontTools against the exact code-point set of the two corpora the
app renders — the `arabicText` of all 6236 stored Kemenag ayat (72 distinct code
points) and every bundled amaliyah/sholawat `content_steps.arabicText`. The font
choice is app-wide, so a candidate has to clear both.

**A `cmap` check is not the gate.** A face fails in two ways, and the second is
the one that shipped a broken font on 23 August 2026:

1. **No glyph** — the code point is absent from `cmap`; it renders as tofu.
2. **An unpositioned mark** — the glyph exists, but the face gives a Unicode
   combining mark (category `Mn`) a non-mark GDEF class, or no
   `MarkBasePos`/`MarkMarkPos` anchor. HarfBuzz then draws it at the pen
   position rather than over its base letter: on the baseline if the glyph is
   small, as a blot over the word if it is large.

So the gate is: cmap coverage, **then** GDEF class and GPOS mark coverage for
every `Mn` code point in the corpora, **then** eyes on a real page containing
the highest-frequency marks (Al-Baqarah 1 exercises U+06E4 twice).

| Face                 | No glyph                                           | Unpositioned marks                                                                                                                                | Outcome                                               |
|----------------------|----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| LPMQ Isep Misbah     | none                                               | none in practice (a few zero-advance marks lack anchors but are drawn correctly by design; the three with a non-zero advance total 6 occurrences) | default; also the fallback face                       |
| KFGQPC HAFS Uthmanic | U+0622 (14), U+08D6 (562), U+08D9 (1)              | U+06E4 (2099), U+06DF (27), U+06E3 (3), U+06EB (1) — all classed as base glyphs, so they land on the baseline                                     | packaged, ~2700 occurrences fall back (≈4 words/page) |
| Amiri Quran          | U+06D4 (29), U+06D5 (98), U+08D6 (562), U+08D9 (1) | none                                                                                                                                              | packaged, ~690 occurrences fall back (≈1 word/page)   |
| KFGQPC Nastaleeq     | U+08D6, U+08D9                                     | 8 marks, ~8400 occurrences                                                                                                                        | rejected                                              |
| DigitalKhatt V2      | 8 code points, 5036 occurrences                    | not assessed                                                                                                                                      | rejected                                              |
| QPC V4 Tajweed       | all 72                                             | not assessed                                                                                                                                      | rejected                                              |

Both failure kinds feed the same table, `FONT_MISSING_CODE_POINTS` in
`feature/quran/QuranFontFamilies.kt`: only the whole word containing an affected
code point is restyled in LPMQ, so the rest of the page keeps the chosen face
and the stored text is never modified. The table is measured, not guessed —
re-measure it whenever a font file changes or synced content introduces a code
point outside these two corpora. An unmeasured gap renders as tofu; an
unmeasured unpositioned mark renders as a misplaced or overlapping sign, which
is worse, because it is wrong rather than obviously absent.
