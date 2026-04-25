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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.hypot
import java.time.LocalDate
import javax.inject.Inject

private data class Pt(val x: Float, val y: Float)

private val WARD_PATH = listOf(
    Pt(0.08f, 0.55f),
    Pt(0.26f, 0.42f),
    Pt(0.48f, 0.58f),
    Pt(0.7f, 0.4f),
    Pt(0.9f, 0.52f),
)

private fun polylineLength(path: List<Pt>): Float {
    var sum = 0f
    for (i in 0 until path.size - 1) {
        val a = path[i]
        val b = path[i + 1]
        sum += hypot(b.x - a.x, b.y - a.y)
    }
    return sum
}

/** Closest Euclidean distance from [q] to the polyline and arc-length along the path to that closest point. */
private fun closestOnPolyline(path: List<Pt>, q: Pt): Pair<Float, Float> {
    var bestD = Float.MAX_VALUE
    var arcAtBest = 0f
    var cum = 0f
    for (i in 0 until path.size - 1) {
        val a = path[i]
        val b = path[i + 1]
        val abx = b.x - a.x
        val aby = b.y - a.y
        val segLen = hypot(abx, aby)
        if (segLen < 1e-5f) {
            continue
        }
        val t = (((q.x - a.x) * abx + (q.y - a.y) * aby) / (segLen * segLen)).coerceIn(0f, 1f)
        val cx = a.x + t * abx
        val cy = a.y + t * aby
        val d = hypot(q.x - cx, q.y - cy)
        val arc = cum + t * segLen
        if (d < bestD) {
            bestD = d
            arcAtBest = arc
        }
        cum += segLen
    }
    return bestD to arcAtBest
}

sealed class ThreadUiPhase {
    data object Intro : ThreadUiPhase()

    data class Playing(
        val progress: Float,
    ) : ThreadUiPhase()

    data class Summary(
        val victory: Boolean,
        val xpGranted: Boolean,
        val xpAlreadyClaimedToday: Boolean,
    ) : ThreadUiPhase()
}

data class CompanionThreadUiState(
    val companion: CompanionSnapshot,
    val species: FamiliarSpecies,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val gameDifficulty: CompanionGameDifficulty,
    val phase: ThreadUiPhase,
)

@HiltViewModel
class CompanionThreadViewModel @Inject constructor(
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
        private val pathLength = polylineLength(WARD_PATH)

        private fun laneThreshold(difficulty: CompanionGameDifficulty): Float = when (difficulty) {
            CompanionGameDifficulty.EASY -> 0.095f
            CompanionGameDifficulty.NORMAL -> 0.062f
            CompanionGameDifficulty.HARD -> 0.048f
        }

        private fun startRadius(difficulty: CompanionGameDifficulty): Float = when (difficulty) {
            CompanionGameDifficulty.EASY -> 0.14f
            CompanionGameDifficulty.NORMAL -> 0.095f
            CompanionGameDifficulty.HARD -> 0.075f
        }

        fun winProgressThreshold(difficulty: CompanionGameDifficulty): Float = when (difficulty) {
            CompanionGameDifficulty.EASY -> 0.90f
            CompanionGameDifficulty.NORMAL -> 0.94f
            CompanionGameDifficulty.HARD -> 0.965f
        }
    }

    private val day = todayEpochDay()

    private val _ui = MutableStateFlow<CompanionThreadUiState?>(null)
    val uiState = _ui.asStateFlow()

    private var strokeActive: Boolean = false

    init {
        combine(
            combine(
                profileRepository.observeProfile(),
                habitRepository.observeHabits(),
                habitRepository.observeCompletionsForDay(day),
                habitRepository.observeCompletionsForDay(day - 1),
                metricsRepository.observeDay(day),
            ) { profile, habits, todayComp, yesterdayComp, metric ->
                ThreadInner(profile, habits, todayComp, yesterdayComp, metric)
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
                        strokeActive = false
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
                    val difficulty = homeSnap.settings.resolvedPerGameDifficulties().thread
                    val current = _ui.value
                    val keepPhase = current?.let { ch ->
                        ch.companion.mood == companion.mood &&
                            ch.species == species &&
                            ch.gameDifficulty == difficulty &&
                            ch.phase !is ThreadUiPhase.Intro &&
                            ch.phase !is ThreadUiPhase.Summary
                    } == true
                    val phase = when {
                        current == null || !keepPhase -> {
                            strokeActive = false
                            ThreadUiPhase.Intro
                        }
                        else -> current.phase
                    }
                    _ui.value = CompanionThreadUiState(
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
        strokeActive = false
        _ui.value = base.copy(phase = ThreadUiPhase.Playing(progress = 0f))
    }

    fun onStrokeStart(nx: Float, ny: Float) {
        val base = _ui.value ?: return
        if (base.phase !is ThreadUiPhase.Playing) return
        val start = WARD_PATH.first()
        val d = hypot(nx - start.x, ny - start.y)
        strokeActive = d <= startRadius(base.gameDifficulty)
        if (strokeActive) {
            val (laneD, arc) = closestOnPolyline(WARD_PATH, Pt(nx, ny))
            if (laneD <= laneThreshold(base.gameDifficulty)) {
                val p = (arc / pathLength).coerceIn(0f, 1f)
                _ui.value = base.copy(phase = ThreadUiPhase.Playing(progress = p))
            }
        }
    }

    fun onStrokeMove(nx: Float, ny: Float) {
        val base = _ui.value ?: return
        val play = base.phase as? ThreadUiPhase.Playing ?: return
        if (!strokeActive) return
        val (d, arc) = closestOnPolyline(WARD_PATH, Pt(nx, ny))
        if (d > laneThreshold(base.gameDifficulty)) {
            strokeActive = false
            viewModelScope.launch { finish(base, victory = false) }
            return
        }
        val rel = (arc / pathLength).coerceIn(0f, 1f)
        val merged = maxOf(play.progress, rel)
        _ui.value = base.copy(phase = ThreadUiPhase.Playing(progress = merged))
        if (merged >= winProgressThreshold(base.gameDifficulty)) {
            strokeActive = false
            viewModelScope.launch { finish(base.copy(phase = ThreadUiPhase.Playing(progress = merged)), victory = true) }
        }
    }

    fun onStrokeEnd() {
        val base = _ui.value ?: return
        if (base.phase is ThreadUiPhase.Summary) return
        val play = base.phase as? ThreadUiPhase.Playing ?: return
        if (!strokeActive && play.progress <= 0f) return
        strokeActive = false
        val p = play.progress
        val victory = p >= winProgressThreshold(base.gameDifficulty)
        viewModelScope.launch { finish(base, victory = victory) }
    }

    fun returnToIntro() {
        strokeActive = false
        val s = _ui.value ?: return
        _ui.value = s.copy(phase = ThreadUiPhase.Intro)
    }

    private suspend fun finish(state: CompanionThreadUiState, victory: Boolean) {
        val snap = privacyPreferences.homeSnapshot.first()
        val hadPriorToday = snap.companionTreatXpEpochDay == day
        var xpGranted = false
        if (victory) {
            xpGranted = privacyPreferences.markCompanionTreatXpDayIfNew(day)
            if (xpGranted) {
                xpEngine.award(CompanionGameXp.SHARED_DAILY_XP, "Companion thread-run")
                feedbackController.playQuestSeal(state.soundEnabled, state.hapticsEnabled)
            }
        } else {
            feedbackController.playTreatTossMiss(state.soundEnabled, state.hapticsEnabled)
        }
        _ui.value = state.copy(
            phase = ThreadUiPhase.Summary(
                victory = victory,
                xpGranted = xpGranted,
                xpAlreadyClaimedToday = victory && !xpGranted && hadPriorToday,
            ),
        )
    }

    private data class ThreadInner(
        val profile: UserProfile,
        val habits: List<Habit>,
        val todayComp: Map<Long, Boolean>,
        val yesterdayComp: Map<Long, Boolean>,
        val metric: DailyMetric?,
    )
}
