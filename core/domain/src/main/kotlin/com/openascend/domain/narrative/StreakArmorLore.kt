package com.openascend.domain.narrative

/** Plain-language copy for streak armor on the character sheet. */
object StreakArmorLore {
    const val shortBlurb: String =
        "Streak armor is narrative padding from consistent check-ins—it softens bad days in the tale, not in real life. " +
            "Each evening you seal the day, the armor thickens slightly; long gaps thin it. It does not replace rest, care, or professional advice."

    fun chipLine(armor: Int): String =
        if (armor >= 7) "Streak armor high—your chronicle carries extra grace this week."
        else if (armor >= 3) "Streak armor steady—keep sealing evenings to shore it up."
        else "Streak armor thin—one honest check-in begins the mend."
}
