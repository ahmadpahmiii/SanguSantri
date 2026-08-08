# Release Engineering

Applies to any release-track task: CI setup, build configuration, signing,
or version bumps.

## Current state

**No CI pipeline exists** (`.github/workflows/` is empty/absent). This is
the single largest concrete release-readiness gap in the project — see
`docs/reviews/audit-resolution.md` and
`docs/operations/PRODUCTION_READINESS.md`. Nothing currently stops a broken
build, failing test, or unformatted file from landing on `master`. Creating
the CI workflow is explicitly a follow-up task, not part of this
documentation pass — it is real implementation work (choosing a runner,
pinning action versions, wiring caches) that deserves its own session with
its own verification, not a rubber-stamped file.

## Quality commands

Android CI must run equivalent tasks to:

```text
lint
detekt
ktlint format check
unit tests (testDebugUnitTest)
Room instrumentation tests
Compose UI tests
assembleDebug
assembleRelease
```

Content-hosting CI (once `content-hosting/` work starts, ADR 0014) must
run:

```text
content schema validation (schema version, required fields, checksums)
manifest/package cross-reference check (no dangling versionId, no version-number regression)
duplicate-id check
firebase deploy --only hosting (on merge to the deploy branch, after the above pass)
```

There is no Go service, no database migrations, and no OpenAPI contract to
validate — the backend those would have belonged to (ADR 0011) was
superseded before implementation.

Claude must not claim that a build or test passes unless the command was
actually executed successfully in this session.

## CI/CD design (target, not yet built)

Use GitHub Actions.

Pull request checks: Android static analysis, Android unit tests,
formatting, debug build, content schema validation over `content-hosting/`,
no uncommitted generated files.

Release workflow: create version tag → build signed Android App Bundle
using protected secrets → generate release notes → upload Android bundle
to the selected Play testing track → run `firebase deploy --only hosting`
for `content-hosting/` → verify the deployed content manifest is reachable
→ promote only after smoke testing.

Production signing credentials must never be exposed to Claude output or
committed.

For `0.0.6`, the Kemenag `username`/`token` are also protected release
inputs. CI/local release builds inject them from a secret store into generated
native build input that is excluded from version control; no real value appears
in Gradle configuration cache output, build scans, artifacts other than the
necessarily obfuscated APK/AAB client, test reports, or logs. Release assembly
must fail with a clear non-secret message when either value or the expected
release signing-certificate digest is absent. Debug/test builds use fake
fixtures and must remain buildable without production access.

## Versioning

Application versions use pre-1.0 semantic progression (`0.0.1`, `0.0.2`,
...). Each public build increments Android `versionCode`. Content versions
are independent of application versions — a content correction does not
require an APK release unless the schema or reader capability changes (PRD
§4.2).

## Release build configuration

* `buildTypes.release.optimization.enable = true` in `app/build.gradle.kts`
  — R8 shrinking/obfuscation must run on release builds. Requires
  `android.r8.gradual.support=true` in `gradle.properties` under AGP 9.2.1's
  new `optimization {}` DSL; re-check this flag on every AGP upgrade in case
  the DSL graduates out of its incubating gate.
* `app/src/main/keepRules/rules.keep` is currently a stub (commented-out
  WebView example only). Review actual keep-rule needs against the real
  dependency set with the installed `r8-analyzer` skill once real feature
  code exists to shrink.
* No signing config exists yet — tracked as a Blocking Production Input
  (PRD §13), governance-owned, not an engineering defect.
* `0.0.6` adds an NDK/CMake boundary solely for Kemenag credential hardening.
  Keep the C++ surface minimal, strip release symbols, scan the built APK/AAB
  for accidental plain-text credentials, and follow
  `docs/security/SECURITY_BASELINE.md`; never describe this as secret storage.

## Definition of Done

The full release `0.0.1` Definition of Done checklist lives in
`docs/operations/PRODUCTION_READINESS.md` — do not duplicate it here.
