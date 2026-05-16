package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.GameQuest
import com.openascend.domain.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ChronicleCompassResolverTest {

    private val quest = GameQuest(
        id = "q1",
        title = "March the line",
        description = "",
        linkedStat = CoreStat.STAMINA,
        xpReward = 20,
        completed = false,
    )

    @Test
    fun sunday_prefersWeeklyReview() {
        val sunday = LocalDate.of(2026, 4, 19).toEpochDay()
        val directive = ChronicleCompassResolver.resolve(
            todayEpochDay = sunday,
            hourOfDay = 10,
            loggedToday = false,
            openQuest = quest,
            bossSealedThisWeek = false,
            bossName = "Boss",
            habits = emptyList(),
            todayCompletions = emptyMap(),
        )
        assertEquals(ChronicleCompassKind.WeeklyReview, directive.kind)
    }

    @Test
    fun evening_notLogged_prefersCheckIn() {
        val directive = ChronicleCompassResolver.resolve(
            todayEpochDay = LocalDate.of(2026, 4, 20).toEpochDay(),
            hourOfDay = 19,
            loggedToday = false,
            openQuest = quest,
            bossSealedThisWeek = true,
            bossName = "Boss",
            habits = listOf(Habit(1, "a", 7, 2, CoreStat.DISCIPLINE)),
            todayCompletions = emptyMap(),
        )
        assertEquals(ChronicleCompassKind.EveningCheckIn, directive.kind)
        assertEquals(1, directive.habitsOpenCount)
    }
}
