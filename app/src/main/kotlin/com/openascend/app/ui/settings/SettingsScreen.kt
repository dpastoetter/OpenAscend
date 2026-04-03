package com.openascend.app.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.BuildConfig
import com.openascend.domain.model.ThemePreference
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chronicle settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Version v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("Privacy-first", style = MaterialTheme.typography.titleMedium)
            Text(
                "OpenAscend stays offline in this MVP. Toggle future telemetry preferences now so contributors know the stance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ToggleRow(
                title = "Product analytics (off by default)",
                checked = ui.privacy.analyticsOptIn,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(analyticsOptIn = it))
                },
            )
            ToggleRow(
                title = "Crash reports (off by default)",
                checked = ui.privacy.crashReportsOptIn,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(crashReportsOptIn = it))
                },
            )
            ToggleRow(
                title = "Show gentle finance hints",
                checked = ui.privacy.showFinanceHints,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(showFinanceHints = it))
                },
            )

            Text("Appearance", style = MaterialTheme.typography.titleMedium)
            Text(
                "System follows your device; Android 12+ can use dynamic colors from wallpaper. Light and dark use OpenAscend's fixed palette.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    ThemePreference.SYSTEM to "System",
                    ThemePreference.LIGHT to "Light",
                    ThemePreference.DARK to "Dark",
                ).forEach { (pref, label) ->
                    FilterChip(
                        selected = ui.privacy.themePreference == pref,
                        onClick = { viewModel.setPrivacy(ui.privacy.copy(themePreference = pref)) },
                        label = { Text(label) },
                    )
                }
            }

            Text("Data portability", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = {
                    scope.launch {
                        val json = viewModel.exportJson()
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(json)
                            .setChooserTitle("Export OpenAscend JSON")
                            .startChooser()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Export my data (JSON)")
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
