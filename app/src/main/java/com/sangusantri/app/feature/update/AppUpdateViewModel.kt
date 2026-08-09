package com.sangusantri.app.feature.update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.ktx.requestCompleteUpdate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sangusantri.app.BuildConfig
import com.sangusantri.app.domain.model.AppUpdateRequirement
import com.sangusantri.app.domain.model.decideAppUpdateRequirement
import com.sangusantri.app.domain.repository.AppUpdatePolicyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the in-app update gate (ADR 0017). [checkForUpdate] is called once per cold start from
 * Beranda. Fails open everywhere a signal is missing or contradictory — a Remote Config outage or
 * a policy/Play Core mismatch must never trap the user on a dead-end screen (confirmed product
 * decision): if the policy says FORCE but Play Core cannot actually deliver an immediate update,
 * this falls back to offering a flexible update (or doing nothing) instead of blocking.
 */
@HiltViewModel
class AppUpdateViewModel
@Inject
constructor(
    private val appUpdatePolicyRepository: AppUpdatePolicyRepository,
    private val appUpdateManager: AppUpdateManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AppUpdateUiState>(AppUpdateUiState.Idle)
    val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()

    private val installStateListener =
        InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                _uiState.value = AppUpdateUiState.FlexibleUpdateReadyToInstall
            }
        }

    init {
        appUpdateManager.registerListener(installStateListener)
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            val appUpdateInfo = requestAppUpdateInfoOrNull() ?: return@launch

            // A previously started immediate flow was interrupted (e.g. process death) — resume
            // it directly, no need to re-evaluate the policy.
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                _uiState.value = AppUpdateUiState.RequireForceUpdate(appUpdateInfo)
                return@launch
            }

            if (appUpdateInfo.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
                _uiState.value = AppUpdateUiState.Idle
                return@launch
            }

            val policy = appUpdatePolicyRepository.fetchPolicy()
            val requirement =
                policy?.let { decideAppUpdateRequirement(BuildConfig.VERSION_CODE, it) }
                    ?: AppUpdateRequirement.NONE

            _uiState.value = resolveUiState(requirement, appUpdateInfo)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun completeFlexibleUpdate() {
        viewModelScope.launch {
            try {
                appUpdateManager.requestCompleteUpdate()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    /** Keeps [AppUpdateManager] fully encapsulated here — [AppUpdateGate] only owns the launcher. */
    @Suppress("TooGenericExceptionCaught")
    fun startUpdateFlow(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        appUpdateInfo: AppUpdateInfo,
        updateType: Int,
    ) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                launcher,
                AppUpdateOptions.newBuilder(updateType).build(),
            )
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun resolveUiState(
        requirement: AppUpdateRequirement,
        appUpdateInfo: AppUpdateInfo,
    ): AppUpdateUiState =
        when (requirement) {
            AppUpdateRequirement.FORCE ->
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    AppUpdateUiState.RequireForceUpdate(appUpdateInfo)
                } else {
                    FirebaseCrashlytics.getInstance().recordException(
                        IllegalStateException(
                            "in_app_update policy requires FORCE but Play Core cannot deliver an " +
                                "immediate update — failing open",
                        ),
                    )
                    offerFlexibleOrIdle(appUpdateInfo)
                }

            AppUpdateRequirement.FLEXIBLE -> offerFlexibleOrIdle(appUpdateInfo)
            AppUpdateRequirement.NONE -> AppUpdateUiState.Idle
        }

    private fun offerFlexibleOrIdle(appUpdateInfo: AppUpdateInfo): AppUpdateUiState =
        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            AppUpdateUiState.OfferFlexibleUpdate(appUpdateInfo)
        } else {
            AppUpdateUiState.Idle
        }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun requestAppUpdateInfoOrNull(): AppUpdateInfo? =
        try {
            appUpdateManager.requestAppUpdateInfo()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }

    override fun onCleared() {
        appUpdateManager.unregisterListener(installStateListener)
        super.onCleared()
    }
}
