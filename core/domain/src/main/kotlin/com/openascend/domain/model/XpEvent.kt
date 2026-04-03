package com.openascend.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class XpEvent(
    val id: Long,
    val timestampMillis: Long,
    val amount: Int,
    val reason: String,
)
