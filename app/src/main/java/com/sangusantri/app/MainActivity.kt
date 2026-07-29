package com.sangusantri.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.navigation.SanguSantriNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SanguSantriTheme {
                // SanguSantriNavHost owns the app's single top-level Scaffold (bottom navigation
                // bar) — no outer Scaffold here, which would otherwise double-apply system-bar
                // inset padding (docs/engineering/ARCHITECTURE.md's edge-to-edge rule).
                SanguSantriNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
