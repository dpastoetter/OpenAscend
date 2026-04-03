package com.openascend.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.util.todayEpochDay
import com.openascend.app.util.weekStartMondayEpochDay
import com.openascend.domain.model.CharacterProgress
import com.openascend.domain.model.GameQuest
import com.openascend.domain.model.Habit
import com.openascend.domain.model.StatBlock
import com.openascend.domain.model.UserProfile
import com.openascend.domain.model.WeeklyBoss
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.repository.QuestCompletionRepository
import com.openascend.domain.service.BossGenerator
import com.openascend.domain.service.QuestGenerator
import com.openascend.domain.service.StatComputationService
import com.openascend.domain.service.XpEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val profile: UserProfile,
    val stats: StatBlock,
    val rollingStats: StatBlock,
    val progress: CharacterProgress,
    val quests: List<GameQuest>,
    val boss: WeeklyBoss,
    val todayEpochDay: Long,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val habitRepository: HabitRepository,
    private val metricsRepository: MetricsRepository,
    private val questCompletionRepository: QuestCompletionRepository,
    private val statComputation: StatComputationService,
    private val questGenerator: QuestGenerator,
    private val bossGenerator: BossGenerator,
    private val xpEngine: XpEngine,
) : ViewModel() {

    private val dayFlow = MutableStateFlow(todayEpochDay())

    private val _ui = MutableStateFlow<HomeUiState?>(null)
    val uiState = _ui.asStateFlow()

    init {
        dayFlow
            .flatMapLatest { day ->
                combine(
                    profileRepository.observeProfile(),
                    habitRepository.observeHabits(),
                    habitRepository.observeCompletionsForDay(day),
                    metricsRepository.observeDay(day),
                    questCompletionRepository.observeCompletedIds(day),
                ) { profile, habits, completions, metric, questDone ->
                    Snapshot(profile, habits, day, completions, metric, questDone)
                }
            }
            .onEach { snap ->
                viewModelScope.launch {
                    val rollingMetrics = metricsRepository.metricsBetween(snap.day - 6, snap.day)
                    val completionMap = loadCompletionMap(snap.habits, snap.day)
                    val rolling = statComputation.computeRollingSevenDay(
                        lastSevenDays = rollingMetrics,
                        habits = snap.habits,
                        isHabitCompleted = { hid, epoch -> completionMap[Pair(hid, epoch)] == true },
                        todayEpochDay = snap.day,
                    )
                    val todayStats = statComputation.computeToday(
                        snap.metric,
                        snap.habits,
                        snap.completions,
                    )
                    val boss = bossGenerator.weeklyBoss(
                        weekStartEpochDay = weekStartMondayEpochDay(),
                        stats = rolling,
                    )
                    val quests = questGenerator.dailyQuests(
                        stats = todayStats,
                        goals = snap.profile.goals,
                        todayEpochDay = snap.day,
                        completions = snap.questDone,
                    )
                    val progress = xpEngine.progressForStats(todayStats, snap.profile.streakDays)
                    _ui.value = HomeUiState(
                        profile = snap.profile,
                        stats = todayStats,
                        rollingStats = rolling,
                        progress = progress,
                        quests = quests,
                        boss = boss,
                        todayEpochDay = snap.day,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshToday() {
        dayFlow.value = todayEpochDay()
    }

    fun completeQuest(quest: GameQuest) {
        viewModelScope.launch {
            val day = dayFlow.value
            if (questCompletionRepository.completedIds(day).contains(quest.id)) return@launch
            questCompletionRepository.markComplete(quest.id, day)
            xpEngine.award(quest.xpReward, "Quest: ${quest.title}")
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

private data class Snapshot(
    val profile: UserProfile,
    val habits: List<Habit>,
    val day: Long,
    val completions: Map<Long, Boolean>,
    val metric: com.openascend.domain.model.DailyMetric?,
    val questDone: Set<String>,
)
