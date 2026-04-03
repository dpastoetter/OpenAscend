package com.openascend.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.openascend.data.local.db.OpenAscendDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profile ADD COLUMN avatarRelativePath TEXT")
        }
    }

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): OpenAscendDatabase =
        Room.databaseBuilder(context, OpenAscendDatabase::class.java, "openascend.db")
            .addMigrations(migration2To3)
            .fallbackToDestructiveMigration()
            .build()

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
