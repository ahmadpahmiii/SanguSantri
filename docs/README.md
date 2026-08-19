# SanguSantri Documentation

Start with `CLAUDE.md` at the repository root — it has the reading matrix
that points to exactly which document below a given task needs. This index
is a map of what exists, not a reading order.

## Product

* [`product/PRD.md`](product/PRD.md) — vision, users, functional
  requirements, user flows, release scope, acceptance criteria.
* [`product/ROADMAP.md`](product/ROADMAP.md) — releases beyond `0.0.1`,
  future pesantren rules.

## Engineering

* [`engineering/ARCHITECTURE.md`](engineering/ARCHITECTURE.md) — stack,
  package structure, layer rules, modularisation triggers, content delivery
  shape.
* [`engineering/CODING_STANDARD.md`](engineering/CODING_STANDARD.md) —
  Compose rules, prohibited patterns, working method, reference policy.
* [`engineering/CONTENT_MODEL.md`](engineering/CONTENT_MODEL.md) — content
  hierarchy, Room field reference.
* [`engineering/OFFLINE_FIRST.md`](engineering/OFFLINE_FIRST.md) —
  synchronisation design, reliability requirements.
* [`engineering/TESTING.md`](engineering/TESTING.md) — required test
  scenarios by layer.
* [`engineering/RELEASE_ENGINEERING.md`](engineering/RELEASE_ENGINEERING.md)
  — CI/CD design, quality commands, release build configuration.
* [`engineering/MCP_TOOLING.md`](engineering/MCP_TOOLING.md) — MCP servers as
  development/CI tooling only, and their boundaries with the Android app.
  (The Firebase Hosting content tree it was written for is gone — content now
  comes from the CMS API; see `../../cms/docs/engineering/API.md`.)

## Design

* [`design/DESIGN_SYSTEM.md`](design/DESIGN_SYSTEM.md) — visual direction,
  anti-patterns, token requirements.
* [`design/ACCESSIBILITY.md`](design/ACCESSIBILITY.md) — RTL, adaptive
  layout, large-font requirements.

## Security

* [`security/SECURITY_BASELINE.md`](security/SECURITY_BASELINE.md) —
  controls required by release phase.
* [`security/PRIVACY.md`](security/PRIVACY.md) — data collection
  commitments.
* [`security/THREAT_MODEL.md`](security/THREAT_MODEL.md) — controls
  deliberately deferred, and why.

## Operations

* [`operations/CONTENT_GOVERNANCE.md`](operations/CONTENT_GOVERNANCE.md) —
  editorial workflow, approval/revocation authority.
* [`operations/INCIDENT_RESPONSE.md`](operations/INCIDENT_RESPONSE.md) —
  monitoring, recovery, incident runbook.
* [`operations/PRODUCTION_READINESS.md`](operations/PRODUCTION_READINESS.md)
  — Definition of Done, staged rollout, backup policy.

## Reference

* [`content-schema.md`](content-schema.md) — bundled JSON seed format.
* [`decisions/`](decisions/) — architecture decision records (ADRs), one
  file per durable decision.
* [`reviews/`](reviews/) — point-in-time audits and their resolutions.
* [`PROGRESS.md`](PROGRESS.md) — append-only milestone log; the
  authoritative source for "what actually exists today."
