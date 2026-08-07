package com.sangusantri.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.sangusantri.app.MainActivity.Companion.EXTRA_REMINDER_CONTENT_ID
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.navigation.SanguSantriNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // A class-level (not remember-scoped) holder so both a cold start (onCreate) and a reminder
    // notification tapped while already running (onNewIntent) can set it, and both are observed by
    // the same setContent tree.
    private val deepLinkContentId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLinkContentId.value = intent.reminderContentIdExtra()
        setContent {
            SanguSantriTheme {
                // SanguSantriNavHost owns the app's single top-level Scaffold (bottom navigation
                // bar) — no outer Scaffold here, which would otherwise double-apply system-bar
                // inset padding (docs/engineering/ARCHITECTURE.md's edge-to-edge rule).
                SanguSantriNavHost(
                    modifier = Modifier.fillMaxSize(),
                    deepLinkContentId = deepLinkContentId.value,
                    onDeepLinkConsumed = { deepLinkContentId.value = null },
                )
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
