package com.openascend.app.ui.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.app.util.todayEpochDay
import com.openascend.data.local.prefs.PrivacyPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CompanionHubUiState(
    val dailyBoonAvailable: Boolean,
)

@HiltViewModel
class CompanionHubViewModel @Inject constructor(
    privacyPreferences: PrivacyPreferences,
) : ViewModel() {

    private val day = todayEpochDay()

    val uiState = privacyPreferences.homeSnapshot
        .map { snap ->
            CompanionHubUiState(
                dailyBoonAvailable = snap.companionTreatXpEpochDay != day,
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            CompanionHubUiState(dailyBoonAvailable = true),
        )
}
