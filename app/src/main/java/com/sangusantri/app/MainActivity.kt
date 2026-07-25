package com.sangusantri.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SanguSantriNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
