package com.openascend.data.local.db

import androidx.room.Entity

@Entity(
    tableName = "quest_completions",
    primaryKeys = ["questId", "epochDay"],
)
data class QuestCompletionEntity(
    val questId: String,
    val epochDay: Long,
)
