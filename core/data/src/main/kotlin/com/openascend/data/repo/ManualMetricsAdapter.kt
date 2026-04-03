package com.openascend.data.repo

import com.openascend.domain.adapter.ManualMetricsPort
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.repository.MetricsRepository

class ManualMetricsAdapter(
    private val metricsRepository: MetricsRepository,
) : ManualMetricsPort {

    override suspend fun loadDay(epochDay: Long): DailyMetric? =
        metricsRepository.getDay(epochDay)

    override suspend fun upsertDay(metric: DailyMetric) {
        metricsRepository.upsertDay(metric)
    }
}
