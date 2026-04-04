package com.openascend.app.ui.companion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R
import com.openascend.domain.companion.CompanionMood
import com.openascend.domain.companion.TreatTossTiming
import com.openascend.domain.model.FamiliarSpecies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionPlayScreen(
    onBack: () -> Unit,
    viewModel: CompanionPlayViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val needle by viewModel.needle.collectAsState()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.companion_play_title),
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
            TreatTossStage(scheme = scheme)
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                scheme.surface.copy(alpha = 0.72f),
                                scheme.surface.copy(alpha = 0.92f),
                            ),
                        ),
                    ),
            )
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
                        stringResource(R.string.companion_play_theater_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }

                when (val phase = state.phase) {
                    is TreatUiPhase.Intro ->
                        IntroBody(
                            species = state.species,
                            mood = state.companion.mood,
                            onStart = { viewModel.startSession() },
                        )
                    is TreatUiPhase.Playing ->
                        PlayingBody(
                            roundIndex = phase.roundIndex,
                            species = state.species,
                            mood = state.companion.mood,
                            treatTossEasyMode = state.treatTossEasyMode,
                            needle = needle,
                            onToss = { viewModel.onTossTap() },
                        )
                    is TreatUiPhase.RoundResult ->
                        RoundResultBody(
                            species = state.species,
                            mood = state.companion.mood,
                            quality = phase.quality,
                            runningTotal = phase.runningTotal,
                            roundIndex = phase.roundIndex,
                            onNext = { viewModel.continueAfterRound() },
                        )
                    is TreatUiPhase.Summary ->
                        SummaryBody(
                            totalPoints = phase.totalPoints,
                            xpGranted = phase.xpGranted,
                            xpAlreadyClaimedToday = phase.xpAlreadyClaimedToday,
                            mood = state.companion.mood,
                            species = state.species,
                            onDone = onBack,
                        )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun IntroBody(
    species: FamiliarSpecies,
    mood: CompanionMood,
    onStart: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerHigh),
    ) {
        Column(
            Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(species.emoji, style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(R.string.companion_play_intro, species.displayName),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            val spriteDesc = stringResource(
                R.string.familiar_pixel_content_description,
                species.displayName,
                stringResource(moodLabelRes(mood)),
            )
            PixelFamiliar(
                species = species,
                mood = mood,
                contentDescription = spriteDesc,
            )
            Text(
                stringResource(R.string.companion_play_rules),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            FilledTonalButton(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Outlined.TrackChanges, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.companion_play_start))
            }
        }
    }
}

@Composable
private fun TreatTossStage(scheme: ColorScheme) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        scheme.primary.copy(alpha = 0.22f),
                        scheme.tertiary.copy(alpha = 0.18f),
                        scheme.secondary.copy(alpha = 0.12f),
                    ),
                ),
                size = size,
            )
            val starSeed = 42
            repeat(28) { i ->
                val sx = ((i * 47 + starSeed) % 1000) / 1000f * size.width
                val sy = ((i * 89 + starSeed) % 700) / 1000f * size.height * 0.55f
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f + (i % 3) * 0.08f),
                    radius = 1.2f + (i % 2),
                    center = Offset(sx, sy),
                )
            }
            val groundTop = size.height * 0.58f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        scheme.primary.copy(alpha = 0.45f),
                        scheme.scrim.copy(alpha = 0.55f),
                    ),
                    startY = groundTop,
                    endY = size.height,
                ),
                topLeft = Offset(0f, groundTop),
                size = Size(size.width, size.height - groundTop),
            )
            val hill = Path().apply {
                moveTo(0f, groundTop + size.height * 0.08f)
                quadraticTo(
                    size.width * 0.35f,
                    groundTop - size.height * 0.06f,
                    size.width * 0.55f,
                    groundTop + size.height * 0.04f,
                )
                quadraticTo(
                    size.width * 0.8f,
                    groundTop + size.height * 0.12f,
                    size.width,
                    groundTop + size.height * 0.06f,
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                hill,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        scheme.tertiary.copy(alpha = 0.35f),
                        scheme.primary.copy(alpha = 0.25f),
                    ),
                ),
            )
        }
    }
}

@Composable
private fun PlayingBody(
    roundIndex: Int,
    species: FamiliarSpecies,
    mood: CompanionMood,
    treatTossEasyMode: Boolean,
    needle: Float,
    onToss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val progress = roundIndex.toFloat() / CompanionPlayViewModel.ROUNDS_TOTAL.toFloat()
    val bands = TreatTossTiming.scoreBands(mood, treatTossEasyMode)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = scheme.primary,
            trackColor = scheme.surfaceContainerHighest,
        )
        Text(
            stringResource(R.string.companion_play_round, roundIndex, CompanionPlayViewModel.ROUNDS_TOTAL),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
        )
        Text(
            stringResource(R.string.companion_play_toss_hint, species.displayName),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val spriteDesc = stringResource(
                    R.string.familiar_pixel_content_description,
                    species.displayName,
                    stringResource(moodLabelRes(mood)),
                )
                PixelFamiliar(species = species, mood = mood, contentDescription = spriteDesc)
                Text(
                    stringResource(R.string.companion_play_ready_catch),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant,
                )
            }
        }

        TossMeter(needle = needle, bands = bands)

        val pulse = rememberInfiniteTransition(label = "toss_pulse")
        val scale by pulse.animateFloat(
            initialValue = 1f,
            targetValue = 1.035f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "scale",
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(onClick = onToss),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = scheme.primaryContainer,
            ),
            border = BorderStroke(2.dp, scheme.primary.copy(alpha = 0.45f)),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.companion_play_tap_toss),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                    Text(stringResource(R.string.companion_play_tap_sub), style = MaterialTheme.typography.bodySmall, color = scheme.onPrimaryContainer.copy(alpha = 0.85f), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun TossMeter(needle: Float, bands: TreatTossTiming.Bands) {
    val scheme = MaterialTheme.colorScheme
    val bad = scheme.error
    val warm = scheme.tertiary
    val good = scheme.primary
    val goodGlow = scheme.tertiary.copy(alpha = 0.55f)
    val shape = RoundedCornerShape(18.dp)
    val okLo = bands.okLo
    val okHi = bands.okHi
    val gLo = bands.greatLo
    val gHi = bands.greatHi

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .background(scheme.outline.copy(alpha = 0.12f))
            .padding(3.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(15.dp)),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val radius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to bad.copy(alpha = 0.9f),
                            okLo to warm.copy(alpha = 0.85f),
                            gLo to good.copy(alpha = 0.95f),
                            gHi to good.copy(alpha = 0.95f),
                            okHi to warm.copy(alpha = 0.85f),
                            1f to bad.copy(alpha = 0.9f),
                        ),
                        startX = 0f,
                        endX = w,
                    ),
                    size = size,
                    cornerRadius = radius,
                )
                val gx0 = gLo * w
                val gx1 = gHi * w
                drawRoundRect(
                    color = goodGlow,
                    topLeft = Offset(gx0, 0f),
                    size = Size(gx1 - gx0, h),
                    cornerRadius = CornerRadius(6f, 6f),
                )
                listOf(okLo, gLo, gHi, okHi).distinct().sorted().forEach { t ->
                    val x = t * w
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(x, h * 0.15f),
                        end = Offset(x, h * 0.85f),
                        strokeWidth = 2f,
                    )
                }
                val nx = needle.coerceIn(0f, 1f) * w
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.25f),
                    topLeft = Offset(nx - 5f, 4f),
                    size = Size(10f, h - 2f),
                    cornerRadius = CornerRadius(3f, 3f),
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(nx - 4f, 2f),
                    size = Size(8f, h + 4f),
                    cornerRadius = CornerRadius(3f, 3f),
                )
                drawRoundRect(
                    color = scheme.onSurface,
                    topLeft = Offset(nx - 3f, 3f),
                    size = Size(6f, h - 2f),
                    cornerRadius = CornerRadius(2f, 2f),
                )
            }
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(R.string.companion_play_meter_miss), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        Text(stringResource(R.string.companion_play_meter_sweet), style = MaterialTheme.typography.labelSmall, color = scheme.primary, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.companion_play_meter_miss), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
    }
}

@Composable
private fun RoundResultBody(
    species: FamiliarSpecies,
    mood: CompanionMood,
    quality: TossQuality,
    runningTotal: Int,
    roundIndex: Int,
    onNext: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val (cardColor, accent) = when (quality) {
        TossQuality.GREAT -> scheme.primaryContainer to scheme.primary
        TossQuality.OK -> scheme.secondaryContainer to scheme.secondary
        TossQuality.MISS -> scheme.surfaceContainerHighest to scheme.outline
    }
    val title = when (quality) {
        TossQuality.GREAT -> stringResource(R.string.companion_treat_quality_great)
        TossQuality.OK -> stringResource(R.string.companion_treat_quality_ok)
        TossQuality.MISS -> stringResource(R.string.companion_treat_quality_miss)
    }

    key(roundIndex, quality) {
        AnimatedVisibility(visible = true, enter = fadeIn(tween(220)) + scaleIn(tween(280), initialScale = 0.92f)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            ) {
                Column(
                    Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (quality == TossQuality.GREAT) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = accent, modifier = Modifier.size(40.dp))
                    } else {
                        Text(text = species.emoji, style = MaterialTheme.typography.displaySmall)
                    }
                    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent, textAlign = TextAlign.Center)
                    Text(catchLine(species, quality), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    val spriteDesc = stringResource(
                        R.string.familiar_pixel_content_description,
                        species.displayName,
                        stringResource(moodLabelRes(mood)),
                    )
                    PixelFamiliar(species = species, mood = mood, contentDescription = spriteDesc)
                    Surface(shape = RoundedCornerShape(12.dp), color = scheme.surface.copy(alpha = 0.55f)) {
                        Text(
                            stringResource(R.string.companion_play_score_running, runningTotal),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                    val nextLabel =
                        if (roundIndex >= CompanionPlayViewModel.ROUNDS_TOTAL) {
                            stringResource(R.string.companion_play_see_summary)
                        } else {
                            stringResource(R.string.companion_play_next_toss)
                        }
                    Button(onClick = onNext, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Text(nextLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun catchLine(
    species: FamiliarSpecies,
    quality: TossQuality,
): String {
    val res =
        when (species) {
            FamiliarSpecies.BEAR ->
                when (quality) {
                    TossQuality.GREAT -> R.string.companion_treat_catch_bear_great
                    TossQuality.OK -> R.string.companion_treat_catch_bear_ok
                    TossQuality.MISS -> R.string.companion_treat_catch_bear_miss
                }
            FamiliarSpecies.WOLF ->
                when (quality) {
                    TossQuality.GREAT -> R.string.companion_treat_catch_wolf_great
                    TossQuality.OK -> R.string.companion_treat_catch_wolf_ok
                    TossQuality.MISS -> R.string.companion_treat_catch_wolf_miss
                }
            FamiliarSpecies.DRAGON ->
                when (quality) {
                    TossQuality.GREAT -> R.string.companion_treat_catch_dragon_great
                    TossQuality.OK -> R.string.companion_treat_catch_dragon_ok
                    TossQuality.MISS -> R.string.companion_treat_catch_dragon_miss
                }
        }
    return stringResource(res)
}

@Composable
private fun SummaryBody(
    totalPoints: Int,
    xpGranted: Boolean,
    xpAlreadyClaimedToday: Boolean,
    mood: CompanionMood,
    species: FamiliarSpecies,
    onDone: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.companion_play_summary_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val spriteDesc = stringResource(
                R.string.familiar_pixel_content_description,
                species.displayName,
                stringResource(moodLabelRes(mood)),
            )
            PixelFamiliar(species = species, mood = mood, contentDescription = spriteDesc)
            Text(
                stringResource(R.string.companion_play_summary_score, totalPoints),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            Text(
                moodSummaryLine(mood, species),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            when {
                xpGranted ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = scheme.primaryContainer,
                    ) {
                        Text(
                            stringResource(R.string.companion_play_xp_granted, CompanionPlayViewModel.TREAT_TOSS_XP),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                xpAlreadyClaimedToday ->
                    Text(
                        stringResource(R.string.companion_play_xp_already_today),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
            }
            Text(
                stringResource(R.string.companion_play_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            FilledTonalButton(onClick = onDone, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text(stringResource(R.string.companion_play_done))
            }
        }
    }
}

@Composable
private fun moodSummaryLine(
    mood: CompanionMood,
    species: FamiliarSpecies,
): String {
    val moodRes =
        when (mood) {
            CompanionMood.SPARKLING -> R.string.companion_treat_summary_sparkling
            CompanionMood.COZY -> R.string.companion_treat_summary_cozy
            CompanionMood.WATCHING -> R.string.companion_treat_summary_watching
            CompanionMood.FADING -> R.string.companion_treat_summary_fading
            CompanionMood.CURIOUS -> R.string.companion_treat_summary_curious
            CompanionMood.DORMANT -> R.string.companion_treat_summary_dormant
        }
    return stringResource(moodRes, species.displayName)
}

private fun moodLabelRes(mood: CompanionMood): Int =
    when (mood) {
        CompanionMood.SPARKLING -> R.string.mood_label_sparkling
        CompanionMood.COZY -> R.string.mood_label_cozy
        CompanionMood.WATCHING -> R.string.mood_label_watching
        CompanionMood.FADING -> R.string.mood_label_fading
        CompanionMood.CURIOUS -> R.string.mood_label_curious
        CompanionMood.DORMANT -> R.string.mood_label_dormant
    }
