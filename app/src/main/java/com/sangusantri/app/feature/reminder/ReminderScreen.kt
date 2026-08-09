package com.sangusantri.app.feature.reminder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.Reminder

@Composable
fun ReminderRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var notificationPermissionGranted by remember { mutableStateOf(hasNotificationPermission(context)) }
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            notificationPermissionGranted = granted
            if (!granted) {
                val activity = context.findActivity()
                val canShowRationale =
                    activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) ?: true
                permanentlyDenied = !canShowRationale
            }
        }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    notificationPermissionGranted = hasNotificationPermission(context)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ReminderScreen(
        uiState = uiState,
        notificationPermissionGranted = notificationPermissionGranted,
        permanentlyDenied = permanentlyDenied,
        onRequestNotificationPermission = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        onOpenNotificationSettings = { context.startActivity(notificationSettingsIntent(context)) },
        onAction = viewModel::onAction,
        onBack = onBack,
        modifier = modifier,
    )
}

private fun hasNotificationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

// ACTION_APP_NOTIFICATION_SETTINGS has existed since API 26 — minSdk itself, so no version branch
// is needed (unlike a real API-26 feature check elsewhere in this project).
private fun notificationSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    uiState: ReminderUiState,
    notificationPermissionGranted: Boolean,
    permanentlyDenied: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onAction: (ReminderUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var formTarget by remember { mutableStateOf<FormTarget?>(null) }
    var pendingDelete by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = { ReminderTopBar(onBack = onBack) },
        floatingActionButton = {
            if (uiState is ReminderUiState.Loaded && uiState.availableContent.isNotEmpty()) {
                FloatingActionButton(onClick = { formTarget = FormTarget.New }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.reminder_add_action),
                    )
                }
            }
        },
    ) { innerPadding ->
        when (uiState) {
            ReminderUiState.Loading ->
                Box(
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

            is ReminderUiState.Loaded ->
                ReminderList(
                    uiState = uiState,
                    notificationPermissionGranted = notificationPermissionGranted,
                    permanentlyDenied = permanentlyDenied,
                    onRequestNotificationPermission = onRequestNotificationPermission,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    onToggle = { reminder -> onAction(ReminderUiAction.ToggleEnabled(reminder)) },
                    onEdit = { reminder -> formTarget = FormTarget.Edit(reminder) },
                    onDeleteRequest = { reminder -> pendingDelete = reminder },
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }

    val currentFormTarget = formTarget
    if (currentFormTarget != null && uiState is ReminderUiState.Loaded) {
        ReminderFormOverlay(
            uiState = uiState,
            formTarget = currentFormTarget,
            onAction = onAction,
            onDismiss = { formTarget = null },
        )
    }
    pendingDelete?.let { reminder ->
        ReminderDeleteOverlay(reminder = reminder, onAction = onAction, onDismiss = { pendingDelete = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.reminder_screen_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_content_description),
                )
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun ReminderScreenEmptyPreview() {
    SanguSantriTheme {
        ReminderScreen(
            uiState = ReminderUiState.Loaded(reminders = emptyList(), availableContent = emptyList()),
            notificationPermissionGranted = true,
            permanentlyDenied = false,
            onRequestNotificationPermission = {},
            onOpenNotificationSettings = {},
            onAction = {},
            onBack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ReminderScreenLoadingPreview() {
    SanguSantriTheme {
        ReminderScreen(
            uiState = ReminderUiState.Loading,
            notificationPermissionGranted = false,
            permanentlyDenied = false,
            onRequestNotificationPermission = {},
            onOpenNotificationSettings = {},
            onAction = {},
            onBack = {},
        )
    }
}
