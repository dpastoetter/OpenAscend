package com.openascend.domain.companion

/** Pattern-based whispers from recent companion mood history (no persistence API in domain). */
object CompanionMemoryWhisper {
    fun line(recentMoods: List<CompanionMood>): String? {
        if (recentMoods.size < 3) return null
        val calmCount = recentMoods.count {
            it == CompanionMood.SPARKLING || it == CompanionMood.COZY
        }
        if (calmCount >= 3) {
            return "Your familiar remembers several calm evenings—you both seem steadier."
        }
        val lowCount = recentMoods.count {
            it == CompanionMood.FADING || it == CompanionMood.WATCHING
        }
        if (lowCount >= 3) {
            return "Lately the chronicle has been heavy; your familiar stays close anyway."
        }
        return null
    }
}
