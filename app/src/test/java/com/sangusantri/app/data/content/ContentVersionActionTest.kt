package com.sangusantri.app.data.content

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentVersionActionTest {
    @Test
    fun noActiveVersionAlwaysImports() {
        val action = decideContentVersionAction(1, "checksum", active = null)

        assertEquals(ContentVersionAction.IMPORT, action)
    }

    @Test
    fun lowerCandidateVersionIsSkipped() {
        val active = summary(versionNumber = 3, checksum = "abc")

        val action = decideContentVersionAction(1, "different", active)

        assertEquals(ContentVersionAction.SKIP_OLDER, action)
    }

    @Test
    fun sameVersionMatchingChecksumIsSkippedAsUpToDate() {
        val active = summary(versionNumber = 2, checksum = "ABCDEF")

        val action = decideContentVersionAction(2, "abcdef", active)

        assertEquals(ContentVersionAction.SKIP_UP_TO_DATE, action)
    }

    @Test
    fun sameVersionDifferentChecksumIsRejectedAsConflict() {
        val active = summary(versionNumber = 2, checksum = "abc123")

        val action = decideContentVersionAction(2, "def456", active)

        assertEquals(ContentVersionAction.REJECT_CHECKSUM_CONFLICT, action)
    }

    @Test
    fun higherCandidateVersionImports() {
        val active = summary(versionNumber = 1, checksum = "abc")

        val action = decideContentVersionAction(2, "different", active)

        assertEquals(ContentVersionAction.IMPORT, action)
    }

    private fun summary(
        versionNumber: Int,
        checksum: String,
    ): ContentPackageImporter.ActiveVersionSummary =
        ContentPackageImporter.ActiveVersionSummary(
            versionId = "v$versionNumber",
            versionNumber = versionNumber,
            checksumSha256 = checksum,
        )
}
