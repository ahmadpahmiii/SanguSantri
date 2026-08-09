# Kalender Hijriah design references

This directory is the durable, local visual baseline for Kalender
Hijriah in Android release `0.0.7`. It complements
`docs/product/HIJRI_CALENDAR_PRD.md`; it does not implement the feature.

Each approved state is stored as:

* an editable HTML source at `360x800` logical pixels;
* a JSON sidecar containing state, semantics, provenance boundaries, and
  design-handoff metadata;
* a `720x1600` PNG preview rendered at device scale 2.

These files are local design references, not payloads exported from a hosted
design tool. If the screens are later recreated in one, retain the frame names
and record their page/node IDs in `docs/design/DESIGN_HANDOFF.md`.

Open `00-hijri-calendar-state-catalog.html` for the local visual catalog. Run
`ruby generate-hijri-calendar-catalog.rb` after editing the generator, then
render the four frame HTML files at device scale 2 to refresh PNG previews.

## Approved state set

| Sequence | State                     | Intent                                                              |
|----------|---------------------------|---------------------------------------------------------------------|
| 01       | Calendar overview — light | Current month, selected ordinary date, full agenda                  |
| 02       | Calendar overview — dark  | Dark theme and selected official religious holiday                  |
| 03       | Puasa filter              | Weekly fasting excluded; Ayyamul Bidh grouped as one range          |
| 04       | Sumber & metode           | Umm al-Qura/Kemenag authority boundary and local-source explanation |

## Locked design decisions

* Weekday headings use **Ahad, Senin, Selasa, Rabu, Kamis, Jumat, Sabtu** in
  full; they are not abbreviated on compact screens.
* The main Gregorian number uses Latin digits. Only the smaller in-cell Hijri
  number uses Arabic-Indic digits such as `٢٥`.
* The cell shows pasaran only: Legi, Pahing, Pon, Wage, Kliwon. It never shows
  weton, neptu, or primbon.
* Red Gregorian numbers mean Sunday or a sourced official holiday. Amber dots
  mean fasting; coral dots mean a religious observance/official holiday.
* Puasa Senin–Kamis is intentionally absent. Multi-day items are grouped into
  one agenda row to keep the interface compact.
* Arabic-Indic digits are visual presentation only. Accessibility semantics
  announce a normal full Indonesian date.

## Fixture and authority boundary

The August 2026 dates are design fixtures based on the approved method and
source research. They must not be copied blindly into a runtime production
bundle. The production bundle still needs record-level provenance, annual
official-holiday data, editorial acceptance, and versioning as required by the
PRD.

The source sheet deliberately says that Android Umm al-Qura calculations may
differ from the Kalender Hijriah Indonesia Kementerian Agama or an official
sidang-isbat determination. The UI must never remove or weaken that boundary.

## Design-tool mapping

Suggested page name, if a hosted design tool is ever adopted: **Kalender
Hijriah**. Frame names match the HTML base names. Current node IDs are `null`
by design because this pass creates the local export package only; update
`docs/design/DESIGN_HANDOFF.md` if the frames are ever recreated there.

