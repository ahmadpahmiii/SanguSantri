# SanguSantri Product Requirements Document

**Document version:** 1.2 — Milestone 5 scope correction: removed public
feedback (`Koreksi Bacaan`), remote content synchronisation, and the Go
backend from `0.0.1`; simplified approval to a compact `Approved by` status;
added the in-reader mode-switch requirement (FR-016).
**Product:** SanguSantri
**Initial release:** Android `0.0.1`
**Package name:** `com.sangusantri.app`
**Product owner:** Ahmad Fahmi Aisar
**Document status:** Ready for engineering
**Initial platform:** Native Android
**Backend:** Not part of release `0.0.1`. A future Go + PostgreSQL backend
(remote content synchronisation, public content API, admin CLI) remains an
unscheduled future item, not a committed roadmap version — see
`docs/product/ROADMAP.md`. Release `0.0.1` is local-only and offline-first.
**Date:** 25 July 2026

---

# 1. Document Purpose

This document is the product source of truth for SanguSantri: vision, users,
functional requirements, user flows, business rules, release scope, and
acceptance criteria.

Engineering process, architecture, security controls, design tokens, and
content-operations detail live in the documents listed in `CLAUDE.md`'s
reading matrix, not here — see [Related Documents](#related-documents) below.
Keeping those out of this file prevents the same rule existing in two places
and drifting, which is a real failure this document has had before (see
`docs/reviews/audit-resolution.md`).

Claude must implement only the release currently requested. Future roadmap
items (`docs/product/ROADMAP.md`) must influence extensibility but must not
be implemented prematurely.

Normative language:

* **MUST:** mandatory.
* **SHOULD:** expected unless there is a documented technical reason.
* **MAY:** optional.
* **MUST NOT:** prohibited.

---

# 2. Product Overview

## 2.1 Product name

**SanguSantri**

"Sangu" means provisions or supplies. SanguSantri represents a digital
provision for santri in their religious practice, study, and pesantren
community life.

## 2.2 Long-term vision

SanguSantri will become a **pesantren super-app** containing:

* Public santri amaliyah.
* Pesantren-specific private amaliyah.
* Pesantren membership verification.
* Pesantren schedules and announcements.
* Daily devotional tools.
* Nahwu quizzes.
* Gamification.
* Inter-pesantren rankings.
* Community features.

SanguSantri is currently a **non-commercial application**. Advertising and
subscriptions are not part of any current or planned roadmap item (see
`docs/product/ROADMAP.md`); reintroducing them would require a new, explicit
product decision.

The product will be strongly aligned with Nahdlatul Ulama traditions while
remaining open to pesantren with other traditions.

Unless a formal relationship is established, SanguSantri MUST be presented as
an independent product and MUST NOT imply that it is an official PBNU or NU
application.

## 2.3 Initial product position

Release `0.0.1` is a public, account-free, offline-first amaliyah reader
containing:

* Tahlil.
* Istighosah.

The first release is not the super-app itself. It is the reliable foundation
upon which the super-app will be built.

## 2.4 Target users

Primary users:

* Active santri.
* Pesantren alumni.
* Members of the general public who practise pesantren-style amaliyah.

Future restricted users:

* Verified santri.
* Verified alumni.
* Pesantren administrators.
* Content reviewers.
* Kyai and sesepuh approving religious content.

---

# 3. Product Principles

## 3.1 Religious accuracy before content volume

No public religious content may be published without:

* A documented source.
* Manual verification.
* Approval from an appointed kyai or sesepuh.
* An approval record visible to users.

Full editorial and approval workflow: `docs/operations/CONTENT_GOVERNANCE.md`.

## 3.2 Offline by default

Public amaliyah must remain usable without login, internet connection, an
available backend, or previously completed synchronisation. The application
must ship with approved seed content. See ADR
[0007](../decisions/0007-offline-first-public-content.md) and
`docs/engineering/OFFLINE_FIRST.md`.

## 3.3 Progressive delivery

The product follows a small, frequent release strategy. Each release should
introduce one clear, user-visible vertical slice. A weekly release is a
target, not permission to ship an unstable build.

## 3.4 No forced account

Public users must be able to access the core amaliyah without registration.
Authentication is introduced only when a feature genuinely requires identity,
such as private pesantren access. See ADR
[0009](../decisions/0009-no-authentication-in-public-mvp.md).

## 3.5 One content source, multiple reading experiences

Full reading mode and guided reading mode must render the same canonical
content model. The implementation MUST NOT duplicate Tahlil or Istighosah
content for each reader mode.

## 3.6 Clean architecture without ceremony

The project must have clear UI, domain, and data boundaries. It MUST NOT
create unnecessary abstractions such as one use case per repository method,
generic base repositories or ViewModels, empty wrapper classes, interfaces
with only one implementation and no testing or boundary benefit, or UI models
identical to domain models. Full architecture rules:
`docs/engineering/ARCHITECTURE.md` and `docs/engineering/CODING_STANDARD.md`.

## 3.7 Long-term maintainability

The codebase must favour explicit behaviour, small classes, stable naming,
testable business logic, reusable design tokens, minimal hidden magic,
migration-safe persistence, and backward-compatible content schemas.

---

# 4. Release Strategy

## 4.1 Application versioning

Application versions use pre-1.0 semantic progression: `0.0.1`, `0.0.2`,
`0.0.3`, `0.0.4`, ... Each public build must have an incremented Android
`versionCode`.

## 4.2 Content versioning

Application versions and content versions are independent. Examples:
application version `0.0.1`; Tahlil general variant `tahlil-general@1`;
Istighosah general variant `istighosah-general@1`; content schema version
`1`. A correction to Tahlil content must not require an APK release unless
the content schema or reader capability changes.

## 4.3 Weekly release rule

Each weekly release should contain one main user-visible improvement,
relevant tests, documentation updates, no unrelated unfinished feature, and
no hidden breaking schema change. A release must be withheld when its
quality gate fails (`docs/engineering/RELEASE_ENGINEERING.md`).

## 4.4 Feature flags

Future unfinished features must remain inaccessible through local feature
flags or server configuration. Feature flags must have safe offline
defaults.

---

# 5. Release 0.0.1 Scope

## 5.1 Included

Release `0.0.1` includes:

1. Native Android application using Jetpack Compose.
2. Indonesian and Arabic application localisation.
3. Right-to-left layout support for Arabic.
4. A home screen named **Serambi**.
5. Public Tahlil content.
6. Public Istighosah content.
7. Full Arabic text with complete harakat.
8. Indonesian translation displayed per ayah or logical reading segment.
9. No Latin transliteration.
10. Full reading mode.
11. Guided reading mode.
12. Automatic and manual progression options in guided mode.
13. Integrated counters for repeated readings.
14. Haptic feedback when the counter is pressed.
15. Persisted reading progress.
16. Persisted counter progress.
17. A saved reader-mode preference, with an in-reader action to switch
    between Bacaan Lengkap and Panduan without losing progress (§8.4a).
18. Reader appearance settings.
19. Light and dark themes.
20. Green Islamic visual identity.
21. Traditional-modern pesantren design direction.
22. Offline seed content, fixed as the release-candidate baseline (§6.7).
23. Preservation of previous content versions (local fallback only; see
    FR-011).
24. A compact "Approved by" status for every amaliyah, sourced from
    structured content metadata (§6.5).
25. Portrait and landscape support.
26. Phone and tablet support.
27. Edge-to-edge layout.
28. Automated Android testing.
29. CI validation.

Remote content synchronisation, the Go public content API, the Go content
administration CLI, PostgreSQL, and Supabase Studio are **not** part of
release `0.0.1` — see §5.2 and `docs/product/ROADMAP.md`. The application is
entirely local and offline-first for this release.

## 5.2 Explicitly excluded

Release `0.0.1` does not include:

* User login, phone OTP, Google login.
* Pesantren verification, pesantren invitation codes, private pesantren
  content, pesantren schedules, announcements.
* Community posts, comments, chat.
* Inter-pesantren rankings, Nahwu quizzes.
* Standalone digital tasbih, streaks, reading history screen.
* Gregorian or Hijri reminders, shareable achievement cards.
* Downloadable Quran audio.
* Advertising, subscriptions, Google Play Billing.
* A custom web CMS.
* Multiple pesantren variants, multiple active public variants, a comparison
  of differences between variants.
* Any web scraping performed by the Android application at runtime. Content
  scraping, when used at all, is a developer-side, offline tool that produces
  a local draft for manual review (§6.1) — never something the shipped app
  does itself.
* A PDF reader, or PDF parsing/extraction, inside the Android application.
  Reader content is always Unicode text sourced from the canonical content
  model, never a rendered or extracted PDF page or image of Arabic text.
* Public content-correction feedback (**Koreksi Bacaan**), a feedback form,
  a local feedback outbox, or any feedback submission endpoint. Content
  correction is handled internally by the SanguSantri team — see §6.7 and
  `docs/operations/CONTENT_GOVERNANCE.md`. This is a scope correction from
  document version 1.0, which described a public feedback flow (former
  FR-012); no such flow has been built, and none is planned for `0.0.1`.
* Remote content synchronisation and the Go + PostgreSQL backend (public
  content API, admin CLI, Supabase Studio). Both remain an unscheduled
  future item, not a committed roadmap version — see
  `docs/product/ROADMAP.md`. This is a scope correction from document
  version 1.0's former FR-010; no synchronisation code exists, and
  `0.0.1` ships fully local and offline-first.
* A full pentashihan (content-approval) workflow, checksum display, raw
  approval documents, internal reviewer identity, or other detailed
  content-governance data inside the normal app UI. Users see only a
  compact `Approved by` status (§6.5); the underlying editorial and
  approval workflow remains an internal, non-user-facing operation
  (`docs/operations/CONTENT_GOVERNANCE.md`).

The data model may support future variants, but the `0.0.1` interface
exposes only one default general variant for each amaliyah.

---

# 6. Initial Content

## 6.1 Tahlil

Displayed title: **Tahlil**. Variant: **Umum**.

Initial editorial reference: NU Online's "Bacaan Tahlil Singkat, Lengkap
dengan Doa dan Terjemahannya." The article presents an ordered Tahlil
sequence including Arabic readings, repetition counts, prayers, and
Indonesian translations. It must be used as an editorial reference, not
automatically scraped into production.

A developer-only tool (`tools/content-importer/`, see
`docs/operations/CONTENT_GOVERNANCE.md`) converts a locally saved snapshot of
this article into a structured JSON draft compatible with the seed content
schema, for manual review only. The tool never publishes content
automatically and never runs at application runtime — it produces a `DRAFT`
that still requires manual transcription review and kyai/sesepuh approval
before it may become production content (§6.3).

## 6.2 Istighosah

Displayed title: **Istighosah**. Variant: **Umum**.

Proposed initial editorial reference: the KH Romli Tamim Istighosah
collection available through Quran NU Online. This source remains subject to
selection and approval by the assigned kyai or sesepuh.

## 6.3 Content entry rule

Claude MUST NOT:

* Invent Arabic readings.
* Invent translations.
* Generate missing prayers from memory.
* Automatically scrape and publish website text.
* Correct religious content based solely on AI judgement.
* Merge different versions without written instruction.
* Add Latin transliteration.

Content must be entered into structured content files by the product or
content team. Development fixtures may use clearly marked sample text, but
fixtures must never enter the release build, and the release build must fail
validation when approved production content is missing.

## 6.4 Quran content

Any Quran ayah appearing inside an amaliyah must contain surah number, ayah
number or range, approved Arabic text, approved Indonesian translation,
source identifier, and an optional future audio identifier. Quran text,
translation, and audio licensing must be verified separately.

There is no standalone Quran feature, no Quran Kemenag API integration, no
Quran Foundation API integration, and no Quran audio in the current or
planned roadmap. A `QURAN_AYAH` step (§10) exists only to represent a verse
that is already part of an amaliyah's own reading text (for example, Al-
Fatihah inside Tahlil); its text is entered and versioned as part of that
amaliyah's approved content package, the same as any other step, never
fetched from a separate Quran API or service at runtime.

## 6.5 Approval (user-facing, compact)

Content review and correction are internal SanguSantri-team operations
(§6.7). Users do not submit corrections and do not participate in the
approval workflow — approval is a deployment/content-operations gate, not a
user-facing feature. The application's normal UI exposes only a compact
public status, sourced from structured content metadata, never invented:

```text
Approved by
<approver name or institution>
```

Rules:

* When a content version's approval metadata is valid (status `APPROVED`
  with a real approver name), the app displays `Approved by` and the
  approver/institution name.
* While final approval metadata has not been supplied, the app displays a
  neutral internal status such as "Persetujuan akhir belum tersedia" only in
  development builds. Release builds never display a fake or placeholder
  approval status.
* An optional future detail action may show approval evidence (e.g. a
  signed letter or approval sheet reference). This is not required for
  `0.0.1` and does not include document upload, PDF viewing, or a CMS.
* Approval verifies the accuracy of the specified content version only. It
  must not be presented as institutional endorsement of the entire
  SanguSantri application unless such endorsement exists in writing.
* Final public deployment requires real approver metadata and a real
  approval-evidence reference (§13).

Full internal approval record fields (approver role, institution, approval
date, document reference number, internal reviewer name, checksum) remain
part of the structured content model for internal/content-operations use —
see `docs/engineering/CONTENT_MODEL.md` — but are not required to appear in
the normal app UI.

## 6.6 Approval document privacy

The raw signed approval document, when one exists, is kept privately by the
content-operations team, never bundled with the app and never displayed in
the reader. Full editorial, approval, and revocation process:
`docs/operations/CONTENT_GOVERNANCE.md`.

## 6.7 Release-candidate content baseline

Release `0.0.1`'s bundled Tahlil (59 ordered steps) and Istighosah (27
ordered steps) are fixed local release-candidate packages, loaded through
the existing canonical content model in both debug and release builds,
fully offline. They are not reparsed or rewritten during normal Android
builds; the developer-only `tools/content-importer/` remains available as a
separate tool for preparing future content updates, never invoked at
runtime. Neither package may be presented as finally approved until real
kyai/sesepuh approval metadata is supplied (§6.5, §13) — see
`docs/operations/CONTENT_GOVERNANCE.md` for the internal review process.

---

# 7. Information Architecture

## 7.1 Primary destinations

Release `0.0.1` uses the following destinations: Bootstrap, Serambi, Reader
mode selection, Amaliyah reader (Bacaan Lengkap or Panduan, switchable
in-place — §8.4a), Reader settings, About SanguSantri. A compact approval
status ("Approved by") is shown from within the reader itself, not as a
separate destination (§6.5).

A bottom navigation bar is not required for `0.0.1`. The number of
destinations does not justify permanent bottom navigation.

## 7.2 Pesantren terminology

Preferred Indonesian labels:

* Home: **Serambi**
* Practices: **Amaliyah**
* Guided reading: **Panduan**
* Full reading: **Bacaan Lengkap**
* Counter: **Tasbih**
* Content verification (compact status): **Sumber & Pentashihan**
* Settings: **Setelan**
* Continue reading: **Lanjutkan Bacaan**

Arabic localisation must use natural Arabic labels and proper RTL layout
rather than transliterated Indonesian terminology.

---

# 8. User Flows

## 8.1 First application launch

1. User launches the application.
2. Application initialises the local database.
3. Approved seed content is imported when the database is empty.
4. Serambi appears immediately from local data.
5. Network synchronisation begins in the background when connected.
6. Synchronisation must not block Serambi.
7. User sees Tahlil and Istighosah cards.

## 8.2 Opening an amaliyah

1. User selects Tahlil or Istighosah.
2. Application opens the default general variant.
3. If no mode preference exists, a mode selection sheet appears.
4. User chooses Bacaan Lengkap or Panduan.
5. The chosen mode may be saved as the default later.
6. Reader restores unfinished progress when available.

## 8.3 Full reading mode

1. Application displays all reading sections in one vertically scrollable
   screen.
2. Each section displays a section title when applicable, Arabic text,
   Indonesian translation, and repetition target when applicable.
3. Repeated sections display an embedded counter.
4. User manually scrolls.
5. User may mark the amaliyah complete after required counters are
   completed.
6. Scroll position and counters survive process death.

## 8.4 Guided reading mode

1. Application displays one logical step at a time.
2. Current position is visible, for example `5 of 22`.
3. Arabic text and translation are displayed together.
4. If a step has a repeat target, the counter appears.
5. Counter tap produces haptic feedback.
6. Progression behaviour is configurable: **Automatic** (move to the next
   step when the target is reached) or **Manual** (enable the Continue
   button when the target is reached).
7. Non-counter steps use a Continue button.
8. User may move to the previous step.
9. The session resumes at the last unfinished step.

## 8.4a Switching reading mode inside the reader

1. Full Reader shows an easy-to-find but not visually dominant action,
   "Beralih ke Panduan" (top-app-bar/overflow action, not a permanent bottom
   navigation bar).
2. Guided Reader shows the equivalent action, "Beralih ke Bacaan Lengkap".
3. Switching preserves the same amaliyah and content version, and does not
   show the initial mode-selection screen again.
4. Full → Guide: the Guided Reader opens at the Full Reader's currently
   visible step (or the nearest valid step), with existing Guided Reader
   counter progress for that content version intact.
5. Guide → Full: the Full Reader opens at the item index corresponding to
   the Guided Reader's current step, with a safe scroll offset.
6. The saved reader-mode preference (§8.2) updates to match the newly
   active mode.
7. Repeated switching does not duplicate navigation entries or progress
   records; back navigation remains predictable (returns to Serambi, not to
   an intermediate switch state).

## 8.5 Viewing source and approval

1. User opens the overflow menu inside the reader.
2. User selects **Sumber & Pentashihan**.
3. Application displays the compact `Approved by` status (§6.5) — approver
   or institution name when valid approval metadata exists, or a
   development-only pending status otherwise.
4. An optional future detail action may show approval evidence; not built
   in `0.0.1`.

No account is required. Content correction is an internal SanguSantri-team
operation (§6.7, `docs/operations/CONTENT_GOVERNANCE.md`) — users do not
submit corrections or participate in the approval workflow through the
application.

---

# 9. Functional Requirements

## FR-001: Offline bootstrap

The application MUST bundle approved Tahlil and Istighosah content.

Acceptance criteria:

* A fresh installation opened in airplane mode displays both amaliyah.
* The user can open and complete either amaliyah offline.
* No empty loading screen waits for the backend.
* Seed import is idempotent.
* Reopening the app does not duplicate content.

## FR-002: Serambi

Serambi MUST display SanguSantri identity, Tahlil card, Istighosah card, a
continue-reading section when progress exists, a subtle content update
status, access to Setelan, and access to About.

Cards must use data from Room, not hardcoded screen lists.

## FR-003: Default variants

Each amaliyah MUST have one default general variant in `0.0.1`. Selecting an
amaliyah opens the default variant immediately. The data layer must support
future variants without exposing a variant selector yet.

## FR-004: Full reading mode

Full reading mode MUST render all content from one canonical version, use
stable item keys, support large Arabic text, display translations per ayah
or segment, display embedded counters, restore scroll and counter state,
work in portrait and landscape, and avoid keeping the screen permanently
awake.

The application MUST NOT set `FLAG_KEEP_SCREEN_ON`.

## FR-005: Guided mode

Guided mode MUST display one step at a time, display progress, support back
and forward navigation, support automatic advancement, support manual
advancement, persist selected progression behaviour, and preserve the
current step after process death.

## FR-006: Integrated counter

A step may define a positive repetition target. The counter MUST begin at
zero unless restored, never exceed the target in integrated mode, provide
haptic feedback, display current and target values, provide reset with
confirmation, persist immediately after meaningful changes, and mark the
step complete when the target is reached.

No sound is required.

## FR-007: Completion

An amaliyah is complete only when every required counter reaches its target
and the user presses the final completion confirmation. Opening the final
page alone must not mark completion.

A completed state may be stored locally, but streak and history interfaces
are excluded from `0.0.1`.

## FR-008: Reader settings

Users MUST be able to configure Arabic font size, translation font size,
Arabic line spacing, translation line spacing, light/dark/system theme,
reader background style, show or hide translation, guided progression
(automatic or manual), and Indonesian or Arabic application language.

Preferences must use DataStore and apply without restarting the application
when practical.

## FR-009: Content details (compact)

Every displayed amaliyah MUST expose a compact `Approved by` status (§6.5),
sourced from the content version's structured approval metadata, never
invented. The application MUST NOT display fake approver names, fake
approval dates, or fabricated approval evidence. A content item's
publication/readability status (whether the app can display it at all) and
its approval metadata (what `Approved by` shows) are controlled
independently — see `docs/engineering/CONTENT_MODEL.md`.

Full internal approval fields (approver role, institution, approval date,
document reference number, internal reviewer name, content checksum) remain
part of the structured content model for content-operations use, but are
not required in the normal app UI.

## FR-010: Content synchronisation (deferred beyond `0.0.1`)

Remote content synchronisation is **not** part of release `0.0.1` — no
synchronisation code, manifest comparison, or network content fetch exists
or is built for this release. Release `0.0.1` ships fully local, fixed
release-candidate content (§6.7). A future synchronisation requirement, if
scheduled, would need to run without blocking local reads, validate schema
version and checksum, import inside a database transaction, preserve
previous versions, and respect network constraints — design detail (for
that future work only): `docs/engineering/OFFLINE_FIRST.md`.

## FR-011: Previous versions (local fallback)

The data model preserves previous content versions locally. If the active
version is revoked (`AmaliyahVersionStatus.REVOKED`), the application falls
back to the newest non-revoked version and the revoked version is not
opened by default. This is a local, on-device fallback in `0.0.1` — there is
no backend manifest or remote revocation signal yet (see FR-010). A
dedicated screen for browsing previous versions is not required for
`0.0.1`.

## FR-012: Feedback (removed from scope)

Public content-correction feedback (**Koreksi Bacaan**) is **not** part of
release `0.0.1` and none is currently planned. Content correction is
handled internally by the SanguSantri team (§6.7,
`docs/operations/CONTENT_GOVERNANCE.md`) — the application does not collect,
store, or submit user feedback. This document version removes the feedback
requirement that a previous document version (1.0) described here; no
feedback outbox, feedback form, or feedback endpoint has been built.

## FR-013: Localisation

The release MUST support Indonesian, Arabic, RTL layout, localised dates,
localised numerals where appropriate, and mirrored navigation icons in RTL.

Arabic devotional content must remain correctly aligned and readable
regardless of the selected interface language.

## FR-014: Adaptive layout

The application MUST support compact phones, landscape phones, foldables
where possible, and tablets. Reader text should use a constrained readable
width on large displays rather than stretching from edge to edge.

## FR-015: Edge-to-edge

The application must use modern edge-to-edge rendering. System bar and
keyboard insets must be handled exactly once. Interactive content must not
be hidden beneath status bars, navigation bars, or the IME.

## FR-016: Reader mode switching

Both Full Reader and Guided Reader MUST expose an easy-to-find,
non-dominant action to switch to the other mode (§8.4a) — a top-app-bar or
overflow action, never a permanent bottom navigation bar. Switching MUST
preserve the same amaliyah and content version, MUST NOT show the initial
mode-selection screen again, MUST update the saved reader-mode preference
(§8.2), MUST preserve existing Guided Reader counter progress, and MUST NOT
duplicate navigation entries or Room progress records on repeated
switching. Back navigation after switching must remain predictable.

---

# 10. Reader Content Model (summary)

The canonical hierarchy is `Amaliyah → Variant → Immutable Version → Ordered
Steps → Optional Assets`. Published versions are immutable — any correction
creates a new version (ADR
[0008](../decisions/0008-immutable-content-versions.md)).

Supported step types: `HEADING`, `INSTRUCTION`, `ARABIC_TEXT`, `QURAN_AYAH`,
`PRAYER`, `REPEATED_READING`, `DIVIDER`, `CLOSING`.

For Quran content, translation must map to its corresponding ayah. For
non-Quran content, translation must map to a logical Arabic segment; long
prayers may be split into manageable segments, but meaning must not be
rearranged for visual convenience.

Full field-level schema (server tables, Room tables, JSON seed format):
`docs/engineering/CONTENT_MODEL.md` and `docs/content-schema.md`.

---

# 11. Non-Functional Requirements (summary)

Full normative detail for each category lives in its canonical document —
do not restate these rules elsewhere:

| Category | Canonical document |
|---|---|
| Reliability (offline operation, migration safety, failed-sync handling) | `docs/engineering/OFFLINE_FIRST.md` |
| Performance (rendering, lazy loading, main-thread rules) | `docs/engineering/ARCHITECTURE.md` |
| Accessibility (content descriptions, font scaling, RTL, touch targets) | `docs/design/ACCESSIBILITY.md` |
| Security (HTTPS, credential handling, checksum verification) | `docs/security/SECURITY_BASELINE.md` |
| Privacy (no devotional history upload, privacy policy) | `docs/security/PRIVACY.md` |
| Application resilience (empty/error/offline states) | `docs/engineering/OFFLINE_FIRST.md` |

These are still product acceptance criteria — Definition of Done
(`docs/operations/PRODUCTION_READINESS.md`) checks against them.

---

# 12. Testing, Release, and Observability

Claude must inspect and follow the official Android testing skill when
implementing tests. Required unit, Room, Compose UI, and end-to-end test
scenarios: `docs/engineering/TESTING.md`.

CI quality gates, release workflow, and versioning mechanics:
`docs/engineering/RELEASE_ENGINEERING.md`.

Crash/ANR monitoring, structured logs, and sync/feedback success-rate
tracking: `docs/operations/INCIDENT_RESPONSE.md`. Telemetry MUST NOT record
Arabic reading text, counter values, or personal devotional history.

Claude must not claim that a build or test passes unless the command was
actually executed successfully.

---

# 13. Blocking Production Inputs

Engineering may begin immediately, but production publication is blocked
until these assets exist:

1. Final Tahlil structured content.
2. Final Istighosah structured content.
3. Verified Quran text source.
4. Verified Indonesian Quran translation source.
5. Kyai or sesepuh approval.
6. Redacted approval documents.
7. Content reproduction permission or documented legal basis.
8. Final logo.
9. Final application icon.
10. Privacy policy.
11. Google Play developer configuration.
12. Production backend credentials.
13. Android signing key.

Claude must use development-safe substitutes where possible, but must never
disguise missing production inputs as completed work.

The full engineering/release-readiness checklist (CI, R8, monitoring, store
listing, etc.) is tracked separately in
`docs/operations/PRODUCTION_READINESS.md` — that list is engineering-owned;
this one is product/legal/governance-owned.

---

# Related Documents

* Architecture rules, Android/backend stack: `docs/engineering/ARCHITECTURE.md`
* Compose and Kotlin coding standard, prohibited patterns: `docs/engineering/CODING_STANDARD.md`
* Content model field reference: `docs/engineering/CONTENT_MODEL.md`, `docs/content-schema.md`
* Offline-first and synchronisation design: `docs/engineering/OFFLINE_FIRST.md`
* Testing strategy: `docs/engineering/TESTING.md`
* CI/CD and release process: `docs/engineering/RELEASE_ENGINEERING.md`
* Visual identity and anti-patterns: `docs/design/DESIGN_SYSTEM.md`
* Accessibility and adaptive layout: `docs/design/ACCESSIBILITY.md`
* Security controls by release phase: `docs/security/SECURITY_BASELINE.md`
* Privacy commitments: `docs/security/PRIVACY.md`
* Deferred security controls and rationale: `docs/security/THREAT_MODEL.md`
* Editorial workflow, approval, revocation authority: `docs/operations/CONTENT_GOVERNANCE.md`
* Monitoring and incident response: `docs/operations/INCIDENT_RESPONSE.md`
* Definition of Done and release-readiness checklist: `docs/operations/PRODUCTION_READINESS.md`
* Release roadmap beyond `0.0.1`: `docs/product/ROADMAP.md`
* Decision records: `docs/decisions/`
* Engineering progress log: `docs/PROGRESS.md`
