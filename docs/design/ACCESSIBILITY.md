# Accessibility

Applies to any UI task. Read alongside `docs/design/DESIGN_SYSTEM.md`.

## Requirements (FR-013, FR-014)

* Controls must have content descriptions.
* Counter state must be announced by accessibility services (current count,
  target, completion).
* Touch targets must meet Material guidance (48dp minimum).
* The interface must support large font scales — test at `1.5×` minimum.
* Colour must not be the only status indicator (e.g. completion, revoked
  content, sync failure all need a non-color signal too).
* RTL must be functionally tested, not just visually inspected — mirrored
  navigation icons, correct reading order, correct text alignment.
* Arabic and translation text must remain selectable when practical.

## RTL and localisation

* Indonesian and Arabic are both supported interface languages; Arabic
  devotional content renders correctly regardless of which interface
  language is active (FR-013).
* Use natural Arabic labels in Arabic localisation — never transliterated
  Indonesian terminology (PRD §7.2).
* Localise dates and numerals where appropriate.
* `android:supportsRtl="true"` is already set in the manifest. No
  `values-in`/`values-ar` locale resources exist yet — correctly deferred
  until user-facing strings ship with a real screen.

## Adaptive layout

Support compact phones, landscape phones, foldables where possible, and
tablets (FR-014). Reader text uses a constrained readable width on large
displays rather than stretching edge to edge. Use the AndroidX adaptive
layout APIs and the installed `adaptive` skill; do not hand-roll breakpoint
logic.

## Testing expectation

Every reader-facing Compose UI test suite must include an RTL render, a
landscape render, a tablet-width render, and a font-scale `1.5` render as
baseline scenarios — these are already listed as required scenarios in
`docs/engineering/TESTING.md`; this document is the accessibility rationale
for why they are required, not a second copy of the test list.
