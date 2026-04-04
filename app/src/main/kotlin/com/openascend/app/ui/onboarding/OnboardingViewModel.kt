package com.openascend.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.domain.model.UserProfile
import com.openascend.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    fun complete(displayName: String, goals: List<String>, starterPathId: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            val base = profileRepository.getProfile()
                ?: UserProfile(
                    displayName = "Traveler",
                    onboardingComplete = false,
                    goals = emptyList(),
                    streakDays = 0,
                    starterPath = null,
                )
            profileRepository.saveProfile(
                base.copy(
                    displayName = displayName.ifBlank { "Traveler" },
                    onboardingComplete = true,
                    goals = goals.filter { it.isNotBlank() }.take(5),
                    starterPath = starterPathId,
                ),
            )
            onDone()
        }
    }
}
