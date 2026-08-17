package com.sangusantri.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.MainActivity.Companion.EXTRA_REMINDER_CONTENT_ID
import com.sangusantri.app.core.designsystem.theme.LocalAppThemeMode
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.navigation.SanguSantriNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // A class-level (not remember-scoped) holder so both a cold start (onCreate) and a reminder
    // notification tapped while already running (onNewIntent) can set it, and both are observed by
    // the same setContent tree.
    private val deepLinkContentId = mutableStateOf<String?>(null)

    private val themeViewModel: AppThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLinkContentId.value = intent.reminderContentIdExtra()
        setContent {
            // One theme for the whole app (Beranda/Quran revamp) — resolved here, once, rather
            // than by the Quran destination alone as before. A null persisted mode means the user
            // has never chosen, so the system setting applies until they tap the toggle.
            val storedMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = storedMode?.let { it == AppThemeMode.DARK } ?: isSystemInDarkTheme()
            SystemBarIconAppearance(isDark)

            CompositionLocalProvider(
                LocalAppThemeMode provides if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT,
            ) {
                SanguSantriTheme(darkTheme = isDark) {
                    // SanguSantriNavHost owns the app's single top-level Scaffold (bottom
                    // navigation bar) — no outer Scaffold here, which would otherwise double-apply
                    // system-bar inset padding (docs/engineering/ARCHITECTURE.md's edge-to-edge
                    // rule).
                    SanguSantriNavHost(
                        modifier = Modifier.fillMaxSize(),
                        deepLinkContentId = deepLinkContentId.value,
                        onDeepLinkConsumed = { deepLinkContentId.value = null },
                    )
                }
            }
        }
    }

    /** Keeps the system bars' icons legible against the app's own background — light icons over the
     * dark canvas, dark icons over the light one. Previously done per Quran screen; app-wide now
     * that one theme covers every destination. */
    @Composable
    private fun SystemBarIconAppearance(isDark: Boolean) {
        val view = LocalView.current
        if (view.isInEditMode) return
        LaunchedEffect(view, isDark) {
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    /** A reminder notification tap (`data/reminder/ReminderAlarmReceiver`) launches this Activity
     * with [EXTRA_REMINDER_CONTENT_ID] set — reaches here instead of [onCreate] whenever the
     * Activity is already running. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkContentId.value = intent.reminderContentIdExtra()
    }

    private fun Intent?.reminderContentIdExtra(): String? = this?.getStringExtra(EXTRA_REMINDER_CONTENT_ID)

    companion object {
        const val EXTRA_REMINDER_CONTENT_ID = "reminder_content_id"
    }
}
