# Figma Handoff

Applies to any UI task touching a screen this document maps. Read alongside
`docs/design/DESIGN_SYSTEM.md` and `docs/design/ACCESSIBILITY.md` for token
and accessibility rules, and `docs/reviews/figma-product-alignment.md` for
the detailed gap analysis this handoff feeds.

## Status of this document

Created during the Figma product-alignment documentation pass
(2026-07-26). **The Figma MCP connection was rate-limited (Starter plan)
for the entire session that produced this document** — every node below
was never actually opened via `get_metadata`/`get_design_context`/
`get_screenshot`. Everything under "Frame-to-feature mapping" is sourced
from the node list and confirmed product/UX decisions supplied directly in
the request that started this pass, not from inspecting the frames
themselves. Node-level detail (exact spacing, type ramp, component
variants, colour tokens, states drawn in the frame) is marked **PENDING
FIGMA VERIFICATION** throughout and MUST be confirmed against the live
file — with `get_metadata`/`get_design_context`/`get_screenshot`, per the
installed `figma-use`/`figma-design-to-code` skills — before or during
whichever implementation phase touches that frame. Do not implement a
phase's visual detail from this document alone; re-open the file first.

## File reference

* Figma file: `https://www.figma.com/design/cSjOfVGxBo6N0lX5lqsGEi`
* File key: `cSjOfVGxBo6N0lX5lqsGEi`

## Node reference (as supplied, unverified)

| Node ID  | Name (as given)                       | Status                                               |
|----------|---------------------------------------|------------------------------------------------------|
| `3:3`    | Product screens (page)                | Container page — not yet opened                      |
| `3:136`  | Final unified product section         | Not yet opened                                       |
| `14:2`   | Revised Full Reader                   | Revised — supersedes any earlier Full Reader frame   |
| `14:32`  | Revised Guided Reader                 | Revised — supersedes any earlier Guided Reader frame |
| `16:2`   | Full Reader overflow menu             | Revised                                              |
| `16:45`  | Guided Reader overflow menu           | Revised                                              |
| `16:89`  | Reader Settings bottom sheet          | Revised                                              |
| `16:148` | Reader Table of Contents bottom sheet | New — no equivalent screen exists in the app today   |
| `17:2`   | Revised Standalone Tasbih             | Revised                                              |
| `17:32`  | Custom Tasbih target dialog           | Revised                                              |
| `19:2`   | Revised future-proof Beranda          | Revised — supersedes the current Serambi screen      |
| `19:84`  | Jelajahi Amaliyah                     | New — no equivalent screen exists in the app today   |

## Legacy vs. revised

The request that produced this document explicitly labelled every node
above as either "Revised" or new; it did not enumerate any older/legacy
Figma frame IDs to mark superseded. Per the source-of-truth priority
(request > revised Figma > PRD/governance docs > `docs/PROGRESS.md`/code >
older Figma/screenshots), **any frame in the file not listed above is
assumed legacy and MUST NOT be used** as a build reference — confirm this
against the file's page list the next time Figma access is available,
and record any additional legacy frame IDs found.

## Frame-to-feature mapping

| Frame                            | Feature / phase  | Current implementation                                                                                       | Gap (see alignment review for detail)                                                                                                                                                                                                                   |
|----------------------------------|------------------|--------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `19:2` Beranda                   | Phase B, `0.0.1` | `feature/home/SerambiScreen.kt` — single flat `LazyColumn` of two hardcoded-shaped cards from Room           | Full rebuild to scalable, section-based, hide-if-empty dashboard (PRD §7/decision G)                                                                                                                                                                    |
| `19:84` Jelajahi Amaliyah        | Phase B, `0.0.1` | Does not exist                                                                                               | New destination, new package `feature/explore`                                                                                                                                                                                                          |
| `14:2` Full Reader               | Phase A, `0.0.1` | `feature/reader/ReaderScreen.kt`                                                                             | Add repetition→Guided shortcut action (decision D); move settings entry into overflow (decision F)                                                                                                                                                      |
| `14:32` Guided Reader            | Phase A, `0.0.1` | `feature/guidedreader/GuidedReaderScreen.kt`                                                                 | Verify against revised layout once Figma is reachable; overflow restructure (below)                                                                                                                                                                     |
| `16:2` Full Reader overflow      | Phase A, `0.0.1` | `ReaderOverflowMenu.kt`: mode-switch + "Sumber & Pentashihan" only                                           | Add "Daftar isi" and "Tampilan bacaan" items (settings currently a separate top-bar icon, not an overflow item)                                                                                                                                         |
| `16:45` Guided Reader overflow   | Phase A, `0.0.1` | Same shared `ReaderOverflowMenu.kt`                                                                          | Same restructure as above                                                                                                                                                                                                                               |
| `16:89` Reader Settings sheet    | Phase A, `0.0.1` | `feature/reader/settings/ReaderSettingsSheet.kt` — already a modal bottom sheet                              | Verify field set/order against revised frame; move trigger into overflow                                                                                                                                                                                |
| `16:148` Table of Contents sheet | Phase A, `0.0.1` | Does not exist                                                                                               | New bottom sheet; derive sections from existing `HEADING`-typed steps, no schema change (see `CONTENT_MODEL.md`)                                                                                                                                        |
| `17:2` Standalone Tasbih         | Phase C, `0.0.2` | **Implemented, Milestone 9** — `feature/tasbih/TasbihScreen.kt`, `TasbihSessionEntity`/`TasbihHistoryEntity` | None — verify against the revised frame once Figma access returns (this pass built from `docs/design/figma-export/future-releases/02-release-0.0.2-tasbih.md` and the reused baseline export, not a freshly re-opened `17:2`)                           |
| `17:32` Custom Tasbih dialog     | Phase C, `0.0.2` | **Implemented, Milestone 9** — `feature/tasbih/components/CustomTasbihTargetDialog.kt`                       | None — small numeric-input dialog, not a full-screen form (decision J), as built                                                                                                                                                                        |
| Aktivitas (no frame supplied)    | Phase D, `0.0.3` | Does not exist                                                                                               | **Known incomplete Figma area** — no revised frame was provided for Aktivitas; do not guess its layout. Confirm a frame exists before starting Phase D, or proceed from the written decision (K) alone if the product owner confirms no frame is coming |

## Navigation map (resolved — bottom-navigation-only through 0.0.5)

```text
Beranda | Aktivitas | Tasbih
```

**Resolved** (product owner/tech lead decision, 2026-07-29, ADR
[0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md),
implemented Milestone 9 — supersedes this section's earlier "open
question" and five-destination bar/rail description): **bottom navigation
bar only**, on every window-size class including expanded/tablet, through
`0.0.5`. The nav chrome is introduced incrementally, not speculatively:
Beranda | Tasbih at `0.0.2` (`navigation/SanguSantriNavHost.kt`,
`BottomNavigationBar.kt`), Beranda | Aktivitas | Tasbih from `0.0.3`
onward through `0.0.5`. Pengingat Amaliyah and Nahwu Quiz are never
bottom-nav destinations. Pesantren and Profil are out of scope entirely
through `0.0.5` — no nav item, not even disabled/inert. Serambi/Beranda/
the public Amaliyah entry point are not a separate bottom-nav destination;
"Serambi" persists as the internal class-name/route identifier
(`SerambiRoute`/`SerambiScreen`/`SerambiViewModel`), unchanged by this
pass, while "Beranda" is the user-facing label only.

## Reader interaction map

* Full Reader ⇄ Guided Reader: existing bidirectional switch via the
  overflow menu (`ReaderOverflowMenu`, FR-016), preserving stable step-id
  mapping — already implemented, re-verify only.
* **New**: Full Reader's repetition indicator becomes an interactive
  action ("Dibaca N kali · Buka Panduan →") that jumps straight into Guided
  Reader at the same step, no confirmation dialog, preserving Full Reader
  scroll position and restoring the guided counter (decision D). Reuses
  the same stable-step-id switch mechanism `onSwitchToGuided` already
  provides — this is a second trigger for an existing code path, not a new
  one.
* **New**: Table of Contents bottom sheet, opened from either reader's
  overflow menu — jump to a section without marking skipped steps
  complete, highlight current section, preserve reading progress.
* Reader Settings bottom sheet: existing, move its trigger from a
  standalone top-bar icon into the overflow menu alongside the two items
  above (decision F groups "Tampilan bacaan" with the other overflow
  actions).

## Responsive rules (unverified against Figma — inherited from existing docs)

Compact / medium / expanded window-size classes via AndroidX adaptive
layout APIs (`docs/design/DESIGN_SYSTEM.md`, `docs/design/ACCESSIBILITY.md`).
Bottom navigation only on every window-size class including expanded,
through `0.0.5` — decision A's original "rail on expanded" wording is
superseded by ADR
[0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md);
implemented Milestone 9. Adaptive *content* layout (constrained/centred
max-width columns) is unaffected.

## State list (per screen, to verify against Figma)

* Beranda: loading, each section's own empty/hidden state, populated.
* Jelajahi Amaliyah: loading, empty search result, empty category, All /
  Favourite / Offline filter states, populated.
* Full/Guided Reader: loading, content-unavailable, recoverable error,
  content-available (existing `ReaderUiState`/`GuidedReaderUiState` sealed
  states already cover this shape).
* Standalone Tasbih: no session yet (target picker), active session,
  target reached, reset-confirmation.
* Table of Contents: current-section-highlighted, jump-in-progress.

## Motion (unverified against Figma)

Existing pattern: short fixed-duration fade (`AnimatedContent` in
`GuidedReaderScreen.kt`), no reduced-motion branch today. Decision A
requires continued restraint (no decorative motion); Phase A should add a
reduced-motion check before extending animation to new screens
(`docs/design/ACCESSIBILITY.md`).

## Compose component mapping (existing → to build)

| Design concept                                                                            | Existing Compose             | New needed                                                             |
|-------------------------------------------------------------------------------------------|------------------------------|------------------------------------------------------------------------|
| Reader overflow menu                                                                      | `ReaderOverflowMenu.kt`      | Extend with TOC + settings entries                                     |
| Reader settings sheet                                                                     | `ReaderSettingsSheet.kt`     | Verify field order only                                                |
| Card                                                                                      | `AmaliyahCard.kt`            | Reuse for Beranda/Jelajahi lists — do not fork a second card           |
| Home screen                                                                               | `SerambiScreen.kt`           | Rebuild as sectioned Beranda (rename or replace; see alignment review) |
| —                                                                                         | —                            | `feature/explore` (Jelajahi Amaliyah)                                  |
| `feature/tasbih` (Standalone Tasbih + custom target dialog)                               | **Implemented, Milestone 9** | —                                                                      |
| —                                                                                         | —                            | `feature/activity` (Aktivitas, `0.0.3`)                                |
| Bottom nav scaffold (`navigation/`) — bottom-navigation-only, no adaptive rail (ADR 0013) | **Implemented, Milestone 9** | —                                                                      |
| —                                                                                         | —                            | Table of Contents bottom sheet component                               |
| —                                                                                         | —                            | Spiritual-gold accent color token (`core/designsystem/theme/Color.kt`) |

## Implementation order

Matches the request's phase lettering exactly — do not reorder or collapse
phases:

1. **Phase A** (`0.0.1`) — Reader UX alignment: revised Full/Guided layout,
   overflow restructure, repetition shortcut, TOC sheet, settings sheet
   placement, adaptive/dark/RTL reader behaviour.
2. **Phase B** (`0.0.1`) — Beranda + Jelajahi Amaliyah.
3. **Phase C** (`0.0.2`) — Standalone Tasbih + the bottom navigation shell.
   **Implemented, Milestone 9** (bottom-navigation-only, ADR 0013 —
   supersedes this document's earlier bar/rail description above).
4. **Phase D** (`0.0.3`) — Aktivitas.
5. **Phase E** (`0.0.4`) — Reminders, only on explicit continuation past
   Activity.

## Known incomplete Figma areas

* **Aktivitas has no supplied frame.** Decision K describes its structure
  in prose; no Figma node ID was given. Do not invent a layout from the
  other frames' visual language — confirm a frame exists (and get its node
  ID) before Phase D, or treat decision K's prose as the sole source if the
  product owner confirms no frame is coming.
* **Every node in the table above is otherwise unverified** — this pass
  could not call `get_metadata`, `get_design_context`, or `get_screenshot`
  even once (Figma MCP rate limit, Starter plan). Re-run the discovery
  workflow (`figma-use` skill, §9 "Discover Conventions Before Creating")
  before Phase A begins.
