package com.sangusantri.app.feature.reader.toc

import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.StepType

/** One Table of Contents entry (FR-017) — a named span of ordered step positions. */
data class TocSection(
    val stepId: String,
    val titleId: String,
    val startPosition: Int,
    val endPosition: Int,
)

/**
 * Derives Table of Contents sections from already-existing ordered steps — a section begins at
 * each [StepType.HEADING] step carrying a non-blank title and runs to the position immediately
 * before the next such heading (or the end of the list). No separate section data is authored;
 * this is purely a read-time grouping of the canonical content model
 * (`docs/engineering/CONTENT_MODEL.md`).
 */
fun List<AmaliyahStep>.toTocSections(): List<TocSection> {
    val headings =
        withIndex().filter { (_, step) ->
            step.stepType == StepType.HEADING && !step.titleId.isNullOrBlank()
        }
    return headings.mapIndexed { order, (index, step) ->
        val nextHeadingIndex = headings.getOrNull(order + 1)?.index ?: size
        TocSection(
            stepId = step.id,
            titleId = step.titleId.orEmpty(),
            startPosition = step.position,
            endPosition = this[nextHeadingIndex - 1].position,
        )
    }
}

/** The section containing [currentPosition] (1-based `AmaliyahStep.position`), if any. */
fun List<TocSection>.sectionContaining(currentPosition: Int): TocSection? =
    lastOrNull {
        currentPosition >=
            it.startPosition
    }
