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

    /** Adds [QuestDisplayBonus.PER_SEALED_QUEST] per sealed quest to its linked stat (cap 100). */
    fun withSealedQuestSpotlight(quests: List<GameQuest>): StatBlock {
        val extra = CoreStat.entries.associateWith { 0 }.toMutableMap()
        for (q in quests) {
            if (q.completed) {
                extra[q.linkedStat] = extra.getValue(q.linkedStat) + QuestDisplayBonus.PER_SEALED_QUEST
            }
        }
        fun bump(stat: CoreStat, base: Int): Int =
            (base + extra.getValue(stat)).coerceIn(0, 100)
        return copy(
            recovery = bump(CoreStat.RECOVERY, recovery),
            stamina = bump(CoreStat.STAMINA, stamina),
            stability = bump(CoreStat.STABILITY, stability),
            discipline = bump(CoreStat.DISCIPLINE, discipline),
            vitality = bump(CoreStat.VITALITY, vitality),
        )
    }
}
