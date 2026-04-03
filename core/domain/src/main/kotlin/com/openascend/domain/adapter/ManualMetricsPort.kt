package com.openascend.domain.adapter

import com.openascend.domain.model.DailyMetric

interface ManualMetricsPort {
    suspend fun loadDay(epochDay: Long): DailyMetric?
    suspend fun upsertDay(metric: DailyMetric)
}
