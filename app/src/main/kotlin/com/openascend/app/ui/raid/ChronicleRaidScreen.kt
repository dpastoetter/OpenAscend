package com.openascend.app.ui.raid

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R
import com.openascend.app.share.RaidShareCardUi
import com.openascend.app.share.ShareCardCopy
import com.openascend.app.share.ShareLauncher
import com.openascend.app.share.captureRaidShareCardBitmap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronicleRaidScreen(
    onBack: () -> Unit,
    viewModel: ChronicleRaidViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            }.getOrNull()
            if (text != null) {
                viewModel.importMemberJson(text)
            } else {
                Toast.makeText(context, R.string.chronicle_raid_import_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chronicle_raid_title)) },
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
        val ui = state ?: return@Scaffold
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.chronicle_raid_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ui.members.forEach { member ->
                Text("${member.displayName} · R${member.recovery} S${member.stamina} St${member.stability}")
            }
            Button(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = ui.members.size < 4,
            ) {
                Text(stringResource(R.string.chronicle_raid_import_member))
            }
            Button(
                onClick = {
                    val json = viewModel.exportHostJson()
                    ShareLauncher.shareText(
                        context,
                        json,
                        "application/json",
                        context.getString(R.string.chronicle_raid_share_host),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.chronicle_raid_export_host))
            }
            ui.result?.let { result ->
                Text(
                    if (result.success) {
                        stringResource(R.string.chronicle_raid_success, result.bossName)
                    } else {
                        stringResource(R.string.chronicle_raid_fail, result.bossName)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(
                        R.string.chronicle_raid_power,
                        result.teamPower,
                        result.threshold,
                    ),
                )
                Button(
                    onClick = {
                        scope.launch {
                            val names = ui.members.joinToString(", ") { it.displayName }
                            val payload = RaidShareCardUi(
                                partyNames = names,
                                bossName = result.bossName,
                                outcomeLine = if (result.success) {
                                    context.getString(R.string.chronicle_raid_success, result.bossName)
                                } else {
                                    context.getString(R.string.chronicle_raid_fail, result.bossName)
                                },
                                powerLine = context.getString(
                                    R.string.chronicle_raid_power,
                                    result.teamPower,
                                    result.threshold,
                                ),
                                tagline = ShareCardCopy.tagline(context),
                            )
                            val bitmap = captureRaidShareCardBitmap(context, payload)
                            ShareLauncher.shareBitmap(
                                context,
                                bitmap,
                                context.getString(R.string.chronicle_raid_share_card),
                                context.getString(R.string.chronicle_raid_share_text, names),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.chronicle_raid_share_card))
                }
            }
        }
    }
}
