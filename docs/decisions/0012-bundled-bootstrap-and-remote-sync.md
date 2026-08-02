# 0012: Bundled bootstrap plus remote Room synchronisation

## Status

Accepted

## Context

Release `0.0.1` has, until now, shipped fully local, fixed release-candidate
content (ADR 0007, Milestone 5/6). The Go + PostgreSQL backend (ADR 0011) is
a parallel workstream and remains undeployed. The product owner and tech
lead approved building the Android remote-content-synchronisation
foundation now, ahead of the backend's own completion, so that:

* the Android app is ready to consume the backend contract the moment it
  ships, with no further Android release required to activate it, and
* the existing bundled-content pipeline (`SeedContentSource`/
  `AssetSeedContentSource`/`SeedContentImporter`) — which only ever needed
  to insert content once, never compare or replace it — is refactored
  before a second, parallel "insert or replace" implementation would
  otherwise have to be built for remote sync.

This supersedes PRD version 1.4's FR-010 ("not part of `0.0.1`, no
synchronisation code exists") and FR-011 ("local on-device fallback to a
retained previous version") — see the rewritten `docs/product/PRD.md`
FR-010/FR-011. It does not reopen ADR 0007 (offline-first is still the
product principle this decision protects), ADR 0008 (backend content
immutability is unchanged), or ADR 0011 (the backend technology choice is
unchanged and still not implemented).

## Decision

**One shared, transport-agnostic importer.** `ContentPackageImporter`
(`data/content/`) receives validated package bytes plus an expected
version id and checksum, and performs the canonical transactional Room
comparison/import/replace operation. It has no knowledge of whether those
bytes came from `AssetManager` or an HTTP response.
`BundledContentBootstrapper` (`data/local/content/`) and
`ContentSyncCoordinator`/`ContentRemoteDataSource` (`data/sync/`,
`data/remote/`) are the only two callers, each owning exactly one
transport-specific responsibility (reading bundled assets; talking to the
backend). The former `SeedContentSource`/`AssetSeedContentSource`/
`SeedContentImporter`/`SeedContentValidator`/`SeedContentChecksum`/
`SeedImportOutcome` — a speculative source-abstraction interface with
exactly one implementation, built for the seed-only phase of the project —
are deleted, not replaced with a second generically-named `ContentSource`
interface; the two concrete responsibilities are named for what they are.

**Room remains the sole source of truth.** Neither the bundled bootstrapper
nor the remote sync path is ever rendered directly by the UI; both only
ever write to Room, and screens continue to observe Room reactively,
exactly as ADR 0003 already established.

**Bundled assets remain mandatory after backend launch.** Bundled content
is the fresh-install bootstrap, the guaranteed-offline baseline, and the
recovery path when the backend has never been reached. It is not replaced
by remote sync — it coexists with it, and `ContentPackageImporter`'s
version comparison ensures whichever source has the higher `versionNumber`
for a variant is what Room ends up holding, regardless of which one ran
most recently.

**Android keeps one active version per variant; the backend keeps
immutable history.** The backend retains every published `AmaliyahVersion`
row for audit, publication, and rollback (ADR 0008, unaffected). Android
does not mirror that history: replacing a variant's active version deletes
the previous version's row, its steps, its approval row, and its
version-scoped reading progress inside the same atomic transaction that
inserts the new version. There is no previous-version browsing screen and
no previous-version fallback logic on-device — the on-device fallback
described in the superseded FR-011 was never actually implemented in code,
so this decision removes documentation and a same-named but differently-
purposed debug-only DAO method (`getLatestNonRevokedForVariant`,
`ContentRepositoryImpl`'s corresponding fallback branch), not working
production behaviour.

**Package replacement is atomic.** A remote or bundled update never
deletes old content and commits before the new content is ready. The
sequence inside one `SanguSantriDatabase.withTransaction` block is: upsert
amaliyah/variant metadata → insert the new approval/version/steps → delete
the old version's version-scoped reading progress → delete the old version
row (cascades its steps) → delete the old approval row. Any failure rolls
back the entire block; the previously valid content remains readable
immediately, with no intermediate empty state.

**Sync uses a 24-hour opportunistic one-time worker, not a periodic one.**
`ContentSyncScheduler.enqueueIfStale()` runs from app startup/foreground
entry and enqueues one unique `ContentSyncWorker`
(`ExistingWorkPolicy.KEEP`) only when the last terminal sync attempt
(success or failure) is 24+ hours old. A permanently repeating periodic
worker was rejected: it would poll a backend that may not exist yet for no
benefit, and the product requirement is "don't hammer a backend that is
still being deployed," not "sync on a fixed schedule regardless of
outcome."

**A Retrofit header is never a content-source switch.** `If-None-Match` is
the only header with sync-relevant meaning (real conditional-request
semantics against the manifest ETag). Whether the app uses bundled or
remote content is never gated by a header, a `USE_LOCAL_CONTENT` flag, or a
`USE_REMOTE_CONTENT` flag — the app always runs bundled bootstrap and
optionally performs remote refresh, exactly as the shared importer's
version comparison naturally resolves.

## Alternatives rejected

* **A generic `ContentSource` interface re-introduced under a new name** —
  rejected; the two responsibilities (reading assets, calling HTTP) are
  concrete and have exactly one implementation each. An interface with one
  implementation and no testing/boundary benefit is exactly what
  `CODING_STANDARD.md` prohibits.
* **Two separate import/replace implementations** (one for bundled, one for
  remote) — rejected; the transactional comparison/replace logic is
  identical regardless of transport, and maintaining it twice would be the
  duplication `CODING_STANDARD.md`'s no-duplication rule exists to prevent.
* **A permanently repeating periodic WorkManager job** — rejected in favour
  of the opportunistic one-time job above.
* **Retaining previous Android content versions with revoked-version
  fallback** — rejected; the backend already owns that history, and
  on-device retention would duplicate it for no product benefit while
  complicating progress-reset semantics (which version's progress does a
  "restored" old version's UI show?).
* **A destructive Room migration or `fallbackToDestructiveMigration`** to
  simplify schema changes for this work — rejected; not needed (no schema
  change was required — see `docs/engineering/CONTENT_MODEL.md`'s
  schema-freeze policy) and remains prohibited regardless (ADR 0003).

## Consequences

* Bundled content and remote sync share exactly one transactional Room
  operation, so a future correction to atomic-replacement or progress-reset
  logic only has one place to change.
* `docs/product/PRD.md` FR-010/FR-011, `docs/engineering/ARCHITECTURE.md`,
  `docs/engineering/OFFLINE_FIRST.md`, and `docs/engineering/CONTENT_MODEL.md`
  all needed updates in the same change to stay internally consistent — see
  each document's own revision.
* Real backend deployment status is still unknown at the time of this
  decision; `BuildConfig.CONTENT_API_BASE_URL` defaults to a non-routable
  `.invalid` host so the project remains buildable and the app remains
  fully offline-functional with no backend configured. Supplying the real
  `SANGU_CONTENT_API_BASE_URL` Gradle/CI property activates real remote
  sync with no further Android code change.
* Any future correction to the backend's manifest/package response shape
  must be reflected in `RemoteContentManifestDto`/`ContentPackageDto` and
  `docs/content-schema.md` together — the schema doc is the contract both
  the Android team and the (not yet built) Go publication pipeline must
  honour.

## Amendment (2026-07-28): sync simplification

Approved by the product owner and tech lead: the remote-sync implementation
above had accumulated more machinery than the actual requirement (the
manifest is checked at most once every 24 hours) justified. This amendment
does not reopen the decision above — bundled-plus-remote sharing one
`ContentPackageImporter`, Room as sole source of truth, one active version
per variant, atomic replacement, and the 24-hour opportunistic worker are
all unchanged. It simplifies the *implementation* of the remote path:

* **ETag removed entirely.** The manifest is small and is fetched at most
  once per 24 hours, so conditional-request caching added complexity with
  no real bandwidth benefit. `If-None-Match`, `304` handling,
  `ManifestFetchOutcome.NotModified`/`ContentSyncOutcome.NotModified`,
  `ContentSyncStatus.NOT_MODIFIED`, `ManifestSyncInfo.etag`, and the stored
  `content_manifest_etag` metadata key are all deleted. A normal sync now
  always issues a plain `GET v1/content/manifest` and reads the `200`
  response; the backend contract no longer needs to support conditional
  requests at all. `content_manifest_version` is deleted too — the
  simplified remote manifest (section 10 of the task brief that drove this
  amendment) has no `manifestVersion` field left to store, and nothing else
  read it.
* **`ContentRemoteDataSource` deleted.** It was a typed wrapper around
  exactly one Retrofit service with exactly one caller — the HTTP handling
  (manifest fetch, package streaming into a size-limited temporary file,
  HTTP/`IOException` classification) moved directly into the renamed
  `ContentSyncManager`, which is still the only place in `data/sync`/
  `data/remote` that touches a Retrofit `Response`/`ResponseBody`/HTTP
  status code — those types still never reach domain, repository
  contracts, ViewModels, or Compose UI.
* **`ContentSyncCoordinator` renamed to `ContentSyncManager`.** Same single
  responsibility (own one complete remote-sync execution), same shared
  `ContentPackageImporter` dependency; the rename reflects that it now also
  owns the HTTP handling `ContentRemoteDataSource` used to own.
* **Six-outcome `ContentSyncOutcome` replaced by three-case `SyncResult`.**
  `Completed(updatedVersionIds, skippedVersionIds, rejectedVersionIds)`
  replaces the former `NotModified`/`NoChanges`/`Updated`/`PartialFailure`
  distinction — a partial package-level failure is just a non-empty
  `rejectedVersionIds` alongside whatever did update, not a separate
  outcome type. `RetryableFailure`/`PermanentFailure` replace the former
  `Failed(RemoteContentFailure)` and `CompleteFailure`, and are used
  consistently for both manifest-level and package-level failures.
* **Package-download retry behaviour fixed.** The previous implementation
  isolated a package-level HTTP failure as if it were always a permanent,
  per-package rejection (`Rejected(entry.versionId, describe(failure))`),
  which meant a genuinely transient package timeout or `500` was never
  retried at the sync level. `ContentSyncManager` now classifies a
  package-level failure the same way a manifest-level failure is
  classified: a retryable one (`IOException`, timeout, HTTP
  408/429/5xx, a temporary download interruption) aborts the whole
  `sync()` call with `SyncResult.RetryableFailure`, so `ContentSyncWorker`
  retries the entire sync — packages already imported earlier in the same
  attempt are simply skipped on the retry, since Room already matches
  them. A permanent package failure (checksum mismatch, malformed JSON,
  non-retryable HTTP 4xx, minimum-app-version too high) still only rejects
  that one package and lets the rest of the manifest continue.
* **Bundled manifest pre-comparison added.** `BundledManifestEntryDto`
  gained `variantId`/`versionNumber` so `BundledContentBootstrapper` can
  compare against `ContentPackageImporter.activeVersionSummary` *before*
  reading a bundled package's asset bytes — an older or already-current
  bundled entry is now skipped without ever opening its file, matching the
  bandwidth-avoidance optimisation the remote path already had. The
  comparison itself (`decideContentVersionAction`, `data/content/`) is one
  pure function shared by both callers rather than duplicated; either way,
  `ContentPackageImporter.importPackage` still re-runs its own
  authoritative comparison and checksum verification — this is an
  optimisation, not a safety relaxation.
* **`ContentSyncMetadata` simplified.** Only `content_last_sync` remains
  (`value` one of `SUCCESS`/`PARTIAL`/`FAILED`, per the new three-case
  `SyncResult`); `content_manifest_etag` and `content_manifest_version` are
  both gone, and the 24-hour gate reads only the terminal-sync timestamp,
  as it always did.

None of this changes: bundled content remaining mandatory, one active
version per variant, atomic transactional replacement (including
version-scoped progress deletion), the 24-hour opportunistic one-time
worker (`ExistingWorkPolicy.KEEP`, not periodic), or API failure leaving
Room untouched. The real Go backend remains undeployed; this amendment
only changes what the Android client sends and how it classifies the
responses it gets back.

## Amendment (2026-08-02): backend dropped, static Firebase Hosting instead

Approved by the product owner and tech lead, recorded fully in ADR
[0014](0014-firebase-hosting-static-content-delivery.md): the Go +
Supabase backend this decision was written against (ADR 0011) is dropped
entirely, before it was ever implemented. Every reference above and in
`docs/engineering/ARCHITECTURE.md` §Remote content synchronisation to "the
backend" now means **static files on Firebase Hosting**, not a dynamic Go
service — see ADR 0014 for the full reasoning.

This amendment does not reopen anything else this decision or the prior
amendment settled: `ContentPackageImporter` remains the one shared
transactional Room operation for both bundled and remote content; Room
remains the sole source of truth; Android keeps one active version per
variant with atomic replacement; the 24-hour opportunistic
`ExistingWorkPolicy.KEEP` worker is unchanged; `ContentSyncManager` keeps
the same three-case `SyncResult` and the same retryable/permanent failure
classification. `ContentApiService`'s two `GET` endpoints
(`v1/content/manifest`, `v1/content/packages/{versionId}`) are unchanged —
static files at those same paths on Firebase Hosting satisfy the same
Retrofit contract with no Android code change, only a different
`SANGU_CONTENT_API_BASE_URL`.

What does change, once the actual migration is scheduled as its own task:
`RemoteContentManifestDto` keeps its current shape (it already matches a
static manifest file exactly); "the backend keeps immutable history" in
this document and in `docs/engineering/CONTENT_MODEL.md` now means "git
history of the `content-hosting/` directory," not a Postgres table — there
is no database anywhere in this architecture any more.
