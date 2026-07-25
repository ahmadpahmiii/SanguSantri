# Content Governance

Applies to any content task: entering amaliyah text, handling feedback,
publishing a version, or responding to a reported error. Read alongside
`CLAUDE.md` Content Safety and `docs/engineering/CONTENT_MODEL.md`.

## AI is not an approval authority

Claude MUST NOT invent Arabic readings or translations, generate missing
prayers from memory, automatically scrape and publish website text, correct
religious content based solely on AI judgement, merge different versions
without written instruction, or add Latin transliteration. Claude may
structure, format, and validate content that a human has supplied — Claude
never originates or approves religious content. This is absolute and has no
phase-based exception.

## Editorial workflow

```text
Source selected
→ Manually transcribed
→ Structured content created
→ Automated validation
→ Internal review
→ Kyai/sesepuh review
→ Signed approval recorded
→ Immutable content version generated
→ Published
→ Bundled or synchronised
```

## Correction workflow

```text
Feedback received
→ Triaged by content team
→ Compared with source
→ Reviewed by kyai/sesepuh when required
→ New version created
→ New approval attached
→ Published
→ Client automatically activates new version
```

Corrections always create a new immutable version (ADR
[0008](../decisions/0008-immutable-content-versions.md)) — an approved
version is never edited in place.

## Severity levels for content errors

Use these to decide how fast a correction must move, not whether it needs
approval — every production correction still needs kyai/sesepuh review
before publication, regardless of severity:

* **Critical** — Arabic text is doctrinally wrong, a harakat error changes
  meaning, or a mistranslation reverses meaning. Publish a corrected
  version as fast as the approval workflow allows; consider revoking the
  faulty version in the interim if the correction will take more than a
  few days.
* **Moderate** — a harakat error that does not change meaning, an awkward
  but not wrong translation, a missing repetition-count clarification.
  Correct in the next regular content update.
* **Minor** — formatting, source-citation wording, non-doctrinal metadata.
  Batch with other minor fixes.

## Publication and revocation authority

* **Publication authority**: whoever holds the Go admin CLI's `content
  publish` credentials (backend, not yet built) or, until then, whoever
  commits an approved seed package to the repository. This must be a named
  person, not "whoever is available" — record the name once the operational
  team exists.
* **Revocation authority**: the same named authority as publication.
  `AmaliyahVersionStatus.REVOKED` and FR-011's fallback-to-previous-
  approved-version logic are already implemented in the schema and seed
  importer — the human process (who decides, how fast, what triggers it) is
  what this document defines, not the mechanism.
* **Emergency correction**: a Critical-severity error found in a published
  version triggers immediate revocation consideration by the named
  authority, independent of the normal weekly-release cadence (PRD §4.3).
  The application must already fall back correctly to the newest
  non-revoked approved version (FR-011) — verify this behaviour, don't
  assume it, before relying on it in an emergency.
* **Two-person publication**: once the operational team is more than one
  person, production content publication requires two-person sign-off (the
  content reviewer and the kyai/sesepuh approver are already distinct roles
  per PRD §6.5 — this requirement formalizes that as a publication gate,
  not just a data field). Not enforceable in tooling yet since there is no
  publication tooling; enforce by process until then.

## Copyright and licensing

Public availability of a source on a website is not automatic republication
permission. Before a monetised release, the product owner (not Claude) must
verify: permission to reproduce the source's editorial arrangement,
permission to reproduce its Indonesian translation, permission to
distribute audio, attribution requirements, and whether independent
transcription or translation is required. This is release governance, not a
task Claude may resolve by assumption — track licensing status per source
alongside the approval record, not separately.

## Approval vs. endorsement

Approval verifies the accuracy of a specific content version. It must never
be presented as institutional endorsement of the entire SanguSantri
application unless such endorsement exists in writing (PRD §6.5). This
distinction must be visible wherever approval is shown to users (Sumber &
Pentashihan, FR-009).

## Approval document privacy

The raw signed approval document is stored privately. Users may view a
redacted approval document, approver identity, role, date, approval scope,
and document reference number. Private signatures, phone numbers,
addresses, and identity numbers are redacted when unnecessary (PRD §6.6).
