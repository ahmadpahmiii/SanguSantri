# Firebase MCP Tooling

Applies to any task involving `content-hosting/`, Firebase Hosting
deployment, or an MCP (Model Context Protocol) server configured against
this project's Firebase project. Read alongside
`docs/engineering/ARCHITECTURE.md` §Backend and ADR
[0014](../decisions/0014-firebase-hosting-static-content-delivery.md)/ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md), which
this document assumes.

## What this is, in one sentence

A Firebase MCP server is a development/CI convenience that lets AI or
developer tooling inspect the Firebase Hosting project and propose or
validate changes to `content-hosting/` — it is not part of the Android
application, does not replace Retrofit/OkHttp, and never ships in the APK.

## The Android/MCP boundary — read before touching either side

| Area                                        | Allowed                                                           |
|----------------------------------------------|--------------------------------------------------------------------|
| Firebase MCP server                          | Development and CI tooling only                                   |
| Firebase Hosting (`content-hosting/`)        | Static content delivery (ADR 0014/0015)                            |
| Firebase SDK in `app/` (Android)             | Not part of this decision either way — see note below              |
| Firestore                                    | Rejected (ADR 0014, Alternatives rejected)                         |
| Cloud Functions                              | Rejected (ADR 0014, Alternatives rejected)                         |
| Retrofit/OkHttp as the Android sync client   | Unchanged — still the only way `app/` fetches content (ADR 0012/0015) |

Rules that follow directly from ADR 0014/0015:

* The Firebase MCP server is never a Gradle dependency of `app/` and is
  never invoked from a ViewModel, Repository, DAO, or any other production
  Kotlin code.
* It does not replace `ContentApiService`/`ContentSyncManager` — Android
  still fetches content over plain HTTPS `GET`, exactly as ADR 0012 built
  it, whether the response comes from a dynamic API or (now) a static file.
* Its write/propose scope is `content-hosting/**`. It has no standing
  authority to modify Android source, Gradle configuration, or CI workflow
  files — a human applies those changes, same as any other AI-assisted
  edit in this repository.
* This document has no bearing on `app/build.gradle.kts`'s separate,
  already-in-progress Firebase Crashlytics Android SDK integration
  (`google-services` plugin, `firebase-crashlytics` dependency,
  `app/google-services.json`). That is a crash-reporting concern for the
  Android runtime, tracked independently of content delivery, and this
  document does not authorize, endorse, or depend on it either way.

## Repository layout

`content-hosting/` is a new top-level directory, parallel to `app/` — it is
never merged into `app/src/**` and is never bundled into the APK. A real
Firebase project (`sangusantri-81cc6`, `.firebaserc`) is already linked to
this directory:

```text
content-hosting/
├── firebase.json          # Hosting config: public dir, ignore list, cache headers
├── .firebaserc             # Firebase project alias (already configured — do not commit a
│                           # different project id over it without confirming with the team)
├── public/
│   ├── index.html          # Default Firebase Hosting placeholder — harmless, not app-specific
│   ├── 404.html
│   └── content/
│       ├── catalog.json    # ContentCatalogDto shape (docs/content-schema.md, ADR 0015)
│       ├── packages/
│       │   ├── tahlil-v1.json
│       │   └── istighosah-v1.json
│       └── images/         # empty for now — no bundled amaliyah has an image yet
└── scripts/
    └── validate-content.mjs
```

File formats (`catalog.json`, each package file under `packages/`) are
exactly `docs/content-schema.md`'s existing schema — there is no separate
"MCP schema" or "hosting schema." Filenames under `packages/` must exactly
match the `contentUrl` each catalog item declares, since Firebase Hosting
resolves them as literal static paths, not templated routes.

## Setting up the Firebase project (one-time, human-run)

Already done for this project (`sangusantri-81cc6`). For a new environment:

```bash
firebase login
cd content-hosting
firebase init hosting
```

When prompted, select only **Hosting**. Do not enable Firestore, Realtime
Database, Functions, or any other product — ADR 0014 rejected all of them
for this project. Point the public directory at `public/`.

## Setting up the Firebase MCP server

Firebase's official MCP server ships as part of `firebase-tools` and is
also distributed as a Claude Code plugin. Prefer the plugin install:

```bash
claude plugin marketplace add firebase/firebase-tools
claude plugin install firebase@firebase
```

Or configure it manually as a project-scoped MCP server:

```bash
claude mcp add firebase npx -- -y firebase-tools@latest mcp
```

Verify it registered correctly:

```bash
claude mcp list
```

Do not hand-write a custom MCP server configuration with invented fields
(e.g. a `contentMapping` or `syncRules` block) — those are not recognised
by the official server and only create a maintenance burden for a config
shape nothing reads. Scope the server's usefulness to `content-hosting/`:
when directing an agent to use it, tell it explicitly that it may read and
propose changes only under `content-hosting/**`, per the boundary table
above.

## Deployment

```bash
cd content-hosting
node scripts/validate-content.mjs
firebase deploy --only hosting
```

Run only after `validate-content.mjs` passes (see below) — never deploy an
unvalidated file, and never run this as an interactive step that skips
validation "just this once." Deploying to `sangusantri-81cc6` is a
real, shared-system action — confirm with the team before running it, the
same as any other production deployment.

## CI validation (replaces the never-built Go admin CLI)

`scripts/validate-content.mjs` validates `public/content/` before every
deploy, enforcing the same gates the previously planned Go admin CLI's
`content validate` would have (ADR 0014/0015,
`docs/engineering/ARCHITECTURE.md` §Backend):

* `schemaVersion` matches the supported version (`docs/content-schema.md`).
* `catalog.json` is valid JSON with no duplicate catalog item `id`.
* Every catalog item's `contentUrl` resolves to an existing file, and that
  file's `id`/`version` match the catalog entry that named it.
* Required Arabic text and translation fields are non-empty, and every
  step's `repeatTarget` is at least `1`.
* No duplicate step `id` within one content file.
* Optional version-regression check against a previously deployed
  `catalog.json` (`node scripts/validate-content.mjs --previous <path>`).

This script is intentionally simple tooling (plain Node, no dependencies),
not a second copy of `ContentValidator`/`ContentImporter`'s Kotlin logic —
Android's importer remains the authoritative, final validation gate
regardless of what CI checks, exactly as it already is for bundled content
(`docs/content-schema.md` Import behaviour).

## Secrets

The Firebase Hosting deploy credential (a CI service account or deploy
token) is the only secret this tooling introduces. It must be stored as a
CI secret, never committed, and never exposed in Claude/agent output —
same handling as any other production credential
(`docs/operations/PRODUCTION_READINESS.md` §Production credential
ownership). Public content itself (catalog, packages) requires no secret
to read — it is public and unauthenticated by design.

## What this document does not cover

The CI workflow file that runs `validate-content.mjs` and
`firebase deploy` automatically on merge is not yet written — see
`docs/engineering/RELEASE_ENGINEERING.md`. See `docs/PROGRESS.md`'s
Firebase Hosting pass entries for current status.
