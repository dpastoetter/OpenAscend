package com.openascend.domain.repository

import com.openascend.domain.model.DailyMetric
import kotlinx.coroutines.flow.Flow

interface MetricsRepository {
    fun observeDay(epochDay: Long): Flow<DailyMetric?>
    suspend fun getDay(epochDay: Long): DailyMetric?
    suspend fun upsertDay(metric: DailyMetric)
    suspend fun metricsBetween(startEpochDay: Long, endEpochDayInclusive: Long): List<DailyMetric>
}
