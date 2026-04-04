package com.openascend.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PrivacySettings(
    val analyticsOptIn: Boolean,
    val crashReportsOptIn: Boolean,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    /** Asset pack under `narrative/{id}.json`. */
    val flavorPackId: String = "default",
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    /** When true, show the compact familiar strip on Home. */
    val familiarEnabled: Boolean = false,
    val familiarSpecies: FamiliarSpecies = FamiliarSpecies.WOLF,
    /** Slower needle + wider sweet bands for Treat toss (and similar companion timing games). */
    val treatTossEasyMode: Boolean = false,
    /** Pull sleep/steps from Health Connect when permitted. */
    val healthConnectSyncEnabled: Boolean = false,
    /** Master switch for local reminder notifications. */
    val remindersEnabled: Boolean = false,
    val reminderMorningEnabled: Boolean = true,
    val reminderEveningEnabled: Boolean = true,
    val reminderBossEnabled: Boolean = true,
)
