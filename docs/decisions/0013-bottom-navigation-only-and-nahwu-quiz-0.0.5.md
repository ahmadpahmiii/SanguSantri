# 0013: Bottom-navigation-only through 0.0.5, and Nahwu Quiz moved to 0.0.5

## Status

Accepted (2026-07-29, product owner/tech lead)

## Context

The 2026-07-26 design product-alignment pass (`docs/reviews/
design-product-alignment.md`, `docs/design/DESIGN_HANDOFF.md`) documented a
"final navigation model" of five destinations (Beranda/Aktivitas/Tasbih/
Pesantren/Profil) shown as a bottom navigation bar on compact window-size
class and a navigation rail (or other adaptive nav) on expanded/tablet
width, per `docs/product/PRD.md` §7.1 and `docs/design/DESIGN_SYSTEM.md`.
That pass also left "whether the nav chrome ships in `0.0.1` or `0.0.2`"
as an open, unresolved question. Separately, `docs/product/ROADMAP.md`
scheduled Nahwu Quiz at `0.4.0`, after Accounts (`0.1.0`), Pesantren
Membership (`0.2.0`), and Private Pesantren Space (`0.3.0`).

The product owner/tech lead reviewed both open items together while
approving continued Android development on Standalone Tasbih (`0.0.2`)
through Nahwu Quiz, and made two scope decisions in the same session,
recorded here as one ADR since they were decided together and both affect
the same `0.0.2`–`0.0.5` implementation window.

## Decision

1. **Navigation is bottom-navigation-only through `0.0.5`.** No Navigation
   Rail (or other adaptive nav-area swap) is built for any window-size
   class, including expanded/tablet, at any point in this range. This
   resolves the previously open "does the nav chrome ship in `0.0.1` or
   `0.0.2`" question implicitly: the chrome ships incrementally, starting
   at `0.0.2` (the first release with two real root destinations —
   Beranda and Tasbih), growing to three at `0.0.3` (Aktivitas added).
   Adaptive *content* layout (constrained/centred max-width columns on
   large screens, following the existing reader precedent) is unaffected
   and still required — only the navigation chrome itself stays a bottom
   bar. Pengingat Amaliyah (`0.0.4`) and Nahwu Quiz (`0.0.5`, see below)
   are never bottom-nav destinations, matching the original design intent
   — they stay reachable only through entry points on Beranda/Aktivitas.
   Pesantren and Profil remain out of scope entirely for this window (no
   nav item, not even disabled/inert) — they are `0.1.0`/`0.2.0`+ items,
   unaffected by this ADR.
2. **Nahwu Quiz moves from `0.4.0` to `0.0.5`**, immediately after
   Pengingat Amaliyah (`0.0.4`) and before Accounts (`0.1.0`). The scope
   itself is unchanged from the prior `0.4.0` description: individual,
   guest, offline-first, bundled static JSON content, no login, no
   pesantren representation, no leaderboard, no server-verified
   competitive scoring — only the version number and roadmap position
   moved. Accounts (`0.1.0`), Pesantren Membership (`0.2.0`), Private
   Pesantren Space (`0.3.0`), leaderboard, and inter-pesantren ranking
   remain deferred/future work, unaffected by this ADR.

## Consequences

* `docs/product/PRD.md` §7.1, `docs/product/ROADMAP.md`,
  `docs/engineering/ARCHITECTURE.md`'s Navigation destinations section,
  and `docs/design/DESIGN_SYSTEM.md`'s Adaptive navigation section are
  updated in the same pass as this ADR to describe bottom-navigation-only
  through `0.0.5` and Nahwu Quiz at `0.0.5` — the previously documented
  five-destination bottom-bar/rail model and `0.4.0` Nahwu Quiz scheduling
  are superseded for this window, not deleted from history (this ADR is
  the record of why).
* `docs/design/design-export/future-releases/` (an offline design-spec
  build for this same work, written 2026-07-29) is corrected in the same
  pass: its Navigation Rail component/frame is removed from the active
  spec (the two exported baseline frames, `17:2`/`17:32`, are unaffected
  — they were never rail-related), and its `0.4.0 — Nahwu Quiz` file is
  renamed/renumbered to `0.0.5`.
* Whether a Navigation Rail (or the AndroidX adaptive-navigation
  `NavigationSuiteScaffold` API generally) is revisited for a *later*
  release beyond `0.0.5` is not decided by this ADR — it would need its
  own future product decision, not an assumption that this ADR's
  restriction lapses automatically.
* `docs/engineering/CODING_STANDARD.md`'s reference to
  `android/nav3-recipes` for Compose navigation patterns is unaffected —
  the bottom-navigation-only shell still uses Navigation 3's standard
  multiple-back-stacks pattern (`TopLevelBackStack`, per that recipe),
  just without ever swapping to a rail composable.
