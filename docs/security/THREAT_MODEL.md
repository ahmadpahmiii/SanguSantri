# Threat Model — Deferred Controls

Applies before adding any security control not already required by
`docs/security/SECURITY_BASELINE.md`. Purpose: stop a well-intentioned
future session (or a well-intentioned audit) from adding security theatre —
controls that cost real engineering time and user friction against a threat
that does not exist at this product's current stage.

None of the following are warranted yet, and none should be added without a
concrete triggering threat appearing first:

## Certificate pinning

**Deferred.** No MITM threat model specific to a public content reader
justifies this. Reconsider only if a concrete, targeted-interception threat
emerges — not preemptively once networking code exists.

## Root/tamper detection, Play Integrity API enforcement

**Deferred.** Reconsider only once pesantren membership codes (`0.2.0`) or
competitive Nahwu quiz rankings exist (a later, currently unscheduled
release — the individual/offline Nahwu Quiz itself ships at `0.0.5`, ADR
[0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md),
with no ranking and no Play Integrity), where fraud has real cost. Adding this
now defends nothing — there is no reader-content threat that root detection
mitigates. SanguSantri is currently non-commercial, so there is no payments
milestone to trigger this either.

## Enterprise secret management (Vault/HSM-backed KMS)

**Deferred.** There is no backend and none is planned (ADR 0014); the only
secret in this architecture is a Firebase Hosting deploy credential for
CI, which environment-variable/CI-secret storage is sufficient for at this
scale. Revisit only if a genuinely higher-stakes credential is introduced.

## Screenshot/clipboard blocking

**Deferred.** Only relevant once private pesantren content (`0.3.0`)
exists. Public devotional text has no confidentiality requirement to
protect — blocking screenshots on a public Tahlil reader is a pure UX cost
with no corresponding benefit.

## Brute-force protection beyond basic API rate limiting

**Deferred until `0.1.0`/`0.2.0`** (login, invitation codes). There is no
feedback endpoint or any other network-facing endpoint in `0.0.1`
(`docs/product/PRD.md` FR-012) — the account-free, backend-free surface
has no brute-force target yet. Revisit once the first real network-facing
surface (login, invitation codes) actually exists.

## Why this document exists

Security findings tend to accumulate as a flat list with no expiry and no
"why not yet" — every subsequent reader treats every item as equally live,
and the project either implements things it doesn't need or ignores the
list entirely because it's noisy. Keeping deferred-with-reasoning items
here, separate from `SECURITY_BASELINE.md`'s active-by-phase list, lets a
future session tell the difference between "not done yet" and "correctly
not done."
