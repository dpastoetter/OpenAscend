package com.openascend.app.ui.raid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.util.todayEpochDay
import com.openascend.data.export.ChronicleDuelCodec
import com.openascend.data.export.ChronicleDuelSummary
import com.openascend.data.export.ChronicleRaidCodec
import com.openascend.domain.model.StatBlock
import com.openascend.domain.raid.ChronicleRaidResolver
import com.openascend.domain.raid.ChronicleRaidResult
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
import java.time.LocalDate
import javax.inject.Inject

data class ChronicleRaidUiState(
    val hostName: String,
    val members: List<ChronicleDuelSummary>,
    val result: ChronicleRaidResult?,
)

@HiltViewModel
class ChronicleRaidViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val metricsRepository: MetricsRepository,
    private val habitRepository: HabitRepository,
    private val statComputation: StatComputationService,
    private val bossGenerator: BossGenerator,
    private val narrativeRepository: NarrativeRepository,
    private val privacyPreferences: PrivacyPreferences,
    private val xpEngine: XpEngine,
) : ViewModel() {

    private val _ui = MutableStateFlow<ChronicleRaidUiState?>(null)
    val uiState = _ui.asStateFlow()

    init {
        viewModelScope.launch { refreshHost() }
    }

    private suspend fun refreshHost() {
        val you = buildYouSummary() ?: return
        _ui.value = ChronicleRaidUiState(
            hostName = you.displayName,
            members = listOf(you),
            result = null,
        )
    }

    fun importMemberJson(text: String) {
        viewModelScope.launch {
            val summary = ChronicleRaidCodec.decodeMember(text) ?: return@launch
            val current = _ui.value ?: return@launch
            if (current.members.size >= ChronicleRaidResolver.MAX_PARTY_SIZE) return@launch
            if (current.members.any { it.displayName == summary.displayName }) return@launch
            val updated = current.members + summary
            _ui.value = current.copy(
                members = updated,
                result = computeResult(updated),
            )
        }
    }

    fun exportHostJson(): String {
        val host = _ui.value?.members?.firstOrNull() ?: return "{}"
        return ChronicleRaidCodec.encodeMember(host)
    }

    private suspend fun buildYouSummary(): ChronicleDuelSummary? {
        val day = todayEpochDay()
        val profile = profileRepository.getProfile() ?: return null
        val habits = habitRepository.observeHabits().first()
        val metrics = metricsRepository.metricsBetween(day - 6, day)
        val completionMap = mutableMapOf<Pair<Long, Long>, Boolean>()
        for (offset in 0L..6L) {
            val d = day - offset
            for (h in habits) {
                completionMap[h.id to d] = habitRepository.isCompleted(h.id, d)
            }
        }
        val stats = statComputation.computeRollingSevenDay(
            lastSevenDays = metrics,
            habits = habits,
            isHabitCompleted = { hid, epoch -> completionMap[hid to epoch] == true },
            todayEpochDay = day,
        )
        val settings = privacyPreferences.settings.first()
        val pack = narrativeRepository.loadPack(settings.flavorPackId)
        val boss = bossGenerator.weeklyBoss(
            weekStartEpochDay = day - 6,
            stats = stats,
            narrative = NarrativeContext(LocalDate.ofEpochDay(day), pack),
        )
        val progress = xpEngine.progressForStats(stats, profile.streakDays)
        return ChronicleDuelSummary(
            displayName = profile.displayName,
            level = progress.level,
            recovery = stats.recovery,
            stamina = stats.stamina,
            stability = stats.stability,
            discipline = stats.discipline,
            vitality = stats.vitality,
            bossName = boss.name,
            weekLabel = LocalDate.ofEpochDay(day).toString(),
        )
    }

    private fun computeResult(members: List<ChronicleDuelSummary>): ChronicleRaidResult {
        val pairs = members.map { summary ->
            summary.displayName to StatBlock(
                recovery = summary.recovery,
                stamina = summary.stamina,
                stability = summary.stability,
                discipline = summary.discipline,
                vitality = summary.vitality,
            )
        }
        val bossName = members.firstOrNull()?.bossName ?: "The Raid Warden"
        return ChronicleRaidResolver.resolve(bossName, pairs)
    }
}
