# 0008: Immutable, versioned content

## Status

Accepted

## Context

PRD §10 (Reader Content Model) and the content editorial workflow require that an approved
content version never change in place — corrections must produce a new
version. ADR 0006 implements the schema this requires but states the
decision only implicitly (through `AmaliyahVersionStatus` and the
DRAFT/PUBLISHED/REVOKED lifecycle). This decision needs to be explicit
because it is the guarantee the entire approval, revocation, and
"previous versions remain accessible" (FR-011) feature set depends on.

## Decision

Once an `AmaliyahVersion` reaches `PUBLISHED`, its content (steps, Arabic
text, translations, checksum) is never mutated. Any correction, regardless
of severity (`docs/operations/CONTENT_GOVERNANCE.md`), creates a new
`AmaliyahVersion` row with an incremented `versionNumber`, a new approval
record, and a new checksum. The previous version is retained and remains
reachable via **Sumber & Pentashihan** (FR-011) unless explicitly revoked.
Revocation sets `status = REVOKED` and `revokedAt`; it does not delete the
row.

## Consequences

* The importer and future sync client can treat `version.id` as a stable,
  content-addressed key for idempotency (already implemented, ADR 0006).
* Storage grows monotonically with corrections — acceptable at this
  content volume (two amaliyah); revisit only if version history genuinely
  becomes a storage concern, not preemptively.
* No code path may `UPDATE` an `amaliyah_steps` or `amaliyah_versions` row
  for a version already at `PUBLISHED` or later status. A future migration
  that appears to require this is a sign the schema or workflow needs
  reconsideration, not an exception to this rule.
