# Murottal per ayat & unduhan audio — implementation progress

Feature-specific progress doc (kept out of `docs/PROGRESS.md`, which carries only a
pointer). Design source: the turn-4 addendum (`4a`–`4f`) of the Beranda/Al-Qur'an
revamp handoff. Decision of record: the 2026-08-17 amendment to ADR
`docs/decisions/0018-myquran-for-prayer-times-and-qibla.md`.

## Turn 4 — murottal per ayat + per-surah audio download (2026-08-17)

**Status:** Implemented and verified on a booted emulator (Pixel_9, API 36).
Product-owner decision recorded as the 2026-08-17 amendment to ADR
`docs/decisions/0018-myquran-for-prayer-times-and-qibla.md`, which reverses that
ADR's own "Quran audio — deferred" note. **`ROADMAP.md`'s and `CLAUDE.md`'s
"no Quran audio" statements are now out of date.**

myquran (`cdn.myquran.com`) is the murottal source, **audio bytes only** — Kemenag
remains the sole Quran *content* API (ADR 0016 §2). Per-ayah and per-surah URLs are
computed arithmetically from `(surah, ayah)`, so this needed **no API call, no DTO,
no sync change, and no Room version bump** — nothing was wiped by this work.

### What shipped

* **Tap an ayah number → that ayah plays, then playback auto-continues** through the
  surah, and across surah boundaries while "Lanjut otomatis antarsurah" is on.
* Reader while playing (`4a`): tint on the active ayah, its number chip filled
  primary, `graphic_eq` + "Sedang diputar", a 2dp position line *inside* the ayah
  between the Arabic and the translation, "Berikutnya" on the next ayah. No progress
  bar above the player bar (removed in design review).
* Loading (`4e`): the number chip becomes a spinner with "Menyiapkan audio…" and the
  player bar shows "Mengunduh · disimpan offline" with "Batal". Cached ayat skip it.
* Mini player bar: 42dp play/pause, label, `skip_previous`/`skip_next`/`close`;
  tapping the label opens the murottal panel.
* Ayah action sheet (`4b`): `playlist_play` "Putar dari ayat ini" as a tint block,
  then "Putar ayat ini saja" and "Ulangi ayat ini" with a 3× chip, above the existing
  bookmark/tafsir/last-read/position rows.
* Murottal panel (`4c`): qari, speed 0,75×/1×/1,25×, "Lanjut otomatis antarsurah",
  "Layar tetap menyala", the audio-download block with progress and "Batalkan", and
  the queue line.
* Hub (`4d`): "SEDANG DIPUTAR" replaces "Terakhir dibaca" while audio runs; per-surah
  audio state in the trailing slot — `headphones` + "Unduh" pill, `downloading` + 2dp track, or a
  filled
  `check_circle`. Never a bare download icon (that reads as "download the text").
* Mushaf mode (`4f`): the strip's right side becomes `graphic_eq` "MENGIKUTI AUDIO",
  the recited ayah is highlighted inline in the flowing paragraph, and the page
  auto-scrolls to follow it.
* **Following continues across surah boundaries.** When auto-continue carries playback
  from the end of one surah into the next, the open reader follows it there — the
  addendum's frames only cover following *within* a surah, and stopping at the boundary
  left the page stranded on a surah that had finished while the player bar named a
  different one. See "Following across a surah boundary" below.
* Playback survives leaving the reader: `QuranMurottalService` is a
  `MediaSessionService` publishing the app's single `ExoPlayer`, promoted to a
  foreground media-playback service with a transport notification.

### Deliberate deviations from the addendum, and why

* **Audio is stored as files, not in Room** (`filesDir/murottal/`, library derived by
  directory listing). Room BLOBs would bloat the database and — because the standing
  policy is `fallbackToDestructiveMigration(dropAllTables = true)` — would be deleted
  along with the corpus on the next schema bump. Full reasoning in the ADR amendment.
* **The qari row is a static value, not a picker.** The service publishes one
  recitation and documents no reciter; the displayed name is the product owner's
  attribution, held in `QuranAudioSource.RECITER_NAME`.
* **A partially downloaded surah reports "Unduh", not "tersimpan"** — it must not
  advertise itself as offline-ready.
* **A fully stored surah shows a filled primary `play_arrow` "Putar" pill, not the
  design's bare `check_circle`.** Product-owner request: the check said "stored" but
  could not be acted on, so starting a recitation meant opening the reader first.
  "Putar" plays from ayat 1 in place, and still carries the stored meaning because it
  only appears once every ayah is present. Filled against the download state's
  outlined pill, so a glance down the list separates "ready to play" from "needs
  downloading"; the stored fact stays explicit for screen readers.
* **The "Sedang diputar" meta line names the next surah instead of counting a
  queue.** The frame reads "Alafasy · antrean 3 surah"; a count cannot be honest,
  because with "Lanjut otomatis antarsurah" on playback runs to the end of the
  mushaf, and the "3" only ever reflected the player's two-surah look-ahead. It now
  reads "<qari> · lanjut ke <surah>", or just the qari at the end of the mushaf or
  when continuation is off. The panel's "Antrean: A → B → C" line is unchanged and is
  explicitly a look-ahead, not a total.
* **The downloaded-audio line lives in Tampilan Al-Qur'an, not on the hub**
  (2026-08-17, product owner): "AUDIO TERUNDUH · N SURAH · X MB" with "Penyimpanan"
  sits between "Kecerahan Quran" and "Sumber Al-Qur'an" in the settings screen, and
  the storage sheet opens from there. The hub keeps only the per-surah download/play
  controls, which is where a reader acts on one surah rather than on the library.
* **The line's trailing link is "Penyimpanan", not the design's "Kelola", and it
  opens a sheet instead of deleting.** First implementation followed the frame
  literally: "Kelola" deleted the entire library on one tap, with no warning and no
  way back. Two faults — a label promising management that only destroyed, and an
  irreversible bulk delete behind a single tap. It now opens a storage sheet stating
  what is stored, that Quran text/translation/tafsir are unaffected and stay readable
  offline, and a destructive action labelled with the amount
  ("Hapus semua audio (12,4 MB)"). Deletion is still whole-library: a per-surah
  management list is not in the turn-4 frames.
* The media service is **not exported** (see the ADR amendment).

### Commands executed

`ktlintFormat`, `ktlintCheck`, `detekt`, `:app:lint`, `:app:assembleDebug`,
`:app:installDebug`, plus manual on-device verification.

* `detekt`, `lint`, `assembleDebug`, `installDebug`: **pass**.
* `ktlintCheck`: **fails, pre-existing and not caused by this work.** It reports
  ~3,261 `standard:indent` violations across 51 files that this change never touches
  (e.g. `AppThemeViewModel.kt`, `NahwuQuizBootstrapper.kt`) — the repo's committed
  constructor-injection style disagrees with the configured ktlint version.
  `ktlintFormat` "fixes" it only by reformatting the whole repository, which was
  reverted rather than smuggled into this diff. New code follows the surrounding
  committed style; zero non-indent violations are attributable to it.
* No unit/instrumented tests were added or run, per `CLAUDE.md`'s temporary
  implementation-pass constraints.

### Manually verified on the emulator

Corpus download → hub → reader; tapping ayah 1's number chip downloaded
`001001.mp3` and **prefetched** `001002.mp3`; auto-continue ran to ayah 7 and then
crossed into Al-Baqarah; `dumpsys media_session` showed `state=PLAYING` with
metadata "Al-Fātiḥah : 7, Syaikh Misyari Rasyid Al-'Afasi"; after HOME the service
reported `isForeground=true types=0x00000002` with a `category=transport`
notification and playback continued; the action sheet, murottal panel, and hub
"Sedang diputar" block all rendered as specified.

**One bug found and fixed during that verification:** the panel's queue line joined
the *reader's* surah onto the *player's* queue, so it read
"Al-Fātiḥah → Āli 'Imrān → An-Nisā'" and skipped whatever was actually playing. The
queue is now built entirely from player state and reads
"Al-Fātiḥah → Al-Baqarah → Āli 'Imrān".

### Follow-scrolling is measured, not item-based

The first implementation scrolled with `animateScrollToItem(index)`, which was too coarse in both
modes:

* **Mushaf mode**: one list item is a whole *page* of flowing text. At a large Arabic size that page
  is several screens tall, so every ayah after the first resolved to the same item index and nothing
  scrolled — the recitation simply ran off the bottom. The bigger the font, the worse it got.
* **Translation mode**: each ayah was pinned flush against the top edge, with no preceding context.

The target is now computed in pixels: the item's own viewport offset, plus the ayah's offset
*inside*
a measured page, minus a lead of `FOLLOW_LEAD_FRACTION` (0.2) of the viewport height so the ayah
lands
a little below the top. `QuranFlowingPageText` reports the ayah's vertical position from its real
`TextLayoutResult`, keyed off the character-range annotation it already maintains for long-press
hit-testing — so no second index of positions has to be kept in step with the text. Because both
inputs are measured rather than assumed, the result adapts to Arabic size, line spacing and screen
height without being told any of them, and re-runs when the reader changes the size mid-recitation.

It also holds still when it can: an ayah already sitting between `FOLLOW_COMFORT_TOP_FRACTION` and
`FOLLOW_COMFORT_BOTTOM_FRACTION` of the viewport is left alone, so short ayat do not drag the page
every few seconds.

**Bug fixed after first release of this behaviour — overshoot on a page change.** The measured
offset
was held as a bare `Float?` updated by whichever page reported one, guarded by `if (offset != null)`
so
it would not be cleared. Crossing into the next page therefore applied the *previous* ayah's
offset —
measured deep inside the page just left — to the new page, scrolling clean past its opening ayah
(reported at default size: playing from Al-Baqarah ayat 16, the first ayah of page 4 ended up hidden
above the top edge). Two changes:

* The measurement now travels with the ayah it belongs to (`QuranMeasuredAyatOffset`) and is only
  applied when that ayah is the one playing. A page that does not hold the recited ayah stays silent
  rather than reporting `null`, so composed pages cannot overwrite each other.
* The follow effect keys on the offset's *value*, not its presence. Crossing onto a page that has
  not
  been composed yet now scrolls to that page's top first and re-runs to refine once the page reports
  a
  real measurement — previously the coarse first pass was also the last.

Re-verified by letting a recitation run unattended across several page boundaries at the default
Arabic size: at the 24→25 crossing the "JUZ 1 · HAL 5" strip and ayat 25, page 5's opening ayah,
both
landed fully visible instead of being scrolled past.

Verified on the emulator at **48sp** (near the 52sp maximum): in Arab + terjemahan, ayat advanced
with
the active ayah scrolled into view and context retained above it; switching to Arab saja
mid-playback,
Al-Baqarah ayat 6 on page 3 — a page far taller than the viewport — was highlighted inline and
scrolled into view with a lead above it, which the previous item-level scroll could not do at all.

### Following across a surah boundary

The reader loads one surah at a time (`QuranReaderViewModel` takes its surah by assisted
injection), so following the recitation into the next surah means re-opening the reader
on that surah rather than teaching one screen to hold two. `QuranReaderRoute` therefore
raises `onFollowAudioToSurah`, and the nav host answers it with
`TopLevelBackStack.replaceLast` — not `add`, so an hour of continuous listening cannot
leave a surah-deep back stack to walk out of.

The guard that makes this safe is small but load-bearing: the effect only navigates when
*this* reader's surah was the one being recited and playback then moved elsewhere. Opening
Al-Kahfi by hand while Al-Baqarah plays never sets that flag, so the reader is never
yanked to whatever happens to be playing. No setting gates it — crossing surahs only
happens while "Lanjut otomatis antarsurah" is on, which is already a request for
continuous recitation.

Verified on the emulator in dark mushaf mode: long-pressed Al-Fātiḥah ayat 7 → "Putar dari
ayat ini" → the ayah highlighted inline under a "MENGIKUTI AUDIO" strip → at its end the
reader retitled itself **Al-Baqarah**, kept the strip, and went on highlighting and
auto-scrolling ayah by ayah (reached ayat 11 unattended).

**Also fixed while verifying this:** the player bar said "Akhir surah" on a surah's last
ayah even when cross-surah continuation was about to carry on into the next one. It now
reads "Lanjut ke <surah>" whenever a next surah is queued, and keeps "Akhir surah" only
when nothing actually follows.

### Concurrency: how partial downloads and playback interleave

Nothing is ever concatenated. Each ayah is an independent file played as its own
`MediaItem`, one at a time, so a surah that is half-downloaded needs no "combining" —
`downloadAyah` returns immediately for an ayah already on disk, and fetches only the
ones that are missing. Playing ayat 5 fetches ayat 5 alone; it does not walk 1–4.
Auto-continue then advances one ayah at a time, skipping the fetch wherever a file is
already present.

**A real race existed here and was fixed.** Three callers can want the same ayah at
once: the surah-download loop walking 1..N, the player fetching the ayah it is about
to play, and the player prefetching the next one. Two of them would open writers on
the same `.part` path and interleave bytes into a corrupt file, and whichever renamed
second could report a spurious failure for an ayah that had in fact arrived.
`QuranAudioDownloader` now keeps a `ConcurrentHashMap` of in-flight downloads keyed by
positional file name: concurrent callers await one shared download instead of starting
their own. The shared work runs in the downloader's own scope, so one caller giving up
does not abort a fetch another is still waiting on.

Two properties make partial state safe to reason about:

* A file only ever carries its real name once fully written (`.part` + rename), so
  presence always means playable.
* `.part` files are excluded by `QuranAudioSource.parseFileName`, so an in-flight
  download is never counted as stored.

Verified on the emulator by starting Al-Baqarah's download while repeatedly tapping an
ayah chip in that same surah: no leftover `.part` files, no zero-byte files, no
spurious error state, no crash.

### Known limitations

* Downloads are not resumable — the CDN ignores range requests and sends no
  `Accept-Ranges`, so a cancelled ayah restarts (≤2.3 MB each).
* One surah download runs at a time, matching the design's single-progress block.
* The mushaf follow-scroll re-centres on each ayah boundary, so a manual scroll is
  respected only until the next ayah starts — which is the specified behaviour.
* Playback needs the corpus present, since ayat counts and surah names come from Room.
