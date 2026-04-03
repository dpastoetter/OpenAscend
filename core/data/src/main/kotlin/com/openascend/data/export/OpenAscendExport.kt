package com.openascend.data.export

import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.Habit
import com.openascend.domain.model.PrivacySettings
import com.openascend.domain.model.UserProfile
import com.openascend.domain.model.XpEvent
import kotlinx.serialization.Serializable

@Serializable
data class ExportedHabitCompletion(
    val habitId: Long,
    val epochDay: Long,
    val completed: Boolean,
)

@Serializable
data class ExportedQuestCompletion(
    val questId: String,
    val epochDay: Long,
)

@Serializable
data class OpenAscendExport(
    /** 1 = legacy (no completions/privacy blob); 2 = full backup. */
    val schemaVersion: Int = 1,
    val exportedAtMillis: Long,
    /** User preferences at export time (restored on full backup import). */
    val privacy: PrivacySettings? = null,
    val profile: UserProfile?,
    val habits: List<Habit>,
    val dailyMetrics: List<DailyMetric>,
    val xpTotal: Int,
    val xpEvents: List<XpEvent>,
    val habitCompletions: List<ExportedHabitCompletion> = emptyList(),
    val questCompletions: List<ExportedQuestCompletion> = emptyList(),
)
