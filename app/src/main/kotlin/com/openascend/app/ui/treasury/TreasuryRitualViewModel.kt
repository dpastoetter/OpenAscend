package com.openascend.app.ui.treasury

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.util.todayEpochDay
import com.openascend.app.util.weekStartMondayEpochDay
import java.time.LocalDate
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.narrative.NarrativeRepository
import com.openascend.domain.narrative.TreasuryPrompts
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TreasuryChoice { Win, Leak, Intention }

data class TreasuryRitualUiState(
    val prompts: TreasuryPrompts,
    val saved: Boolean,
)

@HiltViewModel
class TreasuryRitualViewModel @Inject constructor(
    private val metricsRepository: MetricsRepository,
    private val profileRepository: ProfileRepository,
    private val narrativeRepository: NarrativeRepository,
    private val privacyPreferences: PrivacyPreferences,
) : ViewModel() {

    private val day = todayEpochDay()
    private val _ui = MutableStateFlow<TreasuryRitualUiState?>(null)
    val uiState = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = privacyPreferences.settings.first()
            val pack = narrativeRepository.loadPack(settings.flavorPackId)
            _ui.value = TreasuryRitualUiState(
                prompts = pack.treasuryPrompts,
                saved = privacyPreferences.treasuryRitualDoneForWeek(
                    weekStartMondayEpochDay(LocalDate.ofEpochDay(day)),
                ),
            )
        }
    }

    fun save(choice: TreasuryChoice, note: String) {
        viewModelScope.launch {
            val existing = metricsRepository.getDay(day)
            val (score, prefix) = when (choice) {
                TreasuryChoice.Win -> 8 to "Win"
                TreasuryChoice.Leak -> 4 to "Leak"
                TreasuryChoice.Intention -> 6 to "Intention"
            }
            val mergedNote = listOf(prefix, note.trim()).filter { it.isNotBlank() }.joinToString(": ")
            metricsRepository.upsertDay(
                DailyMetric(
                    epochDay = day,
                    sleepHours = existing?.sleepHours,
                    steps = existing?.steps,
                    bankControlScore = score,
                    moneyNote = mergedNote.ifBlank { existing?.moneyNote },
                    vitalityScore = existing?.vitalityScore,
                ),
            )
            privacyPreferences.markTreasuryRitualWeek(weekStartMondayEpochDay(LocalDate.ofEpochDay(day)))
            profileRepository.getProfile()?.let { /* touch profile for streak context */ }
            _ui.value = _ui.value?.copy(saved = true)
        }
    }
}
