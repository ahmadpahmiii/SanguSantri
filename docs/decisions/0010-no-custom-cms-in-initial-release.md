# 0010: No custom CMS in the initial release

## Status

Accepted

## Context

PRD §5.2 excludes a custom web CMS from `0.0.1`. Content authoring today is
versioned JSON files under `app/src/main/assets/content/`
(`docs/content-schema.md`); the originally planned backend would have added
a Go admin CLI (`content validate|import|review|approve|publish|revoke|
list|export`) and Supabase Studio for draft-data editing. ADR
[0014](0014-firebase-hosting-static-content-delivery.md) dropped that
backend entirely — publication is now direct JSON file authoring under
`content-hosting/` plus CI validation and `firebase deploy --only hosting`,
not a bespoke web application either way.

## Decision

Do not build a custom web content-management UI for `0.0.1` or any
currently-roadmapped release. Content authoring uses structured JSON files
throughout — bundled under `app/src/main/assets/content/` and, once ADR
0014's static hosting migration is scheduled, remotely-served under
`content-hosting/`, both validated by CI rather than an admin CLI or a
GUI. Publication goes through CI's validation gates before
`firebase deploy --only hosting` runs (`docs/operations/
CONTENT_GOVERNANCE.md`), never through hand-editing a live file with no
validation.

## Consequences

* Content team members need structured-file comfort, not a GUI or a CLI,
  for the foreseeable roadmap. This is acceptable given the current volume
  (two amaliyah, one content team).
* If content-authoring volume or non-technical contributor count grows
  enough to make this workflow a real bottleneck, that is a concrete
  trigger to revisit this decision — not a hypothetical one to design
  around now.
