# Design System

Applies to any UI task: Serambi, the reader, settings, or any new screen.
Read alongside `docs/design/ACCESSIBILITY.md` and
`docs/engineering/CODING_STANDARD.md` (Compose rules).

## Why this document exists

No screen beyond a placeholder exists yet (`docs/PROGRESS.md`). The risk
this document manages is prospective: the most likely default output of any
agent — Claude included — asked to build a home screen without explicit
restraint tokens and a written anti-pattern list is a generic Material
"card wall" with a hero header. That is exactly what SanguSantri must not
look like. Define the restraint here, before Serambi's first draft, not
after.

## Visual direction

* Content-first visual hierarchy — typography and spacing carry hierarchy,
  not decoration.
* Restrained Islamic green identity (`SantriGreen10`–`90` in
  `core/designsystem/theme/Color.kt`) — traditional-modern pesantren
  character, not a generic startup palette.
* Warm neutral reading surfaces (`SantriNeutral10/90/95/99`) — calm, high
  readability, non-ornamental.
* Modern Material 3 interaction patterns, traditional pesantren tone.
* Dynamic color is intentionally disabled (`SanguSantriTheme` in `Theme.kt`)
  so the SanguSantri identity stays consistent across devices instead of
  following the user's wallpaper — this is a deliberate, already-implemented
  choice, not something to "fix" if a future session notices dynamic color
  is off.

## Anti-patterns — do not build these

* No card-wall home screens — do not default every content block to a
  `Card`. Use a card only when a bounded, tappable, visually distinct unit
  is genuinely needed.
* No oversized hero sections or generic marketing-style headers.
* No decorative gradients as a default background or accent treatment.
* No glassmorphism (blurred translucent panels).
* No ornamental backgrounds, textures, or patterns behind Arabic text —
  Arabic devotional text sits on a plain, high-contrast reading surface.
* No pseudo-Arabic Latin typefaces (decorative fonts that mimic Arabic
  calligraphy using Latin letterforms) anywhere in the app, including
  headings and marketing copy.
* No advertisements or promotional elements inside the reader.

## Tokens

Currently defined: brand colors (`Color.kt`), one `bodyLarge` text style
(`Type.kt`). Not yet defined: a full type scale, Arabic-specific
typography, spacing scale, shape tokens, elevation policy, icon sizes. This
is correct for the current milestone — do not build tokens nothing consumes
yet.

**Before the first Serambi screen is written**, the implementing session
must add, in `core/designsystem/theme`:

* A spacing scale (e.g. 4/8/12/16/24/32dp steps) — not ad hoc `.dp` literals
  per screen.
* A small shape set: 2–3 corner radii used deliberately, not one radius
  invented per component.
* An elevation policy that prefers tonal surfaces or borders over shadow
  stacking — Material's default elevation shadows read as generic/AI-typical
  at default settings.
* A type scale that gives Arabic text and Indonesian translation text
  distinct, deliberately different styles (see Arabic typography below) —
  typography is the primary hierarchy tool per the visual direction above,
  so this cannot be deferred past the first reader screen. **Implemented
  Milestone 3**: `core/designsystem/theme/ReaderTypography.kt`
  (`arabicTextStyle`/`translationTextStyle`) — functions rather than fixed
  `Typography` entries, since Arabic/translation font size and line spacing
  are user-configurable (FR-008).
* Icon sizes, if icons beyond default Material icon sizing are needed.

Do not build these speculatively now — this is Milestone 3 (Serambi) setup
work, out of scope for this documentation pass.

## Arabic typography

The Arabic font must correctly render harakat and the Quranic marks used by
approved content, have a legally verified distribution licence, remain
readable at large sizes, work in both themes, and be visually tested on
multiple Android versions. Do not download a font at runtime for core
reading. Sourcing/licensing the typeface is a Blocking Production Input
(`docs/product/PRD.md` §13) as much as a design-system dependency — do not
ship a substitute Arabic font as if it were final.

## Layout

* Reader text uses a constrained readable width on large displays rather
  than stretching edge to edge (FR-014).
* Support compact, medium, and expanded window-size classes (phone,
  foldable, tablet) using the AndroidX adaptive layout APIs and the
  installed `adaptive` skill — do not hand-roll breakpoint logic.
* Support large font scales (up to at least 1.5×) without clipping or
  overlap — test this explicitly, it is a required Compose UI test
  scenario (`docs/engineering/TESTING.md`).
* RTL: mirror layout and navigation icons correctly; Arabic devotional
  content stays correctly aligned and readable regardless of the selected
  interface language (FR-013).

## Previews and screenshot testing

* Add `@Preview` composables for every reusable visual component, using
  realistic sample content (not `"Lorem ipsum"` or single-word placeholders)
  so a reviewer can actually judge the visual result.
* Once reader screens exist, add Roborazzi-based screenshot tests (see the
  installed `testing-setup` skill's reference on Compose screenshot
  testing) covering: light theme, dark theme, RTL, and font scale `1.5` at
  minimum, for every screen added to `feature/`.
