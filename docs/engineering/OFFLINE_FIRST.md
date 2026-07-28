# Offline-First and Synchronisation

Applies to any task touching the seed importer, repositories, or the future
remote sync implementation (FR-010). See ADR
[0007](../decisions/0007-offline-first-public-content.md) for the product
decision this implements, and ADR
[0003](../decisions/0003-room-as-local-source-of-truth.md) for the local
source-of-truth decision.

## Source of truth

Room is the Android application's canonical source of truth. Screens and
ViewModels must not render directly from network responses. The network
updates Room; the UI observes Room. This follows official Android
offline-first guidance.

## Seed import (implemented)

Approved production content packages live under
`app/src/main/assets/content/` (`manifest.json` + one file per package). On
first launch: read seed manifest → validate schema → validate package
checksum → import content transactionally → mark imported versions active →
store seed manifest version. Arabic content must never be hardcoded inside
Kotlin source files. Full behaviour and validation rules:
`docs/content-schema.md` (this is the canonical schema doc — do not restate
its field tables here).

## Remote manifest (not yet implemented)

The backend will expose a lightweight content manifest: schema version,
generation timestamp, active content versions, version checksums, package
URLs, minimum application version, revocation status, optional asset
package information. The Android client sends `If-None-Match` when an ETag
is available.

## Package import (design target for FR-010, not yet implemented)

Downloaded packages: download to temporary storage → size check → checksum
verify → parse → structurally validate → import in one transaction →
activate only after import success → delete from temporary storage. A
malformed package must never partially replace local content. Build to this
exact sequence rather than discovering it iteratively — it mirrors the
already-implemented and tested seed-import shape in
`data/local/seed/SeedContentImporter`; reuse that shape rather than building
a second, parallel import implementation.

## Scheduling (not yet implemented)

Synchronisation runs at application startup without blocking UI, through
periodic WorkManager work, on manual refresh, and after connectivity
returns when pending work exists. The client must use backoff and avoid
repeated network loops.

## Local user-state features (Figma product-alignment pass)

Favourites, recently-opened, Standalone Tasbih's unfinished count, and
Aktivitas activity data (streak, completion history, tasbih history) are
all local-first state, same as existing reading position/guided-session
persistence — Room (structured, queryable state) or DataStore (simple
preferences), never a network round-trip to read or write. None of these
features depend on connectivity, an account, or a backend; all remain
usable fully offline, consistent with the guest-first/offline-first
principles (`docs/product/PRD.md` §3.2/§3.4, ADR 0007/0009). Reminder
schedules (`0.0.4`) are also local (WorkManager-scheduled), with no server
round-trip required to fire a locally-scheduled notification. None of
these tables are designed or created by this documentation pass — see
`docs/engineering/CONTENT_MODEL.md` for their planned shape and owning
phase.

## Reliability requirements

* Core reading must work when the API is unavailable.
* Failed synchronisation must not remove local content.
* Database migrations must be tested (see
  `SanguSantriMigrationTest` for the existing pattern).
* Destructive Room migration is prohibited for production.
* Content imports must be transactional.
* User progress must survive application termination.

## Application resilience

The application must provide meaningful states for: empty local catalogue,
seed import failure, content package validation failure, unsupported
schema, offline mode, synchronisation failure, revoked content, feedback
pending, feedback submission failure. No raw stack trace or backend error
text may be shown to users.
