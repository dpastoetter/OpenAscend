package com.openascend.app.ui.companion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R
import com.openascend.domain.companion.CompanionMood
import com.openascend.domain.model.CompanionGameDifficulty
import com.openascend.domain.model.FamiliarSpecies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionFlappyScreen(
    onBack: () -> Unit,
    viewModel: CompanionFlappyViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.companion_glide_title),
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
            val theaterNote: @Composable () -> Unit = {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.surfaceContainerHighest.copy(alpha = 0.65f),
                    tonalElevation = 1.dp,
                ) {
                    Text(
                        stringResource(R.string.companion_glide_theater_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }

            when (val phase = state.phase) {
                is FlappyPhase.Playing -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { viewModel.flap() }
                            },
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Spacer(Modifier.height(4.dp))
                        theaterNote()
                        GlidePlayingBody(
                            species = state.species,
                            mood = state.companion.mood,
                            moodLabel = state.companion.moodLabel,
                            gameDifficulty = state.gameDifficulty,
                            playing = phase,
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
                is FlappyPhase.Intro -> {
                    Column(
                        Modifier
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState())
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Spacer(Modifier.height(4.dp))
                        theaterNote()
                        GlideIntroBody(
                            speciesName = state.species.displayName,
                            gameDifficulty = state.gameDifficulty,
                            onStart = { viewModel.startSession() },
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
                is FlappyPhase.Summary -> {
                    Column(
                        Modifier
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState())
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Spacer(Modifier.height(4.dp))
                        theaterNote()
                        GlideSummaryBody(
                            summary = phase,
                            gameDifficulty = state.gameDifficulty,
                            onTryAgain = { viewModel.returnToIntro() },
                            onDone = onBack,
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GlideIntroBody(
    speciesName: String,
    gameDifficulty: CompanionGameDifficulty,
    onStart: () -> Unit,
) {
    val need = CompanionFlappyViewModel.victoryThreshold(gameDifficulty)
    Text(
        stringResource(R.string.companion_glide_intro, speciesName),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        stringResource(R.string.companion_glide_rules, need),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.companion_glide_start))
    }
}

@Composable
private fun GlidePlayingBody(
    species: FamiliarSpecies,
    mood: CompanionMood,
    moodLabel: String,
    gameDifficulty: CompanionGameDifficulty,
    playing: FlappyPhase.Playing,
) {
    val scheme = MaterialTheme.colorScheme
    val skyTop = scheme.primaryContainer.copy(alpha = 0.35f)
    val skyBot = scheme.secondaryContainer.copy(alpha = 0.4f)
    val pipeColor = scheme.tertiaryContainer.copy(alpha = 0.92f)
    val victoryNeed = CompanionFlappyViewModel.victoryThreshold(gameDifficulty)

    Text(
        stringResource(R.string.companion_glide_tap_hint),
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        stringResource(R.string.companion_glide_score_line, playing.score, victoryNeed),
        style = MaterialTheme.typography.labelLarge,
        color = scheme.primary,
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
    ) {
        val w = maxWidth
        val h = maxHeight
        Canvas(Modifier.fillMaxSize()) {
            drawRect(brush = Brush.verticalGradient(listOf(skyTop, skyBot)))
            for (pipe in playing.pipes) {
                val left = pipe.x * size.width
                val pw = CompanionFlappyViewModel.PIPE_WIDTH_NORM * size.width
                val gapTop = (pipe.gapCenter - playing.openHalf) * size.height
                val gapBot = (pipe.gapCenter + playing.openHalf) * size.height
                drawRect(pipeColor, topLeft = Offset(left, 0f), size = Size(pw, gapTop.coerceAtLeast(0f)))
                drawRect(
                    pipeColor,
                    topLeft = Offset(left, gapBot),
                    size = Size(pw, (size.height - gapBot).coerceAtLeast(0f)),
                )
            }
        }
        val halfSprite = 24.dp
        val tilt = (playing.birdVy * 520f).coerceIn(-38f, 62f)
        PixelFamiliarGameSprite(
            species = species,
            mood = mood,
            contentDescription = stringResource(
                R.string.familiar_pixel_content_description,
                species.displayName,
                moodLabel,
            ),
            size = 48.dp,
            rotationDegrees = tilt,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = w * CompanionFlappyViewModel.BIRD_CENTER_X_NORM - halfSprite,
                    y = h * playing.birdY - halfSprite,
                ),
        )
    }
}

@Composable
private fun GlideSummaryBody(
    summary: FlappyPhase.Summary,
    gameDifficulty: CompanionGameDifficulty,
    onTryAgain: () -> Unit,
    onDone: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val need = CompanionFlappyViewModel.victoryThreshold(gameDifficulty)
    Text(
        stringResource(R.string.companion_glide_summary_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
    Text(
        stringResource(R.string.companion_glide_summary_score, summary.score, need),
        style = MaterialTheme.typography.bodyLarge,
    )
    when {
        summary.victory && summary.xpGranted -> Text(
            stringResource(R.string.companion_play_xp_granted, CompanionGameXp.SHARED_DAILY_XP),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.primary,
        )
        summary.victory && summary.xpAlreadyClaimedToday -> Text(
            stringResource(R.string.companion_play_xp_already_today),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
        !summary.victory -> Text(
            stringResource(R.string.companion_glide_summary_retry_hint, need),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
    Text(
        stringResource(R.string.companion_play_disclaimer),
        style = MaterialTheme.typography.bodySmall,
        color = scheme.onSurfaceVariant,
    )
    FilledTonalButton(onClick = onTryAgain, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.companion_glide_try_again))
    }
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.companion_glide_done))
    }
}
