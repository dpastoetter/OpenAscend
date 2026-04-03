package com.openascend.domain.narrative

interface NarrativeRepository {
    suspend fun loadPack(packId: String): NarrativePack
}
