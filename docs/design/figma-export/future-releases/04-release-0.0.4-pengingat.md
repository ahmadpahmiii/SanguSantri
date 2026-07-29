# 0.0.4 — Pengingat Amaliyah

Pengingat is **never** a bottom-nav/rail destination (explicit rule,
verified against `00-overview-and-tokens.md`'s navigation table). All 11
screens/states below are reached only from the three entry points listed.

## Entry points (built as small inline additions to existing/other frames,

not full re-frames of them — per "jangan mengubah frame reader atau Beranda
existing kecuali membuat salinan")

* **`Beranda / Entry Point Copy — Quick Action Pengingat`**: a duplicate of
  the relevant slice of `19:2` (Beranda) showing one new Quick Action chip
  ("Pengingat", `notifications` icon) added to Beranda's existing quick-
  action row — a **copy**, clearly labeled `(salinan entry-point, bukan
  revisi 19:2)`, not an edit to the real `19:2` frame.
* **`Aktivitas / Entry Point Copy — Section Pengingat`**: a duplicate slice
  of the `Aktivitas / Data Lengkap` frame (03-release file) with one
  additional section, "Pengingat", inserted after "Riwayat Tasbih" —
  up to 3 `Reminder Row` instances + "Lihat semua" → the reminder list
  (state 2 below). Only shown once real reminder data exists, matching
  every other Aktivitas section's hide-if-empty rule.
* **`Beranda / Entry Point Copy — Pengingat Terdekat`**: a duplicate slice
  of `19:2` with a new section "Pengingat terdekat" (state 10 below).

## Screens and states (11, per the request)

1. **`Pengingat / Daftar Kosong`** — list screen, top app bar "Pengingat" +
   `add` action (48dp target) → Buat Pengingat. Body: `Status State
   (Kind=Empty)`, icon `notifications`, heading "Belum ada pengingat",
   body "Buat pengingat pertamamu, atau pilih dari preset.", one action
   button "Lihat preset" → Preset Picker (state 3).
2. **`Pengingat / Daftar Aktif`** — vertical list of `Reminder Row`
   instances (mixed active/inactive), `add` FAB or top-bar action to create
   new. Same top app bar as state 1.
3. **`Pengingat / Preset Picker`** — modal bottom sheet (matches the
   existing `ReaderSettingsSheet`/TOC-sheet convention — a sheet, not a
   full screen), drag handle (`SantriOutline`), heading "Pilih preset",
   two preset rows: **Tahlil malam Jumat** and **Istighosah mingguan**
   (the two mandatory presets from the request), each showing the preset's
   default day/time/recurrence as secondary text, tap → pre-fills the
   Reminder Schedule Form (state 4) with those values, still editable
   before saving. A "Buat kustom" row at the bottom skips presets entirely.
4. **`Pengingat / Buat Pengingat`** — full `Reminder Schedule Form`
   component (from `01-navigation-and-shared-components.md`) on its own
   screen (top app bar "Pengingat baru" + `close`/back action), all fields
   empty except when arrived via a preset.
5. **`Pengingat / Edit Pengingat`** — same form, pre-filled with an
   existing reminder's values, top app bar "Edit pengingat", plus a
   `delete` action in the top bar (opens state 6).
6. **`Pengingat / Hapus Konfirmasi`** — `Confirmation Dialog Shell`
   (shared component), heading "Hapus pengingat?", body names the specific
   amaliyah + schedule being deleted, actions `Batal` / `Hapus`
   (destructive-tinted).
7. **`Pengingat / Rationale Izin Notifikasi`** — `Permission State
   (Kind=Rationale)` shown the first time a user tries to save a reminder
   before the OS permission has ever been requested.
8. **`Pengingat / Izin Ditolak`** — `Permission State (Kind=Denied)`,
   shown after the OS prompt is dismissed/denied once; the in-progress
   reminder being created is not discarded — annotate that the form state
   is preserved so retrying doesn't lose the user's input.
9. **`Pengingat / Izin Ditolak Permanen`** — `Permission State
   (Kind=PermanentlyDenied)`, action deep-links to Android Settings
   (annotated interaction, not a real link in the static frame).
10. **`Beranda / Pengingat Terdekat`** — see Entry point above: a section
    showing the single next-upcoming reminder as one `Reminder Row`-style
    entry (or a short "Tidak ada pengingat mendatang" line when none exist
    — this line only appears inside this specific section, since it's
    reporting a real absence of upcoming data, not a generic empty-state
    card) with a "Lihat semua" → Pengingat list.
11. **`Aktivitas / Pengingat History atau Summary`** — see Entry point
    above: the Aktivitas section itself (up to 3 `Reminder Row`s +
    "Lihat semua"). No separate "history of fired notifications" concept is
    invented here — the request's field list (schedule fields only) does
    not describe a firing log, so this section shows current schedules,
    not a notification-delivery history.

## Local-first annotation (every relevant frame)

Every screen above that shows or edits a schedule (states 2, 4, 5, 10, 11)
carries the same annotation used in the Reminder Schedule Form: schedules
are local-first and rescheduled automatically after device reboot — no
"remind me later" affordance exists anywhere in this spec (verified: no
snooze/defer control on any of the 11 states or the shared `Reminder Row`/
`Reminder Schedule Form` components).

## Expanded (1280×800) note

Built for states 2 (`Daftar Aktif`) and 4 (`Buat Pengingat`) — list reflow
and form-width constraint are the only genuinely width-sensitive cases;
states 3/6/7/8/9 are sheets/dialogs that behave identically at both widths
(centered overlay, same max width) and are not re-framed.
