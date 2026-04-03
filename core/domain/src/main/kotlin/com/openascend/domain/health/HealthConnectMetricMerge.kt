package com.openascend.domain.health

import com.openascend.domain.model.DailyMetric

object HealthConnectMetricMerge {

    /**
     * Manual values in [existing] win over Health Connect when present.
     * Returns null if there is nothing to persist.
     */
    fun mergeDay(
        epochDay: Long,
        existing: DailyMetric?,
        healthSleepHours: Float?,
        healthSteps: Int?,
    ): DailyMetric? {
        val sleep = when {
            existing?.sleepHours != null -> existing.sleepHours
            healthSleepHours != null -> healthSleepHours
            else -> null
        }
        val steps = when {
            existing?.steps != null -> existing.steps
            healthSteps != null -> healthSteps
            else -> null
        }
        if (existing == null && sleep == null && steps == null) return null
        return DailyMetric(
            epochDay = epochDay,
            sleepHours = sleep,
            steps = steps,
            bankControlScore = existing?.bankControlScore,
            moneyNote = existing?.moneyNote,
            vitalityScore = existing?.vitalityScore,
        )
    }
}
