package com.openascend.app.ui.duel

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
import com.openascend.app.share.ShareLauncher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronicleDuelScreen(
    onBack: () -> Unit,
    viewModel: ChronicleDuelViewModel = hiltViewModel(),
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
            val ok = text != null && viewModel.importDuelJson(text)
            Toast.makeText(
                context,
                context.getString(
                    if (ok) R.string.chronicle_duel_import_success else R.string.chronicle_duel_import_error,
                ),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val json = viewModel.exportDuelJson()
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chronicle_duel_title)) },
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
                stringResource(R.string.chronicle_duel_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DuelColumn(stringResource(R.string.chronicle_duel_you), ui.you)
            ui.them?.let { DuelColumn(stringResource(R.string.chronicle_duel_them), it) }
            Button(
                onClick = {
                    scope.launch {
                        val json = viewModel.exportDuelJson()
                        ShareLauncher.shareText(
                            context,
                            json,
                            "application/json",
                            context.getString(R.string.chronicle_duel_share_chooser),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.chronicle_duel_share))
            }
            Button(
                onClick = { exportLauncher.launch("openascend_duel.json") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.chronicle_duel_export))
            }
            Button(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.chronicle_duel_import))
            }
        }
    }
}

@Composable
private fun DuelColumn(label: String, summary: com.openascend.data.export.ChronicleDuelSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Text("${summary.displayName} · L${summary.level}")
        Text(
            "R${summary.recovery} S${summary.stamina} St${summary.stability} " +
                "D${summary.discipline} V${summary.vitality}",
        )
        Text("Boss · ${summary.bossName}")
    }
}
