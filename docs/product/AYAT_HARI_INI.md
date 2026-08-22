# Ayat Hari Ini — sumber, kontrol editorial, dan sinkronisasi

Ayat harian yang tampil di kepala Beranda dan di widget "Jadwal Sholat + Ayat Hari Ini".

## 1. Siapa yang memilih ayatnya

**Manusia, lewat CMS.** Seorang editor menjadwalkan ayat per tanggal; aplikasi hanya menampilkan apa
yang sudah dijadwalkan. Tidak ada pemilihan otomatis di dalam aplikasi.

### Yang berubah dan kenapa

Versi pertama fitur ini memilih ayat sendiri: `Random(date.toEpochDay()).nextInt(6236)` — satu ayat
acak dari seluruh mushaf, deterministik per tanggal. Pendekatan itu **dibatalkan oleh pemilik
produk** karena tidak bisa dikendalikan: yang muncul besok tidak diketahui siapa pun sampai besok
tiba, dan tidak ada cara menahan, mengganti, atau menjadwalkan ayat tanpa merilis ulang aplikasi.

Kalender lengkap hasil selektor lama pernah dibuatkan untuk diaudit, dan justru audit itu yang
memperjelas masalahnya: karena kolamnya seluruh mushaf, jadwal berisi ayat tentang Jahanam, zaqqum,
dan hukum-hukum tertentu di bawah judul yang dibaca sebagai penyemangat harian. Menyaring itu
berarti melakukan **seleksi editorial atas teks agama** — dan seleksi itu memang harus dilakukan,
tetapi oleh orang yang berwenang, bukan oleh kode. Karena itu keputusannya dipindahkan ke CMS.

Selektor lama sudah dihapus dari kode (`ayatOfDay`, `QuranVerseDao.getByOrdinal`, dan tesnya).

## 2. Yang dikirim CMS: rujukan, bukan teks

Endpoint **tidak pernah mengirim teks Arab atau terjemahan.** Yang dikirim hanya nomor surah dan
nomor ayat; aplikasi mengambil teksnya dari dataset LPMQ Kemenag yang sudah tersimpan di Room.

Tiga alasan, dan yang pertama yang menentukan:

1. **Kemenag tetap satu-satunya sumber teks Al-Qur'an** (ADR 0016 §2). Kalau CMS boleh mengirim
   teks, ada dua sumber ayat yang bisa berbeda isinya, dan kesalahan ketik di CMS akan tampil
   sebagai Al-Qur'an.
2. Payload-nya jadi sangat kecil — satu baris per hari.
3. Kalau editor salah, kesalahannya paling jauh adalah **ayat yang keliru**, bukan **ayat yang
   rusak**.

Aplikasi juga memverifikasi ulang: rujukan yang tidak bisa diresolusi ke dataset lokal (misalnya
nomor ayat melebihi jumlah ayat surahnya) tidak ditampilkan sama sekali.

Satu-satunya field yang benar-benar editorial adalah `theme` — label pendek opsional dari editor
("Sabar", "Syukur"). Itu alasan editor menjadwalkannya, bukan klaim tentang ayatnya.

## 3. Offline-first: kapan aplikasi memanggil API

Aturannya, persis seperti yang diminta pemilik produk:

| Keadaan                                                                      | Yang terjadi                                                                                               |
|------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| Aplikasi dibuka, Room **sudah** punya baris untuk tanggal perangkat hari ini | **Tidak ada request.** Tampilkan dari Room.                                                                |
| Aplikasi dibuka, Room **belum** punya baris untuk hari ini                   | Panggil API sekali, simpan jendela jadwal yang diterima, tampilkan.                                        |
| Tanggal berganti, panggilan API **gagal** (offline, server mati)             | Room tidak disentuh. Tampilkan entri terakhir yang tersedia (`getLatestOnOrBefore`) — bukan layar kosong.  |
| Belum pernah sinkron sama sekali dan sedang offline                          | Tidak ada yang ditampilkan; bagian ayat tidak dirender (aturan Beranda: bagian tanpa data tidak dirender). |
| Dataset Al-Qur'an belum diunduh                                              | Tidak ada yang ditampilkan — teksnya memang belum ada di perangkat.                                        |

Room **hanya ditulis kalau request berhasil**, jadi kegagalan tidak pernah merusak cache yang sudah
ada. Widget tidak pernah memanggil API sendiri; ia membaca baris Room yang sama dengan Beranda.

**Konsekuensi yang disengaja:** begitu hari ini sudah ter-cache, perubahan editor atas jadwal *hari
ini* baru terbaca besok. Jadwal memang dimaksudkan disiapkan di muka. Endpoint mengirim satu jendela
tanggal (bukan hanya hari ini) supaya perangkat yang offline berhari-hari tetap punya isi; 90 hari
ke belakang disimpan sebagai bahan fallback di tabel `ayat_hari_ini`.

## 4. Status implementasi

Lapisan data, domain, dan presentasi **sudah selesai dan berjalan**. Yang belum ada adalah
endpoint-nya.

| Lapisan           | Berkas                                                                                                      | Status                        |
|-------------------|-------------------------------------------------------------------------------------------------------------|-------------------------------|
| Domain            | `domain/model/AyatHariIni.kt`, `domain/repository/AyatHariIniRepository.kt`                                 | Selesai                       |
| Data — lokal      | `data/local/entity/AyatHariIniEntity.kt`, `data/local/dao/AyatHariIniDao.kt`, tabel `ayat_hari_ini` (DB v8) | Selesai                       |
| Data — remote     | `data/remote/ayat/` (DTO, `AyatHariIniApiService`, validator)                                               | Selesai                       |
| Data — sync       | `data/sync/ayat/AyatHariIniSyncManager.kt`                                                                  | Selesai                       |
| Data — repository | `data/repository/AyatHariIniRepositoryImpl.kt`                                                              | Selesai                       |
| Presentasi        | `feature/home/BerandaAyatHariIni.kt`, sheet, kartu bagikan, widget                                          | Selesai                       |
| **Sumber data**   | `FixtureAyatHariIniRemoteSource`                                                                            | **Sementara — harus diganti** |

`FixtureAyatHariIniRemoteSource` menjawab seperti endpoint akan menjawab, tetapi isinya satu entri
saja: **QS. Al-Jumu'ah : 1**, ayat yang memang tampil pada 22 Agustus 2026 dengan mekanisme lama.
Ia mengembalikan entri yang sama untuk tanggal apa pun. Isinya tidak diperbanyak dengan "ayat-ayat
pilihan" secara sengaja — memilih ayat justru pekerjaan editor yang sedang dipindahkan ke CMS.

Fixture ini **tidak boleh ikut rilis.** Menggantinya satu baris di `di/AyatHariIniModule.kt`
(`FixtureAyatHariIniRemoteSource` → `ApiAyatHariIniRemoteSource`), ditambah mendaftarkan
`AyatHariIniApiService` di `NetworkModule` seperti `ContentApiService`.

## 5. Yang harus dibangun berikutnya

Brief untuk sesi terpisah: **`docs/product/AYAT_HARI_INI_CMS_BRIEF.md`** — kontrak endpoint, skema
tabel, dan layar admin di `../cms`.
