package com.openascend.domain.insight

/**
 * One weekly pattern the chronicle surfaces on Home (heuristic, local-only).
 */
data class ChronicleInsight(
    val id: String,
    val headline: String,
    val body: String,
)
