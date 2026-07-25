# Content Schema (`schemaVersion: 1`)

Defines the bundled JSON format under `app/src/main/assets/content/` that the
offline seed importer reads (PRD 12.2, FR-001). This is the only place
Arabic/Indonesian amaliyah text may live — never inside Kotlin source (PRD
12.2, CLAUDE.md).

## Layout

```text
app/src/main/assets/content/
├── manifest.json
├── tahlil-general-v1.json
└── istighosah-general-v1.json
```

## `manifest.json`

| Field | Type | Notes |
|---|---|---|
| `schemaVersion` | int | Must equal `1`. |
| `generatedAt` | string | ISO-8601 timestamp, informational only. |
| `packages[]` | array | One entry per content package file. |
| `packages[].versionId` | string | Must match `version.id` inside the package file. |
| `packages[].file` | string | Filename under `content/`. |
| `packages[].checksumSha256` | string | Lowercase hex SHA-256 of the **raw package file bytes**. The importer rejects the package if this does not match. |

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

### Step types (PRD 10.2)

`HEADING`, `INSTRUCTION`, `ARABIC_TEXT`, `QURAN_AYAH`, `PRAYER`,
`REPEATED_READING`, `DIVIDER`, `CLOSING`. Shared as a single enum across the
DTO, Room entity, and domain layers (`domain/model/StepType.kt`) — the
vocabulary has no per-layer meaning, so it is not duplicated three times.

### Structural validation (`SeedContentValidator`)

Enforced before any database write:

* `schemaVersion` must equal the importer's supported version (`1`).
* `amaliyah.id`/`slug`, `variant.id`/`slug`, `version.id`, `approval.id` non-blank.
* `version.versionNumber` and `steps` non-empty.
* Step `position` values are unique and positive.
* Per `stepType`, the fields that type is meaningless without are required
  (e.g. `ARABIC_TEXT`/`PRAYER`/`REPEATED_READING` need `arabicText`;
  `QURAN_AYAH` needs `arabicText` + `quranSurahNumber` + `quranAyahStart`;
  `REPEATED_READING` needs a positive `repeatTarget`).

## Import behaviour (`SeedContentImporter`)

1. Read `manifest.json`; validate `schemaVersion`.
2. For each entry, read the package file's raw bytes and verify
   `SHA-256(bytes) == checksumSha256`. Mismatch → package rejected, nothing
   written, import continues with the next package.
3. Parse the package JSON; run structural validation. Failure → package
   rejected, nothing written.
4. **Idempotency**: if `version.id` already exists in `amaliyah_versions`,
   the package is skipped (already imported) — safe to re-run on every launch.
5. Otherwise, insert amaliyah → variant → approval → version → steps inside
   one `SanguSantriDatabase.withTransaction { }` block. Any failure during
   insertion rolls back the entire package atomically; no partial rows remain
   (PRD 12.4).

Each package is imported independently: one malformed package must never
block or partially corrupt another (PRD 12.4).

## Content safety

The fixtures currently bundled under `app/src/main/assets/content/` are
**development, non-production placeholders**. Every Arabic/Indonesian text
field is a bracketed placeholder string, not real Tahlil/Istighosah wording —
Claude must not invent or transcribe religious text (CLAUDE.md, PRD 6.3, 25).
`approval.status` in the fixtures is `PENDING`, never `APPROVED`. Production
content requires kyai/sesepuh-approved packages supplied by the content team;
a release-blocking validation gate (failing the build when only
non-production fixtures are present) is not yet implemented — tracked as a
follow-up, not silently skipped.
