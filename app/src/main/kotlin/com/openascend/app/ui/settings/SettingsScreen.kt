package com.openascend.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.BuildConfig
import com.openascend.app.R
import com.openascend.domain.model.FamiliarSpecies
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

    val createBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val ok = viewModel.saveBackupToUri(context, uri)
            Toast.makeText(
                context,
                if (ok) context.getString(R.string.backup_export_success) else context.getString(R.string.backup_export_error),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
    val openBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val ok = viewModel.restoreBackupFromUri(context, uri)
            Toast.makeText(
                context,
                if (ok) context.getString(R.string.backup_import_success) else context.getString(R.string.backup_import_error),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
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
                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(stringResource(R.string.settings_privacy_first), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_privacy_offline_blurb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(stringResource(R.string.settings_analytics_title), style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.settings_analytics_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(stringResource(R.string.settings_crash_title), style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.settings_crash_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ToggleRow(
                title = stringResource(R.string.settings_finance_hints),
                checked = ui.privacy.showFinanceHints,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(showFinanceHints = it))
                },
            )

            Text(stringResource(R.string.settings_appearance), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_appearance_blurb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    ThemePreference.SYSTEM to R.string.settings_theme_system,
                    ThemePreference.LIGHT to R.string.settings_theme_light,
                    ThemePreference.DARK to R.string.settings_theme_dark,
                ).forEach { (pref, labelRes) ->
                    FilterChip(
                        selected = ui.privacy.themePreference == pref,
                        onClick = { viewModel.setPrivacy(ui.privacy.copy(themePreference = pref)) },
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }

            Text(stringResource(R.string.settings_narrative_flavor), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_narrative_flavor_blurb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("default" to R.string.settings_flavor_default, "cozy" to R.string.settings_flavor_cozy).forEach { (id, labelRes) ->
                    FilterChip(
                        selected = ui.privacy.flavorPackId == id,
                        onClick = { viewModel.setPrivacy(ui.privacy.copy(flavorPackId = id)) },
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }

            Text(stringResource(R.string.settings_companion), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_companion_blurb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ToggleRow(
                title = stringResource(R.string.settings_companion_toggle),
                checked = ui.privacy.familiarEnabled,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(familiarEnabled = it))
                },
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .alpha(if (ui.privacy.familiarEnabled) 1f else 0.45f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FamiliarSpecies.entries.forEach { species ->
                    FilterChip(
                        selected = ui.privacy.familiarSpecies == species,
                        onClick = {
                            if (ui.privacy.familiarEnabled) {
                                viewModel.setPrivacy(ui.privacy.copy(familiarSpecies = species))
                            }
                        },
                        label = { Text("${species.emoji} ${species.displayName}") },
                    )
                }
            }

            Text(stringResource(R.string.settings_health_connect), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_health_connect_blurb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ToggleRow(
                title = stringResource(R.string.settings_health_connect_toggle),
                checked = ui.privacy.healthConnectSyncEnabled,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(healthConnectSyncEnabled = it))
                },
            )
            Button(
                onClick = { viewModel.openHealthConnectSettings(context) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_health_connect_permissions))
            }

            Text(stringResource(R.string.settings_notifications), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_notifications_blurb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ToggleRow(
                title = stringResource(R.string.settings_notifications_master),
                checked = ui.privacy.remindersEnabled,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(remindersEnabled = it))
                },
            )
            ToggleRow(
                title = stringResource(R.string.settings_notify_morning),
                checked = ui.privacy.reminderMorningEnabled,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(reminderMorningEnabled = it))
                },
            )
            ToggleRow(
                title = stringResource(R.string.settings_notify_evening),
                checked = ui.privacy.reminderEveningEnabled,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(reminderEveningEnabled = it))
                },
            )
            ToggleRow(
                title = stringResource(R.string.settings_notify_boss),
                checked = ui.privacy.reminderBossEnabled,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(reminderBossEnabled = it))
                },
            )

            Text(stringResource(R.string.settings_feedback), style = MaterialTheme.typography.titleMedium)
            ToggleRow(
                title = stringResource(R.string.settings_haptics),
                checked = ui.privacy.hapticsEnabled,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(hapticsEnabled = it))
                },
            )
            ToggleRow(
                title = stringResource(R.string.settings_sound),
                checked = ui.privacy.soundEnabled,
                onCheckedChange = {
                    viewModel.setPrivacy(ui.privacy.copy(soundEnabled = it))
                },
            )

            Text(stringResource(R.string.settings_data_portability), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_backup_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = {
                    scope.launch {
                        val json = viewModel.exportJson()
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(json)
                            .setChooserTitle(context.getString(R.string.settings_chooser_export_json))
                            .startChooser()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_export_json))
            }
            Button(
                onClick = {
                    scope.launch {
                        val md = viewModel.exportMarkdownLast30Days()
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(md)
                            .setChooserTitle(context.getString(R.string.settings_chooser_export_md))
                            .startChooser()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_export_md))
            }
            Button(
                onClick = {
                    createBackupLauncher.launch("openascend-backup.json")
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_export_backup))
            }
            Button(
                onClick = {
                    openBackupLauncher.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_import_backup))
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
