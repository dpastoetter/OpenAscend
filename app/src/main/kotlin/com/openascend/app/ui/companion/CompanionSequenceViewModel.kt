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

private val SIGILS = listOf("✦", "◈", "❖")

sealed class SequenceUiPhase {
    data object Intro : SequenceUiPhase()

    data class Playback(
        val roundIndex: Int,
        val sequence: List<String>,
        /** Which beat is highlighted, or null during the short pause between beats. */
        val showingIndex: Int?,
    ) : SequenceUiPhase()

    data class EchoInput(
        val roundIndex: Int,
        val sequence: List<String>,
        val entered: List<String>,
        val runningScore: Int,
    ) : SequenceUiPhase()

    data class RoundFeedback(
        val roundIndex: Int,
        val correct: Boolean,
        val runningScore: Int,
    ) : SequenceUiPhase()

    data class Summary(
        val totalPoints: Int,
        val xpGranted: Boolean,
        val xpAlreadyClaimedToday: Boolean,
    ) : SequenceUiPhase()
}

data class CompanionSequenceUiState(
    val companion: CompanionSnapshot,
    val species: FamiliarSpecies,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val gameDifficulty: CompanionGameDifficulty,
    val phase: SequenceUiPhase,
)

@HiltViewModel
class CompanionSequenceViewModel @Inject constructor(
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
        private const val DISPLAY_MS_NORMAL = 560L
        private const val DISPLAY_MS_EASY = 720L
        private const val DISPLAY_MS_HARD = 420L
        private const val GAP_MS_NORMAL = 220L
        private const val GAP_MS_EASY = 300L
        private const val GAP_MS_HARD = 160L
    }

    private val day = todayEpochDay()
    private val random = Random.Default

    private val _ui = MutableStateFlow<CompanionSequenceUiState?>(null)
    val uiState = _ui.asStateFlow()

    private var playbackJob: Job? = null

    init {
        combine(
            combine(
                profileRepository.observeProfile(),
                habitRepository.observeHabits(),
                habitRepository.observeCompletionsForDay(day),
                habitRepository.observeCompletionsForDay(day - 1),
                metricsRepository.observeDay(day),
            ) { profile, habits, todayComp, yesterdayComp, metric ->
                CsInner(profile, habits, todayComp, yesterdayComp, metric)
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
                        playbackJob?.cancel()
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
                            ch.phase !is SequenceUiPhase.Intro &&
                            ch.phase !is SequenceUiPhase.Summary
                    } == true
                    val phase = when {
                        current == null || !keepPhase -> {
                            if (!keepPhase) playbackJob?.cancel()
                            SequenceUiPhase.Intro
                        }
                        else -> current.phase
                    }
                    _ui.value = CompanionSequenceUiState(
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
        playbackJob?.cancel()
        beginRound(base, roundIndex = 1, runningScore = 0)
    }

    fun onSigilTapped(choice: String) {
        val base = _ui.value ?: return
        val input = base.phase as? SequenceUiPhase.EchoInput ?: return
        val expected = input.sequence[input.entered.size]
        if (choice != expected) {
            feedbackController.playTreatTossMiss(base.soundEnabled, base.hapticsEnabled)
            _ui.value = base.copy(
                phase = SequenceUiPhase.RoundFeedback(input.roundIndex, false, input.runningScore),
            )
            return
        }
        val newEntered = input.entered + choice
        if (newEntered.size == input.sequence.size) {
            feedbackController.playTreatTossGreat(base.soundEnabled, base.hapticsEnabled)
            val newScore = input.runningScore + 2
            _ui.value = base.copy(
                phase = SequenceUiPhase.RoundFeedback(input.roundIndex, true, newScore),
            )
        } else {
            _ui.value = base.copy(
                phase = SequenceUiPhase.EchoInput(
                    roundIndex = input.roundIndex,
                    sequence = input.sequence,
                    entered = newEntered,
                    runningScore = input.runningScore,
                ),
            )
        }
    }

    fun continueAfterRound() {
        val base = _ui.value ?: return
        val fb = base.phase as? SequenceUiPhase.RoundFeedback ?: return
        if (fb.roundIndex >= ROUNDS_TOTAL) {
            viewModelScope.launch { finishSession(base.copy(phase = fb)) }
            return
        }
        beginRound(base, roundIndex = fb.roundIndex + 1, runningScore = fb.runningScore)
    }

    private fun sequenceLength(roundIndex: Int, difficulty: CompanionGameDifficulty): Int =
        when (difficulty) {
            CompanionGameDifficulty.EASY -> when (roundIndex) {
                1 -> 2
                2 -> 2
                else -> 3
            }
            CompanionGameDifficulty.NORMAL -> roundIndex + 1
            CompanionGameDifficulty.HARD -> roundIndex + 2
        }

    private fun buildSequence(length: Int): List<String> =
        List(length) { SIGILS[random.nextInt(SIGILS.size)] }

    private fun beginRound(base: CompanionSequenceUiState, roundIndex: Int, runningScore: Int) {
        playbackJob?.cancel()
        val sequence = buildSequence(sequenceLength(roundIndex, base.gameDifficulty))
        val displayMs = when (base.gameDifficulty) {
            CompanionGameDifficulty.EASY -> DISPLAY_MS_EASY
            CompanionGameDifficulty.NORMAL -> DISPLAY_MS_NORMAL
            CompanionGameDifficulty.HARD -> DISPLAY_MS_HARD
        }
        val gapMs = when (base.gameDifficulty) {
            CompanionGameDifficulty.EASY -> GAP_MS_EASY
            CompanionGameDifficulty.NORMAL -> GAP_MS_NORMAL
            CompanionGameDifficulty.HARD -> GAP_MS_HARD
        }
        playbackJob = viewModelScope.launch {
            for (i in sequence.indices) {
                val cur = _ui.value ?: return@launch
                _ui.value = cur.copy(
                    phase = SequenceUiPhase.Playback(roundIndex, sequence, showingIndex = i),
                )
                delay(displayMs)
                if (!isActive) return@launch
                val mid = _ui.value ?: return@launch
                val pb = mid.phase as? SequenceUiPhase.Playback ?: return@launch
                if (pb.roundIndex != roundIndex || pb.sequence != sequence) return@launch
                _ui.value = mid.copy(
                    phase = SequenceUiPhase.Playback(roundIndex, sequence, showingIndex = null),
                )
                delay(gapMs)
                if (!isActive) return@launch
            }
            val fin = _ui.value ?: return@launch
            val last = fin.phase as? SequenceUiPhase.Playback ?: return@launch
            if (last.roundIndex != roundIndex || last.sequence != sequence) return@launch
            _ui.value = fin.copy(
                phase = SequenceUiPhase.EchoInput(
                    roundIndex = roundIndex,
                    sequence = sequence,
                    entered = emptyList(),
                    runningScore = runningScore,
                ),
            )
        }
    }

    private suspend fun finishSession(base: CompanionSequenceUiState) {
        val fb = base.phase as? SequenceUiPhase.RoundFeedback ?: return
        val snap = privacyPreferences.homeSnapshot.first()
        val hadPriorToday = snap.companionTreatXpEpochDay == day
        val granted = privacyPreferences.markCompanionTreatXpDayIfNew(day)
        if (granted) {
            xpEngine.award(CompanionGameXp.SHARED_DAILY_XP, "Companion echo sigils")
            feedbackController.playQuestSeal(base.soundEnabled, base.hapticsEnabled)
        }
        _ui.value = base.copy(
            phase = SequenceUiPhase.Summary(
                totalPoints = fb.runningScore,
                xpGranted = granted,
                xpAlreadyClaimedToday = !granted && hadPriorToday,
            ),
        )
    }

    override fun onCleared() {
        playbackJob?.cancel()
        super.onCleared()
    }

    private data class CsInner(
        val profile: UserProfile,
        val habits: List<Habit>,
        val todayComp: Map<Long, Boolean>,
        val yesterdayComp: Map<Long, Boolean>,
        val metric: DailyMetric?,
    )
}
