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

**Superseded by the Figma product-alignment pass** (`docs/design/FIGMA_HANDOFF.md`,
`docs/reviews/figma-product-alignment.md`): the product direction is now
a **modern Islamic identity** — premium, calm, comfortable to read — not
necessarily traditional pesantren ornament. Where this document previously
said "traditional-modern pesantren character/tone," read that as
superseded by the direction below; it is kept in this document's history
rather than silently deleted, per the project's own content/documentation
change-tracking convention.

* Content-first visual hierarchy — typography and spacing carry hierarchy,
  not decoration.
* Restrained Islamic green identity (`SantriGreen10`–`90` in
  `core/designsystem/theme/Color.kt`) — modern, not a generic startup
  palette and not a traditional-ornament palette either.
* Warm neutral reading surfaces (`SantriNeutral10/90/95/99`) — calm, high
  readability, non-ornamental.
* A **limited spiritual-gold accent** — new token, not yet defined in
  `Color.kt` (Phase A/B implementation task; do not build this
  speculatively before that phase). Use it sparingly (e.g. a single
  highlight state such as a completed counter or an active tab indicator),
  never as a dominant surface colour.
* Modern Material 3 interaction patterns.
* The interface must remain comfortably usable by teenagers, adults, and
  elderly users alike — legibility and touch-target size are product
  requirements, not accessibility-only concerns (`docs/design/ACCESSIBILITY.md`).
* Dynamic color is intentionally disabled (`SanguSantriTheme` in `Theme.kt`)
  so the SanguSantri identity stays consistent across devices instead of
  following the user's wallpaper — this is a deliberate, already-implemented
  choice, not something to "fix" if a future session notices dynamic color
  is off.

## Anti-patterns — do not build these

* No card-wall home screens — do not default every content block to a
  `Card`. Use a card only when a bounded, tappable, visually distinct unit
  is genuinely needed. Beranda in particular must not default to "every
  section is a `Card`" (`docs/reviews/figma-product-alignment.md`).
* No oversized hero sections or generic marketing-style headers.
* No decorative gradients as a default background or accent treatment.
* No glassmorphism (blurred translucent panels).
* No ornamental patterns, textures, or backgrounds behind Arabic text —
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

Standalone Quran `0.0.6` has an intentionally dark-only reading environment,
dedicated high-contrast tokens, Quran-font preview cards, and page/ayat reader
components. Those feature-specific tokens extend this system; they do not
replace or duplicate the global app theme. The canonical specification is
[`QURAN_DESIGN_SYSTEM.md`](QURAN_DESIGN_SYSTEM.md). Entering Quran from the
global light theme applies that feature scheme only, then restores the prior
theme on exit.

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

## Adaptive navigation (bottom-navigation-only shell through 0.0.6)

**Implemented, Milestone 9.** Product owner/tech lead decision (ADR
[0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md),
2026-07-29) — supersedes this section's earlier bar-on-compact/rail-on-
expanded model: a **bottom navigation bar only**, on every window-size
class including expanded/tablet, through `0.0.5` (Beranda/Aktivitas/
Tasbih; Pengingat and Nahwu Quiz are never nav destinations; Pesantren/
Profil are out of scope entirely in this window). `NavigationSuiteScaffold`
is deliberately not used, since its purpose is exactly the adaptive
bar/rail swap this decision forbids — a plain Material 3 `NavigationBar`
is used instead (`navigation/BottomNavigationBar.kt`). The AndroidX
adaptive-layout APIs and installed `adaptive` skill remain the correct
tool for adaptive *content* layout (a constrained, centred max-width
column on large screens, same rule as reader layout) and for whichever
future release, if any, revisits a rail.

Al-Qur'an Kemenag `0.0.6` does not change the shell destinations: it opens
from Beranda and hides the bar throughout its immersive flow. Its design is
portrait-primary but must not force device orientation or break larger-window
accessibility.

## Component rules

Restraint-first guidance for the new screens this pass introduces (Beranda,
Jelajahi Amaliyah, Standalone Tasbih, Aktivitas). None of this is a license
to reintroduce a card wall (see Anti-patterns above).

* **Search**: a single, unobtrusive entry point (e.g. a search field or
  affordance at the top of Beranda/Jelajahi), not a full-screen search
  takeover by default.
* **Section** (Beranda/Aktivitas): a plain vertical block with a title and
  content — not automatically wrapped in a `Card`. A section that has no
  real data to show renders nothing (FR-019), never an empty-state card.
* **Card**: reserved for genuinely bounded, tappable, visually distinct
  items — an amaliyah entry, for instance. Flat with a hairline border
  (existing `AmaliyahCard` elevation policy), not shadow-elevated.
* **Chip**: for compact filters (Jelajahi's All/Favourite/Offline) and
  category labels — not for primary navigation.
* **Counter**: the Guided Reader/Tasbih counter is the strongest visual
  element in its immediate context (see Tasbih target hierarchy below);
  completion is signalled with both an icon/shape change and a colour
  change, never colour alone (`docs/design/ACCESSIBILITY.md`).
* **Dialog**: reserved for short, focused decisions (reset confirmation,
  custom Tasbih target). The custom Tasbih target dialog is a small
  numeric-input dialog, never a full-screen form (decision J).
* **Bottom sheet**: reader appearance settings and the reader Table of
  Contents are both modal bottom sheets, not full navigation destinations
  (FR-008, FR-017) — this is already the pattern `ReaderSettingsSheet`
  established; the Table of Contents sheet follows the same convention.

## Reader mode action

The Full Reader repetition shortcut ("Dibaca N kali · Buka Panduan →",
FR-018) must look interactive (e.g. an underline, arrow glyph, or tonal
label) but must not visually outweigh the Arabic text it sits beside. It
is a secondary action, styled closer to a caption/label than a button.

## Tasbih target hierarchy

On the Standalone Tasbih screen (`0.0.2`), visual weight ranks: (1) the
main count, (2) the target, (3) the compact target selector, (4) the
optional session name. The target selector itself is compact (segmented
control or similar), not large preset cards — large preset cards were
considered and rejected for this screen (decision J).

## Motion

Existing pattern: a short, fixed-duration fade for step transitions
(`AnimatedContent` in `GuidedReaderScreen.kt`). Keep new motion equally
restrained — no decorative or elaborate animation anywhere in the app.
Respect the system's reduced-motion / animator-scale setting once a
reduced-motion signal exists in the app (`docs/design/ACCESSIBILITY.md`) —
this is a known gap (Milestone 4), not yet fixed.

## Previews and screenshot testing

* Add `@Preview` composables for every reusable visual component, using
  realistic sample content (not `"Lorem ipsum"` or single-word placeholders)
  so a reviewer can actually judge the visual result.
* Once reader screens exist, add Roborazzi-based screenshot tests (see the
  installed `testing-setup` skill's reference on Compose screenshot
  testing) covering: light theme, dark theme, RTL, and font scale `1.5` at
  minimum, for every screen added to `feature/`.
