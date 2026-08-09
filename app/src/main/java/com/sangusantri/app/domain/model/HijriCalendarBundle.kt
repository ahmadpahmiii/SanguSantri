package com.sangusantri.app.domain.model

/**
 * The versioned, curated local content bundle for Kalender Hijriah (`0.0.7`). [rules] is the
 * initial allowlist from `docs/product/HIJRI_CALENDAR_PRD.md` §5.2 — the product owner's own
 * approval document (approved 8 August 2026) already names a Kemenag source per rule, which is the
 * editorial acceptance this risk-based-publication category requires
 * (`docs/operations/CONTENT_GOVERNANCE.md`). Puasa Senin–Kamis is intentionally absent (§4.2).
 *
 * [officialRecords] — sourced Gregorian national-holiday/cuti-bersama dates — is deliberately empty
 * this pass. §12 is explicit that "the design fixture dates are not substitutes for [the] release
 * dataset": populating real per-year official dates requires its own source-by-source editorial
 * acceptance against a named government publication, which is separate curation work, not something
 * to infer or scrape here (`CLAUDE.md` content-safety rules). Sundays still render red without it;
 * only the "sourced official holiday" red-numeral/agenda path stays inactive until that dataset
 * lands.
 */
object HijriCalendarBundle {
    const val VERSION = 1

    private const val EDITORIAL_NOTE =
        "Diterima product owner pada persetujuan docs/product/HIJRI_CALENDAR_PRD.md §5.2 (8 Agustus 2026)."

    val officialRecords: List<HijriCalendarEvent> = emptyList()

    val rules: List<HijriRecurringEventRule> =
        listOf(
            HijriRecurringEventRule(
                id = "ramadan",
                kind = HijriEventKind.FASTING,
                title = "Ramadan",
                description = "Puasa wajib Ramadan.",
                hijriMonth = HIJRI_MONTH_RAMADAN,
                startDay = 1,
                endDay = null,
                provenance =
                    provenance(
                        publisher = "Kementerian Agama RI",
                        title =
                            "Kemenag Terbitkan PMA 1/2026, Padukan Hisab dan Rukyat dalam " +
                                "Penetapan Awal Bulan Hijriah",
                        url =
                            "https://kemenag.go.id/nasional/kemenag-terbitkan-pma-12026-padukan-hisab-dan-rukyat-" +
                                "dalam-penetapan-awal-bulan-hijriah-a94ay",
                    ),
            ),
            HijriRecurringEventRule(
                id = "tasua-asyura",
                kind = HijriEventKind.FASTING,
                title = "Tasu'a dan Asyura",
                description = "Puasa sunnah 9–10 Muharram.",
                hijriMonth = HIJRI_MONTH_MUHARRAM,
                startDay = 9,
                endDay = 10,
                provenance =
                    provenance(
                        publisher = "Kemenag Kepri",
                        title = "Kegiatan Penyuluhan Keutamaan Bulan Muharram",
                        url =
                            "https://kepri.kemenag.go.id/page/det/kegiatan-penyuluhan-keutamaan-bulan-muharram-" +
                                "digelar-di-masjid-al-a-laa-kecamatan-singkep",
                    ),
            ),
            HijriRecurringEventRule(
                id = "ayyamul-bidh",
                kind = HijriEventKind.FASTING,
                title = "Ayyamul Bidh",
                description = "Puasa sunnah pertengahan bulan Hijriah (13–15).",
                hijriMonth = null,
                excludedHijriMonths = setOf(HIJRI_MONTH_ZULHIJJAH),
                startDay = 13,
                endDay = 15,
                provenance =
                    provenance(
                        publisher = "Kemenag Gorontalo",
                        title = "Kakankemenag Sampaikan Hikmah Puasa dalam Safari Ramadan",
                        url =
                            "https://gorontalo.kemenag.go.id/daerah/kakankemenag-sampaikan-hikmah-puasa-dalam-" +
                                "safari-ramadan-bupati-pohuwato",
                    ),
            ),
            HijriRecurringEventRule(
                id = "ayyamul-bidh-zulhijjah",
                kind = HijriEventKind.FASTING,
                title = "Ayyamul Bidh",
                description = "Puasa sunnah pertengahan Zulhijah (14–15); 13 Zulhijah adalah hari Tasyrik.",
                hijriMonth = HIJRI_MONTH_ZULHIJJAH,
                startDay = 14,
                endDay = 15,
                provenance =
                    provenance(
                        publisher = "Kemenag Gorontalo",
                        title = "Kakankemenag Sampaikan Hikmah Puasa dalam Safari Ramadan",
                        url =
                            "https://gorontalo.kemenag.go.id/daerah/kakankemenag-sampaikan-hikmah-puasa-dalam-" +
                                "safari-ramadan-bupati-pohuwato",
                    ),
            ),
            HijriRecurringEventRule(
                id = "tarwiyah",
                kind = HijriEventKind.FASTING,
                title = "Tarwiyah",
                description = "Puasa sunnah 8 Zulhijah.",
                hijriMonth = HIJRI_MONTH_ZULHIJJAH,
                startDay = 8,
                endDay = 8,
                provenance =
                    provenance(
                        publisher = "Kementerian Agama RI",
                        title = "1 Zulhijah 1447 H Jatuh pada 18 Mei 2026, Iduladha 27 Mei 2026",
                        url =
                            "https://kemenag.go.id/pers-rilis/1-zulhijah-1447-h-jatuh-pada-18-mei-2026-" +
                                "iduladha-27-mei-2026-xARut",
                    ),
            ),
            HijriRecurringEventRule(
                id = "arafah",
                kind = HijriEventKind.FASTING,
                title = "Arafah",
                description = "Puasa sunnah 9 Zulhijah, dianjurkan bagi yang tidak sedang berhaji.",
                hijriMonth = HIJRI_MONTH_ZULHIJJAH,
                startDay = 9,
                endDay = 9,
                provenance =
                    provenance(
                        publisher = "Kementerian Agama RI",
                        title = "1 Zulhijah 1447 H Jatuh pada 18 Mei 2026, Iduladha 27 Mei 2026",
                        url =
                            "https://kemenag.go.id/pers-rilis/1-zulhijah-1447-h-jatuh-pada-18-mei-2026-" +
                                "iduladha-27-mei-2026-xARut",
                    ),
            ),
            HijriRecurringEventRule(
                id = "syawal-enam-hari",
                kind = HijriEventKind.FASTING,
                title = "Puasa 6 Hari Syawal",
                description = "Jendela panduan puasa sunnah 6 hari, kapan saja mulai 2 Syawal hingga akhir Syawal.",
                hijriMonth = HIJRI_MONTH_SYAWAL,
                startDay = 2,
                endDay = null,
                isFlexibleWindow = true,
                provenance =
                    provenance(
                        publisher = "Kementerian Agama RI",
                        title = "Tanya Jawab Fiqih: Puasa Sunnah Syawal",
                        url =
                            "https://kemenag.go.id/tanya-jawab-fiqih/ingin-sekali-saya-puasa-sunah-syawal-" +
                                "bagaimana-hukum-dan-ketentuannya-HJHdZ",
                    ),
            ),
            HijriRecurringEventRule(
                id = "idul-fitri",
                kind = HijriEventKind.FASTING_PROHIBITED,
                title = "Idul Fitri",
                description = "1 Syawal; puasa diharamkan pada hari ini.",
                hijriMonth = HIJRI_MONTH_SYAWAL,
                startDay = 1,
                endDay = 1,
                provenance =
                    provenance(
                        publisher = "Kementerian Agama RI",
                        title = "Idul Fitri dan Beberapa Amalan Utama Rasulullah saw.",
                        url =
                            "https://kemenag.go.id/ar/hikmah/idul-fitri-dan-beberapa-amalan-utama-rasulullah-" +
                                "saw-Q2x0C",
                    ),
            ),
            HijriRecurringEventRule(
                id = "idul-adha",
                kind = HijriEventKind.FASTING_PROHIBITED,
                title = "Idul Adha",
                description = "10 Zulhijah; puasa diharamkan pada hari ini.",
                hijriMonth = HIJRI_MONTH_ZULHIJJAH,
                startDay = 10,
                endDay = 10,
                provenance =
                    provenance(
                        publisher = "Kementerian Agama RI",
                        title = "1 Zulhijah 1447 H Jatuh pada 18 Mei 2026, Iduladha 27 Mei 2026",
                        url =
                            "https://kemenag.go.id/pers-rilis/1-zulhijah-1447-h-jatuh-pada-18-mei-2026-" +
                                "iduladha-27-mei-2026-xARut",
                    ),
            ),
            HijriRecurringEventRule(
                id = "tasyrik",
                kind = HijriEventKind.FASTING_PROHIBITED,
                title = "Hari Tasyrik",
                description = "11–13 Zulhijah; puasa diharamkan pada hari-hari ini.",
                hijriMonth = HIJRI_MONTH_ZULHIJJAH,
                startDay = 11,
                endDay = 13,
                provenance =
                    provenance(
                        publisher = "Kemenag Bali",
                        title = "5 Amalan Sunah di Hari-Hari Tasyrik",
                        url = "https://bali.kemenag.go.id/jembrana/berita/27022/5-amalan-sunah-di-hari-hari-tasyrik",
                    ),
            ),
        )

    private fun provenance(
        publisher: String,
        title: String,
        url: String,
    ) = HijriEventProvenance(
        bundleVersion = VERSION,
        sourcePublisher = publisher,
        sourceTitle = title,
        sourceUrl = url,
        sourceYear = null,
        editorialNote = EDITORIAL_NOTE,
    )

    private const val HIJRI_MONTH_MUHARRAM = 1
    private const val HIJRI_MONTH_RAMADAN = 9
    private const val HIJRI_MONTH_SYAWAL = 10
    private const val HIJRI_MONTH_ZULHIJJAH = 12
}
