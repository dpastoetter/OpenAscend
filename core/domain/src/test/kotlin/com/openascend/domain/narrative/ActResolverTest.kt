package com.openascend.domain.narrative

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ActResolverTest {

    private val pack = NarrativePack(
        id = "t",
        actTitles = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
        questActPrefix = "",
        bossTellTemplates = listOf("x"),
        questTitleFlavorSuffixes = listOf(""),
    )

    @Test
    fun actTitle_matchesMonth() {
        assertEquals("Jan", ActResolver.actTitleFor(LocalDate.of(2026, 1, 15), pack))
        assertEquals("Dec", ActResolver.actTitleFor(LocalDate.of(2026, 12, 1), pack))
    }

    @Test
    fun monthIndex_zeroBased() {
        assertEquals(0, ActResolver.monthIndex(LocalDate.of(2026, 1, 1)))
        assertEquals(11, ActResolver.monthIndex(LocalDate.of(2026, 12, 31)))
    }
}
