package com.openascend.app.di

import android.content.Context
import androidx.room.Room
import com.openascend.app.BuildConfig
import com.openascend.data.local.db.OpenAscendDatabase
import com.openascend.data.local.db.OpenAscendMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): OpenAscendDatabase {
        val builder = Room.databaseBuilder(context, OpenAscendDatabase::class.java, "openascend.db")
            .addMigrations(*OpenAscendMigrations.ALL)
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration()
        }
        return builder.build()
    }

    @Provides
    fun profileDao(db: OpenAscendDatabase) = db.profileDao()

    @Provides
    fun habitDao(db: OpenAscendDatabase) = db.habitDao()

    @Provides
    fun dailyMetricDao(db: OpenAscendDatabase) = db.dailyMetricDao()

    @Provides
    fun habitCompletionDao(db: OpenAscendDatabase) = db.habitCompletionDao()

    @Provides
    fun xpDao(db: OpenAscendDatabase) = db.xpDao()

    @Provides
    fun questCompletionDao(db: OpenAscendDatabase) = db.questCompletionDao()
}
