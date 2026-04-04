package com.openascend.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.feedback.FeedbackController
import com.openascend.app.health.HealthConnectMetricsSync
import com.openascend.app.util.todayEpochDay
import com.openascend.app.util.weekStartMondayEpochDay
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.data.local.prefs.WidgetSnapshotStore
import com.openascend.domain.model.CharacterProgress
import com.openascend.domain.model.GameQuest
import com.openascend.domain.model.Habit
import com.openascend.domain.model.StatBlock
import com.openascend.domain.model.UserProfile
import com.openascend.domain.model.FamiliarSpecies
import com.openascend.domain.model.WeeklyBoss
import com.openascend.domain.companion.CompanionResolver
import com.openascend.domain.companion.CompanionSnapshot
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.narrative.ActResolver
import com.openascend.domain.narrative.ArchetypeSuffixCatalog
import com.openascend.domain.narrative.BossWeekArc
import com.openascend.domain.narrative.DailySigilRecap
import com.openascend.domain.narrative.EveningMoodCopy
import com.openascend.domain.narrative.LevelUpFlair
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativeRepository
import com.openascend.domain.narrative.OmenPhrases
import com.openascend.domain.narrative.QuestSealFlair
import com.openascend.domain.narrative.StarterPaths
import com.openascend.domain.narrative.StreakArmorLore
import com.openascend.domain.narrative.WidgetStoryLines
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.repository.QuestCompletionRepository
import com.openascend.domain.service.BossGenerator
import com.openascend.domain.service.QuestChainDetector
import com.openascend.domain.service.QuestGenerator
import com.openascend.domain.service.StatComputationService
import com.openascend.domain.service.XpEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val todayEpochDay: Long,
    val actTitle: String,
    val actDaysRemaining: Int,
    val bossWeekBanner: String?,
    val streakArmorChip: String?,
    val starterPathLabel: String?,
    /** Shown when new calendar day or pinned */
    val omenLine: String?,
    val showOmenCard: Boolean,
    val omenPinned: Boolean,
    val moodHeadline: String?,
    val levelUpSheet: LevelUpSheetData?,
    val suffixPicker: SuffixPickerData?,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val familiarEnabled: Boolean,
    val familiarSpecies: FamiliarSpecies,
    val companion: CompanionSnapshot,
)

@OptIn(ExperimentalCoroutinesApi::class)
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
    private val narrativeRepository: NarrativeRepository,
    private val privacyPreferences: PrivacyPreferences,
    private val widgetSnapshotStore: WidgetSnapshotStore,
    private val feedbackController: FeedbackController,
    private val healthConnectMetricsSync: HealthConnectMetricsSync,
) : ViewModel() {

    private val dayFlow = MutableStateFlow(todayEpochDay())

    private val _ui = MutableStateFlow<HomeUiState?>(null)
    val uiState = _ui.asStateFlow()

    private val _questSealFlair = MutableStateFlow<String?>(null)
    val questSealFlair = _questSealFlair.asStateFlow()

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
                ) { inner, questToday, questYesterday, homeSnap ->
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
                    val boss = bossGenerator.weeklyBoss(
                        weekStartEpochDay = weekStart,
                        stats = rolling,
                        narrative = narrative,
                        bossDeferredForThisWeek = bossDeferred,
                    )
                    val questDoneByDay = (1L..3L).associate { off ->
                        val d = bundle.day - off
                        d to questCompletionRepository.completedIds(d)
                    }
                    val chain = QuestChainDetector.recoveryChainActive(bundle.day) { d ->
                        questDoneByDay[d].orEmpty()
                    }
                    val quests = questGenerator.dailyQuests(
                        stats = todayStats,
                        goals = bundle.inner.profile.goals,
                        todayEpochDay = bundle.day,
                        completions = bundle.questToday,
                        narrative = narrative,
                        recoveryChainActive = chain,
                    )
                    val progress = xpEngine.progressForStats(todayStats, bundle.inner.profile.streakDays)
                    val habitSeed = bundle.inner.habits.fold(0L) { acc, h -> acc xor h.id * 31 }
                    val focusHabit = bundle.inner.habits.firstOrNull {
                        !(bundle.inner.todayComp[it.id] ?: false)
                    } ?: bundle.inner.habits.firstOrNull()
                    val omenText = OmenPhrases.pick(bundle.day + habitSeed, focusHabit?.name.orEmpty())
                    val dismissedForToday = bundle.homeSnap.omenEpochDay == bundle.day
                    val showOmen = !dismissedForToday || bundle.homeSnap.omenPinned
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
                    )
                    val actDaysRemaining = ActResolver.daysRemainingInAct(localDate)
                    val bossWeekBanner = BossWeekArc.homeBannerLine(
                        today = localDate,
                        bossTargetStat = boss.targetStat,
                        bossDeferredThisWeek = bossDeferred,
                    )
                    val streakArmorChip = if (progress.streakArmor >= 3) {
                        StreakArmorLore.chipLine(progress.streakArmor)
                    } else {
                        null
                    }
                    _ui.value = HomeUiState(
                        profile = bundle.inner.profile,
                        stats = todayStats,
                        rollingStats = rolling,
                        progress = progress,
                        quests = quests,
                        boss = boss,
                        todayEpochDay = bundle.day,
                        actTitle = narrative.actTitle,
                        actDaysRemaining = actDaysRemaining,
                        bossWeekBanner = bossWeekBanner,
                        streakArmorChip = streakArmorChip,
                        starterPathLabel = StarterPaths.labelForStoredId(bundle.inner.profile.starterPath),
                        omenLine = omenText,
                        showOmenCard = showOmen,
                        omenPinned = bundle.homeSnap.omenPinned,
                        moodHeadline = moodHeadline,
                        levelUpSheet = levelUpSheet,
                        suffixPicker = suffixPicker,
                        soundEnabled = bundle.homeSnap.settings.soundEnabled,
                        hapticsEnabled = bundle.homeSnap.settings.hapticsEnabled,
                        familiarEnabled = bundle.homeSnap.settings.familiarEnabled,
                        familiarSpecies = bundle.homeSnap.settings.familiarSpecies,
                        companion = companion,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshToday() {
        dayFlow.value = todayEpochDay()
    }

    fun dismissOmenForToday() {
        viewModelScope.launch {
            privacyPreferences.setOmenDismissed(dayFlow.value)
        }
    }

    fun setOmenPinned(pinned: Boolean) {
        viewModelScope.launch {
            privacyPreferences.setOmenPinned(pinned)
        }
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
                _questSealFlair.value = QuestSealFlair.line(quest.linkedStat)
            }
        }
    }

    fun consumeQuestSealFlair() {
        _questSealFlair.value = null
    }

    fun playLevelUpFeedback() {
        val ui = _ui.value ?: return
        feedbackController.playLevelUp(ui.soundEnabled, ui.hapticsEnabled)
    }

    fun buildDailySigilText(): String {
        val u = _ui.value ?: return ""
        val arch = u.progress.archetype.displayName +
            u.profile.archetypeSuffix?.let { " · $it" }.orEmpty()
        val sealed = u.quests.count { it.completed }
        val total = u.quests.size
        return DailySigilRecap.build(
            displayName = u.profile.displayName,
            level = u.progress.level,
            actTitle = u.actTitle,
            questsSealed = sealed,
            questsTotal = total,
            moodHeadlineYesterday = u.moodHeadline,
            archetypeLine = arch,
        )
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
