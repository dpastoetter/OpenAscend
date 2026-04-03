package com.openascend.data.export

import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.Habit
import com.openascend.domain.model.UserProfile
import com.openascend.domain.model.XpEvent
import kotlinx.serialization.Serializable

@Serializable
data class OpenAscendExport(
    val exportedAtMillis: Long,
    val profile: UserProfile?,
    val habits: List<Habit>,
    val dailyMetrics: List<DailyMetric>,
    val xpTotal: Int,
    val xpEvents: List<XpEvent>,
)
