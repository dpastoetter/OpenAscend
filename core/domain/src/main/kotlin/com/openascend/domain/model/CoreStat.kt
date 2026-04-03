package com.openascend.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CoreStat {
    RECOVERY,
    STAMINA,
    STABILITY,
    DISCIPLINE,
    VITALITY,
}
