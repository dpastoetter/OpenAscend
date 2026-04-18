package com.openascend.app.ui.companion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R

private val threadPathNorm = listOf(
    Offset(0.08f, 0.55f),
    Offset(0.26f, 0.42f),
    Offset(0.48f, 0.58f),
    Offset(0.7f, 0.4f),
    Offset(0.9f, 0.52f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionThreadScreen(
    onBack: () -> Unit,
    viewModel: CompanionThreadViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.companion_thread_title),
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
                        stringResource(R.string.companion_thread_theater_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }

                when (val phase = state.phase) {
                    is ThreadUiPhase.Intro -> {
                        Text(
                            stringResource(R.string.companion_thread_intro),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            stringResource(R.string.companion_thread_rules),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        Button(onClick = { viewModel.startSession() }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.companion_thread_start))
                        }
                    }
                    is ThreadUiPhase.Playing -> {
                        val pct = (phase.progress * 100f).toInt().coerceIn(0, 100)
                        Text(
                            stringResource(R.string.companion_thread_progress, pct),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            stringResource(R.string.companion_thread_drag_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        val lanePx = if (state.treatTossEasyMode) 26f else 18f
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val w = size.width.toFloat().coerceAtLeast(1f)
                                            val h = size.height.toFloat().coerceAtLeast(1f)
                                            viewModel.onStrokeStart(offset.x / w, offset.y / h)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val w = size.width.toFloat().coerceAtLeast(1f)
                                            val h = size.height.toFloat().coerceAtLeast(1f)
                                            viewModel.onStrokeMove(change.position.x / w, change.position.y / h)
                                        },
                                        onDragEnd = { viewModel.onStrokeEnd() },
                                        onDragCancel = { viewModel.onStrokeEnd() },
                                    )
                                },
                        ) {
                            Canvas(Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val p = Path()
                                threadPathNorm.forEachIndexed { i, o ->
                                    val px = o.x * w
                                    val py = o.y * h
                                    if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
                                }
                                drawPath(
                                    path = p,
                                    color = scheme.outline.copy(alpha = 0.35f),
                                    style = Stroke(width = lanePx * 2.2f, cap = StrokeCap.Round),
                                )
                                drawPath(
                                    path = p,
                                    color = scheme.primary.copy(alpha = 0.85f),
                                    style = Stroke(width = lanePx, cap = StrokeCap.Round),
                                )
                            }
                        }
                    }
                    is ThreadUiPhase.Summary -> {
                        if (phase.victory) {
                            Text(
                                stringResource(R.string.companion_thread_summary_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                stringResource(R.string.companion_thread_summary_done),
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
                                stringResource(R.string.companion_thread_slip_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                stringResource(R.string.companion_thread_slip_body),
                                style = MaterialTheme.typography.bodyMedium,
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
                            Text(stringResource(R.string.companion_thread_try_again))
                        }
                        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.companion_thread_done))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
