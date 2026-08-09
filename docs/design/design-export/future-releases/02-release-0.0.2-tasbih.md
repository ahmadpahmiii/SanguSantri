# 0.0.2 — Standalone Tasbih

Baseline: existing frames `17:2` (Standalone Tasbih) and `17:32` (Custom
Tasbih Target Dialog) — reused unmodified as the structural pattern; this
spec adds every remaining state as sibling frames in the new page's
`0.0.2 — Tasbih` section, never editing `17:2`/`17:32` themselves (per the
request).

## Baseline layer map (from the exported `17:2` structure — verified, not

guessed)

```
Tasbih / <State>                              (Frame, 360×800)
├─ Top App Bar
│  ├─ "Tasbih"                                  (titleLarge, onSurface)
│  └─ Action / Reset                            (restart_alt icon, 48dp target — replaces the ↻ glyph)
└─ Tasbih Content                                (Auto Layout vertical, gap=default, padding=default)
   ├─ Optional Reading Name                      (Auto Layout horizontal, spaceBetween)
   │  ├─ session name text / "Tanpa nama"          (titleMedium)
   │  └─ "Nama opsional" + chevron_right           (labelLarge, onSurfaceVariant, tap → inline text field)
   ├─ Target Header
   │  ├─ "TARGET BACAAN" (labelLarge, onSurfaceVariant, letter-spacing)
   │  └─ "{n} kali" / "Tanpa batas"                (titleLarge, onSurface)
   ├─ Tasbih Counter                               (see Component: Tasbih Counter below)
   ├─ "Pilih target"                               (titleMedium, onSurface)
   ├─ Preset Group                                 (see Component: Target Selector below)
   ├─ auto-save caption                            (bodyMedium, onSurfaceVariant)
   └─ Tasbih secondary actions
      ├─ Preset / Reset                             (labelLarge chip, opens Reset Confirmation Dialog)
      └─ Preset / Riwayat                            (labelLarge chip, navigates to Session History)
```

Hierarchy order matches the request exactly: **hitungan → target → selector
→ nama sesi** is the *visual weight* order (counter largest/boldest, target
second, selector compact, name smallest/most muted) even though "Optional
Reading Name" sits structurally above "Target Header" in the layout — visual
weight is carried by type scale/color, not by vertical position (see
Component: Tasbih Counter).

## Components

### Tasbih Counter

Component set: `Tasbih Counter`. Auto Layout vertical, centered, `primary
Container` (`SantriGreen95`) fill, `extraLarge` (pill/stadium) shape, sized
to be the single largest tappable element on the screen (min 220×220dp
compact, 280×280dp expanded) — the strongest visual element per
`DESIGN_SYSTEM.md`'s Tasbih target hierarchy.

Variants: `State={Counting, TargetReached}`.

* `Counting`: digit text uses `counterDisplay` (SemiBold, 72sp compact /
  96sp expanded — see `00-overview-and-tokens.md`), color
  `onPrimaryContainer` (`SantriGreen20`); helper caption "Ketuk untuk
  menghitung" (`bodyMedium`, `onPrimaryContainer`, reduced opacity).
* `TargetReached`: fill changes to `primary` (`SantriGreen40`, a distinct
  shape/color change), digit color becomes `onPrimary` (white), a
  `check_circle` filled icon (24dp, `onPrimary`) appears above the digits,
  and the helper caption changes to "Target tercapai — ketuk untuk
  mengulang" — icon + fill-color change + caption text together, never a
  color swap alone (`ACCESSIBILITY.md`).
* Interaction annotation (design-tool comment/annotation text node, since a
  static frame can't show motion): "Tap anywhere in the counter to
  increment. Each tap fires `HapticFeedbackType.LongPress` (matches the
  existing `GuidedTasbihCounter` pattern) — no visual-only feedback."
  `stateDescription` accessibility annotation: "{current} dari {target
  atau 'tanpa batas'}, {status}" announced on every change.

### Target Selector

Component: `Target Selector` (`Preset Group`). Auto Layout horizontal,
wrap-capable, gap `small` (8dp) — a **compact segmented/chip row**, never
large preset cards (explicit rejection in the request and
`DESIGN_SYSTEM.md`'s Tasbih hierarchy note).

Chip component set: `Preset Chip`. Variants: `Value={33, 100, Unlimited,
Custom}` × `Selected={True, False}`. Pill shape (`extraLarge`), `default`
horizontal padding, 40dp height (chip itself can be under 48dp visually as
long as its tap target is padded to 48dp — `ACCESSIBILITY.md` minimum).

* `Selected=True`: `primaryContainer` fill, `onPrimaryContainer` text,
  `primary` 1.5dp border.
* `Selected=False`: `surface` fill, `onSurface` text, `outline` 1dp border.
* `Value=Unlimited` label: "∞" replaced with the word "Tanpa batas" as the
  accessible label (the glyph alone is not announced meaningfully) — visible
  chip text may stay compact ("∞") but must carry a real content
  description, not rely on the glyph.
* `Value=Custom` chip always reads "Atur sendiri", opens the Custom Target
  Dialog — never itself carries a `Selected=True` numeric value (the dialog
  result becomes a new numeral shown in Target Header instead, with this
  chip simply the trigger).
* **No 99 preset anywhere** — explicit exclusion, verified against every
  frame in this file.

### Counter Completion State

Covered by `Tasbih Counter`'s `TargetReached` variant above — documented as
a separate line item here only because the request lists it as its own
component: it is not a second component, it is that one variant, plus the
`Target Header` value simultaneously switching to a `Tercapai` label style
(`primary` color instead of `onSurface`) so the completion signal is visible
in two places at once, not just the counter.

### Custom Target Dialog

Baseline: `17:63` (`Dialog / Custom target`) inside `17:32` — reused
structure: `Confirmation Dialog Shell` (see
`01-navigation-and-shared-components.md`) containing a `Numeric Input` field
(leading/trailing "kali" unit label, numeric keyboard `KeyboardType.Number`)
and a `Dialog Actions` row (`Batal` / `Simpan`).

Add **validation states** as a component property `ValidationState={Valid,
Empty, Zero, Negative, NonNumeric, TooLarge}` on the `Numeric Input` sub-
component:

* `Valid`: `outline` border, `Simpan` enabled.
* `Empty` / `Zero` / `Negative` / `NonNumeric`: `SantriError40` 2dp border +
  inline error text below the field (`bodyMedium`, `SantriError40`, e.g.
  "Masukkan angka lebih dari 0", "Hanya angka yang diperbolehkan"), `Simpan`
  disabled — rejected before dismissal is possible, not after
  (`ACCESSIBILITY.md`'s numeric-input rule). Error text is real text, not a
  color-only cue, and is exposed to accessibility services on state change.
* `TooLarge`: same error treatment, message "Target maksimum adalah
  {N_MAX}" (the exact ceiling is an engineering decision, not a design one —
  annotate as `[ENGINEERING: define max target value]` rather than
  inventing a number here).

### Reset Confirmation Dialog

New frame `Tasbih / Reset Confirmation`, built from `Confirmation Dialog
Shell`: heading "Reset hitungan?", body "Hitungan saat ini ({n}) akan
dihapus dan tidak dapat dikembalikan.", actions `Batal` / `Reset`
(destructive-tinted, `SantriError40`/`onError`). Always required before any
reset executes — no direct-reset affordance anywhere else in the spec.

### Session-History Row

Reuses `Activity Row` (`Kind=Tasbih`) from
`01-navigation-and-shared-components.md` exactly — not a new component.

## Screen states (all 9, each a `360×800` frame; states 2/3/7/8 also get a

`1280×800` expanded frame — see Expanded note below)

1. **`Tasbih / Belum Ada Sesi`** — Target Header shows "Pilih target untuk
   memulai"; Tasbih Counter shows "0" in a neutral (`surfaceVariant`, not
   `primaryContainer`) tone since no target is chosen yet; Target Selector
   has no chip selected; secondary actions' `Riwayat` chip is present but
   `Reset` chip is disabled/hidden (nothing to reset yet).
2. **`Tasbih / Sesi Aktif`** — matches baseline `17:2` exactly (target 33,
   count 12, `Counting` variant).
3. **`Tasbih / Target Tercapai`** — `Tasbih Counter` in `TargetReached`
   variant, count equals target (e.g. 33/33), Target Header in the
   `Tercapai` label style.
4. **`Tasbih / Target Tanpa Batas`** — Target Header shows "Tanpa batas",
   `Preset Chip Value=Unlimited` selected, counter has no target ceiling so
   `TargetReached` never triggers for this state — counter always renders
   `Counting`.
5. **`Tasbih / Custom Target Dialog`** — matches baseline `17:32`
   (`ValidationState=Valid`), plus 5 additional dialog-only frames for each
   `ValidationState` variant (`Empty`, `Zero`, `Negative`, `NonNumeric`,
   `TooLarge`) so every validation message is visible somewhere, not just
   documented in prose.
6. **`Tasbih / Reset Confirmation`** — the dialog above the `Sesi Aktif`
   screen (scrim + dialog, matching the `17:32` scrim pattern).
7. **`Tasbih / Riwayat Kosong`** — Session History screen (new destination,
   reached via the `Riwayat` chip), `Status State (Kind=Empty)` component,
   icon `history`, heading "Belum ada riwayat tasbih", body "Sesi yang
   selesai akan muncul di sini.".
8. **`Tasbih / Riwayat Terisi`** — Session History screen, a vertical list
   of `Activity Row (Kind=Tasbih)` instances, most-recent first, each
   showing session name (or "Tasbih" if none), target, final count, time +
   duration — matching the request's exact field list.
9. **`Tasbih / Sesi Dipulihkan`** — visually identical to `Sesi Aktif`
   (state 2) plus a small non-modal indicator row above the counter
   ("Hitungan sebelumnya dipulihkan", `bodyMedium`, `onSurfaceVariant`,
   `lock_clock`-style restore icon) so the "restored after relaunch" case
   is visually distinguishable in the spec — annotate that this indicator
   is transient (shown once per cold start, not persistent chrome) since a
   static frame can't show it fading.

## Expanded (1280×800) note

Built for states 2 (`Sesi Aktif`), 3 (`Target Tercapai`), 7
(`Riwayat Kosong`), 8 (`Riwayat Terisi`) — the states where width actually
changes composition (counter grows per the `counterDisplay` 96sp expanded
rule; content column becomes centered with a constrained max width rather
than stretching edge-to-edge, consistent with the reader's existing
constrained-width rule in `DESIGN_SYSTEM.md`; the bottom navigation bar
stays a bottom bar at every width, including expanded — no navigation
rail, product owner/tech lead decision, ADR 0013). States 1/4/5/6/9 are
compositionally identical at both widths (same
column, same components, just centered wider) and are not mechanically
re-frames to avoid an unreviewable, purely-redundant frame count — note this
explicitly on the Components/validation page instead (see
`06-validation-matrix.md`).
