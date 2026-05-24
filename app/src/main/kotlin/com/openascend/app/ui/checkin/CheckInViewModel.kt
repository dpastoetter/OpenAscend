package com.openascend.app.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.feedback.FeedbackController
import com.openascend.app.health.HealthConnectMetricsSync
import com.openascend.app.health.HealthConnectSyncStatus
import com.openascend.app.util.todayEpochDay
import com.openascend.app.util.withStreakAfterLog
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.app.ui.FeedbackLineFormatter
import com.openascend.domain.narrative.StarterPaths
import com.openascend.domain.service.HabitRewards
import com.openascend.domain.service.XpEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class CheckInCore(
    val metric: com.openascend.domain.model.DailyMetric?,
    val habits: List<com.openascend.domain.model.Habit>,
    val completions: Map<Long, Boolean>,
    val settings: com.openascend.domain.model.PrivacySettings,
    val profile: com.openascend.domain.model.UserProfile,
)

data class CheckInSaveEffect(
    val snackbarMessage: String,
    /** First seal of the day: offer optional sigil micro-ritual. */
    val offerSigilRitual: Boolean,
)

data class CheckInUiState(
    val epochDay: Long,
    val sleepHours: String,
    val steps: String,
    val bankControl: String,
    val moneyNote: String,
    val vitality: String,
    val habits: List<com.openascend.domain.model.Habit>,
    val completions: Map<Long, Boolean>,
    val healthConnectEnabled: Boolean,
    val sleepFromHealthConnect: Boolean,
    val stepsFromHealthConnect: Boolean,
    val starterPathLabel: String?,
    val healthConnectStatus: HealthConnectSyncStatus,
)

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val metricsRepository: MetricsRepository,
    private val habitRepository: HabitRepository,
    private val profileRepository: ProfileRepository,
    private val xpEngine: XpEngine,
    private val privacyPreferences: PrivacyPreferences,
    private val feedbackController: FeedbackController,
    private val healthConnectMetricsSync: HealthConnectMetricsSync,
) : ViewModel() {

    private val day = todayEpochDay()

    private val healthConnectStatusFlow =
        MutableStateFlow(HealthConnectSyncStatus.Disabled)

    private val _saveEffects = MutableSharedFlow<CheckInSaveEffect>(extraBufferCapacity = 1)
    val saveEffects = _saveEffects.asSharedFlow()

    private val _bossPrepLore = MutableSharedFlow<String?>(extraBufferCapacity = 1)
    val bossPrepLore = _bossPrepLore.asSharedFlow()

    init {
        privacyPreferences.settings
            .onEach { settings ->
                viewModelScope.launch {
                    healthConnectStatusFlow.value = healthConnectMetricsSync.probeStatus(settings)
                }
            }
            .launchIn(viewModelScope)
    }

    val uiState: StateFlow<CheckInUiState> = combine(
        combine(
            metricsRepository.observeDay(day),
            habitRepository.observeHabits(),
            habitRepository.observeCompletionsForDay(day),
            privacyPreferences.settings,
            profileRepository.observeProfile(),
        ) { metric, habits, completions, settings, profile ->
            CheckInCore(metric, habits, completions, settings, profile)
        },
        healthConnectStatusFlow,
    ) { core, hcStatus ->
        CheckInUiState(
            epochDay = day,
            sleepHours = core.metric?.sleepHours?.toString().orEmpty(),
            steps = core.metric?.steps?.toString().orEmpty(),
            bankControl = core.metric?.bankControlScore?.toString().orEmpty(),
            moneyNote = core.metric?.moneyNote.orEmpty(),
            vitality = core.metric?.vitalityScore?.toString().orEmpty(),
            habits = core.habits,
            completions = core.completions,
            healthConnectEnabled = core.settings.healthConnectSyncEnabled,
            sleepFromHealthConnect = core.settings.healthConnectSyncEnabled &&
                core.metric?.sleepHours != null,
            stepsFromHealthConnect = core.settings.healthConnectSyncEnabled &&
                core.metric?.steps != null,
            starterPathLabel = core.profile.starterPath,
            healthConnectStatus = hcStatus,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CheckInUiState(
            day, "", "", "", "", "", emptyList(), emptyMap(),
            false, false, false, null, HealthConnectSyncStatus.Disabled,
        ),
    )

    fun toggleHabit(habitId: Long, done: Boolean) {
        viewModelScope.launch {
            val wasDone = habitRepository.isCompleted(habitId, day)
            habitRepository.setCompleted(habitId, day, done)
            if (!done || wasDone) return@launch
            val habit = habitRepository.getHabit(habitId) ?: return@launch
            val xp = HabitRewards.xpForDifficulty(habit.difficulty)
            xpEngine.award(xp, "Habit: ${habit.name}")
            val settings = privacyPreferences.settings.first()
            feedbackController.playHabitSeal(settings.soundEnabled, settings.hapticsEnabled)
            if (habit.bossPrep) {
                _bossPrepLore.emit("Boss-prep habit sealed—the weekly encounter takes notice.")
            }
        }
    }

    fun save(
        sleepHours: String,
        steps: String,
        bankControl: String,
        moneyNote: String,
        vitality: String,
        eveningMoodIds: List<String>,
    ) {
        viewModelScope.launch {
            val metric = DailyMetric(
                epochDay = day,
                sleepHours = sleepHours.toFloatOrNull(),
                steps = steps.toIntOrNull(),
                bankControlScore = bankControl.toIntOrNull()?.coerceIn(1, 10),
                moneyNote = moneyNote.ifBlank { null },
                vitalityScore = vitality.toIntOrNull()?.coerceIn(1, 10),
            )
            metricsRepository.upsertDay(metric)
            val profile = profileRepository.getProfile() ?: return@launch
            val prevStreak = profile.streakDays
            val prevLast = profile.lastLoggedEpochDay
            val firstLogOfDay = profile.lastLoggedEpochDay != day
            val updated = profile.withStreakAfterLog(day)
            profileRepository.saveProfile(updated)
            if (eveningMoodIds.isNotEmpty()) {
                privacyPreferences.setEveningMood(
                    eveningMoodIds.joinToString(","),
                    day,
                )
            }
            if (firstLogOfDay) {
                xpEngine.award(12, "Evening check-in sealed")
                val settings = privacyPreferences.settings.first()
                feedbackController.playCheckInSeal(settings.soundEnabled, settings.hapticsEnabled)
            }
            val lore = when {
                updated.streakDays > prevStreak ->
                    "The chronicle notes your streak holding—armor thickens in the tale."
                prevStreak > 0 && updated.streakDays == 1 &&
                    prevLast != null && prevLast < day - 1 ->
                    "The path had a gap; your streak reset kindly—fresh ink, no shame."
                else -> null
            }
            val pathLabel = StarterPaths.labelForStoredId(profile.starterPath)
            val snackbarMessage = lore ?: FeedbackLineFormatter.checkIn(pathLabel)
            _saveEffects.emit(
                CheckInSaveEffect(
                    snackbarMessage = snackbarMessage,
                    offerSigilRitual = firstLogOfDay,
                ),
            )
        }
    }
}
