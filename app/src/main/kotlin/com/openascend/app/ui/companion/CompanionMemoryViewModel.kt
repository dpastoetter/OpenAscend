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
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

private val SIGILS = listOf("✦", "◈", "❖")

sealed class MemoryUiPhase {
    data object Intro : MemoryUiPhase()

    data class Flash(
        val roundIndex: Int,
        val targetSigil: String,
        val runningScore: Int,
    ) : MemoryUiPhase()

    data class Pick(
        val roundIndex: Int,
        val targetSigil: String,
        val choices: List<String>,
        val runningScore: Int,
    ) : MemoryUiPhase()

    data class RoundFeedback(
        val roundIndex: Int,
        val correct: Boolean,
        val runningScore: Int,
    ) : MemoryUiPhase()

    data class Summary(
        val totalPoints: Int,
        val xpGranted: Boolean,
        val xpAlreadyClaimedToday: Boolean,
    ) : MemoryUiPhase()
}

data class CompanionMemoryUiState(
    val companion: CompanionSnapshot,
    val species: FamiliarSpecies,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val treatTossEasyMode: Boolean,
    val phase: MemoryUiPhase,
)

@HiltViewModel
class CompanionMemoryViewModel @Inject constructor(
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
        const val ROUNDS_TOTAL = 3
        private const val FLASH_MS_EASY = 780L
        private const val FLASH_MS_NORMAL = 520L
    }

    private val day = todayEpochDay()
    private val random = Random.Default

    private val _ui = MutableStateFlow<CompanionMemoryUiState?>(null)
    val uiState = _ui.asStateFlow()

    private var flashJob: Job? = null

    init {
        combine(
            combine(
                profileRepository.observeProfile(),
                habitRepository.observeHabits(),
                habitRepository.observeCompletionsForDay(day),
                habitRepository.observeCompletionsForDay(day - 1),
                metricsRepository.observeDay(day),
            ) { profile, habits, todayComp, yesterdayComp, metric ->
                CmInner(profile, habits, todayComp, yesterdayComp, metric)
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
                        flashJob?.cancel()
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
                            ch.phase !is MemoryUiPhase.Intro &&
                            ch.phase !is MemoryUiPhase.Summary
                    } == true
                    val phase = when {
                        current == null || !keepPhase -> {
                            if (!keepPhase) flashJob?.cancel()
                            MemoryUiPhase.Intro
                        }
                        else -> current.phase
                    }
                    _ui.value = CompanionMemoryUiState(
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
        flashJob?.cancel()
        beginRound(base, roundIndex = 1, runningScore = 0)
    }

    fun onSigilPicked(choice: String) {
        val base = _ui.value ?: return
        val pick = base.phase as? MemoryUiPhase.Pick ?: return
        flashJob?.cancel()
        val correct = choice == pick.targetSigil
        val newScore = pick.runningScore + if (correct) 2 else 0
        if (correct) {
            feedbackController.playTreatTossGreat(base.soundEnabled, base.hapticsEnabled)
        } else {
            feedbackController.playTreatTossMiss(base.soundEnabled, base.hapticsEnabled)
        }
        _ui.value = base.copy(
            phase = MemoryUiPhase.RoundFeedback(pick.roundIndex, correct, newScore),
        )
    }

    fun continueAfterRound() {
        val base = _ui.value ?: return
        val fb = base.phase as? MemoryUiPhase.RoundFeedback ?: return
        if (fb.roundIndex >= ROUNDS_TOTAL) {
            viewModelScope.launch { finishSession(base.copy(phase = fb)) }
            return
        }
        beginRound(base, roundIndex = fb.roundIndex + 1, runningScore = fb.runningScore)
    }

    private fun beginRound(base: CompanionMemoryUiState, roundIndex: Int, runningScore: Int) {
        flashJob?.cancel()
        val target = SIGILS[random.nextInt(SIGILS.size)]
        _ui.value = base.copy(
            phase = MemoryUiPhase.Flash(roundIndex, target, runningScore),
        )
        val flashMs = if (base.treatTossEasyMode) FLASH_MS_EASY else FLASH_MS_NORMAL
        flashJob = viewModelScope.launch {
            delay(flashMs)
            if (!isActive) return@launch
            val b = _ui.value ?: return@launch
            val f = b.phase as? MemoryUiPhase.Flash ?: return@launch
            if (f.roundIndex != roundIndex) return@launch
            val shuffled = SIGILS.shuffled(random)
            _ui.value = b.copy(
                phase = MemoryUiPhase.Pick(
                    roundIndex = f.roundIndex,
                    targetSigil = f.targetSigil,
                    choices = shuffled,
                    runningScore = f.runningScore,
                ),
            )
        }
    }

    private suspend fun finishSession(base: CompanionMemoryUiState) {
        val fb = base.phase as? MemoryUiPhase.RoundFeedback ?: return
        val snap = privacyPreferences.homeSnapshot.first()
        val hadPriorToday = snap.companionTreatXpEpochDay == day
        val granted = privacyPreferences.markCompanionTreatXpDayIfNew(day)
        if (granted) {
            xpEngine.award(CompanionPlayViewModel.TREAT_TOSS_XP, "Companion flash sigils")
            feedbackController.playQuestSeal(base.soundEnabled, base.hapticsEnabled)
        }
        _ui.value = base.copy(
            phase = MemoryUiPhase.Summary(
                totalPoints = fb.runningScore,
                xpGranted = granted,
                xpAlreadyClaimedToday = !granted && hadPriorToday,
            ),
        )
    }

    override fun onCleared() {
        flashJob?.cancel()
        super.onCleared()
    }

    private data class CmInner(
        val profile: UserProfile,
        val habits: List<Habit>,
        val todayComp: Map<Long, Boolean>,
        val yesterdayComp: Map<Long, Boolean>,
        val metric: DailyMetric?,
    )
}
