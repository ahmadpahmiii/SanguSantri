# Ayat Hari Ini — sumber, kontrol editorial, dan sinkronisasi

Kutipan harian yang tampil di kepala Beranda dan di widget "Jadwal Sholat + Ayat Hari Ini".

## 1. Siapa yang memilih kutipannya

**Manusia, lewat CMS — tetapi tidak per hari.** Editor mengisi *kolam* kutipan; sistem yang
membagikannya ke tanggal secara acak. Editor tidak perlu memilih satu per satu setiap pagi, tetapi
tetap bisa mengunci kutipan tertentu ke tanggal tertentu (misalnya menjelang Ramadan).

Itulah yang membuat "acak" tetap terkendali: keacakannya ada di *pembagian tanggal*, bukan di *isi*.
Semua yang bisa muncul sudah lolos meja editor.

### Yang berubah dan kenapa

Versi pertama fitur ini memilih ayat sendiri di dalam aplikasi:
`Random(date.toEpochDay()).nextInt(6236)` — satu ayat acak dari seluruh mushaf, deterministik per
tanggal. Pendekatan itu **dibatalkan oleh pemilik produk** karena tidak bisa dikendalikan: yang
muncul besok tidak diketahui siapa pun sampai besok tiba. Audit kalender lengkapnya memperjelas
masalahnya: karena kolamnya seluruh mushaf, jadwal berisi ayat tentang Jahanam dan hukum-hukum
tertentu di bawah judul yang dibaca sebagai penyemangat harian. Menyaring itu berarti melakukan
**seleksi editorial atas teks agama**, dan itu harus dilakukan oleh orang yang berwenang, bukan
oleh kode.

Versi kedua memindahkan keputusannya ke CMS, tetapi CMS hanya mengirim **rujukan** —
`(surah, ayat)` — dan aplikasi mengambil teksnya dari dataset LPMQ Kemenag di Room. ADR 0016 §2
menjadikan Kemenag satu-satunya sumber teks Al-Qur'an, dan bentuk itu menghormatinya secara harfiah.

**Versi ketiga (schemaVersion 2, 2026-08-23) mengirim teksnya sendiri.** Alasannya bukan kemudahan
melainkan cakupan: permukaan yang sama dimaksudkan memuat hadis dan kutipan lain, dan tidak ada
dataset lokal untuk meresolusi rujukan semacam itu. Kontrak berbasis rujukan hanya akan pernah bisa
memuat Al-Qur'an.

## 2. Konsekuensi yang harus dibaca sebelum menyentuh fitur ini

**Teks Al-Qur'an sekarang diketik tangan di CMS.** Verifikasi yang dulu didapat gratis — rujukan
yang tidak resolve ditolak, jadi Arab yang salah mustahil secara struktural — sudah hilang, dan
tidak ada yang menggantikannya di aplikasi. `AyatHariIniValidator` masih memeriksa tanggal yang
bisa diurai, terjemahan Indonesia tidak kosong, dan `sourceLabel` tidak kosong. Ia **tidak bisa**
memeriksa apakah sebuah ayat benar, dan tidak ada pemeriksaan sisi klien yang bisa.

Pemilik produk memutuskan **tidak** menarik teks Kemenag ke dalam CMS untuk diperbandingkan
berdampingan (usulan brief §2a). Yang ada di form hanyalah pengingat di sebelah kolom Arab, beserta
tautan ke `quran.kemenag.go.id`. Jadi kendali sisanya adalah **proses editorial, bukan kode**:
siapa pun yang menerbitkan kutipan `kind = 'quran'` adalah pemeriksa terakhir teksnya. Rekamannya
ada di amandemen ADR [0016](../decisions/0016-standalone-quran-kemenag-direct-api.md) tertanggal
2026-08-23.

**Hadis butuh sanad dan derajat.** Hadis tanpa perawi dan status (sahih/hasan/daif) masuk kategori
konten berisiko tinggi menurut `docs/operations/CONTENT_GOVERNANCE.md`. Karena itu `source_label`
**wajib** di tingkat basis data (`NOT NULL`), di form CMS, dan di validator aplikasi — tiga lapis
untuk satu aturan, karena inilah satu-satunya yang masih bisa ditegakkan secara mekanis.

`kind` (`quran` / `hadith` / `other`) adalah **label penataan, bukan klaim**. Aplikasi tidak boleh
menampilkannya sebagai pernyataan tentang otoritas teks.

## 3. Offline-first: kapan aplikasi memanggil API

Aturannya tidak berubah dari versi sebelumnya:

| Keadaan                                                                      | Yang terjadi                                                                                                     |
|------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| Aplikasi dibuka, Room **sudah** punya baris untuk tanggal perangkat hari ini | **Tidak ada request.** Tampilkan dari Room.                                                                      |
| Aplikasi dibuka, Room **belum** punya baris untuk hari ini                   | Panggil API sekali, simpan jendela jadwal yang diterima, tampilkan.                                              |
| Tanggal berganti, panggilan API **gagal** (offline, server mati)             | Room tidak disentuh. Tampilkan entri terakhir yang tersedia (`getLatestOnOrBefore`) — bukan layar kosong.        |
| Belum pernah sinkron sama sekali dan sedang offline                          | Tidak ada yang ditampilkan; bagian kutipan tidak dirender (aturan Beranda: bagian tanpa data tidak dirender).    |
| CMS mengirim `schemaVersion` yang tidak dikenal                              | Cache lama dipertahankan. Inilah yang membuat pergantian versi 1 → 2 aman dirilis dari sisi mana pun lebih dulu. |

Room **hanya ditulis kalau request berhasil**, jadi kegagalan tidak pernah merusak cache yang sudah
ada. Widget tidak pernah memanggil API sendiri; ia membaca baris Room yang sama dengan Beranda.

**Konsekuensi yang disengaja:** begitu hari ini sudah ter-cache, perubahan editor atas jadwal *hari
ini* baru terbaca besok. Jadwal memang dimaksudkan disiapkan di muka. Endpoint mengirim jendela
`hari ini + 90 hari` supaya perangkat yang offline berhari-hari tetap punya isi; 90 hari ke belakang
disimpan sebagai bahan fallback di tabel `ayat_hari_ini`.

## 4. Penjadwal

Seluruh kebijakannya ada di satu fungsi Postgres, `fill_quote_schedule()`
(`../../cms/db/migrations/008_quotes.sql`), yang dipanggil endpoint sebelum membaca:

1. Tanggal masa depan **tidak terkunci** yang memegang kutipan tak-terbit mengembalikan tanggalnya.
2. Setiap tanggal kosong dari hari ini sampai hari ini+90 mendapat kutipan acak yang terbit.
3. Kutipan yang dipakai **dalam 60 hari** sebelum atau sesudah suatu tanggal tidak memenuhi syarat.
   Kalau kolamnya terlalu kecil, syaratnya melonggar — dulu ke "tidak di hari tetangga", lalu tidak
   sama sekali — daripada meninggalkan hari kosong.
4. Tanggal **terkunci** tidak pernah disentuh.
5. **Kolam kosong** tidak menjadwalkan apa pun; endpoint mengirim `items: []`.

Jendelanya berpatokan pada `Asia/Jakarta`, bukan hari UTC server.

## 5. Status implementasi

Selesai dan berjalan, dari basis data sampai widget.

| Lapisan           | Berkas                                                                                                                           | Status                            |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|
| Domain            | `domain/model/AyatHariIni.kt` (`AyatHariIni`, `QuoteKind`, `AyatHariIniSelection`), `domain/repository/AyatHariIniRepository.kt` | Selesai                           |
| Data — lokal      | `data/local/entity/AyatHariIniEntity.kt`, `data/local/dao/AyatHariIniDao.kt`, tabel `ayat_hari_ini` (DB v9)                      | Selesai                           |
| Data — remote     | `data/remote/ayat/` (DTO v2, `AyatHariIniApiService`, `AyatHariIniRemoteSource`, validator)                                      | Selesai                           |
| Data — sync       | `data/sync/ayat/AyatHariIniSyncManager.kt`                                                                                       | Selesai                           |
| Data — repository | `data/repository/AyatHariIniRepositoryImpl.kt`                                                                                   | Selesai                           |
| Presentasi        | `feature/home/BerandaAyatHariIni.kt`, sheet, kartu bagikan, widget                                                               | Selesai                           |
| Sumber data       | `AyatHariIniRemoteSource` → CMS Content API                                                                                      | **Nyata** — fixture sudah dihapus |

`FixtureAyatHariIniRemoteSource` **sudah tidak ada**, begitu pula antarmuka
`AyatHariIniRemoteSource` yang dulu menampungnya: keduanya hanya ada selama endpoint CMS belum
dibangun. `AyatHariIniApiService` terdaftar di `NetworkModule` bersama `ContentApiService`, memakai
Retrofit dan origin yang sama.

Kontrak wire-nya dikunci oleh `app/src/test/.../CmsApiContractTest.kt`, yang mengurai respons
tangkapan asli dari API dan menjalankan validator aplikasi di atasnya.

## 6. Layar admin

Ada di `../../cms` — daftar kutipan (cari + saring per `kind`, `theme`, status terbit), form
kutipan, kalender jadwal dengan kunci/ganti per tanggal, dan peringatan kalau kolam terbit di bawah
60 kutipan. Kontrak endpoint lengkapnya: `../../cms/docs/engineering/API.md`.
