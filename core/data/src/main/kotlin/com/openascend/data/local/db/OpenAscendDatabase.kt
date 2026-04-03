package com.openascend.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProfileEntity::class,
        HabitEntity::class,
        DailyMetricEntity::class,
        HabitCompletionEntity::class,
        XpEventEntity::class,
        QuestCompletionEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class OpenAscendDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun habitDao(): HabitDao
    abstract fun dailyMetricDao(): DailyMetricDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun xpDao(): XpDao
    abstract fun questCompletionDao(): QuestCompletionDao
}
