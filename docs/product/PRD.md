# SanguSantri Product Requirements Document

**Document version:** 1.0
**Product:** SanguSantri
**Initial release:** Android `0.0.1`
**Package name:** `com.sangusantri.app`
**Product owner:** Ahmad Fahmi Aisar
**Document status:** Ready for engineering
**Primary implementation agent:** Claude
**Initial platform:** Native Android
**Backend:** Go + PostgreSQL
**Date:** 25 July 2026

---

# 1. Document Purpose

This document is the single source of truth for designing and implementing SanguSantri.

Claude must use this PRD to:

1. Create the Android application foundation.
2. Create the Go backend foundation.
3. Implement release `0.0.1`.
4. Preserve an architecture suitable for long-term development.
5. Prevent duplicated code, duplicated models, and parallel implementations of the same functionality.
6. Avoid inventing religious content, product rules, technical dependencies, or requirements not stated here.

Claude must implement only the release currently requested. Future roadmap items must influence extensibility, but must not be implemented prematurely.

Normative language:

* **MUST:** mandatory.
* **SHOULD:** expected unless there is a documented technical reason.
* **MAY:** optional.
* **MUST NOT:** prohibited.

---

# 2. Product Overview

## 2.1 Product name

**SanguSantri**

“Sangu” means provisions or supplies. SanguSantri represents a digital provision for santri in their religious practice, study, and pesantren community life.

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
* Optional subscriptions and advertising.

The product will be strongly aligned with Nahdlatul Ulama traditions while remaining open to pesantren with other traditions.

Unless a formal relationship is established, SanguSantri MUST be presented as an independent product and MUST NOT imply that it is an official PBNU or NU application.

## 2.3 Initial product position

Release `0.0.1` is a public, account-free, offline-first amaliyah reader containing:

* Tahlil.
* Istighosah.

The first release is not the super-app itself. It is the reliable foundation upon which the super-app will be built.

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

## 3.2 Offline by default

Public amaliyah must remain usable without:

* Login.
* Internet connection.
* An available backend.
* Previously completed synchronisation.

The application must ship with approved seed content.

## 3.3 Progressive delivery

The product follows a small, frequent release strategy.

Each release should introduce one clear, user-visible vertical slice. A weekly release is a target, not permission to ship an unstable build.

## 3.4 No forced account

Public users must be able to access the core amaliyah without registration.

Authentication is introduced only when a feature genuinely requires identity, such as private pesantren access.

## 3.5 One content source, multiple reading experiences

Full reading mode and guided reading mode must render the same canonical content model.

The implementation MUST NOT duplicate Tahlil or Istighosah content for each reader mode.

## 3.6 Clean architecture without ceremony

The project must have clear UI, domain, and data boundaries.

It MUST NOT create unnecessary abstractions such as:

* One use case for every repository method.
* Generic base repositories.
* Generic base ViewModels.
* Empty wrapper classes.
* Interfaces with only one implementation when no testing or boundary benefit exists.
* Separate UI models that are identical to domain models.

## 3.7 Long-term maintainability

The codebase must favour:

* Explicit behaviour.
* Small classes.
* Stable naming.
* Testable business logic.
* Reusable design tokens.
* Minimal hidden magic.
* Migration-safe persistence.
* Backward-compatible content schemas.

---

# 4. Release Strategy

## 4.1 Application versioning

Application versions use pre-1.0 semantic progression:

* `0.0.1`
* `0.0.2`
* `0.0.3`
* `0.0.4`

Each public build must have an incremented Android `versionCode`.

## 4.2 Content versioning

Application versions and content versions are independent.

Examples:

* Application version: `0.0.1`
* Tahlil general variant: `tahlil-general@1`
* Istighosah general variant: `istighosah-general@1`
* Content schema version: `1`

A correction to Tahlil content must not require an APK release unless the content schema or reader capability changes.

## 4.3 Weekly release rule

Each weekly release should contain:

* One main user-visible improvement.
* Relevant tests.
* Documentation updates.
* No unrelated unfinished feature.
* No hidden breaking schema change.

A release must be withheld when its quality gate fails.

## 4.4 Feature flags

Future unfinished features must remain inaccessible through local feature flags or server configuration.

Feature flags must have safe offline defaults.

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
17. Reader appearance settings.
18. Light and dark themes.
19. Green Islamic visual identity.
20. Traditional-modern pesantren design direction.
21. Offline seed content.
22. Automatic non-blocking content synchronisation.
23. Preservation of previous content versions.
24. Source and approval details for every amaliyah.
25. Content correction feedback.
26. Portrait and landscape support.
27. Phone and tablet support.
28. Edge-to-edge layout.
29. Go public content API.
30. Go content administration CLI.
31. PostgreSQL database.
32. Temporary database administration through Supabase Studio.
33. Automated Android and backend testing.
34. CI validation.

## 5.2 Explicitly excluded

Release `0.0.1` does not include:

* User login.
* Phone OTP.
* Google login.
* Pesantren verification.
* Pesantren invitation codes.
* Private pesantren content.
* Pesantren schedules.
* Announcements.
* Community posts.
* Comments.
* Chat.
* Inter-pesantren rankings.
* Nahwu quizzes.
* Standalone digital tasbih.
* Streaks.
* Reading history screen.
* Gregorian or Hijri reminders.
* Shareable achievement cards.
* Downloadable Quran audio.
* Advertising.
* Subscriptions.
* Google Play Billing.
* A custom web CMS.
* Multiple pesantren variants.
* Multiple active public variants.
* A comparison of differences between variants.

The data model may support future variants, but the `0.0.1` interface exposes only one default general variant for each amaliyah.

---

# 6. Initial Content

## 6.1 Tahlil

Displayed title:

**Tahlil**

Variant:

**Umum**

Initial editorial reference:

* NU Online’s “Bacaan Tahlil Singkat, Lengkap dengan Doa dan Terjemahannya.”

The article presents an ordered Tahlil sequence including Arabic readings, repetition counts, prayers, and Indonesian translations. It must be used as an editorial reference, not automatically scraped into production.

## 6.2 Istighosah

Displayed title:

**Istighosah**

Variant:

**Umum**

Proposed initial editorial reference:

* The KH Romli Tamim Istighosah collection available through Quran NU Online.

This source remains subject to selection and approval by the assigned kyai or sesepuh.

## 6.3 Content entry rule

Claude MUST NOT:

* Invent Arabic readings.
* Invent translations.
* Generate missing prayers from memory.
* Automatically scrape and publish website text.
* Correct religious content based solely on AI judgement.
* Merge different versions without written instruction.
* add Latin transliteration.

Content must be entered into structured content files by the product or content team.

Development fixtures may use clearly marked sample text, but:

* Fixtures must never enter the release build.
* The release build must fail validation when approved production content is missing.

## 6.4 Quran content

Any Quran ayah appearing inside an amaliyah must contain:

* Surah number.
* Ayah number or range.
* Approved Arabic text.
* Approved Indonesian translation.
* Source identifier.
* Optional future audio identifier.

Quran text, translation, and audio licensing must be verified separately.

## 6.5 Approval

Each published content version must include:

* Approver name.
* Approver role.
* Institution or pesantren, when applicable.
* Approval date.
* Approval status.
* Approval document.
* Source name.
* Source reference.
* Internal reviewer name.
* Content checksum.

Approval verifies the accuracy of the specified content version. It must not be presented as institutional endorsement of the entire SanguSantri application unless such endorsement exists in writing.

## 6.6 Approval document privacy

The raw signed document should be stored privately.

Users may view:

* A redacted approval document.
* Approver identity.
* Role.
* Date.
* Approval scope.
* Document reference number.

Private signatures, phone numbers, addresses, and identity numbers must be redacted when unnecessary.

---

# 7. Information Architecture

## 7.1 Primary destinations

Release `0.0.1` uses the following destinations:

1. Bootstrap.
2. Serambi.
3. Reader mode selection.
4. Amaliyah reader.
5. Source and approval detail.
6. Reader settings.
7. Feedback form.
8. About SanguSantri.

A bottom navigation bar is not required for `0.0.1`. The number of destinations does not justify permanent bottom navigation.

## 7.2 Pesantren terminology

Preferred Indonesian labels:

* Home: **Serambi**
* Practices: **Amaliyah**
* Guided reading: **Panduan**
* Full reading: **Bacaan Lengkap**
* Counter: **Tasbih**
* Content verification: **Sumber & Pentashihan**
* Settings: **Setelan**
* Feedback: **Koreksi Bacaan**
* Continue reading: **Lanjutkan Bacaan**

Arabic localisation must use natural Arabic labels and proper RTL layout rather than transliterated Indonesian terminology.

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
4. User chooses:

    * Bacaan Lengkap.
    * Panduan.
5. The chosen mode may be saved as the default later.
6. Reader restores unfinished progress when available.

## 8.3 Full reading mode

1. Application displays all reading sections in one vertically scrollable screen.
2. Each section displays:

    * Section title when applicable.
    * Arabic text.
    * Indonesian translation.
    * Repetition target when applicable.
3. Repeated sections display an embedded counter.
4. User manually scrolls.
5. User may mark the amaliyah complete after required counters are completed.
6. Scroll position and counters survive process death.

## 8.4 Guided reading mode

1. Application displays one logical step at a time.
2. Current position is visible, for example `5 of 22`.
3. Arabic text and translation are displayed together.
4. If a step has a repeat target, the counter appears.
5. Counter tap produces haptic feedback.
6. Progression behaviour is configurable:

    * **Automatic:** move to the next step when the target is reached.
    * **Manual:** enable the Continue button when the target is reached.
7. Non-counter steps use a Continue button.
8. User may move to the previous step.
9. The session resumes at the last unfinished step.

## 8.5 Viewing source and approval

1. User opens the overflow menu or information action.
2. User selects **Sumber & Pentashihan**.
3. Application displays:

    * Source.
    * Content version.
    * Approver.
    * Approval date.
    * Approval status.
    * Redacted approval document.
4. Previous downloaded versions may be opened from this screen.

## 8.6 Reporting a correction

1. User opens **Koreksi Bacaan**.
2. The current amaliyah, version, and step are preselected.
3. User selects a category:

    * Arabic text.
    * Harakat.
    * Translation.
    * Repetition count.
    * Source.
    * Audio, in future.
    * Other.
4. User writes a description.
5. Feedback is saved locally first.
6. Application submits it when connected.
7. The user receives a local submission status.

No account is required.

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

Serambi MUST display:

* SanguSantri identity.
* Tahlil card.
* Istighosah card.
* Continue-reading section when progress exists.
* A subtle content update status.
* Access to Setelan.
* Access to About.

Cards must use data from Room, not hardcoded screen lists.

## FR-003: Default variants

Each amaliyah MUST have one default general variant in `0.0.1`.

Selecting an amaliyah opens the default variant immediately.

The data layer must support future variants without exposing a variant selector yet.

## FR-004: Full reading mode

Full reading mode MUST:

* Render all content from one canonical version.
* Use stable item keys.
* Support large Arabic text.
* Display translations per ayah or segment.
* Display embedded counters.
* Restore scroll and counter state.
* Work in portrait and landscape.
* Avoid keeping the screen permanently awake.

The application MUST NOT set `FLAG_KEEP_SCREEN_ON`.

## FR-005: Guided mode

Guided mode MUST:

* Display one step at a time.
* Display progress.
* Support back and forward navigation.
* Support automatic advancement.
* Support manual advancement.
* Persist selected progression behaviour.
* Preserve the current step after process death.

## FR-006: Integrated counter

A step may define a positive repetition target.

The counter MUST:

* Begin at zero unless restored.
* Never exceed the target in integrated mode.
* Provide haptic feedback.
* Display current and target values.
* Provide reset with confirmation.
* Persist immediately after meaningful changes.
* Mark the step complete when the target is reached.

No sound is required.

## FR-007: Completion

An amaliyah is complete only when:

1. Every required counter reaches its target.
2. The user presses the final completion confirmation.

Opening the final page alone must not mark completion.

A completed state may be stored locally, but streak and history interfaces are excluded from `0.0.1`.

## FR-008: Reader settings

Users MUST be able to configure:

* Arabic font size.
* Translation font size.
* Arabic line spacing.
* Translation line spacing.
* Light theme.
* Dark theme.
* System theme.
* Reader background style.
* Show or hide translation.
* Guided progression: automatic or manual.
* Indonesian or Arabic application language.

Preferences must use DataStore and apply without restarting the application when practical.

## FR-009: Content details

Every published amaliyah version MUST expose:

* Source.
* Version.
* Approval.
* Publication date.
* Content checksum.
* Approval document metadata.

A content item without an approved status must not appear in the production catalogue.

## FR-010: Content synchronisation

Synchronisation MUST:

* Run without blocking local reads.
* Compare local and remote manifests.
* Download only newer or missing packages.
* Validate schema version.
* Validate checksum.
* Import inside a database transaction.
* Activate the new version only after a successful import.
* Preserve previous versions.
* Retain current local content when synchronisation fails.
* support manual retry.
* respect network constraints.

When a valid new version is imported, it becomes the default automatically.

## FR-011: Previous versions

Previous downloaded content versions must remain accessible through **Sumber & Pentashihan**.

The main amaliyah card always opens the latest active approved version.

If the latest version is revoked:

* The backend manifest marks it revoked.
* The application falls back to the newest non-revoked approved version.
* The revoked version is not opened by default.

## FR-012: Feedback

Feedback MUST be written locally before network submission.

A local outbox must track:

* Pending.
* Sending.
* Submitted.
* Failed.

Feedback submission must include:

* Anonymous installation identifier.
* Application version.
* Amaliyah ID.
* Variant ID.
* Content version ID.
* Step ID when applicable.
* Feedback category.
* User description.
* Device locale.
* Timestamp.

It must not include the user’s devotional history or counter history.

## FR-013: Localisation

The release MUST support:

* Indonesian.
* Arabic.
* RTL layout.
* Localised dates.
* Localised numerals where appropriate.
* Mirrored navigation icons in RTL.

Arabic devotional content must remain correctly aligned and readable regardless of the selected interface language.

## FR-014: Adaptive layout

The application MUST support:

* Compact phones.
* Landscape phones.
* Foldables where possible.
* Tablets.

Reader text should use a constrained readable width on large displays rather than stretching from edge to edge.

## FR-015: Edge-to-edge

The application must use modern edge-to-edge rendering.

System bar and keyboard insets must be handled exactly once. Interactive content must not be hidden beneath status bars, navigation bars, or the IME.

---

# 10. Reader Content Model

## 10.1 Core hierarchy

The canonical hierarchy is:

```text
Amaliyah
└── Variant
    └── Immutable Version
        └── Ordered Steps
            └── Optional Assets
```

Examples:

```text
Tahlil
└── Umum
    └── Version 1
        ├── Step 1
        ├── Step 2
        └── ...
```

Future example:

```text
Tahlil
├── Umum
└── Pondok A
    └── Version 1
```

## 10.2 Step types

Supported initial step types:

* `HEADING`
* `INSTRUCTION`
* `ARABIC_TEXT`
* `QURAN_AYAH`
* `PRAYER`
* `REPEATED_READING`
* `DIVIDER`
* `CLOSING`

A single step may contain:

* Indonesian title.
* Arabic title.
* Arabic body.
* Indonesian translation.
* Quran reference.
* Repetition target.
* Reader instruction.
* Future audio reference.

## 10.3 Translation segmentation

For Quran content:

* Translation must map to its corresponding ayah.

For non-Quran content:

* Translation must map to a logical Arabic segment.
* Long prayers may be split into manageable segments.
* The meaning must not be rearranged merely for visual convenience.

## 10.4 Content immutability

Published versions are immutable.

Any correction creates a new version.

The system must never mutate an already approved version in place.

---

# 11. Database Model

## 11.1 Server tables

### `amaliyah`

Fields:

* `id`
* `slug`
* `title_id`
* `title_ar`
* `description_id`
* `description_ar`
* `category`
* `status`
* `created_at`
* `updated_at`

### `amaliyah_variants`

Fields:

* `id`
* `amaliyah_id`
* `slug`
* `name_id`
* `name_ar`
* `owner_type`
* `pondok_id`
* `visibility`
* `is_default`
* `created_at`

Initial values:

* `owner_type = PUBLIC`
* `pondok_id = null`
* `visibility = PUBLIC`

### `amaliyah_versions`

Fields:

* `id`
* `variant_id`
* `version_number`
* `schema_version`
* `status`
* `source_name`
* `source_reference`
* `approval_id`
* `checksum_sha256`
* `minimum_app_version_code`
* `published_at`
* `revoked_at`
* `created_at`

Statuses:

* `DRAFT`
* `IN_REVIEW`
* `APPROVED`
* `PUBLISHED`
* `REVOKED`

### `amaliyah_steps`

Fields:

* `id`
* `version_id`
* `position`
* `step_type`
* `title_id`
* `title_ar`
* `arabic_text`
* `translation_id`
* `instruction_id`
* `instruction_ar`
* `repeat_target`
* `quran_surah_number`
* `quran_ayah_start`
* `quran_ayah_end`
* `audio_group_id`
* `created_at`

### `approvals`

Fields:

* `id`
* `approver_name`
* `approver_role`
* `institution_name`
* `approval_date`
* `approval_scope`
* `document_storage_key`
* `public_document_storage_key`
* `document_reference_number`
* `status`
* `created_at`

### `content_assets`

Fields:

* `id`
* `asset_type`
* `storage_key`
* `checksum_sha256`
* `size_bytes`
* `mime_type`
* `language`
* `created_at`

### `feedback`

Fields:

* `id`
* `installation_id`
* `app_version`
* `amaliyah_id`
* `variant_id`
* `version_id`
* `step_id`
* `category`
* `description`
* `locale`
* `status`
* `created_at`

## 11.2 Android Room tables

Android mirrors the content hierarchy using Room entities and adds:

### `reading_sessions`

* `id`
* `version_id`
* `reader_mode`
* `advance_mode`
* `current_step_id`
* `scroll_index`
* `scroll_offset`
* `started_at`
* `last_opened_at`
* `completed_at`

### `step_progress`

* `session_id`
* `step_id`
* `current_count`
* `is_complete`
* `updated_at`

### `feedback_outbox`

* Local feedback payload.
* Submission status.
* Retry count.
* Last error.
* Created time.
* Last attempt time.

### `sync_metadata`

* Last successful sync.
* Manifest ETag.
* Manifest checksum.
* Content schema version.
* Last sync error.

User preferences remain in DataStore rather than Room.

---

# 12. Synchronisation Design

## 12.1 Source of truth

Room is the Android application’s canonical source of truth.

Screens and ViewModels must not render directly from network responses.

The network updates Room; the UI observes Room.

This follows official Android offline-first guidance, which recommends a local source as the source of truth and requires critical reads to remain available without network access.

## 12.2 Seed import

Approved production content packages live under an Android asset directory.

Suggested structure:

```text
app/src/main/assets/content/
├── manifest.json
├── tahlil-general-v1.json
└── istighosah-general-v1.json
```

On first launch:

1. Read seed manifest.
2. Validate schema.
3. Validate package checksum.
4. Import content transactionally.
5. Mark imported versions active.
6. Store seed manifest version.

Arabic content must never be hardcoded inside Kotlin source files.

## 12.3 Remote manifest

The backend exposes a lightweight content manifest containing:

* Schema version.
* Generation timestamp.
* Active content versions.
* Version checksums.
* Package URLs.
* Minimum application version.
* Revocation status.
* Optional asset package information.

The Android client sends `If-None-Match` when an ETag is available.

## 12.4 Package import

Downloaded packages are:

1. Downloaded to temporary storage.
2. Size checked.
3. Checksum verified.
4. Parsed.
5. Structurally validated.
6. Imported in one transaction.
7. Activated only after import success.
8. Deleted from temporary storage.

A malformed package must never partially replace local content.

## 12.5 Scheduling

Synchronisation runs:

* At application startup, without blocking UI.
* Through periodic WorkManager work.
* On manual refresh.
* After connectivity returns when pending work exists.

The client must use backoff and avoid repeated network loops.

---

# 13. Android Technical Architecture

## 13.1 Technology stack

Use:

* Kotlin.
* Jetpack Compose.
* Material 3.
* Navigation 3 stable release.
* Hilt.
* Kotlin coroutines.
* Flow and StateFlow.
* Room.
* DataStore.
* WorkManager.
* Retrofit.
* OkHttp.
* Kotlinx Serialization.
* AndroidX Lifecycle.
* AndroidX adaptive layout APIs.
* Gradle Kotlin DSL.
* Version catalog.
* JUnit.
* Compose UI testing.
* Android Lint.
* Detekt.
* KtLint or equivalent deterministic formatter.

Use the latest mutually compatible stable versions available when implementation begins.

Do not use alpha or beta dependencies when a stable supported alternative exists.

Navigation 3 is the preferred navigation system because it is designed for Compose, supports explicit back-stack ownership, and has stable releases available.

## 13.2 SDK configuration

* `minSdk = 26`
* `compileSdk = 36` or later stable equivalent.
* `targetSdk = 36`

Target API 36 avoids an immediate migration because new apps and updates submitted from 31 August 2026 must target Android 16/API 36 or higher.

## 13.3 Gradle modules

Release `0.0.1` uses one Android application Gradle module.

Do not create multiple Gradle feature modules yet.

Use package boundaries that can later be extracted into modules.

Suggested structure:

```text
com.sangusantri.app
├── app
│   ├── SanguSantriApplication
│   └── MainActivity
├── core
│   ├── common
│   ├── designsystem
│   ├── model
│   └── util
├── data
│   ├── local
│   │   ├── dao
│   │   ├── database
│   │   └── entity
│   ├── remote
│   │   ├── api
│   │   └── dto
│   ├── mapper
│   ├── repository
│   └── sync
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── feature
│   ├── home
│   ├── reader
│   ├── contentdetail
│   ├── settings
│   ├── feedback
│   └── about
├── navigation
└── di
```

## 13.4 Layer rules

### UI layer

The UI layer:

* Renders immutable UI state.
* Sends user actions.
* Contains presentation logic.
* Does not access DAOs or API services.
* Does not contain database entities or network DTOs.

Each major screen should use:

* `Route` composable for ViewModel collection and navigation.
* Stateless `Screen` composable.
* `UiState`.
* `UiAction`.
* `UiEffect` only for genuine one-time effects.

ViewModels expose StateFlow and follow unidirectional data flow. Official Android architecture guidance recommends repositories, clear data/UI boundaries, UDF, coroutines, and flows.

### Domain layer

The domain layer:

* Uses plain Kotlin.
* Contains repository contracts.
* Contains business models.
* Contains complex or reusable business rules.

Create a use case when:

* Logic combines multiple repositories.
* Logic is used by multiple ViewModels.
* Logic has meaningful business rules.
* Extracting it materially improves testing.

Do not create a pass-through use case merely to call one repository method.

### Data layer

The data layer:

* Owns repositories.
* Owns local and remote data sources.
* Resolves synchronisation.
* Maps DTOs and entities.
* Exposes domain models.
* Defines Room as the canonical source for content reads.

## 13.5 Model duplication rules

Separate models are required when boundaries differ:

* Network DTO.
* Room entity.
* Domain model.

Do not create an additional UI model when the domain model is already suitable for rendering.

Mappings must live at boundaries.

Do not map the same object repeatedly through unnecessary intermediate classes.

## 13.6 Compose rules

Claude must:

* Hoist screen state.
* Keep composables side-effect-safe.
* Use lifecycle-aware Flow collection.
* Use stable keys in lazy collections.
* Avoid passing ViewModels into child components.
* Pass state and callbacks instead.
* Keep business logic outside composables.
* Preserve reader state across configuration and process recreation.
* Add previews for reusable visual components.
* Use string resources.
* Use dimension and typography tokens.
* Support font scaling.
* Add semantics to counters and navigation controls.
* Avoid unnecessary recomposition.
* Avoid prematurely adding `@Stable` or `@Immutable`.
* Avoid storing domain state with plain `remember`.
* Avoid nested scroll structures that render the full document eagerly.
* Never use `GlobalScope`.
* Never use blocking work on the main thread.
* Never use `!!` without a documented invariant.

## 13.7 Edge-to-edge

Use `enableEdgeToEdge()` before `setContent`.

Insets must be applied through Material components, Scaffold padding, or one intentional inset strategy. Do not apply duplicate system-bar padding.

The relevant official Android edge-to-edge skill should be consulted during implementation.

## 13.8 Design system

Create reusable tokens for:

* Brand colours.
* Reader surfaces.
* Typography.
* Arabic typography.
* Spacing.
* Corners.
* Elevation.
* Icon sizes.

Visual direction:

* Primary green identity.
* Warm, calm reading surfaces.
* Traditional pesantren character.
* Modern Material 3 interaction.
* High readability.
* No excessive ornamental backgrounds behind Arabic text.
* No advertisements or promotional elements inside the reader.

Dynamic colour should be disabled by default so the SanguSantri identity remains consistent.

## 13.9 Arabic typography

The Arabic font must:

* Correctly render harakat.
* Correctly render Quranic marks used by approved content.
* Have a legally verified distribution licence.
* Remain readable at large sizes.
* Work in both themes.
* Be visually tested on multiple Android versions.

Do not download a font at runtime for core reading.

---

# 14. Backend Technical Architecture

## 14.1 Backend decision

Use:

* Go, latest stable version.
* PostgreSQL.
* Supabase-managed PostgreSQL for initial production.
* Supabase Storage for content packages, approval documents, and future audio.
* Supabase Studio as the temporary database interface.
* A custom Go API between Android and the database.
* A Go administration CLI for validation and publication.

Android must not connect directly to PostgreSQL or expose Supabase service credentials.

## 14.2 Go libraries

Preferred stack:

* `net/http`
* Chi router.
* `pgx`
* `sqlc`
* Goose or equivalent SQL migration tool.
* `log/slog`
* OpenAPI 3.1.
* Standard Go testing.
* Testcontainers or Docker PostgreSQL for integration tests.
* `golangci-lint`.

Avoid a heavy ORM.

SQL must remain visible, reviewable, and testable.

## 14.3 Project structure

```text
backend/
├── cmd
│   ├── api
│   └── admin
├── internal
│   ├── config
│   ├── content
│   ├── feedback
│   ├── httpapi
│   ├── storage
│   └── database
├── migrations
├── queries
├── openapi
├── testdata
├── Dockerfile
├── compose.yaml
├── go.mod
└── README.md
```

## 14.4 Go architecture rules

* Package names must describe business responsibilities.
* Interfaces should be declared by consumers.
* Do not introduce repository interfaces for every database query.
* Database access must accept `context.Context`.
* Every outbound operation must use a timeout.
* Database transactions must be explicit.
* Generated `sqlc` files must not be manually edited.
* Errors must retain their original cause.
* Public API errors must use consistent codes.
* Logs must be structured.
* Secrets must come from environment variables or a secret manager.
* No global mutable database client.
* No business logic in HTTP handlers.
* No HTTP-specific types inside core content logic.

## 14.5 Initial public endpoints

### `GET /healthz`

Returns service health.

### `GET /v1/config`

Returns:

* Supported content schema.
* Minimum supported application version.
* Feature flags.
* Maintenance state.

### `GET /v1/content/manifest`

Returns:

* Active content versions.
* Checksums.
* Download locations.
* Revocations.
* Minimum application version.
* ETag.

### `GET /v1/content/packages/{versionID}`

Returns or redirects to the immutable content package.

### `POST /v1/feedback`

Accepts anonymous correction feedback.

The endpoint must include:

* Body-size limits.
* Input validation.
* Basic rate limiting.
* Request identifier.
* Structured error response.

## 14.6 Admin CLI commands

The backend must provide commands equivalent to:

```text
content validate
content import
content review
content approve
content publish
content revoke
content list
content export
```

Publication must fail when:

* Approval is missing.
* Approval status is invalid.
* Arabic text is empty.
* Required translation is empty.
* Positions are duplicated.
* A repeat target is invalid.
* A Quran reference is incomplete.
* A checksum cannot be generated.
* The schema is unsupported.

Supabase Studio may edit draft data, but it must not be the mechanism that publishes content directly.

---

# 15. Content Editorial Workflow

## 15.1 Workflow

```text
Source selected
→ Manually transcribed
→ Structured content created
→ Automated validation
→ Internal review
→ Kyai/sesepuh review
→ Signed approval recorded
→ Immutable content version generated
→ Published
→ Bundled or synchronised
```

## 15.2 Correction workflow

```text
Feedback received
→ Triaged by content team
→ Compared with source
→ Reviewed by kyai/sesepuh when required
→ New version created
→ New approval attached
→ Published
→ Client automatically activates new version
```

## 15.3 Copyright and content rights

Public availability on a website must not be treated as automatic republication permission.

Before a monetised release, the product owner must verify:

* Permission to reproduce the source’s editorial arrangement.
* Permission to reproduce its Indonesian translation.
* Permission to distribute audio.
* Attribution requirements.
* Whether independent transcription or translation is required.

This is a release governance requirement, not a task Claude may resolve by assumption.

---

# 16. Non-Functional Requirements

## 16.1 Reliability

* Core reading must work when the API is unavailable.
* Failed synchronisation must not remove local content.
* Database migrations must be tested.
* Destructive Room migration is prohibited for production.
* Content imports must be transactional.
* User progress must survive application termination.

## 16.2 Performance

Targets:

* Serambi should render local content without waiting for network.
* Reader scrolling should remain smooth on a typical API 26 device.
* Long content must use lazy rendering.
* No full-document parsing should occur repeatedly during recomposition.
* Content package parsing must run outside the main thread.
* Large audio files must never be loaded fully into memory.
* Release builds must enable R8 resource and code shrinking.

## 16.3 Accessibility

* Controls must have content descriptions.
* Counter state must be announced by accessibility services.
* Touch targets must meet Material guidance.
* The interface must support large font scales.
* Colour must not be the only status indicator.
* RTL must be functionally tested.
* Arabic and translation text must remain selectable when practical.

## 16.4 Security

* HTTPS only.
* No database credentials in Android.
* No administrative endpoint accessible without strong authentication.
* Feedback input must be sanitised and size-limited.
* Approval documents must use controlled access.
* Content packages must be checksum verified.
* Server secrets must not enter Git.
* Debug logging must not expose content administration credentials.
* Release builds must disable verbose network logging.

Certificate pinning is not required for `0.0.1`.

## 16.5 Privacy

Release `0.0.1` does not require identity.

The application must not upload:

* Reading history.
* Counter history.
* Exact devotional frequency.
* Unfinished session data.
* Local preferences.

Feedback uploads only the minimum technical and content context needed to investigate the report.

A public privacy policy is required before Google Play publication.

## 16.6 Application resilience

The application must provide meaningful states for:

* Empty local catalogue.
* Seed import failure.
* Content package validation failure.
* Unsupported schema.
* Offline mode.
* Synchronisation failure.
* Revoked content.
* Feedback pending.
* Feedback submission failure.

No raw stack trace or backend error text may be shown to users.

---

# 17. Testing Strategy

Claude must inspect and follow the official Android testing skill when establishing the project testing strategy. The skill recommends testing business logic, preferring fakes, testing Room against SQLite, using Compose UI tests for Compose applications, and explicitly verifying state restoration.

## 17.1 Android unit tests

Required tests:

* Seed manifest comparison.
* Content checksum validation.
* Content version selection.
* Revoked-version fallback.
* Guided automatic advancement.
* Guided manual advancement.
* Counter increment.
* Counter reset.
* Completion eligibility.
* Reading progress restoration.
* Repository local-first behaviour.
* Sync failure retaining old content.
* Feedback outbox state transitions.
* Reader settings mapping.

## 17.2 Room tests

Use an in-memory Room database on Android instrumentation for:

* Seed import.
* Duplicate import.
* Transaction rollback.
* Version activation.
* Previous-version retention.
* Progress persistence.
* Database migration.

## 17.3 Compose UI tests

Required flows:

1. Open Serambi offline.
2. Open Tahlil.
3. Switch reader mode.
4. Increment repeated reading.
5. Complete a guided step.
6. Restore after Activity recreation.
7. Change Arabic font size.
8. Change theme.
9. Open source details.
10. Submit feedback offline.
11. Render Arabic RTL interface.
12. Render landscape layout.
13. Render tablet width.
14. Render font scale `1.5`.

Prefer semantic matchers. Use test tags only when semantic matching becomes unreasonable.

## 17.4 End-to-end tests

Maintain a small number of end-to-end journeys:

* Fresh install → Tahlil → guided reading → completion.
* Existing content → remote update → automatic activation.
* Offline feedback → network restoration → submission.
* Revoked latest version → fallback to previous approved version.

## 17.5 Backend tests

Required tests:

* Manifest response.
* ETag handling.
* Package retrieval.
* Invalid feedback payload.
* Rate limiting.
* Content validation.
* Approval enforcement.
* Publishing transaction.
* Revocation.
* Database migration.
* Storage failure.
* Context cancellation.

## 17.6 Quality commands

Android CI must run equivalent tasks:

```text
lint
detekt
format check
unit tests
Room instrumentation tests
Compose UI tests
assemble debug
assemble release
```

Backend CI must run:

```text
go test ./...
go vet ./...
golangci-lint run
migration validation
OpenAPI validation
Docker image build
```

Claude must not claim that a build or test passes unless the command was actually executed successfully.

---

# 18. CI/CD

Use GitHub Actions.

Pull request checks:

* Android static analysis.
* Android unit tests.
* Backend tests.
* Formatting.
* OpenAPI validation.
* Debug build.
* Content schema validation.
* No uncommitted generated files.

Release workflow:

1. Create version tag.
2. Build signed Android App Bundle using protected secrets.
3. Build backend Docker image.
4. Generate release notes.
5. Upload Android bundle to the selected Play testing track.
6. Deploy backend image.
7. Run health check.
8. Verify content manifest.
9. Promote only after smoke testing.

Production signing credentials must never be exposed to Claude output or committed.

---

# 19. Observability

Release `0.0.1` should collect only operational data required to maintain quality.

Recommended:

* Android crash reporting.
* Android vitals through Play Console.
* Backend structured logs.
* Request IDs.
* API latency.
* Error rates.
* Content sync success rate.
* Feedback submission success rate.

Do not record Arabic reading text, counter values, or personal devotional history in logs or analytics.

Telemetry credentials must be configurable so the debug application can build without production secrets.

---

# 20. Definition of Done for Release 0.0.1

Release `0.0.1` is complete only when:

* Tahlil content is fully entered.
* Istighosah content is fully entered.
* Both contain complete harakat.
* Both contain Indonesian translations by ayah or logical segment.
* Both have documented sources.
* Both have kyai or sesepuh approval.
* Approval documents are available.
* Content usage rights have been reviewed.
* No placeholder content remains in release.
* Fresh offline installation works.
* Full reader works.
* Guided reader works.
* Automatic progression works.
* Manual progression works.
* Counter progress survives process death.
* Reading position survives process death.
* Reader settings persist.
* Indonesian localisation is complete.
* Arabic localisation and RTL are complete.
* Portrait works.
* Landscape works.
* Tablet layout works.
* Synchronisation is non-blocking.
* Invalid downloads do not replace local content.
* Previous versions remain accessible.
* Feedback works offline and online.
* Android tests pass.
* Backend tests pass.
* A clean checkout builds successfully.
* Privacy policy exists.
* Store listing assets exist.
* Final logo and app icon exist.
* No critical or high-severity known defect remains.

---

# 21. Planned Release Roadmap

## `0.0.1` — Core Amaliyah Reader

* Tahlil.
* Istighosah.
* Full reader.
* Guided reader.
* Integrated repeated-reading counter.
* Offline content.
* Content synchronisation.
* Source and approval.
* Reader settings.
* Feedback.

## `0.0.2` — Standalone Tasbih

* Independent digital tasbih.
* Custom target.
* Unlimited mode.
* Haptic feedback.
* Persisted unfinished count.
* Reset confirmation.
* Preset common counts.

## `0.0.3` — Riwayat and Streak

* Daily amaliyah streak.
* Completion history.
* Amaliyah name.
* Version.
* Completion time.
* Duration.
* Private local statistics.
* No sharing yet.

## `0.0.4` — Pengingat Amaliyah

* Personal schedules.
* Tahlil malam Jumat preset.
* Istighosah weekly preset.
* Gregorian date.
* Hijri date.
* Notification permission flow.
* Rescheduling after reboot.
* No “remind me later” requirement.

## `0.0.5` — Downloadable Quran Audio

* Download complete audio packages.
* Multiple reciters supported by the data model.
* Download progress.
* Checksum verification.
* Remove downloaded package.
* Offline playback.
* Media3.
* No non-Quran prayer audio yet.

## `0.1.0` — Accounts

* Google login.
* Phone-number login.
* Minimal profile.
* No mandatory login for public content.

## `0.2.0` — Pesantren Membership

* Pesantren directory managed by SanguSantri.
* One active pesantren per user.
* Private pesantren code.
* Code rotation.
* Code hashing.
* Membership validation.
* Public users cannot enter pesantren community spaces.

## `0.3.0` — Private Pesantren Space

* Private amaliyah variants.
* Private schedules.
* Pesantren announcements.
* No chat.
* No public posting.

## `0.4.0` — Nahwu Quiz

* Question bank.
* Individual score.
* Pesantren representation.
* Anti-cheating controls.
* Seasonal leaderboard.
* Moderated content.

## `0.5.0` — Monetisation

* Advertising on non-reader surfaces.
* No advertisements between prayers or over Arabic text.
* Optional subscription.
* Ad-free experience.
* Public essential amaliyah remains accessible without payment.
* Pesantren private spaces remain free unless strategy changes explicitly.

---

# 22. Future Pesantren Rules

These rules are not implemented in `0.0.1`, but future design must account for them:

* A user may belong to only one active pesantren.
* Public users cannot access a pesantren community.
* Membership requires validation.
* The initial validation method is a private pesantren code.
* Codes must not be stored as plain text.
* Codes must be rotatable.
* Private amaliyah is visible only to validated members.
* Pesantren-specific content uses the same amaliyah/variant/version model.
* Public and private content must never be mixed by accidental caching.
* Membership revocation must remove future access to private content.
* Previously downloaded private content must be protected or removed after membership loss.

---

# 23. Claude Engineering Contract

Claude must follow these instructions when generating or editing code.

## 23.1 Before coding

Claude must:

1. Inspect the existing repository.
2. Search for existing classes before creating new ones.
3. Read the current Gradle files and version catalogue.
4. Inspect existing architecture and naming.
5. Consult relevant official Android documentation.
6. Consult relevant skills from the official `android/skills` repository.
7. Use stable dependencies.
8. Produce a concise implementation plan.
9. Identify files that will be created or changed.
10. Implement only the requested release or feature.

The Android Skills repository contains AI-oriented instructions grounded in official Android development guidance and must be used as a technical reference where relevant.

## 23.2 Android Skills usage

When Android CLI is available, install relevant skills into the project.

Potentially relevant skills include:

* Android CLI.
* Jetpack Compose styles.
* Navigation 3.
* Edge-to-edge.
* Testing setup.
* R8 analyser.
* Android profilers.

Do not install or apply an unrelated skill merely because it exists.

## 23.3 Code generation behaviour

Claude must:

* Modify the repository directly when tool access exists.
* Use patches rather than printing unchanged files.
* State the full path for every new file.
* Avoid repeating code already present.
* Avoid creating a second implementation beside an existing one.
* Refactor the existing implementation when necessary.
* Keep one canonical class for each responsibility.
* Keep one canonical content model.
* Keep one canonical theme system.
* Keep one canonical navigation state.
* Reuse existing components when behaviour and appearance are genuinely equivalent.
* Extract shared code only after a real duplication or stable common concept exists.
* Run formatting after modifications.
* Run relevant tests.
* Report commands executed and their actual results.
* Report unresolved failures honestly.

## 23.4 Prohibited patterns

Claude must not introduce:

* `BaseViewModel`.
* `BaseRepository`.
* Generic `BaseUseCase`.
* A generic application-wide `UiState`.
* God ViewModels.
* God repositories.
* God composables.
* Network calls from composables.
* DAO calls from ViewModels.
* Hardcoded Arabic religious content in Kotlin.
* Hardcoded user-facing strings.
* Hardcoded production URLs.
* Secrets in source control.
* `GlobalScope`.
* Destructive database migration.
* Silent exception swallowing.
* Duplicate mappers.
* Duplicate design tokens.
* Multiple competing navigation frameworks.
* Alpha dependencies without justification.
* Empty interfaces.
* Interfaces created only to satisfy a diagram.
* Comments that merely restate the code.
* Fake religious content presented as real content.
* Build-success claims without execution evidence.

## 23.5 Documentation to create

The repository should contain:

```text
README.md
AGENTS.md
docs/
├── prd.md
├── architecture.md
├── content-schema.md
├── content-workflow.md
├── testing.md
├── release-process.md
└── decisions/
```

`AGENTS.md` must summarise:

* Architecture boundaries.
* Naming conventions.
* Testing commands.
* Content safety rules.
* No-duplication rule.
* Current release scope.
* Links to detailed documents.

## 23.6 Decision records

Material technical decisions must be recorded as short ADRs, including:

* Go backend selection.
* Single Android Gradle module.
* Room as source of truth.
* Navigation 3.
* Immutable content versions.
* Supabase as managed infrastructure.
* No account in public MVP.
* No custom CMS in MVP.

## 23.7 Completion response

After each implementation task, Claude must provide:

1. What was implemented.
2. Files created.
3. Files modified.
4. Architecture decisions made.
5. Commands executed.
6. Test results.
7. Known limitations.
8. The next release item, without implementing it.

Claude must not paste every source file again after already writing it to the repository.

---

# 24. Initial Claude Implementation Request

Use the following as the first implementation instruction after providing this PRD to Claude:

> Implement SanguSantri release 0.0.1 from this PRD.
>
> Start by inspecting the repository. If it is empty, create the Android and Go backend projects.
>
> Create the complete technical foundation and working vertical slice, including:
>
> * Android project configuration.
> * Jetpack Compose application.
> * Material 3 design system.
> * Navigation 3.
> * Hilt.
> * Room.
> * DataStore.
> * WorkManager.
> * Retrofit and OkHttp.
> * Seed content import.
> * Serambi.
> * Tahlil and Istighosah catalogue entries.
> * Full reader.
> * Guided reader.
> * Automatic and manual progression.
> * Integrated counters.
> * Persisted reading progress.
> * Reader settings.
> * Source and approval detail.
> * Offline feedback outbox.
> * Content synchronisation architecture.
> * Indonesian and Arabic localisation.
> * RTL.
> * Landscape and tablet support.
> * Go API.
> * PostgreSQL migrations.
> * Content manifest endpoint.
> * Immutable content package endpoint.
> * Feedback endpoint.
> * Content administration CLI.
> * Android tests.
> * Go tests.
> * GitHub Actions.
> * Project documentation.
>
> Do not invent or scrape religious content. Create development fixture files that are visibly marked non-production, and make the release build fail content validation until approved production Tahlil and Istighosah packages are supplied.
>
> Use one Android Gradle application module with strict package boundaries. Do not prematurely modularise.
>
> Before adding any class, search the repository for an existing equivalent. Do not duplicate code, models, components, mappers, themes, navigation, repositories, or use cases.
>
> Use only stable dependency versions verified from official sources at implementation time.
>
> Run all relevant build, lint, formatting, and test commands. Do not claim success unless they actually pass.
>
> When complete, report only the implementation summary, changed files, commands, test results, risks, and next release. Do not repeat unchanged source files in the response.

---

# 25. Blocking Production Inputs

Engineering may begin immediately, but production publication is blocked until these assets exist:

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

Claude must use development-safe substitutes where possible, but must never disguise missing production inputs as completed work.
