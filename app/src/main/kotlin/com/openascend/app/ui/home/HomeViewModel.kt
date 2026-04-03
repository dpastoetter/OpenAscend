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
import com.openascend.domain.narrative.ArchetypeSuffixCatalog
import com.openascend.domain.narrative.EveningMoodCopy
import com.openascend.domain.narrative.LevelUpFlair
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativeRepository
import com.openascend.domain.narrative.OmenPhrases
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

    private val _pickedSuffixThisSession = MutableStateFlow(false)

    init {
        dayFlow
            .flatMapLatest { day ->
                val core = combine(
                    profileRepository.observeProfile(),
                    habitRepository.observeHabits(),
                    habitRepository.observeCompletionsForDay(day),
                    metricsRepository.observeDay(day),
                    questCompletionRepository.observeCompletedIds(day),
                ) { profile, habits, completions, metric, questDone ->
                    Quintuple(profile, habits, completions, metric, questDone)
                }
                combine(core, privacyPreferences.homeSnapshot) { q, homeSnap ->
                    Snapshot(q.a, q.b, day, q.c, q.d, q.e, homeSnap)
                }
            }
            .onEach { snap ->
                viewModelScope.launch {
                    healthConnectMetricsSync.syncIfEnabled(snap.homeSnap.settings)
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
                    val pack = narrativeRepository.loadPack(snap.homeSnap.settings.flavorPackId)
                    val localDate = LocalDate.ofEpochDay(snap.day)
                    val narrative = NarrativeContext(localDate, pack)
                    val weekStart = weekStartMondayEpochDay(localDate)
                    val bossDeferred = snap.homeSnap.deferredBossWeekStart == weekStart
                    val boss = bossGenerator.weeklyBoss(
                        weekStartEpochDay = weekStart,
                        stats = rolling,
                        narrative = narrative,
                        bossDeferredForThisWeek = bossDeferred,
                    )
                    val questDoneByDay = (1L..3L).associate { off ->
                        val d = snap.day - off
                        d to questCompletionRepository.completedIds(d)
                    }
                    val chain = QuestChainDetector.recoveryChainActive(snap.day) { d ->
                        questDoneByDay[d].orEmpty()
                    }
                    val quests = questGenerator.dailyQuests(
                        stats = todayStats,
                        goals = snap.profile.goals,
                        todayEpochDay = snap.day,
                        completions = snap.questDone,
                        narrative = narrative,
                        recoveryChainActive = chain,
                    )
                    val progress = xpEngine.progressForStats(todayStats, snap.profile.streakDays)
                    val habitSeed = snap.habits.fold(0L) { acc, h -> acc xor h.id * 31 }
                    val focusHabit = snap.habits.firstOrNull { !(snap.completions[it.id] ?: false) }
                        ?: snap.habits.firstOrNull()
                    val omenText = OmenPhrases.pick(snap.day + habitSeed, focusHabit?.name.orEmpty())
                    val dismissedForToday = snap.homeSnap.omenEpochDay == snap.day
                    val showOmen = !dismissedForToday || snap.homeSnap.omenPinned
                    val moodHeadline = if (snap.homeSnap.eveningMoodEpochDay == snap.day - 1) {
                        EveningMoodCopy.headlineForYesterday(snap.homeSnap.eveningMoodIds)
                    } else {
                        null
                    }
                    val storedLevel = snap.homeSnap.lastKnownLevel
                    if (storedLevel == null) {
                        privacyPreferences.setLastKnownLevel(progress.level)
                    }
                    val levelUpSheet = if (storedLevel != null && progress.level > storedLevel) {
                        val arch = progress.archetype.displayName +
                            snap.profile.archetypeSuffix?.let { " · $it" }.orEmpty()
                        LevelUpSheetData(
                            newLevel = progress.level,
                            compliment = LevelUpFlair.compliment(progress.level, snap.profile.displayName),
                            archetypeDisplay = arch,
                        )
                    } else {
                        null
                    }
                    val band = ArchetypeSuffixCatalog.bandForLevel(progress.level)
                    val suffixPicker = if (
                        band != null &&
                        snap.profile.archetypeSuffix == null &&
                        !_pickedSuffixThisSession.value
                    ) {
                        SuffixPickerData(band, ArchetypeSuffixCatalog.choicesForBand(band))
                    } else {
                        null
                    }
                    val habitsDone = snap.habits.count { snap.completions[it.id] == true }
                    val habitsTotal = snap.habits.size
                    val questsDone = quests.count { it.completed }
                    val questsTotal = quests.size
                    val companion = CompanionResolver.resolve(
                        todayEpochDay = snap.day,
                        lastLoggedEpochDay = snap.profile.lastLoggedEpochDay,
                        streakDays = snap.profile.streakDays,
                        habitsDoneToday = habitsDone,
                        habitsTotalToday = habitsTotal,
                        questsDoneToday = questsDone,
                        questsTotalToday = questsTotal,
                        onboardingComplete = snap.profile.onboardingComplete,
                    )
                    val firstQuestTitle = quests.firstOrNull()?.title ?: "—"
                    widgetSnapshotStore.write(
                        level = progress.level,
                        questTitle = firstQuestTitle,
                        bossName = boss.name,
                    )
                    _ui.value = HomeUiState(
                        profile = snap.profile,
                        stats = todayStats,
                        rollingStats = rolling,
                        progress = progress,
                        quests = quests,
                        boss = boss,
                        todayEpochDay = snap.day,
                        actTitle = narrative.actTitle,
                        omenLine = omenText,
                        showOmenCard = showOmen,
                        omenPinned = snap.homeSnap.omenPinned,
                        moodHeadline = moodHeadline,
                        levelUpSheet = levelUpSheet,
                        suffixPicker = suffixPicker,
                        soundEnabled = snap.homeSnap.settings.soundEnabled,
                        hapticsEnabled = snap.homeSnap.settings.hapticsEnabled,
                        familiarEnabled = snap.homeSnap.settings.familiarEnabled,
                        familiarSpecies = snap.homeSnap.settings.familiarSpecies,
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
                feedbackController.playSeal(ui.soundEnabled, ui.hapticsEnabled)
            }
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

private data class Quintuple<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E,
)

private data class Snapshot(
    val profile: UserProfile,
    val habits: List<Habit>,
    val day: Long,
    val completions: Map<Long, Boolean>,
    val metric: com.openascend.domain.model.DailyMetric?,
    val questDone: Set<String>,
    val homeSnap: com.openascend.data.local.prefs.HomePreferenceSnapshot,
)
