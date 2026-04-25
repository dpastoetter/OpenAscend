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
)

sealed class FlappyPhase {
    data object Intro : FlappyPhase()

    data class Playing(
        val birdY: Float,
        val birdVy: Float,
        val pipes: List<FlappyPipe>,
        val score: Int,
        val openHalf: Float,
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
        /** Matches half of the 48.dp sprite in the 320.dp-tall playfield ([CompanionFlappyScreen]). */
        private const val BIRD_RADIUS = 0.075f
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
        /**
         * Spawn the next pipe only once the rightmost pipe's right edge has moved past this x (0–1).
         * Lower value → spawn later → more horizontal space between consecutive pipes.
         */
        private const val SPAWN_FURTHEST_MAX = 0.42f
        /** Left edge (normalized) of the first pipe; farther right gives a calmer first approach. */
        private const val FIRST_PIPE_X = 0.88f
        /** Left edge (normalized) for each newly spawned pipe after the first. */
        private const val PIPE_SPAWN_X = 1.02f

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
                    val current = _ui.value
                    val keepPhase = current?.let { ch ->
                        ch.companion.mood == companion.mood &&
                            ch.species == species &&
                            ch.gameDifficulty == homeSnap.settings.companionGameDifficulty &&
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
                        gameDifficulty = homeSnap.settings.companionGameDifficulty,
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
        pipes = listOf(
            FlappyPipe(x = FIRST_PIPE_X, gapCenter = randomGapFirstPipe(base.gameDifficulty), scored = false),
        )
        val openHalf = openHalfFor(base.gameDifficulty)
        _ui.value = base.copy(
            phase = FlappyPhase.Playing(birdY, birdVy, pipes, score, openHalf),
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
        birdVy = when (state.gameDifficulty) {
            CompanionGameDifficulty.EASY -> FLAP_EASY
            CompanionGameDifficulty.NORMAL -> FLAP_NORMAL
            CompanionGameDifficulty.HARD -> FLAP_HARD
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

    /** First obstacle: gap must actually fit the bird at the default start height (0.5). */
    private fun randomGapFirstPipe(difficulty: CompanionGameDifficulty): Float {
        val oh = openHalfFor(difficulty)
        val margin = 0.028f
        val minC = 0.5f - oh + BIRD_RADIUS + margin
        val maxC = 0.5f + oh - BIRD_RADIUS - margin
        val lo = maxOf(minC, 0.36f)
        val hi = minOf(maxC, 0.64f)
        return if (hi > lo) lo + random.nextFloat() * (hi - lo) else 0.5f
    }

    private fun randomGap(difficulty: CompanionGameDifficulty): Float {
        val (lo, hi) = when (difficulty) {
            CompanionGameDifficulty.EASY -> 0.38f to 0.62f
            CompanionGameDifficulty.NORMAL -> 0.36f to 0.64f
            CompanionGameDifficulty.HARD -> 0.37f to 0.63f
        }
        return lo + random.nextFloat() * (hi - lo)
    }

    private fun tick() {
        val state = _ui.value ?: return
        if (state.phase !is FlappyPhase.Playing) {
            gameJob?.cancel()
            return
        }
        val d = state.gameDifficulty
        val openHalf = openHalfFor(d)
        val gravity = when (d) {
            CompanionGameDifficulty.EASY -> GRAVITY_EASY
            CompanionGameDifficulty.NORMAL -> GRAVITY_NORMAL
            CompanionGameDifficulty.HARD -> GRAVITY_HARD
        }
        val speed = when (d) {
            CompanionGameDifficulty.EASY -> SPEED_EASY
            CompanionGameDifficulty.NORMAL -> SPEED_NORMAL
            CompanionGameDifficulty.HARD -> SPEED_HARD
        }

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

        val furthestRight = pipes.maxOfOrNull { it.x + PIPE_WIDTH_NORM } ?: 0f
        if (furthestRight < SPAWN_FURTHEST_MAX) {
            pipes = pipes + FlappyPipe(x = PIPE_SPAWN_X, gapCenter = randomGap(d), scored = false)
        }

        pipes = pipes.map { p ->
            if (!p.scored && p.x + PIPE_WIDTH_NORM < BIRD_CENTER_X_NORM - BIRD_RADIUS * 0.25f) {
                score++
                p.copy(scored = true)
            } else {
                p
            }
        }

        for (p in pipes) {
            val overlapX = p.x < BIRD_CENTER_X_NORM + BIRD_RADIUS &&
                p.x + PIPE_WIDTH_NORM > BIRD_CENTER_X_NORM - BIRD_RADIUS
            if (!overlapX) continue
            val gapLow = p.gapCenter - openHalf
            val gapHigh = p.gapCenter + openHalf
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
                openHalf = openHalf,
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
