package com.openascend.app.di

import android.content.Context
import com.openascend.data.narrative.AssetNarrativeRepository
import com.openascend.domain.narrative.NarrativeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NarrativeModule {

    @Provides
    @Singleton
    fun narrativeRepository(@ApplicationContext context: Context): NarrativeRepository =
        AssetNarrativeRepository(context)
}
