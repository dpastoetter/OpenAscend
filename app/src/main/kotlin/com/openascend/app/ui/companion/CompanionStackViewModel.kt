package com.openascend.app.ui.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.feedback.FeedbackController
import com.openascend.app.util.todayEpochDay
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.domain.companion.CompanionResolver
import com.openascend.domain.companion.CompanionSnapshot
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
import kotlin.math.abs
import kotlin.math.sin
import java.time.LocalDate
import javax.inject.Inject

private const val STACK_CENTER = 0.5f
private const val SWEEP_AMPLITUDE = 0.32f
private const val FRAME_MS = 16L

sealed class StackUiPhase {
    data object Intro : StackUiPhase()

    data class Playing(
        val phaseRad: Float,
        /** Horizontal position of the drop marker in 0..1 (same formula as hit test). */
        val cursorX: Float,
        val stackHeight: Int,
        val landHalfWidth: Float,
    ) : StackUiPhase()

    data class Summary(
        val stackHeight: Int,
        val victory: Boolean,
        val xpGranted: Boolean,
        val xpAlreadyClaimedToday: Boolean,
    ) : StackUiPhase()
}

data class CompanionStackUiState(
    val companion: CompanionSnapshot,
    val species: FamiliarSpecies,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val treatTossEasyMode: Boolean,
    val phase: StackUiPhase,
)

@HiltViewModel
class CompanionStackViewModel @Inject constructor(
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
        fun victoryHeight(easy: Boolean): Int = if (easy) 6 else 8

        private fun initialLandHalf(easy: Boolean): Float = if (easy) 0.16f else 0.12f

        private fun phaseSpeedPerFrame(easy: Boolean): Float = if (easy) 0.038f else 0.055f

        private fun minLandHalf(easy: Boolean): Float = if (easy) 0.045f else 0.035f

        private fun shrinkFactor(easy: Boolean): Float = if (easy) 0.96f else 0.92f
    }

    private val day = todayEpochDay()

    private val _ui = MutableStateFlow<CompanionStackUiState?>(null)
    val uiState = _ui.asStateFlow()

    private var gameJob: Job? = null

    init {
        combine(
            combine(
                profileRepository.observeProfile(),
                habitRepository.observeHabits(),
                habitRepository.observeCompletionsForDay(day),
                habitRepository.observeCompletionsForDay(day - 1),
                metricsRepository.observeDay(day),
            ) { profile, habits, todayComp, yesterdayComp, metric ->
                StackInner(profile, habits, todayComp, yesterdayComp, metric)
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
                            ch.treatTossEasyMode == homeSnap.settings.treatTossEasyMode &&
                            ch.phase !is StackUiPhase.Intro &&
                            ch.phase !is StackUiPhase.Summary
                    } == true
                    val phase = when {
                        current == null || !keepPhase -> {
                            if (!keepPhase) gameJob?.cancel()
                            StackUiPhase.Intro
                        }
                        else -> current.phase
                    }
                    _ui.value = CompanionStackUiState(
                        companion = companion,
                        species = species,
                        soundEnabled = homeSnap.settings.soundEnabled,
                        hapticsEnabled = homeSnap.settings.hapticsEnabled,
                        treatTossEasyMode = homeSnap.settings.treatTossEasyMode,
                        phase = phase,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun startSession() {
        val base = _ui.value ?: return
        gameJob?.cancel()
        val easy = base.treatTossEasyMode
        val rad0 = 0f
        _ui.value = base.copy(
            phase = StackUiPhase.Playing(
                phaseRad = rad0,
                cursorX = STACK_CENTER + SWEEP_AMPLITUDE * sin(rad0),
                stackHeight = 0,
                landHalfWidth = initialLandHalf(easy),
            ),
        )
        gameJob = viewModelScope.launch {
            while (isActive) {
                delay(FRAME_MS)
                tick()
            }
        }
    }

    fun tryDrop() {
        val state = _ui.value ?: return
        val play = state.phase as? StackUiPhase.Playing ?: return
        val easy = state.treatTossEasyMode
        val hit = abs(play.cursorX - STACK_CENTER) <= play.landHalfWidth
        if (!hit) {
            gameJob?.cancel()
            feedbackController.playTreatTossMiss(state.soundEnabled, state.hapticsEnabled)
            viewModelScope.launch { finish(state, victory = false, finalHeight = play.stackHeight) }
            return
        }
        feedbackController.playTreatTossGreat(state.soundEnabled, state.hapticsEnabled)
        val newHeight = play.stackHeight + 1
        val goal = victoryHeight(easy)
        if (newHeight >= goal) {
            gameJob?.cancel()
            viewModelScope.launch { finish(state, victory = true, finalHeight = newHeight) }
            return
        }
        val newHalf = (play.landHalfWidth * shrinkFactor(easy)).coerceAtLeast(minLandHalf(easy))
        _ui.value = state.copy(
            phase = StackUiPhase.Playing(
                phaseRad = play.phaseRad,
                cursorX = play.cursorX,
                stackHeight = newHeight,
                landHalfWidth = newHalf,
            ),
        )
    }

    fun returnToIntro() {
        gameJob?.cancel()
        val s = _ui.value ?: return
        _ui.value = s.copy(phase = StackUiPhase.Intro)
    }

    private fun tick() {
        val state = _ui.value ?: return
        val play = state.phase as? StackUiPhase.Playing ?: return
        val speed = phaseSpeedPerFrame(state.treatTossEasyMode)
        val newRad = play.phaseRad + speed
        _ui.value = state.copy(
            phase = play.copy(
                phaseRad = newRad,
                cursorX = STACK_CENTER + SWEEP_AMPLITUDE * sin(newRad),
            ),
        )
    }

    private suspend fun finish(state: CompanionStackUiState, victory: Boolean, finalHeight: Int) {
        val snap = privacyPreferences.homeSnapshot.first()
        val hadPriorToday = snap.companionTreatXpEpochDay == day
        var xpGranted = false
        if (victory) {
            xpGranted = privacyPreferences.markCompanionTreatXpDayIfNew(day)
            if (xpGranted) {
                xpEngine.award(CompanionGameXp.SHARED_DAILY_XP, "Companion stack drop")
                feedbackController.playQuestSeal(state.soundEnabled, state.hapticsEnabled)
            }
        }
        _ui.value = state.copy(
            phase = StackUiPhase.Summary(
                stackHeight = finalHeight,
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

    private data class StackInner(
        val profile: UserProfile,
        val habits: List<Habit>,
        val todayComp: Map<Long, Boolean>,
        val yesterdayComp: Map<Long, Boolean>,
        val metric: DailyMetric?,
    )
}
