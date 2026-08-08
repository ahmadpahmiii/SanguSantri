# Kalender Hijriah — Product Requirements Document

**Document version:** 1.0
**Target release:** Android `0.0.7`
**Status:** Product and visual scope approved; Android implementation not started
**Product owner:** Ahmad Fahmi Aisar
**Approved:** 8 August 2026

## 1. Purpose

Kalender Hijriah is a compact, offline-first Gregorian–Hijri calendar for
SanguSantri. It helps users read a Gregorian month alongside calculated Hijri
dates, Indonesian pasaran, selected religious observances, non-weekly fasting
guidance, and official holidays without turning the screen into a dense event
portal.

This document authorises product and visual-design preparation only. Android
implementation remains a separate milestone and must follow the project-wide
architecture, accessibility, content-governance, and testing rules linked from
`docs/product/PRD.md`.

## 2. Approved product decisions

| Area             | Decision                                                                                |
|------------------|-----------------------------------------------------------------------------------------|
| Entry            | Open from Beranda; never add a bottom-navigation item                                   |
| Primary calendar | Gregorian month, Sunday-first                                                           |
| Hijri method     | Android API 26+ `java.time.chrono.HijrahDate` (Umm al-Qura)                             |
| Network          | No runtime calendar API; all required behaviour works offline                           |
| Day rollover     | Local civil midnight, not Maghrib                                                       |
| Browse range     | Ten years before through ten years after the current year                               |
| Weekday labels   | Full names: Ahad, Senin, Selasa, Rabu, Kamis, Jumat, Sabtu                              |
| Cell numerals    | Gregorian day uses Latin digits; the smaller Hijri day uses Arabic-Indic digits (`١٢٣`) |
| Javanese element | Pasaran name only: Legi, Pahing, Pon, Wage, Kliwon                                      |
| Fasting agenda   | Non-weekly, calendar-suitable items only; no Senin–Kamis list or dots                   |
| Holiday emphasis | Sundays and official holidays use a red Gregorian day number                            |
| Event marking    | Amber dot/tag for fasting; coral dot/tag for religious observance or official holiday   |
| Agenda density   | Multi-day observances are one range row; their individual dates may each carry a dot    |
| Theme            | Light and dark; clean, compact, restrained, and consistent with SanguSantri             |

## 3. Feasibility and source decision

### 3.1 Decision

**Go, using local calculations and a versioned local data bundle.** The current
minimum SDK already provides `HijrahDate`, and Pengingat already relies on the
same chronology. A 42-cell month can therefore be generated instantly without
network requests or a new calendar dependency.

MyQuran remains an evaluated comparison source, not a production dependency.
The public endpoint converts one date per request, its holiday routes do not
provide usable event data, and its conversion implementation does not claim to
be the official Indonesian calendar. No documented, stable public Kemenag REST
API for a complete daily/monthly Hijri calendar was identified during this
research pass.

### 3.2 Authority boundary

Umm al-Qura is deterministic calculation data supplied by the Android runtime;
it is not an Indonesian sidang-isbat decision. Kementerian Agama uses an
integrated hisab/rukyat and sidang-isbat process when officially determining
the starts of Ramadan, Syawal, and Zulhijah.

For example, Android Umm al-Qura calculates **8 August 2026 as 25 Safar 1448**,
while the supplied Indonesian reference shows **24 Safar 1448**. The product
owner accepts this difference provided the app identifies its method clearly.

Required information copy:

> Tanggal Hijriah dihitung offline memakai kalender Umm al-Qura bawaan Android.
> Hasil dapat berbeda dari Kalender Hijriah Indonesia Kementerian Agama dan
> penetapan resmi. Pasaran dihitung dari siklus Pancawara. Agenda berasal dari
> bundle lokal terkurasi.

Future Ramadan, Syawal, and Zulhijah dates derived only from Umm al-Qura must
be labelled as **Perhitungan Umm al-Qura**, never **Tanggal resmi Kemenag**.
Once an official determination is available and has been added to a sourced
bundle, the item may be labelled **Dikonfirmasi dari sumber resmi**.

### 3.3 Source matrix

| Data                                | Production source/method                                                            | Product wording                    |
|-------------------------------------|-------------------------------------------------------------------------------------|------------------------------------|
| Hijri day/month/year                | Android `HijrahDate`, Umm al-Qura                                                   | Perhitungan Umm al-Qura            |
| Official Indonesian annual calendar | Published Kemenag calendar/PDF, retained with year and URL                          | Kalender Hijriah Indonesia Kemenag |
| Ramadan/Syawal/Zulhijah decisions   | Kemenag sidang-isbat publication                                                    | Dikonfirmasi dari sumber resmi     |
| Pasaran                             | Local five-day Pancawara cycle using the documented Friday Legi, 8 July 1633 anchor | Perhitungan siklus Pancawara       |
| National holiday/cuti bersama       | Official annual government joint decree or equivalent primary publication           | Hari libur resmi                   |
| Fasting guidance                    | Versioned allowlist audited against named Kemenag publications                      | Panduan puasa; see source detail   |

Primary references:

* [Android `HijrahDate`](https://developer.android.com/reference/java/time/chrono/HijrahDate)
* [Android ICU
  `IslamicCalendar`](https://developer.android.com/reference/android/icu/util/IslamicCalendar)
* [Kemenag: Kalender Hijriah Indonesia 2026](https://diy.kemenag.go.id/news/51879-kalender-hijriah-indonesia-2026-rujukan-bersama-untuk-kepastian-ibadah-dan-layanan-publik.html)
* [Kemenag: PMA 1/2026 and hisab/rukyat](https://kemenag.go.id/nasional/kemenag-terbitkan-pma-12026-padukan-hisab-dan-rukyat-dalam-penetapan-awal-bulan-hijriah-a94ay)
* [Kalender Jawa reference implementation](https://kalenderjawa.github.io)
* [MyQuran calendar documentation](https://api.myquran.com/v3/doc#tag/Kalender)

The Android implementation must use `HijrahDate`, not the ICU
`IslamicCalendar` class, because it already matches Pengingat and avoids two
independent Hijri policies. `IslamicCalendar` is retained only as an evaluated
Android reference.

## 4. Scope

### 4.1 Included in `0.0.7`

* Beranda entry and one full-screen Kalender Hijriah destination.
* Gregorian month navigation, swipe navigation, and **Hari ini**.
* Full Sunday-first weekday labels.
* Gregorian, Arabic-Indic Hijri, and pasaran labels in each day cell.
* Gregorian/Hijri month-span heading and selected-date summary.
* Sundays and official national holidays shown as red Gregorian dates.
* Compact event dots with an accessible text equivalent.
* Filterable agenda: **Semua**, **Puasa**, and **Hari besar & libur**.
* Curated non-weekly fasting/fasting-prohibition rules described in §5.
* Official national holidays/cuti bersama stored as sourced Gregorian dates.
* Source and method information surface.
* Fully offline operation after app installation.
* Light/dark themes and compact, medium, and expanded layouts.

### 4.2 Explicitly excluded

* MyQuran or another calendar network call at runtime.
* Weton combinations, neptu, primbon, fortune, compatibility, or divination.
* Puasa Senin–Kamis rows/dots, because their weekly repetition overwhelms the
  monthly agenda.
* Puasa Daud or other personal recurring patterns that cannot be represented
  as one fixed calendar observance.
* Invented fixed dates for general Rajab or Syakban fasting guidance.
* Haul, pesantren, kyai, NU, historical-birth/death, or community events;
  those require a separate PRD.
* Reminders/notifications, device-calendar integration, `.ics`, sharing, and
  event creation.
* Prayer time, sunset/Maghrib rollover, location-specific hilal, manual Hijri
  adjustment, or selectable calculation methods.
* Claims of Kemenag, NU, pesantren, or religious-authority endorsement without
  a written primary source.

## 5. Local fasting and observance bundle

### 5.1 Editorial rule

“Diakui Kemenag” is not treated as an unlimited or inferred content category.
The shipped bundle is a finite allowlist. Every rule must have a named Kemenag
source, recorded URL, manual structural review, and explicit product-owner
editorial acceptance before release. Corrections create a new bundle version;
they do not silently rewrite an already published version.

### 5.2 Initial allowlist

| Item                     | Rule/presentation                                         | Agenda behaviour                                                        |
|--------------------------|-----------------------------------------------------------|-------------------------------------------------------------------------|
| Ramadan                  | 1 Ramadan until the calculated/confirmed end              | One range entry; never 29–30 repetitive rows                            |
| Tasu'a and Asyura        | 9–10 Muharram                                             | One two-day range with dots on both dates                               |
| Ayyamul Bidh             | 13–15 of each Hijri month                                 | One three-day range; dots on each date                                  |
| Ayyamul Bidh in Zulhijah | 14–15 Zulhijah only                                       | 13 Zulhijah is excluded because it is Tasyrik                           |
| Tarwiyah                 | 8 Zulhijah                                                | One dated entry                                                         |
| Arafah                   | 9 Zulhijah                                                | One dated entry                                                         |
| Six days of Syawal       | Flexible guidance window from 2 through the end of Syawal | One guidance/window entry; do not invent six selected dates or six dots |
| Idul Fitri               | 1 Syawal, fasting prohibited                              | Holiday/observance entry, not a fasting recommendation                  |
| Idul Adha                | 10 Zulhijah, fasting prohibited                           | Holiday/observance entry, not a fasting recommendation                  |
| Days of Tasyrik          | 11–13 Zulhijah, fasting prohibited                        | One three-day prohibition range                                         |

References for the initial allowlist include Kemenag publications on
[Idul Fitri and fasting prohibition](https://kemenag.go.id/ar/hikmah/idul-fitri-dan-beberapa-amalan-utama-rasulullah-saw-Q2x0C),
[Tasyrik](https://bali.kemenag.go.id/jembrana/berita/27022/5-amalan-sunah-di-hari-hari-tasyrik),
[Tarwiyah and Arafah](https://kemenag.go.id/pers-rilis/1-zulhijah-1447-h-jatuh-pada-18-mei-2026-iduladha-27-mei-2026-xARut),
[Tasu'a and Asyura](https://kepri.kemenag.go.id/page/det/kegiatan-penyuluhan-keutamaan-bulan-muharram-digelar-di-masjid-al-a-laa-kecamatan-singkep),
[Ayyamul Bidh](https://gorontalo.kemenag.go.id/daerah/kakankemenag-sampaikan-hikmah-puasa-dalam-safari-ramadan-bupati-pohuwato),
and [six days of Syawal](https://kemenag.go.id/tanya-jawab-fiqih/ingin-sekali-saya-puasa-sunah-syawal-bagaimana-hukum-dan-ketentuannya-HJHdZ).

### 5.3 Required local record metadata

The exact Kotlin/storage shape is an implementation decision, but every
bundled record must preserve at least:

* stable identifier and bundle version;
* category: fasting, fasting prohibition, religious observance, national
  holiday, or collective leave;
* Indonesian title and optional concise explanation;
* a Hijri recurrence rule, flexible window, or explicit Gregorian date/range;
* calculation/confirmation status;
* source publisher, title, URL, publication date when known, and source year;
* editorial review status and correction/replacement note.

The UI must never render directly from a network response. The feature does
not require Room merely to cache immutable calculations, but any persistent
production dataset introduced later must respect Room as the source of truth.

### 5.4 Offline update limitation

With no runtime source sync, future official holidays and sidang-isbat
corrections arrive only through an app/content-bundle update. Dates outside the
known official bundle can still be calculated, but must retain the
**Perhitungan Umm al-Qura** status and must not be coloured or described as an
official holiday unless a sourced Gregorian record exists.

## 6. Information architecture and core flow

```text
Beranda
└── Kalender Hijriah
    ├── Month navigation + Hari ini
    ├── Gregorian / Hijri month heading
    ├── Full-name weekday row
    ├── Month grid (Gregorian + Arabic-Indic Hijri + pasaran + dots)
    ├── Agenda filters and compact event list
    └── Sumber & metode bottom sheet
```

The global shell remains **Beranda | Aktivitas | Tasbih**. Opening Kalender
Hijriah pushes a destination on the existing Navigation 3 back stack. Back
returns to the prior Beranda state.

On first open, the current local Gregorian month appears immediately, today is
selected, and no loading spinner is needed. Selecting a date updates its full
summary and relevant agenda rows. Selecting a muted adjacent-month cell moves
to that month while preserving the selected date. **Hari ini** restores the
current month and selection. The today marker is recalculated on app resume
after a system date or timezone change.

## 7. Visual and interaction contract

### 7.1 Header and grid

* Title: full Indonesian Gregorian month and year.
* Subtitle: covered Hijri month/year span, e.g. **Safar – Rabiulawal 1448**.
* Weekday labels are never abbreviated, including on compact phones.
* Calendar stays Sunday-first.
* Each cell prioritises the Gregorian day number, followed by a smaller
  Arabic-Indic Hijri day in the upper area and the pasaran below.
* Arabic-Indic digits apply only to the visual Hijri number. Screen-reader
  descriptions use ordinary Indonesian spoken date text, not a sequence of
  glyph names.
* Adjacent-month cells are muted but remain selectable and maintain accessible
  contrast.
* Today and selected date must remain distinguishable without relying on
  colour alone.

### 7.2 Colour semantics

| Treatment             | Meaning                                                        |
|-----------------------|----------------------------------------------------------------|
| Red Gregorian numeral | Sunday or sourced official holiday/cuti bersama                |
| Amber dot/tag         | Fasting recommendation or window                               |
| Coral dot/tag         | Religious observance, fasting prohibition, or official holiday |
| Teal outline/fill     | Selected date                                                  |
| Neutral outline/text  | Ordinary date and pasaran                                      |

A date may have more than one dot. Dots are supplemental: TalkBack content
descriptions and the selected-date agenda must name the categories.

### 7.3 Agenda

The default **Semua** view shows only items intersecting the visible month,
sorted chronologically. **Puasa** removes holidays; **Hari besar & libur**
removes fasting recommendations. A multi-day event is one row with a clear
date range, not one row per day. Puasa Senin–Kamis never appears in this
section or as a dot.

### 7.4 Adaptive behaviour

* Compact: single column; grid then agenda.
* Medium: centred single column with larger margins.
* Expanded: grid/selected-date pane beside agenda/source pane when width
  allows; the product still uses bottom navigation at the app-shell level.
* Touch targets are at least 48 dp; font scaling up to 200% must not truncate
  essential date/source meaning.

The approved local visual baseline is
`docs/design/figma-export/hijri-calendar/`. It is a Figma-ready design reference,
not a claim that Figma nodes already exist.

## 8. Functional requirements

### CAL-FR-001 — Entry and navigation

Expose one accessible Beranda entry. Use the existing Navigation 3 stack and
do not add an Activity, parallel navigation framework, or fourth bottom tab.

### CAL-FR-002 — Canonical calculation

Convert Gregorian dates with Android `HijrahDate`. Search for and reuse or
refactor the existing reminder conversion/month-name policy; do not create a
second algorithm or contradictory Hijri name table.

### CAL-FR-003 — Month model

Produce a stable six-week grid where required, including selectable adjacent-
month dates, a correct Hijri span, local weekday, today state, selection state,
and browse limits.

### CAL-FR-004 — Pasaran

Calculate the five-day Pancawara cycle locally from one documented anchor.
Expose only Legi, Pahing, Pon, Wage, and Kliwon. Add unit coverage for the
anchor, negative offsets, leap years, and representative historical/future
dates within the supported range.

### CAL-FR-005 — Numeral presentation

Render the small in-cell Hijri day with Arabic-Indic numerals. Keep Gregorian
numbers, years, range labels, storage values, test fixtures, and accessibility
semantics in their appropriate locale representation; do not alter global app
number formatting.

### CAL-FR-006 — Agenda derivation

Merge calculated allowlist rules with sourced Gregorian official-date records
into one presentation model. Group contiguous multi-day events. Prevent an
item from being represented simultaneously as both a fasting recommendation
and a prohibition.

### CAL-FR-007 — Holiday emphasis

Render every Sunday and only sourced official holiday/cuti-bersama Gregorian
dates in red. A religious observance that is not an official holiday receives
a dot/tag but not automatically a red Gregorian number.

### CAL-FR-008 — Provenance

Every production agenda item must expose its source/status in the detail or
source surface. Missing provenance is a release-blocking data error, not a UI
state to hide.

### CAL-FR-009 — Offline and correction behaviour

Opening, browsing, filtering, and source inspection must work without network
access. A new versioned bundle is required to correct or add official data.

### CAL-FR-010 — Accessibility

Provide full-date cell descriptions, selected/today state, pasaran, holiday
and agenda semantics, logical traversal order, non-colour indicators, 48 dp
targets, dark-theme contrast, and font-scale resilience.

### CAL-FR-011 — Privacy and security

Request no calendar, location, contacts, account, or notification permission.
Send no selected date or browsing activity over the network.

## 9. State model

Minimum UI states:

* current month with today selected;
* browsed month with another date selected;
* month containing two Hijri months or years;
* agenda **Semua**, **Puasa**, and **Hari besar & libur** filters;
* grouped multi-day agenda entry;
* no item for a selected date/month after filtering;
* light and dark themes;
* information/source bottom sheet;
* lower and upper browse boundaries with the unavailable direction disabled.

There is no loading/offline-error state for the core calendar because all
required data is local. A malformed bundled record is a build/release
validation failure, not a recoverable user-facing network error.

## 10. Acceptance criteria

1. The feature works in airplane mode from first open.
2. A supported Gregorian date maps consistently across Kalender Hijriah and
   Pengingat.
3. 8 August 2026 displays **25 Safar 1448** and **Pahing**, with the method
   notice available from the same screen.
4. Weekday headings read **Ahad, Senin, Selasa, Rabu, Kamis, Jumat, Sabtu** in
   full.
5. The small Hijri day in a cell uses Arabic-Indic digits; the main Gregorian
   date remains Latin.
6. Sunday and sourced official-holiday dates are red; ordinary religious
   observances are not made red automatically.
7. Puasa Senin–Kamis produces neither agenda rows nor dots.
8. Ayyamul Bidh appears as one range row and at most one fasting dot per
   included date; 13 Zulhijah is not recommended as Ayyamul Bidh.
9. Idul Fitri, Idul Adha, and Tasyrik are never described as recommended fasts.
10. Every agenda item has inspectable provenance and calculation/confirmation
    status.
11. The app never calls MyQuran or Kemenag to render the calendar.
12. TalkBack announces the full selected date, pasaran, and event semantics;
    dots and colour are not the sole communication mechanism.
13. Compact and expanded layouts remain usable at 200% font scale.
14. The existing three-item bottom navigation and back-stack state remain
    unchanged.

## 11. Delivery slices

1. **Source bundle and domain rules:** final source audit, versioned records,
   Umm al-Qura policy reuse, pasaran calculation, event grouping, and tests.
2. **Calendar UI:** Beranda entry, navigation, grid, selection/today, numeral
   formatting, adaptive layout, and accessibility.
3. **Agenda and provenance:** filters, dots/tags, source sheet, official-status
   wording, dark theme, and screenshot/manual verification.

No slice is independently released as a reduced feature. Do not implement all
slices unless the product owner explicitly starts the `0.0.7` milestone.

## 12. Risks and release gates

| Risk                                          | Mitigation / gate                                           |
|-----------------------------------------------|-------------------------------------------------------------|
| Umm al-Qura differs from Indonesian reference | Persistent source wording; never imply official equivalence |
| Future official dates change                  | Calculation status plus versioned app/bundle update         |
| “All Kemenag fasting” becomes unbounded       | Ship only the audited allowlist in §5                       |
| Dense calendar becomes unreadable             | No weekly fasts; dots plus grouped agenda rows              |
| Red colour implies a holiday incorrectly      | Require sourced Gregorian official-date record              |
| Pasaran anchor/offset error                   | Document anchor and test positive/negative offsets          |
| Arabic numerals confuse accessibility         | Visual-only conversion; Indonesian semantic descriptions    |

Before Android implementation begins, the production bundle itself still
requires source-by-source editorial acceptance and official annual
holiday/cuti-bersama records for every shipped year. The design fixture dates
are not substitutes for that release dataset.
