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
fun CompanionMimicScreen(
    onBack: () -> Unit,
    viewModel: CompanionMimicViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.companion_mimic_title),
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
            val totalRounds = CompanionMimicViewModel.roundsTotal(state.treatTossEasyMode)
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
                        stringResource(R.string.companion_mimic_theater_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }

                when (val phase = state.phase) {
                    is MimicUiPhase.Intro -> {
                        Text(
                            stringResource(R.string.companion_mimic_intro, state.species.displayName),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            stringResource(R.string.companion_mimic_rules),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        Button(onClick = { viewModel.startSession() }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.companion_mimic_start))
                        }
                    }
                    is MimicUiPhase.ShowFirst -> {
                        Text(
                            stringResource(R.string.companion_mimic_round, phase.roundIndex, totalRounds),
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.primary,
                        )
                        Text(
                            stringResource(R.string.companion_mimic_see_first),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            stringResource(R.string.companion_mimic_score_running, phase.runningScore),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(phase.firstSigil, fontSize = 72.sp, textAlign = TextAlign.Center)
                        }
                    }
                    is MimicUiPhase.Pick -> {
                        Text(
                            stringResource(R.string.companion_mimic_round, phase.roundIndex, totalRounds),
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.primary,
                        )
                        Text(
                            stringResource(R.string.companion_mimic_second_prompt),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            stringResource(R.string.companion_mimic_score_running, phase.runningScore),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        Text(
                            phase.firstSigil,
                            fontSize = 36.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text("→", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        Text(
                            phase.secondSigil,
                            fontSize = 72.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            FilledTonalButton(
                                onClick = { viewModel.onChoice(saidSame = true) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.companion_mimic_same))
                            }
                            FilledTonalButton(
                                onClick = { viewModel.onChoice(saidSame = false) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.companion_mimic_different))
                            }
                        }
                    }
                    is MimicUiPhase.RoundFeedback -> {
                        val title = if (phase.correct) {
                            stringResource(R.string.companion_mimic_correct_title)
                        } else {
                            stringResource(R.string.companion_mimic_wrong_title)
                        }
                        val body = if (phase.correct) {
                            stringResource(R.string.companion_mimic_correct_body)
                        } else {
                            stringResource(R.string.companion_mimic_wrong_body)
                        }
                        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(body, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            stringResource(R.string.companion_mimic_score_running, phase.runningScore),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        val nextLabel = if (phase.roundIndex >= totalRounds) {
                            stringResource(R.string.companion_mimic_see_summary)
                        } else {
                            stringResource(R.string.companion_mimic_next_round)
                        }
                        Button(onClick = { viewModel.continueAfterRound() }, modifier = Modifier.fillMaxWidth()) {
                            Text(nextLabel)
                        }
                    }
                    is MimicUiPhase.Summary -> {
                        Text(
                            stringResource(R.string.companion_mimic_summary_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.companion_mimic_summary_score, phase.totalPoints),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        when {
                            phase.xpGranted -> Text(
                                stringResource(R.string.companion_play_xp_granted, CompanionGameXp.SHARED_DAILY_XP),
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.primary,
                            )
                            phase.xpAlreadyClaimedToday -> Text(
                                stringResource(R.string.companion_play_xp_already_today),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            stringResource(R.string.companion_play_disclaimer),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.companion_mimic_done))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
