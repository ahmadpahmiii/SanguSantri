# 0016: Standalone Al-Qur'an Kemenag with direct API access and local-first cache

## Status

Accepted (2026-08-08, product owner/tech lead); update policy amended
2026-08-09

## Context

SanguSantri previously prohibited a standalone Quran feature because no
trusted, authorised Quran source had been secured. The product owner has now
completed the official access process for the LPMQ Kementerian Agama Quran
API and approved a full text/translation/tafsir feature for release `0.0.6`,
after Nahwu Quiz `0.0.5`.

The supplied API contract provides list-surah, list-ayat-by-surah, and
tafsir-by-remote-ayat-ID endpoints. Ayat rows include official MSI Usmani
text, translation, Juz, and page metadata. The API exposes no dataset version
or change manifest. A supplied Surah 114 response was not in numeric ayat
order, so transport order cannot be treated as canonical religious-content
order.

API access requires a `username` and `token`. The product owner explicitly
chose direct Android access instead of adding a SanguSantri proxy, while
accepting that NDK/obfuscation can only raise reverse-engineering cost and
cannot make a client-shipped credential secret.

## Decision

1. **Release `0.0.6` adds the standalone Al-Qur'an Kemenag feature.** This
   supersedes the former no-standalone-Quran rule in the PRD, roadmap, and
   engineering instructions. Audio, Quran Foundation, copy/share, and Latin
   transliteration remain out of scope.
2. **Kemenag is the only Quran-content API.** Android consumes official
   read-only source fields without editorial mutation or secondary-source
   merging.
3. **The client calls Kemenag directly.** No Cloud Function, Cloud Run,
   Firebase mirror, or other SanguSantri proxy is introduced.
4. **The residual credential-extraction risk is accepted and documented.**
   Release secrets stay out of source control and are injected into generated
   native build input. Native C++ splitting/encoding, signing-certificate
   verification, symbol stripping, R8, HTTPS-only transport, and strict log
   redaction are defence-in-depth—not a claim of secrecy. A rotation requires
   a newly signed app release.
5. **Quran uses a dedicated authenticated network client.** Kemenag headers
   must never be attached to the existing Firebase Hosting content client or
   any non-Kemenag origin.
6. **Room is the Quran UI source of truth.** Network DTOs are validated and
   mapped; composables and ViewModels never render them directly or access the
   API/DAO themselves.
7. **Initial preparation is complete and atomic.** With no local dataset, the
   app fetches all 114 surahs, validates completeness/order, and commits once.
   Any failure writes no active partial Quran and retry begins again from the
   start; resumable staging is rejected as unnecessary complexity.
8. **Corpus updates are complete, atomic, and Remote Config version-gated.**
   Firebase Remote Config `quran_stable_version` starts at `1`; only a strictly
   higher value than Room's `quran_applied_stable_version` can enqueue a full
   update. There is no weekly/monthly fallback. The update is unique,
   unmetered, battery-not-low, and makes one complete attempt; failure preserves
   the old dataset and applies a 24-hour cooldown to that target. The applied
   version is committed in the same transaction as the validated corpus.
9. **Tafsir is on-demand and cached.** A cached tafsir is local-first and
   stale-while-revalidate after seven days.
10. **The Quran model is separate from amaliyah content.** Dedicated surah,
    verse, tafsir, bookmark, reading-state, and reading-session tables are a
    justified bounded context; forcing 6,236 ayat through `ContentStep` would
    couple unrelated publication/version/progress semantics.
11. **The existing shell remains unchanged.** Al-Qur'an is reached from
    Beranda, hides bottom navigation while active, and uses the existing
    Navigation 3 stack. No second Activity or navigation system is introduced.
12. **Every Quran surface is dark-only.** It uses feature colour roles within
    the existing design system and restores the previous app theme/brightness
    on exit.
13. **Design is portrait-primary, not orientation-locked.** Rotation must
    remain functional and preserve state.
14. **Quran sessions contribute to the combined amalan streak** only after
    position advances by at least one ayat.

## Alternatives rejected

* **Continue prohibiting standalone Quran** — rejected because the original
  source-validity blocker is resolved by official LPMQ access.
* **SanguSantri server proxy** — security-preferred but rejected by the
  product owner for this release; it would add an operational service and
  change the chosen direct-consumption model.
* **Hardcode the plain token in Kotlin/XML/BuildConfig or commit it in C++** —
  rejected; it leaks source credentials and provides less resistance than
  generated native build input. The token still remains extractable from a
  release APK, an accepted limitation.
* **Firebase Hosting snapshot of the Quran dataset** — rejected because the
  granted API is to be consumed directly and the product owner does not want
  a redistributed SanguSantri copy served outside the app.
* **Bundled full Quran baseline** — rejected for `0.0.6`; a fresh install needs
  one successful online preparation. Offline-first begins after that commit.
* **Partial/resumable initial activation** — rejected to keep implementation
  simple and prevent an incomplete Quran appearing complete.
* **Reuse the amaliyah content-package schema** — rejected because its
  versioned step/repetition model does not represent Surah/Juz/page/tafsir.
* **Trust API array order** — rejected based on the supplied out-of-order
  response; canonical numeric ordering and completeness validation are
  mandatory.
* **Treat `halaman` as exact printed layout** — rejected; it supports grouping
  but not official line composition.
* **Font switching with arbitrary Quran datasets** — rejected; selectable
  fonts render the same Kemenag text and must pass compatibility checks.
* **Weekly or monthly defensive full refresh** — rejected because the Kemenag
  corpus has no cheap change manifest and a no-change refresh still repeats the
  full list-plus-114-surah request set per installation.
* **Treat Remote Config as upstream proof or rollback storage** — rejected;
  `quran_stable_version` is only SanguSantri's monotonic fetch trigger. Kemenag
  remains the content source and exposes no historical snapshot by that number.

## Consequences

* `docs/product/QURAN_PRD.md` owns complete feature requirements and
  acceptance criteria; `docs/design/QURAN_DESIGN_SYSTEM.md` owns its visual
  language.
* Existing statements that no standalone Quran/Kemenag integration is planned
  must be updated, while historical progress entries remain unchanged.
* Release `0.0.6` introduces the first production API credential shipped in
  the client and therefore a deliberately accepted extraction/rotation risk.
* Initial first-use availability is weaker than bundled amaliyah: without
  internet and without prior preparation, Quran shows an error; after one
  successful preparation, reading is local-first.
* After first preparation, an installation makes no further full-corpus Kemenag
  request until operations publishes a higher `quran_stable_version`. Remote
  Config must be increased only after the intended Kemenag data is fully live
  and verified; publishing it early can cause clients to stamp the target after
  fetching the previous live API state.
* A successful corpus replacement clears cached tafsir because it is keyed by
  Kemenag remote ayat id; bookmarks and last-read state remain keyed by stable
  `(surah, ayat)` identity and survive.
* Font binaries remain design inputs until licence and glyph gates pass.
* No Kotlin, Room, NDK, design tooling, or production feature implementation
  is part of this documentation decision itself.

## Amendment (2026-08-09): in-memory per-surah retry

Decision #7 said "retry begins again from the start; resumable staging is
rejected as unnecessary complexity," and "Alternatives rejected" rejected
"Partial/resumable initial activation." In practice a single transient
per-surah failure (a dropped/incomplete connection such as `unexpected end
of stream` on one of the 114 `ayat` requests) forced re-downloading the
entire corpus on every "coba lagi" tap, which is wasteful and, on a weak
connection, can make an attempt never converge. This amendment narrows
"resumable staging" to mean *durable, persisted* staging — the thing that
was actually rejected — and permits a lighter mechanism that does not
reopen it:

* `QuranSyncManager` transparently retries a single failed request (up to 3
  attempts, short backoff) before ever surfacing a failure, absorbing the
  common one-off blip without any retry-button involvement at all.
* Successfully-fetched surahs are additionally kept in an in-memory,
  process-lifetime cache. A subsequent `sync()` call — whether a manual
  "coba lagi" or an automatic retry — only re-fetches surahs missing from
  that cache instead of all 114.
* This cache is discarded the instant the target `stableVersion` changes,
  on a successful commit, and on any permanent (non-retryable) failure. It
  never survives process death — an app kill still falls back to the
  original full-restart behaviour — and it is never read by Room or any
  other component: the commit remains the same single atomic
  `withTransaction` replace, gated on the complete, validated 114-surah set.

What decision #7 actually guarantees is unchanged: no partial Quran is ever
written to Room or exposed to the UI, and there is no cross-process-death
resumable staging (no new Room tables, no WorkManager-persisted progress).
Only the in-memory fetch phase of a single sync attempt became
retry-aware.
