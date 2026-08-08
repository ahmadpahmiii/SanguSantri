# Quran design-response fixtures

This directory preserves product-owner-supplied Kemenag responses used by the
local Quran visual references. Files here are design evidence, not bundled
release data and not an alternate Quran source.

## `al-fajr-89-kemenag-response.json`

* Source: response attached by the product owner on 8 August 2026.
* Envelope: `code = 200`, `res = success`.
* Records: 30 unique ayat for Surah 89, Juz 30.
* Page metadata: ayat 1–23 are page 593; ayat 24–30 are page 594.
* Response order is non-canonical and must be sorted numerically by `ayat`
  before presentation.
* Footnotes are present for ayat 2, 9, 16, and 17.
* SHA-256:
  `f9f0d8ac8cf4d327be0f4d06f1b76b7ddafc30b2eb047123d252da39724c0cf4`.

Future prototypes may use the Arabic, translation, and footnote fields from
this response, but must not display or persist its `teks` Latin transliteration
field. Preserve every source string exactly and never infer missing content.

## `an-nas-114-tafsir-6232-response.json`

* Source: response supplied by the product owner on 7 August 2026.
* Record: An-Nas ayat 2, remote ayat ID 6232, Juz 30.
* The Tafsir Ringkas and Tafsir Tahlili design frames read their wording
  directly from this file.
* Loading and error frames never invent replacement tafsir content.
