package com.openascend.domain.companion

import com.openascend.domain.model.CompanionGameDifficulty
import com.openascend.domain.model.FamiliarSpecies

/**
 * Central tuning for the companion **Treat toss** needle: species/mood signatures, per-round ramp,
 * and score bands (including easy-accessibility mode and mood trade-offs).
 */
object TreatTossTiming {

    /** Base scale inside `sin(t * base * …)` before other multipliers. */
    private const val BASE_ANGULAR = 2.05

    /** Easy: slower needle oscillation. */
    const val EASY_SPEED_MULTIPLIER = 0.72

    /** Hard: faster needle. */
    private const val HARD_SPEED_MULTIPLIER = 1.14

    /** Per-round difficulty ramp after round 1 (multiplicative). */
    fun roundSpeedRamp(roundIndex: Int): Double =
        1.0 + (roundIndex - 1).coerceAtLeast(0) * 0.12

    /** Species timing signature: bear slower, wolf quicker, dragon neutral with slight lag feel via ViewModel phase (optional). */
    fun speciesSpeedFactor(species: FamiliarSpecies): Double =
        when (species) {
            FamiliarSpecies.BEAR -> 0.86
            FamiliarSpecies.WOLF -> 1.14
            FamiliarSpecies.DRAGON -> 0.98
        }

    /** Mood affects baseline reactivity. */
    fun moodSpeedFactor(mood: CompanionMood): Double =
        when (mood) {
            CompanionMood.FADING -> 0.76
            CompanionMood.SPARKLING -> 1.12
            CompanionMood.COZY -> 0.94
            CompanionMood.WATCHING -> 1.0
            CompanionMood.CURIOUS -> 1.06
            CompanionMood.DORMANT -> 0.88
        }

    fun effectiveNeedleSpeed(
        species: FamiliarSpecies,
        mood: CompanionMood,
        roundIndex: Int,
        difficulty: CompanionGameDifficulty,
    ): Double {
        var v = BASE_ANGULAR * speciesSpeedFactor(species) * moodSpeedFactor(mood) * roundSpeedRamp(roundIndex)
        v *= when (difficulty) {
            CompanionGameDifficulty.EASY -> EASY_SPEED_MULTIPLIER
            CompanionGameDifficulty.NORMAL -> 1.0
            CompanionGameDifficulty.HARD -> HARD_SPEED_MULTIPLIER
        }
        return v.coerceIn(0.85, 3.65)
    }

    data class Bands(
        val greatLo: Float,
        val greatHi: Float,
        val okLo: Float,
        val okHi: Float,
    )

    /**
     * **Sparkling**: slightly narrower great band (reward precision) on normal/hard.
     * **Fading**: slightly wider OK band for forgiveness.
     */
    fun scoreBands(mood: CompanionMood, difficulty: CompanionGameDifficulty): Bands {
        return when (difficulty) {
            CompanionGameDifficulty.EASY -> Bands(0.38f, 0.62f, 0.22f, 0.78f)
            CompanionGameDifficulty.NORMAL -> {
                val great = when (mood) {
                    CompanionMood.SPARKLING -> 0.44f to 0.56f
                    else -> 0.42f to 0.58f
                }
                val ok = when (mood) {
                    CompanionMood.FADING -> 0.26f to 0.74f
                    else -> 0.28f to 0.72f
                }
                Bands(great.first, great.second, ok.first, ok.second)
            }
            CompanionGameDifficulty.HARD -> {
                val great = when (mood) {
                    CompanionMood.SPARKLING -> 0.475f to 0.525f
                    else -> 0.465f to 0.535f
                }
                val ok = when (mood) {
                    CompanionMood.FADING -> 0.30f to 0.70f
                    else -> 0.32f to 0.68f
                }
                Bands(great.first, great.second, ok.first, ok.second)
            }
        }
    }
}
