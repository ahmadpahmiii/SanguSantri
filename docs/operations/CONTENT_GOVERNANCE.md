# Content Governance

Applies to any content task: entering amaliyah text, publishing a version,
or responding to a reported error. Read alongside `CLAUDE.md` Content
Safety and `docs/engineering/CONTENT_MODEL.md`.

## AI is not an approval authority

Claude MUST NOT invent Arabic readings or translations, generate missing
prayers from memory, automatically scrape and publish website text, correct
religious content based solely on AI judgement, merge different versions
without written instruction, add Latin transliteration, or claim
kyai/sesepuh approval or institutional endorsement that does not exist.
Claude may structure, format, and validate content that a human has
supplied — Claude never originates religious content and never decides,
on its own judgement, that content is approved or endorsed by anyone. This
is absolute and has no phase-based exception. What Claude *may* do,
following an explicit product-owner instruction, is apply the risk-based
publication model below — that is a publication-governance decision the
product owner makes, not a religious judgement Claude makes.

## Risk-based publication model (Milestone 6, product-owner decision)

Supersedes the previous universal "every version needs kyai/sesepuh
approval before publication" rule. Two categories:

**Standard public amaliyah** — commonly practised, publicly recited
content sourced from an identified, publicly accessible, trusted editorial
source. Publishable when:

* It comes from an identified, publicly accessible, trusted editorial
  source, with the source URL and publisher recorded.
* Extraction results have been manually inspected for structural problems.
* No content was invented by AI; no different versions were silently
  merged.
* Arabic text and translations remain exactly as sourced.
* The product owner explicitly accepts the package as the release
  baseline.

Kyai/sesepuh sign-off is **optional**, not mandatory, for this category.
Tahlil (Umum) and Istighosah (Umum), as currently packaged, are standard
public amaliyah (`docs/product/PRD.md` §6.7).

**Higher-risk content** — kyai, ustaz, sesepuh, or other qualified
religious review remains **required** before publication when content is:

* Private or pesantren-specific.
* Sourced from an unclear or disputed origin.
* Manually modified beyond formatting.
* Compiled by merging multiple versions.
* Translated internally (rather than kept exactly as sourced).
* Doctrinally sensitive.
* Associated with a specific ijazah, sanad, tarekat, or pesantren authority.
* Materially different from the selected published source.

## Editorial workflow — standard public amaliyah

```text
Trusted public source selected
→ Source snapshot obtained
→ Structured extraction
→ Ambiguity report reviewed
→ Source comparison
→ Product-owner editorial acceptance
→ Immutable version published
→ Bundled in application
```

## Editorial workflow — higher-risk content

```text
Source selected
→ Structured content created
→ Internal review
→ Qualified religious review
→ Approval evidence recorded
→ Immutable version published
```

Both workflows still prohibit: AI inventing religious content, AI
correcting religious content from memory, silently merging different
versions, false endorsement claims, invented reviewer identities, invented
evidence, and runtime scraping.

## Developer draft tooling

`tools/content-importer/` (see its own `README.md`) is a small,
developer-only Python tool that turns one allowlisted, publicly available
source page into a local structured JSON draft, without becoming a step in
the editorial workflow above:

```text
Source page (allowlisted, one per source in content_importer/config.py)
→ local HTML snapshot (gitignored, never committed)
→ source-specific parser (one module per source, not a generic scraper)
→ structured draft JSON (schemaVersion 1, gitignored)
→ manual content review  ← editorial workflow above starts here
→ source comparison + ambiguity report reviewed
→ product-owner editorial acceptance (standard public amaliyah) or
  qualified religious review (higher-risk content)
→ Android assets (app/src/main/assets/content/ for accepted/published
  packages; app/src/debug/assets/content/ remains available for a package
  still being drafted and not yet accepted — see docs/content-schema.md's
  debug/release split)
→ existing content importer (`ContentPackageImporter`, shared with remote sync)
→ Room
→ Full Reader / Guided Reader
```

The tool itself never runs at application runtime, never runs
automatically, and is never the approval or publication authority — it
only ever produces a candidate draft. Promoting a draft into
`app/src/main/assets/content/` is always an explicit human decision (the
product owner for standard public amaliyah, or the qualified reviewer
for higher-risk content), never something the tool or Claude does
unilaterally. The tool must not invent missing Arabic text or translation;
when a page section cannot be parsed deterministically, it must be
reported as ambiguous rather than guessed. It fetches only the URLs
allowlisted in `content_importer/config.py`, never a Kemenag or Quran
Foundation API, and never a PDF — PDF may only be kept manually as a
private visual reference (never parsed, never bundled, never displayed in
the reader; see `docs/product/PRD.md` §5.2).

**Istighosah's source.** PRD §6.2's reference (the KH Romli Tamim
Istighosah reading via Quran NU Online) has a specific URL
(`https://quran.nu.or.id/doa/istighotsah-mujahadah`, reading 1 of 7 —
"Istighotsah (KH Romli Tamim)" only) wired into
`tools/content-importer/content_importer/config.py`. The other six
readings on that page (other Mujahadah/reading collections) remain out of
scope — no parser exists for them.

**Public content baseline (Milestone 6).** The current Tahlil (59 steps)
and Istighosah (27 steps) packages have been reviewed for structural
problems and explicitly accepted by the product owner as the `0.0.1`
published release baseline — standard public amaliyah (`docs/product/PRD.md`
§3.1, §6.7), not higher-risk content. `version.status` is `PUBLISHED`;
`approval.status` (religious-authority approval) remains `PENDING` since no
kyai/sesepuh has reviewed either package — that field is optional for this
content category and does not block publication, but the app must never
present it as if a religious authority had signed off. `tools/content-importer/`
remains available as a developer-only tool for preparing future content
updates; it is never invoked automatically or at application runtime.

## Correction workflow

Content correction is an internal SanguSantri-team operation. Users do not
submit corrections through the application — there is no public feedback
form, feedback outbox, or feedback endpoint in `0.0.1`, and none is
currently planned (`docs/product/PRD.md` §6.7, FR-012). Corrections are
triggered by the content team's own review, an internally reported error, or
a source update — never by an in-app user submission:

```text
Internal review, reported error, or source update noticed by the content team
→ Compared with source
→ Reviewed by kyai/sesepuh when required
→ New version created
→ New approval attached
→ Published
→ Client automatically activates new version (once synchronisation exists —
  see docs/product/PRD.md FR-010; until then, updated packages are
  re-bundled as a new local published-baseline package, see above)
```

Corrections always create a new immutable version (ADR
[0008](../decisions/0008-immutable-content-versions.md)) — an approved
version is never edited in place.

## Severity levels for content errors

Use these to decide how fast a correction must move, and which risk
category (§Risk-based publication model) the correction itself falls into
— severity does not automatically waive the higher-risk criteria:

* **Critical** — Arabic text is doctrinally wrong, a harakat error changes
  meaning, or a mistranslation reverses meaning. Determining the correct
  text is itself a doctrinal judgement, so this is always higher-risk
  content (§3.1) — qualified religious review is required before
  publishing the correction, regardless of source category. Publish as
  fast as the approval workflow allows; consider revoking the faulty
  version in the interim if the correction will take more than a few days.
* **Moderate** — a harakat error that does not change meaning, an awkward
  but not wrong translation, a missing repetition-count clarification. If
  the fix simply makes the transcription match the original public source
  more faithfully (not a doctrinal judgement call), it stays
  standard-category and may be accepted by the product owner alone; treat
  as higher-risk whenever there is any doubt. Correct in the next regular
  content update.
* **Minor** — formatting, source-citation wording, non-doctrinal metadata.
  Standard-category; batch with other minor fixes.

## Publication and revocation authority

* **Publication authority**: whoever holds the Go admin CLI's `content
  publish` credentials (backend, not yet built) or, until then, whoever
  commits an approved seed package to the repository. This must be a named
  person, not "whoever is available" — record the name once the operational
  team exists.
* **Revocation authority**: the same named authority as publication.
  `AmaliyahVersionStatus.REVOKED` is implemented in the schema. Android has
  no on-device fallback-to-previous-approved-version logic (superseded
  FR-011, ADR 0012) — a revocation only takes effect for a given device
  once the backend publishes the corrected replacement version and that
  device's own sync gate fetches it; the human process (who decides, how
  fast, what triggers it) is what this document defines, not the mechanism.
* **Emergency correction**: a Critical-severity error found in a published
  version triggers immediate revocation consideration by the named
  authority, independent of the normal weekly-release cadence (PRD §4.3).
  Propagation to devices is bounded by the 24-hour sync gate (FR-010), not
  instantaneous — do not assume an emergency correction reaches devices
  faster than that without verifying it.
* **Two-person publication**: once the operational team is more than one
  person, production content publication requires two-person sign-off. For
  standard public amaliyah, this is the product owner plus a second
  internal reviewer's editorial acceptance; for higher-risk content, it is
  the content reviewer and the kyai/sesepuh approver (already distinct
  roles per PRD §6.5). Not enforceable in tooling yet since there is no
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

## Source verification vs. approval vs. endorsement

Three distinct claims, never collapsed into one:

* **Source verification** — this content was transcribed from an
  identified, publicly accessible source (e.g. "NU Online"). Always shown,
  truthfully, for every amaliyah.
* **Religious-authority approval** — a kyai/sesepuh verified the accuracy
  of a specific content version. Optional for standard public amaliyah,
  mandatory for higher-risk content; shown only when it genuinely happened.
* **Institutional endorsement** — the source publisher (e.g. NU/PBNU,
  Quran NU Online) endorses SanguSantri as an application. This does not
  currently exist for any source and must never be implied by showing a
  source's name next to the word "Approved."

This distinction must be visible wherever source or approval information is
shown to users (Sumber & Pentashihan, FR-009).

## User-facing source and approval display (compact)

Since Milestone 5 (source display) and Milestone 6 (risk-based approval),
the app's normal UI shows only compact, truthful status — never the full
pentashihan workflow, checksum, raw approval document, or internal
reviewer identity (`docs/product/PRD.md` §6.5):

```text
Sumber
NU Online
```

* Source attribution is always shown, for every amaliyah, sourced from
  `sourceName` — never invented.
* It is never rendered as `Approved by NU Online` or any other phrasing
  that implies the source publisher approved or endorsed SanguSantri.

```text
Approved by
<real reviewer name or institution>
```

* Shown only when the content version's approval metadata is genuinely
  valid (`status = APPROVED`, real, non-blank approver name) — never
  invented, guessed, or presented before real approval exists. This is
  optional for standard public amaliyah and does not block publication.
* While no religious-authority approval has been recorded, release builds
  show nothing approval-related (only the source line above). Development
  builds may show a neutral internal marker such as "Baseline rilis
  internal" — never `DRAFT`, `PENDING`, or other alarming/unprofessional
  engineering-status wording.
* An optional future detail action may show approval evidence (a signed
  letter, approval sheet, or other verifiable record); document upload, PDF
  viewing, and a CMS remain out of scope.
* A real approver identity and a real approval-evidence reference are
  required only before the application may display an `Approved by` status
  at all — they are not required for standard public amaliyah to be
  published and visible in release builds.

## Approval document privacy

The raw signed approval document is stored privately. Internally, the
content team may reference a redacted approval document, approver
identity, role, date, approval scope, and document reference number.
Private signatures, phone numbers, addresses, and identity numbers are
redacted when unnecessary (PRD §6.6). This detail is not required in the
normal app UI (see above).
