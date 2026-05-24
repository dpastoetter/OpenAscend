package com.openascend.domain.raid

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.StatBlock

object ChronicleRaidResolver {

    const val MAX_PARTY_SIZE = 4
    private const val SUCCESS_THRESHOLD_BASE = 180

    fun resolve(
        bossName: String,
        members: List<Pair<String, StatBlock>>,
    ): ChronicleRaidResult {
        val blocks = members.map { it.second }
        val avg = StatBlock.average(blocks)
        val teamPower = avg.recovery + avg.stamina + avg.stability + avg.discipline + avg.vitality
        val threshold = SUCCESS_THRESHOLD_BASE + members.size * 15
        val weakest = avg.weakestStat()
        val memberResults = members.map { (name, block) ->
            val mvp = CoreStat.entries.maxByOrNull { stat ->
                statValue(block, stat)
            } ?: CoreStat.DISCIPLINE
            ChronicleRaidMemberResult(
                displayName = name,
                stats = CoreStat.entries.associateWith { statValue(block, it) },
                mvpStat = mvp,
            )
        }
        return ChronicleRaidResult(
            bossName = bossName,
            teamPower = teamPower,
            threshold = threshold,
            success = teamPower >= threshold,
            members = memberResults,
            weakestTeamStat = weakest,
        )
    }

    private fun statValue(block: StatBlock, stat: CoreStat): Int = when (stat) {
        CoreStat.RECOVERY -> block.recovery
        CoreStat.STAMINA -> block.stamina
        CoreStat.STABILITY -> block.stability
        CoreStat.DISCIPLINE -> block.discipline
        CoreStat.VITALITY -> block.vitality
    }
}
