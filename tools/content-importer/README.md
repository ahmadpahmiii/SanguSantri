# content-importer (developer-only)

Converts a locally saved snapshot of one allowlisted, publicly available
source page into a structured JSON **draft**, compatible with the Android
app's seed content schema (`docs/content-schema.md`). Part of Milestone 3.5's
content flow — see `docs/operations/CONTENT_GOVERNANCE.md` for how this fits
into the full editorial workflow.

```text
NU Online page
→ local HTML snapshot (gitignored)
→ source-specific parser
→ structured draft JSON (status DRAFT, gitignored)
→ manual content review        ← you are here after `parse`
→ approved local JSON later
→ Android assets (app/src/main/assets/content/)
→ existing seed importer → Room → Full Reader
```

## What this is not

* Not a generic HTML-to-content scraper — it knows the layout of exactly one
  page (`content_importer/parser_nu_tahlil.py`). A different source gets its
  own parser module, not a flag on this one.
* Not something the Android app runs. It never executes at application
  runtime and is not part of the Gradle build.
* Not a publication mechanism. Its output is always `status: DRAFT` /
  `approval.status: PENDING` and is never written into
  `app/src/main/assets/content/` automatically — a human does that only
  after manual review and kyai/sesepuh approval
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

`fetch` enforces a request timeout (15s) and a response-size cap (5 MiB), and
only ever downloads the URLs listed in `content_importer/config.py` —
passing an arbitrary URL is not supported. `parse` can be re-run against an
existing snapshot at any time without re-downloading
(`--snapshot path/to/file.html`).

## Output

* `snapshots/` — raw HTML snapshots + a `.meta.json` sidecar per snapshot
  (source URL, retrieval timestamp, HTTP status, byte length, SHA-256).
  Gitignored; never commit a full copyrighted HTML snapshot.
* `output/` — generated draft package (`*.draft.json`), a provenance sidecar
  (`*.provenance.json`: source URL, retrieval date, snapshot checksum,
  package checksum), and an ambiguity report (`*.report.json`: skipped
  preamble paragraphs, ambiguous sections that need a human's judgement,
  paragraphs that look like they might need `QURAN_AYAH` metadata). All
  gitignored.

## What the parser will and will not do

It extracts headings, Arabic text (with harakat preserved verbatim), the
paired Indonesian translation, and a repetition count when the count is
written directly in a heading (e.g. `"(3 kali)"`, `"100 kali"`, `"2x"`). It
never invents missing Arabic text or a missing translation. When a paragraph
cannot be classified with confidence — an empty Arabic paragraph, an
Arabic block with no following translation, a repetition marker embedded
inside the Arabic text itself rather than in the heading — it is left out of
the draft's step list (or kept with a `null` field) and reported instead of
guessed. Every `parse` run prints an ambiguous-section summary and writes
the full list to `*.report.json`; read it before treating a draft as
review-ready.

The parser never assigns `QURAN_AYAH` (which requires a surah/ayah number the
page does not state in a structured way) — headings that look like they name
a specific Quran surah/ayah are instead listed under
`possibleQuranAyahCandidates` in the report, for a human reviewer to
classify and fill in the surah/ayah fields by hand.

## Promoting a reviewed draft

1. Manually review `output/tahlil-general-v1.draft.json` against the source
   article and resolve everything in `output/tahlil-general-v1.report.json`.
2. Route the reviewed content through the editorial workflow in
   `docs/operations/CONTENT_GOVERNANCE.md` (internal review → kyai/sesepuh
   review → signed approval).
3. Only then update `approval`/`version` status and copy the file into
   `app/src/main/assets/content/`, updating `manifest.json`'s
   `checksumSha256` to the SHA-256 of the final file's bytes (the checksum
   this tool records in `*.provenance.json` is for the draft, and will
   change if the file is edited during review).
