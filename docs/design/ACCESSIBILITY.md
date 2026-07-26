# Accessibility

Applies to any UI task. Read alongside `docs/design/DESIGN_SYSTEM.md`.

## Requirements (FR-013, FR-014)

* Controls must have content descriptions.
* Counter state must be announced by accessibility services (current count,
  target, completion) — applies equally to the Guided Reader counter and
  the `0.0.2` Standalone Tasbih counter; both share the same semantics
  pattern (`stateDescription`, non-colour completion cue below).
* Touch targets must meet Material guidance (**48dp minimum**) — this
  applies to every new interactive element this pass introduces: the
  Beranda/Jelajahi search entry, category chips, the Full Reader
  repetition-shortcut action, Table of Contents section rows, and the
  Tasbih target selector/counter.
* The interface must be legible and comfortable for teenagers, adults, and
  elderly users alike (`docs/design/DESIGN_SYSTEM.md`) — this is a product
  requirement, not solely an accessibility-mode concern. Support large
  font scales — test at `1.5×` minimum.
* Colour must not be the only status indicator (e.g. completion, revoked
  content, sync failure, and the Tasbih target-reached state all need a
  non-colour signal too — an icon or shape change alongside any colour
  change, matching the existing Guided Reader counter's checkmark +
  colour pattern).
* RTL must be functionally tested, not just visually inspected — mirrored
  navigation icons, correct reading order, correct text alignment. Applies
  to the new bottom navigation bar/rail icons as well once built.
* Arabic and translation text must remain selectable when practical.

## Modal bottom sheets (Reader Settings, Table of Contents)

* On open, initial accessibility focus must land on the sheet's heading or
  first actionable control, not silently remain on the trigger behind it.
* The sheet must be dismissible via a visible close action and the system
  back gesture/button alike — do not rely on a swipe-only dismissal for
  users who cannot perform it.
* Table of Contents section rows must announce the section title and step
  range together, and must announce which section is "current" as a
  state, not merely via visual highlight.

## Numeric input (custom Tasbih target dialog)

* The custom-target numeric field must expose an appropriate input type
  (numeric keyboard) and announce validation errors (e.g. zero, negative,
  or non-numeric input) through accessibility services, not colour alone.
* The dialog must not accept a target that would make the counter
  meaningless (non-positive) — reject before dismissal, not after.

## Reduced motion

No reduced-motion signal exists anywhere in the app yet (known gap since
Milestone 4 — the Guided Reader's step-transition fade ignores the system
animator-scale setting). Any new motion introduced by this pass (Full
Reader repetition-shortcut transition, Tasbih counter feedback, bottom-nav
transitions) must be added with this gap in mind — prefer no animation or
an instant state change over adding more unconditional motion, until a
reduced-motion check is implemented.

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
