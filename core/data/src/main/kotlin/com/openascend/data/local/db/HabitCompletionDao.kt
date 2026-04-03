package com.openascend.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Query(
        """
        SELECT completed FROM habit_completions
        WHERE habitId = :habitId AND epochDay = :epochDay LIMIT 1
        """,
    )
    suspend fun isCompleted(habitId: Long, epochDay: Long): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HabitCompletionEntity)

    @Query("SELECT * FROM habit_completions WHERE epochDay = :epochDay")
    fun observeForDay(epochDay: Long): Flow<List<HabitCompletionEntity>>
}
