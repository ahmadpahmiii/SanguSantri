package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

private val prototypeFixtureAyats =
    listOf(
        QuranReaderAyatUiModel(
            remoteId = 9001,
            surahNumber = 1,
            surahName = "[FIXTURE] Surah",
            ayatNumber = 1,
            juz = 1,
            page = 1,
            arabicText = "نَصٌّ عَرَبِيٌّ لِاخْتِبَارِ التَّنْسِيقِ وَالتَّفَاعُلِ",
            translation = "[FIXTURE] Teks Indonesia untuk menguji pasangan ayat dan terjemahan.",
        ),
        QuranReaderAyatUiModel(
            remoteId = 9002,
            surahNumber = 1,
            surahName = "[FIXTURE] Surah",
            ayatNumber = 2,
            juz = 1,
            page = 1,
            arabicText = "هٰذَا نَصٌّ أَطْوَلُ لِاخْتِبَارِ اِلْتِفَافِ الْكَلِمَاتِ فِي أَكْثَرَ مِنْ سَطْرٍ",
            translation = "[FIXTURE] Baris kedua membuktikan pemisahan blok saat terjemahan ditampilkan.",
        ),
        QuranReaderAyatUiModel(
            remoteId = 9003,
            surahNumber = 1,
            surahName = "[FIXTURE] Surah",
            ayatNumber = 3,
            juz = 1,
            page = 1,
            arabicText = "وَنَصٌّ ثَالِثٌ لِلتَّحَقُّقِ مِنْ نِطَاقِ التَّحْدِيدِ عِنْدَ الضَّغْطِ الْمُطَوَّلِ",
            translation = "[FIXTURE] Highlight hanya mengikuti ayat yang ditekan lama.",
        ),
    )

@Composable
private fun QuranFlowingPrototype() {
    var selectedAyat by remember { mutableStateOf<QuranReaderAyatUiModel?>(prototypeFixtureAyats[1]) }
    var showSheet by remember { mutableStateOf(false) }
    SanguSantriTheme(darkTheme = true) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(QuranBackground),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.large),
                modifier =
                    Modifier
                        .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                        .fillMaxWidth()
                        .padding(SanguSantriSpacing.large),
            ) {
                Text(
                    text = stringResource(R.string.quran_fixture_prototype_label),
                    color = QuranMutedText,
                )
                QuranSurahStartHeader(
                    surahNumber = 2,
                    category = "[FIXTURE] Kategori",
                    surahDisplayName = "[FIXTURE] Surah",
                    surahArabicName = "[FIXTURE]",
                    ayatCount = prototypeFixtureAyats.size,
                    // Never a real basmalah in a fixture preview — the header draws none
                    // when this is blank, which is exactly what this debug-only screen should show.
                    basmalahArabic = "",
                )
                QuranFlowingPageText(
                    ayats = prototypeFixtureAyats,
                    selectedAyatId = selectedAyat?.remoteId,
                    onAyatLongPress = {
                        selectedAyat = it
                        showSheet = true
                    },
                )
            }
        }
        if (showSheet && selectedAyat != null) {
            QuranAyatActionSheet(
                ayat = requireNotNull(selectedAyat),
                isBookmarked = false,
                actions =
                    QuranAyatActionSheetActions(
                        onPlayFromHere = {},
                        onPlaySingle = {},
                        onRepeatAyat = {},
                        onToggleBookmark = {},
                        onOpenTafsir = {},
                        onMarkLastRead = {},
                        onShowPosition = {},
                    ),
                onDismiss = { showSheet = false },
            )
        }
    }
}

@Preview(name = "Quran flowing page — phone", device = Devices.PHONE, showSystemUi = true)
@Composable
private fun QuranFlowingPhonePreview() = QuranFlowingPrototype()

@Preview(name = "Quran flowing page — tablet", device = Devices.TABLET, showSystemUi = true)
@Composable
private fun QuranFlowingTabletPreview() = QuranFlowingPrototype()

@Preview(name = "Quran translated rows", device = Devices.PHONE, showSystemUi = true)
@Composable
private fun QuranTranslatedRowsPreview() {
    SanguSantriTheme(darkTheme = true) {
        QuranTranslationAyatList(
            ayats = prototypeFixtureAyats,
            selectedAyatId = prototypeFixtureAyats[1].remoteId,
            onAyatLongPress = {},
            modifier = Modifier.background(QuranBackground),
        )
    }
}
