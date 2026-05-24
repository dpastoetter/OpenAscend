package com.openascend.domain.insight

import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.StatBlock
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ChronicleInsightEngineTest {

    @Test
    fun pickWeeklyInsight_returnsNullWithSparseData() {
        assertNull(
            ChronicleInsightEngine.pickWeeklyInsight(
                weekStartEpochDay = 20_000L,
                metricsByDay = emptyMap(),
                statsByDay = emptyMap(),
                sealedEpochDays = emptySet(),
            ),
        )
    }

    @Test
    fun stabilityStreak_insightWhenEnoughNotes() {
        val start = 20_000L
        val metrics = (0L..6L).associate { off ->
            val day = start + off
            day to DailyMetric(day, null, null, 7, "honest note", null)
        }
        val stats = (0L..20L).associate { day ->
            day to StatBlock(50, 50, 50, 50, 50)
        }
        val insight = ChronicleInsightEngine.pickWeeklyInsight(
            weekStartEpochDay = start,
            metricsByDay = metrics,
            statsByDay = stats,
            sealedEpochDays = emptySet(),
        )
        assertNotNull(insight)
    }
}
