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
