# 0014: Firebase Hosting static content delivery, superseding the Go/Supabase backend

## Status

Accepted. Partially superseded by ADR
[0015](0015-simplified-dynamic-catalog-content-model.md) (2026-08-03): this ADR's decision to use
Firebase Hosting for static delivery stands, but its claim that the existing manifest/package
contract (`variantId`/`versionId`/`checksumSha256`, "no Android code change") would be preserved
unchanged was itself superseded before implementation — read ADR 0015 before assuming the contract
details below are current.

## Context

ADR [0011](0011-go-and-supabase-managed-postgresql-backend.md) committed to
a Go + Supabase-managed PostgreSQL backend for the public content API,
explicitly so that later sessions would not "silently second-guess or
replace piecemeal." That backend was never implemented — no `backend/`
directory has ever existed in this repository. The product owner and tech
lead have now decided, deliberately and not piecemeal, to drop it entirely:
there will be no Go service, no PostgreSQL database, and no Supabase
project. Content that would have been served by a dynamic API is instead
published as static files on **Firebase Hosting**, alongside a **Firebase
MCP** tool used only as development/CI tooling to manage that static
deployment.

This matters less than it might sound, because of what ADR
[0012](0012-bundled-bootstrap-and-remote-sync.md) already built: the
Android sync client (`ContentApiService`, `ContentSyncManager`,
`ContentPackageImporter`) only ever does two things against "the backend" —
`GET v1/content/manifest` and `GET v1/content/packages/{versionId}`, both
plain `GET` requests, both already read into DTOs that match
`docs/content-schema.md` exactly. Nothing in that client depends on the
server being dynamic. Static files at matching paths satisfy the same
Retrofit interface with **no Android code change** — only
`SANGU_CONTENT_API_BASE_URL` needs to point at the Firebase Hosting domain
instead of a Go host.

## Decision

**Static files replace the Go API; nothing else about the Android sync
contract changes.** A new top-level `content-hosting/` directory (parallel
to `app/`, not inside it — it is not bundled into the APK) holds:

* `v1/content/manifest.json` — same shape as `RemoteContentManifestDto`
  (`docs/content-schema.md`): `schemaVersion`, `packages[]` of
  `{contentId, variantId, versionId, versionNumber, checksumSha256,
  minimumAppVersionCode}`.
* `v1/content/packages/{versionId}` — the immutable package JSON
  (`ContentPackageDto`), byte-identical in format to a bundled package file.
* `v1/config.json` — replaces the planned `GET /v1/config` endpoint:
  supported schema version, minimum app version, feature flags,
  maintenance state, all as static data. There is no dynamic
  `/healthz` — Firebase Hosting's own availability is Google-managed and
  not this project's concern to health-check.

These map directly onto Firebase Hosting's `public` directory so the
resolved URLs match the paths `ContentApiService` already requests.
Deployment is `firebase deploy --only hosting`, run from CI after
validation (see Consequences).

**Content authoring becomes direct file editing, not an admin CLI.** The
Go admin CLI (`content validate|import|review|approve|publish|revoke|list|
export`, ADR 0011/0010) is dropped along with the rest of the backend.
Content authors edit/commit JSON files directly under `content-hosting/`
(mirroring how bundled content under `app/src/main/assets/content/` is
already authored — this unifies the two authoring paths onto one format,
not two). A CI validation script is the replacement for the CLI's
validation gates (schema validity, non-empty Arabic/translation text, no
duplicate ids, checksum correctness, no version-number regression) — see
`docs/engineering/CONTENT_MODEL.md` and `docs/operations/
CONTENT_GOVERNANCE.md` for what stays the same in the governance process
itself.

**Git history is the immutable revision record.** ADR 0011's Postgres
`amaliyah_versions` table was the planned mechanism for retaining every
published version forever (ADR 0008). With no database, that responsibility
moves to the `content-hosting/` git history itself: a correction adds a new
version file and updates the manifest to point at it; the previous
version's file is never edited or deleted from history, only stops being
referenced by the current manifest. This satisfies ADR 0008 (immutable
versions, corrections create a new version) without a database — see
`docs/engineering/CONTENT_MODEL.md`'s revised "Historical content record"
section.

**Firebase MCP is tooling only, never a runtime dependency.** An MCP server
configuration lets AI/developer tooling read the Firebase Hosting project
structure and propose or validate static content changes. It:

* runs only in development and CI contexts;
* never ships inside the APK and is not a Gradle dependency of `app/`;
* is never called from a ViewModel, Repository, or any other
  production Kotlin code;
* does not replace Retrofit/OkHttp — Android still fetches content over
  plain HTTPS `GET`, exactly as ADR 0012 built it;
* has read/propose access to `content-hosting/**` only, and no authority to
  modify Android source, Gradle config, or any file outside that directory
  without a human applying the change.

Full setup: `docs/engineering/MCP_TOOLING.md`.

**Firestore, Cloud Functions, and any other Firebase backend product are
out of scope and explicitly rejected** — see Alternatives rejected. Only
Firebase Hosting (static files) is adopted. This decision has no bearing on
the separate, already-in-progress Firebase Crashlytics Android SDK
integration (`app/build.gradle.kts`) — that is a crash-reporting concern,
unrelated to content delivery, and is out of scope for this ADR.

## Alternatives rejected

* **Implementing ADR 0011's Go + Supabase backend as originally planned** —
  rejected; the product owner and tech lead judged the operational cost of
  running and maintaining a service and a managed database unjustified for
  a content model that is, in practice, a small number of infrequently
  updated JSON files. A backend earns its cost when it needs to run
  server-side logic (auth, per-user state, computed data); this project's
  public content has none of that.
* **Firestore or Realtime Database instead of Hosting** — rejected; the
  content model is already file-shaped (`docs/content-schema.md`), has no
  per-document query pattern that benefits from a document database, and
  Firestore would reintroduce a live backend dependency (quotas, security
  rules, a service to reason about) for content that is fundamentally
  static and infrequently updated.
* **Cloud Functions as a thin API shim over the static files** — rejected;
  it would recreate the dynamic-service operational burden this decision
  exists to remove, for no behavioural benefit over serving the files
  directly.
* **Keeping the Go admin CLI as a local-only authoring tool without a
  deployed API** — rejected; maintaining a bespoke Go CLI purely to validate
  JSON that a CI script can validate just as well is exactly the kind of
  parallel-implementation cost ADR 0012 already rejected once for the
  Android side.

## Consequences

* `docs/product/PRD.md` (backend section, FR-010/FR-011),
  `docs/engineering/ARCHITECTURE.md` §Backend, `docs/content-schema.md`,
  `docs/engineering/CONTENT_MODEL.md`, `docs/operations/
  CONTENT_GOVERNANCE.md`, `docs/product/ROADMAP.md`, and the security/
  operations docs that referenced "no backend exists **yet**" all needed
  updates in the same pass to stay internally consistent — see each
  document's own revision, dated with this ADR.
* ADR [0011](0011-go-and-supabase-managed-postgresql-backend.md) is marked
  Superseded by this ADR, not deleted — its reasoning remains a valid
  historical record of a decision that was later reversed.
* ADR [0012](0012-bundled-bootstrap-and-remote-sync.md)'s actual decisions
  (shared `ContentPackageImporter`, Room as sole source of truth, one active
  version per variant, atomic replacement, the 24-hour opportunistic
  worker) are **unchanged** — only its "backend" framing is amended to
  "static Firebase Hosting content source." No Android sync code changes as
  a result of this ADR; only the deployed content location and
  `SANGU_CONTENT_API_BASE_URL` do, whenever that migration is actually
  scheduled as its own task.
* Losing a dynamic API means losing anything that genuinely needs
  server-side logic later (per-user state, real-time push, computed
  aggregation). None of that is in scope for this product today
  (`docs/product/PRD.md` §3.4 no forced account, §5.2 explicitly excluded);
  if a future feature genuinely requires it, that is a new ADR superseding
  this one, not a silent reintroduction of a backend.
* The Go admin CLI's built-in validation gates are replaced by CI-enforced
  checks over `content-hosting/`. Publication authority is now whoever has
  merge/deploy access to that directory and the Firebase project — a named
  person, per `docs/operations/CONTENT_GOVERNANCE.md`, not "the CLI."
* This is a documentation/architecture decision only. No Kotlin source
  under `app/` changes as part of this ADR — implementing the actual
  `content-hosting/` directory, CI validation script, and repointing
  `SANGU_CONTENT_API_BASE_URL` remain separate, explicitly-requested tasks.
