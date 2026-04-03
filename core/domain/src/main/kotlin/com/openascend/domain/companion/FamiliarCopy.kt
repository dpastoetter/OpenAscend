package com.openascend.domain.companion

import com.openascend.domain.model.FamiliarSpecies

/**
 * Rewrites generic "familiar" phrasing in [CompanionResolver] lines for the chosen species.
 */
object FamiliarCopy {

    fun stylize(snapshot: CompanionSnapshot, species: FamiliarSpecies): CompanionSnapshot {
        val noun = species.displayName.lowercase()
        val nounTitle = species.displayName
        return snapshot.copy(
            line = snapshot.line
                .replace("Your familiar", "Your $nounTitle")
                .replace("your familiar", "your $noun"),
            whisper = snapshot.whisper
                .replace("your familiar", "your $noun"),
        )
    }
}
