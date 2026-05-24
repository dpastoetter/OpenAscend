package com.openascend.domain.adapter

/**
 * Default MVP port: no automatic health metrics (manual check-in only).
 */
class ManualHealthMetricsPort : HealthMetricsPort {
  override suspend fun readSleepHoursForDay(epochDay: Long): Float? = null

  override suspend fun readStepsForDay(epochDay: Long): Int? = null
}
