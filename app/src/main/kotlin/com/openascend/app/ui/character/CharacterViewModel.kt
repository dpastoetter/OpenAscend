package com.openascend.app.ui.character

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.util.todayEpochDay
import com.openascend.domain.model.CharacterProgress
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.Habit
import com.openascend.domain.model.StatBlock
import com.openascend.domain.model.UserProfile
import com.openascend.domain.model.XpEvent
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.app.media.ProfileAvatarImporter
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.repository.XpRepository
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
import javax.inject.Inject

data class CharacterUiState(
    val profile: UserProfile,
    val stats: StatBlock,
    val progress: CharacterProgress,
    val xpLog: List<XpEvent>,
)

private data class CharSnap(
    val profile: UserProfile,
    val habits: List<Habit>,
    val completions: Map<Long, Boolean>,
    val metric: DailyMetric?,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val profileAvatarImporter: ProfileAvatarImporter,
    private val habitRepository: HabitRepository,
    private val metricsRepository: MetricsRepository,
    private val xpRepository: XpRepository,
    private val statComputation: StatComputationService,
    private val xpEngine: XpEngine,
) : ViewModel() {

    private val dayFlow = MutableStateFlow(todayEpochDay())

    private val _ui = MutableStateFlow<CharacterUiState?>(null)
    val uiState = _ui.asStateFlow()

    init {
        dayFlow
            .flatMapLatest { day ->
                combine(
                    combine(
                        profileRepository.observeProfile(),
                        habitRepository.observeHabits(),
                        habitRepository.observeCompletionsForDay(day),
                        metricsRepository.observeDay(day),
                    ) { profile, habits, completions, metric ->
                        CharSnap(profile, habits, completions, metric)
                    },
                    xpRepository.observeEvents(50),
                ) { snap, xpLog -> snap to xpLog }
            }
            .onEach { (snap, xpLog) ->
                viewModelScope.launch {
                    val stats = statComputation.computeToday(snap.metric, snap.habits, snap.completions)
                    val progress = xpEngine.progressForStats(stats, snap.profile.streakDays)
                    _ui.value = CharacterUiState(snap.profile, stats, progress, xpLog)
                }
            }
            .launchIn(viewModelScope)
    }

    fun importAvatar(uri: Uri) {
        viewModelScope.launch {
            if (!profileAvatarImporter.importFrom(uri)) return@launch
            val p = profileRepository.getProfile() ?: return@launch
            profileRepository.saveProfile(
                p.copy(avatarRelativePath = ProfileAvatarImporter.RELATIVE_PATH),
            )
        }
    }

    fun clearAvatar() {
        viewModelScope.launch {
            profileAvatarImporter.deleteStoredFile()
            val p = profileRepository.getProfile() ?: return@launch
            profileRepository.saveProfile(p.copy(avatarRelativePath = null))
        }
    }
}
