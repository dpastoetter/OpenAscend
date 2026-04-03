package com.openascend.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val displayName: String,
    val onboardingComplete: Boolean,
    val goalsJson: String,
    val streakDays: Int,
    /** Last day the player logged a check-in (epoch day). */
    val lastLoggedEpochDay: Long?,
    /** Path under app files dir; null if no custom photo. */
    val avatarRelativePath: String? = null,
)
