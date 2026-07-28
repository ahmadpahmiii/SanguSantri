# Offline-First and Synchronisation

Applies to any task touching the bundled bootstrap, content importer,
repositories, or remote sync (FR-010). See ADR
[0007](../decisions/0007-offline-first-public-content.md) for the product
decision this implements, ADR
[0003](../decisions/0003-room-as-local-source-of-truth.md) for the local
source-of-truth decision, and ADR
[0012](../decisions/0012-bundled-bootstrap-and-remote-sync.md) for the
bundled-bootstrap-plus-remote-sync architecture this document describes.

## Source of truth

Room is the Android application's canonical source of truth. Screens and
ViewModels must not render directly from network responses. The network
updates Room; the UI observes Room. This follows official Android
offline-first guidance. This is unaffected by remote sync below — sync only
ever writes to Room, never to a UI-visible cache of its own.

## Data flow (implemented)

```text
Bundled JSON assets ─────┐
                         ├── ContentPackageImporter ── Room ── Repository ── UI
Backend content API ─────┘
```

Bundled assets and the backend are not mutually exclusive modes — both feed
the same Room tables through the same shared, transport-agnostic
`ContentPackageImporter` (`data/content/`). Bundled JSON guarantees a
fresh-install/offline/no-backend-ever-reached baseline; the backend
provides a remote manifest, newer content packages, and corrections without
requiring an APK release. The backend updates Room; it is never rendered
directly by the UI.

## Bundled bootstrap (implemented)

`BundledContentBootstrapper` (`data/local/content/`) reads
`app/src/main/assets/content/manifest.json` directly from `AssetManager`
(no source-abstraction interface — there is only one bundled-storage
implementation). For each listed package: read bytes → verify SHA-256 →
parse → structurally validate → identity-check against the manifest entry
→ compare against Room's currently active version for that variant →
import/replace/skip as below. Runs off the main thread, on every launch,
and is idempotent. Arabic content must never be hardcoded inside Kotlin
source files. Full schema/validation rules: `docs/content-schema.md` (the
canonical schema doc — do not restate its field tables here).

### Version comparison (bundled and remote — shared logic)

```text
No Room version for this variant  → import
Package version > Room version    → import (replace the active version)
Package version < Room version    → skip; Room is never downgraded
Package version == Room version, checksum matches   → skip (already up to date)
Package version == Room version, checksum differs   → immutable-version
                                                        contract violation;
                                                        log, keep Room as is
```

This is the same comparison for bundled content and remote content — a
bundled package that is behind whatever a prior remote sync already
installed in Room is skipped silently; a bundled package equal to Room's
active version and checksum is a no-op. Bundled bootstrap never wins a
race against a genuinely newer synced version, and remote sync never wins
against a genuinely newer bundled version — whichever has the higher
`versionNumber` is what Room ends up holding.

## Remote manifest (implemented)

`GET /v1/content/manifest` with `If-None-Match: <stored ETag>`. A `304`
response performs no package download and no Room write. A `200` response
is schema-version-checked, then every listed package is evaluated per the
comparison above using the manifest's own declared `versionNumber`/
`checksumSha256` — a package that is clearly not worth downloading (older,
or equal-version-matching-checksum) is skipped **before** any network
request for its bytes (`ContentSyncCoordinator`, bandwidth avoidance).
Full contract: `docs/content-schema.md`, `CLAUDE.md` §7.

## Package import (implemented)

For a package actually worth downloading: `GET
/v1/content/packages/{versionId}`, streamed into a size-limited temporary
cache file (not assumed to always stay tiny) → checksum verify → parse →
structurally validate → verify the package's own `version.id` matches the
manifest entry that named it → import inside one Room transaction →
temporary file always deleted, success or failure. A malformed package
never partially replaces local content, and one package's failure never
affects another package in the same manifest (per-package isolation).
Bundled bootstrap and remote sync both call the same
`ContentPackageImporter.importPackage` — there is exactly one import/replace
implementation, not two parallel ones.

### Atomic replacement

A remote or bundled update replacing an existing active version never
deletes first and inserts later. The sequence inside one
`SanguSantriDatabase.withTransaction` block is: upsert amaliyah/variant
metadata → insert the new approval/version/steps → delete the old
version's version-scoped reading progress (reading position, guided-reader
session, step counters) → delete the old version row (cascades its steps)
→ delete the old approval row. If any step fails, Room rolls back the
entire block and the previously valid content remains exactly as it was —
readable immediately, with no intermediate empty state.

## Scheduling (implemented)

`ContentSyncScheduler.enqueueIfStale()` is called from
`SanguSantriApplication.onCreate()` (after bundled bootstrap), on an
application-scoped IO coroutine, never blocking the first frame. It
enqueues one unique one-time `ContentSyncWorker`
(`ExistingWorkPolicy.KEEP`, work name `sangu-santri-content-sync`,
`NetworkType.CONNECTED` constraint) only when the last *terminal* sync
attempt — success or failure — is 24+ hours old or has never happened.
This is deliberately **not** a periodic worker: repeated app foregrounds
within the same 24 hours enqueue nothing further (`KEEP` also protects
against duplicate enqueues from rapid repeated calls).

## Failure and retry semantics (implemented)

* **Transient** (`IOException`, timeout, HTTP 408/429/5xx): bounded
  exponential backoff, `Result.retry()`, 3 attempts total. Only after the
  final attempt is a terminal `FAILED` status recorded — earlier attempts
  do not touch the 24-hour gate, so a still-retrying sync does not look
  like a fresh terminal failure.
* **Permanent/data-contract** (unsupported schema, checksum mismatch,
  invalid structure, same version with different bytes, unsupported
  minimum app version, non-retriable 4xx): terminal `FAILED` recorded
  immediately, a concise diagnostic logged (never the package body or
  Arabic text), Room untouched, no retry of the same invalid package this
  run.
* Either way: the application never crashes, no raw error reaches the
  user, and Room's previously valid content keeps rendering exactly as
  before the sync attempt.
* Sync bookkeeping (`content_last_sync`, `content_manifest_etag`,
  `content_manifest_version`) lives in the existing `app_metadata`
  key-value table (`ContentSyncMetadata`) — no dedicated sync table was
  created solely for one timestamp and one ETag.

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

* Core reading must work when the API is unavailable, unreachable, or has
  never been deployed.
* Failed synchronisation must not remove, replace, downgrade, or hide local
  content already in Room.
* Any Room schema change must follow the current schema-freeze policy
  (`docs/engineering/CONTENT_MODEL.md`) — a real, tested `Migration` once
  the initial public schema ships; `fallbackToDestructiveMigration` remains
  prohibited in any build that could reach a real user.
* Content imports (bundled or remote) must be transactional and atomic —
  see Atomic replacement above.
* User progress must survive application termination.

## Application resilience

The application must provide meaningful states for: empty local catalogue,
bundled bootstrap failure, content package validation or checksum failure,
unsupported schema, offline mode, synchronisation failure (network, HTTP,
malformed manifest), and an immutable-version checksum conflict. No raw
stack trace or backend error text may be shown to users.
