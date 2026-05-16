package com.openascend.app.notifications

import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.service.StatComputationService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderWorkerEntryPoint {
    fun privacyPreferences(): PrivacyPreferences
    fun profileRepository(): ProfileRepository
    fun metricsRepository(): MetricsRepository
    fun habitRepository(): HabitRepository
    fun statComputation(): StatComputationService
}
