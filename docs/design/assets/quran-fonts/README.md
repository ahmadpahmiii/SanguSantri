# Quran font candidates

These files are product/design inputs for the planned standalone Quran
feature. They are intentionally stored outside `app/src/main/res/font` and
are **not packaged in the APK yet**. Packaging requires the Quran PRD,
licence review, glyph-compatibility checks against the exact Kemenag API
text, and visual verification on supported Android versions.

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

## Planned selection UX

The approved direction is a single-choice font selector rendered as small
preview cards, each showing the same verified Kemenag-sourced Arabic sample.
Although the product owner described the control as "checkboxes", the
choices are mutually exclusive and must therefore use radio-button semantics
for accessibility and state correctness. The selected card is announced as
selected; tapping either the card or its control selects it.

The candidate list currently has two locally supplied fonts. A King Fahd
Complex Hafs smart-device font may be added after it is downloaded from the
official developer platform and supplied with its licence/readme:
`https://qurancomplex.gov.sa/en/techquran/dev/`.

On that page, choose **“Unicode Uthmanic Font (Hafs Narration) for smart
devices”**. Do not substitute the verse-image or full Mushaf application
packages; supply the downloaded font plus every accompanying licence/readme
before it is copied into Android resources.

## Compatibility gate

Font selection must never alter the stored Kemenag Quran text. Before a font
is offered to users, render a fixed corpus from `teks_msi_usmani` that covers
the Quranic combining marks and special signs present in the API, then compare
every candidate for missing glyphs, mark collisions, clipping, shaping
differences, and Android-version consistency. A visually attractive preview
alone is not sufficient approval.
