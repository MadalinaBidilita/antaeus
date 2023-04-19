package io.pleo.antaeus.models

import java.time.Instant

data class Transaction(
    val id: Int,
    val invoiceId: Int,
    val status: TransactionStatus,
    val reason: String,
    val timestamp: Instant
)
