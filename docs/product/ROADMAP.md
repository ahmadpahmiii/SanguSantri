# SanguSantri Release Roadmap

Product-level roadmap only. Engineering milestone tracking (what has actually
shipped) lives in [`docs/PROGRESS.md`](../PROGRESS.md), not here. Do not
implement an item on this roadmap until it is explicitly requested — PRD §1
already states future roadmap items must influence extensibility without
being built prematurely.

## `0.0.1` — Core Amaliyah Reader (current)

* Tahlil, Istighosah.
* Full reader, guided reader.
* Integrated repeated-reading counter.
* Offline content, content synchronisation.
* Source and approval, reader settings, feedback.

See `docs/product/PRD.md` for full scope and acceptance criteria.

## `0.0.2` — Standalone Tasbih

* Independent digital tasbih, custom target, unlimited mode.
* Haptic feedback, persisted unfinished count, reset confirmation.
* Preset common counts.

## `0.0.3` — Riwayat and Streak

* Daily amaliyah streak, completion history.
* Amaliyah name, version, completion time, duration.
* Private local statistics only — no sharing yet.

## `0.0.4` — Pengingat Amaliyah

* Personal schedules; Tahlil malam Jumat and Istighosah weekly presets.
* Gregorian and Hijri date, notification permission flow.
* Rescheduling after reboot. No "remind me later" requirement.

## `0.0.5` — Downloadable Quran Audio

* Downloadable complete audio packages, multiple reciters in the data model.
* Download progress, checksum verification, package removal, offline playback.
* Media3. No non-Quran prayer audio yet.

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

## `0.4.0` — Nahwu Quiz

* Question bank, individual score, pesantren representation.
* Anti-cheating controls, seasonal leaderboard, moderated content.

## `0.5.0` — Monetisation

* Advertising on non-reader surfaces only — never between prayers or over
  Arabic text.
* Optional subscription, ad-free experience.
* Public essential amaliyah stays accessible without payment; pesantren
  private spaces stay free unless strategy explicitly changes.

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
