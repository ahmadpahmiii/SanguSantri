package com.sangusantri.app.feature.sholawat

import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.baitHemistichs

/**
 * One row of the Sholawat reader: either a bait drawn in two columns, or a block drawn full width.
 *
 * The distinction is per row, not per item, because a maulid is not uniformly one or the other.
 * Maulid ad-Diba'i alternates qasidah and prose chapter by chapter — 94 baits and 27 prose blocks
 * in one reading — and prose forced into two columns is cut mid-sentence.
 */
sealed interface SholawatReaderRow {
    /** Stable across recompositions and edits to other rows; the LazyColumn key. */
    val key: String

    /** Sadr and ajuz side by side. [translations] holds one entry per hemistich, or one for the bait. */
    data class Bait(override val key: String, val hemistichs: List<String>, val translations: List<String>) :
        SholawatReaderRow

    /** Anything read straight across: prose, a heading, the recurring refrain. */
    data class Verse(override val key: String, val step: ContentStep) : SholawatReaderRow
}

/**
 * Arrange steps into rows. [pairHemistichs] is the caller's already-narrowed decision that this
 * item may be drawn in two columns at all (CMS layout, screen width, user preference); false means
 * every step reads straight down, which is correct for any content.
 *
 * Two shapes of paired content exist and both have to keep working:
 *
 * - **One step per hemistich** — Salamun Salam, and how the CMS editor still writes paired verse.
 *   Steps carry no separator, so they pair two at a time. An odd count leaves a single-hemistich
 *   final row rather than dropping it: the editor refuses to save that, but content published
 *   before it did still has to read.
 * - **One step per bait** — Maulid ad-Diba'i, imported from NU with `۞` inside the step. Here the
 *   text says where each bait divides, so rows map one-to-one onto steps and the prose steps in
 *   between stay full width.
 *
 * A single separator anywhere in the item switches the whole item to the second shape, so a
 * bait-per-step item never also tries to pair its prose two at a time.
 */
fun List<ContentStep>.toSholawatReaderRows(pairHemistichs: Boolean): List<SholawatReaderRow> = when {
    !pairHemistichs -> map { SholawatReaderRow.Verse(key = it.id, step = it) }

    none { it.baitHemistichs != null } ->
        chunked(BAYT_HEMISTICHS).map { bait ->
            SholawatReaderRow.Bait(
                key = bait.first().id,
                hemistichs = bait.map { it.arabicText },
                translations = bait.map { it.translation },
            )
        }

    else ->
        map { step ->
            val hemistichs = step.baitHemistichs
            if (hemistichs == null) {
                SholawatReaderRow.Verse(key = step.id, step = step)
            } else {
                // One translation for the whole bait: NU writes it as a single sentence, and
                // half a sentence under each column would guess where it divides.
                SholawatReaderRow.Bait(
                    key = step.id,
                    hemistichs = hemistichs,
                    translations = listOf(step.translation),
                )
            }
        }
}

private const val BAYT_HEMISTICHS = 2
