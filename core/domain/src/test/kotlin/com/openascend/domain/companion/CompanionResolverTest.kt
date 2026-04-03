package com.openascend.domain.companion

import org.junit.Assert.assertEquals
import org.junit.Test

class CompanionResolverTest {

    private val day = 20_000L

    @Test
    fun dormant_when_onboardingIncomplete() {
        val s = CompanionResolver.resolve(day, null, 0, 0, 0, 0, 0, onboardingComplete = false)
        assertEquals(CompanionMood.DORMANT, s.mood)
    }

    @Test
    fun sparkling_when_sealedAndEngaged() {
        val s = CompanionResolver.resolve(
            todayEpochDay = day,
            lastLoggedEpochDay = day,
            streakDays = 3,
            habitsDoneToday = 2,
            habitsTotalToday = 3,
            questsDoneToday = 0,
            questsTotalToday = 4,
            onboardingComplete = true,
        )
        assertEquals(CompanionMood.SPARKLING, s.mood)
    }

    @Test
    fun cozy_when_sealedButQuiet() {
        val s = CompanionResolver.resolve(
            todayEpochDay = day,
            lastLoggedEpochDay = day,
            streakDays = 1,
            habitsDoneToday = 0,
            habitsTotalToday = 2,
            questsDoneToday = 0,
            questsTotalToday = 4,
            onboardingComplete = true,
        )
        assertEquals(CompanionMood.COZY, s.mood)
    }

    @Test
    fun fading_when_missedDay() {
        val s = CompanionResolver.resolve(
            todayEpochDay = day,
            lastLoggedEpochDay = day - 3,
            streakDays = 1,
            habitsDoneToday = 0,
            habitsTotalToday = 0,
            questsDoneToday = 0,
            questsTotalToday = 0,
            onboardingComplete = true,
        )
        assertEquals(CompanionMood.FADING, s.mood)
    }

    @Test
    fun watching_when_streakButNotSealed() {
        val s = CompanionResolver.resolve(
            todayEpochDay = day,
            lastLoggedEpochDay = day - 1,
            streakDays = 5,
            habitsDoneToday = 1,
            habitsTotalToday = 2,
            questsDoneToday = 0,
            questsTotalToday = 2,
            onboardingComplete = true,
        )
        assertEquals(CompanionMood.WATCHING, s.mood)
    }

    @Test
    fun curious_when_fresh() {
        val s = CompanionResolver.resolve(
            todayEpochDay = day,
            lastLoggedEpochDay = null,
            streakDays = 0,
            habitsDoneToday = 0,
            habitsTotalToday = 0,
            questsDoneToday = 0,
            questsTotalToday = 0,
            onboardingComplete = true,
        )
        assertEquals(CompanionMood.CURIOUS, s.mood)
    }
}
