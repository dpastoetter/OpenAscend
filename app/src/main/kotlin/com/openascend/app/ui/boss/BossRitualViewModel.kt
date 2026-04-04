package com.openascend.app.ui.boss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.util.todayEpochDay
import com.openascend.app.util.weekStartMondayEpochDay
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.domain.model.Habit
import com.openascend.domain.model.StatBlock
import com.openascend.domain.model.UserProfile
import com.openascend.domain.model.WeeklyBoss
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativeRepository
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.app.feedback.FeedbackController
import com.openascend.domain.service.BankHealthScorer
import com.openascend.domain.service.BossGenerator
import com.openascend.domain.service.StatComputationService
import com.openascend.domain.service.XpEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BossRitualUiState(
    val profile: UserProfile,
    val rolling: StatBlock,
    val boss: WeeklyBoss,
    val bankLabel: String,
    val actTitle: String,
    val bossDeferredThisWeek: Boolean,
    val bossSealedThisWeek: Boolean,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
)

@HiltViewModel
class BossRitualViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val habitRepository: HabitRepository,
    private val metricsRepository: MetricsRepository,
    private val statComputation: StatComputationService,
    private val bossGenerator: BossGenerator,
    private val narrativeRepository: NarrativeRepository,
    private val privacyPreferences: PrivacyPreferences,
    private val xpEngine: XpEngine,
    private val feedbackController: FeedbackController,
) : ViewModel() {

    companion object {
        /** One-time chronicle XP when sealing the weekly boss (same week cannot double-award). */
        const val BOSS_SEAL_XP = 40
    }

    private val day = todayEpochDay()

    private val _ui = MutableStateFlow<BossRitualUiState?>(null)
    val uiState = _ui.asStateFlow()

    init {
        combine(
            profileRepository.observeProfile(),
            habitRepository.observeHabits(),
            privacyPreferences.homeSnapshot,
        ) { profile, habits, homeSnap -> Triple(profile, habits, homeSnap) }
            .onEach { (profile, habits, homeSnap) ->
                viewModelScope.launch {
                    val rollingMetrics = metricsRepository.metricsBetween(day - 6, day)
                    val completionMap = loadCompletionMap(habits, day)
                    val rolling = statComputation.computeRollingSevenDay(
                        lastSevenDays = rollingMetrics,
                        habits = habits,
                        isHabitCompleted = { hid, epoch -> completionMap[Pair(hid, epoch)] == true },
                        todayEpochDay = day,
                    )
                    val weekStart = weekStartMondayEpochDay(LocalDate.ofEpochDay(day))
                    val pack = narrativeRepository.loadPack(homeSnap.settings.flavorPackId)
                    val narrative = NarrativeContext(LocalDate.ofEpochDay(day), pack)
                    val deferred = homeSnap.deferredBossWeekStart == weekStart
                    val sealed = homeSnap.bossRitualSealedWeekStart == weekStart
                    val boss = bossGenerator.weeklyBoss(
                        weekStartEpochDay = weekStart,
                        stats = rolling,
                        narrative = narrative,
                        bossDeferredForThisWeek = deferred,
                    )
                    val todayMetric = metricsRepository.getDay(day)
                    val bankScore = todayMetric?.bankControlScore
                    _ui.value = BossRitualUiState(
                        profile = profile,
                        rolling = rolling,
                        boss = boss,
                        bankLabel = BankHealthScorer.label(bankScore),
                        actTitle = narrative.actTitle,
                        bossDeferredThisWeek = deferred,
                        bossSealedThisWeek = sealed,
                        soundEnabled = homeSnap.settings.soundEnabled,
                        hapticsEnabled = homeSnap.settings.hapticsEnabled,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun deferBossToNextWeek() {
        viewModelScope.launch {
            val weekStart = weekStartMondayEpochDay(LocalDate.ofEpochDay(day))
            privacyPreferences.setDeferredBossWeekStart(weekStart)
        }
    }

    fun clearBossDeferral() {
        viewModelScope.launch {
            privacyPreferences.setDeferredBossWeekStart(null)
        }
    }

    fun sealBossRitual() {
        viewModelScope.launch {
            val weekStart = weekStartMondayEpochDay(LocalDate.ofEpochDay(day))
            if (!privacyPreferences.markBossRitualSealedIfNew(weekStart)) return@launch
            val ui = _ui.value
            val label = ui?.boss?.name ?: "Weekly boss"
            xpEngine.award(BOSS_SEAL_XP, "Weekly boss sealed: $label")
            feedbackController.playQuestSeal(
                ui?.soundEnabled ?: true,
                ui?.hapticsEnabled ?: true,
            )
        }
    }

    private suspend fun loadCompletionMap(habits: List<Habit>, today: Long): Map<Pair<Long, Long>, Boolean> {
        val map = mutableMapOf<Pair<Long, Long>, Boolean>()
        for (offset in 0L..6L) {
            val d = today - offset
            for (h in habits) {
                map[h.id to d] = habitRepository.isCompleted(h.id, d)
            }
        }
        return map
    }
}
