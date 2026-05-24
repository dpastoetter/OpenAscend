package com.openascend.app.ui.companion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Pause
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionStackScreen(
    onBack: () -> Unit,
    viewModel: CompanionStackViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.companion_stack_title),
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
            val goal = CompanionStackViewModel.victoryHeight(state.gameDifficulty)
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
                        stringResource(R.string.companion_stack_theater_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }

                when (val phase = state.phase) {
                    is StackUiPhase.Intro -> {
                        Text(
                            stringResource(R.string.companion_stack_intro, state.species.displayName),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            stringResource(R.string.companion_stack_rules, goal),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        Button(onClick = { viewModel.startSession() }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.companion_stack_start))
                        }
                    }
                    is StackUiPhase.Playing, is StackUiPhase.Paused -> {
                        val play = when (phase) {
                            is StackUiPhase.Playing -> phase
                            is StackUiPhase.Paused -> phase.playing
                            else -> error("unreachable")
                        }
                        val paused = phase is StackUiPhase.Paused
                        Text(
                            stringResource(R.string.companion_stack_height, play.stackHeight, goal),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            stringResource(R.string.companion_stack_tap_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!paused) {
                                OutlinedButton(onClick = { viewModel.pause() }) {
                                    Icon(Icons.Outlined.Pause, contentDescription = stringResource(R.string.cd_pause))
                                    Text(stringResource(R.string.companion_glide_paused_title))
                                }
                            } else {
                                Button(onClick = { viewModel.resume() }) {
                                    Text(stringResource(R.string.companion_glide_resume))
                                }
                            }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .pointerInput(paused) {
                                    if (!paused) {
                                        detectTapGestures(onTap = { viewModel.tryDrop() })
                                    }
                                },
                        ) {
                            Canvas(Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val baseY = h * 0.82f
                                val halfPx = play.landHalfWidth * w
                                drawRoundRect(
                                    color = scheme.primary.copy(alpha = 0.35f),
                                    topLeft = Offset(size.width / 2f - halfPx, baseY - 10f),
                                    size = Size(halfPx * 2f, 18f),
                                    cornerRadius = CornerRadius(6f, 6f),
                                )
                                val stackW = (24f + play.stackHeight * 6f).coerceAtMost(w * 0.35f)
                                drawRoundRect(
                                    color = scheme.secondary.copy(alpha = 0.5f),
                                    topLeft = Offset(size.width / 2f - stackW / 2f, baseY - 28f - play.stackHeight * 10f),
                                    size = Size(stackW, 28f + play.stackHeight * 10f),
                                    cornerRadius = CornerRadius(8f, 8f),
                                )
                                val cx = play.cursorX * w
                                drawCircle(
                                    color = scheme.tertiary,
                                    radius = 14f,
                                    center = Offset(cx, h * 0.28f),
                                )
                            }
                            if (paused) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(scheme.scrim.copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        stringResource(R.string.companion_glide_paused_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = scheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                    is StackUiPhase.Summary -> {
                        if (phase.victory) {
                            Text(
                                stringResource(R.string.companion_stack_summary_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                stringResource(R.string.companion_stack_summary_height, phase.stackHeight, goal),
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
                        } else {
                            Text(
                                stringResource(R.string.companion_stack_miss_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                stringResource(R.string.companion_stack_miss_body),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                stringResource(R.string.companion_stack_summary_retry_hint, goal),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            stringResource(R.string.companion_play_disclaimer),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        FilledTonalButton(
                            onClick = { viewModel.returnToIntro() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.companion_stack_try_again))
                        }
                        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.companion_stack_done))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
