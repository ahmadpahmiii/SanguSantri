package com.sangusantri.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.sangusantri.app.R
import kotlinx.serialization.Serializable

@Serializable
private data object Home : NavKey

/**
 * Navigation 3 skeleton for Milestone 0. Serambi and the other real
 * destinations (PRD 7.1) replace [FoundationPlaceholderScreen] as each
 * feature milestone lands.
 */
@Composable
fun SanguSantriNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Home)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is Home -> NavEntry(key) { FoundationPlaceholderScreen() }
                else -> error("Unknown navigation key: $key")
            }
        },
    )
}

@Composable
private fun FoundationPlaceholderScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(id = R.string.foundation_placeholder_message),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
