# Brief — CMS "Quotes / Ayat Hari Ini"

**Untuk dikerjakan di sesi terpisah, di repositori `../cms`.**

Ganti dari rencana sebelumnya: CMS tidak lagi hanya menjadwalkan rujukan ayat. Admin mengetik
kutipan bebas — Arab dan terjemahan — sehingga isinya bisa ayat Al-Qur'an sekarang dan hadis nanti.
CMS menyimpan banyak kutipan, lalu API memberi satu kutipan per tanggal.

---

## 1. Bentuknya

**Kumpulan kutipan + penjadwalan otomatis.** Admin mengisi kolam kutipan; sistem yang membagikannya
ke tanggal. Admin tidak perlu memilih satu per satu setiap hari, tetapi tetap bisa mengunci kutipan
tertentu ke tanggal tertentu (misalnya menjelang Ramadan).

Ini yang membuat "acak" tetap terkendali: keacakannya ada di *pembagian tanggal*, bukan di *isi*.
Semua yang bisa muncul sudah lolos meja editor.

## 2. Dua hal yang harus dipikirkan sebelum mulai

**a. Teks Al-Qur'an yang diketik tangan.** Aplikasi Android punya dataset LPMQ Kemenag lengkap, dan
ADR 0016 §2 menyatakan Kemenag satu-satunya sumber teks Al-Qur'an. Kalau admin mengetik sendiri
Arab sebuah ayat, satu harakat salah ketik akan tampil sebagai Al-Qur'an. Keputusan pemilik produk
adalah menerima ini demi keluwesan; konsekuensinya **ADR 0016 perlu diamandemen**, dan alur editor
wajib punya langkah verifikasi (§5).

**b. Hadis butuh sanad dan derajat.** Hadis tanpa perawi dan status (sahih/hasan/daif) masuk
kategori konten berisiko tinggi menurut `docs/operations/CONTENT_GOVERNANCE.md` dan memerlukan
tinjauan ustaz/kyai sebelum dipublikasikan. Karena itu `source_label` **wajib**, bukan opsional.

## 3. Tabel (Supabase)

```sql
create table quotes (
  id              uuid primary key default gen_random_uuid(),
  kind            text not null check (kind in ('quran','hadith','other')),
  arabic          text,
  translation_id  text not null,
  translation_en  text,
  source_label    text not null,
  source_note     text,
  theme           text,
  is_published    boolean not null default false,
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now()
);

create table quote_schedule (
  date       date primary key,
  quote_id   uuid not null references quotes(id),
  is_pinned  boolean not null default false,
  created_at timestamptz not null default now()
);
```

| Kolom            | Catatan                                                                                                    |
|------------------|------------------------------------------------------------------------------------------------------------|
| `kind`           | `quran` / `hadith` / `other`. Aplikasi memakainya hanya untuk penataan, bukan klaim.                       |
| `arabic`         | Boleh kosong — kutipan `other` mungkin tidak berbahasa Arab.                                               |
| `translation_id` | Wajib. Bahasa Indonesia adalah bahasa dasar aplikasi.                                                      |
| `translation_en` | Opsional. Aplikasi memakai ini kalau bahasa perangkat Inggris, dan jatuh ke `translation_id` kalau kosong. |
| `source_label`   | **Wajib.** "QS. Al-Jumu'ah : 1", "HR. Bukhari no. 6114". Yang tampil sebagai rujukan di kartu.             |
| `source_note`    | Derajat hadis, penerbit, atau catatan editor.                                                              |
| `is_published`   | Hanya baris `true` yang boleh masuk penjadwalan.                                                           |

`quote_schedule.date` sebagai primary key menjamin satu tanggal satu kutipan. `is_pinned` menandai
tanggal yang dipilih admin sendiri, supaya penjadwal otomatis tidak menimpanya.

## 4. Endpoint

```
GET /api/v1/ayat-hari-ini
```

Read-only, tanpa autentikasi, seperti route Content API lain.

```json
{
  "schemaVersion": 2,
  "items": [
    {
      "date": "2026-08-22",
      "kind": "quran",
      "arabic": "يُسَبِّحُ لِلّٰهِ مَا فِى السَّمٰوٰتِ ...",
      "translation": { "id": "Apa yang ada di langit ...", "en": "Whatever is in the heavens ..." },
      "sourceLabel": "QS. Al-Jumu'ah : 1",
      "sourceNote": null,
      "theme": "Tasbih"
    }
  ]
}
```

**`schemaVersion` naik ke 2.** Bentuknya tidak kompatibel dengan versi 1 yang hanya mengirim
`surah`/`ayat`. Aplikasi menolak versi yang tidak dikenal dan mempertahankan cache lama, jadi
pergantian ini aman selama aplikasi diperbarui bersamaan.

**Kirim jendela `hari ini + 90 hari`,** bukan hanya hari ini. Aplikasi offline-first: kalau hanya
hari ini yang dikirim, layar kosong begitu tanggal berganti tanpa jaringan.

Tanggal berformat `YYYY-MM-DD` polos tanpa zona waktu — dibandingkan dengan tanggal lokal perangkat.

## 5. Penjadwal otomatis

Sebuah job (cron harian atau fungsi yang dipanggil saat endpoint diakses) mengisi `quote_schedule`
sampai 90 hari ke depan:

1. Ambil tanggal yang belum terisi.
2. Untuk tiap tanggal, pilih acak dari `quotes` yang `is_published = true`.
3. **Hindari pengulangan dekat** — jangan pakai kutipan yang muncul dalam 60 hari terakhir, kecuali
   kolamnya memang lebih kecil dari itu.
4. Jangan pernah menimpa baris `is_pinned = true`.

Kalau kolam kutipan kosong, jangan mengisi apa pun — endpoint mengirim `items` kosong dan aplikasi
menampilkan entri terakhir yang masih ter-cache.

## 6. Layar admin

- **Daftar kutipan** — cari dan saring per `kind`, `theme`, dan status publikasi.
- **Form kutipan:** pilih `kind`, textarea Arab (RTL, font Arab yang terbaca), terjemahan Indonesia,
  terjemahan Inggris opsional, `source_label`, `source_note`, `theme`, toggle publish.
- **Langkah verifikasi sebelum publish.** Untuk `kind = 'quran'`, tampilkan teks resmi Kemenag
  berdampingan dengan yang diketik admin supaya selisihnya kelihatan sebelum disimpan. Ini yang
  menahan risiko di §2a. Untuk `kind = 'hadith'`, wajibkan `source_label` terisi dan tampilkan
  pengingat bahwa hadis perlu tinjauan yang berwenang.
- **Kalender jadwal** — satu sel per tanggal berisi kutipan yang akan tampil, dengan tombol
  mengunci/mengganti kutipan pada tanggal tertentu.
- **Peringatan kolam menipis** — kalau jumlah kutipan terpublikasi kurang dari 60, tampilkan
  peringatan: kolam sekecil itu membuat pengulangan terasa cepat.

## 7. Perubahan di aplikasi Android

Sisi Android saat ini dibangun untuk kontrak versi 1 (hanya rujukan) dan **harus diperbarui**:

- `AyatHariIniEntity` menyimpan teks, bukan `surahNumber`/`ayatNumber`.
- `AyatHariIniRepositoryImpl` berhenti menggabungkan dengan `quran_verses`.
- `AyatHariIni` (model domain) mengganti `surahName`/`ayatNumber` dengan `sourceLabel`, dan
  menambah pemilihan bahasa terjemahan.
- Kartu bagikan dan kepala Beranda menampilkan `sourceLabel` alih-alih "QS. X : Y" yang dirakit
  sendiri.
- `FixtureAyatHariIniRemoteSource` dihapus, binding diganti ke `ApiAyatHariIniRemoteSource`.

Rincian yang masih berlaku ada di `docs/product/AYAT_HARI_INI.md`.
