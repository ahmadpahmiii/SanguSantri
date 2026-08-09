# Quran design assets

Assets in this directory preserve the editable design-source provenance. The
product owner approved the restrained basmalah treatment for Android promotion
on 2026-08-08; the production derivative and its licence now live under
`app/src/main/res/`.

## Basmalah SVG candidate

`basmalah-simple-amiri.svg` is the active prototype asset. It is intentionally
plain and uses no ornamental frame or stretched calligraphic flourish.

* Text source: the exact unvocalised `بسم الله الرحمن الرحيم` displayed in the
  official LPMQ article [Dua Surah yang Tidak Ada di Mushaf Zaman
  Sekarang](https://lajnah.kemenag.go.id/info-lpmq/berita-dan-artikel/artikel/dua-surah-yang-tidak-ada-di-mushaf-zaman-sekarang.html).
* Typeface source: `AmiriQuran-Regular.ttf`, covered by the included SIL Open
  Font License.
* Conversion: HarfBuzz `hb-view` shaped the RTL source into self-contained SVG
  paths. The SVG therefore has no runtime font dependency and does not expose
  editable Quran text.
* Colour: paths use `currentColor`; the root fallback is Quran Arabic text
  `#F1F1EB`.
* Preview: `basmalah-simple-amiri-preview.png` is generated from the SVG and is
  not a second source of truth.

The SVG remains the editable source of the approved Android VectorDrawable
`res/drawable/quran_basmalah_simple.xml`. The derivative preserves the same
glyph outlines, crops only unused viewBox whitespace, and is tinted through
the Quran semantic Arabic-text colour. The Amiri OFL notice is packaged at
`res/raw/amiri_quran_ofl.txt`.

## Superseded placement reference

`basmalah-reference-from-user-screenshot.png` is a transparent crop derived
from the product-owner-supplied screenshot
`WhatsApp Image 2026-08-08 at 12.37.21.jpeg`. It is used only to lock placement
and visual scale without asking an image model to invent Quranic Arabic. It is
retained for provenance but is no longer used by the active prototype.
