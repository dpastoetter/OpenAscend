package com.openascend.app.di

import com.openascend.data.repo.HabitRepositoryImpl
import com.openascend.data.repo.MetricsRepositoryImpl
import com.openascend.data.repo.ProfileRepositoryImpl
import com.openascend.data.repo.QuestCompletionRepositoryImpl
import com.openascend.data.repo.XpRepositoryImpl
import com.openascend.domain.repository.HabitRepository
import com.openascend.domain.repository.MetricsRepository
import com.openascend.domain.repository.ProfileRepository
import com.openascend.domain.repository.QuestCompletionRepository
import com.openascend.domain.repository.XpRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun profileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun habitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun metricsRepository(impl: MetricsRepositoryImpl): MetricsRepository

    @Binds
    @Singleton
    abstract fun xpRepository(impl: XpRepositoryImpl): XpRepository

    @Binds
    @Singleton
    abstract fun questCompletionRepository(impl: QuestCompletionRepositoryImpl): QuestCompletionRepository
}
