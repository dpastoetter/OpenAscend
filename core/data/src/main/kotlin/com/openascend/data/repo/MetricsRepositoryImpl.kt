package com.openascend.data.repo

import com.openascend.data.local.db.DailyMetricDao
import com.openascend.data.local.mapper.toDomain
import com.openascend.data.local.mapper.toEntity
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.repository.MetricsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MetricsRepositoryImpl @Inject constructor(
    private val dailyMetricDao: DailyMetricDao,
) : MetricsRepository {

    override fun observeDay(epochDay: Long): Flow<DailyMetric?> =
        dailyMetricDao.observeDay(epochDay).map { it?.toDomain() }

    override suspend fun getDay(epochDay: Long): DailyMetric? =
        dailyMetricDao.getDay(epochDay)?.toDomain()

    override suspend fun upsertDay(metric: DailyMetric) {
        dailyMetricDao.upsert(metric.toEntity())
    }

    override suspend fun metricsBetween(startEpochDay: Long, endEpochDayInclusive: Long): List<DailyMetric> =
        dailyMetricDao.range(startEpochDay, endEpochDayInclusive).map { it.toDomain() }
}
