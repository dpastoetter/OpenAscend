package com.openascend.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.domain.model.ThemePreference
import com.openascend.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    profileRepository: ProfileRepository,
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> =
        profileRepository.observePrivacy()
            .map { it.themePreference }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                ThemePreference.SYSTEM,
            )
}
