package com.openascend.domain.service

/**
 * Simple narrative score from manual 1–10 control rating (not financial advice).
 */
object BankHealthScorer {
    fun label(score1to10: Int?): String = when (score1to10) {
        null -> "Uncharted"
        in 1..3 -> "Stormy"
        in 4..5 -> "Uneasy"
        in 6..7 -> "Steady"
        in 8..9 -> "Fortified"
        10 -> "Citadel"
        else -> "Uncharted"
    }
}
