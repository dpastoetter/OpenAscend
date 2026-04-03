package com.openascend.app.di

import com.openascend.app.health.HealthConnectBridge
import com.openascend.app.health.HealthConnectBridgeImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthModule {

    @Binds
    @Singleton
    abstract fun healthConnectBridge(impl: HealthConnectBridgeImpl): HealthConnectBridge
}
