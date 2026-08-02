# Firebase MCP Tooling

Applies to any task involving `content-hosting/`, Firebase Hosting
deployment, or an MCP (Model Context Protocol) server configured against
this project's Firebase project. Read alongside
`docs/engineering/ARCHITECTURE.md` §Backend and ADR
[0014](../decisions/0014-firebase-hosting-static-content-delivery.md),
which this document assumes.

## What this is, in one sentence

A Firebase MCP server is a development/CI convenience that lets AI or
developer tooling inspect the Firebase Hosting project and propose or
validate changes to `content-hosting/` — it is not part of the Android
application, does not replace Retrofit/OkHttp, and never ships in the APK.

## The Android/MCP boundary — read before touching either side

| Area                                       | Allowed                                                          |
|--------------------------------------------|------------------------------------------------------------------|
| Firebase MCP server                        | Development and CI tooling only                                  |
| Firebase Hosting (`content-hosting/`)      | Static content delivery (ADR 0014)                               |
| Firebase SDK in `app/` (Android)           | Not part of this decision either way — see note below            |
| Firestore                                  | Rejected (ADR 0014, Alternatives rejected)                       |
| Cloud Functions                            | Rejected (ADR 0014, Alternatives rejected)                       |
| Retrofit/OkHttp as the Android sync client | Unchanged — still the only way `app/` fetches content (ADR 0012) |

Rules that follow directly from ADR 0014:

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
never merged into `app/src/**` and is never bundled into the APK:

```text
content-hosting/
├── firebase.json          # Hosting config: public dir, rewrites, ignore list
├── .firebaserc             # Firebase project alias (not committed if it embeds a project id you don't want public — see Secrets below)
└── public/
    └── v1/
        ├── config.json     # supported schema version, min app version, feature flags
        └── content/
            ├── manifest.json
            └── packages/
                ├── tahlil-umum-v1
                └── istighosah-umum-v1
```

File formats (`manifest.json`, `config.json`, each package file) are
exactly `docs/content-schema.md`'s existing schema — there is no separate
"MCP schema" or "hosting schema." Filenames under `packages/` must exactly
match the `versionId` values `ContentApiService.getPackage(versionId)`
requests, since Firebase Hosting resolves them as literal static paths, not
templated routes.

## Setting up the Firebase project (one-time, human-run)

```bash
firebase login
cd content-hosting
firebase init hosting
```

When prompted, select only **Hosting**. Do not enable Firestore, Realtime
Database, Functions, or any other product — ADR 0014 rejected all of them
for this project. Point the public directory at `public/`.

## Setting up the Firebase MCP server

Firebase ships an official MCP server as part of `firebase-tools`. The
exact launch command and available tool set can change between
`firebase-tools` releases — run `firebase --help` (or check the installed
version's own docs) rather than assuming a specific invocation stays
correct over time. Register it as a project-scoped MCP server for Claude
Code in `.mcp.json` at the repository root, e.g.:

```json
{
    "mcpServers": {
        "firebase": {
            "command": "firebase",
            "args": [
                "experimental:mcp"
            ]
        }
    }
}
```

Verify the exact `args` against your installed `firebase-tools` version
before relying on it — do not copy this verbatim into a CI script without
confirming it still launches an MCP server, not something else.

Scope the server's usefulness to `content-hosting/`: when directing an
agent to use it, tell it explicitly that it may read and propose changes
only under `content-hosting/**`, per the boundary table above.

## Deployment

```bash
cd content-hosting
firebase deploy --only hosting
```

Run only after CI validation passes (see below) — never deploy an
unvalidated file, and never run this as an interactive step that skips
validation "just this once."

## CI validation (replaces the never-built Go admin CLI)

A CI script must validate `content-hosting/public/v1/content/` before every
deploy, enforcing the same gates the previously planned Go admin CLI's
`content validate` would have (ADR 0014, `docs/engineering/ARCHITECTURE.md`
§Backend):

* `schemaVersion` matches the supported version (`docs/content-schema.md`).
* `manifest.json` is valid JSON and every entry's `checksumSha256` matches
  the referenced package file's actual SHA-256.
* Every `manifest.json` entry has a corresponding file under `packages/`
  and vice versa — no dangling references either direction.
* No duplicate `variantId`/`versionId` pairs.
* No `versionNumber` regression versus what is already deployed.
* Required Arabic text and translation fields are non-empty
  (`docs/content-schema.md` structural validation rules,
  `ContentPackageValidator`'s existing checks are the reference
  implementation for what "valid" means — this CI script should not
  invent different validation criteria than the Android importer already
  enforces).

This script is intentionally simple tooling (e.g. Node or Python), not a
second copy of `ContentPackageImporter`'s Kotlin logic — Android's importer
remains the authoritative, final validation gate regardless of what CI
checks, exactly as it already is for bundled content
(`docs/content-schema.md` Import behaviour).

## Secrets

The Firebase Hosting deploy credential (a CI service account or deploy
token) is the only secret this tooling introduces. It must be stored as a
CI secret, never committed, and never exposed in Claude/agent output —
same handling as any other production credential
(`docs/operations/PRODUCTION_READINESS.md` §Production credential
ownership). Public content itself (manifest, packages, config) requires no
secret to read — it is public and unauthenticated by design.

## What this document does not cover

Implementing `content-hosting/`'s actual files, the CI validation script,
and the real Firebase project deployment are separate, explicitly-requested
tasks — this document records the setup and the boundary, not a claim that
any of it has been built yet. See `docs/PROGRESS.md`'s Firebase Hosting
pass entry for current status.
