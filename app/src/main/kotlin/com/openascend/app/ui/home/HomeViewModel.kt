package com.openascend.app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.feedback.FeedbackController
import com.openascend.app.ui.FeedbackLineFormatter
import com.openascend.app.health.HealthConnectMetricsSync
import com.openascend.app.util.todayEpochDay
import com.openascend.app.util.weekStartMondayEpochDay
import com.openascend.data.local.prefs.CompanionMemoryStore
import com.openascend.data.local.prefs.CompanionPlayDayStore
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.data.local.prefs.WidgetSnapshotStore
import com.openascend.domain.model.CharacterProgress
import com.openascend.domain.model.GameQuest
import com.openascend.domain.model.Habit
import com.openascend.domain.model.StatBlock
import com.openascend.domain.model.UserProfile
import com.openascend.domain.model.FamiliarSpecies
import com.openascend.domain.model.WeeklyBoss
import com.openascend.domain.companion.CompanionMemoryWhisper
import com.openascend.domain.companion.CompanionResolver
import com.openascend.domain.companion.CompanionSnapshot
import com.openascend.domain.service.ChronicleCompassKind
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.narrative.ArchetypeSuffixCatalog
import com.openascend.domain.narrative.EveningMoodCopy
import com.openascend.domain.narrative.LevelUpFlair
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativeRepository
import com.openascend.domain.narrative.StarterPaths
import com.openascend.domain.narrative.WidgetStoryLines
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.repository.QuestCompletionRepository
import com.openascend.domain.repository.XpRepository
import com.openascend.domain.service.BossGenerator
import com.openascend.domain.service.BossPrepMeter
import com.openascend.domain.service.ChronicleCompassDirective
import com.openascend.domain.service.ChronicleCompassResolver
import com.openascend.domain.service.HabitRewards
import com.openascend.domain.service.QuestChainDetector
import com.openascend.domain.insight.ChronicleInsight
import com.openascend.domain.insight.ChronicleInsightEngine
import com.openascend.domain.narrative.SeasonProgress
import com.openascend.domain.narrative.SeasonProgressResolver
import com.openascend.domain.service.StreakMilestone
import com.openascend.data.local.prefs.InsightOracleStore
import com.openascend.domain.service.QuestGenerator
import com.openascend.domain.service.StatComputationService
import com.openascend.domain.service.XpEngine
import java.time.LocalTime
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class LevelUpSheetData(
    val newLevel: Int,
    val compliment: String,
    val archetypeDisplay: String,
)

data class SuffixPickerData(
    val bandLevel: Int,
    val choices: List<String>,
)

data class HomeUiState(
    val profile: UserProfile,
    val stats: StatBlock,
    val rollingStats: StatBlock,
    val progress: CharacterProgress,
    val quests: List<GameQuest>,
    val boss: WeeklyBoss,
    val bossSealedThisWeek: Boolean,
    val compass: ChronicleCompassDirective,
    val habits: List<Habit>,
    val todayCompletions: Map<Long, Boolean>,
    val dailyBoonAvailable: Boolean,
    val bossPrepSealsThisWeek: Int,
    val actTitle: String,
    val starterPathLabel: String?,
    val levelUpSheet: LevelUpSheetData?,
    val suffixPicker: SuffixPickerData?,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val familiarEnabled: Boolean,
    val familiarSpecies: FamiliarSpecies,
    val companion: CompanionSnapshot,
    val companionMemoryWhisper: String?,
    val streakMilestoneDays: Int?,
    val companionPlayStreakDays: Int,
    val showHealthConnectOnboarding: Boolean,
    val seasonProgress: SeasonProgress?,
    val weeklyInsight: ChronicleInsight?,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext appContext: Context,
    private val profileRepository: ProfileRepository,
    private val habitRepository: HabitRepository,
    private val metricsRepository: MetricsRepository,
    private val questCompletionRepository: QuestCompletionRepository,
    private val xpRepository: XpRepository,
    private val statComputation: StatComputationService,
    private val questGenerator: QuestGenerator,
    private val bossGenerator: BossGenerator,
    private val xpEngine: XpEngine,
    private val narrativeRepository: NarrativeRepository,
    private val privacyPreferences: PrivacyPreferences,
    private val widgetSnapshotStore: WidgetSnapshotStore,
    private val feedbackController: FeedbackController,
    private val healthConnectMetricsSync: HealthConnectMetricsSync,
) : ViewModel() {

    private val insightOracleStore = InsightOracleStore(appContext)
    private val companionMemoryStore = CompanionMemoryStore(appContext)
    private val companionPlayDayStore = CompanionPlayDayStore(appContext)

    private val dayFlow = MutableStateFlow(todayEpochDay())

    private val _ui = MutableStateFlow<HomeUiState?>(null)
    val uiState = _ui.asStateFlow()

    private val _questSealFlair = MutableStateFlow<String?>(null)
    val questSealFlair = _questSealFlair.asStateFlow()

    private val _habitSealFlair = MutableStateFlow<String?>(null)
    val habitSealFlair = _habitSealFlair.asStateFlow()

    private val _pickedSuffixThisSession = MutableStateFlow(false)

    init {
        dayFlow
            .flatMapLatest { day ->
                combine(
                    combine(
                        profileRepository.observeProfile(),
                        habitRepository.observeHabits(),
                        habitRepository.observeCompletionsForDay(day),
                        habitRepository.observeCompletionsForDay(day - 1),
                        metricsRepository.observeDay(day),
                    ) { profile, habits, todayComp, yesterdayComp, metric ->
                        HomeInnerSnap(profile, habits, todayComp, yesterdayComp, metric)
                    },
                    questCompletionRepository.observeCompletedIds(day),
                    questCompletionRepository.observeCompletedIds(day - 1),
                    privacyPreferences.homeSnapshot,
                    xpRepository.observeEvents(50),
                ) { inner, questToday, questYesterday, homeSnap, _ ->
                    HomeBundle(day, inner, questToday, questYesterday, homeSnap)
                }
            }
            .onEach { bundle ->
                viewModelScope.launch {
                    healthConnectMetricsSync.syncIfEnabled(bundle.homeSnap.settings)
                    val rollingMetrics = metricsRepository.metricsBetween(bundle.day - 6, bundle.day)
                    val completionMap = loadCompletionMap(bundle.inner.habits, bundle.day)
                    val rolling = statComputation.computeRollingSevenDay(
                        lastSevenDays = rollingMetrics,
                        habits = bundle.inner.habits,
                        isHabitCompleted = { hid, epoch -> completionMap[Pair(hid, epoch)] == true },
                        todayEpochDay = bundle.day,
                    )
                    val todayStats = statComputation.computeToday(
                        bundle.inner.metric,
                        bundle.inner.habits,
                        bundle.inner.todayComp,
                    )
                    val pack = narrativeRepository.loadPack(bundle.homeSnap.settings.flavorPackId)
                    val localDate = LocalDate.ofEpochDay(bundle.day)
                    val narrative = NarrativeContext(localDate, pack)
                    val weekStart = weekStartMondayEpochDay(localDate)
                    val bossDeferred = bundle.homeSnap.deferredBossWeekStart == weekStart
                    val bossSealedThisWeek = bundle.homeSnap.bossRitualSealedWeekStart == weekStart
                    val boss = bossGenerator.weeklyBoss(
                        weekStartEpochDay = weekStart,
                        stats = rolling,
                        narrative = narrative,
                        bossDeferredForThisWeek = bossDeferred,
                        bossSealedThisWeek = bossSealedThisWeek,
                    )
                    val questDoneByDay = (1L..3L).associate { off ->
                        val d = bundle.day - off
                        d to questCompletionRepository.completedIds(d)
                    }
                    val activeChains = QuestChainDetector.activeChains(bundle.day) { d ->
                        questDoneByDay[d].orEmpty()
                    }
                    val quests = questGenerator.dailyQuests(
                        stats = todayStats,
                        goals = bundle.inner.profile.goals,
                        todayEpochDay = bundle.day,
                        completions = bundle.questToday,
                        narrative = narrative,
                        activeStatChains = activeChains,
                    )
                    val displayStats = todayStats.withSealedQuestSpotlight(quests)
                    val progress = xpEngine.progressForStats(todayStats, bundle.inner.profile.streakDays)
                    val moodHeadline = if (bundle.homeSnap.eveningMoodEpochDay == bundle.day - 1) {
                        EveningMoodCopy.headlineForYesterday(bundle.homeSnap.eveningMoodIds)
                    } else {
                        null
                    }
                    val storedLevel = bundle.homeSnap.lastKnownLevel
                    if (storedLevel == null) {
                        privacyPreferences.setLastKnownLevel(progress.level)
                    }
                    val levelUpSheet = if (storedLevel != null && progress.level > storedLevel) {
                        val arch = progress.archetype.displayName +
                            bundle.inner.profile.archetypeSuffix?.let { " · $it" }.orEmpty()
                        LevelUpSheetData(
                            newLevel = progress.level,
                            compliment = LevelUpFlair.compliment(progress.level, bundle.inner.profile.displayName),
                            archetypeDisplay = arch,
                        )
                    } else {
                        null
                    }
                    val band = ArchetypeSuffixCatalog.bandForLevel(progress.level)
                    val suffixPicker = if (
                        band != null &&
                        bundle.inner.profile.archetypeSuffix == null &&
                        !_pickedSuffixThisSession.value
                    ) {
                        SuffixPickerData(band, ArchetypeSuffixCatalog.choicesForBand(band))
                    } else {
                        null
                    }
                    val habitsDone = bundle.inner.habits.count { bundle.inner.todayComp[it.id] == true }
                    val habitsTotal = bundle.inner.habits.size
                    val questsDone = quests.count { it.completed }
                    val questsTotal = quests.size
                    val habitsDoneYesterday = bundle.inner.habits.count {
                        bundle.inner.yesterdayComp[it.id] == true
                    }
                    val questsDoneYesterday = bundle.questYesterday.size
                    val loggedToday = bundle.inner.profile.lastLoggedEpochDay == bundle.day
                    val openQuest = quests.firstOrNull { !it.completed }
                    val treasuryDone = privacyPreferences.treasuryRitualDoneForWeek(weekStart)
                    val compass = ChronicleCompassResolver.resolve(
                        todayEpochDay = bundle.day,
                        hourOfDay = LocalTime.now().hour,
                        loggedToday = loggedToday,
                        openQuest = openQuest,
                        bossSealedThisWeek = bossSealedThisWeek,
                        bossName = boss.name,
                        bossTargetStat = boss.targetStat,
                        habits = bundle.inner.habits,
                        todayCompletions = bundle.inner.todayComp,
                        treasuryRitualDoneThisWeek = treasuryDone,
                    )
                    val seasonProgress = SeasonProgressResolver.resolve(bundle.day, pack)
                    val metrics30 = metricsRepository.metricsBetween(bundle.day - 29, bundle.day)
                    val metricsByDay = metrics30.associateBy { it.epochDay }
                    val completion30 = loadCompletionMap(bundle.inner.habits, bundle.day, 29)
                    val statsByDay = (bundle.day - 29..bundle.day).associateWith { d ->
                        statComputation.computeToday(
                            metricsByDay[d],
                            bundle.inner.habits,
                            completion30
                                .filterKeys { (_, epoch) -> epoch == d }
                                .mapKeys { it.key.first },
                        )
                    }
                    val sealedDays = buildSet {
                        for (d in bundle.day - 29..bundle.day) {
                            if (bundle.inner.profile.lastLoggedEpochDay == d) add(d)
                            if (questCompletionRepository.completedIds(d).isNotEmpty()) add(d)
                        }
                    }
                    val rawInsight = ChronicleInsightEngine.pickWeeklyInsight(
                        weekStartEpochDay = weekStart,
                        metricsByDay = metricsByDay,
                        statsByDay = statsByDay,
                        sealedEpochDays = sealedDays,
                    )
                    val weeklyInsight =
                        if (rawInsight != null && !insightOracleStore.isDismissedForWeek(weekStart)) {
                            rawInsight
                        } else {
                            null
                        }
                    val dailyBoonAvailable = bundle.homeSnap.companionTreatXpEpochDay != bundle.day
                    val bossPrepSeals = BossPrepMeter.countPrepSealsThisWeek(
                        habits = bundle.inner.habits,
                        weekStartEpochDay = weekStart,
                        todayEpochDay = bundle.day,
                        isCompleted = { hid, epoch -> completionMap[Pair(hid, epoch)] == true },
                    )
                    val companion = CompanionResolver.resolve(
                        todayEpochDay = bundle.day,
                        lastLoggedEpochDay = bundle.inner.profile.lastLoggedEpochDay,
                        streakDays = bundle.inner.profile.streakDays,
                        habitsDoneToday = habitsDone,
                        habitsTotalToday = habitsTotal,
                        questsDoneToday = questsDone,
                        questsTotalToday = questsTotal,
                        onboardingComplete = bundle.inner.profile.onboardingComplete,
                        habitsDoneYesterday = habitsDoneYesterday,
                        questsDoneYesterday = questsDoneYesterday,
                        yesterdayMoodHeadline = moodHeadline,
                    )
                    companionMemoryStore.append(bundle.day, companion.mood)
                    val companionMemoryWhisper =
                        CompanionMemoryWhisper.line(companionMemoryStore.recentMoods())
                    val pathLabel = StarterPaths.labelForStoredId(bundle.inner.profile.starterPath)
                    val firstQuestTitle = quests.firstOrNull()?.title ?: "—"
                    val widgetFlavor = WidgetStoryLines.pick(
                        bundle.day,
                        bundle.homeSnap.settings.flavorPackId,
                    )
                    widgetSnapshotStore.write(
                        level = progress.level,
                        questTitle = firstQuestTitle,
                        bossName = boss.name,
                        flavorLine = widgetFlavor,
                        dailyBoonAvailable = dailyBoonAvailable,
                        actionUri = widgetDeepLinkFor(compass),
                        streakDays = bundle.inner.profile.streakDays,
                        bossSealedThisWeek = bossSealedThisWeek,
                        seasonLine = seasonProgress?.chapterLine.orEmpty(),
                    )
                    val streakMilestone = StreakMilestone.milestoneDays(bundle.inner.profile.streakDays)
                    _ui.value = HomeUiState(
                        profile = bundle.inner.profile,
                        stats = displayStats,
                        rollingStats = rolling,
                        progress = progress,
                        quests = quests,
                        boss = boss,
                        bossSealedThisWeek = bossSealedThisWeek,
                        compass = compass,
                        habits = bundle.inner.habits,
                        todayCompletions = bundle.inner.todayComp,
                        dailyBoonAvailable = dailyBoonAvailable,
                        bossPrepSealsThisWeek = bossPrepSeals,
                        actTitle = narrative.actTitle,
                        starterPathLabel = StarterPaths.labelForStoredId(bundle.inner.profile.starterPath),
                        levelUpSheet = levelUpSheet,
                        suffixPicker = suffixPicker,
                        soundEnabled = bundle.homeSnap.settings.soundEnabled,
                        hapticsEnabled = bundle.homeSnap.settings.hapticsEnabled,
                        familiarEnabled = bundle.homeSnap.settings.familiarEnabled,
                        familiarSpecies = bundle.homeSnap.settings.familiarSpecies,
                        companion = companion,
                        companionMemoryWhisper = companionMemoryWhisper,
                        streakMilestoneDays = streakMilestone,
                        companionPlayStreakDays = companionPlayDayStore.consecutivePlayDaysEnding(bundle.day),
                        showHealthConnectOnboarding =
                            bundle.inner.profile.onboardingComplete &&
                            !privacyPreferences.healthConnectOnboardingSeen.first() &&
                            !bundle.homeSnap.settings.healthConnectSyncEnabled,
                        seasonProgress = seasonProgress,
                        weeklyInsight = weeklyInsight,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun dismissWeeklyInsight() {
        viewModelScope.launch {
            val weekStart = weekStartMondayEpochDay(LocalDate.ofEpochDay(dayFlow.value))
            val insight = _ui.value?.weeklyInsight ?: return@launch
            insightOracleStore.dismissForWeek(weekStart, insight.id)
            _ui.value = _ui.value?.copy(weeklyInsight = null)
        }
    }

    fun dismissHealthConnectOnboarding() {
        viewModelScope.launch {
            privacyPreferences.setHealthConnectOnboardingSeen()
            _ui.value = _ui.value?.copy(showHealthConnectOnboarding = false)
        }
    }

    fun healthConnectPermissionStrings(): Set<String> =
        healthConnectMetricsSync.readPermissions

    fun enableHealthConnectSync() {
        viewModelScope.launch {
            val settings = privacyPreferences.getSettingsSnapshot()
            privacyPreferences.save(settings.copy(healthConnectSyncEnabled = true))
            privacyPreferences.setHealthConnectOnboardingSeen()
            healthConnectMetricsSync.syncIfEnabled(settings.copy(healthConnectSyncEnabled = true))
            _ui.value = _ui.value?.copy(showHealthConnectOnboarding = false)
            refreshToday()
        }
    }

    fun refreshToday() {
        dayFlow.value = todayEpochDay()
    }

    fun dismissLevelUp() {
        val level = _ui.value?.progress?.level ?: return
        viewModelScope.launch {
            privacyPreferences.setLastKnownLevel(level)
            _ui.value = _ui.value?.copy(levelUpSheet = null)
        }
    }

    fun chooseArchetypeSuffix(suffix: String) {
        val p = _ui.value?.profile ?: return
        viewModelScope.launch {
            profileRepository.saveProfile(p.copy(archetypeSuffix = suffix))
            _pickedSuffixThisSession.value = true
            _ui.value = _ui.value?.copy(suffixPicker = null)
        }
    }

    fun dismissSuffixPicker() {
        _pickedSuffixThisSession.value = true
        _ui.value = _ui.value?.copy(suffixPicker = null)
    }

    fun completeQuest(quest: GameQuest) {
        viewModelScope.launch {
            val day = dayFlow.value
            if (questCompletionRepository.completedIds(day).contains(quest.id)) return@launch
            questCompletionRepository.markComplete(quest.id, day)
            xpEngine.award(quest.xpReward, "Quest: ${quest.title}")
            val ui = _ui.value
            if (ui != null) {
                feedbackController.playQuestSeal(ui.soundEnabled, ui.hapticsEnabled)
                val path = _ui.value?.starterPathLabel
                _questSealFlair.value = FeedbackLineFormatter.quest(
                    quest.xpReward,
                    quest.linkedStat,
                    path,
                )
            }
        }
    }

    fun consumeQuestSealFlair() {
        _questSealFlair.value = null
    }

    fun toggleHabit(habitId: Long, completed: Boolean) {
        viewModelScope.launch {
            val day = dayFlow.value
            val wasDone = habitRepository.isCompleted(habitId, day)
            habitRepository.setCompleted(habitId, day, completed)
            if (!completed || wasDone) return@launch
            val habit = habitRepository.getHabit(habitId) ?: return@launch
            val xp = HabitRewards.xpForDifficulty(habit.difficulty)
            xpEngine.award(xp, "Habit: ${habit.name}")
            val ui = _ui.value ?: return@launch
            feedbackController.playHabitSeal(ui.soundEnabled, ui.hapticsEnabled)
            _habitSealFlair.value = FeedbackLineFormatter.habit(
                xp,
                habit.linkedStat,
                ui.starterPathLabel,
            )
        }
    }

    fun consumeHabitSealFlair() {
        _habitSealFlair.value = null
    }

    fun playLevelUpFeedback() {
        val ui = _ui.value ?: return
        feedbackController.playLevelUp(ui.soundEnabled, ui.hapticsEnabled)
    }

    private fun widgetDeepLinkFor(compass: ChronicleCompassDirective): String = when (compass.kind) {
        ChronicleCompassKind.WeeklyReview -> "openascend://weekly"
        ChronicleCompassKind.EveningCheckIn -> "openascend://check_in"
        ChronicleCompassKind.BossEncounter -> "openascend://boss"
        ChronicleCompassKind.TreasuryRitual -> "openascend://treasury"
        else -> "openascend://home"
    }

    private suspend fun loadCompletionMap(
        habits: List<Habit>,
        today: Long,
        daysBack: Long = 6,
    ): Map<Pair<Long, Long>, Boolean> {
        val map = mutableMapOf<Pair<Long, Long>, Boolean>()
        for (offset in 0L..daysBack) {
            val d = today - offset
            for (h in habits) {
                map[h.id to d] = habitRepository.isCompleted(h.id, d)
            }
        }
        return map
    }
}

private data class HomeInnerSnap(
    val profile: UserProfile,
    val habits: List<Habit>,
    val todayComp: Map<Long, Boolean>,
    val yesterdayComp: Map<Long, Boolean>,
    val metric: DailyMetric?,
)

private data class HomeBundle(
    val day: Long,
    val inner: HomeInnerSnap,
    val questToday: Set<String>,
    val questYesterday: Set<String>,
    val homeSnap: com.openascend.data.local.prefs.HomePreferenceSnapshot,
)
