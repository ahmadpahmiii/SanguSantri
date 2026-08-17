# SanguSantri Product Requirements Document

**Document version:** 1.7 — standalone Al-Qur'an Kemenag `0.0.6` is an
approved future milestone with its own bounded PRD, design system, API
contract, and ADR. This supersedes version 1.6's statement that no
standalone Quran feature or Kemenag integration was planned; it does not
authorise implementation before that milestone is explicitly requested.
Document version 1.6 — Firebase Hosting static content delivery:
approved product/tech-lead decision (ADR
[0014](../decisions/0014-firebase-hosting-static-content-delivery.md)) to
drop the Go + Supabase backend (ADR 0011, never implemented) entirely and
publish content as static files on Firebase Hosting instead. This changes
where content is served from, not the Android sync contract or product
requirements below — FR-010/FR-011 as rewritten in version 1.5 stand
unchanged; only "the backend" in their text now means static Firebase
Hosting files, not a dynamic Go service. Document version 1.5 — Content
Delivery Foundation and Remote Synchronisation: approved product/tech-lead
decision to build the Android remote content-synchronisation foundation
ahead of the (then-planned) Go backend's own implementation. Supersedes
version 1.4's FR-010 ("not part of `0.0.1`, no synchronisation code
exists") and FR-011 ("local on-device fallback to a retained previous
version") — see the rewritten FR-010/FR-011 below. Bundled content remains
the mandatory, always-shipped baseline; remote sync is an optional,
additive refresh that must never degrade the offline experience defined in
§3.2/ADR 0007. Everything else in version 1.4 (Beranda rename, Jelajahi
Amaliyah, reader repetition shortcut/TOC, risk-based publication model)
stays in effect unchanged. Full gap analysis for the design pass:
`docs/reviews/design-product-alignment.md`. A 2026-07-28 engineering
simplification pass (ADR 0012 amendment) removed manifest ETag/`304`
handling and the former six-case sync outcome model from the
implementation without changing FR-010/FR-011's product-level requirements
below — see that amendment for what changed and why.
**Product:** SanguSantri
**Initial release:** Android `0.0.1`
**Package name:** `com.sangusantri.app`
**Product owner:** Ahmad Fahmi Aisar
**Document status:** Ready for engineering
**Initial platform:** Native Android
**Backend:** None. Content is published as static files on Firebase
Hosting (ADR
[0014](../decisions/0014-firebase-hosting-static-content-delivery.md),
superseding the never-implemented Go + PostgreSQL backend, ADR
[0011](../decisions/0011-go-and-supabase-managed-postgresql-backend.md)) —
see `docs/product/ROADMAP.md`. The Android client against that static
contract (manifest/package fetch, WorkManager sync) is implemented and
ships in `0.0.1` regardless of whether static hosting has been deployed:
the app must remain fully functional offline, with hosting unreachable, or
before it has ever been deployed (§3.2, FR-010).
**Date:** 8 August 2026

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
* Official Kemenag-sourced Al-Qur'an reading.
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

The application's primary home destination is named **Beranda** (design
product-alignment pass — renamed from "Serambi"). "Serambi" may continue to
be used as an internal or product-language section label, but it is not a
separate user-facing destination from Beranda. The app opens directly to
Beranda on launch; no login is required (§3.4, §7).

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

Content publication uses a risk-based model (product-owner decision,
Milestone 6; supersedes this document's earlier universal-approval rule).
Full editorial workflow for both categories:
`docs/operations/CONTENT_GOVERNANCE.md`.

**Standard public amaliyah** — commonly practised, publicly recited
content sourced from an identified, publicly accessible, trusted editorial
source — may be published when:

* It comes from an identified, publicly accessible, trusted editorial
  source, with the source URL and publisher recorded.
* Extraction results have been manually inspected for structural problems.
* No content was invented by AI; no different versions were silently
  merged.
* Arabic text and translations remain exactly as sourced.
* The product owner explicitly accepts the package as the release
  baseline.

For this category, kyai/sesepuh sign-off is optional, not mandatory —
internal editorial acceptance by the product owner is sufficient for
publication. Tahlil (Umum) and Istighosah (Umum) as currently packaged
qualify as standard public amaliyah (§6.7).

**Higher-risk content** still requires kyai, ustaz, sesepuh, or other
qualified religious review before publication: private or
pesantren-specific content, content from an unclear or disputed origin,
content manually modified beyond formatting, content compiled by merging
multiple versions, internally translated content, doctrinally sensitive
content, or content associated with a specific ijazah, sanad, tarekat, or
pesantren authority.

Regardless of category, publication status, source verification, internal
editorial acceptance, religious-authority approval, and institutional
endorsement remain five distinct concepts, never collapsed into one
another (§6.5).

## 3.2 Offline by default

Public amaliyah must remain usable without login, internet connection, an
available backend, or previously completed synchronisation. The application
must ship with approved bundled content, and remote synchronisation (FR-010)
is strictly additive: the UI always renders from Room immediately, never
waiting on a network response, and a remote failure — offline, DNS failure,
timeout, HTTP error, malformed manifest, or a package that fails checksum or
schema validation — must never remove, replace, downgrade, or hide valid
content already stored in Room. See ADR
[0007](../decisions/0007-offline-first-public-content.md),
[0012](../decisions/0012-bundled-bootstrap-and-remote-sync.md), and
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
4. A home destination named **Beranda** (renamed from Serambi — §2.3), built
   as a scalable, section-based dashboard (§7) rather than a fixed card list.
5. A **Jelajahi Amaliyah** exploration destination (§7, FR-020).
6. Public Tahlil content.
7. Public Istighosah content.
8. Full Arabic text with complete harakat.
9. Indonesian translation displayed per ayah or logical reading segment.
10. No Latin transliteration.
11. Full reading mode.
12. Guided reading mode.
13. Automatic and manual progression options in guided mode.
14. Integrated counters for repeated readings.
15. Haptic feedback when the counter is pressed.
16. Persisted reading progress.
17. Persisted counter progress.
18. A saved reader-mode preference, with an in-reader action to switch
    between Bacaan Lengkap and Panduan without losing progress (§8.4a).
19. A Full Reader repetition shortcut into Guided Reader at the same step
    (§8.4b, FR-018).
20. ~~A reader Table of Contents bottom sheet (§8.4c, FR-017).~~ **Removed**
    by ADR 0015: it derived sections from `HEADING` steps, and the flat
    step schema has no step-type/title field left to derive them from.
21. Reader appearance settings, presented as a modal bottom sheet.
22. Local favourites and recently-opened amaliyah, with real persistence
    (FR-021).
23. Light and dark themes.
24. Green Islamic visual identity, modern rather than ornamental
    (`docs/design/DESIGN_SYSTEM.md` — supersedes this document's earlier
    "traditional-modern pesantren design direction" wording).
25. Bundled offline content, mandatory and always shipped, as the
    release-candidate baseline (§6.7) — the app must be fully usable
    offline, fresh-installed, with no remote hosting ever deployed.
26. Optional background remote content synchronisation against static
    content published on Firebase Hosting (FR-010, ADR 0014): a
    24-hour-gated, opportunistic WorkManager refresh that atomically
    replaces a variant's active content version when a newer one is
    published, and never degrades, blocks, or replaces the offline
    experience when unavailable.
27. ~~A compact "Approved by" status for every amaliyah~~ **Removed** by
    ADR 0015 — see §6.5; compact source attribution (§6.5) remains.
28. Portrait and landscape support.
29. Phone and tablet support, with adaptive navigation (§7.1).
30. Edge-to-edge layout.
31. Automated Android testing.
32. CI validation.

Authoring and deploying the `content-hosting/` static files and the
Firebase project/CI pipeline that publishes them are content-operations
items, not Android release engineering, and remain **not** part of this
Android release itself — see `docs/product/ROADMAP.md` and
`docs/engineering/ARCHITECTURE.md` §Backend. The Android client against
that static contract, described above, is part of `0.0.1` and must degrade
safely to fully local, offline-first behaviour whenever the hosted content
is absent, unreachable, or has never been deployed.

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
* Standing up the `content-hosting/` static files and the Firebase project/
  CI pipeline that deploys them — a content-operations workstream, not yet
  deployed (`docs/product/ROADMAP.md`, ADR 0014, superseding ADR 0011's
  never-built Go backend). What *is* part of `0.0.1` is the Android remote
  content-synchronisation client against that static contract (FR-010) —
  see §5.1 item 26. The Android app remains fully usable offline
  regardless of whether the hosted content has been deployed yet.
* A full pentashihan (content-approval) workflow, raw approval documents,
  internal reviewer identity, or other detailed content-governance data
  inside the normal app UI. Users see only compact source attribution
  (§6.5) — there is no on-device `Approved by` display as of ADR 0015; the
  underlying editorial workflow remains an internal, non-user-facing
  operation (`docs/operations/CONTENT_GOVERNANCE.md`).

The data model may support future variants, but the `0.0.1` interface
exposes only one default general variant for each amaliyah.

Standalone digital tasbih, streaks, and a reading-history screen remain
excluded from `0.0.1` (see the list above) and ship at `0.0.2`/`0.0.3`
respectively (`docs/product/ROADMAP.md`) — Beranda's "continue reading",
"recently opened", and "favourites" sections (§7) are read/write UI over
existing or newly added local state, not the Aktivitas history screen
itself, and must not be conflated with it.

**Resolved** (product owner/tech lead decision, 2026-07-29, ADR
[0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md)):
the persistent bottom-navigation chrome described in §7.1 is introduced
incrementally, starting at `0.0.2` (Beranda + Tasbih, the first release
with two real root destinations) — not in `0.0.1` with inert placeholder
destinations. It is a **bottom navigation bar only** through `0.0.5`, on
every window-size class including expanded/tablet — no Navigation Rail is
built in this window. This replaces this section's previous "open
question" wording.

---

# 6. Initial Content

## 6.1 Tahlil

Displayed title: **Tahlil**. Variant: **Umum**.

Editorial source: NU Online's "Bacaan Tahlil Singkat, Lengkap dengan Doa
dan Terjemahannya." The article presents an ordered Tahlil sequence
including Arabic readings, repetition counts, prayers, and Indonesian
translations. Automatic runtime scraping into production remains
prohibited (§5.2) — the shipped package was produced by the developer-only
tool below and then explicitly accepted by the product owner as the
`0.0.1` release baseline (§3.1, §6.7), the standard-public-amaliyah path,
not the higher-risk path.

A developer-only tool (`tools/content-importer/`, see
`docs/operations/CONTENT_GOVERNANCE.md`) converts a locally saved snapshot of
this article into a structured JSON draft compatible with the seed content
schema. The tool never publishes content automatically and never runs at
application runtime — it only ever produces a candidate draft; promoting
that draft to the published release baseline is the product owner's
editorial-acceptance decision (§6.3), not something the tool or Claude
does unilaterally.

## 6.2 Istighosah

Displayed title: **Istighosah**. Variant: **Umum**.

Editorial source: the KH Romli Tamim Istighosah reading available through
Quran NU Online (`https://quran.nu.or.id/doa/istighotsah-mujahadah`,
reading 1 of 7). Same standard-public-amaliyah acceptance path as §6.1.

## 6.3 Content entry rule

Claude MUST NOT:

* Invent Arabic readings.
* Invent translations.
* Generate missing prayers from memory.
* Automatically scrape and publish website text.
* Correct religious content based solely on AI judgement.
* Merge different versions without written instruction.
* Add Latin transliteration.
* Claim kyai/sesepuh approval or institutional endorsement that does not
  exist.

Content must be entered into structured content files by the product or
content team, and Arabic text/translations must remain exactly as sourced.
Publication of a specific package as the release baseline is the product
owner's explicit editorial-acceptance decision — see §3.1 for which
category (standard vs. higher-risk) a given package falls into. Development
fixtures used purely to exercise a feature (bracketed placeholder text,
never a real source) must be clearly marked and must never enter the
release build.

## 6.4 Standalone Al-Qur'an Kemenag

A standalone Al-Qur'an feature is approved for Android `0.0.6`. It uses the
official LPMQ Kemenag API under the access granted specifically to
SanguSantri, persists validated source content in Room for offline-first
reading, and provides dark-only Surah/Juz/Bookmark/Terakhir Dibaca browsing,
page and ayat reading modes, optional Indonesian translation, cached tafsir,
font and spacing controls, and local reading activity. It has no audio, Latin
transliteration, copy/share action, account dependency, or download manager.

The complete normative scope is owned by
[`QURAN_PRD.md`](QURAN_PRD.md), its visual rules by
[`QURAN_DESIGN_SYSTEM.md`](../design/QURAN_DESIGN_SYSTEM.md), its observed
wire contract by
[`QURAN_API_CONTRACT_DRAFT.md`](../engineering/QURAN_API_CONTRACT_DRAFT.md),
and the accepted architecture/security trade-off by ADR
[0016](../decisions/0016-standalone-quran-kemenag-direct-api.md). Those
documents do not change how Quran verses embedded inside amaliyah content are
modelled.

## 6.4a Quran content embedded in an amaliyah

Any Quran ayah appearing inside an amaliyah must contain surah number, ayah
number or range, approved Arabic text, approved Indonesian translation,
source identifier, and an optional future audio identifier. Quran text,
translation, and audio licensing must be verified separately.

Quran Foundation API integration and Quran audio are not planned. A Quran
verse appearing inside an amaliyah's own reading
text (for example, Al-Fatihah inside Tahlil) is entered and versioned as an
ordinary reading step (§10) — ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md)
removed the separate `QURAN_AYAH` step type and its surah/ayah reference
fields; the verse's Arabic text and translation are the same as any other
step's, never fetched from a separate Quran API or service at runtime.

## 6.5 Source, publication, and approval (user-facing, compact)

Five concepts stay distinct at the process/documentation level — never
collapsed into one another, even though (per ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md)) only
the first two have an on-device data structure any more:

* **Publication status** — whether the app can display a content item at
  all. Represented on-device only by `Content.isActive` (`docs/
  engineering/CONTENT_MODEL.md`) — there is no separate `DRAFT`/`PUBLISHED`/
  `REVOKED` status enum; an item not yet accepted for publication simply
  is not in the deployed catalog (or lives under `app/src/debug/assets/
  content/`, never `main/`, until it is accepted).
* **Source verification** — the identified public source a content item
  was transcribed from (`sourceName`/`sourceUrl`, still on-device).
* **Internal editorial acceptance** — the product owner's decision to
  publish a standard public amaliyah item (§3.1). Recorded in
  `docs/operations/CONTENT_GOVERNANCE.md` and commit history, not on-device.
* **Religious-authority approval** — a kyai/sesepuh/qualified reviewer's
  sign-off, mandatory only for higher-risk content (§3.1). Recorded the
  same way — in governance documentation/commit history, not as an
  on-device object. ADR 0015 removed the on-device `Approval` entity and
  its status field entirely, since neither published item (Tahlil,
  Istighosah) has ever had one and the app never rendered an "Approved by"
  line for either.
* **Institutional endorsement** — a source publisher's (e.g. NU/PBNU)
  endorsement of SanguSantri itself, which does not exist and must never be
  implied.

The application's normal UI always shows truthful, compact source
attribution:

```text
Sumber
NU Online
```

It never shows `Approved by NU Online` or any other implied institutional
endorsement — the source publisher has not approved or endorsed
SanguSantri. The app does **not** show a separate `Approved by` line at
all as of ADR 0015 — there is no on-device field to source it from, and
showing one truthfully would require reintroducing that data structure the
moment a real kyai/sesepuh sign-off needs to be surfaced to users (a future,
explicitly-requested product decision, not assumed here).

Rules:

* Source attribution (publisher/source name) is always shown for every
  content item, sourced from structured content metadata, never invented.
* Release builds never show `DRAFT`, `PENDING`, or other internal
  engineering status wording anywhere.
* Religious-authority approval, editorial acceptance, and institutional
  endorsement remain distinct concepts in governance process and
  documentation even though only source attribution is currently rendered
  in-app — never imply one from another in future UI work either.

Full content-governance record-keeping (approver role, institution,
approval date, document reference number, internal reviewer name) lives in
`docs/operations/CONTENT_GOVERNANCE.md` and commit/PR history for
internal/content-operations use — it is not part of the on-device content
model or the normal app UI (ADR 0015).

## 6.6 Approval document privacy

The raw signed approval document, when one exists, is kept privately by the
content-operations team, never bundled with the app and never displayed in
the reader. Full editorial, approval, and revocation process:
`docs/operations/CONTENT_GOVERNANCE.md`.

## 6.7 Public content baseline

Release `0.0.1`'s bundled Tahlil (37 ordered reading steps) and Istighosah
(25 ordered reading steps) are the product owner's accepted, published
`0.0.1` content baseline (§3.1, standard public amaliyah path) — loaded
through the existing canonical content model in both debug and release
builds, fully offline. (Step counts were 59 and 27 respectively before ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md)'s
migration to the flat step schema dropped section-heading-only steps — no
reading content changed; see `docs/content-schema.md` §Content safety.)
They are not reparsed or rewritten during normal Android builds; the
developer-only `tools/content-importer/` remains available as a separate
tool for preparing future content updates, never invoked at runtime.
Neither package carries kyai/sesepuh religious-authority approval today
(that remains optional for this content category, and is no longer an
on-device field at all — see §6.5); neither may ever be presented as
carrying such approval or as endorsed by NU/PBNU/Quran NU Online unless
that genuinely exists in writing — see `docs/operations/
CONTENT_GOVERNANCE.md` for the acceptance process actually followed.

---

# 7. Information Architecture

## 7.1 Primary destinations

Release `0.0.1` uses the following destinations: Bootstrap, Beranda,
Jelajahi Amaliyah, Reader mode selection, Amaliyah reader (Bacaan Lengkap
or Panduan, switchable in-place — §8.4a), Reader settings (a bottom sheet
reached from the reader's overflow menu, not a separate destination), About
SanguSantri. (The Reader Table of Contents bottom sheet previously listed
here was removed by ADR 0015 — see §5.1 item 20.) Compact source
attribution is shown from within the reader itself, not as a separate
destination (§6.5) — there is no on-device `Approved by` display as of
ADR 0015.

**Navigation model through `0.0.6`**: the shell decision made for `0.0.5`
(product owner/tech lead, 2026-07-29, ADR
[0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md) —
supersedes this section's earlier five-destination bottom-bar/rail
description) is extended unchanged through `0.0.6` by ADR 0016: a **bottom
navigation bar only**, on every window-size
class including expanded/tablet — no Navigation Rail is built in this
window. Destinations are added incrementally as their own release ships,
never speculatively:

* **Beranda** — `0.0.1` (this release), initial destination throughout.
* **Tasbih** — `0.0.2` (`docs/product/ROADMAP.md`). Bottom nav from this
  release is Beranda | Tasbih.
* **Aktivitas** — `0.0.3`. Bottom nav from this release is Beranda |
  Aktivitas | Tasbih.
* Pengingat Amaliyah (`0.0.4`) and Nahwu Quiz (`0.0.5`) are **never**
  bottom-nav destinations — reachable only through entry points on
  Beranda/Aktivitas (quick actions, sections), per their own release
  specs.
* **Profil** — `0.1.0`+ (implies an authenticated identity, §3.4) and
  **Pesantren** — `0.2.0`+ are out of scope entirely through `0.0.6` — no
  nav item is built for either, not even disabled/inert. Whether either
  becomes a sixth/seventh bottom-nav destination, and whether a
  Navigation Rail is ever introduced for a release beyond `0.0.6`, is a
  future product decision not made by this document.

From `0.0.6`, **Al-Qur'an Kemenag** is reached from a real Beranda entry and
is not a bottom-navigation destination. The existing Beranda | Aktivitas |
Tasbih shell remains unchanged; its bar is hidden throughout the immersive
Quran flow and the user's prior app theme is restored on exit. Quran uses the
same navigation system and activity as the rest of the app.

Jelajahi Amaliyah, the reading-mode gate, both readers, and About remain
reachable from Beranda, not as bottom-nav destinations themselves — the
bottom nav surfaces top-level sections only.

## 7.2 Pesantren terminology

Preferred Indonesian labels:

* Home: **Beranda** (Serambi may persist as an internal/product-language
  label for this section, not a second destination)
* Practices: **Amaliyah**
* Exploration/catalogue: **Jelajahi Amaliyah**
* Guided reading: **Panduan**
* Full reading: **Bacaan Lengkap**
* Counter: **Tasbih**
* Reader appearance settings: **Tampilan Bacaan**
* Source attribution (compact status): **Sumber**
* Activity/history: **Aktivitas**
* Pesantren community (future): **Pesantren**
* Account (future): **Profil**
* Settings: **Setelan**
* Continue reading: **Lanjutkan Bacaan**

Arabic localisation must use natural Arabic labels and proper RTL layout
rather than transliterated Indonesian terminology.

---

# 8. User Flows

## 8.1 First application launch

1. User launches the application.
2. Application initialises the local database.
3. Approved bundled content is imported (or reconciled against Room, if
   Room already has newer synced content) on every launch, non-blocking.
4. Beranda appears immediately from local data — never waiting on step 3
   or step 5.
5. Remote content synchronisation is scheduled in the background, gated by
   the 24-hour staleness check (FR-010); it silently does nothing if the
   backend is unreachable, has never been deployed, or the device is
   offline.
6. Synchronisation must not block Beranda.
7. User sees Tahlil and Istighosah surfaced through Beranda's curated
   amaliyah section (§7).

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
   records; back navigation remains predictable (returns to Beranda, not to
   an intermediate switch state).

## 8.4b Full Reader repetition shortcut (FR-018)

1. When a canonical step has a repetition target greater than one, Full
   Reader shows an interactive action alongside it, e.g. "Dibaca 3 kali ·
   Buka Panduan →" or "Dibaca 100 kali · Buka Panduan →" — visually
   secondary to the Arabic text, never competing with it.
2. Tapping the action immediately opens Guided Reader at the same
   canonical step, with no confirmation dialog.
3. Full Reader's own reading position is preserved for when the user
   returns to it.
4. Guided Reader restores the existing counter for that step (or starts at
   zero if none exists yet) — it does not reset progress.
5. No religious content is duplicated between modes; this is a second
   entry point into the existing Full ⇄ Guided switch mechanism (§8.4a),
   not a new reading surface.

## 8.4c Reader Table of Contents (removed, ADR 0015)

This flow (a modal bottom sheet of logical reading sections derived from
`HEADING`-typed steps) existed under FR-017. ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md)
removed the step-type/title data it depended on, so the flow was removed
rather than adapted — there is no data left to derive sections from.

## 8.5 Viewing source

1. User opens the overflow menu inside the reader.
2. User selects **Sumber**.
3. Application always displays truthful, compact source attribution
   (publisher/source name, §6.5). There is no on-device `Approved by`
   display as of ADR
   [0015](../decisions/0015-simplified-dynamic-catalog-content-model.md) —
   see §6.5 for where approval tracking now lives instead.

No account is required. Content correction is an internal SanguSantri-team
operation (§6.7, `docs/operations/CONTENT_GOVERNANCE.md`) — users do not
submit corrections or participate in any religious-authority approval
workflow through the application.

## 8.6 Using Beranda

1. Beranda renders as independent, vertically scrollable sections, not a
   fixed two-card list.
2. Each section is shown only when a genuine local data source backs it;
   a section with nothing to show renders nothing, never placeholder or
   fabricated content (FR-019).
3. `0.0.1` can genuinely back: search, continue reading (existing reading
   position/guided session state), quick actions (static, no data
   dependency), recently opened, favourites, and curated
   amaliyah/explore-all-categories.
4. Nearest reminder, pesantren content, and learning/Nahwu content sections
   remain hidden until their owning roadmap version (`0.0.4`, `0.2.0`,
   `0.0.5` respectively) ships real data.
5. Selecting an amaliyah from any Beranda section opens the same reading-
   mode flow as §8.2.

## 8.7 Jelajahi Amaliyah

1. User navigates to Jelajahi Amaliyah from Beranda.
2. User can search by title, browse by category, or filter by All /
   Favourite / Offline.
3. Each list item shows compact metadata where available (category, step
   count, repeat information) and current favourite/offline status.
4. Selecting an amaliyah opens the same reading-mode flow as §8.2.
5. The catalogue is scoped to amaliyah/religious reading content only — it
   must not become a general content or news surface (FR-020).

---

# 9. Functional Requirements

## FR-001: Offline bootstrap

The application MUST bundle approved Tahlil and Istighosah content and
import it into Room on first launch, independent of network state or
backend availability.

Acceptance criteria:

* A fresh installation opened in airplane mode displays both amaliyah.
* The user can open and complete either amaliyah offline.
* No empty loading screen waits for the backend.
* Bundled bootstrap is idempotent and safe to run on every launch.
* Reopening the app does not duplicate content.
* Bundled bootstrap never downgrades a variant already at a newer version
  in Room (for example, one a prior remote sync installed) — see FR-010.

## FR-002: Beranda

Beranda MUST display SanguSantri identity, a curated-amaliyah section
surfacing Tahlil and Istighosah, a continue-reading section when progress
exists, a subtle content update status, access to Setelan, and access to
About. See FR-019 for Beranda's full scalable-section model.

Every section's content must use data from Room (or the relevant local
store), not hardcoded screen lists.

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

Every displayed content item MUST expose compact, truthful source
attribution (§6.5), sourced from its structured `sourceName` metadata,
never invented. The application MUST NOT display fake approver names, fake
approval dates, fabricated approval evidence, or implied institutional
endorsement that does not exist in writing. As of ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md), the
app has no on-device `Approved by` display at all — religious-authority
approval is tracked only at the content-governance-process level
(`docs/operations/CONTENT_GOVERNANCE.md`), not as an on-device field; a
content item's publication status (`Content.isActive`) and its source
metadata remain controlled independently — see
`docs/engineering/CONTENT_MODEL.md`.

Full content-governance record-keeping (approver role, institution,
approval date, document reference number, internal reviewer name) lives in
`docs/operations/CONTENT_GOVERNANCE.md` and commit/PR history for
content-operations use — it is not part of the on-device content model.

## FR-010: Content synchronisation

The application MUST support optional background remote content
synchronisation against static content published on Firebase Hosting (ADR
0014), additive to — and never a prerequisite for — the bundled offline
baseline (§3.2, ADR 0007). Bundled content is mandatory; remote refresh is
optional for usability.

Acceptance criteria:

* Room is always rendered; no screen waits for a network response before
  showing bundled/already-synced content.
* Sync is opportunistic and one-time, triggered at app startup or
  foreground entry, gated so it runs at most once per 24 hours based on the
  last *terminal* remote sync attempt (including a terminal failure) —
  never a permanently repeating periodic worker.
* The catalog fetch has no conditional-request header — the catalog is
  small and is checked at most once every 24 hours (the next bullet's
  scheduling gate), so a plain request is fetched and read every time sync
  actually runs.
* Every catalog item's display metadata (title/description/image/category/
  order/active state) is refreshed unconditionally on every sync, with no
  fetch of its content file required. Only an item whose catalog `version`
  is greater than Room's local version has its content file actually
  fetched and imported; a lower or equal remote version is never fetched
  (ADR 0015 — a plain integer comparison, no checksum).
* A fetched content file is schema-validated and identity-checked (its own
  `id`/`version` must match the catalog entry that named it) before any
  database write.
* Content replacement is atomic: the new steps are written, and the
  previous version's steps are removed, inside one database transaction —
  step-level reading/counter progress is preserved for steps that still
  exist and pruned only for steps that no longer exist (`docs/engineering/
  CONTENT_MODEL.md`). A failure at any point leaves the previously valid
  content exactly as it was.
* API failure — offline, DNS failure, timeout, HTTP error, or a malformed
  catalog — MUST NOT remove, replace, downgrade, or hide valid content
  already in Room, MUST NOT show a raw error to the user, and MUST NOT
  crash the application.
* One malformed or stale content item never affects another item in the
  same catalog (per-item failure isolation).

Full design and failure/retry semantics: `docs/engineering/OFFLINE_FIRST.md`,
ADR [0012](../decisions/0012-bundled-bootstrap-and-remote-sync.md).

## FR-011: On-device version retention

Android retains only **one version per content item** — there is no
previous-version retention, no previous-version browsing screen, and no
previous-version fallback on-device. When sync replaces a content item's
version, its old steps are replaced as part of the same atomic replacement
(FR-010); step-level reading/counter progress is preserved for steps whose
id still exists in the new version and pruned only for steps that no
longer exist (`docs/engineering/CONTENT_MODEL.md`) — a more generous rule
than a blanket wipe, since ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md)'s
flat model makes most corrections a small edit to a few steps, not a
wholesale rewrite. Full immutable revision history for audit, publication,
and rollback is retained separately, in the `content-hosting/` git history
(ADR 0014) rather than a database (ADR 0008 is unaffected by this — it
governs the content-publication/correction workflow, not what Android
keeps locally). A dedicated screen for browsing previous versions remains
out of scope for `0.0.1`, consistent with the fact that Android does not
retain them.

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

## FR-017: Reader Table of Contents (removed, ADR 0015)

Previously required a modal bottom-sheet Table of Contents deriving
sections from `HEADING`-typed steps' titles. ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md)
removed the step-type/title fields it depended on — this requirement is
removed, not carried forward in adapted form, since there is no data left
to derive sections from. See §8.4c.

## FR-018: Full Reader repetition shortcut

When a canonical step's repetition target exceeds one, Full Reader MUST
show an interactive, visually secondary action (§8.4b) that opens Guided
Reader at the same step immediately, with no confirmation dialog, while
preserving the Full Reader's own reading position and restoring the
Guided Reader's existing counter for that step. This reuses the FR-016
switch mechanism and MUST NOT duplicate content or progress records.

## FR-019: Beranda (scalable dashboard)

Beranda MUST render as independent, vertically scrollable sections (§8.6),
each of which is shown only when backed by genuine local data — a section
MUST NOT render placeholder or fabricated content when no real data
exists. The implementation MUST NOT assume the catalogue will only ever
contain Tahlil and Istighosah, and MUST NOT branch UI logic on hardcoded
amaliyah slugs.

## FR-020: Jelajahi Amaliyah

The application MUST provide a Jelajahi Amaliyah destination (§8.7)
supporting search, category browsing, and All/Favourite/Offline filtering
over the full amaliyah catalogue, showing available metadata (category,
step count, repeat information) and favourite/offline status per item. The
underlying category taxonomy MUST support additional categories later
without a screen-structure change (`docs/engineering/CONTENT_MODEL.md`).
This destination remains scoped to amaliyah/religious reading content and
MUST NOT become a general content or news surface.

## FR-021: Favourites and recently opened

The application MUST support local, offline-first favourites and a
recently-opened list, both with real persistence (not preview-only fake
state). Recently opened MUST be tracked independently from completion
history (`0.0.3` Aktivitas scope) — opening an amaliyah is not the same
event as completing it.

## FR-022: Standalone Al-Qur'an Kemenag (`0.0.6`)

When the `0.0.6` milestone is explicitly requested, the application MUST
implement all requirements and acceptance criteria in
`docs/product/QURAN_PRD.md`. The general architecture, privacy, testing, and
release documents remain binding. No partial "MVP" may be presented as the
completed `0.0.6` feature, and Quran audio remains a later product decision.

---

# 10. Reader Content Model (summary)

The canonical hierarchy is flat (ADR
[0015](../decisions/0015-simplified-dynamic-catalog-content-model.md)):
`Content → Ordered ContentSteps`. Published versions are immutable — any
correction creates a new version, represented as a plain incrementing
integer on the content item (ADR
[0008](../decisions/0008-immutable-content-versions.md), unaffected in
spirit by ADR 0015's simplified representation).

There is no step "type" any more — every step has exactly `arabicText`,
`translation`, and a `repeatTarget` (minimum `1`). The former `HEADING`/
`INSTRUCTION`/`ARABIC_TEXT`/`QURAN_AYAH`/`PRAYER`/`REPEATED_READING`/
`DIVIDER`/`CLOSING` step-type enum is removed.

Translation must map to a logical Arabic segment; long prayers may be
split into multiple steps, but meaning must not be rearranged for visual
convenience. A Quran verse embedded in an amaliyah's own text (e.g.
Al-Fatihah inside Tahlil) is an ordinary step like any other — see §6.4a.

Full field-level schema (Room tables, catalog/content-file JSON format):
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

Crash/ANR monitoring, structured logs, and sync outcome tracking:
`docs/operations/INCIDENT_RESPONSE.md`. Telemetry MUST NOT record
Arabic reading text, counter values, or personal devotional history.

Claude must not claim that a build or test passes unless the command was
actually executed successfully.

---

# 13. Blocking Production Inputs

Engineering may begin immediately, but production publication is blocked
until these assets exist:

1. ~~Final Tahlil structured content.~~ **Resolved** (Milestone 6): the
   current 37-step (originally 59, before ADR 0015's flat-schema migration
   dropped section-heading markers — see §6.7) NU Online-sourced package
   is the product owner's accepted standard-public-amaliyah release
   baseline (§3.1, §6.7).
2. ~~Final Istighosah structured content.~~ **Resolved** (Milestone 6): the
   current 25-step (originally 27 — see §6.7) Quran NU Online-sourced
   package is the product owner's accepted standard-public-amaliyah
   release baseline (§3.1, §6.7).
3. Verified source for a Quran verse embedded in an amaliyah reading step —
   still required for that separate content pipeline (§6.4a); neither current
   package embeds one. The standalone `0.0.6` source is resolved as the
   official LPMQ Kemenag API and does not automatically authorise copying its
   data into amaliyah packages.
4. Verified Indonesian Quran translation for embedded amaliyah verses — same
   scope as (3). The standalone Kemenag translation source is resolved.
5. Kyai or sesepuh approval — no longer blocks publication of standard
   public amaliyah (§3.1); remains required before publishing any
   higher-risk content (private/pesantren-specific, disputed origin,
   internally modified/merged/translated, doctrinally sensitive, or tied to
   a specific ijazah/sanad/tarekat/pesantren authority). There is no
   on-device `Approved by` status any more (ADR 0015, §6.5) — approval
   remains a governance-process record regardless of this item's status.
6. Redacted approval documents — same conditional scope as (5); not
   required for the current standard-public-amaliyah baseline.
7. Content reproduction permission or documented legal basis.
8. Final logo.
9. Final application icon.
10. Privacy policy.
11. Google Play developer configuration.
12. ~~Production Firebase Hosting deployment and its base URL.~~ **Resolved**:
    Hosting is live and `SANGU_CONTENT_API_BASE_URL` is configured; bundled
    content remains the mandatory offline baseline (FR-010, ADR 0014,
    `docs/operations/PRODUCTION_READINESS.md`).
13. Android signing key.
14. Production Kemenag username/token injected from local/CI release secrets;
    no real credential may be committed. Direct-APK residual extraction risk
    is explicitly accepted by ADR 0016, with required NDK/R8/signature-check
    hardening.
15. Final redistribution/licence confirmation and glyph-compatibility results
    for every selectable Quran font. LPMQ Isep Misbah and Amiri Quran are
    design candidates only until those gates pass; King Fahd's font asset is
    not yet supplied.
16. Final privacy-policy wording covering Kemenag requests, local Quran state,
    and the lack of analytics/account sync.

Claude must use development-safe substitutes where possible, but must never
disguise missing production inputs as completed work, and must never claim
kyai/sesepuh approval or institutional endorsement that was not actually
given.

The full engineering/release-readiness checklist (CI, R8, monitoring, store
listing, etc.) is tracked separately in
`docs/operations/PRODUCTION_READINESS.md` — that list is engineering-owned;
this one is product/legal/governance-owned.

---

# Related Documents

* Architecture rules, Android/backend stack: `docs/engineering/ARCHITECTURE.md`
* Standalone Quran `0.0.6` scope and acceptance criteria: `docs/product/QURAN_PRD.md`
* Approved standalone Kalender Hijriah `0.0.7` scope, source evaluation,
  local-bundle policy, and acceptance criteria:
  `docs/product/HIJRI_CALENDAR_PRD.md`
* Sholawat dan Artinya `0.0.8` scope and acceptance criteria (own progress
  doc, not this file's milestone log): `docs/product/SHOLAWAT_PRD.md`,
  `docs/product/SHOLAWAT_PROGRESS.md`
* Standalone Quran visual system and design frame contract: `docs/design/QURAN_DESIGN_SYSTEM.md`
* Observed Kemenag endpoint/data contract: `docs/engineering/QURAN_API_CONTRACT_DRAFT.md`
* Standalone Quran architecture/security decision:
  `docs/decisions/0016-standalone-quran-kemenag-direct-api.md`
* Compose and Kotlin coding standard, prohibited patterns: `docs/engineering/CODING_STANDARD.md`
* Content model field reference: `docs/engineering/CONTENT_MODEL.md`, `docs/content-schema.md`
* Offline-first and synchronisation design: `docs/engineering/OFFLINE_FIRST.md`
* Testing strategy: `docs/engineering/TESTING.md`
* CI/CD and release process: `docs/engineering/RELEASE_ENGINEERING.md`
* Visual identity and anti-patterns: `docs/design/DESIGN_SYSTEM.md`
* Accessibility and adaptive layout: `docs/design/ACCESSIBILITY.md`
* Design frame mapping and implementation order: `docs/design/DESIGN_HANDOFF.md`
* Kalender Hijriah local design-export baseline:
  `docs/design/design-export/hijri-calendar/README.md`
* Design-vs-implementation gap analysis: `docs/reviews/design-product-alignment.md`
* Security controls by release phase: `docs/security/SECURITY_BASELINE.md`
* Privacy commitments: `docs/security/PRIVACY.md`
* Deferred security controls and rationale: `docs/security/THREAT_MODEL.md`
* Editorial workflow, approval, revocation authority: `docs/operations/CONTENT_GOVERNANCE.md`
* Monitoring and incident response: `docs/operations/INCIDENT_RESPONSE.md`
* Definition of Done and release-readiness checklist: `docs/operations/PRODUCTION_READINESS.md`
* Release roadmap beyond `0.0.1`: `docs/product/ROADMAP.md`
* Decision records: `docs/decisions/`
* Engineering progress log: `docs/PROGRESS.md`
