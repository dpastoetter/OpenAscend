package com.openascend.domain.model

data class PrivacySettings(
    val analyticsOptIn: Boolean,
    val crashReportsOptIn: Boolean,
    val showFinanceHints: Boolean,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
)
