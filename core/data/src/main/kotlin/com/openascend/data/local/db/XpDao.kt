package com.openascend.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface XpDao {
    @Query("SELECT * FROM xp_events ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<XpEventEntity>>

    @Query("SELECT * FROM xp_events ORDER BY timestampMillis DESC LIMIT :limit")
    suspend fun snapshot(limit: Int): List<XpEventEntity>

    @Insert
    suspend fun insert(entity: XpEventEntity): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_events")
    suspend fun totalXp(): Int

    @Query("DELETE FROM xp_events")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: XpEventEntity): Long
}
