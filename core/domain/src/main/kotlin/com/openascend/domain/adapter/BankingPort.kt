package com.openascend.domain.adapter

/**
 * Future: Plaid or open banking. MVP: manual journal only.
 */
interface BankingPort {
    suspend fun readBankHealthSignal(epochDay: Long): Int?
}
