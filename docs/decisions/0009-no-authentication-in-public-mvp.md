# 0009: No authentication in the public MVP

## Status

Accepted

## Context

PRD §3.4 and §5.2 explicitly exclude user login, phone OTP, and Google login
from `0.0.1`. Authentication is a significant engineering surface (secure
token storage, rotation, logout, server-side authorisation) that the
product does not need until a feature genuinely requires identity — private
pesantren access (`0.2.0`+) is the first such feature on the roadmap.

## Decision

Release `0.0.1` (and `0.0.2`–`0.0.4` per the current roadmap) ships with no
authentication of any kind. No account model, no login screen, no token
storage, no server-side session concept exists in this phase. The
`verified-email`/Credential Manager skill and any auth-related dependency
are not installed or added until `0.1.0` is actually being implemented.

## Consequences

* Feedback (FR-012) is anonymous by design — an installation identifier,
  not a user identity.
* No security work from `docs/security/SECURITY_BASELINE.md`'s "Required
  before authentication" phase should be started early; doing so is
  premature complexity, not risk reduction (Current Engineering Priority
  #5).
* When `0.1.0` starts, this ADR should be superseded by a new one recording
  the actual authentication mechanism chosen, rather than amended.
