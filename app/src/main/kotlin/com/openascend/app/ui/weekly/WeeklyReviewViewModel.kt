package com.openascend.app.ui.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.util.todayEpochDay
import com.openascend.app.util.weekStartMondayEpochDay
import com.openascend.domain.model.Habit
import com.openascend.domain.model.StatBlock
import com.openascend.domain.model.UserProfile
import com.openascend.domain.model.WeeklyBoss
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.service.BankHealthScorer
import com.openascend.domain.service.BossGenerator
import com.openascend.domain.service.StatComputationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeeklyUiState(
    val profile: UserProfile,
    val rolling: StatBlock,
    val boss: WeeklyBoss,
    val bankLabel: String,
    val shareSummary: String,
)

@HiltViewModel
class WeeklyReviewViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val habitRepository: HabitRepository,
    private val metricsRepository: MetricsRepository,
    private val statComputation: StatComputationService,
    private val bossGenerator: BossGenerator,
) : ViewModel() {

    private val day = todayEpochDay()

    private val _ui = MutableStateFlow<WeeklyUiState?>(null)
    val uiState = _ui.asStateFlow()

    init {
        combine(
            profileRepository.observeProfile(),
            habitRepository.observeHabits(),
        ) { profile, habits -> profile to habits }
            .onEach { (profile, habits) ->
                viewModelScope.launch {
                    val rollingMetrics = metricsRepository.metricsBetween(day - 6, day)
                    val completionMap = loadCompletionMap(habits, day)
                    val rolling = statComputation.computeRollingSevenDay(
                        lastSevenDays = rollingMetrics,
                        habits = habits,
                        isHabitCompleted = { hid, epoch -> completionMap[Pair(hid, epoch)] == true },
                        todayEpochDay = day,
                    )
                    val boss = bossGenerator.weeklyBoss(weekStartMondayEpochDay(), rolling)
                    val todayMetric = metricsRepository.getDay(day)
                    val bankScore = todayMetric?.bankControlScore
                    val summary = buildShareSummary(profile, rolling, boss)
                    _ui.value = WeeklyUiState(
                        profile = profile,
                        rolling = rolling,
                        boss = boss,
                        bankLabel = BankHealthScorer.label(bankScore),
                        shareSummary = summary,
                    )
                }
            }
            .launchIn(viewModelScope)
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

    private fun buildShareSummary(profile: UserProfile, rolling: StatBlock, boss: WeeklyBoss): String = buildString {
        appendLine("${profile.displayName} · OpenAscend weekly scroll")
        appendLine("Recovery ${rolling.recovery} · Stamina ${rolling.stamina} · Stability ${rolling.stability}")
        appendLine("Discipline ${rolling.discipline} · Vitality ${rolling.vitality}")
        appendLine("Boss: ${boss.name}")
        appendLine(boss.flavor)
    }
}
