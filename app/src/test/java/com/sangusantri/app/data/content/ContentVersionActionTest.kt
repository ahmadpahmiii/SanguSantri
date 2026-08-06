package com.sangusantri.app.data.content

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentVersionActionTest {
    @Test
    fun noLocalVersionAlwaysImports() {
        val action = decideContentVersionAction(candidateVersion = 1, localVersion = null)

        assertEquals(ContentVersionAction.IMPORT, action)
    }

    @Test
    fun lowerCandidateVersionIsSkipped() {
        val action = decideContentVersionAction(candidateVersion = 1, localVersion = 3)

        assertEquals(ContentVersionAction.SKIP_OLDER, action)
    }

    @Test
    fun sameVersionIsSkippedAsUpToDate() {
        val action = decideContentVersionAction(candidateVersion = 2, localVersion = 2)

        assertEquals(ContentVersionAction.SKIP_UP_TO_DATE, action)
    }

    @Test
    fun higherCandidateVersionImports() {
        val action = decideContentVersionAction(candidateVersion = 2, localVersion = 1)

        assertEquals(ContentVersionAction.IMPORT, action)
    }
}
