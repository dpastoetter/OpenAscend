package com.openascend.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.data.export.UserDataExporter
import com.openascend.domain.model.PrivacySettings
import com.openascend.domain.model.ThemePreference
import com.openascend.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val privacy: PrivacySettings,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val userDataExporter: UserDataExporter,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = profileRepository.observePrivacy()
        .map { SettingsUiState(privacy = it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsUiState(
                privacy = PrivacySettings(false, false, true, ThemePreference.SYSTEM),
            ),
        )

    fun setPrivacy(settings: PrivacySettings) {
        viewModelScope.launch { profileRepository.savePrivacy(settings) }
    }

    suspend fun exportJson(): String = userDataExporter.buildJson()
}
