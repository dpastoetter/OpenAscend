package com.openascend.app.ui.sigil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.feedback.FeedbackController
import com.openascend.app.util.todayEpochDay
import com.openascend.data.local.prefs.PrivacyPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Tier A micro-ritual: **Seal the sigil** — tap three runes in order.
 * Success is **flavor + haptics/sound only** (no extra XP; check-in already awarded XP).
 * Completion persists the epoch day in DataStore for a small home-line cosmetic.
 */
@HiltViewModel
class SealSigilViewModel @Inject constructor(
    private val privacyPreferences: PrivacyPreferences,
    private val feedbackController: FeedbackController,
) : ViewModel() {

    private val day = todayEpochDay()

    private val _expectedStep = MutableStateFlow(0)
    val expectedStep = _expectedStep.asStateFlow()

    private val _showWrongOrder = MutableStateFlow(false)
    val showWrongOrder = _showWrongOrder.asStateFlow()

    private val _finished = MutableSharedFlow<SigilFinish>(extraBufferCapacity = 1)
    val finished = _finished.asSharedFlow()

    private var ritualCompletionStarted = false

    fun onRuneTapped(runeIndex: Int) {
        if (ritualCompletionStarted || _expectedStep.value >= 3) return
        val expected = _expectedStep.value
        if (runeIndex == expected) {
            _showWrongOrder.value = false
            val next = expected + 1
            _expectedStep.value = next
            if (next >= 3 && !ritualCompletionStarted) {
                ritualCompletionStarted = true
                viewModelScope.launch { completeRitual() }
            }
        } else {
            _expectedStep.value = 0
            _showWrongOrder.value = true
        }
    }

    fun dismissWrongHint() {
        _showWrongOrder.value = false
    }

    private suspend fun completeRitual() {
        privacyPreferences.setLastSigilRitualEpochDay(day)
        val settings = privacyPreferences.getSettingsSnapshot()
        feedbackController.playSigilRitualComplete(settings.soundEnabled, settings.hapticsEnabled)
        _finished.emit(SigilFinish.Success)
    }

    fun skipRitual() {
        viewModelScope.launch {
            _finished.emit(SigilFinish.Skipped)
        }
    }
}

enum class SigilFinish {
    Success,
    Skipped,
}
