# Quran LPMQ Kemenag API Contract — Draft Evidence

**Status:** Observed wire evidence for the approved `0.0.6` product/architecture
decision. It is still a draft because failure bodies, rate limits, nullability,
and formal field semantics have not been supplied by LPMQ. Normative product
behaviour lives in `docs/product/QURAN_PRD.md`; ADR 0016 owns the architecture
and accepted direct-client security trade-off.

Do not place real API credentials in this document, source control, Android
resources, or `BuildConfig`. The supplied access description requires
`username` and `token` request headers, but their values are intentionally not
recorded here.

## Base URL

```text
https://quran-api.lpmqkemenag.id/api-alquran
```

## Observed response envelope

All three supplied examples use this shape:

```json
{
  "code": 200,
  "res": "success",
  "data": []
}
```

HTTP status handling, non-success bodies, nullability, rate limits, timeouts,
and retry rules remain unverified.

## List surah

```http
GET /surah/local/{first_number}/{number}
```

Example request:

```text
https://quran-api.lpmqkemenag.id/api-alquran/surah/local/2/3
```

Observed item:

```json
{
  "id": 2,
  "nama": "Al-Baqarah",
  "arabic": " البقرة",
  "arti": "Sapi",
  "kategori_ar": "مدنية",
  "kategori": "Madaniyyah",
  "jmlAyat": 286,
  "ayat_ar": "٢٨٦"
}
```

The example returned surah IDs 2, 3, and 4. It therefore suggests—but does
not yet formally prove—that `first_number` is inclusive and `number` is the
requested item count.

## List ayat by surah

```http
GET /ayat/local/{no_surah}
```

Example request:

```text
https://quran-api.lpmqkemenag.id/api-alquran/ayat/local/114
```

Observed item fields:

```json
{
  "id": 6231,
  "surah": 114,
  "ayat": 1,
  "juz": 30,
  "halaman": 604,
  "teks_msi_usmani": "قُلْ اَعُوْذُ بِرَبِّ النَّاسِۙ",
  "teks_gundul": "قل أعوذ برب الناس",
  "teks": "Qul a‘ūżu birabbin-nās(i).",
  "keterangan": "",
  "terjemah": "Katakanlah (Nabi Muhammad), “Aku berlindung kepada Tuhan manusia,",
  "no_foot": "",
  "teks_foot": ""
}
```

Observed meanings to confirm in the official contract:

| Field             | Draft interpretation                             |
|-------------------|--------------------------------------------------|
| `id`              | Stable API ayat identifier and tafsir lookup key |
| `surah`           | Surah number                                     |
| `ayat`            | Ayat number within the surah                     |
| `juz`             | Juz number                                       |
| `halaman`         | Mushaf page number                               |
| `teks_msi_usmani` | Arabic text in the MSI Usmani representation     |
| `teks_gundul`     | Arabic text without harakat                      |
| `teks`            | Latin transliteration supplied by the API        |
| `keterangan`      | Additional note; empty in the supplied sample    |
| `terjemah`        | Indonesian translation                           |
| `no_foot`         | Footnote reference; empty in the supplied sample |
| `teks_foot`       | Footnote text; empty in the supplied sample      |

### Ordering requirement

The supplied Surah 114 response was not ordered by ayat: it began with ayat
2, then ayat 1, followed by ayat 3–6. The future data layer must therefore
never render or persist canonical reading order from response-array position.
It must validate the numbers and sort deterministically by `ayat` within a
surah. Missing and duplicate ayat must be treated as invalid/incomplete data,
not silently repaired or merged.

The presence of `juz` and `halaman` supports Juz and page-based navigation at
the metadata level. It does not by itself prove that the API contains the
official page line breaks or sufficient information for a pixel-identical
mushaf-page layout.

### Reader-layout implication

The approved Arab-only reader may concatenate already validated, numerically
ordered `teks_msi_usmani` values that share one `halaman` into a presentation-
only annotated string. Each appended source string retains a character range
mapped to its stable remote `id`; the UI uses that range for touch hit-testing
and selected-state drawing. The concatenated string is never persisted as a
second Quran representation, and the original Room rows remain authoritative.

Because no supplied field describes official printed line breaks, word boxes,
or glyph coordinates, Android performs responsive line wrapping. Enabling
translation switches to one UI row per ayat; it does not attempt to interleave
translation inside the flowing Arabic paragraph.

## Tafsir by ayat ID

```http
GET /ayat/local/tafsir/{ayat_id}
```

Example request:

```text
https://quran-api.lpmqkemenag.id/api-alquran/ayat/local/tafsir/6232
```

Observed item:

```json
{
  "id": 6232,
  "surah": 114,
  "ayat": 2,
  "juz": 30,
  "teks": "Raja manusia, yang mengatur semua urusan mereka, dan Dia Mahakaya sehingga tidak membutuhkan mereka.",
  "tahlili": " (2) Allah menjelaskan bahwa Tuhan yang mendidik manusia itu adalah yang memiliki dan yang mengatur semua syariat, yang membuat undang-undang, peraturan-peraturan, dan hukum-hukum agama. Barang siapa yang mematuhinya akan berbahagia hidup di dunia dan di akhirat."
}
```

The example demonstrates that the endpoint parameter is the ayat record's
`id` (`6232`), not ayat number `2`. The precise editorial meanings of `teks`
and `tahlili`, including their official display labels, remain to be confirmed
with LPMQ/Kemenag documentation.

## Data-layer implications to carry into the PRD

* Treat remote DTOs as untrusted transport input; validate before replacing
  locally readable Quran data.
* The UI must not render DTOs directly. Room remains the local source of truth
  in line with the existing SanguSantri architecture.
* Preserve source text exactly. Do not AI-correct, normalize, complete, merge,
  or silently reorder content beyond the explicit numeric ordering rule.
* Keep `id` as the remote tafsir lookup key, while using the composite
  `(surah, ayat)` as the human reading identity unless later contract evidence
  establishes a better stable-version key.
* Store `juz` and `halaman` as source metadata; do not infer missing values.
* The PRD explicitly prohibits persisting or displaying the `teks` Latin
  transliteration field.
* Direct authenticated access, seven-day complete refresh, Room caching,
  attribution, and credential hardening are decided in the PRD/ADR. Formal
  rate-limit guidance, LPMQ font redistribution permission, and final
  production credential injection remain release inputs.
