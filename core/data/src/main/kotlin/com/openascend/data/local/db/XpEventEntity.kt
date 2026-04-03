package com.openascend.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xp_events")
data class XpEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val amount: Int,
    val reason: String,
)
