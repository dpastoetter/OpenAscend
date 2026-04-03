package com.openascend.domain.companion

/**
 * Lightweight familiar mood — derived from the same signals as the daily loop (no separate hunger DB).
 */
enum class CompanionMood {
    /** Check-in sealed today + solid reps (habits or quests). */
    SPARKLING,
    /** Check-in sealed today. */
    COZY,
    /** Waiting for tonight's seal; player has an active streak or history. */
    WATCHING,
    /** Missed at least one full day since last log. */
    FADING,
    /** New traveler — no streak rhythm yet. */
    CURIOUS,
    /** Onboarding not finished — familiar still unnamed in the tale. */
    DORMANT,
}

data class CompanionSnapshot(
    val mood: CompanionMood,
    /** Short label, e.g. "Sparkling" */
    val moodLabel: String,
    /** One-line status for the home card. */
    val line: String,
    /** Extra flavor on tap / expand. */
    val whisper: String,
)
