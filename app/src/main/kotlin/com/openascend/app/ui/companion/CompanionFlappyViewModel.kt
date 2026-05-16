package com.openascend.app.ui.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.feedback.FeedbackController
import com.openascend.app.util.todayEpochDay
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.domain.companion.CompanionResolver
import com.openascend.domain.companion.CompanionSnapshot
import com.openascend.domain.model.CompanionGameDifficulty
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.FamiliarSpecies
import com.openascend.domain.model.Habit
import com.openascend.domain.model.UserProfile
import com.openascend.domain.narrative.EveningMoodCopy
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativeRepository
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.repository.QuestCompletionRepository
import com.openascend.domain.service.QuestChainDetector
import com.openascend.domain.service.QuestGenerator
import com.openascend.domain.service.StatComputationService
import com.openascend.domain.service.XpEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

data class FlappyPipe(
    val x: Float,
    val gapCenter: Float,
    val scored: Boolean,
    /** Vertical half-gap for this pipe (frozen at spawn so ramp does not shrink past pipes unfairly). */
    val gapHalf: Float,
)

sealed class FlappyPhase {
    data object Intro : FlappyPhase()

    data class Playing(
        val birdY: Float,
        val birdVy: Float,
        val pipes: List<FlappyPipe>,
        val score: Int,
        val openHalf: Float,
        val flapCount: Int,
        val elapsedMs: Long,
        val flapPulseSeq: Int = 0,
    ) : FlappyPhase()

    data class Paused(
        val playing: Playing,
    ) : FlappyPhase()

    data class Summary(
        val score: Int,
        val victory: Boolean,
        val xpGranted: Boolean,
        val xpAlreadyClaimedToday: Boolean,
    ) : FlappyPhase()
}

data class CompanionFlappyUiState(
    val companion: CompanionSnapshot,
    val species: FamiliarSpecies,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val gameDifficulty: CompanionGameDifficulty,
    val phase: FlappyPhase,
)

@HiltViewModel
class CompanionFlappyViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val habitRepository: HabitRepository,
    private val metricsRepository: MetricsRepository,
    private val questCompletionRepository: QuestCompletionRepository,
    private val statComputation: StatComputationService,
    private val questGenerator: QuestGenerator,
    private val narrativeRepository: NarrativeRepository,
    private val privacyPreferences: PrivacyPreferences,
    private val xpEngine: XpEngine,
    private val feedbackController: FeedbackController,
) : ViewModel() {

    companion object {
        const val FRAME_MS = 16L
        /** Shared with [CompanionFlappyScreen] for layout. */
        const val BIRD_CENTER_X_NORM = 0.22f
        const val PIPE_WIDTH_NORM = 0.10f
        /** Normalized hit radius; tuned to better match the 48.dp sprite in the playfield ([CompanionFlappyScreen]). */
        private const val BIRD_RADIUS = 0.06f
        private const val OPEN_HALF_NORMAL = 0.15f
        private const val OPEN_HALF_EASY = 0.175f
        private const val OPEN_HALF_HARD = 0.125f
        private const val GRAVITY_NORMAL = 0.00138f
        private const val GRAVITY_EASY = 0.00095f
        private const val GRAVITY_HARD = 0.00155f
        private const val FLAP_NORMAL = -0.024f
        private const val FLAP_EASY = -0.021f
        private const val FLAP_HARD = -0.023f
        private const val SPEED_NORMAL = 0.0059f
        private const val SPEED_EASY = 0.0043f
        private const val SPEED_HARD = 0.0064f
        /** Left edge (normalized) of the first pipe; farther right gives a calmer first approach. */
        private const val FIRST_PIPE_X = 0.92f
        /** Left edge (normalized) for each newly spawned pipe after the first. */
        private const val PIPE_SPAWN_X = 1.02f

        /** Pipes cleared before internal ramp reaches full difficulty (smoothstep 0..1). */
        private const val RAMP_PIPE_COUNT = 10f

        /** Brief countdown before physics begin. */
        const val COUNTDOWN_MS = 1_200L

        fun victoryThreshold(difficulty: CompanionGameDifficulty): Int = when (difficulty) {
            CompanionGameDifficulty.EASY -> 5
            CompanionGameDifficulty.NORMAL -> 7
            CompanionGameDifficulty.HARD -> 9
        }
    }

    private val day = todayEpochDay()
    private val random = Random.Default

    private val _ui = MutableStateFlow<CompanionFlappyUiState?>(null)
    val uiState = _ui.asStateFlow()

    private var gameJob: Job? = null
    private var birdY = 0.5f
    private var birdVy = 0f
    private var score = 0
    private var pipes = emptyList<FlappyPipe>()
    private var flapCount = 0
    private var elapsedMs = 0L
    private var flapPulseSeq = 0

    init {
        combine(
            combine(
                profileRepository.observeProfile(),
                habitRepository.observeHabits(),
                habitRepository.observeCompletionsForDay(day),
                habitRepository.observeCompletionsForDay(day - 1),
                metricsRepository.observeDay(day),
            ) { profile, habits, todayComp, yesterdayComp, metric ->
                CfInner(profile, habits, todayComp, yesterdayComp, metric)
            },
            questCompletionRepository.observeCompletedIds(day),
            questCompletionRepository.observeCompletedIds(day - 1),
            privacyPreferences.homeSnapshot,
        ) { inner, questToday, questYesterday, homeSnap ->
            Triple(inner, questToday to questYesterday, homeSnap)
        }
            .onEach { (inner, questsPair, homeSnap) ->
                val (questToday, questYesterday) = questsPair
                viewModelScope.launch {
                    if (!inner.profile.onboardingComplete || !homeSnap.settings.familiarEnabled) {
                        gameJob?.cancel()
                        _ui.value = null
                        return@launch
                    }
                    val todayStats = statComputation.computeToday(
                        inner.metric,
                        inner.habits,
                        inner.todayComp,
                    )
                    val pack = narrativeRepository.loadPack(homeSnap.settings.flavorPackId)
                    val narrative = NarrativeContext(LocalDate.ofEpochDay(day), pack)
                    val questDoneByDay = (1L..3L).associate { off ->
                        val d = day - off
                        d to questCompletionRepository.completedIds(d)
                    }
                    val chain = QuestChainDetector.recoveryChainActive(day) { d ->
                        questDoneByDay[d].orEmpty()
                    }
                    val quests = questGenerator.dailyQuests(
                        stats = todayStats,
                        goals = inner.profile.goals,
                        todayEpochDay = day,
                        completions = questToday,
                        narrative = narrative,
                        recoveryChainActive = chain,
                    )
                    val habitsDone = inner.habits.count { inner.todayComp[it.id] == true }
                    val habitsTotal = inner.habits.size
                    val questsDone = quests.count { it.completed }
                    val questsTotal = quests.size
                    val habitsDoneYesterday = inner.habits.count { inner.yesterdayComp[it.id] == true }
                    val questsDoneYesterday = questYesterday.size
                    val moodHeadline = if (homeSnap.eveningMoodEpochDay == day - 1) {
                        EveningMoodCopy.headlineForYesterday(homeSnap.eveningMoodIds)
                    } else {
                        null
                    }
                    val companion = CompanionResolver.resolve(
                        todayEpochDay = day,
                        lastLoggedEpochDay = inner.profile.lastLoggedEpochDay,
                        streakDays = inner.profile.streakDays,
                        habitsDoneToday = habitsDone,
                        habitsTotalToday = habitsTotal,
                        questsDoneToday = questsDone,
                        questsTotalToday = questsTotal,
                        onboardingComplete = inner.profile.onboardingComplete,
                        habitsDoneYesterday = habitsDoneYesterday,
                        questsDoneYesterday = questsDoneYesterday,
                        yesterdayMoodHeadline = moodHeadline,
                    )
                    val species = homeSnap.settings.familiarSpecies
                    val difficulty = homeSnap.settings.resolvedPerGameDifficulties().glide
                    val current = _ui.value
                    val keepPhase = current?.let { ch ->
                        ch.companion.mood == companion.mood &&
                            ch.species == species &&
                            ch.gameDifficulty == difficulty &&
                            ch.phase !is FlappyPhase.Intro &&
                            ch.phase !is FlappyPhase.Summary
                    } == true
                    val phase = when {
                        current == null || !keepPhase -> {
                            if (!keepPhase) gameJob?.cancel()
                            FlappyPhase.Intro
                        }
                        else -> current.phase
                    }
                    _ui.value = CompanionFlappyUiState(
                        companion = companion,
                        species = species,
                        soundEnabled = homeSnap.settings.soundEnabled,
                        hapticsEnabled = homeSnap.settings.hapticsEnabled,
                        gameDifficulty = difficulty,
                        phase = phase,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun startSession() {
        val base = _ui.value ?: return
        gameJob?.cancel()
        birdY = 0.5f
        birdVy = 0f
        score = 0
        flapCount = 0
        elapsedMs = 0L
        flapPulseSeq = 0
        pipes = listOf(
            FlappyPipe(
                x = FIRST_PIPE_X,
                gapCenter = randomGapFirstPipe(base.gameDifficulty),
                scored = false,
                gapHalf = openHalfRamped(0, base.gameDifficulty),
            ),
        )
        val openHalf = openHalfRamped(score, base.gameDifficulty)
        _ui.value = base.copy(
            phase = FlappyPhase.Playing(
                birdY, birdVy, pipes, score, openHalf, flapCount, elapsedMs, flapPulseSeq,
            ),
        )
        gameJob = viewModelScope.launch {
            while (isActive) {
                delay(FRAME_MS)
                tick()
            }
        }
    }

    fun flap() {
        val state = _ui.value ?: return
        if (state.phase !is FlappyPhase.Playing) return
        val playing = state.phase as FlappyPhase.Playing
        if (playing.elapsedMs < COUNTDOWN_MS) return
        birdVy = flapImpulseRamped(playing.score, state.gameDifficulty)
        flapCount++
        flapPulseSeq++
    }

    fun pause() {
        val state = _ui.value ?: return
        val playing = state.phase as? FlappyPhase.Playing ?: return
        gameJob?.cancel()
        _ui.value = state.copy(phase = FlappyPhase.Paused(playing))
    }

    fun resume() {
        val state = _ui.value ?: return
        state.phase as? FlappyPhase.Paused ?: return
        // Keep using the authoritative vars (birdY/birdVy/pipes/score) and refresh the snapshot.
        val openHalf = openHalfRamped(score, state.gameDifficulty)
        _ui.value = state.copy(
            phase = FlappyPhase.Playing(
                birdY, birdVy, pipes, score, openHalf, flapCount, elapsedMs, flapPulseSeq,
            ),
        )
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (isActive) {
                delay(FRAME_MS)
                tick()
            }
        }
    }

    fun returnToIntro() {
        gameJob?.cancel()
        val s = _ui.value ?: return
        _ui.value = s.copy(phase = FlappyPhase.Intro)
    }

    private fun openHalfFor(difficulty: CompanionGameDifficulty): Float = when (difficulty) {
        CompanionGameDifficulty.EASY -> OPEN_HALF_EASY
        CompanionGameDifficulty.NORMAL -> OPEN_HALF_NORMAL
        CompanionGameDifficulty.HARD -> OPEN_HALF_HARD
    }

    private fun rampT(clearedPipes: Int): Float =
        (clearedPipes / RAMP_PIPE_COUNT).coerceIn(0f, 1f)

    /** Ease-in-out so early pipes stay forgiving longer, then tighten. */
    private fun ramp(clearedPipes: Int): Float {
        val t = rampT(clearedPipes)
        return t * t * (3f - 2f * t)
    }

    private fun openHalfRamped(clearedPipes: Int, difficulty: CompanionGameDifficulty): Float {
        val base = openHalfFor(difficulty)
        val t = ramp(clearedPipes)
        val wide = base * 1.22f
        val tight = base * 0.88f
        return wide + (tight - wide) * t
    }

    private fun gravityRamped(clearedPipes: Int, difficulty: CompanionGameDifficulty): Float {
        val t = ramp(clearedPipes)
        val g0 = when (difficulty) {
            CompanionGameDifficulty.EASY -> GRAVITY_EASY * 0.88f
            CompanionGameDifficulty.NORMAL -> GRAVITY_EASY * 0.98f
            CompanionGameDifficulty.HARD -> GRAVITY_NORMAL * 0.96f
        }
        val g1 = when (difficulty) {
            CompanionGameDifficulty.EASY -> GRAVITY_NORMAL * 0.95f
            CompanionGameDifficulty.NORMAL -> GRAVITY_HARD
            CompanionGameDifficulty.HARD -> GRAVITY_HARD * 1.06f
        }
        return g0 + (g1 - g0) * t
    }

    private fun speedRamped(clearedPipes: Int, difficulty: CompanionGameDifficulty): Float {
        val t = ramp(clearedPipes)
        val s0 = when (difficulty) {
            CompanionGameDifficulty.EASY -> SPEED_EASY * 0.9f
            CompanionGameDifficulty.NORMAL -> SPEED_EASY * 0.98f
            CompanionGameDifficulty.HARD -> SPEED_NORMAL * 0.96f
        }
        val s1 = when (difficulty) {
            CompanionGameDifficulty.EASY -> SPEED_NORMAL
            CompanionGameDifficulty.NORMAL -> SPEED_HARD
            CompanionGameDifficulty.HARD -> SPEED_HARD * 1.08f
        }
        return s0 + (s1 - s0) * t
    }

    private fun spawnFurthestMaxRamped(clearedPipes: Int): Float {
        val t = ramp(clearedPipes)
        val loose = 0.34f
        val busy = 0.48f
        return loose + (busy - loose) * t
    }

    private fun flapImpulseRamped(clearedPipes: Int, difficulty: CompanionGameDifficulty): Float {
        val t = ramp(clearedPipes)
        val weak = when (difficulty) {
            CompanionGameDifficulty.EASY -> FLAP_EASY * 0.94f
            CompanionGameDifficulty.NORMAL -> FLAP_EASY * 0.98f
            CompanionGameDifficulty.HARD -> FLAP_NORMAL * 0.98f
        }
        val strong = when (difficulty) {
            CompanionGameDifficulty.EASY -> FLAP_NORMAL
            CompanionGameDifficulty.NORMAL -> FLAP_HARD
            CompanionGameDifficulty.HARD -> FLAP_HARD * 1.06f
        }
        return weak + (strong - weak) * t
    }

    /** First obstacle: gap must actually fit the bird at the default start height (0.5). */
    private fun randomGapFirstPipe(difficulty: CompanionGameDifficulty): Float {
        val oh = openHalfRamped(0, difficulty)
        val margin = 0.036f
        val minC = 0.5f - oh + BIRD_RADIUS + margin
        val maxC = 0.5f + oh - BIRD_RADIUS - margin
        val lo = maxOf(minC, 0.34f)
        val hi = minOf(maxC, 0.66f)
        return if (hi > lo) lo + random.nextFloat() * (hi - lo) else 0.5f
    }

    private fun randomGap(difficulty: CompanionGameDifficulty, clearedPipes: Int): Float {
        val squeeze = 0.02f * ramp(clearedPipes)
        val (lo0, hi0) = when (difficulty) {
            CompanionGameDifficulty.EASY -> 0.38f to 0.62f
            CompanionGameDifficulty.NORMAL -> 0.36f to 0.64f
            CompanionGameDifficulty.HARD -> 0.37f to 0.63f
        }
        val lo = (lo0 + squeeze).coerceAtMost(hi0 - 0.04f)
        val hi = (hi0 - squeeze).coerceAtLeast(lo + 0.04f)
        return lo + random.nextFloat() * (hi - lo)
    }

    private fun tick() {
        val state = _ui.value ?: return
        if (state.phase !is FlappyPhase.Playing) {
            gameJob?.cancel()
            return
        }
        elapsedMs += FRAME_MS
        if (elapsedMs < COUNTDOWN_MS) {
            birdVy = 0f
            _ui.value = state.copy(
                phase = (state.phase as FlappyPhase.Playing).copy(
                    elapsedMs = elapsedMs,
                    birdY = birdY,
                    birdVy = 0f,
                    flapPulseSeq = flapPulseSeq,
                ),
            )
            return
        }
        val d = state.gameDifficulty
        val rampKey = score
        val gravity = gravityRamped(rampKey, d)
        val speed = speedRamped(rampKey, d)

        birdVy += gravity
        birdY += birdVy
        if (birdY - BIRD_RADIUS < 0.04f) {
            birdY = 0.04f + BIRD_RADIUS
            birdVy = 0f
        }
        if (birdY + BIRD_RADIUS > 0.96f) {
            crash(state)
            return
        }

        pipes = pipes.map { it.copy(x = it.x - speed) }.filter { it.x + PIPE_WIDTH_NORM > -0.1f }

        pipes = pipes.map { p ->
            if (!p.scored && p.x + PIPE_WIDTH_NORM < BIRD_CENTER_X_NORM - BIRD_RADIUS) {
                score++
                p.copy(scored = true)
            } else {
                p
            }
        }

        val furthestRight = pipes.maxOfOrNull { it.x + PIPE_WIDTH_NORM } ?: 0f
        val spawnMax = spawnFurthestMaxRamped(score).let { max ->
            if (score == 0) minOf(max, 0.28f) else max
        }
        if (furthestRight < spawnMax) {
            pipes = pipes + FlappyPipe(
                x = PIPE_SPAWN_X,
                gapCenter = randomGap(d, score),
                scored = false,
                gapHalf = openHalfRamped(score, d),
            )
        }

        val openHalfUi = openHalfRamped(score, d)

        for (p in pipes) {
            val overlapX = p.x < BIRD_CENTER_X_NORM + BIRD_RADIUS &&
                p.x + PIPE_WIDTH_NORM > BIRD_CENTER_X_NORM - BIRD_RADIUS
            if (!overlapX) continue
            val gapLow = p.gapCenter - p.gapHalf
            val gapHigh = p.gapCenter + p.gapHalf
            if (birdY - BIRD_RADIUS < gapLow || birdY + BIRD_RADIUS > gapHigh) {
                crash(state)
                return
            }
        }

        _ui.value = state.copy(
            phase = FlappyPhase.Playing(
                birdY = birdY,
                birdVy = birdVy,
                pipes = pipes,
                score = score,
                openHalf = openHalfUi,
                flapCount = flapCount,
                elapsedMs = elapsedMs,
                flapPulseSeq = flapPulseSeq,
            ),
        )
    }

    private fun crash(state: CompanionFlappyUiState) {
        gameJob?.cancel()
        val finalScore = score
        feedbackController.playTreatTossMiss(state.soundEnabled, state.hapticsEnabled)
        viewModelScope.launch { finishAfterCrash(state, finalScore) }
    }

    private suspend fun finishAfterCrash(state: CompanionFlappyUiState, finalScore: Int) {
        val victory = finalScore >= victoryThreshold(state.gameDifficulty)
        val snap = privacyPreferences.homeSnapshot.first()
        val hadPriorToday = snap.companionTreatXpEpochDay == day
        var xpGranted = false
        if (victory) {
            xpGranted = privacyPreferences.markCompanionTreatXpDayIfNew(day)
            if (xpGranted) {
                xpEngine.award(CompanionGameXp.SHARED_DAILY_XP, "Companion glide")
                feedbackController.playQuestSeal(state.soundEnabled, state.hapticsEnabled)
            }
        }
        _ui.value = state.copy(
            phase = FlappyPhase.Summary(
                score = finalScore,
                victory = victory,
                xpGranted = xpGranted,
                xpAlreadyClaimedToday = victory && !xpGranted && hadPriorToday,
            ),
        )
    }

    override fun onCleared() {
        gameJob?.cancel()
        super.onCleared()
    }

    private data class CfInner(
        val profile: UserProfile,
        val habits: List<Habit>,
        val todayComp: Map<Long, Boolean>,
        val yesterdayComp: Map<Long, Boolean>,
        val metric: DailyMetric?,
    )
}
