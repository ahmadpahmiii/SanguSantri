# Offline-First and Synchronisation

Applies to any task touching the bundled bootstrap, content importer,
repositories, or remote sync (FR-010). See ADR
[0007](../decisions/0007-offline-first-public-content.md) for the product
decision this implements, ADR
[0003](../decisions/0003-room-as-local-source-of-truth.md) for the local
source-of-truth decision, ADR
[0012](../decisions/0012-bundled-bootstrap-and-remote-sync.md) for the
bundled-bootstrap-plus-remote-sync architecture this document describes,
ADR [0014](../decisions/0014-firebase-hosting-static-content-delivery.md)
for the static Firebase Hosting content source that replaced the originally
planned Go backend, and ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md) for
the flat catalog/content-file contract this document assumes.

## Source of truth

Room is the Android application's canonical source of truth. Screens and
ViewModels must not render directly from network responses. The network
updates Room; the UI observes Room. This follows official Android
offline-first guidance. This is unaffected by remote sync below — sync only
ever writes to Room, never to a UI-visible cache of its own.

## Data flow (implemented)

```text
Bundled JSON assets ──────────┐
                              ├── ContentImporter ── Room ── Repository ── UI
Firebase Hosting static files ┘
```

Bundled assets and Firebase Hosting's static files are not mutually
exclusive modes — both feed the same Room tables through the same shared,
transport-agnostic `ContentImporter` (`data/content/`). Bundled JSON
guarantees a fresh-install/offline/hosting-never-reached baseline; Firebase
Hosting provides a remote catalog, newer content files, and corrections
without requiring an APK release (ADR 0014 — no dynamic backend involved,
just static files fetched over plain HTTPS `GET`). Remote sync updates
Room; it is never rendered directly by the UI.

## Bundled bootstrap (implemented)

`BundledContentBootstrapper` (`data/local/content/`) reads
`app/src/main/assets/content/catalog.json` directly from `AssetManager`
(no source-abstraction interface — there is only one bundled-storage
implementation). For every catalog item: `ContentImporter.refreshCatalogMetadata`
unconditionally upserts the cheap display fields (this is how
`isActive`/`order`/title/description/image changes propagate without
reading the item's content file at all), then the item's version is
compared against Room's local version for that content id
(`decideContentVersionAction`) — only when genuinely newer is the content
file actually read, structurally validated, identity-checked against the
catalog entry, and imported. Runs off the main thread, on every launch,
and is idempotent. Arabic content must never be hardcoded inside Kotlin
source files. Full schema/validation rules: `docs/content-schema.md` (the
canonical schema doc — do not restate its field tables here).

### Version comparison (bundled and remote — shared logic)

```text
No Room row for this content id           → import
Catalog/file version > Room version       → import (replace the current version)
Catalog/file version < Room version       → skip; Room is never downgraded
Catalog/file version == Room version      → skip (already up to date)
```

This is the same comparison for bundled content and remote content —
`decideContentVersionAction` (`data/content/ContentVersionAction.kt`) is one
pure function both `BundledContentBootstrapper` and `ContentSyncManager`
call, so a bundled item that is behind whatever a prior remote sync already
installed in Room is skipped **without even reading its content-file
bytes**, and a bundled item equal to Room's version is a no-op the same
way. Bundled bootstrap never wins a race against a genuinely newer synced
version, and remote sync never wins against a genuinely newer bundled
version — whichever has the higher `version` is what Room ends up holding.
`ContentImporter.importContentFile` always re-runs this same comparison
itself before writing anything — the pre-check in each caller is a
bandwidth/IO optimisation, not a relaxation of the importer's own safety.
There is no checksum any more (ADR 0015) — a monotonic integer version is
sufficient once content is authored and deployed from the same git
repository that serves it.

## Remote catalog (implemented)

`GET content/catalog.json` — no request body, no conditional-request
header. The catalog is small and is checked at most once every 24 hours
(the scheduler's own gate below), so a normal sync always issues a plain
request and reads the `200` response. The response is schema-version- and
structurally-validated, then every listed item's cheap metadata is
refreshed unconditionally and its version evaluated per the comparison
above — an item that is clearly not worth fetching (older or equal
version) is skipped **before** any network request for its content file
(`ContentSyncManager`, bandwidth avoidance). Full contract:
`docs/content-schema.md`.

## Content-file import (implemented)

For an item actually worth fetching: `ContentApiService.getContent(contentUrl)`
— a plain `GET` against the catalog item's own `contentUrl`, response-size-
limited transparently by `ResponseSizeLimitInterceptor` (an OkHttp
interceptor, not a manual per-call streaming cap — ADR 0015) → parse →
structurally validate → verify the file's own `id`/`version` match the
catalog entry that named it → import inside one Room transaction. A
malformed content file never partially replaces local content, and one
item's failure never affects another item in the same catalog (per-item
isolation). Bundled bootstrap and remote sync both call the same
`ContentImporter.importContentFile` — there is exactly one import/replace
implementation, not two parallel ones.

### Atomic replacement

A remote or bundled update replacing an existing content item never
deletes first and inserts later. The sequence inside one
`SanguSantriDatabase.withTransaction` block is: upsert the `content` row →
delete the old `content_steps` rows → insert the new `content_steps` rows
→ preserve `step_progress` for step ids that still exist, delete it for
step ids that no longer exist → keep `guided_reading_sessions` if its
`currentStepId` still exists, delete it otherwise → reset
`reading_positions`. If any step fails, Room rolls back the entire block
and the previously valid content remains exactly as it was — readable
immediately, with no intermediate empty state.

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

`ContentSyncManager.sync()` returns one of three `SyncResult`s:
`Completed(updatedContentIds, skippedContentIds, rejectedContentIds)`,
`RetryableFailure(reason)`, or `PermanentFailure(reason)`. There is no
separate partial-failure result: `Completed` can carry `rejectedContentIds`
for item-level failures alongside whatever did update, without failing the
whole sync.

* **Retryable** (`IOException`, timeout, HTTP 408/429/5xx) at the catalog
  level, **or** the same classification for an individual item's
  content-file fetch: either aborts the whole `sync()` call with
  `RetryableFailure`, and `ContentSyncWorker` retries the entire sync
  (bounded exponential backoff, `Result.retry()`, 3 attempts total). An
  item-level retryable failure is not treated as merely that one item's
  problem — a timeout genuinely says nothing about that specific item, so
  the whole attempt restarts. This is safe because items already imported
  earlier in the same attempt already match Room and are skipped on the
  retry (per the version comparison above). Only after the final attempt
  is a terminal `FAILED` status recorded — earlier attempts do not touch
  the 24-hour gate, so a still-retrying sync does not look like a fresh
  terminal failure.
* **Permanent** at the catalog level (unsupported schema, empty/malformed
  body, invalid catalog structure, non-retriable catalog HTTP 4xx):
  terminal `FAILED` recorded immediately, Room untouched.
* **Permanent at the item level** (id/version mismatch against the
  catalog, malformed content-file JSON, invalid structure, non-retriable
  item HTTP 4xx): that one item's content id is added to
  `rejectedContentIds` and Room is left unchanged for it, but the rest of
  the catalog's items still get processed — one bad item never blocks
  another. `ContentSyncWorker` records terminal `PARTIAL` when any item
  was rejected this way (even if others updated successfully), or
  `SUCCESS` when none were.
* Either way: the application never crashes, no raw error reaches the
  user, a concise diagnostic is logged (never the content body or Arabic
  text), and Room's previously valid content keeps rendering exactly as
  before the sync attempt.
* Sync bookkeeping is just `content_last_sync` (`value` one of
  `SUCCESS`/`PARTIAL`/`FAILED`) in the existing `app_metadata` key-value
  table (`ContentSyncMetadata`) — no dedicated sync table was created
  solely for one timestamp. There is no stored ETag, manifest version, or
  checksum to track any more.

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
  the initial public schema ships (ADR 0015's version 1→2 migration is an
  explicit, product-owner-directed exception taken early);
  `fallbackToDestructiveMigration` remains prohibited in any build that
  could reach a real user.
* Content imports (bundled or remote) must be transactional and atomic —
  see Atomic replacement above.
* User progress must survive application termination.

## Application resilience

The application must provide meaningful states for: empty local catalogue,
bundled bootstrap failure, content structural validation failure,
unsupported schema, offline mode, and synchronisation failure (network,
HTTP, malformed catalog). No raw stack trace or backend error text may be
shown to users.
