package com.openascend.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_metrics",
    indices = [Index(value = ["epochDay"], unique = true)],
)
data class DailyMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val sleepHours: Float?,
    val steps: Int?,
    val bankControlScore: Int?,
    val moneyNote: String?,
    val vitalityScore: Int?,
)
