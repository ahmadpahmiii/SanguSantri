# SanguSantri Release Roadmap

Product-level roadmap only. Engineering milestone tracking (what has actually
shipped) lives in [`docs/PROGRESS.md`](../PROGRESS.md), not here. Do not
implement an item on this roadmap until it is explicitly requested — PRD §1
already states future roadmap items must influence extensibility without
being built prematurely.

SanguSantri is currently a **non-commercial application**. There is no
advertising, subscription, or monetisation roadmap item, and none should be
added without an explicit product decision. There is no standalone Quran
feature, Quran Kemenag API integration, Quran Foundation API integration, or
Quran audio planned. Tahlil and Istighosah may still contain Quran verses as
part of their original reading text (an ordinary reading step, ADR 0015 —
there is no separate step type for this); those verses are entered as part
of the approved amaliyah content itself, never fetched from a separate
Quran API or service — see `docs/engineering/CONTENT_MODEL.md`.

## `0.0.1` — Core Reader Completion and Public Amaliyah Foundation (current)

Rebaselined by the Figma product-alignment pass
(`docs/reviews/figma-product-alignment.md`) — supersedes this version's
previous "Core Amaliyah Reader" scope description below with a wider,
still-`0.0.1`-scoped foundation:

* Future-proof, scalable **Beranda** (renamed from Serambi — section-based,
  hides a section when no real data backs it; not a hardcoded
  Tahlil/Istighosah card list).
* **Jelajahi Amaliyah** exploration destination (search, category browsing,
  All/Favourite/Offline filters).
* Continue reading, recently opened, local favourites — real persistence,
  offline-first.
* Tahlil (37 reading steps), Istighosah (25 reading steps) — fixed local
  release-candidate content, bundled offline in both debug and release
  builds. (59/27 before ADR 0015's flat-schema migration dropped
  section-heading markers — no reading content changed.)
* Full reader, guided reader, with an in-reader action to switch between
  them without losing progress.
* Contextual Full → Guided repetition shortcut (tap "Dibaca N kali · Buka
  Panduan →" to jump into Guided Reader at the same step).
* Integrated repeated-reading counter.
* Reader Settings, a modal bottom sheet reached from the reader overflow
  menu. (Table of Contents was also planned here but was removed by ADR
  0015 — it depended on step-type/title data the flat schema no longer
  has.)
* Compact source attribution. (A compact `Approved by` status was also
  planned here but was removed by ADR 0015 along with the on-device
  approval object — see `docs/product/PRD.md` §6.5.)
* Bundled offline content is mandatory and always shipped; optional
  24-hour-gated background remote sync against static content on Firebase
  Hosting refreshes it additively once that hosting is deployed, never
  blocking or degrading offline use.
* Phone/tablet/adaptive layout; light/dark theme and RTL support.

Content correction is an internal SanguSantri-team operation, not a
user-facing feature (`docs/operations/CONTENT_GOVERNANCE.md`); there is no
public feedback form, feedback outbox, or feedback endpoint in `0.0.1` or
currently planned for any future version.

**Content Delivery Foundation and Remote Synchronisation** (approved
product/tech-lead decision, superseding this section's earlier "remote
sync remains unscheduled" wording): the Android remote content-sync
foundation — bundled bootstrap, a shared content-package importer, a
Retrofit/OkHttp client against the static content contract, and a
24-hour-gated opportunistic WorkManager sync — is implemented in `0.0.1`.
The `content-hosting/` static files and the Firebase project/CI pipeline
that deploys them are a parallel content-operations workstream and are
**not** deployed yet — see `docs/engineering/ARCHITECTURE.md` §Backend and
ADR [0014](../decisions/0014-firebase-hosting-static-content-delivery.md)
(superseding ADR
[0011](../decisions/0011-go-and-supabase-managed-postgresql-backend.md)'s
never-implemented Go + Supabase backend). Hosting availability must never
block core application usage: the app ships fully functional offline on
bundled content alone, and activating real remote refresh requires only
supplying the real, deployed Firebase Hosting base URL via
`SANGU_CONTENT_API_BASE_URL` — no Android code change or source-selection
flag toggle. See ADR
[0012](../decisions/0012-bundled-bootstrap-and-remote-sync.md) and
`docs/engineering/OFFLINE_FIRST.md`.

See `docs/product/PRD.md` for full scope and acceptance criteria.

## `0.0.2` — Standalone Tasbih

* Independent digital tasbih: **33, 100, unlimited, and custom target**
  (deliberately no 99).
* Compact target selector (not large preset cards); the target and the
  main count are the strongest visual elements on the screen.
* Optional dhikr/session name — no mandatory predefined dhikr selection.
* Remembers the last-selected target; persists an unfinished count across
  app restarts.
* Haptic feedback on increment; reset requires confirmation.
* Custom target opens a small numeric-input dialog, not a full-screen form.

## `0.0.3` — Aktivitas

Renamed from "Riwayat and Streak" — same underlying scope, restructured to
match the confirmed UX: one vertically scrollable screen, independent
sections, **no horizontal tabs**. No Figma frame has been supplied for
this screen yet (`docs/design/FIGMA_HANDOFF.md`) — confirm one exists
before implementation.

* Independent sections, each with an optional "Lihat semua": streak
  summary, this-week summary, amaliyah completion history, tasbih history,
  reminders, quiz progress, pesantren activity.
* Only sections backed by genuinely implemented data are shown — no
  placeholder/fake activity.
* Amaliyah name, version, completion time, duration.
* Private local statistics only — no sharing yet.

## `0.0.4` — Pengingat Amaliyah

* Personal schedules; Tahlil malam Jumat and Istighosah weekly presets.
* Gregorian and Hijri date, notification permission flow.
* Rescheduling after reboot. No "remind me later" requirement.

## `0.0.5` — Nahwu Quiz

**Moved from `0.4.0`** (product owner/tech lead decision, 2026-07-29, ADR
[0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md)) —
scope unchanged from the prior `0.4.0` description below, only the version
number and roadmap position moved, to immediately after Pengingat Amaliyah
and before Accounts:

* Individual, guest, offline-first — bundled static JSON question bank, no
  login, no pesantren selection/representation, no leaderboard, no social
  ranking, no user-generated questions.
* Never a bottom-nav destination — reached only from a Beranda "Belajar"
  entry point (see `docs/product/PRD.md` §7.1).
* Anti-cheating controls, pesantren representation, and seasonal
  leaderboard are **blocked** on Accounts (`0.1.0`) and Pesantren
  Membership (`0.2.0`) and remain future/deferred, not built at `0.0.5` —
  local score is never trusted for competitive ranking.

## `0.1.0` — Accounts

* Google login, phone-number login, minimal profile.
* No mandatory login for public content.

## `0.2.0` — Pesantren Membership

* Pesantren directory managed by SanguSantri; one active pesantren per user.
* Private pesantren code, code rotation, code hashing, membership validation.
* Public users cannot enter pesantren community spaces.

## `0.3.0` — Private Pesantren Space

* Private amaliyah variants, private schedules, pesantren announcements.
* No chat, no public posting.

---

## Navigation model through `0.0.5` (product owner/tech lead decision)

Bottom-navigation-only through `0.0.5` (ADR
[0013](../decisions/0013-bottom-navigation-only-and-nahwu-quiz-0.0.5.md),
2026-07-29) — supersedes this section's earlier five-destination bottom-
bar/rail description. No Navigation Rail is built for any window-size
class, including expanded/tablet, in this window. Destinations are added
incrementally, never speculatively:

* **Beranda** (`0.0.1`) — initial destination throughout.
* **Beranda | Tasbih** (`0.0.2`).
* **Beranda | Aktivitas | Tasbih** (`0.0.3` onward through `0.0.5`).
* Pengingat Amaliyah (`0.0.4`) and Nahwu Quiz (`0.0.5`) are never bottom-
  nav destinations.
* **Profil** (`0.1.0`+) and **Pesantren** (`0.2.0`+) remain entirely out of
  scope through `0.0.5` — no nav item, not even disabled/inert. Whether
  either becomes a further bottom-nav destination, and whether a
  Navigation Rail is ever introduced beyond `0.0.5`, is a future product
  decision not made by this roadmap.

---

## Future Pesantren Rules

Not implemented in `0.0.1`. Future design (from `0.2.0` onward) must account
for these rules:

* A user may belong to only one active pesantren.
* Public users cannot access a pesantren community.
* Membership requires validation; the initial method is a private pesantren
  code.
* Codes must not be stored as plain text and must be rotatable.
* Private amaliyah is visible only to validated members.
* Pesantren-specific content uses the same amaliyah/variant/version model as
  public content — see `docs/engineering/CONTENT_MODEL.md`.
* Public and private content must never be mixed by accidental caching.
* Membership revocation must remove future access to private content;
  previously downloaded private content must be protected or removed after
  membership loss.
