# Al-Qur'an Kemenag — Product Requirements Document

**Document version:** 1.1
**Target release:** Android `0.0.6`
**Status:** Product scope approved; ready for design and engineering planning
**Product owner:** Ahmad Fahmi Aisar
**Decision date:** 8 August 2026

## 1. Purpose

This document is the feature-level product source of truth for the standalone
Al-Qur'an experience in SanguSantri. It supersedes the earlier product rule
that no standalone Quran feature or Kemenag API integration was planned. That
rule existed because a trusted, authorised source had not yet been secured;
the product owner has now obtained official LPMQ Kementerian Agama API access
for SanguSantri.

This document defines release `0.0.6` only. Audio remains a later, separately
approved release. Implementation must also follow:

* `docs/design/QURAN_DESIGN_SYSTEM.md`
* `docs/engineering/QURAN_API_CONTRACT_DRAFT.md`
* ADR `0016`
* the project-wide architecture, security, privacy, testing, and accessibility
  documents linked from `docs/product/PRD.md`.

## 2. Product outcome

SanguSantri users can read the official Mushaf Standar Indonesia Quran text,
Indonesian translation, and Kemenag tafsir in a focused dark-only reader. The
feature is public, account-free, local-first after its initial online
preparation, and integrated with SanguSantri's local activity streak.

The experience must support four common intents:

1. Find and open a surah.
2. Navigate by Juz or Kemenag page metadata.
3. Continue the user's last reading position.
4. Read Arabic alone or Arabic with translation, then open tafsir when wanted.

## 3. Source and product identity

* Source: Lajnah Pentashihan Mushaf Al-Qur'an (LPMQ), Kementerian Agama
  Republik Indonesia.
* API base URL:
  `https://quran-api.lpmqkemenag.id/api-alquran`.
* On Beranda, the feature label is **Al-Qur'an Kemenag**.
* Inside the feature, the app-bar title is **Al-Qur'an**.
* Source attribution text is:
  **“Sumber teks Al-Qur'an, terjemahan, dan tafsir: Lajnah Pentashihan Mushaf
  Al-Qur'an, Kementerian Agama Republik Indonesia.”**
* SanguSantri must not imply any broader institutional endorsement beyond the
  written API access and source attribution actually granted.
* The data is read-only. SanguSantri does not editorially rewrite, merge,
  translate, or AI-correct the Kemenag payload.

## 4. Release scope

### 4.1 Included in `0.0.6`

* Beranda entry point; not a bottom-navigation destination.
* Full-screen Quran experience with the global bottom navigation hidden.
* Dark-only Quran theme, regardless of the app/system theme outside Quran.
* Surah, Juz, and Bookmark tabs, plus an optional Terakhir dibaca card.
* Search by surah name or surah number only.
* Arab-saja display: flowing Arabic grouped by the API's `halaman` field.
* Arab+terjemahan display: one ordered ayat block at a time.
* One global persisted display choice: Arab saja or Arab + terjemahan.
* Locally persisted global last-read position.
* Local ayat bookmarks without folders or notes.
* Long-press ayat actions: bookmark, tafsir, mark last read, Juz/page metadata.
* Kemenag tafsir ringkas and tahlili in a bottom sheet.
* Cached tafsir available offline after its first successful fetch.
* Full initial Quran preparation from the Kemenag API, followed only by
  Remote Config version-gated corpus updates; no periodic full refresh.
* Room-backed offline reading after initial preparation succeeds.
* Local reading-session history in Aktivitas.
* Quran reading contributes to the existing combined amalan streak.
* User-controlled Quran font, Arabic size, Arabic line spacing, translation
  size, translation visibility, and Quran-window brightness.
* Fresh Quran preferences start at 24sp Arabic with 2.00× Arabic line spacing;
  the Arabic-size control spans 14–52sp in 2sp steps.
* Portrait-primary design without forcing device orientation.
* Full-screen source/permission information destination.

### 4.2 Explicitly excluded

* Quran audio, streaming audio, downloads, reciters, and playback UI.
* Latin transliteration, even though the API exposes a `teks` field.
* Copy or share actions for Arabic text, translation, or tafsir.
* User-created notes, bookmark folders, highlights, or annotations.
* Target khatam, per-Juz completion, daily reading goals, or reminders.
* Full-text Arabic, translation, or tafsir search.
* User-managed download/delete controls.
* Account sync, cloud backup, or cross-device reading state.
* Analytics or upload of reading history.
* A pixel-identical facsimile of the printed Mushaf Standar Indonesia or
  Mushaf Madinah page layout.
* Quran Foundation or any second Quran-content API.
* A server/proxy owned by SanguSantri.

## 5. Information architecture

```text
Beranda
└── Al-Qur'an Kemenag
    ├── Initial preparation / retry
    ├── Quran hub
    │   ├── Surah
    │   ├── Juz
    │   ├── Bookmark
    │   └── Terakhir dibaca card (when a saved position exists)
    ├── Reader
    │   ├── Arab saja — flowing page by `halaman`
    │   └── Arab + terjemahan — ordered ayat rows
    ├── Ayat action sheet
    ├── Tafsir Kemenag sheet
    ├── Tampilan Al-Qur'an
    └── Sumber
```

The existing top-level shell remains Beranda | Aktivitas | Tasbih. Entering
Al-Qur'an hides that shell's bottom navigation; leaving Al-Qur'an restores the
previous destination and theme. This feature must use the existing Navigation
3 back stack, not introduce a second navigation framework or Activity.

## 6. Core flows

### 6.1 First entry with no local Quran

1. User taps **Al-Qur'an Kemenag** on Beranda.
2. Quran dark theme applies immediately.
3. The app checks Room; no complete Quran dataset exists.
4. If connected, show **“Menyiapkan Al-Qur'an Kemenag…”** with determinate
   progress by completed surah count.
5. Fetch and validate the surah list and all 114 surah responses outside the
   main thread.
6. Only after the complete dataset validates, replace the active Room Quran
   dataset in one transaction.
7. Open the Quran hub.

If any request or validation fails, discard the in-memory candidate dataset
and show a concise error with **Coba lagi**. A retry starts initial preparation
again from the beginning; `0.0.6` does not implement resumable staging.

If the device is offline and no complete local dataset exists, show:

* title: **Al-Qur'an belum tersedia offline**;
* explanation: connect to the internet once to prepare Quran data;
* action: **Coba lagi**.

### 6.2 Later entry with local Quran

1. Render the hub immediately from Room.
2. Reading remains fully offline; opening the hub never starts a network call.
3. At an application-start opportunity, fetch the lightweight Firebase Remote
   Config value `quran_stable_version`.
4. When the remote target is greater than the locally applied version, enqueue
   one unique, unmetered, battery-not-low update. Equal or lower targets do
   nothing; there is no time-based fallback refresh.
5. Continue rendering the current local dataset during an eligible update.
6. A failed update keeps the current dataset unchanged and enters a 24-hour
   cooldown for that target. A newer target may bypass the old target's
   cooldown.
7. A successful complete update atomically replaces the active source data,
   clears version-coupled tafsir cache, and records the applied target version
   in the same Room transaction.

### 6.3 Reading

1. User opens a surah, Juz start, page, bookmark, or recent session.
2. Reader opens at the requested `(surah, ayat)` identity.
3. Last global mode and display settings are restored.
4. Position updates only after the visible ayat actually changes.
5. Leaving the reader persists one local reading session if at least one ayat
   of progress occurred.

### 6.4 Ayat actions and tafsir

1. User long-presses an ayat block.
2. A modal action sheet offers Bookmark, Tafsir Kemenag, Tandai terakhir
   dibaca, and Juz/halaman information.
3. Choosing tafsir opens a modal bottom sheet.
4. If cached tafsir exists, render it immediately. When stale, refresh it in
   the background.
5. If no cache exists, show **“Memuat tafsir Kemenag…”**.
6. A fetch failure with no cache shows an inline error and **Coba lagi**;
   reader content remains usable.

## 7. Functional requirements

### QUR-FR-001 — Beranda entry and theme boundary

Beranda must expose a real, accessible **Al-Qur'an Kemenag** entry. Quran is
not a bottom-navigation destination. Every Quran screen, dialog, and sheet
uses the Quran dark scheme. Leaving the feature restores the previous app
theme. System status/navigation bars follow the Quran dark scheme while the
feature is active.

### QUR-FR-002 — Initial complete preparation

The app must fetch `/surah/local/1/114` and `/ayat/local/{no_surah}` for every
surah before declaring the initial local Quran dataset ready. It must not
render directly from response DTOs or expose a partially valid dataset as a
complete Quran. Failure restarts from the beginning on the next explicit
retry.

### QUR-FR-003 — Technical validation

Validation is structural, not editorial. It must verify at minimum:

* successful envelope (`code == 200`, `res == "success"`);
* exactly 114 unique surah IDs numbered 1–114;
* each response ayat belongs to the requested surah;
* unique `(surah, ayat)` and unique non-null remote `id` values;
* ayat numbers form `1..jmlAyat` with no missing or duplicate number;
* non-blank `teks_msi_usmani` and `terjemah`;
* positive `juz` and `halaman` source metadata.

The API array order is never trusted. Canonical reading order is numeric
`surah`, then numeric `ayat`. Validation must not silently invent, repair, or
merge missing religious content.

### QUR-FR-004 — Remote Config version-gated atomic update

`quran_stable_version` is a positive, monotonically increasing SanguSantri
operations trigger with an app default of `1`; it is not a version supplied or
verified by the Kemenag API. After a complete local dataset exists, the app
compares this target with `quran_applied_stable_version` in `app_metadata`.
Only a strictly higher target may enqueue one unique full update. Equal or
lower values are ignored, so a Remote Config decrease never downgrades data.
There is no weekly, monthly, or other elapsed-time full refresh.
An already-complete dataset created before this metadata existed adopts
baseline version `1` locally without downloading the corpus again.

An eligible update uses unmetered network and battery-not-low constraints. It
makes one complete attempt; handled failures are not immediately retried by
WorkManager and place that target under a 24-hour durable cooldown. A newly
published higher target is eligible immediately. All 114 surah responses must
validate before one Room transaction replaces source rows and writes the
applied version. Any network, HTTP, parsing, validation, cancellation, or Room
failure preserves the previous complete dataset and applied version. There is
no partial activation.

Operations must increase the Remote Config value only after the intended
Kemenag data is fully live and manually verified. Because Kemenag exposes no
dataset manifest or historical snapshot, this number triggers a fresh read of
the current API; it does not prove which upstream revision was returned.

### QUR-FR-005 — Quran hub

The hub shows three equal-width tabs: Surah, Juz, and Bookmark. Terakhir dibaca
is a prominent card above the tabs only when a saved position exists; it is not
a fourth tab and has no empty-state placeholder. Labels must not be reduced
below accessible text size.

### QUR-FR-006 — Surah browsing and search

Surah rows show number, Latin name, Arabic name, meaning, Makkiyyah/Madaniyyah
category, and ayat count exactly from local Kemenag data. Search matches only
surah name and number, is case/diacritic tolerant for Latin names, and never
performs a network request.

### QUR-FR-007 — Juz browsing

Juz 1–30 are derived only from locally stored `juz` fields. Each row may show
its first locally ordered surah/ayat and page. No hardcoded or AI-derived
Juz-to-ayat mapping is allowed.

### QUR-FR-008 — Arab-only flowing page

Arab-saja display groups locally ordered verses by the API `halaman` value and
renders Arabic as one responsive flowing surface with inline ayat markers.
“Page” in this release means Kemenag metadata grouping, not guaranteed
printed-mushaf line composition. The API supplies no line-break or glyph
coordinate contract, so wrapping follows the selected font, size, line spacing,
available width, and Android text layout. Translation is never interleaved in
this flowing surface.

### QUR-FR-009 — Arab with translation rows

Arab+terjemahan display renders one stable lazy item per ordered ayat so each
official translation and optional source annotation stays bound to exactly one
ayat. Switching the global display choice changes both content visibility and
layout; there is no separate Halaman/Ayat-mode setting. The API's Latin `teks`
value must not be stored or displayed. Quran/translation text selection is
disabled in `0.0.6` because platform selection would expose copy behaviour; the
product intentionally provides no copy/share path.

When `keterangan`, `no_foot`, or `teks_foot` is non-blank, Arab+translation
mode renders the source annotation immediately after its translation with a
subordinate style and preserved source wording. Empty source fields render
nothing. The app must not infer a footnote association or rewrite its number.

### QUR-FR-010 — Basmalah header

Al-Fatihah must not receive an extra header basmalah because its basmalah is
ayat 1. At-Taubah must not display a basmalah header. Surah 2–8 and 10–114
display one source-verified basmalah header. The Arabic basmalah asset must be
obtained from and verified against an official source; it must never be
transcribed or corrected by AI.

At the beginning of every surah, the reader also displays one compact,
non-sticky source header containing the exact locally stored Kemenag category,
surah name, and ayat count. This header and the basmalah appear only at the
surah start, not at every Kemenag `halaman` boundary.

### QUR-FR-011 — Last read

Exactly one global last-read position is persisted by stable `(surah, ayat)`
identity, with page and timestamp as derived context. It powers the hub's
continue action and “Tandai terakhir dibaca.” Process death, refresh, and app
restart must preserve it when the referenced ayat remains valid.

### QUR-FR-012 — Bookmark

Bookmark is local, ayat-level, unlimited, and idempotent. There are no folders,
notes, colours, or cloud sync. Refresh must preserve bookmarks by `(surah,
ayat)` identity.

### QUR-FR-013 — Tafsir

Tafsir is requested from `/ayat/local/tafsir/{ayat_id}`, where `ayat_id` is
the remote `id` stored with the verse. Both concise `teks` and `tahlili` are
shown with the fixed labels **Tafsir Ringkas** and **Tafsir Tahlili**
respectively. Successful
responses are cached with `cachedAt`; cached data remains readable offline.
A cache older than seven days is stale-while-revalidate on the next open.

### QUR-FR-014 — Long-press interaction

Long-press is the sole visible ayat-action affordance to keep the reader
clean. Implementation must use semantic long-click support so accessibility
services can expose the action without adding visible controls. Haptic
feedback may acknowledge a recognised long press. An ordinary tap must not
accidentally bookmark or open tafsir.

In Arab-saja flowing display, pointer coordinates are resolved through the
composed text layout to the annotated character range for one stable ayat ID.
Only that range receives the selected background while the action sheet is
open. Arab+terjemahan display applies the same selected state to the complete
ayat row. The selection colour is a non-error Quran container role; modal sheet
presence, focus, and haptic feedback ensure state is not communicated by colour
alone.

### QUR-FR-015 — Reader settings

Tampilan Al-Qur'an is a full-screen nested destination because font previews
and live controls exceed a compact sheet. It must provide:

* a mutually exclusive font preview-card selector;
* Arabic-size slider with live verified-text preview;
* Arabic line-spacing slider with live preview;
* translation-size slider with live preview;
* Arab saja / Arab + terjemahan choice;
* Quran-window brightness slider.

No “keep screen on” control is included. Brightness overrides only the Quran
window and restores the prior window value on exit.

### QUR-FR-016 — Font choices

LPMQ Isep Misbah is the default choice and Amiri Quran is selectable. The
official King Fahd Complex smart-device Hafs choice remains visible but disabled
as `Belum tersedia` until its font asset and accompanying licence/readme are
supplied. A fixed Kemenag-text corpus still gates release acceptance for
missing glyphs, mark collisions, clipping, shaping, and Android-version
consistency. Selecting a font changes glyph rendering only; it never changes
the stored Kemenag string.

### QUR-FR-017 — Activity and streak

One local reading-session event is written when the reader closes after its
position advanced by at least one ayat. Merely opening and closing does not
count. Aktivitas shows surah, read ayat range, and local timestamp. A
qualifying Quran session contributes to the existing combined amalan streak;
it does not create a second Quran-only streak.

### QUR-FR-018 — Privacy and user-state locality

Bookmarks, last position, settings, tafsir cache, and reading sessions remain
on device. They are not uploaded, included in analytics, or logged. Initial
full Quran fetch requests all surahs; a later tafsir request necessarily
reveals the requested remote ayat ID to Kemenag and must be disclosed in the
privacy policy. No analytics SDK is introduced by this feature.

### QUR-FR-019 — Source and permissions

The full-screen **Sumber Al-Qur'an** view is reachable from the hub overflow
and the bottom of Tampilan Al-Qur'an. It must identify LPMQ/Kementerian Agama
RI and explain which fields originate from the API. It must not claim that
SanguSantri is an official Kemenag application. No user-facing copy/share
control is included. Screenshots remain permitted; the feature must not enable
`FLAG_SECURE`.

### QUR-FR-020 — Portrait-primary resilience

The design references and the principal experience target portrait phones. The app must not
force portrait orientation, create a Quran-only Activity, or lose state when
rotation occurs. Landscape and larger windows must remain functional and use
a constrained readable column, even though they do not receive separate
feature-specific visual compositions in `0.0.6`.

## 8. Data ownership and persistence

Room remains the only UI-readable source of truth. The expected clean-baseline
tables are:

| Table                    | Responsibility                                                              |
|--------------------------|-----------------------------------------------------------------------------|
| `quran_surahs`           | Kemenag surah metadata                                                      |
| `quran_verses`           | Kemenag ayat source fields keyed by `(surah, ayat)` with unique remote `id` |
| `quran_tafsir`           | Cached ringkas/tahlili per ayat plus `cachedAt`                             |
| `quran_bookmarks`        | Local ayat bookmark plus `createdAt`                                        |
| `quran_reading_state`    | Singleton global last-read position                                         |
| `quran_reading_sessions` | Local qualifying sessions for Aktivitas/streak                              |

Reader preferences belong in the existing DataStore. Sync bookkeeping should
reuse the existing Room-backed `app_metadata` mechanism rather than create a
table that only wraps timestamps. Expected metadata keys are full-sync last
attempt, last success, and terminal status.

This model is a separate bounded context from amaliyah `Content`/
`ContentStep`; Quran must not be forced through the amaliyah content-package
schema. Repositories expose domain models. DTO/entity/domain separation is
required because the boundaries differ; a duplicate identical UI model is
not.

## 9. API and credential requirements

Requests use `username` and `token` headers. The product owner chose a direct
Android client rather than a proxy and accepts that any credential shipped in
an APK can ultimately be extracted. Hardening raises effort but must not be
described as absolute secrecy.

The release implementation must:

* keep real credentials out of Git, Kotlin, XML, assets, `BuildConfig`, logs,
  exceptions, and documentation;
* source release values from an untracked local/CI secret;
* generate native build input outside the source tree;
* split/encode and reconstruct the credential in native C++ only at request
  time;
* verify the expected release signing-certificate digest before providing it;
* strip native symbols and enable existing release R8/resource shrinking;
* use a Quran-specific authenticated OkHttp client so Kemenag headers can
  never leak to Firebase Hosting or another origin;
* redact both header names/values from all logging and test interceptors;
* fail release assembly when required credential inputs are absent;
* support credential rotation through a new signed app release.

Debug and automated tests use fakes/MockWebServer and never require production
credentials. TLS is mandatory. Certificate pinning and root detection are not
required because they do not solve static credential extraction and introduce
separate operational failure modes.

## 10. Loading, empty, and error states

Required states include:

* initial checking;
* initial preparation with progress;
* initial preparation network/HTTP/parse/validation failure;
* no local data while offline;
* populated hub;
* empty bookmarks;
* empty recent sessions;
* version-triggered background update without blocking content;
* failed version update retaining cached content;
* reader loading from Room;
* invalid/deleted navigation target;
* tafsir loading;
* cached tafsir plus refresh;
* tafsir unavailable offline;
* tafsir retryable/permanent failure.

User-facing errors must be concise Indonesian text, never raw HTTP bodies,
stack traces, tokens, or Arabic payload fragments.

## 11. Non-functional requirements

* Typical API 26 phones must scroll long surahs smoothly using lazy rendering.
* Initial parsing, validation, sorting, and Room writes run off the main thread.
* Initial preparation and version-triggered updates use bounded request
  concurrency; never fan out 114 unbounded calls.
* Reader state survives process death and configuration change.
* Quran screens support font scale 1.5×, TalkBack, 48dp touch targets, correct
  RTL Arabic rendering, and non-colour state cues.
* Arabic font size and line spacing must accommodate Quranic combining marks
  without clipping.
* No raw Quran, translation, tafsir, bookmark, reading position, or credential
  appears in application logs or crash payloads.
* The dark palette and typography must be manually tested on OLED and LCD at
  low/medium brightness and in at least one continuous 30-minute reading
  session; the product must not claim medically guaranteed eye comfort.

## 12. Acceptance criteria

Release `0.0.6` is acceptable only when:

1. A clean install with internet can prepare, validate, persist, and open all
   114 surahs.
2. A clean install without internet shows the intended retryable empty state.
3. After preparation, airplane mode supports Surah, Juz, Page/Ayat reading,
   bookmarks, last read, recent sessions, settings, and previously cached
   tafsir.
4. The supplied out-of-order Surah 114 example renders 1–6 in numeric order.
5. A failed initial preparation writes no active partial dataset.
6. Equal/lower Remote Config versions cause no Kemenag corpus request; a higher
   version causes at most one eligible update attempt, with no periodic fallback.
7. A failed version-triggered update leaves the previous complete dataset
   byte-for-byte readable; a successful update preserves bookmark and last-read
   identities and atomically advances the applied version.
8. Arab-only and Arab+translation modes restore after process death.
9. No Latin transliteration, copy/share, or audio affordance appears.
10. Al-Fatihah, At-Taubah, and another surah satisfy the basmalah rules.
11. Long-press actions, cached/stale tafsir, retry, and offline tafsir states
    behave as specified.
12. One-ayat progress creates one Activity session and contributes to the
    combined streak; open/close without progress does not.
13. Every enabled font passes the verified-corpus glyph test and renders live
    preview/settings consistently with the reader.
14. Entering Quran from light mode makes all Quran surfaces/system bars dark;
    leaving restores light mode and prior brightness.
15. Release artifacts and logs contain no plain credential string, while the
    ADR continues to acknowledge native extraction remains possible.

## 13. Blocking production inputs

* The real Kemenag `username` and `token`, supplied only through approved
  local/CI secret storage.
* Written confirmation that the granted credential covers public SanguSantri
  end-user traffic.
* LPMQ Isep Misbah APK-embedding/redistribution permission or an explicit
  licence accompanying the product owner's written access.
* King Fahd font file plus its included licence/readme if that choice is to
  ship.
* Verified official basmalah source asset.
* Final source/attribution wording checked against the written permission.
* Privacy-policy update describing Kemenag API requests and local Quran state.

## 14. Delivery sequence

The release is one complete `0.0.6` product scope, but engineering should land
it in reviewable implementation slices:

1. API contract, credential boundary, Room baseline, validator, initial/full
   sync, repositories.
2. Hub, search, tabs, bookmarks, last-read state.
3. Page/Ayat readers, basmalah, long-press actions, settings, font gates.
4. Tafsir cache/sheet, Aktivitas/streak integration, source view.
5. Design parity, accessibility, security audit, full automated/manual release
   validation.

No slice is independently released as a reduced MVP; unfinished navigation
must remain inaccessible until the complete `0.0.6` acceptance gate passes.
