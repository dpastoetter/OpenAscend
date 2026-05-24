package com.openascend.domain.adapter

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FakeHealthMetricsPortTest {

  @Test
  fun manualPort_returnsNull() = runTest {
    val port = ManualHealthMetricsPort()
    assertNull(port.readSleepHoursForDay(1L))
    assertNull(port.readStepsForDay(1L))
  }

  @Test
  fun fakePort_returnsConfiguredValues() = runTest {
    val port =
      FakeHealthMetricsPort(
        sleepByDay = mapOf(10L to 7.5f),
        stepsByDay = mapOf(10L to 8_000),
      )
    assertEquals(7.5f, port.readSleepHoursForDay(10L))
    assertEquals(8_000, port.readStepsForDay(10L))
    assertNull(port.readSleepHoursForDay(11L))
  }
}

class FakeHealthMetricsPort(
  private val sleepByDay: Map<Long, Float> = emptyMap(),
  private val stepsByDay: Map<Long, Int> = emptyMap(),
) : HealthMetricsPort {
  override suspend fun readSleepHoursForDay(epochDay: Long): Float? = sleepByDay[epochDay]

  override suspend fun readStepsForDay(epochDay: Long): Int? = stepsByDay[epochDay]
}
