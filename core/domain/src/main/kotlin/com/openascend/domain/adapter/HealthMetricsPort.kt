package com.openascend.domain.adapter

/**
 * Future: Apple Health / Health Connect / Google Fit.
 * MVP uses [ManualMetricsPort] only.
 */
interface HealthMetricsPort {
    suspend fun readSleepHoursForDay(epochDay: Long): Float?
    suspend fun readStepsForDay(epochDay: Long): Int?
}
