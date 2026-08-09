# 0.0.5 — Nahwu Quiz (individual, guest/offline)

**Moved from `0.4.0`** (product owner/tech lead decision, 2026-07-29, ADR
[0013](../../../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md))
— every screen/state/component below is otherwise unchanged from the
original `0.4.0` spec; only the version number moved, to immediately after
Pengingat Amaliyah (`0.0.4`) and before Accounts (`0.1.0`).

Guest/offline individual mode only — Accounts (`0.1.0`), Pesantren
Membership (`0.2.0`), and Private Pesantren Space (`0.3.0`) are all skipped
per the request, so this release builds **no** login requirement, no user
profile, no pesantren selection/representation, no leaderboard, no social
ranking, and no user-generated-question authoring UI anywhere below —
verified against every one of the 15 screens.

## Content safety (applies to every screen with question/answer text)

No real Nahwu question, Arabic quotation, dalil, or translation is written
in this spec or should be drawn into any design-tool text layer. Every sample
question/answer/explanation string in every screen below is the literal,
unedited marker text:

```
[DESIGN SAMPLE — KONTEN KUIS WAJIB DIMODERASI]
```

used verbatim as the placeholder content everywhere a real question would
eventually go (question stem, all answer options, any explanation text).
Production question banks require editorial moderation before any content
replaces this marker (per the request and the project's content-safety
rules) — this is a hard constraint on this spec, not a style choice.

## Entry point

* **`Beranda / Entry Point Copy — Section Belajar`**: a duplicate slice of
  `19:2` with a new "Belajar" section — one `Quiz Package Card`-style
  summary tile ("Kuis Nahwu", short description, `chevron_right`) linking
  to the Nahwu Quiz landing screen. Copy, not an edit to `19:2` (same rule
  as the Pengingat entry points).

## Screens and states (15, per the request)

1. **`Nahwu Quiz / Landing`** — top app bar "Kuis Nahwu" + back. Short
   intro text (what Nahwu quiz is, that it's individual/offline), a
   primary action "Lihat paket soal" → state 2. If the user has an
   unfinished attempt, a "Lanjutkan kuis" card appears above the primary
   action (see state 12).
2. **`Nahwu Quiz / Daftar Paket`** — vertical list of `Quiz Package Card`
   instances (status chip variants: `New`/`In Progress`/`Completed`/
   `Unavailable` all represented across different cards in this one
   frame so every status is visible at once).
3. **`Nahwu Quiz / Detail Paket`** — one package's detail: title,
   description, question count, a linear progress bar + "{n}/{total}
   selesai" when progress exists (hidden when the package has never been
   opened), primary action "Mulai" (or "Lanjutkan" if in progress).
4. **`Nahwu Quiz / Instruksi`** — a short instructional screen before the
   first question: number of questions, estimated format ("pilihan
   ganda"), a note that progress saves automatically, primary action
   "Mulai kuis".
5. **`Nahwu Quiz / Pertanyaan`** — `Quiz Progress Indicator` at top, the
   question stem (`bodyLarge`, marked with the design-sample text above),
   a vertical list of `Quiz Answer Option (State=Default)` instances
   (4 options), a disabled "Lanjut" action at the bottom (enabled only once
   an option is selected).
6. **`Nahwu Quiz / Jawaban Dipilih`** — same screen, one
   `Quiz Answer Option (State=Selected)`, "Lanjut" now enabled
   (reads "Kumpulkan jawaban" on this pre-submit state).
7. **`Nahwu Quiz / Feedback Benar`** — post-submit: the chosen option shows
   `State=Correct`, a short affirming caption below the options
   ("Benar!", `primary` color + `check_circle` icon — icon and text
   together, not color alone), "Lanjut ke soal berikutnya" action.
8. **`Nahwu Quiz / Feedback Salah`** — post-submit: the chosen option shows
   `State=Incorrect`, the actual correct option simultaneously shows
   `State=Correct` (so the right answer is always visible after answering,
   never withheld), caption "Kurang tepat", same "Lanjut" action. An
   optional short explanation line uses the same `[DESIGN SAMPLE —
   KONTEN KUIS WAJIB DIMODERASI]` marker, never invented explanation text.
9. **`Nahwu Quiz / Hasil Kuis`** — `Result Summary` component: score,
   correct/total, optional delta vs. previous individual attempt, "Lihat
   riwayat skor" + "Ulangi kuis" actions. No share action.
10. **`Nahwu Quiz / Riwayat Skor Individual`** — a plain vertical list of
    past attempts for this package (date, score, duration) — private,
    individual only, no cross-user comparison of any kind.
11. *(merged into 12 below per the request's own numbering overlap between
    "resume unfinished quiz" as both a screen and a state — see note)*
12. **`Nahwu Quiz / Lanjutkan Kuis Belum Selesai`** — the "Lanjutkan kuis"
    card surfaced on the Landing screen (state 1) and on `Detail Paket`
    (state 3): shows package name, "{n}/{total} soal", primary action
    "Lanjutkan" that resumes exactly at the next unanswered question,
    preserving prior answers.
13. **`Nahwu Quiz / Bank Soal Kosong`** — `Detail Paket` variant using
    `Status State (Kind=Empty)`: icon `error_outline` (or a quiz-specific
    icon if preferred, `quiz` outlined), heading "Belum ada soal di paket
    ini", body explains the package is awaiting content, no "Mulai" action
    (nothing to start).
14. **`Nahwu Quiz / Offline-Ready`** — `Daftar Paket`/`Detail Paket` with a
    persistent `Status State (Kind=Offline)` banner-style note (not a
    blocking dialog): "Kuis ini tersedia offline — semua soal sudah
    tersimpan di perangkat.", `wifi_off`/`cloud_off` icon, reassurance
    tone, does not block interaction.
15. **`Nahwu Quiz / Konten Tidak Tersedia`** — `Status State (Kind=Error)`
    full-screen replacement of `Daftar Paket`/`Detail Paket` when the
    question bank genuinely cannot load (corrupt/missing local data),
    `error_outline` icon, "Coba lagi" action.

## Security and moderation annotations (design-tool annotation text on the

relevant frames, not implemented UI)

Attach these as explicit annotation notes for a future implementation
session — none of them are drawn as visible screen chrome:

* **Anti-cheating controls**: annotate `Nahwu Quiz / Pertanyaan` and
  `Feedback` frames — "Future: basic anti-cheat (e.g. answer-shuffle,
  time-on-question tracking) belongs here once individual scoring has any
  competitive stake. Not built in this offline-individual release."
* **Play Integrity**: annotate the Landing screen — "Play Integrity
  attestation is required only if/when competitive ranking is enabled
  (BLOCKED, see below). Do not wire into this individual/offline release."
* **Moderated question bank**: annotate `Daftar Paket`/`Detail Paket` —
  "Every production question replacing the `[DESIGN SAMPLE]` marker text
  must pass editorial moderation before shipping, per the project's
  content-safety rules — same governance posture as amaliyah content."
* **No sensitive answer-key data in raw UI state**: annotate `Pertanyaan`/
  `Feedback` frames — "Implementation must not expose the correct-answer
  identity in client-readable state before submission (e.g. in a
  human-readable API payload or unobfuscated local field) — future
  engineering concern, flagged here so the design contract doesn't imply
  otherwise."

## Explicitly BLOCKED, not built (documented, never shown in UI)

A single annotation frame, `Nahwu Quiz / BLOCKED — Future Handoff Notes`
(a text-only documentation frame, not a real app screen, placed in the
`0.0.5 — Nahwu Quiz` section for handoff visibility): "Pesantren
representation, inter-pesantren leaderboard, and social ranking are
**BLOCKED — requires Accounts and Pesantren Membership** (`0.1.0`/`0.2.0`).
No UI for any of this exists anywhere in the 15 screens above; do not add
placeholder nav items, disabled cards, or 'coming soon' rows for it in this
release." This directly satisfies the request's instruction to document
the competitive area as blocked rather than surface it.

## Expanded (1280×800) note

Built for states 2 (`Daftar Paket`) and 5 (`Pertanyaan`) — list-grid reflow
and a centered, width-capped question column are the only genuinely
width-sensitive cases; the remaining states reuse the same centered-column
rule as every other screen in this spec and are not mechanically re-framed.
