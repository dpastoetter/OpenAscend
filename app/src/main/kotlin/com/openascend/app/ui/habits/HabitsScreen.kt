package com.openascend.app.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.domain.model.CoreStat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    onBack: () -> Unit,
    onEditHabit: (Long) -> Unit,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit quests") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add habit")
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text("Linked stats power your Discipline roll when you complete them today.")
            }
            items(ui.habits, key = { it.id }) { habit ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(habit.name)
                        Text("${habit.frequencyPerWeek}x / week · diff ${habit.difficulty} · ${habit.linkedStat.name}")
                        TextButton(onClick = { onEditHabit(habit.id) }) { Text("Refine") }
                        TextButton(onClick = { viewModel.deleteHabit(habit.id) }) { Text("Retire quest") }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var perWeek by remember { mutableIntStateOf(5) }
        var difficulty by remember { mutableIntStateOf(2) }
        var stat by remember { mutableStateOf(CoreStat.DISCIPLINE) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addHabit(name, perWeek, difficulty, stat)
                        showDialog = false
                    },
                    enabled = name.isNotBlank(),
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
            title = { Text("New habit quest") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Name") })
                    OutlinedTextField(perWeek.toString(), { perWeek = it.toIntOrNull() ?: perWeek }, label = { Text("Times / week (1-7)") })
                    OutlinedTextField(difficulty.toString(), { difficulty = it.toIntOrNull() ?: difficulty }, label = { Text("Difficulty 1-5") })
                    Text("Linked stat: ${stat.name}")
                    CoreStat.entries.forEach { s ->
                        TextButton(onClick = { stat = s }) { Text(s.name) }
                    }
                }
            },
        )
    }
}
