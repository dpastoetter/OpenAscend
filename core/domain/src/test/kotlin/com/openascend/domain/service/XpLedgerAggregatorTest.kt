package com.openascend.domain.service

import com.openascend.domain.model.XpEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class XpLedgerAggregatorTest {

    @Test
    fun summarize_groupsByReasonPrefix() {
        val events = listOf(
            XpEvent(1, 0, 12, "Evening check-in sealed"),
            XpEvent(2, 0, 20, "Quest: March"),
            XpEvent(3, 0, 40, "Weekly boss sealed: Warden"),
            XpEvent(4, 0, 10, "Companion glide"),
            XpEvent(5, 0, 3, "Habit: Read"),
        )
        val s = XpLedgerAggregator.summarize(events)
        assertEquals(12, s.checkInXp)
        assertEquals(20, s.questXp)
        assertEquals(40, s.bossXp)
        assertEquals(10, s.companionXp)
        assertEquals(3, s.habitXp)
        assertEquals(85, s.total)
    }
}
