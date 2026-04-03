package com.openascend.data.local.db

import androidx.room.Entity

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "epochDay"],
)
data class HabitCompletionEntity(
    val habitId: Long,
    val epochDay: Long,
    val completed: Boolean,
)
