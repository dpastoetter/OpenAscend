package com.openascend.app.ui.weekly

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
import com.openascend.domain.narrative.SeasonProgressResolver
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.service.BankHealthScorer
import com.openascend.domain.service.BossGenerator
import com.openascend.domain.service.StatComputationService
import com.openascend.domain.service.XpEngine
import com.openascend.domain.service.XpLedgerAggregator
import com.openascend.domain.service.XpLedgerSummary
import com.openascend.domain.repository.XpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WeeklyUiState(
    val profile: UserProfile,
    val rolling: StatBlock,
    val boss: WeeklyBoss,
    val bankLabel: String,
    val shareSummary: String,
    val actTitle: String,
    val seasonChapterLine: String?,
    val bossDeferredThisWeek: Boolean,
    val bossSealedThisWeek: Boolean,
    val xpLedger: XpLedgerSummary,
    val level: Int,
    val archetypeLine: String,
    val familiarEnabled: Boolean,
    val familiarSpecies: com.openascend.domain.model.FamiliarSpecies,
)

@HiltViewModel
class WeeklyReviewViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val habitRepository: HabitRepository,
    private val metricsRepository: MetricsRepository,
    private val statComputation: StatComputationService,
    private val bossGenerator: BossGenerator,
    private val narrativeRepository: NarrativeRepository,
    private val privacyPreferences: PrivacyPreferences,
    private val xpRepository: XpRepository,
    private val xpEngine: XpEngine,
) : ViewModel() {

    private val day = todayEpochDay()

    private val _ui = MutableStateFlow<WeeklyUiState?>(null)
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
                        bossSealedThisWeek = sealed,
                    )
                    val weekStartMillis = LocalDate.ofEpochDay(weekStart)
                        .atStartOfDay(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    val xpLedger = XpLedgerAggregator.summarize(
                        xpRepository.observeEvents(200).first(),
                        sinceEpochMillis = weekStartMillis,
                    )
                    val todayMetric = metricsRepository.getDay(day)
                    val bankScore = todayMetric?.bankControlScore
                    val summary = buildShareSummary(profile, rolling, boss, xpLedger)
                    val progress = xpEngine.progressForStats(rolling, profile.streakDays)
                    val archetypeLine = progress.archetype.displayName +
                        profile.archetypeSuffix?.let { " · $it" }.orEmpty()
                    _ui.value = WeeklyUiState(
                        profile = profile,
                        rolling = rolling,
                        boss = boss,
                        bankLabel = BankHealthScorer.label(bankScore),
                        shareSummary = summary,
                        actTitle = narrative.actTitle,
                        seasonChapterLine = SeasonProgressResolver.resolve(day, pack)?.chapterLine,
                        bossDeferredThisWeek = deferred,
                        bossSealedThisWeek = sealed,
                        xpLedger = xpLedger,
                        level = progress.level,
                        archetypeLine = archetypeLine,
                        familiarEnabled = homeSnap.settings.familiarEnabled,
                        familiarSpecies = homeSnap.settings.familiarSpecies,
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

    private fun buildShareSummary(
        profile: UserProfile,
        rolling: StatBlock,
        boss: WeeklyBoss,
        xpLedger: XpLedgerSummary,
    ): String = buildString {
        appendLine("${profile.displayName} · OpenAscend weekly scroll")
        appendLine("Recovery ${rolling.recovery} · Stamina ${rolling.stamina} · Stability ${rolling.stability}")
        appendLine("Discipline ${rolling.discipline} · Vitality ${rolling.vitality}")
        if (xpLedger.total > 0) {
            appendLine("XP this week: ${xpLedger.total}")
            if (xpLedger.checkInXp > 0) appendLine("Check-in +${xpLedger.checkInXp}")
            if (xpLedger.questXp > 0) appendLine("Quests +${xpLedger.questXp}")
            if (xpLedger.bossXp > 0) appendLine("Boss +${xpLedger.bossXp}")
            if (xpLedger.companionXp > 0) appendLine("Companion +${xpLedger.companionXp}")
            if (xpLedger.habitXp > 0) appendLine("Habits +${xpLedger.habitXp}")
        }
        appendLine("Boss: ${boss.name}")
        appendLine(boss.tell)
        appendLine(boss.flavor)
    }
}
