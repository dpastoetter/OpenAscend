package com.openascend.app.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.domain.model.CoreStat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: HabitEditViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var perWeek by remember { mutableIntStateOf(5) }
    var difficulty by remember { mutableIntStateOf(2) }
    var stat by remember { mutableStateOf(CoreStat.DISCIPLINE) }
    var restDay by remember { mutableStateOf(false) }
    var bossPrep by remember { mutableStateOf(false) }

    LaunchedEffect(ui) {
        val ready = ui as? HabitEditUi.Ready ?: return@LaunchedEffect
        name = ready.habit.name
        perWeek = ready.habit.frequencyPerWeek
        difficulty = ready.habit.difficulty
        stat = ready.habit.linkedStat
        restDay = ready.habit.isRestDay
        bossPrep = ready.habit.bossPrep
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refine habit quest") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when (ui) {
            HabitEditUi.Loading -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            HabitEditUi.NotFound -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("This quest is gone or unknown.", style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Go back") }
            }
            is HabitEditUi.Ready -> Column(
                Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    perWeek.toString(),
                    { perWeek = it.toIntOrNull() ?: perWeek },
                    label = { Text("Times / week (1-7)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    difficulty.toString(),
                    { difficulty = it.toIntOrNull() ?: difficulty },
                    label = { Text("Difficulty 1-5") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Linked stat: ${stat.name}", style = MaterialTheme.typography.labelLarge)
                CoreStat.entries.forEach { s ->
                    TextButton(onClick = { stat = s }) { Text(s.name) }
                }
                FilterChip(
                    selected = restDay,
                    onClick = { restDay = !restDay },
                    label = { Text("Sacred rest day") },
                )
                FilterChip(
                    selected = bossPrep,
                    onClick = { bossPrep = !bossPrep },
                    label = { Text("Boss prep (weekly encounter)") },
                )
                Button(
                    onClick = {
                        viewModel.save(name, perWeek, difficulty, stat, restDay, bossPrep)
                        onSaved()
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save changes")
                }
            }
        }
    }
}
