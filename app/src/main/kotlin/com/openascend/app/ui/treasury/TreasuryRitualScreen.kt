package com.openascend.app.ui.treasury

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasuryRitualScreen(
    onBack: () -> Unit,
    viewModel: TreasuryRitualViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var note by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.treasury_ritual_title)) },
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
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.treasury_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(ui.prompts.intro, style = MaterialTheme.typography.bodyMedium)
            if (ui.saved) {
                Text(
                    stringResource(R.string.treasury_ritual_saved),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            OutlinedTextField(
                note,
                { note = it },
                label = { Text(stringResource(R.string.treasury_note_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { viewModel.save(TreasuryChoice.Win, note) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(ui.prompts.winLabel) }
            Button(
                onClick = { viewModel.save(TreasuryChoice.Leak, note) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(ui.prompts.leakLabel) }
            Button(
                onClick = { viewModel.save(TreasuryChoice.Intention, note) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(ui.prompts.intentionLabel) }
        }
    }
}
