package com.openascend.app.ui.checkin

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    var sleep by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var vitality by remember { mutableStateOf("") }

    LaunchedEffect(ui.sleepHours, ui.steps, ui.bankControl, ui.moneyNote, ui.vitality) {
        sleep = ui.sleepHours
        steps = ui.steps
        bank = ui.bankControl
        note = ui.moneyNote
        vitality = ui.vitality
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evening check-in") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Manual inputs only in this MVP. Numbers are a vibe check, not advice.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(sleep, { sleep = it }, label = { Text("Sleep (hours)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(steps, { steps = it }, label = { Text("Steps") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(bank, { bank = it }, label = { Text("Money calm (1-10)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(note, { note = it }, label = { Text("One-line money note") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(vitality, { vitality = it }, label = { Text("Vitality check (1-10, optional)") }, modifier = Modifier.fillMaxWidth())

            Text("Habits today", style = MaterialTheme.typography.titleMedium)
            ui.habits.forEach { h ->
                val checked = ui.completions[h.id] == true
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { viewModel.toggleHabit(h.id, it) },
                    )
                    Text(h.name)
                }
            }

            TextButton(
                onClick = {
                    viewModel.save(sleep, steps, bank, note, vitality)
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Seal today's run")
            }
        }
    }
}
