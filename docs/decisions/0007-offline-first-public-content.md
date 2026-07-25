# 0007: Offline-first public content

## Status

Accepted

## Context

PRD §3.2 requires that public amaliyah remain usable without login, internet
connection, an available backend, or previously completed synchronisation.
This is a product-level commitment, distinct from ADR 0003 (Room as the
local source of truth), which is the engineering mechanism that makes it
possible. The product decision needs its own record because it constrains
every future feature, not just the data layer.

## Decision

The application MUST ship with approved seed content bundled in the APK
(`app/src/main/assets/content/`) and MUST render Serambi and both amaliyah
from that bundled content with zero network dependency. Synchronisation
(FR-010) is strictly additive: it may update content in the background, but
its absence, failure, or delay must never degrade the offline experience.
Any future feature that requires a live backend connection for its core
function (not enhancement) must be scoped as account-gated or
pesantren-gated, never as a change to the public reader's baseline
behaviour.

## Consequences

* Every roadmap item that touches public content (streaks, reminders,
  audio) must define an offline-degraded mode before it ships, not after.
* Backend outages are a non-event for the public reader by design — this
  must remain true and be tested (`docs/engineering/OFFLINE_FIRST.md`), not
  just asserted.
