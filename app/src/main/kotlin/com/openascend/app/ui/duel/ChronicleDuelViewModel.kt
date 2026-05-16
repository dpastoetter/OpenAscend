package com.openascend.app.ui.duel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.util.todayEpochDay
import com.openascend.app.util.weekStartMondayEpochDay
import com.openascend.data.export.ChronicleDuelSummary
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.service.BossGenerator
import com.openascend.domain.service.StatComputationService
import com.openascend.domain.service.XpEngine
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativeRepository
import com.openascend.data.local.prefs.PrivacyPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.openascend.data.export.ChronicleDuelCodec
import java.time.LocalDate
import javax.inject.Inject

data class ChronicleDuelUiState(
    val you: ChronicleDuelSummary,
    val them: ChronicleDuelSummary?,
)

@HiltViewModel
class ChronicleDuelViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val metricsRepository: MetricsRepository,
    private val habitRepository: HabitRepository,
    private val statComputation: StatComputationService,
    private val bossGenerator: BossGenerator,
    private val narrativeRepository: NarrativeRepository,
    private val privacyPreferences: PrivacyPreferences,
    private val xpEngine: XpEngine,
) : ViewModel() {

    private val _ui = MutableStateFlow<ChronicleDuelUiState?>(null)
    val uiState = _ui.asStateFlow()

    init {
        viewModelScope.launch { refreshYou() }
    }

    private suspend fun refreshYou() {
        val day = todayEpochDay()
        val profile = profileRepository.getProfile() ?: return
        val habits = habitRepository.observeHabits().first()
        val metrics = metricsRepository.metricsBetween(day - 6, day)
        val completionMap = mutableMapOf<Pair<Long, Long>, Boolean>()
        for (offset in 0L..6L) {
            val d = day - offset
            for (h in habits) {
                completionMap[h.id to d] = habitRepository.isCompleted(h.id, d)
            }
        }
        val rolling = statComputation.computeRollingSevenDay(
            lastSevenDays = metrics,
            habits = habits,
            isHabitCompleted = { hid, epoch -> completionMap[Pair(hid, epoch)] == true },
            todayEpochDay = day,
        )
        val homeSnap = privacyPreferences.homeSnapshot.first()
        val weekStart = weekStartMondayEpochDay()
        val pack = narrativeRepository.loadPack(homeSnap.settings.flavorPackId)
        val narrative = NarrativeContext(LocalDate.ofEpochDay(day), pack)
        val boss = bossGenerator.weeklyBoss(
            weekStartEpochDay = weekStart,
            stats = rolling,
            narrative = narrative,
            bossDeferredForThisWeek = homeSnap.deferredBossWeekStart == weekStart,
            bossSealedThisWeek = homeSnap.bossRitualSealedWeekStart == weekStart,
        )
        val progress = xpEngine.progressForStats(rolling, profile.streakDays)
        val you = ChronicleDuelSummary(
            displayName = profile.displayName,
            level = progress.level,
            recovery = rolling.recovery,
            stamina = rolling.stamina,
            stability = rolling.stability,
            discipline = rolling.discipline,
            vitality = rolling.vitality,
            bossName = boss.name,
            weekLabel = weekStart.toString(),
        )
        _ui.value = ChronicleDuelUiState(you = you, them = _ui.value?.them)
    }

    suspend fun exportDuelJson(): String {
        val you = _ui.value?.you ?: return "{}"
        return ChronicleDuelCodec.encode(you)
    }

    fun importDuelJson(text: String): Boolean {
        val them = ChronicleDuelCodec.decode(text) ?: return false
        val you = _ui.value?.you ?: return false
        _ui.value = ChronicleDuelUiState(you = you, them = them)
        return true
    }
}
