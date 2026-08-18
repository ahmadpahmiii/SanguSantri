package com.sangusantri.app.data.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

/**
 * Every murottal URL and file name is derived arithmetically rather than fetched, so the derivation
 * is the contract. [QuranAudioSource.parseFileName] additionally decides what counts as downloaded
 * audio when the directory is rescanned — a name it accepts too readily would report ayat as
 * playable offline that are not.
 */
class QuranAudioSourceTest {
    @Test
    fun ayahUrlUsesTheZeroPaddedPositionalKey() {
        assertEquals("https://cdn.myquran.com/audio/ayah/089004.mp3", QuranAudioSource.ayahAudioUrl(89, 4))
    }

    @Test
    fun threeDigitSurahAndAyahAreNotPaddedFurther() {
        assertEquals("https://cdn.myquran.com/audio/ayah/002286.mp3", QuranAudioSource.ayahAudioUrl(2, 286))
    }

    @Test
    fun fileNameMatchesTheRemotePathSegment() {
        assertEquals("114006.mp3", QuranAudioSource.ayahFileName(114, 6))
    }

    @Test
    fun ayahFileNameIsLocaleIndependent() {
        val original = Locale.getDefault()
        try {
            // Locales with non-ASCII digit shapes (Arabic-Indic here) would otherwise produce a file
            // name that never matches the remote path.
            Locale.setDefault(Locale.forLanguageTag("ar-EG-u-nu-arab"))

            assertEquals("089004.mp3", QuranAudioSource.ayahFileName(89, 4))
        } finally {
            Locale.setDefault(original)
        }
    }

    @Test
    fun ourOwnFileNameParsesBackToItsPosition() {
        assertEquals(89 to 4, QuranAudioSource.parseFileName("089004.mp3"))
    }

    @Test
    fun everyDerivedNameRoundTrips() {
        listOf(1 to 1, 2 to 286, 89 to 4, 114 to 6).forEach { (surah, ayah) ->
            assertEquals(surah to ayah, QuranAudioSource.parseFileName(QuranAudioSource.ayahFileName(surah, ayah)))
        }
    }

    @Test
    fun partialDownloadIsNotCountedAsStoredAudio() {
        // The .part sibling QuranAudioDownloader writes to. Counting it would make a half-downloaded
        // ayah look playable offline.
        assertNull(QuranAudioSource.parseFileName("089004.mp3.part"))
    }

    @Test
    fun strayFilesInTheDirectoryAreIgnored() {
        listOf(
            "notes.txt",
            "089004",
            "89004.mp3",
            "0890044.mp3",
            "abcdef.mp3",
            "",
        ).forEach { name ->
            assertNull("expected $name to be rejected", QuranAudioSource.parseFileName(name))
        }
    }

    @Test
    fun surahNumberOutsideTheQuranIsRejected() {
        assertNull(QuranAudioSource.parseFileName("115001.mp3"))
        assertNull(QuranAudioSource.parseFileName("000001.mp3"))
    }

    @Test
    fun ayahNumberZeroIsRejected() {
        assertNull(QuranAudioSource.parseFileName("089000.mp3"))
    }
}
