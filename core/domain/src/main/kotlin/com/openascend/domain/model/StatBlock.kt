package com.openascend.domain.model

data class StatBlock(
    val recovery: Int,
    val stamina: Int,
    val stability: Int,
    val discipline: Int,
    val vitality: Int,
) {
    fun asMap(): Map<CoreStat, Int> = mapOf(
        CoreStat.RECOVERY to recovery,
        CoreStat.STAMINA to stamina,
        CoreStat.STABILITY to stability,
        CoreStat.DISCIPLINE to discipline,
        CoreStat.VITALITY to vitality,
    )

    fun weakestStat(): CoreStat =
        asMap().minBy { it.value }.key
}
