package com.sangusantri.app.feature.update

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.play.core.install.model.AppUpdateType
import com.sangusantri.app.R

/**
 * Mounted once from Beranda (ADR 0017) — every cold start, [AppUpdateViewModel.checkForUpdate]
 * decides force/flexible/none from Remote Config + Play Core, and this composable drives the Play
 * Core UI flow for whichever state results. [snackbarHostState] is the host screen's own
 * `Scaffold` snackbar host, shared rather than duplicated.
 *
 * If the user cancels Play's immediate-update UI while [AppUpdateUiState.RequireForceUpdate] still
 * holds, [checkForUpdate] is called again, which re-evaluates the same policy and (almost always)
 * yields a new [AppUpdateUiState.RequireForceUpdate] instance — driving the effect below to
 * re-invoke the flow. This is what makes the force update non-cancelable, per product decision.
 */
@Composable
fun AppUpdateGate(
    snackbarHostState: SnackbarHostState,
    viewModel: AppUpdateViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK && uiState is AppUpdateUiState.RequireForceUpdate) {
                viewModel.checkForUpdate()
            }
        }

    LaunchedEffect(Unit) { viewModel.checkForUpdate() }

    val flexibleReadyMessage = stringResource(R.string.app_update_flexible_ready_message)
    val flexibleReadyActionLabel = stringResource(R.string.app_update_flexible_ready_action)
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AppUpdateUiState.RequireForceUpdate ->
                viewModel.startUpdateFlow(launcher, state.appUpdateInfo, AppUpdateType.IMMEDIATE)

            is AppUpdateUiState.OfferFlexibleUpdate ->
                viewModel.startUpdateFlow(launcher, state.appUpdateInfo, AppUpdateType.FLEXIBLE)

            AppUpdateUiState.FlexibleUpdateReadyToInstall -> {
                val result =
                    snackbarHostState.showSnackbar(
                        message = flexibleReadyMessage,
                        actionLabel = flexibleReadyActionLabel,
                    )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.completeFlexibleUpdate()
                }
            }

            AppUpdateUiState.Idle -> Unit
        }
    }

    val forceState = uiState as? AppUpdateUiState.RequireForceUpdate
    if (forceState != null) {
        AppUpdateForceDialog(
            onUpdateClick = {
                viewModel.startUpdateFlow(launcher, forceState.appUpdateInfo, AppUpdateType.IMMEDIATE)
            },
        )
    }
}
