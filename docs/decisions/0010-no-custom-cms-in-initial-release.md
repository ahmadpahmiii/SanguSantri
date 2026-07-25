# 0010: No custom CMS in the initial release

## Status

Accepted

## Context

PRD §5.2 excludes a custom web CMS from `0.0.1`. Content authoring today is
versioned JSON files under `app/src/main/assets/content/`
(`docs/content-schema.md`); the planned backend adds a Go admin CLI
(`content validate|import|review|approve|publish|revoke|list|export`) and
Supabase Studio for draft-data editing, not a bespoke web application.

## Decision

Do not build a custom web content-management UI for `0.0.1` or any
currently-roadmapped release. Content authoring uses structured JSON files
(bundled) today, and the Go admin CLI + Supabase Studio once the backend
exists. Supabase Studio may edit draft data but must never be the mechanism
that publishes content — publication goes through the admin CLI's
validation gates (`docs/engineering/ARCHITECTURE.md` §Backend).

## Consequences

* Content team members need CLI or structured-file comfort, not a GUI, for
  the foreseeable roadmap. This is acceptable given the current volume (two
  amaliyah, one content team).
* If content-authoring volume or non-technical contributor count grows
  enough to make this workflow a real bottleneck, that is a concrete
  trigger to revisit this decision — not a hypothetical one to design
  around now.
