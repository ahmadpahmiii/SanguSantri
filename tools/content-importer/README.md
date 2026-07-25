# content-importer (developer-only)

Converts a locally saved snapshot of an allowlisted, publicly available
source page into a structured JSON **draft**, compatible with the Android
app's seed content schema (`docs/content-schema.md`). See
`docs/operations/CONTENT_GOVERNANCE.md` for how this fits into the full
editorial workflow.

```text
Source page (one of content_importer/config.py's SOURCES)
→ local HTML snapshot (gitignored)
→ source-specific parser
→ structured draft JSON (status DRAFT, gitignored)
→ manual content review        ← you are here after `parse`
→ approved local JSON later
→ Android debug assets (app/src/debug/assets/content/) for local testing,
  Android production assets (app/src/main/assets/content/) only after
  kyai/sesepuh approval — see docs/content-schema.md's debug/release split
→ existing seed importer → Room → Full Reader / Guided Reader
```

## What this is not

* Not a generic HTML-to-content scraper — each source gets its own parser
  module keyed to that one page's actual layout
  (`content_importer/parser_nu_tahlil.py` for Tahlil,
  `content_importer/parser_istighosah_nu.py` for Istighosah). A new source
  is a new parser module plus a new `SOURCES` entry in `config.py`, never a
  flag on an existing parser.
* Not something the Android app runs. It never executes at application
  runtime and is not part of the Gradle build.
* Not a publication mechanism. Its output is always `status: DRAFT` /
  `approval.status: PENDING` and is never written into
  `app/src/main/assets/content/` (or even `app/src/debug/assets/content/`)
  automatically — a human does that only after manual review, and, for
  `app/src/main/assets/content/` specifically, kyai/sesepuh approval
  (`docs/operations/CONTENT_GOVERNANCE.md`).

## Requirements

Python 3.9+, standard library only — no `pip install` needed.

## Usage

Run from this directory (`tools/content-importer/`):

```bash
python3 -m content_importer fetch      # download the allowlisted URL, save a local snapshot
python3 -m content_importer parse      # parse the latest snapshot into a draft JSON package
python3 -m content_importer validate   # structurally validate the generated draft
```

Every subcommand accepts `--source <id>` (default `tahlil-nu-online`; also
`istighosah-nu-online`) — see `content_importer/config.py`'s `SOURCES` for
the allowlist. `fetch` enforces a request timeout (15s) and a response-size
cap (5 MiB), and only ever downloads the URLs listed there — passing an
arbitrary URL is not supported. `parse` can be re-run against an existing
snapshot at any time without re-downloading (`--snapshot path/to/file.html`).

## Output

* `snapshots/` — raw HTML snapshots + a `.meta.json` sidecar per snapshot
  (source URL, retrieval timestamp, HTTP status, byte length, SHA-256).
  Gitignored; never commit a full copyrighted HTML snapshot.
* `output/` — generated draft package (`<content_slug>.draft.json`), a
  provenance sidecar (`.provenance.json`: source URL, retrieval date,
  snapshot checksum, package checksum), and an ambiguity report
  (`.report.json`: skipped preamble paragraphs, ambiguous sections that need
  a human's judgement, paragraphs that look like they might need
  `QURAN_AYAH` metadata). All gitignored.

## What the parsers will and will not do

Both parsers extract headings, Arabic text (with harakat preserved
verbatim), and the paired Indonesian translation, in document order. Neither
ever invents missing Arabic text or a missing translation, and the Latin
transliteration present on the Istighosah source is read only to keep span
ordering correct — its text is never stored (SanguSantri currently uses
Arabic and Indonesian translation only). When a block cannot be classified
or a repetition count cannot be confirmed with confidence, it is reported as
ambiguous rather than guessed — e.g. the Tahlil parser leaves an unpaired
Arabic paragraph with `translationId: null`; the Istighosah parser extracts
a repetition count from an Arabic-embedded `×N` marker or the Indonesian
`(Nx)` suffix, and flags it when only one of the two confirms the count, or
when they disagree. Every `parse` run prints an ambiguous-section summary
and writes the full list to `*.report.json`; read it before treating a
draft as review-ready.

Neither parser ever assigns `QURAN_AYAH` (which requires a surah/ayah number
neither source states in a structured way) — headings or translations that
look like they name a specific Quran surah/ayah are instead listed under
`possibleQuranAyahCandidates` in the report, for a human reviewer to
classify and fill in the surah/ayah fields by hand.

## Promoting a reviewed draft

1. Manually review `output/<content_slug>.draft.json` against the source
   and resolve everything in `output/<content_slug>.report.json`.
2. Route the reviewed content through the editorial workflow in
   `docs/operations/CONTENT_GOVERNANCE.md` (internal review → kyai/sesepuh
   review → signed approval).
3. Only then update `approval`/`version` status and copy the file into
   `app/src/main/assets/content/` (production), updating that source set's
   `manifest.json` `checksumSha256` to the SHA-256 of the final file's bytes
   (the checksum this tool records in `*.provenance.json` is for the draft,
   and will change if the file is edited during review). Until that
   approval happens, a draft may still be copied into
   `app/src/debug/assets/content/` for local development testing only —
   debug builds may display local `DRAFT` content, release builds never do
   (CLAUDE.md).
