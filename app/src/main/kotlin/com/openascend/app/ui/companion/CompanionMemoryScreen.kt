package com.openascend.app.ui.companion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionMemoryScreen(
    onBack: () -> Unit,
    viewModel: CompanionMemoryViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.companion_memory_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = scheme.surfaceContainer,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (ui == null) {
                Column(
                    Modifier
                        .padding(24.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        stringResource(R.string.companion_play_unavailable),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    OutlinedButton(onClick = onBack) { Text(stringResource(R.string.action_go_back)) }
                }
                return@Box
            }

            val state = ui!!
            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.surfaceContainerHighest.copy(alpha = 0.65f),
                    tonalElevation = 1.dp,
                ) {
                    Text(
                        stringResource(R.string.companion_memory_theater_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }

                when (val phase = state.phase) {
                    is MemoryUiPhase.Intro -> MemoryIntroBody(
                        speciesName = state.species.displayName,
                        onStart = { viewModel.startSession() },
                    )
                    is MemoryUiPhase.Flash -> MemoryFlashBody(
                        roundIndex = phase.roundIndex,
                        sigil = phase.targetSigil,
                    )
                    is MemoryUiPhase.Pick -> MemoryPickBody(
                        roundIndex = phase.roundIndex,
                        choices = phase.choices,
                        runningScore = phase.runningScore,
                        onPick = { viewModel.onSigilPicked(it) },
                    )
                    is MemoryUiPhase.RoundFeedback -> MemoryFeedbackBody(
                        roundIndex = phase.roundIndex,
                        correct = phase.correct,
                        runningScore = phase.runningScore,
                        onNext = { viewModel.continueAfterRound() },
                    )
                    is MemoryUiPhase.Summary -> MemorySummaryBody(
                        totalPoints = phase.totalPoints,
                        xpGranted = phase.xpGranted,
                        xpAlreadyToday = phase.xpAlreadyClaimedToday,
                        onDone = onBack,
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MemoryIntroBody(
    speciesName: String,
    onStart: () -> Unit,
) {
    Text(
        stringResource(R.string.companion_memory_intro, speciesName),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        stringResource(R.string.companion_memory_rules),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.companion_memory_start))
    }
}

@Composable
private fun MemoryFlashBody(
    roundIndex: Int,
    sigil: String,
) {
    Text(
        stringResource(R.string.companion_memory_round, roundIndex, CompanionMemoryViewModel.ROUNDS_TOTAL),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        stringResource(R.string.companion_memory_flash_hint),
        style = MaterialTheme.typography.bodyMedium,
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(sigil, fontSize = 72.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun MemoryPickBody(
    roundIndex: Int,
    choices: List<String>,
    runningScore: Int,
    onPick: (String) -> Unit,
) {
    Text(
        stringResource(R.string.companion_memory_round, roundIndex, CompanionMemoryViewModel.ROUNDS_TOTAL),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        stringResource(R.string.companion_memory_pick_prompt),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        stringResource(R.string.companion_memory_score_running, runningScore),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        choices.forEach { sigil ->
            FilledTonalButton(onClick = { onPick(sigil) }) {
                Text(sigil, fontSize = 28.sp)
            }
        }
    }
}

@Composable
private fun MemoryFeedbackBody(
    roundIndex: Int,
    correct: Boolean,
    runningScore: Int,
    onNext: () -> Unit,
) {
    val title = if (correct) {
        stringResource(R.string.companion_memory_correct_title)
    } else {
        stringResource(R.string.companion_memory_wrong_title)
    }
    val body = if (correct) {
        stringResource(R.string.companion_memory_correct_body)
    } else {
        stringResource(R.string.companion_memory_wrong_body)
    }
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text(body, style = MaterialTheme.typography.bodyMedium)
    Text(
        stringResource(R.string.companion_memory_score_running, runningScore),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val nextLabel = if (roundIndex >= CompanionMemoryViewModel.ROUNDS_TOTAL) {
        stringResource(R.string.companion_memory_see_summary)
    } else {
        stringResource(R.string.companion_memory_next_round)
    }
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text(nextLabel)
    }
}

@Composable
private fun MemorySummaryBody(
    totalPoints: Int,
    xpGranted: Boolean,
    xpAlreadyToday: Boolean,
    onDone: () -> Unit,
) {
    Text(
        stringResource(R.string.companion_memory_summary_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
    Text(
        stringResource(R.string.companion_memory_summary_score, totalPoints),
        style = MaterialTheme.typography.bodyLarge,
    )
    when {
        xpGranted -> Text(
            stringResource(R.string.companion_play_xp_granted, CompanionPlayViewModel.TREAT_TOSS_XP),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        xpAlreadyToday -> Text(
            stringResource(R.string.companion_play_xp_already_today),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Text(
        stringResource(R.string.companion_play_disclaimer),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.companion_memory_done))
    }
}
