package com.openascend.app.notifications

import com.openascend.data.local.prefs.PrivacyPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderWorkerEntryPoint {
    fun privacyPreferences(): PrivacyPreferences
}
