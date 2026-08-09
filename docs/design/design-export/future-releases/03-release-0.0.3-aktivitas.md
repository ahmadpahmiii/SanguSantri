# 0.0.3 — Aktivitas

No prior frame exists for this screen (`docs/design/DESIGN_HANDOFF.md`'s own
"Known incomplete design-tool areas" note — confirmed, not contradicted, by this
pass). Built new from `docs/reviews/design-product-alignment.md` decision K
and this request's explicit section list, using the same token/component
language as every other frame in this spec — no new visual pattern
invented.

## Screen shell

Frame `Aktivitas / <State>`, `360×800` compact. **One vertical scrollable
column, no horizontal tabs** (explicit request). Top app bar: "Aktivitas"
title only (no back action — this is a top-level nav destination), no
actions beyond a Section Header per section.

Each section:

* A `Section Header` (title + optional "Lihat semua") directly on the
  screen background — never wrapped in a `Card` unless the section's content
  itself is a single bounded unit (none of these are).
* Rendered **only when it has real data** — a section with nothing to show
  renders nothing at all (not an empty-state card, not a hidden-but-present
  placeholder). This is why states 2–4 below exist: they show different
  subsets of sections actually present.

## Sections (independent, in this order)

1. **Ringkasan streak** — one or two `Summary Metric` instances
   (`Emphasis=Highlighted` on the current-streak count, `Plain` on a
   secondary "streak terpanjang" if tracked), no "Lihat semua" (a streak
   has no list to drill into).
2. **Ringkasan minggu ini** — a row of 2–4 `Summary Metric` instances
   (`Plain`), e.g. "Amaliyah selesai", "Sesi tasbih", "Total menit" — no
   "Lihat semua" (same reasoning).
3. **Riwayat penyelesaian amaliyah** — up to 5 most-recent `Activity Row
   (Kind=Amaliyah)` instances + "Lihat semua" action → `Aktivitas / Detail
   Amaliyah` (filtered list screen, state 7 below). Each row: amaliyah
   name, content version, completion time, duration — exact field list
   from the request.
4. **Riwayat Tasbih** — up to 5 most-recent `Activity Row (Kind=Tasbih)`
   instances + "Lihat semua" → `Aktivitas / Detail Tasbih`. Each row:
   session name (if present), target, final count, time + duration.

Explicitly **not** built this release (each is its own future-version
gate, per the request): a Pengingat section (waits for `0.0.4` data), a
quiz-progress section (waits for `0.0.5` data — moved from `0.4.0`, ADR
0013), any pesantren-activity
section (pesantren is out of scope entirely for this pass). Do not render
empty placeholders for any of these — they simply do not exist as layers
in this release's frames.

## States (7, per the request)

1. **`Aktivitas / Semua Data Kosong`** — no section renders at all except a
   single screen-level `Status State (Kind=Empty)`: icon `history`, heading
   "Belum ada aktivitas", body "Selesaikan amaliyah atau mulai sesi tasbih
   untuk melihat aktivitasmu di sini." — this is the one legitimate
   exception to "sections render nothing when empty," since a completely
   blank scrollable screen with zero content is a worse experience than one
   explanatory state.
2. **`Aktivitas / Hanya Amaliyah`** — streak + this-week summary (if those
   have real data) + "Riwayat penyelesaian amaliyah" only; no Tasbih
   section.
3. **`Aktivitas / Hanya Tasbih`** — mirror of state 2, Tasbih section only.
4. **`Aktivitas / Data Parsial`** — some sections present, some absent
   (e.g. streak + amaliyah history, but this-week summary not yet
   meaningful and no tasbih history yet) — demonstrates the hide-if-empty
   rule is per-section, not all-or-nothing.
5. **`Aktivitas / Data Lengkap`** — every section in scope present with
   real-looking data (5 rows each in the amaliyah/tasbih sections, capped
   at the "Lihat semua" threshold).
6. **`Aktivitas / Long History`** — same as state 5 but demonstrates the
   "Lihat semua" destination itself: `Aktivitas / Detail Amaliyah` (or
   Tasbih), a dedicated scrollable list screen, top app bar with back
   action + title matching the section name, plain `Activity Row` list
   with no artificial page cap (scrolls, does not paginate with numbered
   pages).
7. **`Aktivitas / Filter Lihat Semua`** — the same detail list screen with
   a lightweight filter affordance at the top (`tune` icon + a compact
   chip row, e.g. "Semua / 7 hari terakhir / 30 hari terakhir") — a filter,
   not a second navigation system; dismissable, defaults to unfiltered.

## Privacy note (annotation, not a UI element)

Every frame in this section gets a small design-tool annotation (not visible
screen text beyond what's already there): "All Aktivitas statistics are
private and local-first — no share button, no export action, no network
call. Do not add one in implementation." This mirrors the request's
explicit instruction and the existing `DESIGN_SYSTEM.md`/PRD privacy
posture, recorded here so it survives into whichever session implements
this screen in Compose.

## Expanded (1280×800) note

Built for state 5 (`Data Lengkap`) only — the single-column layout gains a
constrained max content width and centers on the wider canvas (same rule as
every other screen in this spec); the section order and component choice
do not change with width, so the remaining 6 states are not mechanically
re-framed at 1280×800.
