# Light / Dark / RTL / Expanded Validation, and the Frame Ledger

## Light / Dark

Light values are the ten verified tokens in `00-overview-and-tokens.md`.
Dark values reuse `Theme.kt`'s existing `DarkColorScheme` exactly (no new
dark hex invented, since no dark-mode design-tool frame has ever been exported):

| Role                       | Light                             | Dark                            |
|----------------------------|-----------------------------------|---------------------------------|
| `background`/`surface`     | `SantriNeutral99`/`SantriSurface` | `SantriNeutral10`               |
| `onBackground`/`onSurface` | `SantriNeutral10`                 | `SantriNeutral90`               |
| `primary`                  | `SantriGreen40`                   | `SantriGreen80`                 |
| `onPrimary`                | `SantriNeutral99`                 | `SantriGreen20`                 |
| `primaryContainer`         | `SantriGreen95`                   | `SantriGreen30`                 |
| `onPrimaryContainer`       | `SantriGreen20`                   | `SantriGreen90`                 |
| `surfaceVariant`           | `SantriNeutral95`                 | `SantriGreen20`                 |
| `onSurfaceVariant`         | `SantriNeutral40`                 | `SantriGreen90`                 |
| `outline`                  | `SantriOutline`                   | `SantriGreen30`                 |
| `error`/`onError`          | `SantriError40`/`SantriNeutral99` | `SantriError80`/`SantriError10` |

Every component in `01-navigation-and-shared-components.md` and every
release file binds to these **role names**, never a literal hex — so a
single design-tool variable mode-swap (Light/Dark) produces every dark frame,
matching how `SanguSantriTheme` already switches schemes in code. Build
**one explicit dark-mode frame per release** as spot-checks rather than a
mechanical duplicate of all ~40 states, since the binding is what
guarantees correctness, not a rebuilt frame:

* `Tasbih / Sesi Aktif — Dark`
* `Aktivitas / Data Lengkap — Dark`
* `Pengingat / Daftar Aktif — Dark`
* `Nahwu Quiz / Pertanyaan — Dark`

## RTL

Applies functionally, not just visually (`docs/design/ACCESSIBILITY.md`):

* **Mirror**: `chevron_right`/`chevron_left` (Section Header "Lihat semua",
  list-row disclosure), `arrow_back`/forward navigation icons, the Target
  Selector/Preset Group's reading-order (chip order follows text direction),
  progress-bar fill direction (`Quiz Progress Indicator`, Quiz Package
  Card's progress bar), the Bottom Nav/Rail item order (destination order
  follows reading direction, per `00-overview-and-tokens.md`).
* **Do not mirror**: `check_circle`, `cancel`, `notifications`/
  `notifications_active`, `calendar_month`, `schedule`, `repeat`, `edit`,
  `delete`, `radio_button_checked`/custom tasbih icon, the counter digits
  themselves — none of these encode directionality.
* **Text alignment**: every field/label in this spec uses
  leading/trailing (start/end) alignment, never hardcoded left/right — the
  Reminder Schedule Form's field icons stay on the *leading* edge in both
  directions (leading = right edge in RTL).
* Build **one explicit RTL frame per release** as a spot-check (same set as
  the dark-mode spot-checks above), each mirrored via the design tool's own
  layout-direction flip on an Auto Layout frame set up with `start`/`end`
  alignment — not a hand-mirrored duplicate.

## Expanded (1280×800 / tablet)

Already scoped per-release in each file's "Expanded note" section
(2 (Tasbih), 1 (Aktivitas), 2 (Pengingat), 2 (Quiz) = 7 expanded frames
total) — built only where width genuinely changes composition (list/grid
reflow, counter/question column recentring with a constrained max width;
the bottom navigation bar stays a bottom bar at every width — no
navigation rail, ADR 0013). The remaining ~33 compact-only
states are compositionally identical when simply recentered wider, so are
not mechanically re-framed — this keeps the frame count reviewable instead
of quadrupling every state for combinatorial completeness with no visual
difference to actually review.

## Font scale 1.5× and touch targets

* Every text node in every component uses `textAutoResize: "WIDTH_AND_HEIGHT"`
  or `"HEIGHT"` (never a fixed clipped height) so a 1.5× scale grows the
  container instead of clipping — verified as the pattern already used in
  the baseline `17:2`/`17:32` export (every `TEXT` node observed used
  `HUG`/`WIDTH_AND_HEIGHT` sizing, none fixed).
* The `Tasbih Counter`'s `counterDisplay` numeral and the `Result Summary`
  score both sit inside a `HUG`-sized pill/container, not a fixed circle —
  at 1.5× scale the container grows rather than the digits overflowing it.
* Chip/label text (`Preset Chip`, `Quiz Package Card` status chips) is
  allowed to wrap to two lines rather than truncate at large font scale —
  annotate `textTruncation: none` on these specifically, since a truncated
  status label would hide the exact non-color signal
  `ACCESSIBILITY.md` requires.
* **Touch target audit** — every interactive element in this spec is
  confirmed ≥48×48dp: Bottom Nav/Rail items, Preset Chips (padded beyond
  visible chip bounds), the Tasbih Counter (220dp+, trivially compliant),
  Section Header's "Lihat semua", Reminder Row's switch + edit/delete
  affordance, Quiz Answer Options (full-width rows, well over 48dp tall),
  all dialog actions.

## Color-is-not-the-only-signal audit

Every state that communicates status pairs a color change with a second
signal (icon, shape, or text) — recap:

| Status                          | Color change                                        | Second signal                                       |
|---------------------------------|-----------------------------------------------------|-----------------------------------------------------|
| Tasbih target reached           | `primaryContainer`→`primary`                        | `check_circle` icon + caption text change           |
| Quiz correct answer             | fill→`primaryContainer`                             | `check_circle` icon + "Benar!" caption              |
| Quiz incorrect answer           | fill→error tint                                     | `cancel` icon + "Kurang tepat" caption              |
| Reminder active/inactive        | none required (switch itself is the primary signal) | `notifications_active` vs `notifications` icon swap |
| Numeric dialog validation error | border→error tint                                   | inline error text, not color alone                  |
| Nav selected/unselected         | icon tint change                                    | filled vs. outlined icon swap + pill container      |

## Frame / node ID ledger

**Navigation Rail removed from this ledger** (product owner/tech lead
decision, 2026-07-29, ADR
[0013](../../../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md))
— bottom navigation only through `0.0.5`, on every window-size class
including expanded/tablet; `Nav / Rail` and `Navigation Rail Item` below
are no longer part of the active spec (struck from the checklist, not
merely left `PENDING`). Every remaining row is a **planned** frame name in
the new `Future Releases — 0.0.2 to 0.0.5` page (renamed from `0.4.0` —
Nahwu Quiz moved to `0.0.5`, same ADR). `Node ID` is `PENDING` until a
session with working design-tool access actually creates it — this table is
the checklist that session fills in, not a claim that these nodes exist yet.

### Section: Navigation

| Frame / Component                 | Node ID |
|-----------------------------------|---------|
| Nav / Bottom Bar (Destinations=2) | PENDING |
| Nav / Bottom Bar (Destinations=3) | PENDING |

### Section: Components

| Component set                   | Node ID |
|---------------------------------|---------|
| Bottom Navigation Item          | PENDING |
| icon/tasbih (outlined + filled) | PENDING |
| Section Header                  | PENDING |
| Summary Metric                  | PENDING |
| Activity Row                    | PENDING |
| Reminder Row                    | PENDING |
| Reminder Schedule Form          | PENDING |
| Permission State                | PENDING |
| Quiz Package Card               | PENDING |
| Quiz Answer Option              | PENDING |
| Quiz Progress Indicator         | PENDING |
| Result Summary                  | PENDING |
| Status State                    | PENDING |
| Confirmation Dialog Shell       | PENDING |
| Tasbih Counter                  | PENDING |
| Target Selector / Preset Chip   | PENDING |

### Section: 0.0.2 — Tasbih (9 states + 6 dialog validation frames + entry

notes)

| Frame                                      | Node ID |
|--------------------------------------------|---------|
| Tasbih / Belum Ada Sesi                    | PENDING |
| Tasbih / Sesi Aktif                        | PENDING |
| Tasbih / Sesi Aktif — Expanded             | PENDING |
| Tasbih / Sesi Aktif — Dark                 | PENDING |
| Tasbih / Sesi Aktif — RTL                  | PENDING |
| Tasbih / Target Tercapai                   | PENDING |
| Tasbih / Target Tercapai — Expanded        | PENDING |
| Tasbih / Target Tanpa Batas                | PENDING |
| Tasbih / Custom Target Dialog (Valid)      | PENDING |
| Tasbih / Custom Target Dialog (Empty)      | PENDING |
| Tasbih / Custom Target Dialog (Zero)       | PENDING |
| Tasbih / Custom Target Dialog (Negative)   | PENDING |
| Tasbih / Custom Target Dialog (NonNumeric) | PENDING |
| Tasbih / Custom Target Dialog (TooLarge)   | PENDING |
| Tasbih / Reset Confirmation                | PENDING |
| Tasbih / Riwayat Kosong                    | PENDING |
| Tasbih / Riwayat Kosong — Expanded         | PENDING |
| Tasbih / Riwayat Terisi                    | PENDING |
| Tasbih / Riwayat Terisi — Expanded         | PENDING |
| Tasbih / Sesi Dipulihkan                   | PENDING |

### Section: 0.0.3 — Aktivitas (7 states)

| Frame                                  | Node ID |
|----------------------------------------|---------|
| Aktivitas / Semua Data Kosong          | PENDING |
| Aktivitas / Hanya Amaliyah             | PENDING |
| Aktivitas / Hanya Tasbih               | PENDING |
| Aktivitas / Data Parsial               | PENDING |
| Aktivitas / Data Lengkap               | PENDING |
| Aktivitas / Data Lengkap — Expanded    | PENDING |
| Aktivitas / Data Lengkap — Dark        | PENDING |
| Aktivitas / Long History (Detail list) | PENDING |
| Aktivitas / Filter Lihat Semua         | PENDING |

### Section: 0.0.4 — Pengingat (11 states + 3 entry-point copies)

| Frame                                               | Node ID |
|-----------------------------------------------------|---------|
| Beranda / Entry Point Copy — Quick Action Pengingat | PENDING |
| Pengingat / Daftar Kosong                           | PENDING |
| Pengingat / Daftar Aktif                            | PENDING |
| Pengingat / Daftar Aktif — Expanded                 | PENDING |
| Pengingat / Daftar Aktif — Dark                     | PENDING |
| Pengingat / Preset Picker                           | PENDING |
| Pengingat / Buat Pengingat                          | PENDING |
| Pengingat / Buat Pengingat — Expanded               | PENDING |
| Pengingat / Edit Pengingat                          | PENDING |
| Pengingat / Hapus Konfirmasi                        | PENDING |
| Pengingat / Rationale Izin Notifikasi               | PENDING |
| Pengingat / Izin Ditolak                            | PENDING |
| Pengingat / Izin Ditolak Permanen                   | PENDING |
| Beranda / Pengingat Terdekat                        | PENDING |
| Aktivitas / Pengingat History atau Summary          | PENDING |

### Section: 0.0.5 — Nahwu Quiz (15 states + entry point + BLOCKED note; moved from 0.4.0, ADR 0013)

| Frame                                        | Node ID |
|----------------------------------------------|---------|
| Beranda / Entry Point Copy — Section Belajar | PENDING |
| Nahwu Quiz / Landing                         | PENDING |
| Nahwu Quiz / Daftar Paket                    | PENDING |
| Nahwu Quiz / Daftar Paket — Expanded         | PENDING |
| Nahwu Quiz / Detail Paket                    | PENDING |
| Nahwu Quiz / Instruksi                       | PENDING |
| Nahwu Quiz / Pertanyaan                      | PENDING |
| Nahwu Quiz / Pertanyaan — Expanded           | PENDING |
| Nahwu Quiz / Pertanyaan — Dark               | PENDING |
| Nahwu Quiz / Jawaban Dipilih                 | PENDING |
| Nahwu Quiz / Feedback Benar                  | PENDING |
| Nahwu Quiz / Feedback Salah                  | PENDING |
| Nahwu Quiz / Hasil Kuis                      | PENDING |
| Nahwu Quiz / Riwayat Skor Individual         | PENDING |
| Nahwu Quiz / Lanjutkan Kuis Belum Selesai    | PENDING |
| Nahwu Quiz / Bank Soal Kosong                | PENDING |
| Nahwu Quiz / Offline-Ready                   | PENDING |
| Nahwu Quiz / Konten Tidak Tersedia           | PENDING |
| Nahwu Quiz / BLOCKED — Future Handoff Notes  | PENDING |

**Total planned frames/components**: 18 navigation+component entities
(reduced from 20 — `Nav / Rail` and `Navigation Rail Item` removed,
bottom-navigation-only through `0.0.5`, ADR 0013), 20 Tasbih frames, 9
Aktivitas frames, 15 Pengingat frames, 19 Quiz frames — 81 total, all
currently `PENDING`.
