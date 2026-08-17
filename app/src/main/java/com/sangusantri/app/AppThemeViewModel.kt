package com.sangusantri.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Observes the persisted app-wide theme mode for [MainActivity], which resolves it (`null` =
 * follow the system) and provides both the resolved value via
 * [com.sangusantri.app.core.designsystem.theme.LocalAppThemeMode] and the matching
 * [com.sangusantri.app.core.designsystem.theme.SanguSantriTheme] — one read shared by every screen
 * rather than each observing the same DataStore flow. */
@HiltViewModel
class AppThemeViewModel
@Inject
constructor(
    settingsRepository: QuranReaderSettingsRepository,
) : ViewModel() {
    /** `null` while the user has never chosen a mode — [MainActivity] then follows the system. */
    val themeMode: StateFlow<AppThemeMode?> =
        settingsRepository
            .observe()
            .map { it.themeMode }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
                null,
            )

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5000L
    }
}
