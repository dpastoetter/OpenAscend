package com.openascend.domain.model

import kotlinx.serialization.Serializable

/**
 * Manual daily inputs. Integrations (Health Connect, Plaid) can populate these via adapters later.
 */
@Serializable
data class DailyMetric(
    val epochDay: Long,
    val sleepHours: Float?,
    val steps: Int?,
    /** User self-rating 1–10: felt in control of money this day */
    val bankControlScore: Int?,
    /** Free text; not interpreted as advice */
    val moneyNote: String?,
    /** Optional 1–10 self-check-in */
    val vitalityScore: Int?,
)
