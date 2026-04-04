package com.openascend.domain.companion

/**
 * Pure Tamagotchi-style familiar: reacts to check-in recency, habit ticks, quest seals,
 * and (lightly) what happened yesterday so lines feel personal.
 */
object CompanionResolver {

    @Suppress("LongParameterList")
    fun resolve(
        todayEpochDay: Long,
        lastLoggedEpochDay: Long?,
        streakDays: Int,
        habitsDoneToday: Int,
        habitsTotalToday: Int,
        questsDoneToday: Int,
        questsTotalToday: Int,
        onboardingComplete: Boolean,
        habitsDoneYesterday: Int = 0,
        questsDoneYesterday: Int = 0,
        yesterdayMoodHeadline: String? = null,
    ): CompanionSnapshot {
        if (!onboardingComplete) {
            return CompanionSnapshot(
                mood = CompanionMood.DORMANT,
                moodLabel = "Dormant",
                line = "A tiny spark hovers in the jar—finish your chronicle to wake it.",
                whisper = "It dreams in monochrome until you name your path.",
            )
        }

        val sealedToday = lastLoggedEpochDay == todayEpochDay
        val sealedYesterday = lastLoggedEpochDay == todayEpochDay - 1
        val missedADay = lastLoggedEpochDay != null && lastLoggedEpochDay < todayEpochDay - 1
        val habitDen = habitsTotalToday.coerceAtLeast(1)
        val habitRatio = if (habitsTotalToday == 0) 1f else habitsDoneToday.toFloat() / habitDen
        val questDen = questsTotalToday.coerceAtLeast(1)
        val questRatio = if (questsTotalToday == 0) 1f else questsDoneToday.toFloat() / questDen
        val engaged = habitRatio >= 0.5f || questRatio >= 0.5f || questsDoneToday > 0 || habitsDoneToday > 0
        val sawYesterdayWork =
            sealedYesterday && (habitsDoneYesterday > 0 || questsDoneYesterday > 0)

        return when {
            missedADay -> CompanionSnapshot(
                mood = CompanionMood.FADING,
                moodLabel = "Fading",
                line = "Your familiar dims—it missed you across the night boundary.",
                whisper = "One honest check-in calls the color back. No shame, only return.",
            )
            !sealedToday && sawYesterdayWork -> CompanionSnapshot(
                mood = CompanionMood.WATCHING,
                moodLabel = "Watching",
                line = "It remembers yesterday's sealed loops—tonight's chapter is still unwritten.",
                whisper = yesterdayMoodHeadline?.let { head ->
                    "Yesterday's mood note echoed: ${head.take(48)}${if (head.length > 48) "…" else ""}"
                } ?: "The ritual doesn't have to be perfect—only honest.",
            )
            sealedToday && engaged -> CompanionSnapshot(
                mood = CompanionMood.SPARKLING,
                moodLabel = "Sparkling",
                line = buildString {
                    append("Fed on real reps—your familiar practically hums.")
                    if (sawYesterdayWork) append(" It still tastes yesterday's work.")
                },
                whisper = when {
                    yesterdayMoodHeadline != null ->
                        "It noticed yesterday's mood in the margins—today's reps answered back."
                    else ->
                        "It steals glances at your streak like a pocket sun."
                },
            )
            sealedToday -> CompanionSnapshot(
                mood = CompanionMood.COZY,
                moodLabel = "Cozy",
                line = if (sawYesterdayWork) {
                    "Sealed for the day—your familiar curls up, still pleased with yesterday's pace."
                } else {
                    "Sealed for the day—your familiar curls up inside the chronicle."
                },
                whisper = if (sawYesterdayWork) {
                    "Yesterday's habits and quests left warmth in the jar."
                } else {
                    "Tiny habits tomorrow will make its ears twitch."
                },
            )
            streakDays > 0 || lastLoggedEpochDay != null -> CompanionSnapshot(
                mood = CompanionMood.WATCHING,
                moodLabel = "Watching",
                line = "It waits by the ledger for tonight's seal.",
                whisper = "The ritual doesn't have to be perfect—only honest.",
            )
            else -> CompanionSnapshot(
                mood = CompanionMood.CURIOUS,
                moodLabel = "Curious",
                line = "A new wisp—curious what your first streak will taste like.",
                whisper = "Log one evening and it learns your name in the margins.",
            )
        }
    }
}
