# Future Releases Design Spec — 0.0.2 to 0.0.5

**Renumbered from "0.0.2 to 0.4.0"** (product owner/tech lead decision,
2026-07-29, ADR
[0013](../../../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md)):
Nahwu Quiz moved from `0.4.0` to `0.0.5`, and navigation across this whole
range is **bottom-navigation-only** — no Navigation Rail on any window-size
class, including expanded/tablet. Every "Rail" reference below is
superseded by that decision; see the Navigation IA table and design-tool
output plan sections below for the corrected model.

## Status

**The design tool's MCP connection is rate-limited on the team's Starter plan**
(confirmed 2026-07-29 — `get_metadata`/`whoami` both rejected with a
tool-call-limit error, matching the same blocker `docs/design/DESIGN_HANDOFF.md`
recorded on 2026-07-26). No page, frame, component, or node has been created
in the live design-tool file. This folder is the complete offline build
specification for that work instead — screens, states, layer names, token
bindings, component variants, icon names, and interaction annotations for
every item in scope — written so a future session with working design-tool
access can execute it directly as `use_design_tool` scripts with no
re-discovery needed. See `06-validation-matrix.md` for the frame ledger
(names planned now, real node IDs filled in once built).

Nothing in this pass touches the live design-tool file, `docs/PROGRESS.md`,
or any engineering doc — design assets only, per the request.

## Scope

In scope — design assets only, no Android code:

1. **0.0.2 — Standalone Tasbih**
2. **0.0.3 — Aktivitas**
3. **0.0.4 — Pengingat Amaliyah**
4. **0.0.5 — Nahwu Quiz (individual/offline)** (moved from `0.4.0`, ADR 0013)

Explicitly out of scope — no screens, no placeholder nav destinations, no
disabled-state affordances for any of the following: `0.1.0` Accounts/Profil,
`0.2.0` Pesantren Membership, `0.3.0` Private Pesantren Space, login,
registrasi, membership, komunitas, chat, pesantren announcements, inter-
pesantren leaderboard/representation, monetisasi/iklan/subscription,
standalone Quran/Quran API/Quran audio.

## Source of truth, in priority order

1. This request's explicit instructions (states, fields, exclusions).
2. The **actual Compose design-system source** —
   `app/src/main/java/com/sangusantri/app/core/designsystem/theme/{Color,Theme,
   SanguSantriSpacing,Shape,SanguSantriElevation,Type}.kt` — verified below
   against the exported design-tool node data, not assumed.
3. The 12 already-exported revised frames in this same directory (`*.json` +
   `*.png`), captured via `get_design_context`/`get_metadata` before the rate
   limit hit. Their structure, naming convention, and token usage is the
   pattern this spec extends — no new visual language invented.
4. `docs/design/DESIGN_SYSTEM.md`, `docs/design/ACCESSIBILITY.md`,
   `docs/design/DESIGN_HANDOFF.md`, `docs/reviews/design-product-alignment.md`.

Reference frames reused (unchanged, not modified — new work only links to
them, per the request):

| Node ID                               | Name                                     |
|---------------------------------------|------------------------------------------|
| `19:2`                                | Beranda Future-Proof (entry points only) |
| `19:84`                               | Jelajahi Amaliyah                        |
| `17:2`                                | Standalone Tasbih                        |
| `17:32`                               | Custom Tasbih Target Dialog              |
| `14:2` / `14:32`                      | Reader (Full / Guided)                   |
| `16:2` / `16:45` / `16:89` / `16:148` | Reader menus/sheets                      |

## Design tokens — verified against real code, not invented

Every color below was cross-checked two ways: (a) extracted directly from the
`fills`/`boundVariables` of all 12 exported design-tool frames (script: walk every
node's `fills`/`strokes`, resolve `VariableID:*` aliases to their concrete
`color`), and (b) matched against the actual constants already shipped in
`Color.kt`/`Theme.kt`. All ten resolved to an exact existing token — nothing
new was invented, and no gold-accent variable exists in either the exported
frames or `Color.kt` yet (`DESIGN_SYSTEM.md` already flags this as an
un-built Phase A/B token; this pass does not add it — none of 0.0.2–0.0.5
requires it).

| Design-tool `VariableID` | Hex       | Compose token (`Color.kt`)                 | Material 3 role                            | Seen on (examples)                                      |
|--------------------|-----------|--------------------------------------------|--------------------------------------------|---------------------------------------------------------|
| `3:5`              | `#FBFDF7` | `SantriNeutral99`                          | `background`                               | Screen background (top-level frames)                    |
| `3:6`              | `#FFFDF8` | `SantriSurface`                            | `surface`                                  | Cards, bottom sheets, dialogs                           |
| `3:7`              | `#F1F1EB` | `SantriNeutral95`                          | `surfaceVariant`                           | Input fields, progress track                            |
| `3:8`              | `#0B6E3B` | `SantriGreen40`                            | `primary`                                  | Active switch, primary text/icon accents                |
| `3:9`              | `#FFFFFF` | (white; via `onPrimary`≈`SantriNeutral99`) | `onPrimary` (button label on filled green) | Filled-button labels                                    |
| `3:10`             | `#D7F8DF` | `SantriGreen95`                            | `primaryContainer`                         | Highlighted cards, active nav pill, counter tap target  |
| `3:11`             | `#00391C` | `SantriGreen20`                            | `onPrimaryContainer`                       | Text/numerals on green-tinted surfaces (counter digits) |
| `3:12`             | `#1A1C19` | `SantriNeutral10`                          | `onSurface`                                | Primary body/heading text                               |
| `3:13`             | `#59605A` | `SantriNeutral40`                          | `onSurfaceVariant`                         | Secondary/caption text, muted icons                     |
| `3:15`             | `#C3C8C0` | `SantriOutline`                            | `outline`                                  | Hairline borders, dividers, drag handles                |

Dark-mode values: no dark-mode design-tool frame has ever been exported (same gap
`Theme.kt`'s own code comment records). Use `Theme.kt`'s existing
`DarkColorScheme` mapping as-is (`SantriGreen80`/`SantriGreen20`/
`SantriGreen30`/`SantriGreen90`/`SantriNeutral10`/`SantriNeutral90`) — do not
invent new dark values; this spec's dark-mode frames apply that exact
existing scheme.

Error color (validation states, e.g. reminder-time conflicts, quiz
content-unavailable): `SantriError40 #BA1A1A` (light) / `SantriError80
#FFB4AB` (dark), `onError` = `SantriNeutral99`/`SantriError10`.

### Typography

`Type.kt`'s existing scale is reused as-is — no new type styles needed for
any 0.0.2–0.0.5 screen (all Latin/Indonesian UI text, no new Arabic content
beyond the existing reader):

| Style           | Weight   | Size / Line height | Used for                                    |
|-----------------|----------|--------------------|---------------------------------------------|
| `headlineSmall` | SemiBold | 24 / 30sp          | Screen titles (Tasbih counter, Quiz result) |
| `titleLarge`    | SemiBold | 20 / 26sp          | Section headers, dialog titles              |
| `titleMedium`   | Medium   | 17 / 22sp          | Card titles, list-row primary text          |
| `bodyLarge`     | Regular  | 16 / 24sp          | Body copy, question text                    |
| `bodyMedium`    | Regular  | 14 / 20sp          | Secondary row text, helper text             |
| `labelLarge`    | Medium   | 14 / 20sp          | Buttons, chips, nav labels                  |

The exported frames additionally show a **78sp/58sp SemiBold numeral style**
used only for the Tasbih/Guided counter digit itself (`fontSize: 78`,
`fontWeight: 600`, observed in `17:13`) — this is a one-off display numeral,
not a named `Type.kt` style; document it in the Tasbih spec as
`counterDisplay` (SemiBold, 72sp compact / 96sp expanded, matching the
existing Guided Reader counter's own scaling rule) rather than adding it to
the shared type ramp speculatively.

### Spacing (`SanguSantriSpacing`)

`extraSmall=4dp, small=8dp, medium=12dp, default=16dp, large=24dp,
extraLarge=32dp` — matches the exported frames' observed padding/gap values
(4/8/9~10/12/14~16/18/24dp cluster around this scale; treat 9/14/18 in the
raw export as designer-rounding noise around 8/16/16, not a separate token).

### Shape (`SanguSantriShapes`)

`small=8dp` (compact controls, chips), `medium=12dp` (cards), `large=20dp`
(sheets/dialogs), `extraLarge=percent(50)` (pills — every stadium-shaped
element in the exported frames: presets, counters, active nav indicator, TOC
pills) — reused exactly, matches the raw corner-radius cluster at
16–24dp for medium containers and the 75/123px full-stadium radii observed
on pill-shaped nodes.

### Elevation

`flat=0dp` + `outlineWidth=1dp` hairline border — no shadow elevation
anywhere in this spec, matching the existing `AmaliyahCard` policy.

## Icon inventory — Material Symbols Rounded only

The 12 exported frames used placeholder Unicode glyphs (`⌂ ◷ ○ ⌘ ◎ ≡ ⋮ ⌕ ▦ ⇄
− +`) as icon stand-ins — an explicit anti-pattern for this pass (per the
request). Every icon below is a named Material Symbols Rounded icon instead,
each mapped 1:1 to what it replaces so Compose can consume it directly via
`androidx.compose.material.icons` / a bundled Material Symbols font resource.

| Icon name (Material Symbols Rounded)                                       | Fill states                                 | Used for                                                          |
|----------------------------------------------------------------------------|---------------------------------------------|-------------------------------------------------------------------|
| `home`                                                                     | outlined / filled                           | Bottom nav — Beranda                                              |
| `history`                                                                  | outlined / filled                           | Bottom nav — Aktivitas                                            |
| `radio_button_checked` (nav) or custom `tasbih_beads` vector (recommended) | outlined(`radio_button_unchecked`) / filled | Bottom nav — Tasbih                                               |
| `notifications` / `notifications_active`                                   | outlined / filled                           | Pengingat entry points, active-reminder state                     |
| `calendar_month`                                                           | outlined                                    | Reminder date field, Hijri/Gregorian display                      |
| `schedule`                                                                 | outlined                                    | Reminder time field                                               |
| `repeat`                                                                   | outlined                                    | Reminder recurrence field                                         |
| `edit`                                                                     | outlined                                    | Edit reminder                                                     |
| `delete`                                                                   | outlined                                    | Delete reminder                                                   |
| `search`                                                                   | outlined                                    | (existing, Jelajahi — referenced, not rebuilt)                    |
| `tune`                                                                     | outlined                                    | Aktivitas "Lihat semua" filter entry                              |
| `restart_alt`                                                              | outlined                                    | Tasbih reset action                                               |
| `bookmark` / `bookmark_border`                                             | filled / outlined                           | Session-history save affordance                                   |
| `check_circle`                                                             | filled                                      | Target-reached / correct-answer state (never color alone)         |
| `cancel`                                                                   | filled                                      | Incorrect-answer state (never color alone)                        |
| `lock_clock`                                                               | outlined                                    | Resume/unfinished-quiz indicator                                  |
| `wifi_off` / `cloud_off`                                                   | outlined                                    | Offline-ready state (Quiz, Pengingat local-first note)            |
| `error_outline`                                                            | outlined                                    | Content-unavailable / error states                                |
| `arrow_back`, `close`, `chevron_right`, `chevron_left`, `expand_more`      | outlined                                    | Navigation chrome (mirror in RTL — see `06-validation-matrix.md`) |
| `add`                                                                      | outlined                                    | Create reminder / new session name                                |
| `settings`                                                                 | outlined                                    | (existing, unrelated to this pass)                                |

**Recommendation for the Tasbih nav icon**: a small custom "tasbih beads"
vector (a simple arc of 5–6 filled/outlined circles around one larger bead) —
Material Symbols has no dedicated tasbih glyph, and `radio_button_checked`
alone reads as a generic radio input, not a prayer-counter concept, at 24dp
in a nav bar. Ship as a named vector component (`icon/tasbih`,
outlined + filled variants) documented in
`01-navigation-and-shared-components.md`, with `radio_button_checked` /
`radio_button_unchecked` recorded as the zero-effort fallback if a custom
vector is deferred.

## Navigation IA per release (progressive, per request; bottom-navigation-

only through 0.0.5, ADR 0013 — Rail column removed)

| Release  | Bottom nav items, in order (every window-size class, including expanded) |
|----------|--------------------------------------------------------------------------|
| `0.0.2`  | Beranda · Tasbih (2 destinations)                                        |
| `0.0.3`+ | Beranda · Aktivitas · Tasbih (3 destinations)                            |

No Navigation Rail is built for any release in this range, on any
window-size class including expanded/tablet — this table's earlier
"Bottom nav (compact) / Rail (expanded)" column header is superseded by
the product owner/tech lead's bottom-navigation-only decision (ADR 0013).
Pengingat and Nahwu Quiz are never bottom-nav destinations at any release
in scope — reachable only via the entry points enumerated in their own
spec files (quick actions, Beranda sections, Aktivitas sections).
Pesantren and Profil are never shown, including disabled — no nav item is
built for them at all (not hidden, not present-but-inert).

## Design-tool output plan (for when design-tool access returns)

* **New page**: `Future Releases — 0.0.2 to 0.0.5` (renamed from `0.4.0` —
  Nahwu Quiz moved to `0.0.5`, ADR 0013).
* **Sections** (design-tool `SectionNode`, left-to-right): `0.0.2 — Tasbih` ·
  `0.0.3 — Aktivitas` · `0.0.4 — Pengingat` · `0.0.5 — Nahwu Quiz` ·
  `Components` · `Navigation` · `Light/Dark/RTL/Expanded validation`. No
  `Nav / Rail` frame or `Navigation Rail Item` component is built in the
  `Navigation`/`Components` sections — bottom-navigation-only (ADR 0013).
* **Frame sizing**: compact phone `360×800`, expanded/tablet `1280×800`,
  matching the request exactly. Every screen gets at minimum one compact
  frame; expanded is built for screens where layout genuinely changes
  (list/grid reflow, centred max-width content column — never a rail
  swap, bottom-navigation-only through `0.0.5`, ADR 0013) — not a
  mechanical duplicate of every state at both widths, to avoid an
  unreviewable frame count. See each release file's "Expanded" note.
* **Frame naming convention** (matches the existing exported frames'
  `Concept / State` pattern): `<Screen> / <State>`, e.g. `Tasbih / Active
  Session`, `Tasbih / Target Reached`, `Pengingat / List Kosong`.
* **Every frame**: Auto Layout throughout (no absolute positioning except
  overlays/dialogs), variables/tokens bound per the table above (never a
  hardcoded hex/dp), semantic layer names (`Section / Subsection` pattern,
  not `Frame 42`), and — where relevant — a small annotation text node or
  design-tool comment-pin describing interaction/haptic/accessibility
  behavior that a static frame can't show.
* **Components page**: one dedicated sub-area per component with its full
  variant grid, following `design-generate-library`'s one-page-per-component-
  family default, componentized (not flat one-off frames) per that skill's
  Phase 3 rules.

## Execution path once the design tool is reachable

1. Re-run `whoami`/`get_metadata` to confirm the rate limit has cleared.
2. Skip re-discovery — `search_design_system`/variable and font extraction
   above is already complete and verified against real code; import the ten
   color variables and Inter font directly by the IDs/family recorded above.
3. Create the page and sections (one `use_design_tool` call).
4. Build `01-navigation-and-shared-components.md`'s components first
   (dependency order — every screen instances these).
5. Build each release frame-by-frame per its own spec file, one section per
   `use_design_tool` call, `get_screenshot` validating after each — per
   `design-generate-design`'s Step 4 workflow.
6. Fill in the frame/node-ID ledger in `06-validation-matrix.md` with real
   IDs as each frame is created.
