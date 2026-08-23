# 0019: Per-category content API, no per-item versioning, ETag revalidation

## Status

Accepted — implemented 2026-08-23, amended the same day (see *Amendment: the list/detail split*)

## Context

ADR [0015](0015-simplified-dynamic-catalog-content-model.md) defined the content contract the app
still spoke until today: `GET /api/v1/catalog` listing every item's metadata plus a `contentUrl`,
and `GET /api/v1/content/{id}` returning that item's steps. Each catalog entry carried a `version`
integer, and the app re-imported an item only when that integer increased. ADR
[0008](0008-immutable-content-versions.md) framed the same integer as the representation of
"published content is immutable; corrections publish a new version".

Three problems surfaced once the CMS was real and content volume grew past two items.

**The version integer was not actually maintained by the database.**
`docs/engineering/CONTENT_MODEL.md`
in the CMS repo documented a `bump_content_version()` trigger, but no migration ever created it and
`pg_trigger` on the live project does not have it. The bump lived only in the CMS's TypeScript, so
any write that did not go through the CMS UI — a migration touching `content_steps`, a hand-written
SQL fix — reached the API and was then *never re-imported by any device that had already synced*.
A correctness mechanism that depends on a human remembering to increment a counter is not one.

**The catalog/detail split cost one request per item.** A fully published catalogue is 63 items:
1 + 63 requests per sync, to avoid transferring ~190 KB gzipped. Measured against the live
deployment, both categories in full are 80 KB raw / 20 KB gzipped at today's five published items.

**`/api/v1/catalog` was `sholawat ∪ amaliyah`** — a third route serving the same query with a
filter, which the app never called.

## Decision

**Two endpoints, `GET /api/v1/sholawat` and `GET /api/v1/amaliyah`, each returning every published
item in that category with its steps inlined.** `schemaVersion` is `2`. `/api/v1/catalog` and
`/api/v1/content/{id}` are deleted and return `404`.

**`version`, `contentUrl` and `isActive` are gone from the wire.** `isActive` was always `true`
(drafts never reach the API); an unpublished item is now simply absent from the response.

**HTTP `ETag`/`If-None-Match` replaces the per-item version integer.** Both endpoints send a strong
ETag over the response body plus `Cache-Control: public, max-age=0, must-revalidate`. The app
installs an OkHttp `Cache`, which stores the validator and replays it — an unchanged category costs
a `304` with no body, and no ETag handling exists in this app's own code. An ETag is derived from
the bytes actually served, so it cannot be forgotten by a writer the way the integer was.

**Which individual items changed is decided on-device, against Room.** A `304` is transparent at the
Retrofit layer and only says "this category is unchanged"; a `200` does not say which items moved.
`ContentImporter.importRemoteItem` compares each incoming item's steps against the stored ones and
rewrites only on a real difference. This is load-bearing, not an optimisation: replacing every item
on any change would wipe reading positions and guided-session state for content nobody edited.

**`ContentEntity.version` survives as a purely local revision counter**, incremented once per
genuine
content replacement. Nothing transmits it. It is kept because
`amaliyah_completion_events.versionNumber`
records it against every completion, and that history is meant to outlive the content it refers to.
Keeping the column also meant **no Room schema change and therefore no destructive migration** —
which matters now that there are production installs the standing
`fallbackToDestructiveMigration(dropAllTables = true)` policy would otherwise have wiped.

**Absence means hidden, not deleted.** Items missing from a successful response are marked
`isActive = false`; their rows and steps stay. Two guards: deactivation runs only when *both*
category requests succeeded (a failed request is no information, not an empty catalogue), and an
entirely empty published set deactivates nothing (far more likely a CMS mistake than an intentional
unpublish-everything).

## Consequences

**This is a breaking change, made in place rather than behind a `/api/v2/` prefix.** The product
owner elected to push it with a forced in-app update (ADR
[0017](0017-in-app-update-remote-config-and-play-core.md)) rather than run both contracts. A build
that misses the update sees `schemaVersion: 2`, does not recognise it, and keeps its cached content
rather than parsing a response it does not understand.

**ADR 0015's catalog/content-file contract and its `version`-comparison model are superseded**, as
is
ADR 0008's *representation* of immutability. ADR 0008's substance is now a CMS-side editorial rule
rather than something the wire format enforces: the API no longer distinguishes "corrected in place"
from "republished". That is a real reduction in on-device evidence and is accepted deliberately —
the integer never delivered the guarantee anyway, since nothing incremented it outside one code
path.

**The origin-relative `contentUrl` pin is retired with the field.** No remote payload carries a URL
any more, so there is nothing for a tampered response to point elsewhere.
`ContentValidator.isOriginRelativeContentPath` still guards the bundled asset path.

**The bundled-asset contract is unchanged** (`schemaVersion` 1, catalog plus package files). It is a
local file layout with no reason to track the wire, and the two now have separate DTOs and separate
validators.

**Fixed in passing:** `BundledContentBootstrapper.evaluate` called `refreshCatalogMetadata`
unconditionally *before* its version check, so every cold start the bundled catalog overwrote title,
category, order and isActive for items the CMS had already replaced — Tahlil and Istighosah visibly
reverted from "Amaliyah" to "Tahlil dan Doa" on each launch and flipped back only when the daily
sync
next ran. The refresh is now gated on the version comparison. This bug predates this ADR.

---

## Amendment (2026-08-23): the list/detail split, `schemaVersion` 3

The decision above inlined every item's steps into its category endpoint. Shipped and measured, that
was wrong in one specific way, and the fix is small enough to record here rather than in a new ADR.

**Context.** The product owner asked for the category listings to be re-fetched on every Beranda
resume, so that publishing or unpublishing in the CMS reaches a reader without waiting for the
24-hour sync window. With steps inlined, that request is the entire category — and, worse, its
`ETag` moves whenever *any* item's steps change. Correcting one word in one sholawat would make
every device re-download every step of every sholawat. Measured on the live database at 63 published
items: list metadata is 26 KB raw / ~6 KB gzipped; steps are 838 KB / ~190 KB.

**Decision.** Two tiers per category, `schemaVersion` 3:

- `GET /api/v1/{category}` — display metadata only, no steps, no source attribution.
- `GET /api/v1/{category}/{id}` — one item with its steps, repeating the list fields so it can draw
  the reader alone. Scoped to its category: an amaliyah id under `/sholawat/` is a `404`.

Both tiers carry an `ETag`. The point of the split is that the two validators move independently:
a step edit changes only that item's detail, never the list.

**On the client.** `ContentSyncManager` now syncs the two listings only, and is called from
`Lifecycle.Event.ON_RESUME` on Beranda (alongside the existing ayat refresh) rather than from a
24-hour worker gate. `ContentDetailSyncManager` fetches one item's steps and is called by the Full
Reader and the Sholawat reader when they open an item — after Room has already rendered, never
before.

**Consequences.**

A list-only row is now a normal state: an item is visible on Beranda before its steps exist locally,
with empty `sourceName`/`sourceUrl` until its first detail fetch. `sourceName` stays non-null and
empty rather than becoming nullable, because changing the column's nullability changes the Room
schema and this app's standing `fallbackToDestructiveMigration(dropAllTables = true)` would then
wipe
every table — including the durable activity history — on upgrade.

**An item that has never been opened cannot be read offline.** That is inherent to not shipping
every
step to every device, and it is the real cost of this decision. It bites once per item, on first
open, and only without a network. Everything opened before reads offline exactly as it did.

Deactivation semantics, the empty-response guard, the local revision counter, and the reasoning
about
ADR 0008's immutability representation are all unchanged from the original decision above.
