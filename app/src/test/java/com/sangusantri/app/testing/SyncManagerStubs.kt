package com.sangusantri.app.testing

import androidx.room.InvalidationTracker
import com.sangusantri.app.data.content.ContentImporter
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.remote.api.ContentApiService

/**
 * Constructor filler for the two sync managers, which ViewModel tests fake by subclassing.
 *
 * `ContentSyncManager`/`ContentDetailSyncManager` are concrete classes (deliberately — this project
 * does not add an interface per collaborator just to test it, see `CLAUDE.md`), so a fake has to
 * call the real constructor. Every fake overrides the one method it exposes, so neither of these
 * dependencies is ever touched; they only have to *exist*.
 *
 * They must also be **constructible without throwing**. A tempting `ContentImporter(error("stub"))`
 * compiles — `error` returns `Nothing`, which satisfies any type — but the expression is evaluated
 * eagerly as part of the super-constructor call, so every test that builds such a fake dies with
 * `IllegalStateException: stub` before its first assertion.
 */
internal fun stubContentApiService(): ContentApiService =
    object : ContentApiService {
        override suspend fun getSholawatList() = error("stubContentApiService is never called")

        override suspend fun getAmaliyahList() = error("stubContentApiService is never called")

        override suspend fun getSholawatDetail(id: String) = error("stubContentApiService is never called")

        override suspend fun getAmaliyahDetail(id: String) = error("stubContentApiService is never called")
    }

/** A [ContentImporter] whose database throws only if something actually reaches it. */
internal fun stubContentImporter(): ContentImporter = ContentImporter(stubDatabase())

/**
 * A [SanguSantriDatabase] that constructs cleanly and fails loudly on any real use. Room's DAO
 * getters are abstract, so all of them have to be named here; none is ever called.
 */
@Suppress("TooManyFunctions")
private fun stubDatabase(): SanguSantriDatabase =
    object : SanguSantriDatabase() {
        override fun appMetadataDao() = unused()

        override fun ayatHariIniDao() = unused()

        override fun contentDao() = unused()

        override fun contentStepDao() = unused()

        override fun readingPositionDao() = unused()

        override fun guidedReadingSessionDao() = unused()

        override fun stepProgressDao() = unused()

        override fun tasbihSessionDao() = unused()

        override fun tasbihHistoryDao() = unused()

        override fun amaliyahCompletionEventDao() = unused()

        override fun reminderDao() = unused()

        override fun nahwuQuizPackageDao() = unused()

        override fun nahwuQuizQuestionDao() = unused()

        override fun nahwuQuizAttemptDao() = unused()

        override fun quranSurahDao() = unused()

        override fun quranVerseDao() = unused()

        override fun quranTafsirDao() = unused()

        override fun quranBookmarkDao() = unused()

        override fun quranReadingStateDao() = unused()

        override fun quranReadingSessionDao() = unused()

        override fun prayerTimesDao() = unused()

        override fun createInvalidationTracker(): InvalidationTracker = unused()

        override fun clearAllTables() = unused()
    }

private fun unused(): Nothing =
    error("stubDatabase has no storage — a fake sync manager must override the method under test")
