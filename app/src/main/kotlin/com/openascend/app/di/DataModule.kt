package com.openascend.app.di

import android.content.Context
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.data.local.prefs.WidgetSnapshotStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun privacyPreferences(@ApplicationContext context: Context): PrivacyPreferences =
        PrivacyPreferences(context)

    @Provides
    @Singleton
    fun widgetSnapshotStore(@ApplicationContext context: Context): WidgetSnapshotStore =
        WidgetSnapshotStore(context)
}
