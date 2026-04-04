package com.openascend.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val displayName: String,
    val onboardingComplete: Boolean,
    val goals: List<String>,
    val streakDays: Int,
    val lastLoggedEpochDay: Long? = null,
    /** Path under app `filesDir` (e.g. `avatars/profile.jpg`), or null if unset. */
    val avatarRelativePath: String? = null,
    /** Cosmetic RPG suffix chosen at level bands, e.g. "of the Quiet Hearth". */
    val archetypeSuffix: String? = null,
    /** Onboarding "class fantasy" id ([com.openascend.domain.narrative.StarterPaths]). */
    val starterPath: String? = null,
)
