package com.openascend.domain.health

import com.openascend.domain.model.DailyMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HealthConnectMetricMergeTest {

    @Test
    fun manualSleepWinsOverHealth() {
        val existing = DailyMetric(5L, 7f, null, null, null, null)
        val merged = HealthConnectMetricMerge.mergeDay(5L, existing, 8f, 10_000)
        assertEquals(7f, merged!!.sleepHours!!, 0.01f)
        assertEquals(10_000, merged.steps)
    }

    @Test
    fun healthFillsWhenManualMissing() {
        val merged = HealthConnectMetricMerge.mergeDay(3L, null, 6.5f, 5000)
        assertEquals(6.5f, merged!!.sleepHours!!, 0.01f)
        assertEquals(5000, merged.steps)
    }

    @Test
    fun returnsNullWhenNoData() {
        assertNull(HealthConnectMetricMerge.mergeDay(1L, null, null, null))
    }
}
