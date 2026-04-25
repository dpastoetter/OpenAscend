package com.openascend.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CompanionMiniGame {
    TREAT_TOSS,
    MEMORY_FLASH,
    ECHO_SEQUENCE,
    GLIDE,
    STACK,
    THREAD,
}

@Serializable
data class CompanionPerGameDifficulties(
    val treatToss: CompanionGameDifficulty = CompanionGameDifficulty.NORMAL,
    val memoryFlash: CompanionGameDifficulty = CompanionGameDifficulty.NORMAL,
    val echoSequence: CompanionGameDifficulty = CompanionGameDifficulty.NORMAL,
    val glide: CompanionGameDifficulty = CompanionGameDifficulty.NORMAL,
    val stack: CompanionGameDifficulty = CompanionGameDifficulty.NORMAL,
    val thread: CompanionGameDifficulty = CompanionGameDifficulty.NORMAL,
) {
    companion object {
        fun uniform(difficulty: CompanionGameDifficulty): CompanionPerGameDifficulties =
            CompanionPerGameDifficulties(
                treatToss = difficulty,
                memoryFlash = difficulty,
                echoSequence = difficulty,
                glide = difficulty,
                stack = difficulty,
                thread = difficulty,
            )
    }
}

