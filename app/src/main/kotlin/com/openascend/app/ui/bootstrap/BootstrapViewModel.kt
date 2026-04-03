package com.openascend.app.ui.bootstrap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BootstrapViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _targetRoute = MutableStateFlow<String?>(null)
    val targetRoute = _targetRoute.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = profileRepository.getProfile()
            _targetRoute.value = if (profile?.onboardingComplete == true) {
                com.openascend.app.navigation.Routes.Home
            } else {
                com.openascend.app.navigation.Routes.Onboarding
            }
        }
    }
}
