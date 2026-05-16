package com.openascend.data.export

import kotlinx.serialization.Serializable

@Serializable
data class ChronicleDuelSummary(
    val schemaVersion: Int = 1,
    val displayName: String,
    val level: Int,
    val recovery: Int,
    val stamina: Int,
    val stability: Int,
    val discipline: Int,
    val vitality: Int,
    val bossName: String,
    val weekLabel: String,
)
