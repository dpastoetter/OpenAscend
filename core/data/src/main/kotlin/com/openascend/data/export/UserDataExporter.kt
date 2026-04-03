package com.openascend.data.export

import com.openascend.data.local.db.OpenAscendDatabase
import com.openascend.data.local.mapper.toDomain
import com.openascend.data.local.prefs.PrivacyPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

/**
 * Local JSON export for privacy / portability. Not a sync protocol.
 */
class UserDataExporter @Inject constructor(
    private val database: OpenAscendDatabase,
    private val privacyPreferences: PrivacyPreferences,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun buildJson(): String {
        val privacy = privacyPreferences.getSettingsSnapshot()
        val profile = database.profileDao().getProfile()?.toDomain()
        val habits = database.habitDao().snapshot().map { it.toDomain() }
        val today = LocalDate.now().toEpochDay()
        val start = today - 90L
        val metrics = database.dailyMetricDao().range(start, today).map { it.toDomain() }
        val xpTotal = database.xpDao().totalXp()
        val xpEvents = database.xpDao().snapshot(500).map { it.toDomain() }
        val habitCompletions = database.habitCompletionDao().snapshotAll().map {
            ExportedHabitCompletion(
                habitId = it.habitId,
                epochDay = it.epochDay,
                completed = it.completed,
            )
        }
        val questCompletions = database.questCompletionDao().snapshotAll().map {
            ExportedQuestCompletion(questId = it.questId, epochDay = it.epochDay)
        }
        val payload = OpenAscendExport(
            schemaVersion = 2,
            exportedAtMillis = System.currentTimeMillis(),
            privacy = privacy,
            profile = profile,
            habits = habits,
            dailyMetrics = metrics,
            xpTotal = xpTotal,
            xpEvents = xpEvents,
            habitCompletions = habitCompletions,
            questCompletions = questCompletions,
        )
        return json.encodeToString(OpenAscendExport.serializer(), payload)
    }

    suspend fun buildMarkdown(lastDays: Int = 30): String {
        val profile = database.profileDao().getProfile()?.toDomain()
        val habits = database.habitDao().snapshot().map { it.toDomain() }
        val today = LocalDate.now().toEpochDay()
        val start = today - lastDays.toLong() + 1L
        val metrics = database.dailyMetricDao().range(start, today).map { it.toDomain() }
        val xpTotal = database.xpDao().totalXp()
        return buildString {
            appendLine("# OpenAscend chronicle (last $lastDays days)")
            appendLine()
            appendLine("- Exported: ${java.time.Instant.now()}")
            appendLine("- Total XP: $xpTotal")
            appendLine()
            appendLine("## Profile")
            if (profile != null) {
                appendLine("- Name: ${profile.displayName}")
                appendLine("- Streak days: ${profile.streakDays}")
                profile.archetypeSuffix?.let { appendLine("- Archetype suffix: $it") }
            } else {
                appendLine("_No profile row_")
            }
            appendLine()
            appendLine("## Habits")
            habits.forEach { h ->
                appendLine("- **${h.name}** (${h.linkedStat}, diff ${h.difficulty}${if (h.isRestDay) ", rest-day" else ""})")
            }
            appendLine()
            appendLine("## Daily metrics (newest last)")
            metrics.sortedBy { it.epochDay }.forEach { m ->
                appendLine(
                    "- Day ${m.epochDay}: sleep=${m.sleepHours ?: "—"} steps=${m.steps ?: "—"} " +
                        "bank=${m.bankControlScore ?: "—"} vitality=${m.vitalityScore ?: "—"}",
                )
            }
        }
    }
}
