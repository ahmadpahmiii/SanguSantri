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
part of their original reading text (`QURAN_AYAH` step type); those verses
are entered as part of the approved amaliyah content itself, never fetched
from a separate Quran API or service — see
`docs/engineering/CONTENT_MODEL.md`.

## `0.0.1` — Core Amaliyah Reader (current)

* Tahlil (59 steps), Istighosah (27 steps) — fixed local release-candidate
  content, bundled offline in both debug and release builds.
* Full reader, guided reader, with an in-reader action to switch between
  them without losing progress.
* Integrated repeated-reading counter.
* Offline content only — no remote content synchronisation in this release.
* Compact `Approved by` status, reader settings.

Content correction is an internal SanguSantri-team operation, not a
user-facing feature (`docs/operations/CONTENT_GOVERNANCE.md`); there is no
public feedback form, feedback outbox, or feedback endpoint in `0.0.1` or
currently planned for any future version. Remote content synchronisation
and a Go + PostgreSQL backend remain an unscheduled future item, not a
committed roadmap version — they are not part of `0.0.1` and do not appear
below until a real product decision schedules them.

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
