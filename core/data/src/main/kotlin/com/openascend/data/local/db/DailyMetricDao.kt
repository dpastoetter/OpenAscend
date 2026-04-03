package com.openascend.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMetricDao {
    @Query("SELECT * FROM daily_metrics WHERE epochDay = :epochDay LIMIT 1")
    fun observeDay(epochDay: Long): Flow<DailyMetricEntity?>

    @Query("SELECT * FROM daily_metrics WHERE epochDay = :epochDay LIMIT 1")
    suspend fun getDay(epochDay: Long): DailyMetricEntity?

    @Query(
        """
        SELECT * FROM daily_metrics
        WHERE epochDay BETWEEN :start AND :endInclusive
        ORDER BY epochDay DESC
        """,
    )
    suspend fun range(start: Long, endInclusive: Long): List<DailyMetricEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyMetricEntity)

    @Query("DELETE FROM daily_metrics")
    suspend fun deleteAll()
}
