package io.pleo.antaeus.models

import java.time.LocalDate

data class Invoice(
    val id: Int,
    val customerId: Int,
    val amount: Money,
    val status: InvoiceStatus,
    val dueDate: LocalDate
)
