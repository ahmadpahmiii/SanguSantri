package com.sangusantri.app.feature.sholawat

import com.sangusantri.app.domain.model.ContentStep
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Guards the one rule that decides what the Sholawat reader draws in two columns. Getting it wrong
 * is not a cosmetic bug: prose paired into columns is cut mid-sentence, and a qasidah left
 * unpaired stops looking like a qasidah.
 */
class SholawatReaderRowTest {
    /** Salamun Salam's shape: no separator anywhere, so steps pair two at a time. */
    @Test
    fun stepsWithoutSeparatorPairTwoAtATime() {
        val rows = listOf(step("1", "sadr"), step("2", "ajuz")).toSholawatReaderRows(pairHemistichs = true)

        assertEquals(1, rows.size)
        val bait = rows.single() as SholawatReaderRow.Bait
        assertEquals(listOf("sadr", "ajuz"), bait.hemistichs)
        assertEquals(2, bait.translations.size)
    }

    /** Published-before-the-editor-checked content: the orphan renders, it is not dropped. */
    @Test
    fun oddPairedStepCountKeepsTheLastHemistich() {
        val rows =
            listOf(step("1", "a"), step("2", "b"), step("3", "orphan"))
                .toSholawatReaderRows(pairHemistichs = true)

        assertEquals(2, rows.size)
        assertEquals(listOf("orphan"), (rows[1] as SholawatReaderRow.Bait).hemistichs)
    }

    /** Maulid ad-Diba'i's shape: the bait arrives as one step carrying its own separator. */
    @Test
    fun stepCarryingSeparatorSplitsIntoTwoColumns() {
        val rows = listOf(step("1", "sadr ۞ ajuz", translation = "satu kalimat")).toSholawatReaderRows(true)

        val bait = rows.single() as SholawatReaderRow.Bait
        assertEquals(listOf("sadr", "ajuz"), bait.hemistichs)
        assertEquals(listOf("satu kalimat"), bait.translations)
    }

    /**
     * The mixed item — 94 baits and 27 prose blocks in one reading. Prose must not be swept into
     * the pairing just because the item is marked bayt.
     */
    @Test
    fun proseInABaitItemStaysFullWidth() {
        val rows =
            listOf(step("1", "sadr ۞ ajuz"), step("2", "prosa panjang"), step("3", "sadr2 ۞ ajuz2"))
                .toSholawatReaderRows(pairHemistichs = true)

        assertEquals(3, rows.size)
        assertEquals(listOf("sadr", "ajuz"), (rows[0] as SholawatReaderRow.Bait).hemistichs)
        assertEquals("prosa panjang", (rows[1] as SholawatReaderRow.Verse).step.arabicText)
        assertEquals(listOf("sadr2", "ajuz2"), (rows[2] as SholawatReaderRow.Bait).hemistichs)
    }

    /** Shalawat Tarhim carries steps with two separators; three columns is not a form. */
    @Test
    fun stepWithTwoSeparatorsIsNotABait() {
        val rows = listOf(step("1", "a ۞ b ۞ c"), step("2", "d ۞ e")).toSholawatReaderRows(true)

        assertEquals("a ۞ b ۞ c", (rows[0] as SholawatReaderRow.Verse).step.arabicText)
        assertEquals(listOf("d", "e"), (rows[1] as SholawatReaderRow.Bait).hemistichs)
    }

    /** Narrow screen, big system font, or the user's own switch: every step reads straight down. */
    @Test
    fun pairingOffDrawsEveryStepFullWidth() {
        val rows =
            listOf(step("1", "sadr ۞ ajuz"), step("2", "prosa"))
                .toSholawatReaderRows(pairHemistichs = false)

        assertEquals(2, rows.count { it is SholawatReaderRow.Verse })
    }

    private fun step(
        id: String,
        arabic: String,
        translation: String = "terjemahan",
    ) = ContentStep(
        id = id,
        contentId = "maulid-dibai",
        position = id.toInt(),
        arabicText = arabic,
        translation = translation,
        repeatTarget = null,
    )
}
