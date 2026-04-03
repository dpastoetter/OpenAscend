package com.openascend.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val frequencyPerWeek: Int,
    val difficulty: Int,
    val linkedStat: String,
)
