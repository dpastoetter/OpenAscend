package com.openascend.domain.raid

import com.openascend.domain.model.CoreStat

data class ChronicleRaidMemberResult(
    val displayName: String,
    val stats: Map<CoreStat, Int>,
    val mvpStat: CoreStat,
)

data class ChronicleRaidResult(
    val bossName: String,
    val teamPower: Int,
    val threshold: Int,
    val success: Boolean,
    val members: List<ChronicleRaidMemberResult>,
    val weakestTeamStat: CoreStat,
)
