package com.openascend.domain.model

import kotlinx.serialization.Serializable

/** Shared difficulty for all companion hub mini-games (treat toss, memory, echo, glide, stack, thread). */
@Serializable
enum class CompanionGameDifficulty {
    EASY,
    NORMAL,
    HARD,
}
