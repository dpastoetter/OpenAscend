package com.openascend.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FamiliarSpeciesTest {

    @Test
    fun fromId_roundTripsIds() {
        FamiliarSpecies.entries.forEach { species ->
            assertEquals(species, FamiliarSpecies.fromId(species.id))
            assertEquals(species, FamiliarSpecies.fromId(species.id.uppercase()))
        }
    }

    @Test
    fun fromId_unknownDefaultsToWolf() {
        assertEquals(FamiliarSpecies.WOLF, FamiliarSpecies.fromId(null))
        assertEquals(FamiliarSpecies.WOLF, FamiliarSpecies.fromId(""))
        assertEquals(FamiliarSpecies.WOLF, FamiliarSpecies.fromId("cat"))
    }
}
