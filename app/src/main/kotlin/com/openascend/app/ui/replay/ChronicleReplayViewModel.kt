package com.openascend.app.ui.replay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.util.todayEpochDay
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.domain.narrative.EveningMoodCopy
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.repository.QuestCompletionRepository
import com.openascend.domain.service.ChronicleReplayDay
import com.openascend.domain.service.ChronicleReplayResolver
import com.openascend.domain.service.StatComputationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChronicleReplayUiState(
    val heroName: String,
    val days: List<ChronicleReplayDay>,
)

@HiltViewModel
class ChronicleReplayViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val metricsRepository: MetricsRepository,
    private val habitRepository: HabitRepository,
    private val questCompletionRepository: QuestCompletionRepository,
    private val statComputation: StatComputationService,
    private val privacyPreferences: PrivacyPreferences,
) : ViewModel() {

    private val _ui = MutableStateFlow<ChronicleReplayUiState?>(null)
    val uiState = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            val day = todayEpochDay()
            val profile = profileRepository.getProfile() ?: return@launch
            val habits = habitRepository.observeHabits().first()
            val start = day - 13
            val metrics = metricsRepository.metricsBetween(start, day)
            val metricsByDay = metrics.associateBy { it.epochDay }
            val homeSnap = privacyPreferences.homeSnapshot.first()
            val habitCompletionByDay = mutableMapOf<Long, Map<Long, Boolean>>()
            for (d in start..day) {
                val map = mutableMapOf<Long, Boolean>()
                for (h in habits) {
                    map[h.id] = habitRepository.isCompleted(h.id, d)
                }
                habitCompletionByDay[d] = map
            }
            val questIdsByDay = (start..day).associateWith { d ->
                questCompletionRepository.completedIds(d)
            }
            val days = ChronicleReplayResolver.buildLastDays(
                todayEpochDay = day,
                days = 14,
                metricsByDay = metricsByDay,
                habits = habits,
                habitCompleted = { hid, epoch ->
                    habitCompletionByDay[epoch]?.get(hid) == true
                },
                questCompleted = { epoch ->
                    profile.lastLoggedEpochDay == epoch ||
                        questIdsByDay[epoch].orEmpty().isNotEmpty()
                },
                moodHeadlineForDay = { epoch ->
                    if (homeSnap.eveningMoodEpochDay == epoch) {
                        EveningMoodCopy.headlineForYesterday(homeSnap.eveningMoodIds)
                    } else {
                        null
                    }
                },
                computeStats = { metric, h, completions ->
                    statComputation.computeToday(metric, h, completions)
                },
            )
            _ui.value = ChronicleReplayUiState(profile.displayName, days)
        }
    }
}
