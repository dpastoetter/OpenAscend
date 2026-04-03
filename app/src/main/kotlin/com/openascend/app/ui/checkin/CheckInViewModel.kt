package com.openascend.app.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.feedback.FeedbackController
import com.openascend.app.util.todayEpochDay
import com.openascend.app.util.withStreakAfterLog
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.service.XpEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckInUiState(
    val epochDay: Long,
    val sleepHours: String,
    val steps: String,
    val bankControl: String,
    val moneyNote: String,
    val vitality: String,
    val habits: List<com.openascend.domain.model.Habit>,
    val completions: Map<Long, Boolean>,
)

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val metricsRepository: MetricsRepository,
    private val habitRepository: HabitRepository,
    private val profileRepository: ProfileRepository,
    private val xpEngine: XpEngine,
    private val privacyPreferences: PrivacyPreferences,
    private val feedbackController: FeedbackController,
) : ViewModel() {

    private val day = todayEpochDay()

    private val _streakLore = MutableSharedFlow<String?>(extraBufferCapacity = 1)
    val streakLore = _streakLore.asSharedFlow()

    val uiState: StateFlow<CheckInUiState> = combine(
        metricsRepository.observeDay(day),
        habitRepository.observeHabits(),
        habitRepository.observeCompletionsForDay(day),
    ) { metric, habits, completions ->
        CheckInUiState(
            epochDay = day,
            sleepHours = metric?.sleepHours?.toString().orEmpty(),
            steps = metric?.steps?.toString().orEmpty(),
            bankControl = metric?.bankControlScore?.toString().orEmpty(),
            moneyNote = metric?.moneyNote.orEmpty(),
            vitality = metric?.vitalityScore?.toString().orEmpty(),
            habits = habits,
            completions = completions,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CheckInUiState(day, "", "", "", "", "", emptyList(), emptyMap()),
    )

    fun toggleHabit(habitId: Long, done: Boolean) {
        viewModelScope.launch {
            habitRepository.setCompleted(habitId, day, done)
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
                feedbackController.playSeal(settings.soundEnabled, settings.hapticsEnabled)
            }
            val lore = when {
                updated.streakDays > prevStreak ->
                    "The chronicle notes your streak holding—armor thickens in the tale."
                prevStreak > 0 && updated.streakDays == 1 &&
                    prevLast != null && prevLast < day - 1 ->
                    "The path had a gap; your streak reset kindly—fresh ink, no shame."
                else -> null
            }
            _streakLore.emit(lore)
        }
    }
}
