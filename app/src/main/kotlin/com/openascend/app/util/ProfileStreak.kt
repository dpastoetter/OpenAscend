package com.openascend.app.util

import com.openascend.domain.model.UserProfile

fun UserProfile.withStreakAfterLog(epochDay: Long): UserProfile = when {
    lastLoggedEpochDay == epochDay -> this
    lastLoggedEpochDay == null -> copy(streakDays = 1, lastLoggedEpochDay = epochDay)
    lastLoggedEpochDay == epochDay - 1 -> copy(
        streakDays = streakDays + 1,
        lastLoggedEpochDay = epochDay,
    )
    else -> copy(streakDays = 1, lastLoggedEpochDay = epochDay)
}
