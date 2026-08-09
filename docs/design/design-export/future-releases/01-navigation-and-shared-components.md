# Navigation and Shared Components

All tokens reference `00-overview-and-tokens.md`. Every component below is a
design-tool component **set** with named variant properties (not a flat frame
per state), built with Auto Layout, ready for `combineAsVariants` per
`design-generate-library`. Minimum touch target is 48×48dp on every
interactive variant (`docs/design/ACCESSIBILITY.md`).

## Bottom navigation bar (compact, 360dp width)

Frame: `Nav / Bottom Bar`, height 80dp, `background` fill, top hairline
`outline` stroke (1dp), Auto Layout horizontal, `spaceBetween`, vertical
padding `small` (8dp).

**Bottom Navigation Item** — component set.
Variants: `Destination={Beranda, Aktivitas, Tasbih}` × `State={Selected,
Unselected}`.

* Structure: Auto Layout vertical, gap `extraSmall` (4dp), width `HUG`,
  min touch target 48×48dp (padding added if the icon+label hugs smaller).
    * **Selected indicator**: a pill/tonal container behind the icon —
      `RoundedCornerShape(percent = 50)` (`SanguSantriShapes.extraLarge`),
      fill `primaryContainer` (`SantriGreen95`), sized to the icon with
      `small` (8dp) horizontal padding, 32dp height.
    * Icon 24dp: filled Material Symbol when `Selected`, outlined when
      `Unselected` (see icon table in `00-overview-and-tokens.md`) —
      `onPrimaryContainer` (`SantriGreen20`) tint when selected, `onSurfaceVariant`
      (`SantriNeutral40`) when unselected. Never rely on the pill alone —
      the icon glyph itself changes (filled vs outlined), satisfying the
      "color is not the only status indicator" rule.
    * Label: always visible (never icon-only, per request), `labelLarge`
      style, same color rule as the icon.
* 3 destinations at `0.0.3`+ (`Beranda`, `Aktivitas`, `Tasbih`); only 2 at
  `0.0.2` (`Beranda`, `Tasbih`) — build both bar widths as two separate
  `Nav / Bottom Bar` frame variants (`Destinations=2`, `Destinations=3`),
  since item count changes item width, not just content.
* RTL: item order mirrors (reading-direction based), icons that are
  inherently directional (none among home/history/tasbih) stay unmirrored;
  chevrons elsewhere in this spec do mirror — see `06-validation-matrix.md`.

## Navigation rail — removed (bottom-navigation-only through 0.0.5)

**Superseded, not built** (product owner/tech lead decision, 2026-07-29,
ADR
[0013](../../../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md)):
this section previously specified a `Nav / Rail` frame (width 96dp, full
height, vertical Auto Layout) and a `Navigation Rail Item` component set
for expanded/tablet width (1280dp). No Navigation Rail is built for any
release through `0.0.5`, on any window-size class including expanded —
`Nav / Bottom Bar` (above) is used unchanged on expanded width too, simply
centred within a constrained max-width column like every other screen in
this spec (`06-validation-matrix.md`'s Expanded note). Left as a struck
section here as the historical record of what this spec originally called
for, per this project's convention of marking superseded content rather
than silently deleting it — see the ADR for the full decision.

## Section Header

Component: `Section Header`. Auto Layout horizontal, `spaceBetween`,
vertical padding `small` (8dp), horizontal padding `default` (16dp).

* Left: title text, `titleLarge`, `onSurface`.
* Right (optional, `HasAction` boolean property): "Lihat semua" text button,
  `labelLarge`, `primary` (`SantriGreen40`) color, trailing `chevron_right`
  16dp icon (mirrors to `chevron_left` in RTL). 48×48dp tap target even
  though the visible label is smaller (padding, not visible box).
* Never wrapped in a `Card` (`DESIGN_SYSTEM.md` anti-pattern rule) — sits
  directly on the screen background.

## Summary Metric

Component: `Summary Metric`. Used by Aktivitas (streak, this-week summary).
Auto Layout vertical, gap `extraSmall` (4dp), no card/border by default
(`Emphasis=Plain`); a `Emphasis=Highlighted` variant adds a `primaryContainer`
background + `medium` (12dp) shape + `default` (16dp) padding, for at most
one hero metric per section (e.g. current streak count) — not every metric,
to avoid a card wall.

* Value: `headlineSmall`, `onSurface` (or `onPrimaryContainer` when
  `Highlighted`).
* Label: `bodyMedium`, `onSurfaceVariant`.
* Optional trend/delta text (`+2 minggu ini`), `labelLarge`, `primary`.

## Activity Row

Component: `Activity Row`. Used by Aktivitas' amaliyah-completion and
tasbih-history sections. Auto Layout horizontal, gap `medium` (12dp),
vertical padding `small` (8dp) — a plain list row, not individually carded
(the section itself may sit on plain background; only the whole "Lihat
semua" destination is a separate screen, not this row).

Variants: `Kind={Amaliyah, Tasbih}`.

* `Kind=Amaliyah`: leading small type icon (24dp, `onSurfaceVariant`),
  primary text = amaliyah name (`titleMedium`), secondary text = "Versi
  {n} · {duration}" (`bodyMedium`, `onSurfaceVariant`), trailing text =
  completion time (`bodyMedium`, `onSurfaceVariant`, right-aligned).
* `Kind=Tasbih`: leading `radio_button_checked`/custom tasbih icon,
  primary text = session name if present else "Tasbih" (`titleMedium`),
  secondary text = "Target {n} · Hitungan akhir {n}" (`bodyMedium`),
  trailing text = time + duration (`bodyMedium`, right-aligned).
* Divider: hairline `outline` stroke below each row except the last
  (`RowDivider` boolean property), not a boxed card.

## Reminder Row

Component: `Reminder Row`. Auto Layout horizontal, gap `medium` (12dp),
vertical padding `small` (8dp), used in the reminder list and Beranda's
"Pengingat terdekat" section.

* Leading: `notifications_active` (24dp, `primary`) when active, or a
  toggle-off variant leading icon `notifications` (`onSurfaceVariant`) when
  inactive.
* Primary text: amaliyah name (`titleMedium`).
* Secondary text: "{Hari/Perulangan} · {Waktu}" plus a smaller
  Gregorian+Hijri date caption (`bodyMedium`, `onSurfaceVariant`) — both
  calendars always shown together, never only one (request's explicit
  field list).
* Trailing: a `Switch` (Material 3, bound to `primary`/`primaryContainer`
  on-state) for active/nonaktif, **and** a 48×48dp overflow affordance
  (`edit`/`delete` reachable via tap — see Pengingat spec for the
  edit/delete surface) — the switch alone is not the only way to reach
  edit/delete, since a switch-only row would bury those actions.

## Reminder Schedule Form

Component: `Reminder Schedule Form` (used inside "Buat pengingat"/"Edit
pengingat"). Auto Layout vertical, gap `default` (16dp), `default` (16dp)
padding all sides, `surface` background, `medium` (12dp) shape (this is a
genuinely bounded, tappable-distinct unit — a sheet/dialog body — so a
container is justified here, unlike a plain list section).

Fields, each its own labeled row (`labelLarge` field label +
`SantriNeutral95` input surface, `small` shape):

1. **Nama amaliyah** — text input, with a `Preset` chip row above it
   (`Tahlil malam Jumat`, `Istighosah mingguan`) that pre-fills the
   remaining fields when tapped (`Preset picker` state, see item 3 below).
2. **Hari/tanggal** — date field, leading `calendar_month` icon, opens a
   date picker; read-only text shows both Gregorian and Hijri
   (`bodyMedium`, `onSurfaceVariant`, e.g. "Jumat, 21 Feb 2026 · 3 Sya'ban
   1447 H").
3. **Waktu** — time field, leading `schedule` icon, opens a time picker.
4. **Perulangan** — segmented control or dropdown, leading `repeat` icon:
   `Sekali`, `Mingguan`, `Bulanan`.
5. **Status aktif/nonaktif** — `Switch`, trailing-aligned, same visual
   token as the Reminder Row switch.
6. Footer annotation text (`bodyMedium`, `onSurfaceVariant`, not a dialog):
   "Jadwal bersifat local-first dan dijadwalkan ulang otomatis setelah
   perangkat dinyalakan kembali." — always visible on this form, not a
   tooltip, since it is a real behavioral constraint the user should see
   before saving.
7. Actions row: `Batal` (text button) + `Simpan` (filled button,
   `primary`/`onPrimary`), right-aligned, both ≥48dp tall.

No "ingatkan nanti" control anywhere on this form or the reminder row
(explicit exclusion).

## Permission State

Component set: `Permission State`. Full-bleed content block (not a dialog)
used inline where the reminder flow needs it.

Variants: `Kind={Rationale, Denied, PermanentlyDenied}`.

* Shared structure: Auto Layout vertical, centered, gap `default` (16dp),
  `large` (24dp) padding. Icon 48dp (`notifications` outlined,
  `onSurfaceVariant`), `titleLarge` heading, `bodyLarge` explanatory text
  (`onSurfaceVariant`), one primary action button.
* `Rationale`: heading "Izinkan notifikasi pengingat", action button "Izinkan"
  (filled, `primary`).
* `Denied`: heading "Notifikasi belum diizinkan", body explains reminders
  won't fire without it, action "Coba lagi" (filled).
* `PermanentlyDenied`: heading "Notifikasi dinonaktifkan permanen", body
  explains the OS blocked future in-app prompts, action "Buka Pengaturan
  Android" (filled) — this variant's action deep-links to system settings,
  annotate this explicitly as an interaction note on the frame since a
  static mock can't show the OS handoff.

## Quiz Package Card

Component: `Quiz Package Card`. This is a genuine bounded/tappable unit —
uses the existing flat, hairline-border card policy (`surface` fill,
`SantriOutline` 1dp border, `medium` shape, `default` padding), matching
`AmaliyahCard`'s existing elevation policy exactly (do not fork a new card
style).

* Title (`titleMedium`), question-count caption ("24 soal",
  `bodyMedium`/`onSurfaceVariant`).
* Progress: a thin linear progress bar (`SantriNeutral95` track,
  `primary` fill) + "12/24 selesai" caption when a package has partial
  progress; hidden entirely when a package has never been started (no
  "0% empty progress bar" — nothing to show yet is nothing shown).
* Status chip variant: `New` (`primaryContainer` chip), `In Progress`
  (outline chip), `Completed` (`check_circle` filled icon + label), or
  `Unavailable` (`onSurfaceVariant`, non-interactive, `wifi_off` icon) —
  color is never the only signal on any chip; each carries a distinct icon
  or label text too.

## Quiz Answer Option

Component set: `Quiz Answer Option`. Auto Layout horizontal, gap `medium`
(12dp), `default` padding, `medium` shape, `SantriOutline` 1dp border by
default.

Variants: `State={Default, Selected, Correct, Incorrect, Disabled}`.

* `Default`: `surface` fill, `outline` border, `onSurface` text.
* `Selected` (post-tap, pre-submit): `primaryContainer` fill, `primary`
  2dp border, no icon yet.
* `Correct`: `primaryContainer` fill, leading `check_circle` filled icon
  (`primary`), `primary` 2dp border — icon + border + fill together, not
  color alone.
* `Incorrect`: `SantriError90` fill, leading `cancel` filled icon
  (`SantriError40`), `SantriError40` 2dp border.
* `Disabled` (a different option after the user has already answered):
  `surface` fill, reduced-opacity `onSurfaceVariant` text, no border
  emphasis — only shown alongside a `Correct`/`Incorrect` sibling so the
  right answer is still visible even when the user picked wrong.

## Quiz Progress Indicator

Component: `Quiz Progress Indicator`. Auto Layout horizontal,
`spaceBetween`, `default` padding.

* Left: "Soal {n} dari {total}" (`labelLarge`, `onSurfaceVariant`).
* Right or full-width below: thin linear bar, `SantriNeutral95` track,
  `primary` fill, `small` shape, matched to the Quiz Package Card's
  progress-bar token so both read as the same concept.

## Result Summary

Component: `Result Summary`. Auto Layout vertical, centered, gap `default`
(16dp), `large` padding.

* Large score display reusing the `counterDisplay` numeral treatment
  documented in `00-overview-and-tokens.md` (same visual weight rule as the
  Tasbih counter — the score is the strongest element on this screen).
* "{correct}/{total} benar" caption (`bodyLarge`, `onSurfaceVariant`).
* Optional delta vs. previous attempt (`labelLarge`, `primary`), only
  shown when a previous individual attempt genuinely exists.
* Actions: "Lihat riwayat skor" (text button) + "Ulangi kuis" (filled
  button) — no share button anywhere (Aktivitas/Quiz statistics are
  private, per the request).

## Empty / Error / Loading State

Component set: `Status State`. Shared shell for every "nothing to show yet"
moment across all four releases (empty riwayat, empty question bank,
content-unavailable, generic loading) — one component family, not a
bespoke empty-state per screen.

Variants: `Kind={Empty, Error, Loading, Offline}`.

* Shared structure: Auto Layout vertical, centered, gap `default`,
  `large` padding, icon 48dp + `titleMedium` heading + `bodyMedium`
  explanatory text (`onSurfaceVariant`), optional single action button.
* `Empty`: icon varies per context (documented per-screen in each release
  file), neutral tone, no action button unless a genuine next step exists
  (e.g. "Tandai favorit" is not invented here — only "Mulai sesi" style
  actions that the screen actually supports).
* `Error`: `error_outline` icon (`SantriError40`), heading + body explain
  the failure, action "Coba lagi".
* `Loading`: circular indeterminate progress indicator (`primary`), no
  heading/body text (a loading state should not require reading).
* `Offline`: `wifi_off` icon, body clarifies the feature still works from
  local data — this is a reassurance state, not an error (see Quiz
  "Offline-ready" screen and Pengingat's local-first note).

## Custom Tasbih Target Dialog trigger and Reset confirmation dialog

Both dialogs are documented in `02-release-0.0.2-tasbih.md` (they are
Tasbih-specific), but their **shared dialog shell** belongs here since the
same shell is reused for Reminder deletion confirmation and any other
short, focused decision (`DESIGN_SYSTEM.md`'s "Dialog: reserved for short,
focused decisions" rule):

Component: `Confirmation Dialog Shell`. Auto Layout vertical, centered
content, gap `default`, `large` (24dp) padding, `surface` fill, `large`
(20dp) shape, no shadow (flat + the dialog scrim itself provides
separation) — `titleLarge` heading, `bodyMedium` body, two actions
right-aligned (`Batal` text button + a destructive/primary filled button,
the destructive variant tinted `SantriError40`/`onError` when the action is
irreversible, e.g. delete/reset).
