package com.openascend.domain.service

import com.openascend.domain.model.XpEvent

data class XpLedgerSummary(
    val checkInXp: Int,
    val questXp: Int,
    val bossXp: Int,
    val companionXp: Int,
    val habitXp: Int,
    val otherXp: Int,
) {
    val total: Int = checkInXp + questXp + bossXp + companionXp + habitXp + otherXp
}

object XpLedgerAggregator {
    fun summarize(events: List<XpEvent>, sinceEpochMillis: Long = 0L): XpLedgerSummary {
        var checkIn = 0
        var quest = 0
        var boss = 0
        var companion = 0
        var habit = 0
        var other = 0
        for (e in events) {
            if (e.timestampMillis < sinceEpochMillis) continue
            val r = e.reason.lowercase()
            when {
                r.contains("check-in") || r.contains("evening") -> checkIn += e.amount
                r.startsWith("quest:") -> quest += e.amount
                r.contains("boss") -> boss += e.amount
                r.contains("companion") -> companion += e.amount
                r.startsWith("habit:") -> habit += e.amount
                else -> other += e.amount
            }
        }
        return XpLedgerSummary(checkIn, quest, boss, companion, habit, other)
    }
}
