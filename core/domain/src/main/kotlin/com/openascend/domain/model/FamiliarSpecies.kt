package com.openascend.domain.model

import kotlinx.serialization.Serializable

/**
 * Home companion creature; persisted as [id] in DataStore.
 */
@Serializable
enum class FamiliarSpecies(val id: String, val displayName: String, val emoji: String) {
    BEAR("bear", "Bear", "🐻"),
    WOLF("wolf", "Wolf", "🐺"),
    DRAGON("dragon", "Dragon", "🐉"),
    ;

    companion object {
        fun fromId(raw: String?): FamiliarSpecies =
            entries.firstOrNull { it.id.equals(raw, ignoreCase = true) } ?: WOLF
    }
}
