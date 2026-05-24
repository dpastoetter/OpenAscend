package com.openascend.domain.narrative

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class SeasonProgressResolverTest {

    private val pack = NarrativePack(
        id = "test",
        actTitles = listOf("Jan"),
        questActPrefix = "",
        bossTellTemplates = emptyList(),
        questTitleFlavorSuffixes = emptyList(),
        seasons = listOf(
            NarrativeSeason(
                id = "s1",
                title = "Test Season",
                weekThemes = listOf("W1", "W2", "W3", "W4"),
                finaleBossName = "Finale Boss",
            ),
        ),
    )

    @Test
    fun resolve_returnsChapterLine() {
        val day = LocalDate.of(2026, 1, 15).toEpochDay()
        val progress = SeasonProgressResolver.resolve(day, pack)
        assertNotNull(progress)
        assertEquals("Test Season", progress!!.seasonTitle)
    }
}
