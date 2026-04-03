package com.openascend.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestCompletionDao {
    @Query("SELECT questId FROM quest_completions WHERE epochDay = :epochDay")
    fun observeForDay(epochDay: Long): Flow<List<String>>

    @Query("SELECT questId FROM quest_completions WHERE epochDay = :epochDay")
    suspend fun idsForDay(epochDay: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: QuestCompletionEntity)

    @Query("SELECT * FROM quest_completions")
    suspend fun snapshotAll(): List<QuestCompletionEntity>

    @Query("DELETE FROM quest_completions")
    suspend fun deleteAll()
}
