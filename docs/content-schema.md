# Content Schema (`schemaVersion: 1`)

Defines the canonical content-package JSON format (PRD 12.2, FR-001, FR-010)
consumed identically by two producers: bundled Android assets, and static
files served from `content-hosting/` via Firebase Hosting's
`v1/content/packages/{versionId}` path (ADR 0014) — there is no dynamic
publication pipeline; both producers are hand-authored JSON files, CI-
validated before being committed/deployed.
`ContentPackageValidator`/`ContentPackageImporter` (`data/content/`) are the
one shared validation/import boundary for all three — there is no
bundled-only or remote-only copy of this schema. This is the only place
Arabic/Indonesian amaliyah text may live — never inside Kotlin source (PRD
12.2, CLAUDE.md).

## Two manifests, one package contract

The **package** JSON below (`ContentPackageDto`) is identical whether it
arrives from a bundled asset file or a downloaded package response. The
**manifest** that lists packages is deliberately transport-specific — the
bundled manifest and the remote (Firebase Hosting) manifest have different
fields because their concerns genuinely differ (a remote manifest needs
`minimumAppVersionCode`; a bundled manifest does not, since an
unsupported-for-this-build bundled package would simply never have been
shipped in that build's APK):

* Bundled: `BundledManifestDto`/`BundledManifestEntryDto`
  (`data/local/content/BundledManifestDto.kt`) — `schemaVersion`,
  `generatedAt`, `packages[]` of `{variantId, versionId, versionNumber,
  file, checksumSha256}`. `variantId`/`versionNumber` let
  `BundledContentBootstrapper` compare against Room's active version
  *before* reading the package asset (see Import behaviour below).
* Remote: `RemoteContentManifestDto`/`RemoteContentManifestPackageDto`
  (`data/remote/dto/RemoteContentManifestDto.kt`) — `schemaVersion`,
  `packages[]` of `{contentId, variantId, versionId, versionNumber,
  checksumSha256, minimumAppVersionCode}`. No conditional-request header
  and no manifest-level version/status field — the manifest is fetched
  plainly at most once every 24 hours (2026-07-28 sync simplification, ADR
  0012 amendment) and lists only each variant's currently active published
  package; full immutable revision history lives in `content-hosting/`'s
  git history instead (ADR 0014), never sent to Android. Full endpoint
  contract:
  `docs/engineering/ARCHITECTURE.md` §Remote content synchronisation,
  `CLAUDE.md` §7.

Neither manifest DTO is forced into one nullable shape with fields like
both `assetFile` and `downloadUrl` — see ADR
[0012](decisions/0012-bundled-bootstrap-and-remote-sync.md) for the
reasoning.

## Layout and debug/release split (introduced Milestone 4.5, published Milestone 6)

Android merges asset source sets per build type; a file at the same relative
path in `debug` wins over `main` for debug builds, and `main` alone is used
for release. This project uses that to keep any *unapproved* draft content
out of release builds while it is still being prepared, per CLAUDE.md's
debug content policy:

```text
app/src/main/assets/content/     # published content, visible in every build
├── manifest.json                # packages: [tahlil-general-v1, istighosah-general-v1]
├── tahlil-general-v1.json       # status: PUBLISHED (Milestone 6 baseline)
└── istighosah-general-v1.json   # status: PUBLISHED (Milestone 6 baseline)

app/src/debug/assets/content/    # currently empty — reserved for a future
                                  # package still being drafted/reviewed,
                                  # not yet accepted for publication
```

Tahlil and Istighosah moved from `debug/` to `main/` in Milestone 6: both
are now the product owner's accepted, published `0.0.1` content baseline
(standard public amaliyah, `docs/product/PRD.md` §3.1, §6.7), so they are
visible in release builds like any other published content — no debug-only
override is needed for them anymore. The debug/release split mechanism
itself remains available for a future amaliyah still being drafted and not
yet accepted.

`BundledContentBootstrapper` is unaware of this split — it just reads
whatever `content/manifest.json` the build merged in and hands the bytes to
`ContentPackageImporter`. There is no `DRAFT`-vs-`PUBLISHED` special-casing
in the importer itself; the importer accepts a package at any
`version.status` exactly the same way, structural validity (plus checksum
and version-identity) is all it checks. Whether a `DRAFT` package (should
one exist in the future) is ever *rendered* once imported into Room is a
repository-layer concern — `ContentRepositoryImpl.getDefaultVersionDetail`
always resolves `AmaliyahVersionDao.getLatestPublishedForVariant` only, in
every build; there is no debug-only fallback to a non-`PUBLISHED` version
(Content Delivery Foundation, ADR 0012 — Android keeps and renders only one
active version per variant, never a draft alongside it).

## Bundled `manifest.json`

| Field                       | Type   | Notes                                                                                                                                                                                                                                                                                            |
|-----------------------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `schemaVersion`             | int    | Must equal `1`.                                                                                                                                                                                                                                                                                  |
| `generatedAt`               | string | ISO-8601 timestamp, informational only.                                                                                                                                                                                                                                                          |
| `packages[]`                | array  | One entry per content package file.                                                                                                                                                                                                                                                              |
| `packages[].variantId`      | string | Must match `variant.id` inside the package file. Used to look up Room's active version for that variant *before* the package file is read (bandwidth/IO avoidance).                                                                                                                              |
| `packages[].versionId`      | string | Must match `version.id` inside the package file — the importer rejects the package if the id declared inside the file itself disagrees with what the manifest entry named.                                                                                                                       |
| `packages[].versionNumber`  | int    | Must match `version.versionNumber` inside the package file. Compared against Room's active version for `variantId` before the file is read: lower or equal-with-matching-checksum skips the read entirely; equal with a different checksum is an immutable-version conflict, logged and skipped. |
| `packages[].file`           | string | Filename under `content/`.                                                                                                                                                                                                                                                                       |
| `packages[].checksumSha256` | string | Lowercase hex SHA-256 of the **raw package file bytes**. The importer rejects the package if this does not match.                                                                                                                                                                                |

See "Two manifests, one package contract" above for the remote
`RemoteContentManifestDto` equivalent, which lists the same kind of
per-package checksum plus `minimumAppVersionCode`.

## Package file (e.g. `tahlil-general-v1.json`)

One immutable [`Amaliyah` → `AmaliyahVariant` → `AmaliyahVersion` → ordered
`AmaliyahStep`s] tree (PRD 10.1), plus the version's `Approval`. Foreign keys
(`amaliyahId`, `variantId`, `versionId`) are **not** repeated in the payload —
the importer derives them from nesting when it maps DTOs to Room entities.

```json
{
  "schemaVersion": 1,
  "amaliyah": {
    "id": "tahlil",
    "slug": "tahlil",
    "titleId": "...",
    "titleAr": "...",
    "descriptionId": null,
    "descriptionAr": null,
    "category": "AMALIYAH"
  },
  "variant": {
    "id": "tahlil-umum",
    "slug": "umum",
    "nameId": "...",
    "nameAr": "...",
    "ownerType": "PUBLIC",
    "pondokId": null,
    "visibility": "PUBLIC",
    "isDefault": true
  },
  "version": {
    "id": "tahlil-umum-v1",
    "versionNumber": 1,
    "status": "PUBLISHED",
    "sourceName": "...",
    "sourceReference": "...",
    "minimumAppVersionCode": 1,
    "publishedAt": "2026-07-25T00:00:00Z",
    "revokedAt": null
  },
  "approval": {
    "id": "tahlil-umum-v1-approval",
    "approverName": "...",
    "approverRole": "...",
    "institutionName": null,
    "approvalDate": "2026-07-25",
    "approvalScope": "...",
    "publicDocumentStorageKey": null,
    "documentReferenceNumber": "...",
    "status": "PENDING"
  },
  "steps": [
    {
      "id": "tahlil-umum-v1-step-01",
      "position": 1,
      "stepType": "HEADING",
      "titleId": "...",
      "titleAr": "..."
    }
  ]
}
```

### `amaliyah.category` (forward note — Figma product-alignment pass)

Currently a free-form string; both bundled packages use `"AMALIYAH"`.
Jelajahi Amaliyah (`docs/product/PRD.md` FR-020) needs a real taxonomy
(e.g. `Tahlil & Doa`, `Shalawat`, `Ratib & Wirid`, `Musiman`) — see
`docs/engineering/CONTENT_MODEL.md`'s "Category taxonomy" section for why
this is a content-metadata edit, not a schema change or a new content
version. No change was made to the bundled JSON packages by this
documentation pass; updating `category`'s actual value is a Phase B
implementation task.

### Step types (PRD 10.2)

`HEADING`, `INSTRUCTION`, `ARABIC_TEXT`, `QURAN_AYAH`, `PRAYER`,
`REPEATED_READING`, `DIVIDER`, `CLOSING`. Shared as a single enum across the
DTO, Room entity, and domain layers (`domain/model/StepType.kt`) — the
vocabulary has no per-layer meaning, so it is not duplicated three times.

### Structural validation (`ContentPackageValidator`)

Enforced before any database write:

* `schemaVersion` must equal the importer's supported version (`1`).
* `amaliyah.id`/`slug`, `variant.id`/`slug`, `version.id`, `approval.id` non-blank.
* `version.versionNumber` and `steps` non-empty.
* Step `position` values are unique and positive.
* Per `stepType`, the fields that type is meaningless without are required
  (e.g. `ARABIC_TEXT`/`PRAYER`/`REPEATED_READING` need `arabicText`;
  `QURAN_AYAH` needs `arabicText` + `quranSurahNumber` + `quranAyahStart`;
  `REPEATED_READING` needs a positive `repeatTarget`).

## Import behaviour (`ContentPackageImporter`, shared by bundled and remote — ADR 0012)

1. (Bundled) Read `manifest.json`; validate `schemaVersion`. (Remote) fetch
   the manifest plainly — no conditional-request header, since it is
   checked at most once every 24 hours (the scheduler's own gate). Both
   paths then compare each entry's `variantId`/`versionNumber`/
   `checksumSha256` against Room's active version for that variant
   (`decideContentVersionAction`, shared by both) *before* reading the
   package's bytes — an older or already-current entry is skipped without
   ever reading its asset file or downloading its bytes.
2. For each entry actually worth reading, read the package's raw bytes
   (from the asset or a downloaded, size-limited temporary file) and
   verify `SHA-256(bytes) == checksumSha256`. Mismatch → package rejected,
   nothing written, import continues with the next package.
3. Parse the package JSON; run structural validation; verify the parsed
   `version.id` matches the manifest entry that named it. Failure → package
   rejected, nothing written.
4. **Version comparison** against Room's currently active version for that
   variant (`AmaliyahVersionDao.getActiveForVariant`): no existing version →
   import; a lower incoming version → skipped, Room never downgraded; equal
   version with a matching checksum → skipped (already up to date, safe to
   re-run on every launch or sync); equal version with a different checksum
   → rejected as an immutable-version contract violation, Room unchanged; a
   higher incoming version → replace.
5. A fresh import inserts amaliyah → variant → approval → version → steps.
   A replace additionally deletes the previous version's version-scoped
   reading progress, its version row, and its approval row. Either way, all
   writes for one package happen inside one
   `SanguSantriDatabase.withTransaction { }` block — any failure rolls back
   the entire package atomically; no partial rows remain (PRD 12.4).

Each package is imported independently: one malformed or stale package must
never block or partially corrupt another (PRD 12.4). Full algorithm and
failure/retry semantics: `docs/engineering/OFFLINE_FIRST.md`.

## Content safety

The packages bundled under `app/src/main/assets/content/` (Tahlil,
Istighosah) are transcriptions from `tools/content-importer/` against
identified, publicly accessible sources (NU Online, Quran NU Online),
manually inspected for structural problems and explicitly accepted by the
product owner as the `0.0.1` published release baseline — standard public
amaliyah under the risk-based publication model
(`docs/product/PRD.md` §3.1, `docs/operations/CONTENT_GOVERNANCE.md`).
`version.status` is `PUBLISHED`. `approval.status` (religious-authority
approval) remains `PENDING` — optional for this content category, not
required for publication — and the app must never present it as if a
kyai/sesepuh had approved it. Claude must not invent or transcribe
religious text from memory (CLAUDE.md, PRD §6.3); both packages' Arabic
text and translations remain exactly as extracted from their source.
Any future package still being drafted and not yet accepted for
publication belongs under `app/src/debug/assets/content/` instead (see
above), so it never reaches a release build before that decision is made.
