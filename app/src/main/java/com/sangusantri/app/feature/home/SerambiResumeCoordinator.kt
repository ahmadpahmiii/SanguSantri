package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.GuidedReadingSession
import com.sangusantri.app.domain.model.QuranReadingState
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.domain.model.ReadingPosition
import com.sangusantri.app.domain.model.TasbihSession
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.GuidedReadingRepository
import com.sangusantri.app.domain.repository.HomePreferencesRepository
import com.sangusantri.app.domain.repository.QuranRepository
import com.sangusantri.app.domain.repository.ReadingPositionRepository
import com.sangusantri.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Selects the newest genuinely resumable local activity across the app's bounded contexts. */
class SerambiResumeCoordinator
@Inject
constructor(
    private val contentRepository: ContentRepository,
    private val readingPositionRepository: ReadingPositionRepository,
    private val guidedReadingRepository: GuidedReadingRepository,
    quranRepository: QuranRepository,
    tasbihRepository: TasbihRepository,
    private val homePreferencesRepository: HomePreferencesRepository,
) {
    private val resumeSources: Flow<ResumeSources> =
        combine(
            quranRepository.observeReadingState(),
            quranRepository.observeSurahs(),
            tasbihRepository.observeSession(),
            homePreferencesRepository.observeDismissedResumeFingerprint(),
        ) { quranReadingState, quranSurahs, tasbihSession, dismissedFingerprint ->
            ResumeSources(quranReadingState, quranSurahs, tasbihSession, dismissedFingerprint)
        }

    fun observe(items: Flow<List<Content>>): Flow<SerambiResumeItem?> =
        combine(items, resumeSources) { activeItems, sources -> resolve(activeItems, sources) }

    suspend fun dismiss(fingerprint: String) {
        homePreferencesRepository.dismissResume(fingerprint)
    }

    private suspend fun resolve(
        items: List<Content>,
        sources: ResumeSources,
    ): SerambiResumeItem? {
        val fullPosition = readingPositionRepository.getMostRecentPosition()
        val guidedSession = guidedReadingRepository.getMostRecentIncompleteSession()
        val latestCandidate =
            listOfNotNull(
                latestAmaliyahResumeItem(items, fullPosition, guidedSession),
                sources.quranReadingState?.toResumeItem(sources.quranSurahs),
                sources.tasbihSession?.toResumeItem(),
            ).maxByOrNull(SerambiResumeItem::lastActivityAtEpochMillis)
        return latestCandidate?.takeUnless {
            it.dismissFingerprint == sources.dismissedFingerprint
        }
    }

    private suspend fun latestAmaliyahResumeItem(
        items: List<Content>,
        fullPosition: ReadingPosition?,
        guidedSession: GuidedReadingSession?,
    ): SerambiResumeItem.Amaliyah? {
        val useGuided =
            guidedSession != null &&
                guidedSession.lastOpenedAtEpochMillis >= (fullPosition?.lastOpenedAtEpochMillis ?: Long.MIN_VALUE)
        val contentId = if (useGuided) guidedSession?.contentId else fullPosition?.contentId
        val content = items.find { it.id == contentId }
        val detail = content?.let { contentRepository.getContentDetail(it.id) }
        if (content == null || detail == null || detail.steps.isEmpty()) return null

        val currentIndex =
            if (useGuided) {
                detail.steps.indexOfFirst { it.id == guidedSession?.currentStepId }
            } else {
                fullPosition?.itemIndex ?: 0
            }.coerceIn(0, detail.steps.lastIndex)

        return SerambiResumeItem.Amaliyah(
            contentId = content.id,
            title = content.title,
            mode = if (useGuided) ReaderMode.GUIDED else ReaderMode.FULL,
            current = currentIndex + 1,
            total = detail.steps.size,
            lastActivityAtEpochMillis =
                if (useGuided) {
                    guidedSession?.lastOpenedAtEpochMillis ?: 0L
                } else {
                    fullPosition?.lastOpenedAtEpochMillis ?: 0L
                },
        )
    }

    private fun QuranReadingState.toResumeItem(surahs: List<QuranSurah>): SerambiResumeItem.Quran? {
        val surah = surahs.find { it.number == surahNumber } ?: return null
        return SerambiResumeItem.Quran(
            surahNumber = surahNumber,
            surahName = surah.latinName,
            ayatNumber = ayatNumber.coerceIn(1, surah.ayatCount.coerceAtLeast(1)),
            totalAyat = surah.ayatCount.coerceAtLeast(1),
            lastActivityAtEpochMillis = updatedAtEpochMillis,
        )
    }

    private fun TasbihSession.toResumeItem() =
        SerambiResumeItem.Tasbih(
            sessionName = sessionName,
            currentCount = currentCount.coerceAtLeast(0),
            targetCount = targetValue,
            lastActivityAtEpochMillis = updatedAtEpochMillis,
        )

    private data class ResumeSources(
        val quranReadingState: QuranReadingState?,
        val quranSurahs: List<QuranSurah>,
        val tasbihSession: TasbihSession?,
        val dismissedFingerprint: String?,
    )
}
