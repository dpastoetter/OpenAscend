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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R
import kotlinx.coroutines.delay

private val moodOptionIds = listOf("steady", "scattered", "heavy", "bright")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onNavigateToSigil: () -> Unit,
    onOpenSettings: () -> Unit = {},
    viewModel: CheckInViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val snack = remember { SnackbarHostState() }
    var sleep by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var vitality by remember { mutableStateOf("") }
    var selectedMoods by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        viewModel.saveEffects.collect { effect ->
            snack.showSnackbar(effect.snackbarMessage)
            if (effect.offerSigilRitual) {
                delay(750)
                onNavigateToSigil()
            } else {
                onSaved()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.bossPrepLore.collect { msg ->
            msg?.let { snack.showSnackbar(it) }
        }
    }

    LaunchedEffect(ui.sleepHours, ui.steps, ui.bankControl, ui.moneyNote, ui.vitality) {
        sleep = ui.sleepHours
        steps = ui.steps
        bank = ui.bankControl
        note = ui.moneyNote
        vitality = ui.vitality
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.checkin_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
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
                stringResource(R.string.checkin_blurb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!ui.healthConnectEnabled) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.checkin_hc_invite_title),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            stringResource(R.string.checkin_hc_invite_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = onOpenSettings) {
                            Text(stringResource(R.string.checkin_hc_open_settings))
                        }
                    }
                }
            }
            OutlinedTextField(
                sleep,
                { sleep = it },
                label = { Text(stringResource(R.string.checkin_sleep)) },
                supportingText = if (ui.sleepFromHealthConnect) {
                    { Text(stringResource(R.string.checkin_hc_badge)) }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                steps,
                { steps = it },
                label = { Text(stringResource(R.string.checkin_steps)) },
                supportingText = if (ui.stepsFromHealthConnect) {
                    { Text(stringResource(R.string.checkin_hc_badge)) }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                bank,
                { bank = it },
                label = { Text(stringResource(R.string.checkin_bank)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                note,
                { note = it },
                label = { Text(stringResource(R.string.checkin_money_note)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                vitality,
                { vitality = it },
                label = { Text(stringResource(R.string.checkin_vitality)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(stringResource(R.string.checkin_habits_today), style = MaterialTheme.typography.titleMedium)
            ui.habits.forEach { h ->
                val checked = ui.completions[h.id] == true
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { viewModel.toggleHabit(h.id, it) },
                    )
                    Column {
                        Text(h.name)
                        if (h.isRestDay) {
                            Text(
                                stringResource(R.string.checkin_rest_day_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Text(stringResource(R.string.checkin_mood_title), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.checkin_mood_blurb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                moodOptionIds.forEach { id ->
                    val label = when (id) {
                        "steady" -> stringResource(R.string.mood_steady)
                        "scattered" -> stringResource(R.string.mood_scattered)
                        "heavy" -> stringResource(R.string.mood_heavy)
                        else -> stringResource(R.string.mood_bright)
                    }
                    FilterChip(
                        selected = selectedMoods.contains(id),
                        onClick = {
                            selectedMoods = if (selectedMoods.contains(id)) {
                                selectedMoods - id
                            } else {
                                selectedMoods + id
                            }
                        },
                        label = { Text(label) },
                    )
                }
            }

            TextButton(
                onClick = {
                    viewModel.save(sleep, steps, bank, note, vitality, selectedMoods.toList())
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.checkin_seal))
            }
        }
    }
}
