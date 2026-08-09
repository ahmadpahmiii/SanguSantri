package com.sangusantri.app.feature.update

import com.google.android.play.core.appupdate.AppUpdateInfo

sealed interface AppUpdateUiState {
    data object Idle : AppUpdateUiState

    data class RequireForceUpdate(
        val appUpdateInfo: AppUpdateInfo,
    ) : AppUpdateUiState

    data class OfferFlexibleUpdate(
        val appUpdateInfo: AppUpdateInfo,
    ) : AppUpdateUiState

    data object FlexibleUpdateReadyToInstall : AppUpdateUiState
}
