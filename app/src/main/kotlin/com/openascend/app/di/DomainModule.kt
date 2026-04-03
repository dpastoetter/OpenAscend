package com.openascend.app.di

import com.openascend.domain.repository.XpRepository
import com.openascend.domain.service.ArchetypeResolver
import com.openascend.domain.service.BossGenerator
import com.openascend.domain.service.QuestGenerator
import com.openascend.domain.service.StatComputationService
import com.openascend.domain.service.XpEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun statComputation(): StatComputationService = StatComputationService()

    @Provides
    @Singleton
    fun archetypeResolver(): ArchetypeResolver = ArchetypeResolver()

    @Provides
    @Singleton
    fun questGenerator(): QuestGenerator = QuestGenerator()

    @Provides
    @Singleton
    fun bossGenerator(): BossGenerator = BossGenerator()

    @Provides
    @Singleton
    fun xpEngine(
        xpRepository: XpRepository,
        archetypeResolver: ArchetypeResolver,
    ): XpEngine = XpEngine(xpRepository, archetypeResolver)
}
