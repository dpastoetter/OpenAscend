package com.openascend.app.ui.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.feedback.FeedbackController
import com.openascend.app.util.todayEpochDay
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.domain.companion.CompanionResolver
import com.openascend.domain.companion.CompanionSnapshot
import com.openascend.domain.companion.TreatTossTiming
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
import kotlin.math.sin

enum class TossQuality {
    GREAT,
    OK,
    MISS,
}

sealed class TreatUiPhase {
    data object Intro : TreatUiPhase()

    data class Playing(
        val roundIndex: Int,
    ) : TreatUiPhase()

    data class RoundResult(
        val roundIndex: Int,
        val quality: TossQuality,
        val runningTotal: Int,
    ) : TreatUiPhase()

    data class Summary(
        val totalPoints: Int,
        val xpGranted: Boolean,
        val xpAlreadyClaimedToday: Boolean,
    ) : TreatUiPhase()
}

data class CompanionPlayUiState(
    val companion: CompanionSnapshot,
    val species: FamiliarSpecies,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val treatTossEasyMode: Boolean,
    val phase: TreatUiPhase,
)

@HiltViewModel
class CompanionPlayViewModel @Inject constructor(
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
        const val TREAT_TOSS_XP = 10
        const val ROUNDS_TOTAL = 3
        private const val FRAME_MS = 16L
    }

    private val day = todayEpochDay()

    private val _needle = MutableStateFlow(0.5f)
    val needle = _needle.asStateFlow()

    private val _ui = MutableStateFlow<CompanionPlayUiState?>(null)
    val uiState = _ui.asStateFlow()

    private var needleJob: Job? = null
    private var sessionAccumulated = 0

    init {
        combine(
            combine(
                profileRepository.observeProfile(),
                habitRepository.observeHabits(),
                habitRepository.observeCompletionsForDay(day),
                habitRepository.observeCompletionsForDay(day - 1),
                metricsRepository.observeDay(day),
            ) { profile, habits, todayComp, yesterdayComp, metric ->
                CpInner(profile, habits, todayComp, yesterdayComp, metric)
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
                        needleJob?.cancel()
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
                            ch.phase !is TreatUiPhase.Intro &&
                            ch.phase !is TreatUiPhase.Summary
                    } == true
                    val phase = when {
                        current == null || !keepPhase -> {
                            if (!keepPhase) needleJob?.cancel()
                            TreatUiPhase.Intro
                        }
                        else -> current.phase
                    }
                    _ui.value = CompanionPlayUiState(
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
        needleJob?.cancel()
        sessionAccumulated = 0
        _needle.value = 0.5f
        _ui.value = base.copy(phase = TreatUiPhase.Playing(1))
        startNeedleTicker(roundIndex = 1)
    }

    fun onTossTap() {
        val base = _ui.value ?: return
        val playing = base.phase as? TreatUiPhase.Playing ?: return
        needleJob?.cancel()
        val n = _needle.value
        val bands = TreatTossTiming.scoreBands(base.companion.mood, base.treatTossEasyMode)
        val q = classify(n, bands)
        val pts = pointsFor(q)
        sessionAccumulated += pts
        feedbackForToss(base.soundEnabled, base.hapticsEnabled, q)
        _ui.value = base.copy(
            phase = TreatUiPhase.RoundResult(playing.roundIndex, q, sessionAccumulated),
        )
    }

    fun continueAfterRound() {
        val base = _ui.value ?: return
        val res = base.phase as? TreatUiPhase.RoundResult ?: return
        if (res.roundIndex >= ROUNDS_TOTAL) {
            viewModelScope.launch {
                finishSession(base)
            }
            return
        }
        val nextRound = res.roundIndex + 1
        _ui.value = base.copy(phase = TreatUiPhase.Playing(nextRound))
        startNeedleTicker(roundIndex = nextRound)
    }

    private suspend fun finishSession(base: CompanionPlayUiState) {
        val snap = privacyPreferences.homeSnapshot.first()
        val hadPriorToday = snap.companionTreatXpEpochDay == day
        val granted = privacyPreferences.markCompanionTreatXpDayIfNew(day)
        if (granted) {
            xpEngine.award(TREAT_TOSS_XP, "Companion treat toss")
            feedbackController.playQuestSeal(base.soundEnabled, base.hapticsEnabled)
        }
        _ui.value = base.copy(
            phase = TreatUiPhase.Summary(
                totalPoints = sessionAccumulated,
                xpGranted = granted,
                xpAlreadyClaimedToday = !granted && hadPriorToday,
            ),
        )
    }

    private fun startNeedleTicker(roundIndex: Int) {
        needleJob?.cancel()
        val base = _ui.value ?: return
        val speed = TreatTossTiming.effectiveNeedleSpeed(
            species = base.species,
            mood = base.companion.mood,
            roundIndex = roundIndex,
            easyMode = base.treatTossEasyMode,
        )
        val phaseOffset = if (base.species == FamiliarSpecies.DRAGON) 0.38 else 0.0
        needleJob = viewModelScope.launch {
            val start = System.nanoTime()
            while (isActive) {
                val t = (System.nanoTime() - start) / 1_000_000_000.0
                val n = ((sin(t * speed + phaseOffset) * 0.5 + 0.5).toFloat()).coerceIn(0f, 1f)
                _needle.value = n
                delay(FRAME_MS)
            }
        }
    }

    private fun classify(n: Float, bands: TreatTossTiming.Bands): TossQuality =
        when {
            n in bands.greatLo..bands.greatHi -> TossQuality.GREAT
            n in bands.okLo..bands.okHi -> TossQuality.OK
            else -> TossQuality.MISS
        }

    private fun pointsFor(q: TossQuality): Int =
        when (q) {
            TossQuality.GREAT -> 2
            TossQuality.OK -> 1
            TossQuality.MISS -> 0
        }

    private fun feedbackForToss(sound: Boolean, haptics: Boolean, q: TossQuality) {
        when (q) {
            TossQuality.GREAT -> feedbackController.playTreatTossGreat(sound, haptics)
            TossQuality.OK -> feedbackController.playTreatTossOk(sound, haptics)
            TossQuality.MISS -> feedbackController.playTreatTossMiss(sound, haptics)
        }
    }

    override fun onCleared() {
        needleJob?.cancel()
        super.onCleared()
    }

    private data class CpInner(
        val profile: UserProfile,
        val habits: List<Habit>,
        val todayComp: Map<Long, Boolean>,
        val yesterdayComp: Map<Long, Boolean>,
        val metric: DailyMetric?,
    )
}
