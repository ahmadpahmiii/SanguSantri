package com.sangusantri.app.feature.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.QuranThemeMode
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Observes the persisted Quran theme mode for [com.sangusantri.app.navigation.SanguSantriNavHost],
 * which provides it ambiently via [com.sangusantri.app.core.designsystem.theme.LocalQuranThemeMode]
 * — a single read shared by every Quran screen and the nav host's own background, rather than each
 * of the five Quran route composables independently observing the same DataStore flow. */
@HiltViewModel
class QuranThemeViewModel
@Inject
constructor(
    settingsRepository: QuranReaderSettingsRepository,
) : ViewModel() {
    val themeMode: StateFlow<QuranThemeMode> =
        settingsRepository
            .observe()
            .map { it.themeMode }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
                QuranThemeMode.DARK,
            )

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5000L
    }
}
